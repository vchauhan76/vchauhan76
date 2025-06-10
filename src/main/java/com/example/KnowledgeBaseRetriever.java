package com.example;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseQuery;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalResult;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

public class KnowledgeBaseRetriever {
    private final BedrockAgentRuntimeClient bedrockAgentRuntimeClient;
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseRetriever.class);
    private ObjectMapper requestMapper = new ObjectMapper();

    public KnowledgeBaseRetriever(BedrockAgentRuntimeClient bedrockAgentRuntimeClient) {
        this.bedrockAgentRuntimeClient = Objects.requireNonNull(bedrockAgentRuntimeClient, 
            "BedrockAgentRuntimeClient cannot be null");
    }

    public String retrieve(String questions, String kbId, String lensFilter) {
       
            // Construct the knowledge base prompt
            String kbPrompt = String.format("""
                For each question provide:
                - Recommendations
                - Best practices
                - Examples
                - Risks
                %s""", questions);
            
            // Create the knowledge base query
            KnowledgeBaseQuery kbQuery = KnowledgeBaseQuery.builder()
                .text(kbPrompt)
                .build();
            
         // Initialize the knowledgebase configuration
            KnowledgeBaseVectorSearchConfiguration knowledgeBaseVectorSearchConfiguration = KnowledgeBaseVectorSearchConfiguration.builder()
                    .numberOfResults(10)
                    .build();
            
            KnowledgeBaseRetrievalConfiguration knowledgeBaseRetrievalConfiguration = KnowledgeBaseRetrievalConfiguration.builder()
                    .vectorSearchConfiguration(knowledgeBaseVectorSearchConfiguration)
                    .build();
            
            

            // Create the retrieve request
            RetrieveRequest retrieveRequest = RetrieveRequest.builder()
                .knowledgeBaseId(kbId)
                .retrievalQuery(kbQuery)
                .retrievalConfiguration(knowledgeBaseRetrievalConfiguration)
                .build();

            logger.info("Executing retrieve request for kbId: {}", kbId);
            RetrieveResponse retrieveResponse =  bedrockAgentRuntimeClient.retrieve(retrieveRequest);
            ArrayNode responseNode = requestMapper.createArrayNode();
            
            if(retrieveResponse.hasRetrievalResults()) {
                 responseNode = requestMapper.createArrayNode();
                for(KnowledgeBaseRetrievalResult result: retrieveResponse.retrievalResults()) {
                    responseNode.add(result.content().text());
                    
                }
            }
            
            return responseNode.toString();
   
    }
    
    
  
}