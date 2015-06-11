#!/bin/sh

#
# This script updates your RAMADDA installation
# Get the latest version of the ramaddaserver.zip from 
# https://sourceforge.net/projects/ramadda/files/
# and copy it into the ramaddainstall directory and then run this script
#

serviceName="ramadda"
installerDir=`dirname $0`
parentDir=`dirname $installerDir`
ramaddaDir=${parentDir}/${serviceName}
serverDir=$ramaddaDir/ramaddaserver

echo "stopping ${serviceName}";
service ${serviceName} stop;
cp ${serverDir}/ramaddaenv.sh  ${installerDir}
rm -r -f ${serverDir};
unzip -d ${ramaddaDir} -o ${installerDir}/ramaddaserver.zip >/dev/null
mv ${installerDir}/ramaddaenv.sh ${serverDir}
echo "starting ${serviceName}";
service ${serviceName} start;

printf "RAMADDA has been updated and restarted. Check the log in:\n${ramaddaDir}/ramadda.log\n"
