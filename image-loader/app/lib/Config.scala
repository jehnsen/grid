package lib

import com.amazonaws.auth.{BasicAWSCredentials, AWSCredentials}
import com.gu.mediaservice.lib.config.Properties


object Config {

  val properties = Properties.fromFile("/etc/gu/image-loader.properties")

  val awsCredentials: AWSCredentials =
    new BasicAWSCredentials(properties("aws.id"), properties("aws.secret"))

  val topicArn: String = properties("sns.topic.arn")

  val imageBucket: String = properties("s3.image.bucket")

  val thumbnailBucket: String = properties("s3.thumb.bucket")

  val tempDir = properties.getOrElse("upload.tmp.dir", "/tmp")

}
