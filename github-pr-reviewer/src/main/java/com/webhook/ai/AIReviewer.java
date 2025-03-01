package com.webhook.ai;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.HashMap;
import java.util.Map;

public class AIReviewer implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private static final BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
//            .region(Region.US_EAST_1)
//            .build();
    
    
    private static final BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.US_EAST_1)
            .build();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        
//        System.setProperty("aws.accessKeyId", "AKIAVIOZFNQOGTU5XZVG");
//        System.setProperty("aws.secretAccessKey", "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");
        
        try {
            // Parse the GitHub webhook payload
            JsonNode payload = objectMapper.readTree(input.getBody());
            
//            // Verify it's a pull request event
//            if (!"pull_request".equals(payload.path("event").asText())) {
//                return createResponse(400, "Not a pull request event");
//            }

            // Extract PR details
            String prTitle = payload.path("pull_request").path("title").asText();
            String prBody = payload.path("pull_request").path("body").asText();
            String diffUrl = payload.path("pull_request").path("diff_url").asText();

            // Create prompt for Claude
            String prompt = createReviewPrompt(prTitle, prBody, diffUrl);

            // Call Bedrock
            String review = invokeBedrockModel(prompt);

            // Create success response
            return createResponse(200, review);

        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createResponse(500, "Error processing request: " + e.getMessage());
        }
    }

    private String createReviewPrompt(String title, String body, String diffUrl) {
        return String.format("""
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
    }

    private String invokeBedrockModel(String prompt) throws Exception {
        // Prepare the request body for Claude
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("prompt", prompt);
//        requestBody.put("max_tokens_to_sample", 2048);
//        requestBody.put("temperature", 0.7);
//        
//        // Create the Bedrock request
//        InvokeModelRequest request = InvokeModelRequest.builder()
//                .modelId("anthropic.claude-3-haiku-20240307-v1:0")
//                .contentType("application/json")
//                .body(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(requestBody)))
//                .build();
//
//        // Invoke the model
//        InvokeModelResponse response = bedrockClient.invokeModel(request);
    	
    	Message message = Message.builder()
                .content(ContentBlock.fromText(prompt))
                .role(ConversationRole.USER)
                .build();
    	
    	
    	ConverseResponse response = bedrockClient.converse(
                request -> request.modelId("anthropic.claude-3-haiku-20240307-v1:0").messages(message)
        );
    	
        String responseText = response.output().message().content().get(0).text();
        
        return responseText;
        
        // Parse and return the response
//        JsonNode responseJson = objectMapper.readTree(response.body().asByteArray());
//        return responseJson.path("completion").asText();
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        
        // Add CORS headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        response.setHeaders(headers);
        
        return response;
    }
}