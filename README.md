make sure you have build the package with the latest file versions (also the react assets like bundle, index, etc.):

Set the following env variables:

    export NEW_RELIC_LICENSE_KEY=XXXX
    export SYNOLOGY_USERNAME=XXXX
    export SYNOLOGY_PASSWORD='XXX'

Optional (if not set, localhost is used):

    export SYNOLOGY_HOST=<hostname>
    The port number is 37081

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
