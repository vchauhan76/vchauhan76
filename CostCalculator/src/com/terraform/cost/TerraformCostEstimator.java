package com.terraform.cost;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.pricing.AWSPricing;
import com.amazonaws.services.pricing.AWSPricingClientBuilder;
import com.amazonaws.services.pricing.model.Filter;
import com.amazonaws.services.pricing.model.GetProductsRequest;
import com.amazonaws.services.pricing.model.GetProductsResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerraformCostEstimator {
    private final AWSPricing pricingClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor initializing AWS Pricing client and JSON object mapper
     */
    public TerraformCostEstimator() {
        this.pricingClient = AWSPricingClientBuilder.standard()
                .withRegion("us-east-1")  // Pricing API is only available in us-east-1
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Main method to process Terraform plan and estimate costs
     * @param planFilePath Path to Terraform plan JSON file
     * @return Map of resource types to their estimated costs
     */
    public Map<String, Double> estimateCosts(String planFilePath) throws IOException {
        JsonNode planJson = objectMapper.readTree(new File(planFilePath));
        Map<String, Double> costEstimates = new HashMap<>();

        // Process planned changes
        JsonNode plannedChanges = planJson.get("planned_values")
                .get("root_module")
                .get("resources");

        for (JsonNode resource : plannedChanges) {
            String resourceType = resource.get("type").asText();
            JsonNode resourceValues = resource.get("values");
            
            double cost = calculateResourceCost(resourceType, resourceValues);
            costEstimates.merge(resourceType, cost, Double::sum);
            //costEstimates.put(resourceType, cost);
        }

        return costEstimates;
    }

    /**
     * Calculate cost for a specific resource
     * @param resourceType AWS resource type
     * @param resourceValues Resource configuration values
     * @return Estimated cost for the resource
     */
    private double calculateResourceCost(String resourceType, JsonNode resourceValues) {
        switch (resourceType) {
            case "aws_instance":
                return calculateEC2Cost(resourceValues);
            case "aws_ebs_volume":
                return calculateEBSCost(resourceValues);
            // Add more resource types as needed
            default:
                return 0.0;
        }
    }

    /**
     * Calculate EC2 instance cost
     * @param resourceValues EC2 instance configuration
     * @return Estimated monthly cost
     */
    private double calculateEC2Cost(JsonNode resourceValues) {
        String instanceType = resourceValues.get("instance_type").asText();
        System.out.println("EC2 block...instance type is " + instanceType );
        //String region = resourceValues.get("region").asText();

        List<String> filters = new ArrayList<>();
        filters.add("ServiceCode=AmazonEC2");
        filters.add("instanceType=" + instanceType);
        filters.add("operatingSystem=Linux");
        filters.add("tenancy=Shared");
        filters.add("capacitystatus=Used");

        
        //TODO: add request object        
        //https://docs.aws.amazon.com/aws-cost-mana	gement/latest/APIReference/Welcome.html#Welcome_AWS_Price_List_Service
        Filter fil = new Filter();
        fil.setType("TERM_MATCH");
        fil.setField("ServiceCode");
        fil.setValue("AmazonEC2");
        
        Filter fil1 = new Filter();
        fil1.setType("TERM_MATCH");
        fil1.setField("instanceType");
        fil1.setValue(instanceType);
        
        Filter fil2 = new Filter();
        fil2.setType("TERM_MATCH");
        fil2.setField("operatingSystem");
        fil2.setValue("Linux");
        
        Filter fil3 = new Filter();
        fil3.setType("TERM_MATCH");
        fil3.setField("tenancy");
        fil3.setValue("Shared");
        
        Filter fil4 = new Filter();
        fil4.setType("TERM_MATCH");
        fil4.setField("capacitystatus");
        fil4.setValue("Used");
           
        List<Filter> listFilter = new ArrayList<>();
        listFilter.add(fil);
        listFilter.add(fil1);
        listFilter.add(fil2);
        listFilter.add(fil3);
        listFilter.add(fil4);
        
        
        GetProductsRequest request = new GetProductsRequest()
                .withServiceCode("AmazonEC2")
				.withFilters(listFilter/* buildFiltersMap(filters) */);
        

        GetProductsResult result = pricingClient.getProducts(request);
        
        
        
        // Parse pricing information from result
        return extractPriceFromResult(result);
    }

    /**
     * Calculate EBS volume cost
     * @param resourceValues EBS volume configuration
     * @return Estimated monthly cost
     */
    private double calculateEBSCost(JsonNode resourceValues) {
        int size = resourceValues.get("size").asInt();
        String volumeType = resourceValues.get("volume_type").asText();

        List<String> filters = new ArrayList<>();
        filters.add("ServiceCode=AmazonEC2");
        filters.add("volumeType=" + volumeType);
        filters.add("productFamily=Storage");
        
        
        Filter fil = new Filter();
        fil.setType("TERM_MATCH");
        fil.setField("ServiceCode");
        fil.setValue("AmazonEC2");
        
        Filter fil1 = new Filter();
        fil1.setType("TERM_MATCH");
        fil1.setField("volumeType");
        fil1.setValue(volumeType);
        
        Filter fil2 = new Filter();
        fil2.setType("TERM_MATCH");
        fil2.setField("productFamily");
        fil2.setValue("Storage");
        
        List<Filter> listFilter = new ArrayList<>();
        listFilter.add(fil);
        listFilter.add(fil1);
        listFilter.add(fil2);
        
      

        GetProductsRequest request = new GetProductsRequest()
                .withServiceCode("AmazonEC2")
				.withFilters( listFilter/* buildFiltersMap(filters) */);

        GetProductsResult result = pricingClient.getProducts(request);
        
        // Calculate based on size and price per GB
        return extractPriceFromResult(result) * size;
    }

    /**
     * Build filters map for AWS Pricing API
     * @param filters List of filter strings
     * @return Map of filters
     */
    private Map<String, List<String>> buildFiltersMap(List<String> filters) {
        Map<String, List<String>> filterMap = new HashMap<>();
        for (String filter : filters) {
            String[] parts = filter.split("=");
            filterMap.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(parts[1]);
        }
        return filterMap;
    }

    /**
     * Extract price information from API result
     * @param result AWS Pricing API result
     * @return Extracted price value
     */
    private double extractPriceFromResult(GetProductsResult result) {
        // Implementation would parse the pricing data from the result
        // This is a simplified version and would need to be expanded based on
        // the actual response format and pricing terms
        try {
            JsonNode priceList = objectMapper.readTree(result.getPriceList().get(0));
            System.out.println("Response price list.." + priceList.toString());
            // Navigate through the price list structure to find the actual price
            // This is a placeholder - actual implementation would depend on the response structure
            return priceList.get("terms")
                    .get("OnDemand")
                    .elements().next()
                    .get("priceDimensions")
                    .elements().next()
                    .get("pricePerUnit")
                    .get("USD")
                    .asDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Example usage
     */
    public static void main(String[] args) {
        try {
//        	System.setProperty("aws.accessKeyId", "AKIAVIOZFNQOCRPUJRX7");
//            System.setProperty("aws.secretKey", "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");
            TerraformCostEstimator estimator = new TerraformCostEstimator();
            Map<String, Double> costs = estimator.estimateCosts("C:\\Users\\vishal\\eclipse-workspace\\CostCalculator\\src\\com\\terraform\\cost\\plan.json");
            
            costs.forEach((resourceType, cost) -> 
                System.out.printf("Resource Type: %s, Estimated Cost: $%.4f%n", 
                    resourceType, cost));
        } catch (IOException e) {
            System.err.println("Error processing Terraform plan: " + e.getMessage());
        }
    }
}