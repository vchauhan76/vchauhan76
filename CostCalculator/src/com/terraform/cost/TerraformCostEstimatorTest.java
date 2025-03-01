//package com.terraform.cost;
//
//import com.terraform.cost.PlanParser;
//import org.junit.Test;
//import static org.junit.Assert.*;
//
//public class TerraformCostEstimatorTest {
//    
//    @Test
//    public void testPlanParsing() {
//        PlanParser parser = new PlanParser();
//        try {
//            var resources = parser.parsePlanFile("src/test/resources/sample-plan.json");
//            assertNotNull(resources);
//            assertFalse(resources.isEmpty());
//        } catch (Exception e) {
//            fail("Should not throw exception: " + e.getMessage());
//        }
//    }
//}