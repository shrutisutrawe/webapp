package com.neu.assignment.datalayer;

import com.neu.assignment.controller.fileOperations.UploadFileRequest;
import com.neu.assignment.exceptions.WebappExceptions;
import com.neu.assignment.model.FileDetails;
import com.neu.assignment.model.User;

import java.util.List;
import java.util.Map;

public interface FileHandlingRepo {
    public final String USERS_TABLE_NAME = "UsersTable";

    User createUser(String first_name, String last_name, String user_name, String password) throws WebappExceptions;
    void deleteUser(String username) throws WebappExceptions;
    User getUser(String user_name, String id) throws  WebappExceptions;
    User getUserByUserName(String user_name) throws  WebappExceptions;
    void updateUser(String first_name, String last_name, String password, String user_name, String id) throws WebappExceptions;

    List<User> getAllUsers();
    void createUsersTable() throws WebappExceptions;

    public final String FILE_UPLOAD_TABLE_NAME = "FileUploadTable";

    FileDetails addFileDetails(User user, String s3FilePath, UploadFileRequest uploadFileRequest) throws WebappExceptions;;
    void deleteFile(String userId, String docId) throws WebappExceptions;
    List<FileDetails> getAllFileDetails(String userId) throws WebappExceptions;
    FileDetails getFileDetailsByDocId(String userId, String docId) throws WebappExceptions;

    void createFileUploadTable() throws WebappExceptions;

    void initialize(Map<String, String> configParameters) throws WebappExceptions;

//    void setUserVerified(String username) throws WebappExceptions;
}
