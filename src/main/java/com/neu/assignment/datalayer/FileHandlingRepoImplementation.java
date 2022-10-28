package com.neu.assignment.datalayer;

import com.neu.assignment.controller.fileOperations.UploadFileRequest;
import com.neu.assignment.exceptions.WebappExceptions;
import com.neu.assignment.model.FileDetails;
import com.neu.assignment.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class FileHandlingRepoImplementation extends JdbcDaoSupport implements FileHandlingRepo {

    private String dbEndpoint;
    private String dbName;
    private String dbUserName;
    private String dbPassword;
    private String dbPort;
    private static final String KEY_STORE_FILE_PATH = "/tmp/rds-ca-certs";
    private static final String KEY_STORE_PASS = "keyStorePassword";

    Logger logger = LoggerFactory.getLogger(FileHandlingRepoImplementation.class);

    public void initialize(Map<String, String> configParameters) throws WebappExceptions {
        this.dbEndpoint = configParameters.get("AWS_RDS_DB_ENDPOINT");
        this.dbName = configParameters.get("AWS_RDS_DB_NAME");
        this.dbUserName = configParameters.get("AWS_RDS_DB_MASTER_USERNAME");
        this.dbPassword = configParameters.get("AWS_RDS_DB_MASTER_PASSWORD");
        this.dbPort = configParameters.get("AWS_RDS_DB_PORT");
        createUsersTable();
        createFileUploadTable();
    }

    Connection getDBConnection() {
        try {
            logger.info("In getDB Connection............");
            logger.info("end point : " + dbEndpoint);
            logger.info("db name : " + dbName);
            logger.info("db username : " + dbUserName);
            logger.info("db pwd : " + dbPassword);
            logger.info("db port : " + dbPort);
//            System.setProperty("javax.net.ssl.trustStore", KEY_STORE_FILE_PATH);
//            System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASS);

//            Properties properties = new Properties();
//            properties.setProperty("sslMode", "VERIFY_IDENTITY");
//            properties.put("user", dbUserName);
//            properties.put("password", dbPassword);

//            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
//            String jdbcUrl = "jdbc:mysql://" + dbEndpoint + ":" + dbPort + "/"
//                    + dbName + "?user=" + dbUserName + "&password=" + dbPassword;
//            logger.info("Creating remote connection with connection string from environment variables - " + jdbcUrl);
//            Connection con = DriverManager.getConnection(jdbcUrl, properties);

            Connection con = DriverManager.getConnection("jdbc:mysql://" + dbEndpoint + ":" + dbPort + "/" + dbName ,dbUserName,dbPassword);

            logger.info("Remote DN connection successful.");
            return con;
        }
        catch (ClassNotFoundException e) {
            throw new WebappExceptions("Unexpected Exception while creating DB connection", e);
        } catch (SQLException e) {
            throw new WebappExceptions("Unexpected Exception while creating DB connection", e);
        }
    }

    @Override
    public User createUser(String first_name, String last_name, String user_name, String password) throws WebappExceptions {
        User newlyCreatedUser = new User();
        Instant currentTime = Instant.now();

        newlyCreatedUser.setFirst_name(first_name);
        newlyCreatedUser.setLast_name(last_name);
        newlyCreatedUser.setUsername(user_name);
        newlyCreatedUser.setPassword(password);
        newlyCreatedUser.setAccount_updated(currentTime.toString());
        newlyCreatedUser.setAccount_created(currentTime.toString());

        String userId = UUID.randomUUID().toString();
        newlyCreatedUser.setId(userId);

        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.INSERT_USER_QUERY)) {
            preparedStatement.setString(1, newlyCreatedUser.getId());
            preparedStatement.setString(2, newlyCreatedUser.getUsername());
            preparedStatement.setString(3, newlyCreatedUser.getFirst_name());
            preparedStatement.setString(4, newlyCreatedUser.getLast_name());
            preparedStatement.setString(5, newlyCreatedUser.getPassword());
            preparedStatement.setString(6, newlyCreatedUser.getAccount_created());
            preparedStatement.setString(7, newlyCreatedUser.getAccount_updated());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new WebappExceptions("Unexpected Exception while creating User", e);
        }

        logger.info("User " + user_name + " created in DB");
        return newlyCreatedUser;
    }

    @Override
    public void deleteUser(String username) throws WebappExceptions {
        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.DELETE_USER_QUERY)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new WebappExceptions("Unexpected Exception while deleting User", e);
        }
    }
