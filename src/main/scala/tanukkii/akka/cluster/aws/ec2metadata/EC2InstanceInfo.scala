package tanukkii.akka.cluster.aws.ec2metadata

import com.amazonaws.util.EC2MetadataUtils.InstanceInfo
import com.typesafe.config.Config

case class EC2InstanceInfo(instanceId: String, instanceType: String, imageId: String, architecture: String, kernelId: String, region: String, availabilityZone: String, privateIp: String)

object EC2InstanceInfo {
  def apply(info: InstanceInfo): EC2InstanceInfo = EC2InstanceInfo(
    info.getInstanceId,
    info.getInstanceType,
    info.getImageId,
    info.getArchitecture,
    info.getKernelId,
    info.getRegion,
    info.getAvailabilityZone,
    info.getPrivateIp
  )

  def fromConfig(config: Config) = EC2InstanceInfo(
    config.getString("instance-id"),
    config.getString("instance-type"),
    config.getString("image-id"),
    config.getString("architecture"),
    config.getString("kernel-id"),
    config.getString("region"),
    config.getString("availability-zone"),
    config.getString("private-ip")
  )
}