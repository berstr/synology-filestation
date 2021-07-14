
Set the following env variables:

    export NEW_RELIC_LICENSE_KEY=XXXX
    export NEW_RELIC_DISTRIBUTED_TRACING_ENABLED=true;
    export NEW_RELIC_APP_NAME=XXXX
    export SYNOLOGY_USERNAME=XXXX
    export SYNOLOGY_PASSWORD='XXXX'
    export SYNOLOGY_HOST=<hostname>

------------------------------------

    The default port number of this service is 37081

-------------------------------------

mvn package

Then start the application:

java  -javaagent:./newrelic/newrelic.jar -jar target/filestation-0.0.1-SNAPSHOT.jar

-------------------------------

Docker

docker build -t berndstransky/synology-filestation:1.0 .

docker push berndstransky/synology-filestation:1.0

X.Y is the image tag

on VM:
docker run -d --name synology-filestation -e SYNOLOGY_USERNAME -e SYNOLOGY_PASSWORD -e SYNOLOGY_HOST -e NEW_RELIC_LICENSE_KEY  -v /var/log/container:/logs -p37081:37081 berndstransky/synology-filestation:X.Y

on Macbook:
docker run -d -e SYNOLOGY_USERNAME -e SYNOLOGY_PASSWORD -e SYNOLOGY_HOST -e NEW_RELIC_LICENSE_KEY  -v $(pwd)/logs-docker:/logs -p37081:37081 berndstransky/synology-filestation:X.Y
