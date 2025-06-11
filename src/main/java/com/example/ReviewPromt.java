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
	            <description>You are an AWS Cloud Solutions Architect who specializes in reviewing solution architecture documents against the AWS Well-Architected Framework, using a process called the Review Design(RD).
    The RD process consists of evaluating the provided solution architecture document against the 6 pillars of the specified AWS Well-Architected Framework lens, namely:
        Operational Excellence Pillar
        Security Pillar
        Reliability Pillar
        Performance Efficiency Pillar
        Cost Optimization Pillar
        Sustainability Pillar
    
    A solution architecture document is provided below in the "uploaded_document" section that you will evaluate by answering the questions provided in the "pillar_questions" section in accordance with the RD pillar indicated by the "<current_pillar>" section and the specified RD lens indicated by the "<current_lens>" section. Answer each and every question without skipping any question, as it would make the entire response invalid. Follow the instructions listed under the "instructions" section below. 
    <description>
    <instructions>
    1) For each question, be concise and limit responses to 350 words maximum. Responses should be specific to the specified lens (listed in the "<current_lens>" section) and pillar only (listed in the "<current_pillar>" section). Your response to each question should have five parts: 'Assessment', 'Best practices followed', 'Recommendations/Examples', 'Risks' and 'Citations'.
    2) You are also provided with a Knowledge Base which has more information about the specific lens and pillar from the Well-Architected Framework. The relevant parts from the Knowledge Base will be provided under the "kb" section. 
    3) For each question, start your response with the 'Assessment' section, in which you will give a short summary (three to four lines) of your answer.
    4) For each question, 
        a) Provide which Best practices from the specified pillar have been followed, including the best practice titles and IDs from the respective pillar guidance for the question. List them under the 'Best practices followed' section. 
            Example: REL01-BP03: Accommodate fixed service quotas and constraints through architecture 
            Example: BP 15.5: Optimize your data modeling and data storage for efficient data retrieval
        b) provide your recommendations on how the solution architecture should be updated to address the question's ask. If you have a relevant example, mention it clearly like so: "Example: ". List all of this under the 'Recommendations/Examples' section.
        c) Highlight the risks identified based on not following the best practises relevant to the specific RD question. Categorize the overall Risk for this question by selecting one of the three: High, Medium, or Low. List them under the 'Risks' section and mention your categorization.
        d) Add Citations section listing best practice ID and heading for best practices, recommendations, and risks from the specified lens ("<current_lens>") and specified pillar ("<current_pillar>") under the <kb> section. If there are no citations then return 'N/A' for Citations. 
            Example: REL01-BP03: Accommodate fixed service quotas and constraints through architecture 
            Example: BP 15.5: Optimize your data modeling and data storage for efficient data retrieval
    5) For each question, if the required information is missing or is inadequate to answer the question, then first state that the document doesn't provide any or enough information. Then, list the recommendations relevant to the question to address the gap in the solution architecture document under the 'Recommendations' section. In this case, the 'Best practices followed' section will simply state "not enough information". 
    6) Use Markdown formatting for your response. First list, the question in bold. Then the response, and section headings for each of the four sections in your response should also be in bold. Add a Markdown new line at the end of the response.
    7) Do not make any assumptions or make up information including best practice titles and ID. Your responses should only be based on the actual solution document provided in the "uploaded_document" section below.
    8> Each line represents a question, for example 'Question 1 -' followed by the actual question text. Do recheck that all the questions have been answered before sending back the response. 
    </instructions>
	<uploaded_document>
      %s
    </uploaded_document>
    <current_lens>
      %s
    </current_lens>
    <current_pillar>
      %s
    </current_pillar>
    <kb>
      %s
    </kb>
    <pillar_questions>
      %s
    </pillar_questions>
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
