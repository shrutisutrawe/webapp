package com.neu.assignment.controller.fileOperations;

import com.neu.assignment.model.FileDetails;

public class UploadFileResponse {
    private String id;
    private String user_id;
    private String file_name;
    private String upload_date;
    private String url;


    public UploadFileResponse(FileDetails fileDetails) {
        this.id = fileDetails.getDoc_id();
        this.user_id = fileDetails.getUser_id();
        this.file_name = fileDetails.getFile_name();
        this.url = fileDetails.getS3_bucket_path();
        this.upload_date = fileDetails.getDate_created();
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
