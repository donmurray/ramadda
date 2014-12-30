#!/bin/sh

#
#This script installs some base packages, Postgres and then RAMADDA
#


OS_REDHAT="redhat"
OS_AMAZON="amazon_linux"
os=$OS_AMAZON

dir=`dirname $0`
userdir=`dirname $dir`
promptUser=1
yumArg=""
ramaddaVersion=1.7

ramaddaDownload="http://downloads.sourceforge.net/project/ramadda/ramadda${ramaddaVersion}/ramaddaserver.zip"
serviceName="ramadda"
serviceDir="/etc/rc.d/init.d"
basedir=""


usage() {
    echo "installer.sh -os redhat -y (assume yes installer) -help "
    exit
}



while [ $# != 0 ]
do
    case $1 in 
	-os)
	    shift
	    os=$1;
	    ;;
	-help)
	    usage
	    ;;
	-y)
	    promptUser=0
	    yumArg=--assumeyes
	    ;;
	*)
	    echo "Unknown argument $1"
	    usage
	    ;;
    esac
    shift
done


echo "target os: $os"

if [ "$os" == "${OS_REDHAT}" ]; then
    pgsql=pgsql
    pgService=postgresql-server
    pgInstall=http://yum.postgresql.org/9.3/redhat/rhel-6-x86_64/pgdg-redhat93-9.3-1.noarch.rpm
else
    pgsql=pgsql93
    pgService=postgresql93
    pgInstall=postgresql93-server
fi

postgresDir=/var/lib/${pgsql}
postgresDataDir=${postgresDir}/data


yumInstall() {
    local target="$1"
    if [ "$yumArg" == "" ]; then
	yum install ${target}
    else 
	yum install ${yumArg} ${target}
    fi
}

askYesNo() {
    local msg="$1"
    local dflt="$2"

    if [ $promptUser == 0 ]; then
	response="$dflt";
	return;
    fi

    read -p "${msg}?  [y|A(all)|n]: " response
    if [ "$response" == "A" ]; then
	promptUser=0;
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
    local extra="$3"
    if [ $promptUser == 0 ]; then
	response="$dflt";
        return;
    fi

    if [ "$extra" != "" ]; then
        printf "\n# $extra\n"
    fi

    read -p "${msg} " response;

    if [ "$response" == "" ]; then
	response="$dflt";
    fi
}

mntDir=""

declare -a dirLocations=("/dev/xvdb" )
for i in "${dirLocations[@]}"
do
   if [ -b "$i" ]; then
       askYesNo  "Do you want to mount the volume: $i [y|n]:"  "y"
       if [ "$response" == "y" ]; then
           mntDir="$i"
           break;
       fi
   fi
done


while [ "$mntDir" == "" ]; do
    ask  "Enter the volume to mount, e.g., /dev/xvdb  [<volume>|n] "  ""
    if [ "$response" == "" ] ||  [ "$response" == "n"  ]; then
        break;
    fi
    if [ -f $response ]; then
        mntDir="$response"
        break;
    fi
    echo "Directory does not exist: $response"
done

if [ "$mntDir" != "" ]; then
    basedir=/mnt/ramadda
    echo "Mounting $basedir on $mntDir"
    sed -e 's/.*$basedir.*//g' /etc/fstab> dummy.fstab
    mv dummy.fstab /etc/fstab
    printf "\n#added by ramadda installer.sh\n${mntDir}   /$basedir ext4 defaults  0 0\n" >> /etc/fstab
    mkfs -t ext4 $mntDir
    mount $mntDir $basedir
fi




dfltDir="";
if [ -d "${userdir}" ]; then
    dfltDir="${userdir}/ramadda";
fi


if [ -d "/mnt/ramadda" ]; then
    dfltDir="/mnt/ramadda";
fi

while [ "$basedir" == "" ]; do
    ask   "Enter base directory: [$dfltDir]:" $dfltDir  "The base directory holds the repository, pgsql, and data sub-directories"
    if [ "$response" == "" ]; then
        break;
    fi
    basedir=$response;
    break
done



homedir=$basedir/repository
datadir=$basedir/data



