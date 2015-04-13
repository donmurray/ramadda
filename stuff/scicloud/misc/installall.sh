#!/bin/sh

dir=`dirname $0`

bindir=/home/ec2-user/bin
mkdir -p ${bindir}

sudo yum-config-manager --enable epel

yum install ftp -y
yum install wget -y
yum install gcc -y
yum install gcc-c++ -y
yum install cmake -y
yum install m4 -y


yum install ImageMagick -y