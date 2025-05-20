# Multi-stage build for coordinator
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

# Install Maven manually
RUN apt-get update && \
    apt-get install -y wget ca-certificates tar && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.3/binaries/apache-maven-3.9.3-bin.tar.gz && \
    tar -xzf apache-maven-3.9.3-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.3/bin/mvn /usr/bin/mvn && \
    rm apache-maven-3.9.3-bin.tar.gz

# Prefetch dependencies offline
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY platform-tcp/pom.xml platform-tcp/pom.xml
COPY platform-rest/pom.xml platform-rest/pom.xml
COPY coordinator/pom.xml coordinator/pom.xml
COPY kafka-consumer/pom.xml kafka-consumer/pom.xml
RUN mvn dependency:go-offline -B

# Copy full project, build coordinator and its dependencies
COPY . .
RUN mvn clean package -pl coordinator -am -DskipTests -B

# Runtime image
FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=build /app/coordinator/target/coordinator-1.0.0.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"] 