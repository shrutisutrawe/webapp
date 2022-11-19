package com.neu.assignment.service;
import com.amazonaws.util.StringUtils;
import com.neu.assignment.controller.createUser.CreateUserRequest;
import com.neu.assignment.controller.fileOperations.UploadFileRequest;
import com.neu.assignment.controller.updateUser.UpdateUserRequest;

import com.neu.assignment.datalayer.FileHandlingRepo;
import com.neu.assignment.datalayer.FileHandlingRepoImplementation;
import com.neu.assignment.datalayer.UploadToS3Builder;
import com.neu.assignment.datalayer.AmazonDDB;
import com.neu.assignment.model.FileDetails;
import com.neu.assignment.model.User;
import com.neu.assignment.exceptions.WebappExceptions;
import com.neu.assignment.model.UserCredentials;
import com.neu.assignment.notification.AmazonSNSUtil;
import com.neu.assignment.notification.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.*;
import java.util.*;

import static com.neu.assignment.notification.NotificationMessageType.EMAIL_VERIFICATION_NOTIFICATION;

@Service
@ResponseStatus
public class UserManagementService implements UserDetailsService {

    Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    @Autowired
    GeneratePassword generatePassword;

    @Autowired
    UploadToS3Builder uploadToS3Builder;

    @Autowired
     AmazonDDB amazonDDB;

    @Autowired
    AmazonSNSUtil amazonSNSUtil;


    FileHandlingRepo fileHandlingRepo;

    String FileUploadS3BucketName;

    public UserManagementService() {
        this.fileHandlingRepo = new FileHandlingRepoImplementation();
    }

    public UserManagementService(FileHandlingRepo fileHandlingRepo, GeneratePassword generatePassword){
        this.generatePassword = generatePassword;
        this.uploadToS3Builder = new UploadToS3Builder();
        this.fileHandlingRepo = fileHandlingRepo;
    }

    String getRandomVerificationToken() {
        Random r = new Random();
        char[] array = new char[16];
        for (int count = 0; count < 16; count++) {
            array[count] = (char)(r.nextInt(26) + 'a');
        }
        return new String(array);
    }

    public void createUsersDataStorage() throws WebappExceptions, IOException {

        Map<String, String> configParameters = loadConfigurationParametersFromDisk();

        fileHandlingRepo.initialize(configParameters);
        this.FileUploadS3BucketName = configParameters.get("AWS_S3_BUCKET_NAME");
        amazonSNSUtil.initialize(configParameters);
    }

    public User createUser(CreateUserRequest createUserRequest) throws WebappExceptions {
        logger.info("create new user request called");

        User newUser =null;
        try{
        // Create new user
            createUserRequest.setPassword(generatePassword.encode(createUserRequest.getPassword()));
            newUser = fileHandlingRepo.createUser(createUserRequest.getFirst_name(),
                    createUserRequest.getLast_name(),
                    createUserRequest.getUsername(),
                    createUserRequest.getPassword());
            logger.info("New User Created = " + newUser.getId());
            if (newUser == null) {
                return newUser;
            }
            setUpOneTimeUserVerification(newUser);
        } catch (Exception e) {
            e.printStackTrace();
            if (newUser != null) {
                fileHandlingRepo.deleteUser(newUser.getUsername());
            }
            throw new WebappExceptions("Either already existing email id or Some exception while creating user", e);
        }
        return newUser;
    }

    public boolean userAlreadyExists(String userName) throws WebappExceptions {
        User alreadyExistingUser = getUserByUsername(userName);
        if (alreadyExistingUser == null) {
            return false; // user does not exists in DB
        }

        return true; // user exists in DB
    }

    public User getUser(String userName, String id) throws WebappExceptions {
        User alreadyExistingUser = fileHandlingRepo.getUser(userName,id);
        logger.info("Existing user:" + String.valueOf(alreadyExistingUser));
        return alreadyExistingUser;
    }

    public User getUserByUsername(String userName) throws WebappExceptions {
        if(StringUtils.isNullOrEmpty(userName)){
            return null;
        }
        User alreadyExistingUser = fileHandlingRepo.getUserByUserName(userName);
        logger.info("Existing user when only username is given:");
        logger.info(String.valueOf(alreadyExistingUser));
        return alreadyExistingUser;
    }

    public User updateUser(UpdateUserRequest updateUserRequest, String id) throws WebappExceptions {
        updateUserRequest.setPassword(generatePassword.encode(updateUserRequest.getPassword()));
        fileHandlingRepo.updateUser(updateUserRequest.getFirst_name(),
                updateUserRequest.getLast_name(),
                updateUserRequest.getPassword(),
                updateUserRequest.getUsername(),id);
        logger.info("Updated User Details - ");
        return fileHandlingRepo.getUser(updateUserRequest.getUsername(), id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username " + username + " does not exists");
        }

        user.setUsername(username);
        logger.info("User Details - " + user.getUsername() + "  password = " + user.getPassword());
        return new UserCredentials(user);
    }

