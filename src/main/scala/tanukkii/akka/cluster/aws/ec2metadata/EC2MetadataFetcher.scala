package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor._
import akka.stream.ActorMaterializer
import tanukkii.akkahttp.aws.util.EC2MetaDataClient
import scala.concurrent.duration.FiniteDuration
import akka.pattern.pipe

private class EC2MetadataClientActor extends Actor {
  import EC2MetadataClientActor._
  import context.system
  import context.dispatcher

  implicit val materializer = ActorMaterializer(namePrefix = Some("ec2-metadata"))(context)

  val ec2MetadataClient = EC2MetaDataClient()

  def receive: Receive = {
    case FetchMetadata =>
      ec2MetadataClient.getInstanceInfo()
        .map(EC2InstanceInfo(_))
        .pipeTo(sender())
  }
}

private[ec2metadata] object EC2MetadataClientActor {
  case object FetchMetadata

  def props: Props = Props(new EC2MetadataClientActor)
}

private class EC2MetadataFetcher(sendTo: ActorRef, retryInterval: FiniteDuration) extends Actor with ActorLogging {
  import EC2MetadataFetcher._
  import context.dispatcher

  val client: ActorRef = context.actorOf(EC2MetadataClientActor.props)

  var cache: Option[EC2InstanceInfo] = None

  def receive: Receive = {
    case FetchEC2Metadata => client ! EC2MetadataClientActor.FetchMetadata
    case msg: EC2InstanceInfo =>
      cache = Some(msg)
      sendTo ! EC2MetadataFetched(msg)
    case Status.Failure(e) =>
      log.error(e, "Failed to fetch EC2 metadata. Retrying.")
      context.system.scheduler.scheduleOnce(retryInterval, client, EC2MetadataClientActor.FetchMetadata)
    case GetEC2Metadata(originalSender) =>
      sendTo ! MyEC2Metadata(cache, originalSender)
  }
}

private[ec2metadata] object EC2MetadataFetcher {
  case object FetchEC2Metadata
  case class EC2MetadataFetched(info: EC2InstanceInfo)
  case class GetEC2Metadata(originalSender: ActorRef)
  case class MyEC2Metadata(info: Option[EC2InstanceInfo], originalSender: ActorRef)

  def props(sendTo: ActorRef, retryInterval: FiniteDuration): Props = Props(new EC2MetadataFetcher(sendTo, retryInterval))
}