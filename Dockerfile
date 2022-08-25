# the first stage of our build will use a maven 3.6.1 parent image
FROM maven:3.8.6-openjdk-11 AS MAVEN_BUILD

COPY ./ ./

RUN mvn clean package -T 2C -DskipTests -q

# the second stage of our build will use a tomcat:9.0-jdk8-slim
FROM tomcat:9.0-jdk8-slim

EXPOSE 8080

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=MAVEN_BUILD /s-pipes-web/target/s-pipes-web-*.war /usr/local/tomcat/webapps/s-pipes.war

CMD ["catalina.sh","run"]
