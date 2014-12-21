
read -p "Do you want to enter your Amazon authentication (only needed one time)? [y|n]: " tmp
case $tmp in
    ""|"y")
        aws configure
;;
esac

securityGroup="ramadda"
imageId="ami-55a7ea65"
instanceType="t1.micro"
keyPair="ramadda"
keyPairFile=""
volumeSize="100"


read -p "Enter the image id [${imageId}]: " tmp
if [ "$tmp" != "" ]; then
    imageId=$tmp
fi

read -p "Enter the instance type id [${instanceType}]: " tmp
if [ "$tmp" != "" ]; then
    instanceType=$tmp
fi

read -p "Enter the key pair name [${keyPair}]: " tmp
if [ "$tmp" != "" ]; then
    keyPair=$tmp
fi

read -p "Do you want to create the keypair [y|n]: " tmp
case $tmp in
    ""|"y")
        aws ec2 create-key-pair --query 'KeyMaterial' --output text --key-name ${keyPair} >  ${keyPair}.pem
        chmod 400 ${keyPair}.pem
        echo "Created the key pair file ${keyPair}.pem"
        echo "Important: This is the only way to access your instance and must be kept private."
        ;;
esac


read -p "Enter the volume size (GB) [${volumeSize}]: " tmp
if [ "$tmp" != "" ]; then
    volumeSize=$tmp
fi


read -p "Create the security group ${securityGroup}? [y|n]: " tmp
case $tmp in
    ""|"y")
        aws ec2 delete-security-group --group-name ${securityGroup} 
        aws ec2 create-security-group --group-name ${securityGroup} --description "RAMADDA security group"
        aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 22 --cidr 0.0.0.0/0
        aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 80 --cidr 0.0.0.0/0
        aws ec2 authorize-security-group-ingress --group-name ${securityGroup}  --protocol tcp --port 443 --cidr 0.0.0.0/0
        ;;
    *)
        tmp="aws ec2 create-security-group --group-name ${securityGroup} --description \"RAMADDA security group\""
        printf "We would have called:\n$tmp\n\n"
        ;;
esac


ipAddress=""

read -p "Do you want to create the instance with image: ${imageId} type: ${instanceType}? [y|n]: " tmp
case $tmp in
    ""|"y")
    echo "Creating instance"
#Note - this device name should match the one in installer.sh
    aws ec2 run-instances --image-id ${imageId} --count 1 --instance-type ${instanceType} --key-name ${keyPair} --security-groups ${securityGroup}  --block-device-mappings "[{\"DeviceName\":\"/dev/xvdb\",\"Ebs\":{\"VolumeSize\":${volumeSize},\"DeleteOnTermination\":false}}]" > runinstance.json
;;
*)
        if [ ! -f runinstance.json ]; then
            tmp="aws ec2 run-instances --image-id ${imageId} --count 1 --instance-type ${instanceType} --key-name ${keyPair} --security-groups ${securityGroup}  --block-device-mappings \"[{\"DeviceName\":\"/dev/sdf\",\"Ebs\":{\"VolumeSize\":${volumeSize},\"DeleteOnTermination\":false}}]\" "
            printf "We would have called:\n$tmp\n\n"
            exit
        fi
        echo "OK, we will use the instance id from the last run"
;; esac

grep InstanceId runinstance.json  | sed   -E   's|.*: +\"([^\"]+)\".*|\1|g'  > instanceid.txt
instanceId=$( cat instanceid.txt )
if [ "$instanceId" == "" ]; then
    echo "Failed to create instance"
    exit
fi

echo "Instance id: $instanceId"


while [ 1  ]; do
    read -p "Hit return to check to see if the instance is ready"
    aws ec2 describe-instances --instance-id "$instanceId" >instanceinfo.json
    grep PublicIpAddress instanceinfo.json | sed   -E   's|.*\"([0-9\.]+)\".*|\1|g'  > ipAddress.txt
    ipAddress=$( cat ipAddress.txt )
    if [ "$ipAddress" != "" ]; then
        break
    fi
    echo "Doesn't seem ready yet"
done



if [ "$ipAddress" == "" ]; then
    echo "Failed to read IP address from instanceinfo.json"
    exit
fi

echo "IP address: $ipAddress"

read -p  "Set instance name to: " tmp
if [ "$tmp" != "" ]; then
    aws ec2 create-tags --resources ${instanceId} --tags Key=Name,Value=$tmp
fi



read -p "Install RAMADDA on the server? [y|n]: " tmp
case $tmp in
    ""|"y")
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

        echo "It will take a minute or so for the machine to come up. We will keep trying to do a 'ssh yum update' "
        while [ 1  ]; do
            echo "ssh: sudo yum update"
            ssh -i ${pemFile} -t  ec2-user@${ipAddress} "sudo yum update"
            read -p "Did the yum update go OK? [y|n]: " tmp
            if [ "$tmp" == "y" ]; then
                break;
           fi
        done
        echo "Downloading the RAMADDA installer"
        echo "ssh: wget http://downloads.sourceforge.net/project/ramadda/ramadda1.7/ramaddainstaller.zip"
        ssh -i ${pemFile} -t  ec2-user@${ipAddress} "wget http://downloads.sourceforge.net/project/ramadda/ramadda1.7/ramaddainstaller.zip"
        echo "sudo sh /home/ec2-user/ramaddainstaller/installer.sh"
        ssh -i ${pemFile} -t  ec2-user@${ipAddress} "unzip -o ramaddainstaller.zip"
        ssh -i ${pemFile} -t  ec2-user@${ipAddress} "sudo sh /home/ec2-user/ramaddainstaller/installer.sh; sleep 5;"
        ;;
esac







