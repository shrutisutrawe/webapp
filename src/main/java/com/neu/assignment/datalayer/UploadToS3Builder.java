package com.neu.assignment.datalayer;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.neu.assignment.controller.fileOperations.UploadFileRequest;
import com.neu.assignment.exceptions.WebappExceptions;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Repository
public class UploadToS3Builder {

    private static final Logger log = LoggerFactory.getLogger(UploadToS3Builder.class);

    private AmazonS3 s3Client;
    private String access_key="AKIA56PP3UDQ22ABRY7X";
    private String secret_key="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";

    public UploadToS3Builder() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(access_key, secret_key);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.US_WEST_2).build();
    }

    public String uploadFile(String bucketName, String userId, UploadFileRequest uploadFileRequest) {
        System.out.println("in upload to s3 bucket code");
        String s3Path = null;

        System.out.println("bucketname b4:");
        System.out.println(bucketName);
        try {
            System.out.println("in try block");
            if(s3Client.doesBucketExistV2(bucketName)){
                System.out.println("s3 yes exits");
            }
            if (!s3Client.doesBucketExistV2(bucketName)) {
                log.error("Bucket does not exists " + bucketName);
                throw new WebappExceptions("S3 bucket " + bucketName + " does not exists");
            }
            s3Path = userId + "/" + uploadFileRequest.getFileName();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(uploadFileRequest.getMultipartFile().getSize());
            s3Client.putObject(bucketName,
                    s3Path,
                    uploadFileRequest.getMultipartFile().getInputStream(),
                    metadata);
        } catch (SdkClientException | IOException e) {
            log.error("Exception while uploading file to S3 for user " + userId);
            e.printStackTrace();
            throw new WebappExceptions("Exception while uploading file to S3", e);
        }

        log.info("Uploaded file to S3 for user " + userId);
        return s3Path;
    }

    public void deleteFile(String bucketName, String s3Path) {
        if (!s3Client.doesBucketExistV2(bucketName)) {
            log.error("Bucket does not exists " + bucketName);
            throw new WebappExceptions("S3 bucket " + bucketName + " does not exists");
        }

        s3Client.deleteObject(bucketName, s3Path);
        log.info("Deleted file from S3. file path " + s3Path);
    }
}
