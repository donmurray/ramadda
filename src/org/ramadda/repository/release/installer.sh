

ask() {
    local msg="$1"
    read -p "${msg}?  [y|n]: " response
    case $response in y|Y) 
            response='y'
            ;;  
        *) response='n'
            ;;
    esac
}




dir=`dirname $0`
answ="y"

basedir=/mnt/ramadda






read -p   "Enter base directory: (default: $basedir): " response
case $response in "") 
;;
*)
       basedir=$response
;;  esac






homedir=$basedir/repository
datadir=$basedir/data
pgdir=$basedir/pgsql93


mkdir -p $homedir
mkdir -p $datadir
mkdir -p $pgdir







read -p  "What device should we mount on /dev? (e.g. xvdb) " mntfrom
case $mntfrom in "") 
;;
*)
echo "Mounting $basedir on $mntfrom"
sed -e 's/.*$homedir.*//g' /etc/fstab> dummy.fstab
mv dummy.fstab /etc/fstab
printf "\n/dev/${mntfrom}   /$homedir ext4 defaults  0 0\n" >> /etc/fstab
echo "mkfs"
mkfs -t ext4 /dev/${mntfrom}
echo "mount"
mount /dev/${mntfrom} $homedir
;;  esac



echo "Fixing the localhost name problem"
sed -e 's/HOSTNAME=localhost.localdomain/HOSTNAME=ramadda.localdomain/g' /etc/sysconfig/network> dummy.network
mv dummy.network /etc/sysconfig/network

sed -e 's/127.0.0.1   localhost localhost.localdomain/127.0.0.1 ramadda.localdomain ramadda localhost localhost.localdomain/g' /etc/hosts> dummy.hosts

mv dummy.hosts /etc/hosts




ask "Install java" 
case $$response in y|Y) sudo yum install java;;  esac


### Database 
ask  "Install postgres" 
case $response in y|Y)
	ln -s $pgdir /var/lib/pgsql93

	sudo yum install postgresql93-server
	sudo service postgresql93 initdb
	sudo chkconfig postgresql93 on
	sudo service postgresql93 start

	sed -e 's/ident/trust/g' /var/lib/pgsql93/data/pg_hba.conf> dummy.conf
	sudo mv dummy.conf /var/lib/pgsql93/data/pg_hba.conf
	sudo service postgresql93 reload

	postgresPassword="password$RANDOM"
	sed -e 's/%password%/${postgresPassword}/g' $dir/postgres.init.sql> $dir/postgres.sql
	sudo su -c "psql -f $dir/postgres.sql"  - postgres
	rm $dir/postgres.sql
        printf "ramadda.db=postgres\nramadda.db.postgres.user=ramadda\nramadda.db.postgres.password=${postgresPassword}"  > ${homedir}/db.properties
;; esac


ask "Install RAMADDA from SourceForge" 
case $$response in y|Y) 
	rm -f ${dir}/ramaddaserver.zip
	rm -r -f ${dir}/ramaddaserver
	wget -O ${dir}/ramaddaserver.zip http://downloads.sourceforge.net/project/ramadda/ramadda1.7/ramaddaserver.zip
	unzip -o ${dir}/ramaddaserver.zip
	;;  esac

sed -e 's/.*ramaddainit.sh.*//g' /etc/rc.local> dummy.rc.local
printf "\nsh $dir/ramaddaserver/ramaddainit.sh start\n\n" >> dummy.rc.local
mv dummy.rc.local /etc/rc.local
printf "\n\nexport RAMADDA_HOME=${homedir}\nexport RAMADDA_PORT=80\n" > ramaddaserver/ramaddaenv.sh

ask "Start RAMADDA"
case $response in y|Y) 
	sh ramaddaserver/ramaddainit.sh restart
	;;  esac

exit














