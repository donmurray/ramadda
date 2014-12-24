#!/bin/sh

ramaddaVersion=1.7


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

read -p "Enter the volume size (GB) [${volumeSize}]: " tmp
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



readit "Security group to create [${securityGroup}|n]? " tmp "RAMADDA needs ports 22 (ssh), 80 (http) and 443 (https) defined in its security group"

case $tmp in
    "n")
        ;;
    *)
    if [ "$tmp" != "" ]; then
        securityGroup="$tmp"
    fi
    aws ec2 create-security-group --group-name ${securityGroup} --description "RAMADDA security group"
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 22 --cidr 0.0.0.0/0
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 80 --cidr 0.0.0.0/0
    aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 443 --cidr 0.0.0.0/0
    ;;
esac


ipAddress=""

read -p "Do you want to create the instance with image: ${imageId} type: ${instanceType} security group: ${securityGroup} ? [y|n]: " tmp
case $tmp in
    ""|"y")
        echo "Creating instance... ";
#Note - this device name should match the one in installer.sh
        aws ec2 run-instances  --output text --image-id ${imageId} --count 1 --instance-type ${instanceType} --key-name ${keyPair} --security-groups ${securityGroup}  --block-device-mappings "[{\"DeviceName\":\"/dev/xvdb\",\"Ebs\":{\"VolumeSize\":${volumeSize},\"DeleteOnTermination\":false}}]" > runinstance.txt
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
    read -p "Hit return to check to see if the instance is ready: "
    aws ec2 describe-instances --instance-id "$instanceId" --output text |  grep INSTANCES | awk 'BEGIN { FS = "\t" } ; { print $16 }'  > ipaddress.txt
    ipAddress=$( cat ipaddress.txt )
    if [ "$ipAddress" == "None" ]; then
        ipAddress="";
    fi
    if [ "$ipAddress" != "" ]; then
        break
    fi
    echo "Doesn't seem ready yet. "
done



if [ "$ipAddress" == "" ]; then
    echo "Failed to read IP address from ipaddress.txt"
    exit
fi

printf "Your instance will be ready to access in minute or so:\nssh -i ${keyPair}.pem ec2-user@${ipAddress}\n\n"


read -p  "Set instance name to: " tmp
if [ "$tmp" != "" ]; then
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

echo "We'll keep trying to do a 'ssh yum update' "
while [ 1  ]; do
    echo "ssh: sudo yum update"
    ssh -i ${pemFile} -t  ec2-user@${ipAddress} "sudo yum update"
    read -p "Did the yum update go OK? If not have patience [y|n]: " tmp
    if [ "$tmp" == "y" ]; then
        break;
    fi
done
        
readit  "Download and install RAMADDA? [y|n]: " tmp  "OK, now we will ssh to the new instance, download and run the RAMADDA installer"
case $tmp in
    ""|"y")
        ssh -i ${pemFile} -t  ec2-user@${ipAddress} "wget ${downloadUrl}"
        ssh -i ${pemFile} -t  ec2-user@${ipAddress} "unzip -o ramaddainstaller.zip"
        ssh -i ${pemFile} -t  ec2-user@${ipAddress} "sudo sh /home/ec2-user/ramaddainstaller/installer.sh; sleep 5;"
        ;;
esac



printf "\nFinish configuration of the RAMADDA repository at https://$ipAddress/repository\n"



