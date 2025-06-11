package com.example;

import java.util.Objects;
import com.amazonaws.services.lambda.runtime.Context;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

public class ReviewPromt {
	
	private BedrockRuntimeClient bedrockClient;
	private Context context;
	
	
	 public ReviewPromt(BedrockRuntimeClient bedrockClient, Context context) {
	        this.bedrockClient = Objects.requireNonNull(bedrockClient, 
	            "BedrockRuntimeClient cannot be null");
	        this.context = context;
	        
	        context.getLogger().log("ReviewPromt initialized....");
	        
	    }
	 
	 public String createReviewPrompt(String extractedDocument, String kbContext, String lense, String pillar, String questions) {
	        return String.format("""
	            You are an AWS Cloud Solutions Architect who specializes in reviewing solution architecture documents against the AWS Well-Architected Framework.
    The process consists of evaluating the provided solution architecture document against the 6 pillars of the specified AWS Well-Architected Framework lens, namely:
        Cost Optimization Pillar, Please review following information text from document: %s and based on lense: %s with pillar : %s and please include following context: %s and please answer following related question: %s
               """, extractedDocument, lense, pillar,kbContext,questions);
	        
	    }
	 
	 public String invokeBedrockModel(String prompt) throws Exception {	
		 
		 context.getLogger().log("ReviewPromt invoking bedrock claud for review....");
	    	
	    	Message message = Message.builder()
	                .content(ContentBlock.fromText(prompt))
	                .role(ConversationRole.USER)
	                .build();
	    	
	    	
	    	ConverseResponse response = bedrockClient.converse(
	                request -> request.modelId("anthropic.claude-3-5-sonnet-20240620-v1:0").messages(message)
	        );
	    	
	        String responseText = response.output().message().content().get(0).text();
	        
	        context.getLogger().log("ReviewPromt review done with text..." + responseText);
	        
	        return responseText;
	    }


}
