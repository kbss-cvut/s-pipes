# STAGE MAVEN_BUILD
FROM maven:3.8.6-openjdk-11 AS MAVEN_BUILD

COPY ./ ./

RUN mvn clean package -T 2C -DskipTests -q


# STAGE MODULES_CHECKOUT
FROM alpine/git:v2.32.0 AS MODULES_CHECKOUT

WORKDIR /
RUN git clone --depth 1 https://github.com/blcham/s-pipes-modules


# FINAL STAGE
FROM tomcat:9.0-jdk11-corretto

EXPOSE 8080

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=MODULES_CHECKOUT ./s-pipes-modules /scripts/s-pipes-modules
COPY --from=MAVEN_BUILD /s-pipes-web/target/s-pipes-web-*[0-9] /usr/local/tomcat/webapps/s-pipes

CMD ["catalina.sh","run"]