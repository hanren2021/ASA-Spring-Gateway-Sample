# Configure Spring Cloud Gateway in Basic/Standard Tier

This article shows you how to configure Spring Cloud Gateway with Azure Spring Apps Basic/Standard Tier.

[Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html/) is an open-source gateway built on top of the Spring Framework, which is designed to handle API requests and provide routing, filtering, and load balancing capabilities for microservices architecture. It serves as a "front door" for multiple microservices, routing incoming requests to the appropriate service and providing cross-cutting concerns such as security, monitoring, and rate limiting.

## Prerequisites

- An already provisioned Azure Spring Apps Basic/Standard Tier service instance. For more information, see [Provision an instance of Azure Spring Apps](https://learn.microsoft.com/en-us/azure/spring-apps/quickstart?tabs=Azure-CLI#provision-an-instance-of-azure-spring-apps).
- Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) version 2.28.0 or higher.

## Deploy Backend Apps

In this example, we will first create two sample backend miscroservices to allow Spring Cloud Gateway routing requests to them.

### Step 1: Create an App called "demo1" in the Azure Spring Apps Basic/Standard Tier service instance

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
   - Choose Maven, Spring Boot, Java version you want to use. 
   - Click Dependencies and select **Spring Web** and **Eureka Discovery Client**.
   - Click Generate and download the resulting ZIP file, which is an archive of a web application that is configured with your choices.
   
   ![image](https://user-images.githubusercontent.com/90367028/219294377-47ba3fc2-6a65-46bf-a358-adbcc0ab9863.png)
   
   - Make sure the following dependency can be found in the pom.xml file
     ```
     <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
     </dependency>
     ```
     
   - Add an annotation (@EnableEurekaClient) to the top level class of your application, as shown in the following example:
     ```
     package com.gatewayexample.demo;

     import org.springframework.boot.SpringApplication;
     import org.springframework.boot.autoconfigure.SpringBootApplication;
     import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

     @SpringBootApplication
     @EnableEurekaClient
     public class DemoApplication {
	      public static void main(String[] args) {
		     SpringApplication.run(DemoApplication.class, args);
	      }
     }
     ```

   - Create a new HelloController.java file in the same folder as DemoApplication.java.
     ```
     package com.gatewayexample.demo;

     import org.springframework.web.bind.annotation.GetMapping;
     import org.springframework.web.bind.annotation.RestController;

     @RestController
     public class HelloController {

	  @GetMapping("/demo1")
	  public String index() {
		return "Greetings from demo 1!";
	  }
     }   
     ```
   
    - Run Maven command to build the project
    
      ```
      mvn clean package -DskipTests
      ```
      
      There should be a demo-0.0.1-SNAPSHOT.jar file generated under the ./target/ folder
      
-  Use the following command to deploy your "demo1" Azure Spring App.
   ```
   az spring app deploy `
       --resource-group <name-of-resource-group> `
       --service <service-instance-name> `
       --name demo1 `
       --artifact-path <file path to demo-0.0.1-SNAPSHOT.jar> `
       --runtime-version Java_17 `
       --jvm-options '-Xms512m -Xmx800m'
   ```

- Test your demo1 Azure Spring App
  Navigate to https://<instance-name>-demo1.azuremicroservices.io/demo1, you should be able the get the response like this:
  
  ![image](https://user-images.githubusercontent.com/90367028/219324968-fe4d3efc-1d2c-4d97-bafa-2f1c3ed9773b.png)



### Step 2: Create an App called "demo2" in the Azure Spring Apps Basic/Standard Tier service instance
- Use the following command to specify the app name on Azure Spring Apps as "demo2".
  ```
  az spring app create `
      --resource-group <name-of-resource-group> `
      --service <service-instance-name> `
      --name demo2 `
      --cpu 500m `
      --memory 1Gi `
      --instance-count 1 `
      --assign-endpoint true
  ```

- Make a slight change in the HelloController.java file we just created in **step 1**
  ```
  package com.gatewayexample.demo;

  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RestController;

  @RestController
  public class HelloController {

	  @GetMapping("/demo2")
	  public String index() {
	  	return "Greetings from demo 2!";
	  }

  }
  ```
  
  You can also change the package version number to **0.0.2-SNAPSHOT** in your pom.xml like this:
  ```
  <groupId>com.gatewayexample</groupId>
  <artifactId>demo</artifactId>
  <version>0.0.2-SNAPSHOT</version>
  <name>demo</name>
  ```
  
- Run Maven command to build the project

  ```
  mvn package -DskipTests
  ```
  There should be a demo-0.0.2-SNAPSHOT.jar file generated under the ./target/ folder
      
- Use the following command to deploy your "demo2" Azure Spring App.
  ```
  az spring app deploy `
       --resource-group <name-of-resource-group> `
       --service <service-instance-name> `
       --name demo2 `
       --artifact-path <file path to demo-0.0.2-SNAPSHOT.jar> `
       --runtime-version Java_17 `
       --jvm-options '-Xms512m -Xmx800m'
  ```

- Test your demo1 Azure Spring App
  Navigate to https://<instance-name>-demo2.azuremicroservices.io/demo2, you should be able the get the response like this:
  
  ![image](https://user-images.githubusercontent.com/90367028/219325325-3c0c807e-cc33-413e-90a9-6e1af995b966.png)


### Step 3: Create the Getway App in the Azure Spring Apps Basic/Standard Tier service instance
- Use the following command to specify the app name on Azure Spring Apps as "gateway".
```
az spring app create `
    --resource-group <name-of-resource-group> `
    --service <service-instance-name> `
    --name gateway `
    --cpu 500m `
    --memory 1Gi `
    --instance-count 1 `
    --assign-endpoint true
```

- Build the Spring Boot Gateway project
   - Navigate to https://start.spring.io. This service pulls in all the dependencies you need for an application and does most of the setup for you.
   - Choose Maven, Spring Boot, Java version you want to use. 
   - Click Dependencies and select **Spring Web** , **Eureka Discovery Client** and **Gateway**.
   - Click Generate and download the resulting ZIP file, which is an archive of a web application that is configured with your choices.
   
     ![image](https://user-images.githubusercontent.com/90367028/219305653-c359f806-e60b-4820-9f81-67f8ec093fff.png)

   - Make sure the following dependencies can be found in the pom.xml file
     ```
     <dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-gateway</artifactId>
     </dependency>
     <dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
     </dependency>
     ```
     
   - Add an annotation (@EnableEurekaClient) to the top level class of your application, as shown in the following example: 
     ```
     package com.gatewayexample.gateway;

     import org.springframework.boot.SpringApplication;
     import org.springframework.boot.autoconfigure.SpringBootApplication;
     import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

     @SpringBootApplication
     @EnableEurekaClient
     public class GatewayApplication {

	     public static void main(String[] args) {
        		SpringApplication.run(GatewayApplication.class, args);
	     }

     }
     ```

   - Create application.yml file under ./resources/ folder with the following contents
     ```
     spring:
     cloud:
       gateway:
         routes:
           - id: demo_one
             uri: lb://demo1
             predicates:
               - Path=/demo1/**
           - id: demo_two
             uri: lb://demo2
             predicates:
               - Path=/demo2/**
     ```
     With the above setting, all the requests send to gateway app /demo1 should be routed to App demo1. All the requests send to gateway app /demo2 should be routed to App demo2.

     For more details about about to configure Spring Cloud Gateway, please refer to https://cloud.spring.io/spring-cloud-gateway/reference/html/.
     
   - Run Maven command to build the project
    
      ```
      mvn clean package -DskipTests
      ```
      There should be a gateway-0.0.1-SNAPSHOT.jar file generated under the ./target/ folder

-  Use the following command to deploy your "gateway" Azure Spring App.
   ```
   az spring app deploy `
       --resource-group <name-of-resource-group> `
       --service <service-instance-name> `
       --name gateway `
       --artifact-path <file path to gateway-0.0.1-SNAPSHOT.jar> `
       --runtime-version Java_17 `
       --jvm-options '-Xms512m -Xmx800m'
   ```
	
	
- Test your gateway Azure Spring App
  Navigate to https://<instance-name>-gateway.azuremicroservices.io/demo1, the Spring Cloud gateway should be able to route the request to your demo1 app, and you should be able the get the response like this:
  
  ![image](https://user-images.githubusercontent.com/90367028/219326127-542fe3a5-06c6-416b-8bca-b993b6d2e024.png)

  Navigate to https://<instance-name>-gateway.azuremicroservices.io/demo2, the Spring Cloud gateway should be able to route the request to your demo2 app, and you should be able the get the response like this:
  
  ![image](https://user-images.githubusercontent.com/90367028/219326519-80ce73e0-741e-4a8a-8ddf-d05685a5d044.png)