//
//    @Override
//    public void setUserVerified(String username) throws WebappExceptions {
//        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.SET_USER_VERIFIED_QUERY)) {
//            preparedStatement.setString(1, User.USER_ACCOUNT_VERIFIED_STATUS);
//            preparedStatement.setString(2, username);
//            preparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            throw new WebappExceptions("Unexpected Exception setting user as verified", e);
//        }
//    }

    @Override
    public User getUser(String user_name, String id) throws WebappExceptions {
        User user = null;

        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.SELECT_USER_QUERY)) {
            preparedStatement.setString(1, user_name);
            preparedStatement.setString(2, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            try {
                while(resultSet.next()) {
                    user = new User();
                    user.setUsername(resultSet.getString("user_name"));
                    user.setLast_name(resultSet.getString("last_name"));
                    user.setFirst_name(resultSet.getString("first_name"));
                    user.setPassword(resultSet.getString("password"));
                    user.setId(resultSet.getString("id"));
                    user.setAccount_updated(resultSet.getString("account_updated"));
                    user.setAccount_created(resultSet.getString("account_created"));
                }
            } catch (Exception e) {
                logger.error("No User details. Returning null user", e);
                return null;
            }
        } catch (SQLException e) {
            logger.error("Unexpected Exception while fetching User " + user_name, e);
            throw new WebappExceptions("Unexpected Exception while fetching User", e);
        }
        logger.info("User " + user_name + " info fetched from DB");
        return user;
    }

    @Override
    public User getUserByUserName(String user_name) throws WebappExceptions {
        User user = null;

        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.SELECT_USER_BY_USERNAME_QUERY)) {
            preparedStatement.setString(1, user_name);
            ResultSet resultSet = preparedStatement.executeQuery();
            try {
                while(resultSet.next()) {
                    user = new User();
                    user.setUsername(resultSet.getString("user_name"));
                    user.setLast_name(resultSet.getString("last_name"));
                    user.setFirst_name(resultSet.getString("first_name"));
                    user.setPassword(resultSet.getString("password"));
                    user.setId(resultSet.getString("id"));
                    user.setAccount_updated(resultSet.getString("account_updated"));
                    user.setAccount_created(resultSet.getString("account_created"));
                }
            } catch (Exception e) {
                logger.error("No User details. Returning null user", e);
                return null;
            }
        } catch (SQLException e) {
            logger.error("Unexpected Exception while fetching User " + user_name, e);
            throw new WebappExceptions("Unexpected Exception while fetching User", e);
        }
        logger.info("User " + user_name + " info fetched from DB");
        return user;
    }

    @Override
    public void updateUser(String first_name, String last_name, String password, String user_name, String id) throws WebappExceptions {
        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.UPDATE_USER_QUERY)) {
            preparedStatement.setString(1, first_name);
            preparedStatement.setString(2, last_name);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, Instant.now().toString());
            preparedStatement.setString(5, user_name);
            preparedStatement.setString(6, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Unexpected Exception while updating User " + user_name, e);
            throw new WebappExceptions("Unexpected Exception while updating User " + user_name, e);
        }

        logger.info("User " + user_name + " updated in DB");
    }

    @Override
    public List<User> getAllUsers() {
        return null;
    }

    @Override
    public void createUsersTable() throws WebappExceptions {
        try(Connection dbConnection = getDBConnection()) {
            Statement setupStatement = dbConnection.createStatement();
            setupStatement.addBatch(Queries.CREATE_USERS_TABLE_QUERY);
            setupStatement.executeBatch();
            setupStatement.close();
        } catch (SQLException e) {
            throw new WebappExceptions("Exception while create users table", e);
        }
        logger.info("Users table created");
    }

    @Override
    public FileDetails addFileDetails(User user, String s3FilePath, UploadFileRequest uploadFileRequest) throws WebappExceptions {
        // FileDetails FileDetails = alterFileDetails(user, s3FilePath, UploadFileRequest, Queries.ADD_TO_IMAGE_TABLE_QUERY);

        String docId = UUID.randomUUID().toString();
        FileDetails FileDetails = new FileDetails(
                docId, user.getId(), uploadFileRequest.getFileName(),
                s3FilePath, uploadFileRequest.getDate_created());
        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.UPLOAD_FILE_TO_TABLE_QUERY)) {
            preparedStatement.setString(1, FileDetails.getDoc_id());
            preparedStatement.setString(2, FileDetails.getUser_id());
            preparedStatement.setString(3, FileDetails.getFile_name());
            preparedStatement.setString(4, FileDetails.getS3_bucket_path());
            preparedStatement.setString(5, FileDetails.getDate_created());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Unexpected Exception while uploading file for user " + user.getUsername(), e);
            throw new WebappExceptions("Unexpected Exception while uploading file for user " + user.getUsername(), e);
        }
        return FileDetails;
    }

    @Override
    public void deleteFile(String userId, String docId) throws WebappExceptions {
        System.out.println("In delete file repo:");
        System.out.println("userID:" + userId);
        System.out.println("docID:" + docId);
        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.DELETE_FILE_QUERY)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, docId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Unexpected Exception while deleting file for user " + userId, e);
            throw new WebappExceptions("Unexpected Exception while deleting file for user " + userId, e);
        }
        logger.info("File Deleted for user id " + userId);
    }

    @Override
    public List<FileDetails> getAllFileDetails(String userId) throws WebappExceptions {
        FileDetails FileDetails = null;
        List<FileDetails> fileDetailsList = new ArrayList<>();

        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.SELECT_ALL_FILE_QUERY)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            try {
                while(resultSet.next()) {
                    FileDetails = new FileDetails();
                    FileDetails.setDoc_id(resultSet.getString("doc_id"));
                    FileDetails.setS3_bucket_path(resultSet.getString("s3_bucket_path"));
                    FileDetails.setUser_id(resultSet.getString("user_id"));
                    FileDetails.setFile_name(resultSet.getString("file_name"));
                    FileDetails.setDate_created(resultSet.getString("date_created"));
                    assert false;
                    fileDetailsList.add(new FileDetails(FileDetails.getDoc_id(),FileDetails.getUser_id(),
                            FileDetails.getFile_name(), FileDetails.getS3_bucket_path(), FileDetails.getDate_created()));
                }
            } catch (Exception e) {
                logger.error("Exception while getting file details. Returning empty data ", e);
                return null;
            }

        } catch (SQLException e) {
            logger.error("Unexpected Exception while fetching file details for user id " + userId, e);
            throw new WebappExceptions("Unexpected Exception while file details for user id " + userId, e);
        }
        logger.info("file details for user id are fetched successfully " + userId);
        return fileDetailsList;
    }

    @Override
    public FileDetails getFileDetailsByDocId(String userId, String docId) throws WebappExceptions {
        FileDetails FileDetails = null;

        try (PreparedStatement preparedStatement = getDBConnection().prepareStatement(Queries.SELECT_FILE_BY_ID_QUERY)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, docId);
            ResultSet resultSet = preparedStatement.executeQuery();
            try {
                while(resultSet.next()) {
                    FileDetails = new FileDetails();
                    FileDetails.setDoc_id(resultSet.getString("doc_id"));
                    FileDetails.setS3_bucket_path(resultSet.getString("s3_bucket_path"));
                    FileDetails.setUser_id(resultSet.getString("user_id"));
                    FileDetails.setFile_name(resultSet.getString("file_name"));
                    FileDetails.setDate_created(resultSet.getString("date_created"));
                }
            } catch (Exception e) {
                logger.error("Exception while getting file details. Returning empty data ", e);
                return null;
            }

        } catch (SQLException e) {
            logger.error("Unexpected Exception while fetching file details for user id " + userId, e);
            throw new WebappExceptions("Unexpected Exception while file details for user id " + userId, e);
        }
        logger.info("file details for user id are fetched successfully " + userId);
        return FileDetails;
    }

    @Override
    public void createFileUploadTable() throws WebappExceptions {
        try(Connection dbConnection = getDBConnection()) {
            Statement setupStatement = dbConnection.createStatement();
            setupStatement.addBatch(Queries.CREATE_FILEUPLOAD_TABLE_QUERY);
            setupStatement.executeBatch();
            setupStatement.close();
        } catch (SQLException e) {
            throw new WebappExceptions("Exception while create file upload table", e);
        }

        logger.info("File upload table created");
    }
}
