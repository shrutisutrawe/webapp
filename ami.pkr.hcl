variable "aws_region" {
  type    = string
  default = "us-west-2"
}

variable "source_ami" {
  type    = string
  default = "ami-017fecd1353bcc96e" # Ubuntu 22.04 LTS
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = "subnet-0efede05bd423a4a7"
}

variable "profile" {
  type    = string
  default = "devuser"
}

variable "aws_demouser" {
  type    = string
  default = "097171053993"
}

variable "aws_devuser" {
  type    = string
  default = "958817607905"
}

#variable "aws_access_key" {
#  type    = string
#  default = "AKIA56PP3UDQ22ABRY7X"
#}
#
#variable "aws_secret_key" {
#  type    = string
#  default = "fL5SZ/MszXYPAperBom7xqeeAe/11pNVKEIE6RHo"
#}

# https://www.packer.io/plugins/builders/amazon/ebs
source "amazon-ebs" "my-ami" {
  region          = "${var.aws_region}"
  profile         = "${var.profile}"
  ami_name        = "csye6225_${formatdate("YYYY_MM_DD_hh_mm_ss", timestamp())}"
  ami_description = "AMI for CSYE 6225"
  ami_regions = [
    "us-west-2",
  ]
  ami_users = [
    "${var.aws_devuser}",
    "${var.aws_demouser}",
  ]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

#  ssh_password = "Shriganesh@1992"
#  associate_public_ip_address = true
#  access_key = "${var.aws_access_key}"
#  secret_key = "${var.aws_secret_key}"
#  ssh_keypair_name = "aws-us-west-2"
#  ssh_private_key_file = "aws-us-west-2"

  instance_type = "t2.micro"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = 50
    volume_type           = "gp2"
  }
}

build {
  sources = ["source.amazon-ebs.my-ami"]
  provisioner "shell" {
    environment_vars = [
      "DEBIAN_FRONTEND=noninteractive",
      "CHECKPOINT_DISABLE=1"
    ]
    inline = [
      "echo ************",
      "pwd",
      "echo ************",
#      "cd /home/runner",
      "cd ../",
      "pwd",
      "echo 00000000000000000",
      "ls",
      "echo 00000000000000000",
      "echo \"going to\"",
      "pwd",
      "echo -----------",
#      "cd work",
      "echo 00000000000000000",
      "pwd"
    ]
#    scripts = [
#      "scripts.sh"
#    ]
  }
#  provisioner "file" {
#    sources  = ["/home/runner/work/webapp/webapp/target/demo1-0.0.1-SNAPSHOT.war"]
#    destination = "~/demo1-0.0.1-SNAPSHOT.war"
#  }
}