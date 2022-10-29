package com.neu.assignment.controller;
import com.neu.assignment.controller.createUser.CreateUserRequest;
import com.neu.assignment.controller.fileOperations.UploadAllFileResponse;
import com.neu.assignment.controller.fileOperations.UploadFileRequest;
import com.neu.assignment.controller.fileOperations.UploadFileResponse;
import com.neu.assignment.controller.updateUser.UpdateUserRequest;
import com.neu.assignment.controller.createUser.CreateUserResponse;
import com.neu.assignment.controller.getUser.GetUserResponse;
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
@RequestMapping(path ="/v1")
public class UserManagementController {
    private final UserManagementService userService;
    private final RequestHandlers requestHandlers;
    Logger logger = LoggerFactory.getLogger(UserManagementController.class);

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

    //Create user request
    @PostMapping(path= "/account", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createUser(@RequestBody String createUserRequestPayload) {
        logger.info("Called Create User API");
        logger.info(createUserRequestPayload);
        // validate input user and send bad request
        CreateUserRequest createUserRequest = null;
        try {
            createUserRequest = requestHandlers.buildCreateUserRequest(createUserRequestPayload);
        } catch (WebappExceptions e) {
            logger.error("Exception while parsing create user request.", e);
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
                return new ResponseEntity<Object>(
                        new ErrorMessages("User already exists"), HttpStatus.BAD_REQUEST);
            }
            createdUser = userService.createUser(createUserRequest);
            createUserResponse = new CreateUserResponse(createdUser);
            logger.debug("User created successfully. User ID = " + createdUser.getId());
        } catch (WebappExceptions e) {
            logger.error("Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("** Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("Returning create user response");
        return new ResponseEntity<Object>(createUserResponse, HttpStatus.CREATED);
    }

    //Get User Data Request
    @GetMapping(path= "/account/{userID}", produces = "application/json")
    public ResponseEntity<Object> getUser(@PathVariable(value="userID") String id, @RequestHeader HttpHeaders headers) {

        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        logger.info("Get User API called for username " + userName);

        User user = null;
        try { //user input validations
            user = userService.getUser(userName, id);
            if (user == null) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("Invalid username or password. No such user found"),
                        HttpStatus.BAD_REQUEST);
            }
        } catch (WebappExceptions e) {
            logger.error("Unexpected exception occurred. Exception - " + e.getMessage());
            return new ResponseEntity<Object>(new ErrorMessages("Internal service error encountered"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Object>(new GetUserResponse(user), HttpStatus.OK);
    }

    //Update User Data Request
    @PutMapping(path="/account/{userID}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> updateUser(@PathVariable(value="userID") String id, @RequestBody String updateUserRequestPayload,
                                             @RequestHeader HttpHeaders headers) {

        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            return new ResponseEntity<Object>(new ErrorMessages("username in the header is empty"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!checkValidUsername(userName)) {
            return new ResponseEntity<Object>(
                    new ErrorMessages("Invalid username. Enter valid email address (example@mail.com)"), HttpStatus.BAD_REQUEST);
        }

        // validate input user and send bad request
        logger.info("Called Update User API");
        UpdateUserRequest updateUserRequest = null;
        try {
            updateUserRequest = requestHandlers.buildUpdateUserRequest(updateUserRequestPayload);
        } catch (WebappExceptions e) {
            logger.error("Exception while mapping update user request.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Invalid update user inputs"),
                    HttpStatus.BAD_REQUEST);
        }
//        String updatedUsername = updateUserRequest.getUsername();
        updateUserRequest.setUsername(userName);
        User updatedUser = null;
        try {
            if (!userService.userAlreadyExists(updateUserRequest.getUsername())) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + updateUserRequest.getUsername() + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }
            updatedUser = userService.updateUser(updateUserRequest, id);
            logger.debug("User created successfully. User ID = " + updatedUser.getId());
        } catch (WebappExceptions e) {
            logger.error("Some unexpected exception occurred.", e);
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

    @PostMapping(path= "/documents",headers = ("content-type=multipart/*"), consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> addFile(@RequestParam("file") MultipartFile multipartFile,
                                                @RequestHeader HttpHeaders headers) throws IOException {
        logger.info("Called Add File API");
        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        UploadFileRequest uploadFileRequest = requestHandlers.buildUploadFileRequest(userName, multipartFile);
        FileDetails FileDetails=null;

        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }

            FileDetails = userService.uploadFile(uploadFileRequest);
        } catch (WebappExceptions e) {
            logger.error("Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("** Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Object>(new UploadFileResponse(FileDetails), HttpStatus.OK);
    }

    @GetMapping(path= "/documents/{docID}")
    public ResponseEntity<Object> getFileDetailsWithFileID(@PathVariable(value="docID") String docId, @RequestHeader HttpHeaders headers) throws IOException {
        logger.info("Called Get Specific File API");
        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        FileDetails FileDetails = null;
        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }

            FileDetails = userService.getFileDetailsFromDocId(user.getId(), docId );
        } catch (WebappExceptions e) {
            logger.error("Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("** Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (FileDetails == null) {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Object>(new UploadFileResponse(FileDetails), HttpStatus.OK);
    }

    @GetMapping(path= "/documents")
    public HttpEntity<? extends Object> getAllFileDetails(@RequestHeader HttpHeaders headers) throws IOException {
        logger.info("Called Get All File API");
        String userName = getUserNameFromAuthHeader(headers);
        if (userName == null) {
            return new ResponseEntity<List<Object>>((List<Object>) new ErrorMessages("Cannot extract username when calling Get All files API"),
                    HttpStatus.BAD_REQUEST);
        }

        List<FileDetails> fileDetailsList = null;
        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                return new ResponseEntity<List<Object>>(
                        (List<Object>) new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }

            fileDetailsList = userService.getFileDetails(user.getId());
        } catch (WebappExceptions e) {
            logger.error("Some unexpected exception occurred.");
            e.printStackTrace();
            return new ResponseEntity<List<Object>>((List<Object>) new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("** Some unexpected exception occurred.");
            e.printStackTrace();
            return new ResponseEntity<List<Object>>((List<Object>) new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (fileDetailsList == null) {
            return new ResponseEntity<List<Object>>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Object>(new UploadAllFileResponse(fileDetailsList), HttpStatus.OK);
    }

    @DeleteMapping(path= "/documents/{docID}")
    public ResponseEntity<Object> deleteProfilePic(@PathVariable(value="docID") String docId, @RequestHeader HttpHeaders headers) throws IOException {
        logger.info("Called delete file API");
        String userName = getUserNameFromAuthHeader(headers);
        System.out.println("In Delete: user name in header:" + userName);
        if (userName == null) {
            return new ResponseEntity<Object>(new ErrorMessages("Cannot extract username"),
                    HttpStatus.BAD_REQUEST);
        }

        boolean fileFound = false;
        try {

            User user = userService.getUserByUsername(userName);
            if (user == null) {
                return new ResponseEntity<Object>(
                        new ErrorMessages("User " + userName + " does not exists"),
                        HttpStatus.BAD_REQUEST);
            }

            fileFound = userService.deleteFile(user.getId(), docId);
        } catch (WebappExceptions e) {
            logger.error("Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("** Some unexpected exception occurred.", e);
            return new ResponseEntity<Object>(new ErrorMessages("Some internal service error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (fileFound) {
            return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }
}
