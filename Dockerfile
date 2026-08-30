FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

ENV TZ=Asia/Seoul
RUN mkdir -p /app/logs

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
