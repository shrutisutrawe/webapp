package com.neu.assignment.controller;

import com.neu.assignment.controller.fileOperations.UploadFileRequest;
import com.neu.assignment.exceptions.WebappExceptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.assignment.controller.createUser.CreateUserRequest;
import com.neu.assignment.controller.updateUser.UpdateUserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

public class RequestHandlers {
    private ObjectMapper objectMapper;

    public RequestHandlers() {
        objectMapper = new ObjectMapper();
    }

    public CreateUserRequest buildCreateUserRequest(String createUserRequestPayload) throws WebappExceptions {
        CreateUserRequest createUserRequest = null;
        try {
            createUserRequest = objectMapper.readValue(createUserRequestPayload, CreateUserRequest.class);
        } catch (Exception e) {
            throw new WebappExceptions("Exception while mapping create user request", e);
        }
        return createUserRequest;
    }

    public UpdateUserRequest buildUpdateUserRequest(String requestPayload) throws WebappExceptions {
        UpdateUserRequest updateUserRequest = null;
        try {
            updateUserRequest = objectMapper.readValue(requestPayload, UpdateUserRequest.class);
        } catch (Exception e) {
            throw new WebappExceptions("Exception while mapping update user request", e);
        }
        return updateUserRequest;
    }

    public UploadFileRequest buildUpdateFileRequest(String fileUploadRequest) throws WebappExceptions {
        UploadFileRequest updateFileRequest = null;
        System.out.println("File Request:");
        System.out.println(fileUploadRequest);
        try {
            updateFileRequest = objectMapper.readValue(fileUploadRequest, UploadFileRequest.class);
        } catch (Exception e) {
            throw new WebappExceptions("Exception while mapping file upload request ", e);
        }
        return updateFileRequest;
    }
    public UploadFileRequest buildUploadFileRequest(String username, MultipartFile multipartFile) throws WebappExceptions {
        System.out.println("in upload file request");
        System.out.println(multipartFile);
        UploadFileRequest UploadFileRequest = new UploadFileRequest();
        UploadFileRequest.setMultipartFile(multipartFile);
        UploadFileRequest.setFileName(multipartFile.getOriginalFilename());
        UploadFileRequest.setDate_created(Instant.now().toString());
        UploadFileRequest.setUserName(username);
        return UploadFileRequest;
    }
}
