FROM maven:3.9.16-eclipse-temurin-25-alpine AS build
WORKDIR /usr/src/app
COPY . .
RUN mvn clean package -U -DskipTests -Dmaven.javadoc.skip=true

FROM amazoncorretto:25-al2023-headless
WORKDIR /usr/app
COPY --from=build /usr/src/app/target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/usr/app/app.jar"]
