##Summary

This repository contains source for a webservice built using Java/Spring-boot based on REST architecture

##Tools and Technologies
| Infra                | Tools/Technologies                   |
|----------------------|--------------------------------------|
| Webapp               | Java, Maven, Spring Boot             |
| Github               | Github Actions
| Database             | MySQL

APIs
----------------------
(1) **Healthz API**
- Path: ``healthz``
- Parameters: None
- Expected response: HTTP 200 OK

(2) **Create User API**
- Path: ``/v1/account/``
- HTTP Method POST
- Parameters:
```
 {
     "username": "...",
     "first_name": "...",
     "last_name": "...",
     "password": "..."
  }
```
- Auth: None
- Expected response:
    - HTTP **201** Created indicating the user was created
      ```
      {
         "id": "...",
         "first_name": "...",
         "last_name": "...",
         "username": "...",
         "account_created": "...",
         "account_updated": "..."
      }
      ```
    - HTTP **400** Bad Request if create user request is invalid
    - HTTP **400** Bad Request if user already exists
      
(3) **Put User API**
- Path: ``/v1/account/{userID}``
- HTTP Method PUT
- Parameters:
```
 {
     "first_name": "...",
     "last_name": "...",
     "password": "...",
  }
```
- Auth: Basic auth (username/password)
- Expected response:
    - HTTP **204** No Content indicating user details were updated
    - HTTP **401** Bad credentials if invalid username/password provided
    - HTTP **400** Bad Request if update user request is invalid

(4) **Get User**
- Path: ``/v1/account/{userID}``
- HTTP Method GET
- Parameters: None
- Auth: Basic auth (username/password)
- Expected response:
    - HTTP **200** OK indicating the user exists and details fetched successfully
      ```
      {
         "id": "...",
         "first_name": "...",
         "last_name": "...",
         "username": "...",
         "account_created": "...",
         "account_updated": "..."
      }
      ```
    - HTTP **401** Bad credentials if invalid username/password provided
      
(5) **Upload Documents**
- Path: ``/v1/documents/``
- HTTP Method POST
- Parameters: None
- Auth: Basic auth (username/password)
- Expected response:
    - HTTP **200** OK indicating the user exists and details fetched successfully
      ```
      {
         "id": "...",
         "user_id": "...",
         "file_name": "...",
         "upload_date": "...",
         "url": "..."
      }
      ```

(6) **Get Documents**
- Path: ``/v1/documents/{doc_id}``
- HTTP Method GET
- Parameters: None
- Auth: Basic auth (username/password)
- Expected response:
    - HTTP **200** OK indicating the user exists and details fetched successfully
      ```
      {
         "id": "...",
         "user_id": "...",
         "file_name": "...",
         "upload_date": "...",
         "url": "..."
      }
      ``` 

(6) **Delete Documents**
- Path: ``/v1/document/{doc_id}``
- HTTP Method DELETE
- Parameters: None
- Auth: Basic auth (username/password)
- Expected response:
    - HTTP **200** OK indicating the user exists and details fetched successfully
      ```
      {} 
Database
----------------------
- **DB**: MySQL
- **Schema**:
```
Users Database
+-----------------+-------------+------+-----+---------+-------+
| Field           | Type        | Null | Key | Default | Extra |
+-----------------+-------------+------+-----+---------+-------+
| id              | varchar(100) | NO   |     | NULL    |       |
| user_name       | varchar(100) | NO   | PRI | NULL    |       |
| first_name      | varchar(100) | NO   |     | NULL    |       |
| last_name       | varchar(100) | NO   |     | NULL    |       |
| password        | varchar(100) | NO   |     | NULL    |       |
| account_created | varchar(100) | NO   |     | NULL    |       |
| account_updated | varchar(100) | NO   |     | NULL    |       |
+-----------------+-------------+------+-----+---------+-------+
```
- Password is stored using Bcrypt Hash + Salt

Web service configuration
----------------------
- Port Number: 8080
- HTTP protocol

Build and running webapp
----------------------
(1) Clone the repository using following command

```
mkdir ~/shruti-csye6225
cd ~/shruti-csye6225
git clone git@github.com:CSYE6225-Shruti/webapp.git
cd webapp
```


(2) Start the web app on local host.
The port number is 8080.
Run below command in the terminal
```
curl -v http://localhost:8080/healthz
```
(3) Testing the service APIs manually (using Postman): In postman, send 
- ``GET`` request to ``http://localhost:8080/v1/account/{user_id}}`` and verify the response
- ``POST`` request to ``http://localhost:8080/v1/account/`` and verify the response
- ``PUT`` request to ``http://localhost:8080/v1/account/{user_id}}`` and verify the response
- ``POST`` request to ``http://localhost:8080/v1/documents`` and verify the response
- ``GET`` request to ``http://localhost:8080/v1/documents/{doc_id}`` and verify the response
- ``GET`` request to ``http://localhost:8080/v1/documents/`` and verify the response
- ``DELETE`` request to ``http://localhost:8080/v1/documents/{doc_id}`` and verify the response

