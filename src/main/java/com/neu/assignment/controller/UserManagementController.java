package com.neu.assignment.controller;
import com.neu.assignment.controller.createUser.CreateUserRequest;
import com.neu.assignment.controller.fileOperations.UploadAllFileResponse;
import com.neu.assignment.controller.fileOperations.UploadFileRequest;
import com.neu.assignment.controller.fileOperations.UploadFileResponse;
import com.neu.assignment.controller.updateUser.UpdateUserRequest;
import com.neu.assignment.controller.createUser.CreateUserResponse;
import com.neu.assignment.controller.getUser.GetUserResponse;
import com.neu.assignment.metrics.WebappApplicationMetrics;
import com.neu.assignment.model.FileDetails;
import com.neu.assignment.model.User;
import com.neu.assignment.exceptions.WebappExceptions;
import com.neu.assignment.service.UserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path ="")
public class UserManagementController {
    private final UserManagementService userService;
    private final RequestHandlers requestHandlers;
    Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    WebappApplicationMetrics webAppApplicationMetrics;

    private static final String CREATE_NEW_USER_METRIC = "CreateUser";
    private static final String EXISTING_USER_METRIC = "ExistingUser";
    private static final String CREATE_USER_ERROR_METRIC = "CreateUserError";
    private static final String GET_USER_METRIC = "GetUser";
    private static final String GET_USER_ERROR_METRIC = "GetUserError";
    private static final String UPDATE_USER_METRIC = "UpdateUser";
    private static final String UPDATE_USER_ERROR_METRIC = "UpdateUserError";
    private static final String UPLOAD_DOCUMENT_METRIC = "UploadDocument";
    private static final String UPLOAD_FILE_ERROR_METRIC = "UploadFileError";
    private static final String GET_DOCUMENT_METRIC = "GetDocument";
    private static final String GET_FILE_ERROR_METRIC = "GetFileError";
    private static final String GET_DOCUMENT_WITH_DOCUMENT_ID_METRIC = "GetDocumentWithDocumentID";
    private static final String GET_FILE_WITH_FILE_ID_ERROR_METRIC = "GetFileWithFileIdError";
    private static final String DELETE_DOCUMENT_METRIC = "DeleteDocument";
    private static final String DELETE_FILE_ERROR_METRIC = "DeleteFileError";

    @PostConstruct
    private void initialize() {
        try {
            userService.createUsersDataStorage();

        } catch (Exception e) {
            logger.error("Unexpected exception while initializing user controller. ", e);
            throw new RuntimeException(e);
        }
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(-1);
        return multipartResolver;

    }

    @Autowired
    public UserManagementController(UserManagementService userService) {
        this.userService = userService;
        requestHandlers = new RequestHandlers();
    }

    public boolean checkValidUsername(String userName) {
        String regexPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(regexPattern)
                .matcher(userName)
                .matches();
    }

    @GetMapping(path= "/health")
    @ResponseStatus(HttpStatus.OK)
    public void getResponse(){
        return ;
    }

