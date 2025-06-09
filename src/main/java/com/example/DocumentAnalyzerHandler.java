package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentAnalyzerHandler implements RequestHandler<S3Event,String> {
    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalyzerHandler.class);
    
    // Initialize AWS clients
    private final S3Client s3Client;
    private final TextractClient textractClient;
    private Context context;

    public DocumentAnalyzerHandler() {
        // Initialize AWS clients with default configuration
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
        this.textractClient = TextractClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        context.getLogger().log("Processing S3 event: " + s3Event);
        this.context = context;
        
        try {
            // Get S3 bucket and key from the event
        	context.getLogger().log("S3 Event that was received debug..:" + s3Event.getRecords());
        	context.getLogger().log("S3 Event that was received Info..:" + s3Event.getRecords());
        	
        	
            String bucket = s3Event.getRecords().get(0).getS3().getBucket().getName();
            String key = s3Event.getRecords().get(0).getS3().getObject().getKey();
            
            context.getLogger().log("S3 Event bucket debug..:" + bucket);
            context.getLogger().log("S3 Event key debug..:" + key);
            

            // Get document from S3
            byte[] documentBytes = getDocumentFromS3(bucket, key);

            // Analyze document with Textract
            return analyzeDocument(documentBytes,bucket, key);

        } catch (Exception e) {
        	context.getLogger().log("Error processing document: ");
            throw new RuntimeException("Error processing document", e);
        }
    }

    private byte[] getDocumentFromS3(String bucket, String key) throws IOException {
        context.getLogger().log("Retrieving document from S3: bucket= "+ bucket + " and key= "+ key);
        
        try {
        	
        	 // First, check if the object exists
            if (!objectExists(bucket, key)) {
            	context.getLogger().log("Object does not exist in S3: bucket="+ bucket + " and key =" + key);
                throw new IOException("S3 object does not exist");
            }
            
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            return objectBytes.asByteArray();
            
        } catch (Exception e) {
            logger.error("Error retrieving document from S3: ", e);
            throw new IOException("Failed to retrieve document from S3", e);
        }
    }
    
 // Helper method to check if object exists
    private boolean objectExists(String bucket, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private String analyzeDocument(byte[] documentBytes, String bucket, String key) {
    	context.getLogger().log("Analyzing document with Textract");
        
        try {
            // Prepare document for Textract
            SdkBytes sourceBytes = SdkBytes.fromByteArray(documentBytes);
            Document document = Document.builder()
                    .bytes(sourceBytes)
                    .build();
            
          return analyzeDocumentAsync(document, bucket, key);

			/*
			 * // Create Textract request DetectDocumentTextRequest request =
			 * DetectDocumentTextRequest.builder() .document(document) .build();
			 * 
			 * // Get Textract response DetectDocumentTextResponse result =
			 * textractClient.detectDocumentText(request);
			 * 
			 * // Process and return results return result.blocks();
			 */

        } catch (TextractException e) {
            logger.error("Error analyzing document with Textract: ", e);
            throw new RuntimeException("Failed to analyze document with Textract", e);
        }
    }
 // For PDF/TIFF documents that require async processing
    private String analyzeDocumentAsync(Document document, String bucket, String key) {
    	
    	 context.getLogger().log("extract_document_text checkpoint 1...");
    	 GetDocumentTextDetectionResponse result;
    	
        StartDocumentTextDetectionRequest startRequest = StartDocumentTextDetectionRequest.builder()
                .documentLocation(DocumentLocation.builder()
                        .s3Object(S3Object.builder()
                                .bucket(bucket)
                                .name(key)
                                .build())
                        .build())
                .build();

        StartDocumentTextDetectionResponse startResponse = 
            textractClient.startDocumentTextDetection(startRequest);
        String jobId = startResponse.jobId();
        
        context.getLogger().log("extract_document_text checkpoint 2...job id:"+ jobId);
        
        
        
        

        // Wait for job completion
		/*
		 * GetDocumentTextDetectionRequest getRequest =
		 * GetDocumentTextDetectionRequest.builder() .jobId(jobId) .build();
		 * 
		 * GetDocumentTextDetectionResponse result; do {
		 * 
		 * result = textractClient.getDocumentTextDetection(getRequest);
		 * if(result.jobStatus() == JobStatus.SUCCEEDED) break; try {
		 * Thread.sleep(1000); } catch (InterruptedException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } // Wait 1 second between checks } while
		 * (result.jobStatus() == JobStatus.IN_PROGRESS);
		 */
        try {
            boolean jobComplete = false;
            
            while (!jobComplete) {
                GetDocumentTextDetectionRequest request = GetDocumentTextDetectionRequest.builder()
                    .jobId(jobId)
                    .build();

                 result = textractClient.getDocumentTextDetection(request);
                String status = result.jobStatus().toString();

                if (status.equals("SUCCEEDED")) {
                    jobComplete = true;
                    context.getLogger().log("Textract job completed successfully");
                } else if (status.equals("FAILED")) {
                    throw new RuntimeException("Textract job failed: " + result.statusMessage());
                } else {
                    // Add delay before next polling attempt
                   // Thread.sleep(1000); // Wait for 1 second before checking again
                }
            }
         /*catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Job polling was interrupted", e);*/
        } catch (TextractException e) {
            throw new RuntimeException("Error checking Textract job status", e);
        }

        context.getLogger().log("extract_document_text checkpoint 3...");
        
     // Collect all pages
        List<GetDocumentTextDetectionResponse> pages = new ArrayList<>();
        String nextToken = null;
        do {
            GetDocumentTextDetectionRequest pageRequest = GetDocumentTextDetectionRequest.builder()
                    .jobId(jobId)
                    .nextToken(nextToken)
                    .build();
            
            result = textractClient.getDocumentTextDetection(pageRequest);
            pages.add(result);
            nextToken = result.nextToken();
        } while (nextToken != null);

        context.getLogger().log("extract_document_text checkpoint 4...");

        // Extract text from all pages
        StringBuilder extractedText = new StringBuilder();
        for (GetDocumentTextDetectionResponse page : pages) {
            page.blocks().stream()
                    .filter(block -> block.blockType() == BlockType.LINE)
                    .forEach(block -> extractedText.append(block.text()).append("\n"));
        }
        
        
        if (result.jobStatus() == JobStatus.SUCCEEDED) {
        	context.getLogger().log("Extraction job succeeded.with....size...: " + result.blocks().size());
        	context.getLogger().log("Extraction text is :  " + extractedText.toString());

            return extractedText.toString();
        } else {
            throw new RuntimeException("Textract job failed: " + result.jobStatus());
        }
    }
    
    
    

}