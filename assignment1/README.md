##Summary

This repository contains source for a webservice built using Java/Spring-boot based on REST architecture

##Tools and Technologies
| Infra                | Tools/Technologies                   |
|----------------------|--------------------------------------|
| Webapp               | Java, Maven, Spring Boot             |
| Github               | Github Actions

APIs
----------------------
(1) **Healthz API**
- Path: ``healthz``
- Parameters: None
- Expected response: HTTP 200 OK


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