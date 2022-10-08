package com.neu.assignment;

import com.neu.assignment.controller.createUser.CreateUserRequest;
import com.neu.assignment.controller.updateUser.UpdateUserRequest;
import com.neu.assignment.model.User;

public class Constants {
   public static CreateUserRequest getMockUserCreateRequest() {
        User mockUser = mockedUser();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(mockUser.getUsername());
        createUserRequest.setFirst_name(mockUser.getFirst_name());
        createUserRequest.setLast_name(mockUser.getLast_name());
        createUserRequest.setPassword(mockUser.getPassword());

        return createUserRequest;
    }

    public static User mockedUser() {
        User mockUser = new User();
        mockUser.setId("c17941f4-26da-40f4-9fe3-d3a5b917b564");
        mockUser.setFirst_name("fname");
        mockUser.setLast_name("lname");
        mockUser.setUsername("lname.fname@example.com");
        mockUser.setPassword("password");
        mockUser.setAccount_created("2022-10-06T09:12:33.001Z");
        mockUser.setAccount_updated("2022-10-06T09:12:33.001Z");

        return mockUser;
    }

    public static UpdateUserRequest getUpdateUserMockRequest() {
        User mockUser = mockedUser();
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();

        updateUserRequest.setUsername(mockUser.getUsername());
        updateUserRequest.setPassword(mockUser.getPassword());
        updateUserRequest.setFirst_name(mockUser.getFirst_name());
        updateUserRequest.setLast_name(mockUser.getLast_name());

        return updateUserRequest;
    }

}
