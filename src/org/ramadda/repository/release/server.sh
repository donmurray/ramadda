

dir=`dirname $0`
answ="y"

homedir=/mnt/ramadda/repository

sed -e 's/HOSTNAME=localhost.localdomain/HOSTNAME=ramadda.localdomain/g' /etc/sysconfig/network> dummy.network
mv dummy.network /etc/sysconfig/network

sed -e 's/127.0.0.1   localhost localhost.localdomain/127.0.0.1 ramadda.localdomain ramadda localhost localhost.localdomain/g' /etc/hosts> dummy.hosts

mv dummy.hosts /etc/hosts






read -p "Enter RAMADDA home directory: (default: $homedir): " tmp
case $tmp in "") 
;;
*)
	homedir=$tmp
;;  esac


mkdir -p $homedir


mntdir=`dirname $homedir`
read -p "Mount $mntdir on /dev/?: (e.g.  xvdb) " mntfrom
case $mntfrom in "") 
;;
*)
sed -e 's/.*$homedir.*//g' /etc/fstab> dummy.fstab
mv dummy.fstab /etc/fstab
printf "\n/dev/${mntfrom}   /$homedir ext4 defaults  0 0\n" >> /etc/fstab
echo "mkfs"
mkfs -t ext4 /dev/${mntfrom}
echo "mount"
mount /dev/${mntfrom} $homedir
;;  esac






read -p "Install java [y|n]? " answ
case $answ in y|Y) sudo yum install java;;  esac


### Database 
read -p "Install postgres [y|n]? " answ
case $answ in y|Y)
	sudo yum install postgresql93-server
	sudo service postgresql93 initdb
	sudo chkconfig postgresql93 on
	sudo service postgresql93 start


	sed -e 's/ident/trust/g' /var/lib/pgsql93/data/pg_hba.conf> dummy.conf
	sudo mv dummy.conf /var/lib/pgsql93/data/pg_hba.conf
	sudo service postgresql93 reload

	postgresPassword="pasword$RANDOM"
	sed -e 's/%password%/${postgresPassword}/g' $dir/postgres.init.sql> $dir/postgres.sql
	sudo su -c "psql -f $dir/postgres.sql"  - postgres
	rm $dir/postgres.sql
        printf "ramadda.db=postgres\nramadda.db.postgres.user=ramadda\nramadda.db.postgres.password=${postgresPassword}"  > ${homedir}/db.properties
;; esac


read -p "Install RAMADDA from SourceForge [y|n]? " answ
case $answ in y|Y) 
	rm -f ${dir}/ramaddaserver.zip
	rm -r -f ${dir}/ramaddaserver
	wget -O ${dir}/ramaddaserver.zip http://downloads.sourceforge.net/project/ramadda/ramadda1.7/ramaddaserver.zip
	unzip -o ${dir}/ramaddaserver.zip
	;;  esac

sed -e 's/.*ramaddainit.sh.*//g' /etc/rc.local> dummy.rc.local
printf "\nsh $dir/ramaddaserver/ramaddainit.sh start\n\n" >> dummy.rc.local
mv dummy.rc.local /etc/rc.local
printf "\n\nexport RAMADDA_HOME=${homedir}\nexport RAMADDA_PORT=80\n" > ramaddaserver/ramaddaenv.sh

read -p "Start RAMADDA [y|n]? " answ
case $answ in y|Y) 
	sh ramaddaserver/ramaddainit.sh restart
	;;  esac

exit














