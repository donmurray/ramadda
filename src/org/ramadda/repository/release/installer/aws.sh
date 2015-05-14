#!/bin/sh

ramaddaVersion=2.1b


downloadUrl="http://downloads.sourceforge.net/project/ramadda/ramadda${ramaddaVersion}/ramaddainstaller.zip"
securityGroup="ramadda"
imageId="ami-55a7ea65"
instanceType="t1.micro"
keyPair="ramadda"
keyPairFile=""
volumeSize="100"


comment() {
    local msg="$1"
    if [ "$msg" != "" ]; then
        printf "\n# $msg\n"
    fi
}
readit() {
    local msg="$1"
    local var="$2"
    local extra="$3"
    comment "$extra";
    read -p "$msg" $var
}


read -p "Do you want to enter your Amazon authentication (only needed one time)? [y|n]: " tmp
case $tmp in
    ""|"y")
        aws configure
;;
esac





readit  "Enter the image id [${imageId}]: " tmp "Enter machine info and disk size. Note: the installer only works on Amazon Linux AMIs"

if [ "$tmp" != "" ]; then
    imageId=$tmp
fi

read -p "Enter the instance type [${instanceType}]: " tmp
if [ "$tmp" != "" ]; then
    instanceType=$tmp
fi

read -p "Enter the size (GB) of the storage volume. 0 for none. [${volumeSize}]: " tmp
if [ "$tmp" != "" ]; then
    volumeSize=$tmp
fi



readit  "Enter the key pair name [${keyPair}]: " tmp "The <key pair>.pem file is the way you access your instance."
if [ "$tmp" != "" ]; then
    keyPair=$tmp
fi

if [ -f ${keyPair}.pem ]; then
    echo "Key pair file ${keyPair}.pem already exists"
else 
    read -p "Do you want to create the keypair file ${keyPair}.pem [y|n]: " tmp
    case $tmp in
        ""|"y")
            aws ec2 create-key-pair --query 'KeyMaterial' --output text --key-name ${keyPair} >  ${keyPair}.pem
            chmod 400 ${keyPair}.pem
            echo "Created the key pair file ${keyPair}.pem"
            echo "Important: This is the only way to access your instance and must be kept private."
            ;;
    esac
fi



readit "Security group [${securityGroup}|n]? " tmp "RAMADDA needs ports 22 (ssh), 80 (http) and 443 (https) defined in its security group\nEnter 'n' if you have already created a ${securityGroup} security group"

case $tmp in
    "n")
        ;;
    *)
    if [ "$tmp" != "" ]; then
        securityGroup="$tmp"
    fi
    aws ec2 create-security-group --group-name ${securityGroup} --description "RAMADDA security group" > /dev/null 2> /dev/null
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 22 --cidr 0.0.0.0/0 > /dev/null 2> /dev/null
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 80 --cidr 0.0.0.0/0 > /dev/null 2> /dev/null
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 443 --cidr 0.0.0.0/0 > /dev/null 2> /dev/null
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 21 --cidr 0.0.0.0/0 > /dev/null 2> /dev/null
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 44001-44099 --cidr 0.0.0.0/0 > /dev/null 2> /dev/null
    ;;
esac

read -p  "Set instance name to: " instanceName

ipAddress=""

echo "Do you want to create the instance with:\n\timage: ${imageId}\n\ttype: ${instanceType}\n\tsecurity group: ${securityGroup}\n\tInstance name: ${instanceName}"
read -p "Enter [y|n]: " tmp
case $tmp in
    ""|"y")
        echo "Creating instance... ";
        if [ ${volumeSize} == 0 ]; then
            device="[]"
        else
            device="[{\"DeviceName\":\"/dev/xvdb\",\"Ebs\":{\"VolumeSize\":${volumeSize},\"DeleteOnTermination\":false}}]"
        fi
