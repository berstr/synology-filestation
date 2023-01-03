
Set the following env variables:

    export NEW_RELIC_LICENSE_KEY=XXXX
    export NEW_RELIC_DISTRIBUTED_TRACING_ENABLED=true;
    export NEW_RELIC_APP_NAME=XXXX
    export NEW_RELIC_APPLICATION_LOGGING_ENABLED=true
    export NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED=true
    export NEW_RELIC_APPLICATION_LOGGING_FORWARDING_MAX_SAMPLES_STORED=10000
    export NEW_RELIC_APPLICATION_LOGGING_LOCAL_DECORATING_ENABLED=false

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

Build and deploy container image:

docker build -t bstransky/synology-filestation:X.Y .

docker push bstransky/synology-filestation:X.Y


Start container: 

docker run -d --name synology-filestation -e SYNOLOGY_USERNAME -e SYNOLOGY_PASSWORD -e SYNOLOGY_HOST -e NEW_RELIC_LICENSE_KEY -e NEW_RELIC_DISTRIBUTED_TRACING_ENABLED -e NEW_RELIC_APP_NAME -e NEW_RELIC_APPLICATION_LOGGING_ENABLED -e NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED -e NEW_RELIC_APPLICATION_LOGGING_FORWARDING_MAX_SAMPLES_STORED -e NEW_RELIC_APPLICATION_LOGGING_LOCAL_DECORATING_ENABLED  -p37081:37081 bstransky/synology-filestation:X.Y
