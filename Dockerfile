FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar filestation.jar
COPY newrelic newrelic
ENTRYPOINT ["java","-javaagent:/newrelic/newrelic.jar","-jar","/filestation.jar"]