#Note - this device name should match the one in installer.sh
        aws ec2 run-instances  --output text --image-id ${imageId} --count 1 --instance-type ${instanceType} --key-name ${keyPair} --security-groups ${securityGroup}  --block-device-mappings $device > runinstance.txt
        ;;
    *)
        if [ ! -f runinstance.txt ]; then
            tmp="aws ec2 run-instances --image-id ${imageId} --count 1 --instance-type ${instanceType} --key-name ${keyPair} --security-groups ${securityGroup}  --block-device-mappings \"[{\"DeviceName\":\"/dev/sdf\",\"Ebs\":{\"VolumeSize\":${volumeSize},\"DeleteOnTermination\":false}}]\" "
            printf "We would have called:\n$tmp\n\n"
            exit
        fi
        echo "OK, we will use the instance id from the last run"
;; esac


grep INSTANCES runinstance.txt  | awk 'BEGIN { FS = "\t" } ; { print $8 }'  > instanceid.txt
instanceId=$( cat instanceid.txt )
if [ "$instanceId" == "" ]; then
    echo "Failed to read instanceid from runinstance.txt"
    exit
fi

echo "Instance id: $instanceId"
while [ 1  ]; do
    echo "Waiting for instance to come up..."
    aws ec2 describe-instances --output text --instance-id "$instanceId"  |  grep INSTANCES | awk 'BEGIN { FS = "\t" } ; { print $16 }'  > ipaddress.txt
    ipAddress=$( cat ipaddress.txt )
    if [ "$ipAddress" == "None" ]; then
        ipAddress="";
    fi
    if [ "$ipAddress" == "ebs" ]; then
        ipAddress="";
    fi
    if [ "$ipAddress" != "" ]; then
        break
    fi
    echo "Not ready yet..."
    sleep 2;
done


if [ "$ipAddress" == "" ]; then
    echo "Failed to read IP address from ipaddress.txt"
    exit
fi

printf "Your instance will be ready to access in a minute or two. You will be able to access it at:\n   ssh -i ${keyPair}.pem ec2-user@${ipAddress}\n\n"


if [ "$instanceName" != "" ]; then
    aws ec2 create-tags --resources ${instanceId} --tags Key=Name,Value=$tmp
fi



pemFile="${keyPair}.pem"
while [ ! -f $pemFile ]; do
    pemFile="~/${keyPair}.pem"
    if [ ! -f $pemFile ]; then
        read -p "Enter path to ${keyPair}.pem file: " tmp
        if [ "$tmp" == "" ]; then
            exit
        fi
        pemFile=$tmp
        if [ ! -f $pemFile ]; then
            echo "File doesn't exist: $pemFile"
            exit
        fi
    fi
done

echo "We'll keep trying to ssh to the instance and update the OS "
echo "This may take some time while the instance is coming up so have patience "
echo "Once you are connected you will see a 'The authenticity of host ...' message. Enter 'yes' and then the yum update will run"
echo "trying: ssh -i ${pemFile} -t  ec2-user@${ipAddress} \"sudo yum update -y\" "
keepGoing=1;
while [ 1  ]; do
    result=`ssh   -i ${pemFile} -t  ec2-user@${ipAddress} "sudo yum update -y" 2> /dev/null`
    case ${result} in
        "") 
            echo "Instance isn't ready yet. We'll sleep a bit and then try again";
            sleep 10;
            ;;
        *) 
            echo "${result}"
            echo "Instance is ready and updated"
            keepGoing=0;
            ;;
    esac
    echo "Keep going: $keepGoing"
    if [  $keepGoing  == 0 ]; then
        echo "DONE"
        break;
    fi
done

        
readit  "Download and install RAMADDA? [y|n]: " tmp  "OK, now we will ssh to the new instance, download and run the RAMADDA installer"
case $tmp in
    ""|"y")
        ssh  -i ${pemFile} -t  ec2-user@${ipAddress} "wget ${downloadUrl}"
        ssh  -i ${pemFile} -t  ec2-user@${ipAddress} "unzip -o ramaddainstaller.zip"
        ssh  -i ${pemFile} -t  ec2-user@${ipAddress} "sudo sh /home/ec2-user/ramaddainstaller/installer.sh; sleep 5;"
        ;;
esac



printf "\nFinish configuration of the RAMADDA repository at https://$ipAddress/repository\n"



