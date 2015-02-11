#Usage:
# sfrelease.sh <sf user id>
#

sfuser=${1}

if test -z ${sfuser} ; then 
     sfuser=jmcwhirter
fi


dest=/home/frs/project/r/ra/ramadda/ramadda${RAMADDA_VERSION}

#scp all of the plugins over to SF
echo "copying plugins"
scp ~/.ramadda/plugins/nlasplugin.jar dist/plugins/miscplugins/* ${sfuser},ramadda@frs.sourceforge.net:${dest}/plugins

#scp the top level build products to SF
echo "copying core"
scp dist/ramaddainstaller.zip  dist/ramaddaserver.zip   dist/plugins/coreplugins.jar dist/plugins/bioplugins.jar dist/plugins/geoplugins.jar dist/ramaddaclient.zip dist/pointtools.zip  dist/repository.war  ${sfuser},ramadda@frs.sourceforge.net:${dest}




