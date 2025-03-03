# PR Reviewer Assistant 

## Problem Statement:
   Once developer raises a PR and waits for review comments from other team members there is a wait time since other members are also busy in completing their task and usually review comments happens at end of sprint and developer has to accomodate changes in limited time it also buys in time from fellow members since they might also be busy in their critical task.

## Solution :
     PR Reviewer will target this problem and act as an assistant and as soon as developer raises PR AI Assitant will review the code and will provide review comments immediately saving deveoper time and overall team time for reviewing code and hence enhancing velocity of team.



## Current Architecture.
    This solution is basically develop in java leveraging prompt engineering in AWS Bedrock using its model's api and Git hub Webhook.

![image](https://github.com/user-attachments/assets/740ced81-9835-4bdb-8ffa-bef54cd03f6d)

- As soon as user will raise a PR.
- Web Hook will call AWS Lambda Function Url where the prompt logic resides.
- AWS Lambda will receive Web Hook PR request as Json including all the data like who rasied PR, what all contents are there 
  in PR and will call AWS BedRock "Claud" model id :**anthropic.claude-3-haiku-20240307-v1 **providing the prompt input.
- Bedrock will provide review comments based on input provided
- Which will be returned to GIT Hub and to user who rasied the PR.

## Prompt Input:

   You are a code reviewer analyzing a pull request.
            Title: %s
            Description: %s
            Diff URL: %s
            
            Please provide a thorough code review focusing on:
            1. Code quality and best practices
            2. Potential bugs or issues
            3. Security concerns
            4. Performance implications
            5. Suggestions for improvement
            
            Format your response in markdown.
            """, title, body, diffUrl);





  ## Screen shots of working example: 

  - ###User raises a PR :

    <img width="665" alt="image" src="https://github.com/user-attachments/assets/5432f48b-59b1-4508-b227-77298417f231" />


  - Web Hook gets trigerred and calls AWS Lambda Function URL

    <img width="528" alt="image" src="https://github.com/user-attachments/assets/5e40f215-a79e-40c7-b0d9-084f9a8c0925" />

   

    <img width="626" alt="image" src="https://github.com/user-attachments/assets/ce2ed471-0315-4523-b56c-46ad0f8928e3" />


   - AWS Lambda gets trigerred and calls Bedrock model api based on Prompt provided.

     <img width="701" alt="image" src="https://github.com/user-attachments/assets/b954c3a4-241a-4959-a20f-b69abdcaf257" />

   - Response is provded to Git Hub with review comments

     <img width="775" alt="image" src="https://github.com/user-attachments/assets/d424ccb7-0183-4597-a071-a1b8564f458b" />


---------------------------------------------------------------------------------------------------------------------------
## Sample Response

   Here is a thorough code review of the pull request:

## Code Review

### 1. Code Quality and Best Practices

1. **Naming Conventions**: The class name `BedrockConfig` follows the recommended Pascal case convention for class names. However, the file name `bedrock_config.py` does not follow the same convention and should be renamed to `bedrock_config.py`.
2. **Docstrings**: The class `BedrockConfig` and its methods should have detailed docstrings that explain the purpose, parameters, and return values of the class and its methods.
3. **Type Annotations**: The code should use type annotations to improve code readability and maintainability.
4. **Configuration Management**: The configuration values should be loaded from a separate configuration file or environment variables to make the code more modular and easier to manage.
5. **Error Handling**: The code should have proper error handling and logging to make it easier to debug and troubleshoot issues.

### 2. Potential Bugs or Issues

1. **Default Values**: The default values for the configuration parameters should be carefully considered and documented to ensure they are appropriate for the application's use case.
2. **Parsing Errors**: The code does not handle parsing errors for the configuration values, which could lead to runtime errors if the configuration file is not in the expected format.
3. **Concurrency**: The code does not consider thread-safety or concurrent access to the `BedrockConfig` class, which could lead to race conditions or other synchronization issues in a multi-threaded or distributed environment.

### 3. Security Concerns

1. **Sensitive Information**: The code does not address the handling of sensitive information, such as API keys, database credentials, or other confidential data, which should be stored and accessed securely.
2. **Input Validation**: The code does not validate the input configuration values, which could lead to potential security vulnerabilities if the configuration file is not properly sanitized.

### 4. Performance Implications

1. **Caching**: The code could benefit from caching the configuration values to improve performance, especially if the configuration is accessed frequently or the configuration file is stored in a remote location.
2. **Lazy Loading**: The code could be optimized to use lazy loading for the configuration values, so that they are only loaded when they are needed, rather than loading all the values upfront.

### 5. Suggestions for Improvement

1. **Configuration Validation**: Implement a validation schema for the configuration parameters to ensure that the values are within the expected range and format.
2. **Configuration Reloading**: Provide a mechanism to automatically reload the configuration when the configuration file is updated, to ensure that the application is using the most up-to-date configuration.
3. **Configuration Versioning**: Consider versioning the configuration schema to handle changes in the configuration structure over time.
4. **Configuration Defaults**: Provide a way to set default values for the configuration parameters, either in the code or through a separate configuration file.
5. **Configuration Documentation**: Create detailed documentation for the configuration parameters, including their purpose, expected values, and any dependencies or interactions between them.

Overall, the code for the `BedrockConfig` class has a good foundation, but there are several areas that could be improved to enhance code quality, maintainability, and security.

    
       
    
## Future Scope:
     This assistant can also be modified and can be provided some context based on RAG and data in vector database based on business contex or improving code quality providing customization based on team needs.


## Notes on links and API documentation :
    

   https://community.aws/content/2hUiEkO83hpoGF5nm3FWrdfYvPt/amazon-bedrock-converse-api-java-developer-guide

   https://docs.aws.amazon.com/bedrock/latest/userguide/models-supported.html#model-ids-arns

   https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference-call.html

   https://docs.aws.amazon.com/lambda/latest/dg/urls-invocation.html
     
  