mkdir -p $homedir
mkdir -p $datadir





echo "Fixing the localhost name problem"
sed -e 's/HOSTNAME=localhost.localdomain/HOSTNAME=ramadda.localdomain/g' /etc/sysconfig/network> dummy.network
mv dummy.network /etc/sysconfig/network
sed -e 's/127.0.0.1   localhost localhost.localdomain/127.0.0.1 ramadda.localdomain ramadda localhost localhost.localdomain/g' /etc/hosts> dummy.hosts
mv dummy.hosts /etc/hosts


echo "Installing base packages - wget, unzip & java"
yum install -y wget > /dev/null
yum install -y unzip > /dev/null
yum install -y java > /dev/null



### Database 
askYesNo  "Install postgres"  "y"
if [ "$response" == "y" ]; then

    yum install -y  ${pgInstall}
    pgdir="${basedir}/${pgsql}"

    if [ "$os" == "${OS_REDHAT}" ]; then
	 postgresql-setup initdb
    else
	 service ${pgService} initdb
    fi

    if  [ -d ${postgresDir} ] ; then
	if  [ ! -h ${postgresDir} ]; then
	    echo "Moving ${postgresDir} to $pgdir"
	    mv  ${postgresDir} $pgdir
	    ln  -s -f  $pgdir ${postgresDir}
	    chown -R postgres ${postgresDir}
	    chown -R postgres ${pgdir}
	fi
    else
	echo "Warning: ${postgresDir} does not exist"	
    fi


    if [ "$os" == "${OS_REDHAT}" ]; then
	 systemctl enable postgresql
	 systemctl start postgresql.service
    else
	 chkconfig ${pgService} on
	 service ${pgService} start
    fi



    if [ ! -f ${postgresDataDir}/pg_hba.conf.bak ]; then
        cp ${postgresDataDir}/pg_hba.conf ${postgresDataDir}/pg_hba.conf.bak
    fi

    postgresPassword="password$RANDOM-$RANDOM"
    postgresUser="ramadda"
    postgresAuth="
#
#written out by the RAMADDA installer
#
host repository ${postgresUser} 127.0.0.1/32  password
local   all             all                                     peer
host    all             all             127.0.0.1/32            ident
host    all             all             ::1/128                 ident
"


        printf "${postgresAuth}" > ${postgresDataDir}/pg_hba.conf

	 service ${pgService} reload

	printf "create database repository;\ncreate user ramadda;\nalter user ramadda with password '${postgresPassword}';\ngrant all privileges on database repository to ramadda;\n" > /tmp/postgres.sql
	chmod 644 /tmp/postgres.sql
	 su -c "psql -f /tmp/postgres.sql"  - postgres
	rm -f $dir/postgres.sql
        printf "ramadda.db=postgres\nramadda.db.postgres.user=ramadda\nramadda.db.postgres.password=${postgresPassword}"  > ${homedir}/db.properties
fi


ramaddaConfig="#\n#Generated by installer.sh\n#\nexport RAMADDA_HOME=${homedir}\nexport RAMADDA_PORT=80\n";

script="#!/bin/sh
echo 'stopping ${serviceName}';
service ${serviceName} stop;
rm -r -f ${dir}/ramaddaserver;
unzip -d ${dir} -o ${dir}/ramaddaserver.zip
printf \"$ramaddaConfig\" > ${dir}/ramaddaserver/ramaddaenv.sh
echo 'starting ${serviceName}';
service ${serviceName} start;
"
printf "$script"  > ${dir}/installRamadda.sh


askYesNo "Download and install RAMADDA from SourceForge"  "y"
if [ "$response" == "y" ]; then
	rm -f ${dir}/ramaddaserver.zip
	wget -O ${dir}/ramaddaserver.zip ${ramaddaDownload}
	rm -r -f ${dir}/ramaddaserver
	unzip -d ${dir} -o ${dir}/ramaddaserver.zip
	printf "$ramaddaConfig" > ${dir}/ramaddaserver/ramaddaenv.sh
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
    printf "Finish the configuration at https://<ip address>/repository\n"
fi

exit














