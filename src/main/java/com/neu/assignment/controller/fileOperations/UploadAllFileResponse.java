package com.neu.assignment.controller.fileOperations;

import com.neu.assignment.model.FileDetails;

import java.util.List;

public class UploadAllFileResponse {
    private List<FileDetails> fileDetailsList;

    public UploadAllFileResponse(List<FileDetails> fileDetailsList) {
        this.fileDetailsList = fileDetailsList;
    }

    public List<FileDetails> getFileDetailsList() {
        return fileDetailsList;
    }

    public void setFileDetailsList(List<FileDetails> fileDetailsList) {
        this.fileDetailsList = fileDetailsList;
    }

}
