package tanukkii.akka.cluster.aws.ec2metadata

import com.amazonaws.util.EC2MetadataUtils.InstanceInfo

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
}