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
- Dynamic Broker Management: Add, remove, and update Kafka brokers programmatically.
- Dynamic Partition Reassignment: Rebalance Kafka partitions across brokers to optimize cluster performance.
- Dynamic Topic Management: Create, delete, and update Kafka topics on-the-fly.

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

### Deployment

1. Fork and clone this repo using `git clone`
2. Install the requirements as stated above. If you have conda installed, run `conda create --name <myenv> --file environment.yml`
3. Run `start-strimzi.sh` to set up a Kubernetes namespace and deploy a Kafka cluster using Strimzi, configured to be operating in Kraft mode
4. Wait for a few seconds for the Strimzi cluster to be configured and ready
5. Run `kubectl apply -f manifests/strimzi-cluster.yml -n kafka` to spin up the controllers, brokers and ingress

*Note that the steps above are for cloud deployment and will not work on local machines. For a managed Kubernetes platform, visit https://www.digitalocean.com/products/kubernetes.

### API Sample Usage

`KafkaAdminClient_API.json` contains a sample [Postman](https://www.postman.com/) collection that demonstrates how to interact with the KafkaAdminClient API.

To use the collection, import it into Postman and update the environment variables to match your Kafka cluster configuration. The only environment variable that needs to be updated is `base_url`, which should be set to the base URL of the KafkaAdminClient API, i.e. IP address of LoadBalancer.