package com.vanigaranaya.rajawsproj.ddb;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;

import java.util.List;

public class HelloDDB {
    static DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.US_EAST_1).build();
    public static void main(String[] args) {
        ListTablesResponse ltr = dynamoDbClient.listTables();
        for (String tableName : ltr.tableNames()) {
            System.out.println(tableName);
        }
        describeTable();
        System.out.println("hello ddb test");
    }

    private static void describeTable() {
        DescribeTableRequest describeTableRequest = DescribeTableRequest.builder().tableName("rajsample").build();
        try{
            DescribeTableResponse describeTableResponse= dynamoDbClient.describeTable(describeTableRequest);
            TableDescription tableInfo = describeTableResponse.table();
            if (tableInfo != null) {
                System.out.format("Table name  : %s\n", tableInfo.tableName());
                System.out.format("Table ARN   : %s\n", tableInfo.tableArn());
                System.out.format("Status      : %s\n", tableInfo.tableStatus());
                System.out.format("Item count  : %d\n", tableInfo.itemCount());
                System.out.format("Size (bytes): %d\n", tableInfo.tableSizeBytes());

                ProvisionedThroughputDescription throughputInfo = tableInfo.provisionedThroughput();
                System.out.println("Throughput");
                System.out.format("  Read Capacity : %d\n", throughputInfo.readCapacityUnits());
                System.out.format("  Write Capacity: %d\n", throughputInfo.writeCapacityUnits());

                List<AttributeDefinition> attributes = tableInfo.attributeDefinitions();
                System.out.println("Attributes");
                for (AttributeDefinition a : attributes) {
                    System.out.format("  %s (%s)\n", a.attributeName(), a.attributeType());
                }
            }

        } catch (DynamoDbException e) {
            System.err.println("error.. "+ e.getMessage());
            System.exit(1);
        }
    }
}
