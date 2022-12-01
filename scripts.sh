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
#ss -ltn
#sudo ufw allow from any to any port 8080 proto tcp

sudo apt-cache search mysql-server
sudo apt info -a mysql-server-8.0
sudo apt-get install mysql-server-8.0 -y
sudo systemctl is-enabled mysql.service
sudo systemctl start mysql.service
sudo systemctl status mysql.service
#export pwd=abc
#echo $pwd
##export altercmd=\Alter user 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Root@123'\
##echo $altercmd
#sudo mysql -uroot -p$pwd --connect-expired-password -e "Alter user 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Root@123'"
#sudo systemctl restart mysql.service
#sudo systemctl status mysql.service
#sudo mysql -uroot -pRoot@123 -e "CREATE DATABASE IF NOT EXISTS Csye6225WebServiceDB"

sudo apt install maven -y

# shellcheck disable=SC2232
sudo cd
sudo pwd
sudo ls -lrt
sudo mkdir -p /opt/webapps
sudo chmod 755 /opt/webapps

sudo groupadd -r appmgr2

sudo useradd -d /opt/webapps -r -s /bin/false -g appmgr2 jvmapps2

echo -e '\n[Unit]\nDescription=Manage JAVA service\n\n[Service]\nWorkingDirectory=/opt/webapps\nExecStart=/bin/java -jar /opt/webapps/demo1-0.0.1-SNAPSHOT.jar\nType=simple\nUser=jvmapps2\nGroup=appmgr2\nRestart=on-failure\nRestartSec=10\n\n[Install]\nWantedBy=multi-user.target\n' | sudo tee /etc/systemd/system/myapp.service
sudo chown -R jvmapps2:appmgr2 /opt/webapps

sudo pwd
sudo ls -lrt

#sudo mvn clean install
# shellcheck disable=SC2164

sudo pwd
sudo mv /home/ubuntu/demo1-0.0.1-SNAPSHOT.jar /opt/webapps/.
sudo mv /home/ubuntu/cloudwatch-config.json /opt/webapps/.

# shellcheck disable=SC2164
cd /opt/webapps
sudo pwd
ls -lrt

#sudo wget https://csye6225-shruti.s3.us-west-2.amazonaws.com/demo1-0.0.1-SNAPSHOT.jar
sudo chmod 755 demo1-0.0.1-SNAPSHOT.jar
sudo systemctl daemon-reload
sudo systemctl start myapp.service
sudo systemctl enable myapp.service
sudo systemctl status myapp.service

#sudo wget https://aws-codedeploy-us-west-2.s3.us-west-2.amazonaws.com/latest/install
#chmod +x ./install
#sudo ./install auto
#sudo service codedeploy-agent status
#sudo service codedeploy-agent start
#sudo service codedeploy-agent status

sudo wget https://s3.us-west-2.amazonaws.com/amazoncloudwatch-agent-us-west-2/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i -E ./amazon-cloudwatch-agent.deb

#sudo wget https://csye6225-shruti.s3.us-west-2.amazonaws.com/cloudwatch-config.json
sudo chmod 755 cloudwatch-config.json
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/opt/webapps/cloudwatch-config.json