# PR Reviewer Assistant 

## Problem Statement:
   Once developer raises a PR and waits for review comments from other team members there is a wait time since other members are also busy in completing their task and usually review comments happens at end of sprint and developer has to accomodate changes in limited time it also buys in time from fellow members since they might also be busy in their critical task.

## Solution :
     PR Reviewer will target this problem and act as an assistant and as soon as developer raises PR AI Assitant will review the code and will provide review comments immediately saving deveoper time and overall team time for reviewing code and hence enhancing velocity of team.

## Future Scope:
     This assistant can also be modified and can be provided some context based on RAG and data in vector database based on business contex or improving code quality providing customization based on team needs.


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



    ## Notes on links and API documentation :
    

   https://community.aws/content/2hUiEkO83hpoGF5nm3FWrdfYvPt/amazon-bedrock-converse-api-java-developer-guide

   https://docs.aws.amazon.com/bedrock/latest/userguide/models-supported.html#model-ids-arns

   https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference-call.html

   https://docs.aws.amazon.com/lambda/latest/dg/urls-invocation.html
   
   

    

    
     
  

