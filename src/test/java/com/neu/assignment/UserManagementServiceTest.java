package com.neu.assignment;

import com.neu.assignment.datalayer.FileHandlingRepo;
import com.neu.assignment.datalayer.UploadToS3Builder;
import com.neu.assignment.model.User;
import com.neu.assignment.service.GeneratePassword;
import com.neu.assignment.service.UserManagementService;

import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.neu.assignment.Constants.getMockUserCreateRequest;
import static com.neu.assignment.Constants.mockedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class UserManagementServiceTest {
    @InjectMocks
    UserManagementService userManagementService;

    @Mock
    FileHandlingRepo fileHandlingRepo;

    @Mock
    GeneratePassword generatePassword;

    @Mock
    UploadToS3Builder mockUploadToS3Builder;

    @Mock
    FileHandlingRepo mockFileHandlingRepo;


    @Test
    public void createUserTest() {
        fileHandlingRepo = mock(FileHandlingRepo.class);
        generatePassword = mock(GeneratePassword.class);
        userManagementService = new UserManagementService(fileHandlingRepo, generatePassword);

        Mockito.when(fileHandlingRepo.createUser(any(), any(), any(), any())).thenReturn(mockedUser());
        Mockito.when(generatePassword.encode(any())).thenReturn("expected_password");
        User user = userManagementService.createUser(getMockUserCreateRequest());

        Assert.assertEquals(user.getId(), mockedUser().getId());
        Assert.assertEquals(user.getUsername(), mockedUser().getUsername());
        Assert.assertEquals(user.getFirst_name(), mockedUser().getFirst_name());
        Assert.assertEquals(user.getLast_name(), mockedUser().getLast_name());
        Assert.assertEquals(user.getPassword(), mockedUser().getPassword());
        Assert.assertEquals(user.getAccount_created(), mockedUser().getAccount_created());
        Assert.assertEquals(user.getAccount_updated(), mockedUser().getAccount_updated());
    }
}