    //Create user request
    @PostMapping(path= "v1/account", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createUser(@RequestBody String createUserRequestPayload) {
        logger.info("UserManagementController: Called Create User API");
        logger.info(createUserRequestPayload);
        webAppApplicationMetrics.addCount(CREATE_NEW_USER_METRIC);
        // validate input user and send bad request
        CreateUserRequest createUserRequest = null;
        try {
            createUserRequest = requestHandlers.buildCreateUserRequest(createUserRequestPayload);
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Exception while parsing create user request.", e);
            return new ResponseEntity<Object>(
                    new ErrorMessages("Invalid create user request. It should have username, firstname, lastname and password"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!checkValidUsername(createUserRequest.getUsername())) {
            return new ResponseEntity<Object>(
                    new ErrorMessages("Invalid username. Enter valid email address (example@mail.com)"), HttpStatus.BAD_REQUEST);
        }

        User createdUser = null;
        CreateUserResponse createUserResponse = null;
        try {

            if (userService.userAlreadyExists(createUserRequest.getUsername())) {
                logger.info("Existing User");
                webAppApplicationMetrics.addCount(EXISTING_USER_METRIC);
                return new ResponseEntity<Object>(
                        new ErrorMessages("User already exists"), HttpStatus.BAD_REQUEST);
            }
            createdUser = userService.createUser(createUserRequest);
            createUserResponse = new CreateUserResponse(createdUser);
            logger.debug("User created successfully. User ID = " + createdUser.getId());
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Some unexpected exception occurred while creating user.", e);
            webAppApplicationMetrics.addCount(CREATE_USER_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred while creating user"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("UserManagementController: Some unexpected exception occurred while creating user.", e);
            webAppApplicationMetrics.addCount(CREATE_USER_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred while creating user"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("Returning create user response");
        return new ResponseEntity<Object>(createUserResponse, HttpStatus.CREATED);
    }

    //Get User Data Request
    @GetMapping(path= "v1/account/{userID}", produces = "application/json")
    public ResponseEntity<Object> getUser(@PathVariable(value="userID") String id, @RequestHeader HttpHeaders headers) {

        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            webAppApplicationMetrics.addCount(GET_USER_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        logger.info("UserManagementController: Get User API called for username " + userName);
        webAppApplicationMetrics.addCount(GET_USER_METRIC);

        User user = null;
        try { //user input validations
            user = userService.getUser(userName, id);
            if (user == null) {
                webAppApplicationMetrics.addCount(GET_USER_ERROR_METRIC);
                return new ResponseEntity<Object>(
                        new ErrorMessages("Invalid username or password. No such user found"),
                        HttpStatus.BAD_REQUEST);
            }
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Unexpected exception occurred while fetching user details. Exception - " + e.getMessage());
            webAppApplicationMetrics.addCount(GET_USER_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Internal service error encountered"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!userService.isUserVerified(user)) {
            return new ResponseEntity<Object>(
                    new ErrorMessages("User account is unverified"), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<Object>(new GetUserResponse(user), HttpStatus.OK);
    }

    //Update User Data Request
    @PutMapping(path="v1/account/{userID}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateUser(@PathVariable(value="userID") String id, @RequestBody String updateUserRequestPayload,
                                             @RequestHeader HttpHeaders headers) {

        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            webAppApplicationMetrics.addCount(UPDATE_USER_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("username in the header is empty"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!checkValidUsername(userName)) {
            webAppApplicationMetrics.addCount(UPDATE_USER_ERROR_METRIC);
            return new ResponseEntity<Object>(
                    new ErrorMessages("Invalid username. Enter valid email address (example@mail.com)"), HttpStatus.BAD_REQUEST);
        }

        // validate input user and send bad request
        logger.info("UserManagementController: Called Update User API");
        webAppApplicationMetrics.addCount(UPDATE_USER_METRIC);
        UpdateUserRequest updateUserRequest = null;
        try {
            updateUserRequest = requestHandlers.buildUpdateUserRequest(updateUserRequestPayload);
        } catch (WebappExceptions e) {
            webAppApplicationMetrics.addCount(UPDATE_USER_ERROR_METRIC);
            logger.error("UserManagementController: Exception while mapping update user request.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Invalid update user inputs"),
                    HttpStatus.BAD_REQUEST);
        }
//        String updatedUsername = updateUserRequest.getUsername();
        updateUserRequest.setUsername(userName);
        User updatedUser = null;
        try {
            User user = userService.getUserByUsername(userName);
            if (!userService.isUserVerified(user)) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User account is unverified"), HttpStatus.UNAUTHORIZED);
            }

            if (!userService.userAlreadyExists(updateUserRequest.getUsername())) {
                webAppApplicationMetrics.addCount(UPDATE_USER_ERROR_METRIC);
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + updateUserRequest.getUsername() + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }
            updatedUser = userService.updateUser(updateUserRequest, id);
            logger.debug("User created successfully. User ID = " + updatedUser.getId());
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Some unexpected exception occurred while updating user details.", e);
            webAppApplicationMetrics.addCount(UPDATE_USER_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred")
                    , HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }


    private String getUserNameFromAuthHeader(HttpHeaders headers) {
        System.out.println("eaders");
        System.out.println(headers);
        List<String> authorizationHeaders = headers.get((Object)"Authorization");
        if (authorizationHeaders.isEmpty()) {
            return null;
        }

        logger.info("authorizationHeaders.get(0) " + authorizationHeaders.get(0));
        // extract token from auth header
        // Basic auth header is : Basic amFuZS5kb2VAZXhhbXBsZS5jb206c2tkamZoc2tkZmpoZw==
        String authToken = authorizationHeaders.get(0).split(" ")[1];
        logger.info("authToken = " + authToken);

        byte[] decodedTokenBytes = Base64.getDecoder().decode(authToken);
        String decodedToken = new String(decodedTokenBytes, StandardCharsets.UTF_8);

        String[] splitToken = decodedToken.split(":");

        if (splitToken.length == 0) {
            return null;
        }

        return splitToken[0];
    }

    @PostMapping(path= "v1/documents",headers = ("content-type=multipart/*"), consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> addFile(@RequestParam("file") MultipartFile multipartFile,
                                                @RequestHeader HttpHeaders headers) throws IOException {
        logger.info("UserManagementController: Called Add File API");
        webAppApplicationMetrics.addCount(UPLOAD_DOCUMENT_METRIC);
        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            webAppApplicationMetrics.addCount(UPLOAD_FILE_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        UploadFileRequest uploadFileRequest = requestHandlers.buildUploadFileRequest(userName, multipartFile);
        FileDetails FileDetails=null;

        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                webAppApplicationMetrics.addCount(UPLOAD_FILE_ERROR_METRIC);
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }
            if (!userService.isUserVerified(user)) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User account is unverified"), HttpStatus.UNAUTHORIZED);
            }

            FileDetails = userService.uploadFile(uploadFileRequest);
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Some unexpected exception occurred while uploading document.", e);
            webAppApplicationMetrics.addCount(UPLOAD_FILE_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred while uploading document"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("UserManagementController: Some unexpected exception occurred while uploading document.", e);
            webAppApplicationMetrics.addCount(UPLOAD_FILE_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Object>(new UploadFileResponse(FileDetails), HttpStatus.OK);
    }

    @GetMapping(path= "v1/documents/{docID}")
    public ResponseEntity<Object> getFileDetailsWithFileID(@PathVariable(value="docID") String docId, @RequestHeader HttpHeaders headers) throws IOException {
        logger.info("UserManagementController: Called Get Specific File API");
        webAppApplicationMetrics.addCount(GET_DOCUMENT_WITH_DOCUMENT_ID_METRIC);
        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            webAppApplicationMetrics.addCount(GET_FILE_WITH_FILE_ID_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        FileDetails FileDetails = null;
        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                webAppApplicationMetrics.addCount(GET_FILE_WITH_FILE_ID_ERROR_METRIC);
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }

            if (!userService.isUserVerified(user)) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User account is unverified"), HttpStatus.UNAUTHORIZED);
            }

            FileDetails = userService.getFileDetailsFromDocId(user.getId(), docId );
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Some unexpected exception occurred while fetching specific document .", e);
            webAppApplicationMetrics.addCount(GET_FILE_WITH_FILE_ID_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("UserManagementController: Some unexpected exception occurred while fetching specific " +
                    "document .", e);
            webAppApplicationMetrics.addCount(GET_FILE_WITH_FILE_ID_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (FileDetails == null) {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Object>(new UploadFileResponse(FileDetails), HttpStatus.OK);
    }

    @GetMapping(path= "v1/documents")
    public HttpEntity<? extends Object> getAllFileDetails(@RequestHeader HttpHeaders headers) throws IOException {
        logger.info("Called Get All File API");
        webAppApplicationMetrics.addCount(GET_DOCUMENT_METRIC);
        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            webAppApplicationMetrics.addCount(GET_FILE_ERROR_METRIC);
            return new ResponseEntity<List<Object>>((List<Object>) new ErrorMessages("Cannot extract username when calling Get All files API"),
                    HttpStatus.BAD_REQUEST);
        }

        List<FileDetails> fileDetailsList = null;
        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                webAppApplicationMetrics.addCount(GET_FILE_ERROR_METRIC);
                return new ResponseEntity<List<Object>>(
                        (List<Object>) new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }
            if (!userService.isUserVerified(user)) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User account is unverified"), HttpStatus.UNAUTHORIZED);
            }

            fileDetailsList = userService.getFileDetails(user.getId());
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Some unexpected exception occurred while fetching all documents .");
            webAppApplicationMetrics.addCount(GET_FILE_ERROR_METRIC);
            e.printStackTrace();
            return new ResponseEntity<List<Object>>((List<Object>) new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("UserManagementController: Some unexpected exception occurred wile fetching all documents .");
            webAppApplicationMetrics.addCount(GET_FILE_ERROR_METRIC);
            e.printStackTrace();
            return new ResponseEntity<List<Object>>((List<Object>) new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (fileDetailsList == null) {
            return new ResponseEntity<List<Object>>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Object>(new UploadAllFileResponse(fileDetailsList), HttpStatus.OK);
    }

    @DeleteMapping(path= "v1/documents/{docID}")
    public ResponseEntity<Object> deleteProfilePic(@PathVariable(value="docID") String docId, @RequestHeader HttpHeaders headers) throws IOException {
        logger.info("Called delete file API");
        webAppApplicationMetrics.addCount(DELETE_DOCUMENT_METRIC);
        String userName = getUserNameFromAuthHeader(headers);
        System.out.println("In Delete: user name in header:" + userName);
        if (userName == null) {
            webAppApplicationMetrics.addCount(DELETE_FILE_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        boolean fileFound = false;
        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                webAppApplicationMetrics.addCount(DELETE_FILE_ERROR_METRIC);
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }

            if (!userService.isUserVerified(user)) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User account is unverified"), HttpStatus.UNAUTHORIZED);
            }

            fileFound = userService.deleteFile(user.getId(), docId);
        } catch (WebappExceptions e) {
            logger.error("UserManagementController: Some unexpected exception occurred while deleting document.", e);
            webAppApplicationMetrics.addCount(DELETE_FILE_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("UserManagementController: Some unexpected exception occurred while " +
                    "deleting document.", e);
            webAppApplicationMetrics.addCount(DELETE_FILE_ERROR_METRIC);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (fileFound) {
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(path= "v1/verifyUserEmail")
    public @ResponseBody String verifyUserEmail(@RequestParam("email") String userName,
                                                @RequestParam("token") String token){

        if (userName == null || token == null || userName.isEmpty() || token.isEmpty()) {
            return "Unable to verify user with email [" + userName + "]. No such user found.";
        }

        try {
            User user = userService.getUserByUsername(userName);

            if (user == null) {
                return "Unable to verify user with email " + userName + " No such user found.";
            }

            logger.info("user found");
            logger.info(user.toString());
            logger.info("username:" + user.getUsername());
            logger.info("password:" + user.getPassword());
            logger.info("firstname:" + user.getFirst_name());
            logger.info("lastname:" + user.getLast_name());
            logger.info("created:" + user.getAccount_created());
            logger.info("updated:" + user.getAccount_updated());
            logger.info("verified:" + user.getVerified());

            if (userService.isUserVerified(user)) {
                return "User Already Verified";
            }

            boolean verificationSuccessful = userService.verifyUser(userName, token);
            if (verificationSuccessful) {
                return "Successfully Verified user with email " + userName;
            }

            return "Token expired. Unable to verify user with email " + userName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to verify user with email " + userName + " Some internal service error occurred";
        }
    }
}
