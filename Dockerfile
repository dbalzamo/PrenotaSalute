FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

ARG GITHUB_TOKEN
ARG GITHUB_ACTOR

RUN mkdir -p /root/.m2 && \
    echo "<settings><servers><server><id>github</id><username>${GITHUB_ACTOR}</username><password>${GITHUB_TOKEN}</password></server></servers></settings>" \
    > /root/.m2/settings.xml

COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]