#!/bin/bash

sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install nginx -y
sudo apt-get clean
sudo apt update
sudo apt-get install openjdk-8-jdk -y
sudo groupadd --system tomcat
sudo useradd -d /usr/share/tomcat -r -s /bin/false -g tomcat tomcat
export VER=10.0.27
sudo wget https://dlcdn.apache.org/tomcat/tomcat-10/v10.0.27/bin/apache-tomcat-10.0.27.tar.gz
sudo tar xvf apache-tomcat-10.0.27.tar.gz -C /usr/share/
sudo ln -s /usr/share/apache-tomcat-10.0.27/ /usr/share/tomcat
sudo chown -R tomcat:tomcat /usr/share/tomcat
sudo chown -R tomcat:tomcat /usr/share/apache-tomcat-10.0.27/
echo "checking what is inside tomcat"
echo -e '\n[Unit]\nDescription=Tomcat Server\nAfter=syslog.target network.target\n\n[Service]\nType=forking\nUser=tomcat\nGroup=tomcat\n\nEnvironment='JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64'\nEnvironment='JAVA_OPTS=-Djava.awt.headless=true'\nEnvironment='CATALINA_HOME=/usr/share/tomcat'\nEnvironment='CATALINA_BASE=/usr/share/tomcat'\nEnvironment='CATALINA_PID=/usr/share/tomcat/temp/tomcat.pid'\nEnvironment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'\n\nExecStart=/usr/share/tomcat/bin/startup.sh\nExecStop=/usr/share/tomcat/bin/shutdown.sh\n\n[Install]\nWantedBy=multi-user.target\n' | sudo tee /etc/systemd/system/tomcat.service
sudo systemctl daemon-reload
sudo systemctl start tomcat.service
sudo systemctl enable tomcat.service
sudo systemctl status tomcat.service
ss -ltn
sudo ufw allow from any to any port 8080 proto tcp

sudo apt-cache search mysql-server
sudo apt info -a mysql-server-8.0
sudo apt-get install mysql-server-8.0 -y
sudo systemctl is-enabled mysql.service
sudo systemctl start mysql.service
sudo systemctl status mysql.service
export pwd=abc
echo $pwd
#export altercmd=\Alter user 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Root@123'\
#echo $altercmd
sudo mysql -uroot -p$pwd --connect-expired-password -e "Alter user 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Root@123'"
sudo systemctl restart mysql.service
sudo systemctl status mysql.service
sudo mysql -uroot -pRoot@123 -e "CREATE DATABASE IF NOT EXISTS Csye6225WebServiceDB"

sudo apt install maven -y

#sudo mkdir -p /home/ubuntu/logs
#sudo touch /home/ubuntu/logs/logs.out
#sudo chown ubuntu:ubuntu /home/ubuntu/logs/logs.out
sleep 30
sudo wget https://csye6225-shruti.s3.us-west-2.amazonaws.com/demo1-0.0.1-SNAPSHOT.jar
sudo nohup java -jar demo1-0.0.1-SNAPSHOT.jar &