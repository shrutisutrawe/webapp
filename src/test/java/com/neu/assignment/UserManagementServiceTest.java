package com.neu.assignment;

import com.neu.assignment.datalayer.UserDatabaseRepo;
import com.neu.assignment.model.User;
import com.neu.assignment.service.GeneratePassword;
import com.neu.assignment.service.UserManagementService;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.test.context.junit4.SpringRunner;

import static com.neu.assignment.Constants.getMockUserCreateRequest;
import static com.neu.assignment.Constants.mockedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
public class UserManagementServiceTest {
    @InjectMocks
    UserManagementService userManagementService;
    @Mock
    UserDatabaseRepo userDatabaseRepo;
    @Mock
    GeneratePassword generatePassword;


    @Test
    public void createUserTest() {
        userDatabaseRepo = mock(UserDatabaseRepo.class);
        generatePassword = mock(GeneratePassword.class);
        userManagementService = new UserManagementService(userDatabaseRepo, generatePassword);

        Mockito.when(userDatabaseRepo.createUser(any(), any(), any(), any())).thenReturn(mockedUser());
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
