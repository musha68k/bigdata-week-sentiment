provider "aws" {
    access_key = "${var.access_key}" 
    secret_key = "${var.secret_key}"
    region = "us-east-1"
}

resource "aws_key_pair" "key" {
  key_name = "key" 
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDWrX3Pf/V0V2gWel4QeOrX2/o5QWSyutcB12HZ3Us7E6+mQYHo1W85KpPJunpJRkD1v1Me9YBKZjoIxFrAWLrmVJftD9vspxPibMDIx9/uu0FOhr4ttcHW4VHoj8MWmp4BGwCuRlr/bXVYUhtaFOB07L0WMLbaqC2p1DyC0buB/mFRwPvXZdkmK8tnsHhZmcGUffjuID+X/5DBpFNvI72Bute7c9EahtKpomFjXXu93EwqhHFK9pGCsNtYSmWn4Oiyj6Rs5VIS0PH0ef73tFb1yl9Cnz1jO17OxffC1zYNZZbDbyF78PS5afeSz0v+BOipGbkLM5JbeDX/jgT0eEXR alexis@icecream.local"
}

resource "aws_eip" "ip" {
    instance = "${aws_instance.spark.id}"
}

resource "aws_instance" "spark" {
    ami = "ami-1ecae776" # AWS Linux AMI
    instance_type = "r3.8xlarge" # 32 CPUs, 244G RAM, 2x320G SSD
    key_name = "key"

    provisioner "remote-exec" {
	    inline = [
	        "sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo",
	        "sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo",
	        "sudo yum install -y apache-maven git",
	        "git clone https://github.com/radialpoint/bigdata-week-sentiment",
	        "cd bigdata-week-sentiment;mvn scala:run"
	        ]
	    connection {
        	user = "ec2-user"
        	key_file = "key.pem"
    	}
    }
}

# sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
# sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
# sudo yum install -y apache-maven

