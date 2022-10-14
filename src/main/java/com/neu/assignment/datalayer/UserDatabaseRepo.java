package com.neu.assignment.datalayer;

import com.neu.assignment.exceptions.WebappExceptions;
import com.neu.assignment.model.User;

import java.util.List;

public interface UserDatabaseRepo {
    public final String UserDBName = "users_db";

    User createUser(String first_name, String last_name, String user_name, String password) throws WebappExceptions;
    User getUser(String user_name, String id) throws WebappExceptions;
    User getUserByUsername(String user_name) throws WebappExceptions;
    void updateUser(String first_name, String last_name, String password, String user_name, String id) throws WebappExceptions;

    List<User> getAllUsers();
    void createUsersTable() throws WebappExceptions;
}
