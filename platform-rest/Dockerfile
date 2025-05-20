# Multi-stage build for platform-rest
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y wget ca-certificates tar && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.3/binaries/apache-maven-3.9.3-bin.tar.gz && \
    tar -xzf apache-maven-3.9.3-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.3/bin/mvn /usr/bin/mvn && \
    rm apache-maven-3.9.3-bin.tar.gz

# Copy full project, build platform-rest and its dependencies
COPY . .
RUN mvn clean package -pl platform-rest -am -DskipTests

# Runtime image
FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=build /app/platform-rest/target/platform-rest-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"] 