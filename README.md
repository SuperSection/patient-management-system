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
