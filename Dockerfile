FROM amazoncorretto:17 as build
WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

RUN sed -i 's/\r//' gradlew && chmod +x gradlew
RUN ./gradlew dependencies

COPY src src
ENV JAVA_TOOL_OPTIONS="-Xmx2g -Xms512m"
RUN ./gradlew build -x test --no-daemon

FROM amazoncorretto:17

EXPOSE 80
ENV PROJECT_NAME=monew
ENV PROJECT_VERSION=0.0.1-SNAPSHOT
ENV JVM_OPTS=""

WORKDIR /app

COPY --from=build /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--server.port=80"]