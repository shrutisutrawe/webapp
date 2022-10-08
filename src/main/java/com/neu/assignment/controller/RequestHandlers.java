package com.neu.assignment.controller;

import com.neu.assignment.exceptions.WebappExceptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.assignment.controller.createUser.CreateUserRequest;
import com.neu.assignment.controller.updateUser.UpdateUserRequest;

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
}
