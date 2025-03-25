FROM eclipse-temurin:17-jdk-alpine as runtime

COPY "./target/s-pipes-debug-[0-9]*[0-9].jar" s-pipes-debug.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/s-pipes-debug.jar"]
