package com.neu.assignment.datalayer;

import com.neu.assignment.exceptions.WebappExceptions;
import com.neu.assignment.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class UserDatabaseRepoImplementation extends JdbcDaoSupport implements UserDatabaseRepo {
    Logger logger = LoggerFactory.getLogger(UserDatabaseRepoImplementation.class);

    @Autowired
    DataSource dataSource;

    final String createTableQuery = "create table if not exists users_db (" +
            "id varchar(100) NOT NULL, " +
            "user_name varchar(100) PRIMARY KEY, " +
            "first_name varchar(100) NOT NULL, " +
            "last_name varchar(100) NOT NULL, " +
            "password varchar(100) NOT NULL, " +
            "account_created varchar(100) NOT NULL, " +
            "account_updated varchar(100) NOT NULL )";

    String insertUserQuery = "insert into users_db (" +
            " id, user_name, first_name, last_name, password, account_created, account_updated) " +
            " values (?,?,?,?,?,?,?)";
    final String selectUserQuery = "select * from users_db where user_name = ? AND id = ?";
    final String selectUserQueryByUsername = "select * from users_db where user_name = ?";
    final String updateUserQuery = "update users_db set " +
            "first_name = ?, last_name = ?, password = ?, account_updated = ? where user_name = ? AND id = ?";

    @PostConstruct
    private void initialize(){
        setDataSource(dataSource);
    }

    // create users table
    @Override
    public void createUsersTable() throws WebappExceptions {
        try {
            getJdbcTemplate().execute(createTableQuery);
        } catch (Exception e) {
            throw new WebappExceptions("Unexpected exception while creating Users database");
        }

        logger.debug("Users database is successfully created");
    }

    //Get user data
    @Override
    public User getUser(String user_name, String id) throws WebappExceptions {
        User user = null;

        try {
            user = (User) getJdbcTemplate().queryForObject(selectUserQuery,
                    new Object[]{user_name, id},
                    new BeanPropertyRowMapper(User.class));
        } catch (EmptyResultDataAccessException e) {
            logger.debug("User with username " + user_name + " was not found");
        } catch (Exception e) {
            throw new WebappExceptions(
                    "Unexpected exception occurred while fetching user with username " + user_name, e);
        }

        if (user != null) {
            user.setUsername(user_name);
        }

        return user;
    }

    @Override
    public User getUserByUsername(String user_name) throws WebappExceptions {
        User user = null;

        try {
            user = (User) getJdbcTemplate().queryForObject(selectUserQueryByUsername,
                    new Object[]{user_name},
                    new BeanPropertyRowMapper(User.class));
        } catch (EmptyResultDataAccessException e) {
            logger.debug("User with username " + user_name + " was not found");
        } catch (Exception e) {
            throw new WebappExceptions(
                    "Unexpected exception occurred while fetching user with username " + user_name, e);
        }

        if (user != null) {
            user.setUsername(user_name);
        }

        return user;
    }

    //create user
    @Override
    public User createUser(String first_name, String last_name, String user_name, String password) throws WebappExceptions {
        User newUser = new User();
        Instant currentTime = Instant.now();

        String userId = UUID.randomUUID().toString();
        newUser.setId(userId);
        newUser.setUsername(user_name);
        newUser.setPassword(password);
        newUser.setFirst_name(first_name);
        newUser.setLast_name(last_name);
        newUser.setAccount_updated(currentTime.toString());
        newUser.setAccount_created(currentTime.toString());

        int rowsUpdated = 0;
        try {
            rowsUpdated = getJdbcTemplate().update(insertUserQuery,
                    newUser.getId(),
                    newUser.getUsername(),
                    newUser.getFirst_name(),
                    newUser.getLast_name(),
                    newUser.getPassword(),
                    newUser.getAccount_created(),
                    newUser.getAccount_updated());
        } catch (Exception e) {
            throw new WebappExceptions(
                    "Unexpected exception while creating user with username " + user_name, e);
        }

        if (rowsUpdated != 1) {
            throw new WebappExceptions(
                    "Unexpected error while creating user. Rows updated should be one.");
        }

        logger.debug("User with " + user_name + " has been successfully created.");
        return newUser;
    }

    //update user
    @Override
    public void updateUser(String first_name, String last_name, String password, String user_name, String id) {
        Instant currentTime = Instant.now();
        int rowsUpdated = 0;
        try {
            rowsUpdated = getJdbcTemplate().update(updateUserQuery,
                    first_name, last_name, password, currentTime.toString(), user_name, id);
        } catch (Exception e) {
            throw new WebappExceptions(
                    "Unexpected exception while updating user with username " + user_name, e);
        }

        if (rowsUpdated != 1) {
            throw new WebappExceptions(
                    "Unexpected error while updating user. Rows updated should be one.");
        }

        logger.debug("User with " + user_name + " has been updated successfully.");
    }

    @Override
    public List<User> getAllUsers() {
        return null;
    }

}
