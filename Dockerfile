FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

RUN chmod +x gradlew && sed -i 's/\r$//' gradlew

COPY src src

RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENV SERVER_PORT=8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
