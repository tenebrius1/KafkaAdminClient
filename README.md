# KafkaAdminClient Platform

## Overview

This project provides a programmatic interface for dynamic Kafka cluster reconfiguration through a RESTful HTTP API.  It is a foundational component within a larger initiative to design and implement a cloud-native stream processing engine. By enabling programmatic Kafka cluster management, the project lays the groundwork for advanced features such as:

- Automated Rebalancing: Implement intelligent rebalancing protocols to optimize cluster performance and resource utilization.
- Rack-Aware Partition Placement: Enhance data locality and resilience by strategically co-locating Kafka partitions.
- Self-Healing Capabilities: Develop mechanisms to automatically detect and mitigate Kafka cluster anomalies, ensuring high availability.

## Key features

Key Features

- HTTP API: Interact with and modify Kafka cluster configurations using a well-defined API.
- Extensibility: Designed as a modular component, allowing readily integrated into a cloud-native stream processing solution.

## Potential Use Cases

- DevOps Automation: Streamline Kafka cluster management within CI/CD pipelines and infrastructure-as-code environments.
- Dynamic Scaling: Respond to changing workloads by programmatically adjusting Kafka resources.
- Advanced Cluster Optimization: Implement custom rebalancing strategies tailored to specific use cases.

## Getting Started

### Requirements

- Java-17
- Gradle-7.4.2
- Docker
- KinD/minikube
- Strimzi Kafka

### Build from source

1. Fork and clone this repo using `git clone`
2. Install the requirements as stated above. If you have conda installed, run `conda create --name <myenv> --file environment.yml`
3. Run `gradle build && gradle run` which will start the Springboot API server
4. By default, Springboot runs on `localhost:8080` which is how you will interact with the API server
