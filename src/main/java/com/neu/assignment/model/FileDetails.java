package com.neu.assignment.model;

public class FileDetails {
    private String doc_id;
    private String user_id;
    private String file_name;
    private String s3_bucket_path;
    private String date_created;

    public FileDetails() {}

    public FileDetails(String doc_id, String user_id, String file_name, String image_s3_url, String date_created) {
        this.doc_id = doc_id;
        this.user_id = user_id;
        this.file_name = file_name;
        this.s3_bucket_path = image_s3_url;
        this.date_created = date_created;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public void setS3_bucket_path(String s3_bucket_path) {
        this.s3_bucket_path = s3_bucket_path;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getS3_bucket_path() {
        return s3_bucket_path;
    }

    public String getDate_created() {
        return date_created;
    }
}
