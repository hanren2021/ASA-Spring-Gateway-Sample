# Configure Spring Cloud Gateway in Azure Spring Apps Standard consumption plan (with ACA internal URL)

This article shows you how to configure Spring Cloud Gateway with Azure Spring Apps Standard consumption plan.

[Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html/) is an open-source gateway built on top of the Spring Framework, which is designed to handle API requests and provide routing, filtering, and load balancing capabilities for microservices architecture. It serves as a "front door" for multiple microservices, routing incoming requests to the appropriate service and providing cross-cutting concerns such as security, monitoring, and rate limiting.

In this artical, we are going to show an exmple, in which the client can call the backend App through a spring cloud gateway app.
![image](https://user-images.githubusercontent.com/90367028/231124157-ac3e8ae6-7300-47b5-91d4-b4840f86cfbc.png)


## Prerequisites

- An already provisioned Azure Spring Apps Standard consumption plan. service instance. For more information, see [Create Azure Spring Apps Consumption Plan](https://learn.microsoft.com/en-us/azure/spring-apps/quickstart-provision-standard-consumption-service-instance?tabs=Azure-portal).
- Install the [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) version 2.28.0 or higher.


### Step 1: Create the Gateway App in the Azure Spring Apps Standard consumption plan service instance
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

Note: we use "--assign-endpoint true", since the Gateway App should be the one exposed to clients making direct call from outside. 

After the gateway App being created, on the App Portal Overview page, we can get the App's access URL like this:
https://gateway.[env-name].[region].azurecontainerapps.io

- Build the Spring Boot Gateway project
   - Navigate to https://start.spring.io. This service pulls in all the dependencies you need for an application and does most of the setup for you.
   - Choose Maven, Spring Boot, Java version you want to use. 
   - Click Dependencies and select **Spring Web** and **Gateway**.
   - Click Generate and download the resulting ZIP file, which is an archive of a web application that is configured with your choices.
   
     ![image](https://user-images.githubusercontent.com/90367028/230290018-62842f50-77ac-47c6-b885-be8fb7d4e4d3.png)

   - Make sure the following dependencies can be found in the pom.xml file
     ```
     <dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-gateway</artifactId>
     </dependency>
     ```
     
   - The top level class of your application should look like this: 
     ```
     package com.gatewayexample.gateway;

     import org.springframework.boot.SpringApplication;
     import org.springframework.boot.autoconfigure.SpringBootApplication;

     @SpringBootApplication
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
           - id: <backend-app-name>
             uri: <backend-app-uri>
             predicates:
               - Path=<predicates-path>
     ```
     For more details about about how to configure Spring Cloud Gateway, please refer to https://cloud.spring.io/spring-cloud-gateway/reference/html/.
     
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
       --runtime-version Java_17
   ```


### Step 2: Create an App called "demo1" in the Azure Spring Apps Standard consumption plan service instance

We will now create a backend app to test the gateway connection. To do this, we will create a simple spring web application, then add a predicate in the gateway resource file, so our gateway app can foward the incoming request to this backend app.

- Use the following command to specify the app name on Azure Spring Apps as "demo1".
```
az spring app create `
    --resource-group <name-of-resource-group> `
    --service <service-instance-name> `
    --name demo1 `
    --cpu 500m `
    --memory 1Gi `
    --instance-count 1
```

- Build the Spring Boot sample project
   - Navigate to https://start.spring.io. This service pulls in all the dependencies you need for an application and does most of the setup for you.
   - Choose Maven, Spring Boot, Java version you want to use. 
   - Click Dependencies and select **Spring Web**.
   - Click Generate and download the resulting ZIP file, which is an archive of a web application that is configured with your choices.
   
   ![image](https://user-images.githubusercontent.com/90367028/230283348-5bce3fab-8a1a-4fcf-9de0-b5fc8a3a825c.png)
   
   - The top level class of your application should look like this:
     ```
     package com.gatewayexample.demo1;
     
     import org.springframework.boot.SpringApplication;
     import org.springframework.boot.autoconfigure.SpringBootApplication;
     
     @SpringBootApplication
     public class Demo1Application {
     
     	public static void main(String[] args) {
     		SpringApplication.run(Demo1Application.class, args);
     	}
     }
     ```

   - Create a new HelloController.java file in the same folder as DemoApplication.java.
     ```
     package com.gatewayexample.demo1;

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
      
      There should be a demo1-0.0.1-SNAPSHOT.jar file generated under the ./target/ folder
      
-  Use the following command to deploy your "demo1" Azure Spring App.
   ```
   az spring app deploy `
       --resource-group <name-of-resource-group> `
       --service <service-instance-name> `
       --name demo1 `
       --artifact-path <file path to demo-0.0.1-SNAPSHOT.jar> `
       --runtime-version Java_17
   ```

- Get your demo1 App's **internal** URL

  In the App Overview page, we can get the App's internal access URL, which can be used by Apps running inside the same container environment to execute internal calls. 
  https://demo1.internal.[env-name].[region].azurecontainerapps.io
  
  ![image](https://user-images.githubusercontent.com/90367028/230330808-128e5cd8-b7bc-4e65-931c-ad3fac5908a3.png)
  
- Test your demo1 Azure Spring App

  To enable client calls to the App from outside the container environment, we need to assign an endpoint to the App. If you do not want the App to be directly called by external clients, we can unassign the endpoint after the test.
  
  ![image](https://user-images.githubusercontent.com/90367028/230331162-ca1cbf08-3ee2-436b-af58-44eb99480d8f.png)

  After the endpoint being assigned, we can get an external accessible url like this:
  https://demo1.[env-name].[region].azurecontainerapps.io
  
  ![image](https://user-images.githubusercontent.com/90367028/230329698-55fb47f9-4b41-49dd-afa6-efdc1fe5ef76.png)

  Navigate to https://demo1.[env-name].[region].azurecontainerapps.io/demo1, you should be able the get the response like this:
  
  ![image](https://user-images.githubusercontent.com/90367028/230329383-cbe4f5ec-4b13-4d4c-9f7c-38d981fad1fa.png)

- Modify the application.yml file of the **gateway** app, then redeploy and resart the gateway app to activate the changes.
  ```
     spring:
      cloud:
       gateway:
         routes:
           - id: demo_one
             uri: https://demo1.internal.[env-name].[region].azurecontainerapps.io
             predicates:
               - Path=/demo1/**
  ```
  With the above setting, all the requests send to gateway app /demo1 should be routed to App demo1.
     
  Since we are using the internal URL (https://demo1.internal.[env-name].[region].azurecontainerapps.io), the traffics between gateway and demo1 apps will remain inside the container environment.
  
  
### Step 3: Test your gateway Azure Spring App

  Navigate to https://gateway.[env-name].[region].azurecontainerapps.io/demo1, the Spring Cloud gateway should be able to route the request to your demo1 app, and you should be able the get the response like this:
  
  ![image](https://user-images.githubusercontent.com/90367028/230337403-a5831c92-deb8-46b4-b9c3-d31d009c61bb.png)