    Map<String, String> loadConfigurationParametersFromDisk() throws WebappExceptions, IOException {

        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File("/etc/profile");
        InputStream inputStream = classLoader.getResourceAsStream("/opt/webapps/custom.properties");
        Map<String, String> configParameters = new HashMap<>();
        Properties mainProperties = new Properties();

        FileInputStream file;

        //the base folder is ./, the root of the main.properties file
        String path = "/opt/webapps/custom.properties";

        //load the file handle for main.properties
        file = new FileInputStream(path);

        //load all the properties from this file
        mainProperties.load(file);

        //we have loaded the properties, so close the file handle
        file.close();
        logger.info("UserManagementService: reading profile file");

        mainProperties.forEach((key,value)-> configParameters.put((String) key,(String) value));

        for (Object key: mainProperties.keySet()) {
            logger.info(key + ": " + mainProperties.getProperty(key.toString()));
        }

        return configParameters;
    }

    public FileDetails uploadFile(
            UploadFileRequest uploadFileRequest) throws WebappExceptions {

        User user = getUserByUsername(uploadFileRequest.getUserName());

        String s3Path = null;
        FileDetails fileDetails = null;
        try {
            logger.info("UserManagementService: Uploading new document");
            s3Path = uploadToS3Builder.uploadFile(FileUploadS3BucketName, user.getId(), uploadFileRequest);
            logger.info("UserManagementService: Adding new DB entry with S3 path - " + s3Path);
            fileDetails = fileHandlingRepo.addFileDetails(user, s3Path, uploadFileRequest);
        } catch (WebappExceptions e) {
            e.printStackTrace();
            logger.error("UserManagementService: Exception while adding file metadata. Removing entry from S3 and DB");
            uploadToS3Builder.deleteFile(FileUploadS3BucketName, s3Path);
            throw (e);
        }
        return fileDetails;
    }

    public FileDetails getFileDetailsFromDocId(String userId, String docId) throws WebappExceptions {
        return fileHandlingRepo.getFileDetailsByDocId(userId,docId);
    }

    public List<FileDetails> getFileDetails(String userId) throws WebappExceptions {
        return fileHandlingRepo.getAllFileDetails(userId);
    }

    public boolean deleteFile(String userId, String docId) throws WebappExceptions {
        FileDetails fileDetails = getFileDetailsFromDocId(userId, docId);
        if (fileDetails == null) {
            return false;
        }

        uploadToS3Builder.deleteFile(FileUploadS3BucketName, fileDetails.getS3_bucket_path());
        fileHandlingRepo.deleteFile(userId, fileDetails.getDoc_id());
        return true;
    }

    void setUpOneTimeUserVerification(User user) {
        String oneTimeVerificationToken = getRandomVerificationToken();
        System.out.println("One time verification token");
        System.out.println(oneTimeVerificationToken);

        boolean isNewUser = amazonDDB.uploadUserVerificationToken(user.getUsername(), oneTimeVerificationToken);
        System.out.println("In setup verification existin email is : " + isNewUser);
        if(!isNewUser){
            System.out.println("inside if is existin email");
            throw new WebappExceptions("Existing email id: " + user.getUsername());
        }
        System.out.println("exietin email outside if : " + isNewUser);
        amazonSNSUtil.notifyUserForAccountVerification(
                new NotificationMessage(
                        user.getUsername(),
                        user.getFirst_name(),
                        oneTimeVerificationToken,
                        EMAIL_VERIFICATION_NOTIFICATION));
    }

    public boolean isUserVerified(User user) {
        logger.info("inside is user verified");
        logger.info("username:" + user.getUsername());
        logger.info("verified:" + user.getVerified());
        String accountVerificationStatus = user.getVerified();
        return User.USER_ACCOUNT_VERIFIED_STATUS.equals(accountVerificationStatus);
    }

    public boolean verifyUser(String username, String oneTimeToken) {
        logger.info("inside verify user function");
        logger.info(username);
        logger.info(oneTimeToken);
        User user = getUserByUsername(username);
        if (user.getVerified().equals(User.USER_ACCOUNT_VERIFIED_STATUS)) {
            return true;
        }
        boolean verificationStatus = false;

        try {
            String expectedVerificationToken = amazonDDB.getUserVerificationToken(user.getUsername());
            String expectedVerificationTokenExpiry =
                    amazonDDB.getUserVerificationTokenExpiryTime(user.getUsername());
            int expectedVerificationTokenExpiryIntValue = Integer.parseInt(expectedVerificationTokenExpiry);
            System.out.println("before checking token is : " + (System.currentTimeMillis() / 1000L) );
            if((System.currentTimeMillis() / 1000L) > expectedVerificationTokenExpiryIntValue){
                logger.info("Token expired for username:" + username);
                return false;
            }
            verificationStatus = oneTimeToken.equals(expectedVerificationToken);

            if (verificationStatus == false) {
                logger.info("Verification token mismatch. Expected " + expectedVerificationToken + "  " +
                        "found " + oneTimeToken);
                return false;
            }

            fileHandlingRepo.setUserVerified(user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebappExceptions("Some exception while verifying user ", e);
        }

        return verificationStatus;
    }
}
