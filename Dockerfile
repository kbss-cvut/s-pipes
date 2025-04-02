# STAGE MAVEN_BUILD
FROM maven:3-eclipse-temurin-17 AS maven_build

WORKDIR /s-pipes

COPY ./ ./

RUN mvn clean package -T 2C -DskipTests -q


# STAGE MODULES_CHECKOUT
FROM alpine/git:v2.32.0 AS modules_checkout

WORKDIR /
RUN git clone --depth 1 https://github.com/blcham/s-pipes-modules


# FINAL STAGE
FROM tomcat:10-jdk17-temurin

EXPOSE 8080

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=modules_checkout ./s-pipes-modules /scripts/s-pipes-modules
COPY --from=maven_build "/s-pipes/s-pipes-web/target/s-pipes-web-*[0-9]" /usr/local/tomcat/webapps/s-pipes

CMD ["catalina.sh", "run"]
