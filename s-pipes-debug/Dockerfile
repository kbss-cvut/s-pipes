# STAGE MAVEN_BUILD
FROM maven:3.8.6-openjdk-11 AS MAVEN_BUILD

COPY ./ ./

RUN mvn clean package -T 2C -DskipTests -q


# STAGE MODULES_CHECKOUT
FROM alpine/git:v2.32.0 AS MODULES_CHECKOUT

WORKDIR /
RUN git clone --depth 1 https://kbss.felk.cvut.cz/gitblit/r/s-pipes-modules.git


# FINAL STAGE
FROM tomcat:9.0-jdk11-corretto

EXPOSE 8080

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=MODULES_CHECKOUT ./s-pipes-modules /scripts/s-pipes-modules
COPY --from=MAVEN_BUILD /s-pipes-web/target/s-pipes-web-*.war /usr/local/tomcat/webapps/s-pipes.war
COPY --from=MAVEN_BUILD /s-pipes-debug/target/s-pipes-debug-*.war /usr/local/tomcat/webapps/s-pipes-debug.war

CMD ["catalina.sh","run"]