## Accelerating Architecture Review

### Problem Statement

   - In an organization when there is a huge cloud foot print there is a challenge in adhering to the Well-Architected Framework due to many factors , Difficulty in keeping  pace with the latest best practices, Time-consuming and resource-intensive manual reviews. To address these challenges, We can use generative AI to help streamline and expedite the process. By automating the initial assessment and documentation process. This allows teams to focus more on implementing improvements.


### Solution
 - It can provide faster analysis involving human in loop process, It will provide consistent application of  Well-Architected principles across reviews which leads to more     reliable and standardized evaluations, can handle multiple reviews simultaneously


![image](https://github.com/user-attachments/assets/636ff70b-58f4-427f-9184-685c93b8f59e)


### Current Architecture

- Pre-requisites
  Ensure you have access to the following models in Amazon Bedrock:
    - Titan Text Embeddings V2
    - Claude 3-5 Sonnet
    
- Initially we create a knowledge base in Amazon Bedrock by setting it up with AWS Well architected and Lense documents so that AI can review based on that guidelines.
User will upload their architecture document for review and some additional inputs like based on which Lense or pillar it should be reviewed in S3 bucket.
This will trigger lambda and it will connect with Textract service to extract the contents of document.
Then lambda will connect with knowledge base to get context.

This will be then inputted to Claud model via prompt ie: context from kb, extracted text from uploaded document and any other questions. 
Once done we will get a review suggestions.


### Future Scope
   - It can be further enhanced to have UserInterface to upload the document.
   - Review comments can be uploaded for manual intervention as well.
     
    


### Some important links 
    - Textract https://docs.aws.amazon.com/textract/latest/dg/how-it-works-detecting.html
    - synchrounous api will only be used for single page document for multiple pages use asych api.
      https://docs.aws.amazon.com/textract/latest/dg/sync.html


    - asynch: https://docs.aws.amazon.com/textract/latest/dg/async.html

    - Lambda example:
       https://docs.aws.amazon.com/textract/latest/dg/lambda.html

    - Setup Knowledge Base :
       https://catalog.workshops.aws/amazon-bedrock/en-US/120-rag

    - Model Access :
       https://docs.aws.amazon.com/bedrock/latest/userguide/getting-started.html#getting-started-bedrock-role

    - Block in Textract: 
       https://docs.aws.amazon.com/textract/latest/dg/API_Block.html

    - AWS Well Architected framework and pillars :

     https://aws.amazon.com/architecture/well-architected/?wa-lens-whitepapers.sort-by=item.additionalFields.sortDate&wa-lens-whitepapers.sort-order=desc&wa-guidance-        whitepapers.sort-by=item.additionalFields.sortDate&wa-guidance-whitepapers.sort-order=desc

 
    





