
#!/bin/sh
#
#This script installs Java, Postgres and RAMADDA
#

useDefault=0
ramaddaVersion=1.7

ramaddaDownload="http://downloads.sourceforge.net/project/ramadda/ramadda${ramaddaVersion}/ramaddaserver.zip"
serviceName="ramadda"
serviceDir="/etc/rc.d/init.d"
basedir=/mnt/ramadda


dir=`dirname $0`
userdir=`dirname $dir`

keepAsking=1

perms==$(stat $userdir)
if [[ $perms =~ .*rwx---.*$ ]]; then
    echo "Changing permissions of home directory $userdir"
    chmod 755 $userdir
fi




askYesNo() {
    local msg="$1"
    local dflt="$2"
    if [ $useDefault == 1 ]; then
	response="$dflt";
	return;
    fi
    
    if [ $keepAsking == 0 ]; then
	response="$dflt";
	return;
    fi

    read -p "${msg}?  [y|A(all)|n]: " response
    if [ "$response" == "A" ]; then
	keepAsking = 0
	response="$dflt";
	return;
    fi

    case $response in y|Y) 
            response='y'
            ;;  
	"")
	    response="$dflt";
	    ;;

        *) response='n'
            ;;
    esac

    if [ "$response" == "" ]; then
	response="$dflt";
    fi
    if [ "$response" == "" ]; then
	response="n";
    fi

}

ask() {
    local msg="$1";
    local dflt="$2";
    if [ $useDefault == 1 ]; then
	response="$dflt";
        return;
    fi


    read -p "${msg} " response;

    if [ "$response" == "" ]; then
	response="$dflt";
    fi
}


ask   "Enter base directory: (default: $basedir):" $basedir
if [ "$response" != "" ]; then
    basedir=$response
fi



homedir=$basedir/repository
datadir=$basedir/data
pgdir=$basedir/pgsql93


mkdir -p $homedir
mkdir -p $datadir
mkdir -p $pgdir


ask  "What device should we mount on /dev? (default: xvdb, enter 'n' for none): "  "xvdb"
if [ "$response" != "" ]; then
    if [ "$response" != "n" ]; then
	mntfrom="$response"
	echo "Mounting $basedir on $mntfrom"
	sed -e 's/.*$homedir.*//g' /etc/fstab> dummy.fstab
	mv dummy.fstab /etc/fstab
	printf "\n/dev/${mntfrom}   /$homedir ext4 defaults  0 0\n" >> /etc/fstab
	mkfs -t ext4 /dev/${mntfrom}
	mount /dev/${mntfrom} $homedir
	fi
fi



echo "Fixing the localhost name problem"
sed -e 's/HOSTNAME=localhost.localdomain/HOSTNAME=ramadda.localdomain/g' /etc/sysconfig/network> dummy.network
mv dummy.network /etc/sysconfig/network
sed -e 's/127.0.0.1   localhost localhost.localdomain/127.0.0.1 ramadda.localdomain ramadda localhost localhost.localdomain/g' /etc/hosts> dummy.hosts
mv dummy.hosts /etc/hosts


askYesNo "Install java"  "y"
if [ "$response" == "y" ]; then
    sudo yum install java
fi


### Database 
askYesNo  "Install postgres"  "y"
if [ "$response" == "y" ]; then
	ln -f -s $pgdir /var/lib/pgsql93

	sudo yum install postgresql93-server
	sudo service postgresql93 initdb
	sudo chkconfig postgresql93 on
	sudo service postgresql93 start

	sed -e 's/ident/trust/g' /var/lib/pgsql93/data/pg_hba.conf> dummy.conf
	sudo mv dummy.conf /var/lib/pgsql93/data/pg_hba.conf
	sudo service postgresql93 reload

	postgresPassword="password$RANDOM"
	printf "create database repository;\ncreate user ramadda;\nalter user ramadda with password '${postgresPassword}';\ngrant all privileges on database repository to ramadda;\n" > $dir/postgres.sql
	chmod 644 $dir/postgres.sql
	sudo su -c "psql -f $dir/postgres.sql"  - postgres
	rm $dir/postgres.sql
        printf "ramadda.db=postgres\nramadda.db.postgres.user=ramadda\nramadda.db.postgres.password=${postgresPassword}"  > ${homedir}/db.properties
fi


askYesNo "Install RAMADDA from SourceForge"  "y"
if [ "$response" == "y" ]; then
	rm -f ${dir}/ramaddaserver.zip
	rm -r -f ${dir}/ramaddaserver
	wget -O ${dir}/ramaddaserver.zip ${ramaddaDownload}
	unzip -d ${dir} -o ${dir}/ramaddaserver.zip
	printf "\n\nexport RAMADDA_HOME=${homedir}\nexport RAMADDA_PORT=80\n" > ${dir}/ramaddaserver/ramaddaenv.sh
fi


askYesNo "Add RAMADDA as a service"  "y"
if [ "$response" == "y" ]; then
    printf "#!/bin/sh\n# chkconfig: - 80 30\n# description: RAMADDA repository\n\nsh $dir/ramaddaserver/ramaddainit.sh \"\$@\"\n" > ${serviceDir}/${serviceName}
    chmod 755 ${serviceDir}/${serviceName}
    chkconfig ${serviceName} on
    printf "     to run: sudo service ${serviceName} <options>\n"
fi


askYesNo "Generate keystore and enable SSL" "y"
if [ "$response" == "y" ]; then
    password="ssl_${RANDOM}_${RANDOM}"
    echo "OK, ignore the keystore output. We're using some default values"
    rm -f ${homedir}/keystore
    printf "${password}\n${password}\nname\nunit\norg\ncity\nstate\nusa\nyes\n\n" | keytool -genkey -keystore ${homedir}/keystore
    printf "#generated password\n\nramadda.ssl.password=${password}\nramadda.ssl.keypassword=${password}\nramadda.ssl.port=443\n" > ${homedir}/ssl.properties
    printf "\n\n"
fi



askYesNo "Start RAMADDA" "y"
if [ "$response" == "y" ]; then
    service ${serviceName} restart
    printf "Finish the configuration at https://<hostname>/repository or http://<hostname>/repository\n"
fi

exit














