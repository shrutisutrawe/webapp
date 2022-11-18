package com.neu.assignment.datalayer;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AmazonDDB {
    private AmazonDynamoDB ddbClient;
    DynamoDB dynamoDB;
    Table userAccountVerificationTable;

    String USER_ACCOUNT_VERIFICATION_TABLE_NAME = "UserAccountVerificationTokenTable";

    Logger logger = LoggerFactory.getLogger(AmazonDDB.class);
    private Item item;

    public AmazonDDB() {
        ddbClient = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(true))
                .withRegion(Regions.US_WEST_2).build();
        dynamoDB = new DynamoDB(ddbClient);
        userAccountVerificationTable = dynamoDB.getTable(USER_ACCOUNT_VERIFICATION_TABLE_NAME);
        logger.info("DDB client created");
    }

    public void uploadUserVerificationToken(String username, String oneTimeVerificationToken) {
        logger.info("In upload user verification token");
        System.out.println("username: " +username);
        System.out.println("token : "+ oneTimeVerificationToken);
        final Map<String, Object> verificationMetadataMap = new HashMap<String,
                Object>();
        verificationMetadataMap.put("one_time_verification_token",
                oneTimeVerificationToken);
        verificationMetadataMap.put("token_expiry_time_epoch",
                ((System.currentTimeMillis() / 1000L) + 120));

        Item item = new Item().withPrimaryKey("email", username)
                .withMap("verification_metadata", verificationMetadataMap);

        PutItemOutcome outcome = userAccountVerificationTable.putItem(item);
        logger.info("userAccountVerificationTable outcome: " + outcome.toString());
        logger.info("Upload user verification token result");
    }

    public String getUserVerificationToken(String username) {
        logger.info("In get user verification token");
        System.out.println("username :" + username);
        GetItemRequest request = new GetItemRequest();
        request.setTableName(USER_ACCOUNT_VERIFICATION_TABLE_NAME);
        request.setConsistentRead(true);

        Map<String, AttributeValue> keysMap = new HashMap<>();
        logger.error("GETTING TOKEN FOR KEY : " + username);
        keysMap.put("email", new AttributeValue(username));
        request.setKey(keysMap);

        GetItemResult result = ddbClient.getItem(request);
        logger.info("DDB Get user verification token response = " + result.getSdkHttpMetadata().getHttpStatusCode());

        Map<String, AttributeValue> responseMap = result.getItem();
        if (responseMap == null) {
            logger.error("Get verification token from DDB returned empty response");
            return "";
        }

        AttributeValue verificationMetadataAttribute = responseMap.get("verification_metadata");
        if (verificationMetadataAttribute == null) {
            logger.error("No attribute named verification_metadata present in DDB response. This should never happen.");
            return "";
        }

        Map<String, AttributeValue> verificationMetadataAttributeMap = verificationMetadataAttribute.getM();
        if (verificationMetadataAttributeMap == null) {
            logger.error("Attribute map for verification_metadata is null in DDB response. This should never happen.");
            return "";
        }

        AttributeValue oneTimeVerificationTokenAttribute = verificationMetadataAttributeMap
                .get("one_time_verification_token");
        if (oneTimeVerificationTokenAttribute == null) {
            logger.error(
                    "oneTimeVerificationTokenAttribute for verification_metadata is null in DDB response. This should never happen.");
            return "";
        }

        String token = oneTimeVerificationTokenAttribute.getS();
        logger.info("verificationToken from DDB: " + token);

        return token;
    }

    public String getUserVerificationTokenExpiryTime(String username) {
        logger.info("In get user verification token Expiry time");
        System.out.println("username :" + username);
        GetItemRequest request = new GetItemRequest();
        request.setTableName(USER_ACCOUNT_VERIFICATION_TABLE_NAME);
        request.setConsistentRead(true);

        Map<String, AttributeValue> keysMap = new HashMap<>();
        logger.error("GETTING TOKEN EXPIRY TIME FOR KEY : " + username);
        keysMap.put("email", new AttributeValue(username));
        request.setKey(keysMap);

        GetItemResult result = ddbClient.getItem(request);
        logger.info("DDB Get user verification token response = " + result.getSdkHttpMetadata().getHttpStatusCode());

        Map<String, AttributeValue> responseMap = result.getItem();
        if (responseMap == null) {
            logger.error("Get verification token expiry time from DDB returned empty response");
            return "";
        }

        AttributeValue verificationMetadataAttribute = responseMap.get("verification_metadata");
        if (verificationMetadataAttribute == null) {
            logger.error("No attribute named verification_metadata present in DDB response. This should never happen.");
            return "";
        }

        Map<String, AttributeValue> verificationMetadataAttributeMap = verificationMetadataAttribute.getM();
        if (verificationMetadataAttributeMap == null) {
            logger.error("Attribute map for verification_metadata is null in DDB response. This should never happen.");
            return "";
        }

        AttributeValue oneTimeVerificationTokenExpiry = verificationMetadataAttributeMap
                .get("token_expiry_time_epoch");
        if (oneTimeVerificationTokenExpiry == null) {
            logger.error(
                    "oneTimeVerificationTokenExpiry for verification_metadata is null in DDB response. This should never happen.");
            return "";
        }

        String tokenExpiry = oneTimeVerificationTokenExpiry.getN();
        logger.info("verificationToken expiry time from DDB: " + tokenExpiry);

        return tokenExpiry;
    }
}
