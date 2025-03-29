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
