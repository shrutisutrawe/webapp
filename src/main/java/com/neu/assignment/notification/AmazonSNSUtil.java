package com.neu.assignment.notification;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neu.assignment.exceptions.WebappExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AmazonSNSUtil {
    private final AmazonSNS amazonSNS;
    private String newUserAccountVerificationTopicArn = null;
    private final String newUserAccountVerificationTopic = null;
    private String serviceHostedDNSName = null;
    private String port = "3000";

    Logger logger = LoggerFactory.getLogger(AmazonSNSUtil.class);

    public AmazonSNSUtil() {
        amazonSNS = AmazonSNSClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(true))
                .withRegion(Regions.US_WEST_2).build();
    }

    public void initialize(Map<String, String> configParameters) {
        String awsAccountId = configParameters.get("AWS_ACCOUNT_ID");
        String awsRegion = configParameters.get("AWS_REGION");
        logger.info("aws account id: " + awsAccountId);
        String newUserAccountVerificationTopic = configParameters.get("AWS_USER_EMAIL_VERIFICATION_SNS_TOPIC_NAME");
        serviceHostedDNSName = configParameters.get("AWS_HOSTED_ZONE_DNS");
        port = configParameters.get("PORT");

        logger.info("aws hosted zone:" + serviceHostedDNSName);
        logger.info("aws region :" + awsRegion);
        if (awsAccountId == null) {
            awsAccountId = "097171053993"; // for testing only use demo account
        }

        if (newUserAccountVerificationTopic == null) {
            newUserAccountVerificationTopic = "NewUserAccountVerificationTopic"; // for testing only use demo account
        }

        if (serviceHostedDNSName == null) {
            serviceHostedDNSName = "demo.devshrutisutrawe.me"; // for testing only use demo account
        }

        newUserAccountVerificationTopicArn = "arn:aws:sns:" + awsRegion + ":" + awsAccountId + ":" + newUserAccountVerificationTopic;
        logger.info("newUserAccountVerificationTopicArn");
        logger.info(newUserAccountVerificationTopicArn);
    }

    private boolean publish(String message, String topicArn) {
        PublishRequest publishRequest = new PublishRequest(topicArn, message);
        PublishResult publishResult = this.amazonSNS.publish(publishRequest);
        if (publishResult == null) {
            logger.error("Did not publish message to SNS");
            return false;
        }
        logger.info("publish result:");
        logger.info(publishResult.toString());
        logger.info("Published message to SNS - " + publishResult.getMessageId());
        return true;
    }

    public void notifyUserForAccountVerification(NotificationMessage notificationMessage) throws WebappExceptions {
        try {
            publish(buildUserEmailVerificationMessage(notificationMessage), newUserAccountVerificationTopicArn);
        } catch (Exception e) {
            throw new WebappExceptions("Exception while publishing message to SNS", e);
        }

        logger.info("Published message to send notification to user");
    }

    private String buildUserEmailVerificationMessage(NotificationMessage notificationMessage) throws JsonProcessingException {
        Map<String, String> messageMap = new HashMap<>();
        String verificationLink = "https://" + serviceHostedDNSName + ":" +
                "/v1/verifyUserEmail?" +
                "email=" + notificationMessage.getUsername() +
                "&" +
                "token=" + notificationMessage.getOneTimeVerificationToken();

        messageMap.put("recipient_email", notificationMessage.getUsername());
        messageMap.put("recipient_name", notificationMessage.getFirstName());
        messageMap.put("verification_link", verificationLink);
        messageMap.put("notification_mode", notificationMessage.getEmailVerificationNotification().toString());

        Gson gson = new GsonBuilder().create();
        String jsonString= gson.toJson(messageMap);
        return jsonString;
    }
}
