# Configure Spring Cloud Gateway in Consumption Tier

This article shows you how to configure Spring Cloud Gateway with Azure Spring Apps Consumption Tier.

[Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html/) is an open-source gateway built on top of the Spring Framework, which is designed to handle API requests and provide routing, filtering, and load balancing capabilities for microservices architecture. It serves as a "front door" for multiple microservices, routing incoming requests to the appropriate service and providing cross-cutting concerns such as security, monitoring, and rate limiting.

## Prerequisites

- An already provisioned Azure Spring Apps Consumption Tier service instance. For more information, see [Create Azure Spring Apps Consumption Plan](https://github.com/Azure/Azure-Spring-Apps-Consumption-Plan/blob/main/articles/create-asa-standard-gen2.md).
- Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) version 2.28.0 or higher.

## Deploy Backend Apps

In this example, we will first create two sample backend miscroservices to allow Spring Cloud Gateway routing requests to them.

### Step 1: Create an App called "demo1" in the Azure Spring Apps Consumption Tier service instance

- Use the following command to specify the app name on Azure Spring Apps as "demo1".
```
az spring app create `
    --resource-group <name-of-resource-group> `
    --service <service-instance-name> `
    --name demo1 `
    --cpu 500m `
    --memory 1Gi `
    --instance-count 1 `
    --assign-endpoint true
```

- Build the Spring Boot sample project
   - Navigate to https://start.spring.io. This service pulls in all the dependencies you need for an application and does most of the setup for you.
   - Choose Maven and the Java version you want to use. 
   - Click Dependencies and select Spring Web.
   - Click Generate.
   - Download the resulting ZIP file, which is an archive of a web application that is configured with your choices.
