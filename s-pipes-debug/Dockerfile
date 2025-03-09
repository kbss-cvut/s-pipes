# STAGE MAVEN_BUILD
FROM maven:3-eclipse-temurin-17 AS maven_build

WORKDIR /s-pipes

COPY ./ ./

RUN mvn clean package -T 2C -DskipTests -q

# FINAL STAGE
FROM eclipse-temurin:17-jre

EXPOSE 8080

WORKDIR /app

COPY --from=maven_build "/s-pipes/s-pipes-debug/target/s-pipes-debug-*.jar" /app/s-pipes-debug.jar

CMD ["java", "-jar", "s-pipes-debug.jar"]