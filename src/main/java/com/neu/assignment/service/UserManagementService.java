package com.neu.assignment.service;
import com.neu.assignment.controller.createUser.CreateUserRequest;
import com.neu.assignment.controller.updateUser.UpdateUserRequest;
import com.neu.assignment.datalayer.UserDatabaseRepo;
import com.neu.assignment.model.User;
import com.neu.assignment.exceptions.WebappExceptions;
import com.neu.assignment.model.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Service
@ResponseStatus
public class UserManagementService implements UserDetailsService {

    Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    @Autowired
    UserDatabaseRepo userDatabaseRepo;
    @Autowired
    GeneratePassword generatePassword;

    public UserManagementService(UserDatabaseRepo userDatabaseRepo, GeneratePassword generatePassword) {
        this.userDatabaseRepo = userDatabaseRepo;
        this.generatePassword = generatePassword;
    }

    public void createUsersDataStorage() throws WebappExceptions {
        userDatabaseRepo.createUsersTable();
    }

    public User createUser(CreateUserRequest createUserRequest) throws WebappExceptions {
        logger.info("create new user dao called");

        // Create new user
        createUserRequest.setPassword(generatePassword.encode(createUserRequest.getPassword()));
        User newUser = userDatabaseRepo.createUser(createUserRequest.getFirst_name(),
                createUserRequest.getLast_name(),
                createUserRequest.getUsername(),
                createUserRequest.getPassword());
        logger.info("Created new user = " + newUser.getId());

        return newUser;
    }

    public boolean userAlreadyExists(String userName) throws WebappExceptions {
        User alreadyExistingUser = getUser(userName);
        if (alreadyExistingUser == null) {
            return false; // user does not exists in DB
        }

        return true; // user exists in DB
    }

    public User getUser(String userName) throws WebappExceptions {
        User alreadyExistingUser = userDatabaseRepo.getUser(userName);
        logger.info(String.valueOf(alreadyExistingUser));
        return alreadyExistingUser;
    }

    public User updateUser(UpdateUserRequest updateUserRequest) throws WebappExceptions {
        updateUserRequest.setPassword(generatePassword.encode(updateUserRequest.getPassword()));
        userDatabaseRepo.updateUser(updateUserRequest.getFirst_name(),
                updateUserRequest.getLast_name(),
                updateUserRequest.getPassword(),
                updateUserRequest.getUsername());
        logger.info("Updated User Details - ");
        return userDatabaseRepo.getUser(updateUserRequest.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username " + username + " does not exists");
        }

        user.setUsername(username);
        logger.info("User Details - " + user.getUsername() + "  password = " + user.getPassword());
        return new UserCredentials(user);
    }
}
