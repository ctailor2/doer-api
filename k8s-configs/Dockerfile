FROM openjdk:8-jdk
COPY . .
RUN ./gradlew assemble
RUN cp api/build/libs/*boot.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/app.jar"]