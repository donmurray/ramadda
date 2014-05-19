
##
## You can define the default repository and (optionally) the user id and password 
## for running the ramadda client



if [ -z "$RAMADDA_CLIENT_REPOSITORY" ]; then
    export RAMADDA_CLIENT_REPOSITORY=http://localhost:8080/repository
fi  

if [ -z "$RAMADDA_CLIENT_USER" ]; then
    export RAMADDA_CLIENT_USER=
    export RAMADDA_CLIENT_PASSWORD=
fi  






