# Patient Management System

## Basic Architecture of Spring Boot Application

### All Layers in an Application

![Layered Architecture](./diagrams/spring-boot-layered-architecture.png)

### Flow of the Architecture

![Flow Architecture](./diagrams/spring-boot-flow-architecture.png)

## Why use DTOs?

- Keep the internal domains models (your database entities) hidden from the client. This protects your app's internal structure.
- Allow you to send or receive only the fields relevant to the client, rather than exposing all fields in your domain model.
- Request DTOs allow you to validate client input (e.g., checking if fields are null or meet certain criteria).

---

## Swagger Integration

`springdoc-openapi` java library helps to automate the generation of API documentation using spring boot projects. It automatically generates documentation in **JSON/YAML and HTML format** APIs.

1. Integrate swagger-ui, by adding the dependency in `pom.xml`

    ```xml
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.8.6</version>
   </dependency>
    ```

2. This will automatically deploy swagger-ui to a spring-boot application:

    - The Swagger UI page will then be available at <http://server:port/context-path/swagger-ui.html>
    - The OpenAPI description will be available at the following url for json format: <http://server:port/context-path/v3/api-docs>

---

## Used Dependencies in this Project

### Dependency Versions as `<properties>` in `pom.xml`

```xml
  <properties>
    <java.version>21</java.version>
    <grpc.version>1.71.0</grpc.version>
    <protobuf-java.version>3.25.6</protobuf-java.version>
    <spring-grpc.version>0.5.0</spring-grpc.version>
    <spring-kafka.version>3.3.4</spring-kafka.version>
  </properties>
```

### List of Dependency, used as Requirement

```xml
<!--GRPC -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>${grpc.version}</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>${grpc.version}</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>${grpc.version}</version>
</dependency>

<dependency> <!-- necessary for Java 9+ -->
    <groupId>org.apache.tomcat</groupId>
    <artifactId>annotations-api</artifactId>
    <version>6.0.53</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>3.1.0.RELEASE</version>
</dependency>

<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>${protobuf-java.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>${spring-kafka.version}</version>
</dependency>
```

### `<build>` Section of `pom.xml` file

```xml
  <build>
    <extensions>
        <!-- Ensure OS compatibility for protoc -->
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.7.0</version>
        </extension>
    </extensions>
    <plugins>
        <!-- Spring boot / maven  -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>

        <!-- PROTO -->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:${protobuf-java.version}:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
  </build>
```

---

## Run docker compose to Start each service

1. Patient Service

    ```bash
    cd patient-service
    docker compose -f .\docker-compose.patient-service.yml up -d
    # This includes the patient-service-db (PostgreSQL)
    ```

2. Billing Service

    ```bash
    cd billing-service
    docker compose -f .\docker-compose.billing-service.yml up -d
    ```

3. Kafka

    ```bash
    # from Root of the project
    docker compose up -d
    ```

---

## gRPC (Google Remote Procedure Calls)

gRPC is a high-performance, open-source RPC (Remote Procedure Call) framework that enables seamless communication between microservices. It uses Protocol Buffers (Protobuf) as the interface definition language (IDL) and supports bi-directional streaming and efficient binary serialization.

### Why Use gRPC?

- **Efficient & Fast** - Uses HTTP/2 and Protobuf for compact and fast communication.
- **Strongly Typed APIs** - Enforces contract-based communication using .proto files.
- **Streaming Support** - Supports Unary, Server Streaming, Client Streaming, and Bi-directional Streaming.
- **Multi-language Support** - Works across multiple programming languages.
- gRPC supports both synchronous and asynchronous Remote Procedure Calls (RPCs)

### Architecture in Microservices

- gRPC Server (Billing Service) → Provides gRPC services.
- gRPC Clients (Other Services) → Consume the services via generated stubs.
- Proto File (.proto) → Defines the service contract.

### gRPC Implementation in `billing_service.proto`

```proto
syntax = "proto3";

package billing;
option java_multiple_files = true;
option java_package = "com.supersection.grpc";
option java_outer_classname = "BillingProto";


service BillingService {
  rpc CreateBillingAccount (BillingRequest) returns (BillingResponse);
}

message BillingRequest {
  string patientId = 1;
  string name = 2;
  string email = 3;
}

message BillingResponse {
  string accountId = 1;
  string status = 2;
}
```

#### Generate Java Classes from Proto

```bash
mvn clean compile
```

### gRPC Communication Flow

1. Client Microservice calls gRPC stub.
2. Stub converts request into a Protobuf message and sends it via HTTP/2.
3. Server Microservice (Billing Service) processes the request and sends a response.
4. Client receives response and processes it.

---

## Kafka

### gRPC vs Kafka

- **gRPC**: 1-to-1 microservice communication, when you need an immediate response (synchronous).
- **Kafka**: 1-to-many microservice communicate, do not need an immediate response (i.e., asynchronous)

### Architecture of Kafka

![Kafka Architecture](./diagrams/kafka-architecture.png)

### IDE Setup

- **VS Code**: Install "vscode-kafka" extension and Add a new Cluster
- **IntelliJ IDEA**: Install "Kafka" plugin and Add a new Connection

> For VSCode, use `.kafka` files for handling consumer & producer

---

## Resolve Package related Issues

1. Go into the specific service folder, for Example:

    ```bash
    cd analytics-service
    ```

2. Remove already existing target folder

    ```bash
    rm -rf target
    ```

3. Compi;e the code freshly using Maven

    ```bash
    mvn clean generated-sources compile
    ```

4. Check if the files are correctly regenerated.

---
