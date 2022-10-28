package com.neu.assignment.datalayer;

public class Queries {

    public static String CREATE_USERS_TABLE_QUERY = "create table if not exists UsersTable (" +
            "id varchar(60) NOT NULL, " +
            "user_name varchar(60) PRIMARY KEY, " +
            "first_name varchar(60) NOT NULL, " +
            "last_name varchar(60) NOT NULL, " +
            "password varchar(60) NOT NULL, " +
            "account_created varchar(60) NOT NULL, " +
            "account_updated varchar(60) NOT NULL)";

    public static String CREATE_FILEUPLOAD_TABLE_QUERY = "create table if not exists FileTable (" +
            "doc_id varchar(60)  PRIMARY KEY, " +
            "user_id varchar(60) NOT NULL, " +
            "file_name varchar(60) NOT NULL, " +
            "s3_bucket_path varchar(200) NOT NULL, " +
            "date_created varchar(60) NOT NULL)";

    public static String INSERT_USER_QUERY = "insert into UsersTable (" +
            " id, user_name, first_name, last_name, password, account_created, account_updated) " +
            " values (?,?,?,?,?,?,?)";

    public static String DELETE_USER_QUERY = "delete from UsersTable where user_name = ?";

    public static String SELECT_USER_QUERY = "select * from UsersTable where user_name = ? AND id = ?";
    public static String SELECT_USER_BY_USERNAME_QUERY = "select * from UsersTable where user_name = ?";
    public static String UPDATE_USER_QUERY = "update UsersTable set " +
            "first_name = ?, last_name = ?, password = ?, account_updated = ? where user_name = ? AND id = ?";
//
//    public static String SET_USER_VERIFIED_QUERY = "update UsersTable set " +
//            " account_verified = ? where user_name = ?";

    //Image table
    public static String UPLOAD_FILE_TO_TABLE_QUERY =
            "insert into FileTable (doc_id, user_id, file_name, s3_bucket_path, date_created) values (?,?,?,?,?)";

    public static String SELECT_ALL_FILE_QUERY = "select * FROM FileTable where user_id = ?";

    public static String SELECT_FILE_BY_ID_QUERY = "select * FROM FileTable where user_id = ? and doc_id = ?";

    public static String DELETE_FILE_QUERY = "DELETE FROM FileTable where user_id = ? AND doc_id = ?";

}

