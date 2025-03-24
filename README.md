# Shift Left FinOps for Infra

## Problem Statement

  FinOps is something everyone is concerned ot talking about. As part of mordenization strategy applications are migrating on   cloud. Here costs comes into picture and it only comes under consideration only when applications are running and deployed    on cloud. Hence insted of full cycle why not have some cost analysis done during development phase itlself before we deploy   infra to the cloud.

## Solution

 Terraform is most popular and cloud augnostic tool used to provision infrastructure.
 In Terrafrom before provisioning infrastructure or applying we can see the plan file which has details of components that 
 are going to be applied.

 AWS cloud provider or any other provider exposes a pricing api which can be queried to give the cost of that component.
 Hence we can combine both these features Terraform Plan file and AWS pricing api to create solution which provide estimated 
 cost of infrastructure going to be provisioned at early stage.

## Implementation

 This application is written in java. Basically this application reads/parses Terrafom plan file in json format.
 Creates a request based on Pricing api request.
 Parses the response and gives the esimated cost for the planned infrastructure.  
 
  
 ## Current Architecture
  

<img width="519" alt="Screenshot 2025-03-07 112328" src="https://github.com/user-attachments/assets/d0d66629-17e7-4747-a15b-f7b7262988ba" />



## Screen Shot of wroking example

<img width="705" alt="image" src="https://github.com/user-attachments/assets/48872025-6ad5-47bc-a8f9-7885e3254dfd" />


## Future Scope

We can create a BackEnd api for this solution and host it , Which can b integrated with CICD pipeline as well as infrastucture provisioning pipeline. can slo be plugged with cli.
