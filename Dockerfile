# the first stage of our build will use a maven 3.6.1 parent image
FROM maven:3.6.1-jdk-8-alpine AS MAVEN_BUILD

COPY ./ ./

ARG AUDIT_RESOURCESPATH=default_path
ARG CONTEXTS_SCRIPTPATHS=default_path
RUN sed -E -i "s@audit[.]resourcesPath=(.*)@audit.resourcesPath=$AUDIT_RESOURCESPATH@g" s-pipes-core/src/main/resources/config-core.properties
RUN sed -E -i "s@contexts[.]scriptPaths=(.*)@contexts.scriptPaths=$CONTEXTS_SCRIPTPATHS@g" s-pipes-core/src/main/resources/config-core.properties

RUN mvn clean package -T 2C -DskipTests -q

# the second stage of our build will use a tomcat:9.0-jdk8-slim
FROM tomcat:9.0-jdk8-slim

EXPOSE 8080

COPY --from=MAVEN_BUILD /s-pipes-web/target/s-pipes-web-*.war /usr/local/tomcat/webapps/spipes.war

CMD ["catalina.sh","run"]