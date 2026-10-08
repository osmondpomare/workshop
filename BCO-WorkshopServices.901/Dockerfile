FROM maven:3.9-eclipse-temurin-8 AS build

WORKDIR /workshop

COPY libs libs
COPY BCO-WorkshopServices.901 BCO-WorkshopServices.901

RUN mvn -B -f BCO-WorkshopServices.901/BCO.WKS.901/pom.xml clean package \
 && mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
      -Dartifact=org.postgresql:postgresql:42.7.4 \
      -DoutputDirectory=/workshop/jdbc

FROM icr.io/appcafe/open-liberty:full-java8-openj9-ubi-minimal

ENV SERVICE_REGISTRY_PATH=/config/service-registry.properties

COPY --chown=1001:0 --from=build /workshop/jdbc/ /opt/ol/wlp/usr/shared/resources/jdbc/
COPY --chown=1001:0 BCO-WorkshopServices.901/liberty/server.xml /config/server.xml
COPY --chown=1001:0 BCO-WorkshopServices.901/liberty/service-registry.properties /config/service-registry.properties
COPY --chown=1001:0 --from=build /workshop/BCO-WorkshopServices.901/BCO.WKS.901.EAR/target/BCO.WorkshopServices.901.ear /config/apps/

RUN configure.sh

EXPOSE 9080 9443
