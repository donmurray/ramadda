#
#This gets run in a ramadda directory that contains the stand-alone
#release directory and is a peer to a source directory that contains
#the ramadda-code dir from SF SVN
#
#ramadda
#      /installfrombuild.sh
#      /@RELEASE@
#source/ramadda-code
#
#Copy this file into the ramadda directory as it deletes and copies over
#the ramadda release from the build
#
#Before you run this do a full  Ant build in source/ramadda-code
#

RAMADDA_HOME=/ramadda

VERSION=@RELEASE@
DIST=../source/ramadda-code/dist


#Stop the server
sh ${VERSION}/ramaddainit.sh stop

#Copy the misc plugins  - the ones that aren't in core/geo/bio
cp ${DIST}/miscplugins/* ${RAMADDA_HOME}/plugins

rm -r -f $VERSION
cp -r ${DIST}/$VERSION .
chmod 755 $VERSION/*.sh
sh ${VERSION}/ramaddainit.sh start


