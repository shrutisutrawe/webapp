package com.neu.assignment.controller;

import com.neu.assignment.datalayer.FileHandlingRepo;
import com.neu.assignment.datalayer.FileHandlingRepoImplementation;
import com.neu.assignment.model.FileDetails;
import com.neu.assignment.service.GeneratePassword;
import com.neu.assignment.service.UserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class ResourceProvider {
    Logger logger = LoggerFactory.getLogger(ResourceProvider.class);
    private static final FileHandlingRepo fileHandlingRepo = new FileHandlingRepoImplementation();
    private static final GeneratePassword generatePassword = new GeneratePassword();
    private static final UserManagementService userService =
            new UserManagementService(fileHandlingRepo, generatePassword);

    @PostConstruct
    private void initialize() {
        try {
            userService.createUsersDataStorage();
        } catch (Exception e) {
            logger.error("Unexpected exception while initializing ResourceProvider. ", e);
            throw new RuntimeException(e);
        }
    }

    public static UserManagementService getUserManagementService() {
        return userService;
    }
}
