package com.cisco.cx.osv.dynamodb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

@SpringBootApplication
public class OsvDynamodbService3Application {

	public static void main(String[] args) {

		SpringApplication.run(OsvDynamodbService3Application.class, args);

		final AmazonDynamoDB amazonDynamoDBClient = getAmazonDynamoDBClient();

		List<CreateTableRequest> requestList = new ArrayList();
		requestList.add(getCreateTableRequest_osv_database_cluster());
		requestList.add(getCreateTableRequest_osv_database_schema_map());
		requestList.add(getCreateTableRequest_osv_schema_migration_audit());

		for (CreateTableRequest request : requestList) {
			try {
				CreateTableResult result = amazonDynamoDBClient.createTable(request);
				System.out.println(result.getTableDescription().getTableName());
			} catch (AmazonServiceException e) {
				System.err.println(e.getErrorMessage());
			}
		}

	}

	private static AmazonDynamoDB getAmazonDynamoDBClient() {
		// Create endpoint configuration which points to the Edge service (running on
		// http://localhost:4566)
		AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(
				"http://localhost:4566", Regions.US_WEST_2.getName());

		return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(endpointConfig).build();
	}

	private static CreateTableRequest getCreateTableRequest_osv_database_cluster() {
		return new CreateTableRequest()
				.withAttributeDefinitions(new AttributeDefinition("clusterName", ScalarAttributeType.S))
				.withKeySchema(new KeySchemaElement("clusterName", KeyType.HASH))
				.withProvisionedThroughput(new ProvisionedThroughput(10L, 10L)).withTableName("osv_database_cluster");
	}

	private static CreateTableRequest getCreateTableRequest_osv_database_schema_map() {

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("clusterName").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("schemaName").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("status").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("customerId").withAttributeType("S"));

		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
		keySchema.add(new KeySchemaElement().withAttributeName("clusterName").withKeyType(KeyType.HASH));
		keySchema.add(new KeySchemaElement().withAttributeName("schemaName").withKeyType(KeyType.RANGE));

		// GSI for status
		GlobalSecondaryIndex statusGSI = new GlobalSecondaryIndex().withIndexName("statusGSI")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> statusGSIKeySchema = new ArrayList<KeySchemaElement>();
		statusGSIKeySchema.add(new KeySchemaElement().withAttributeName("status").withKeyType(KeyType.HASH));
		statusGSIKeySchema.add(new KeySchemaElement().withAttributeName("schemaName").withKeyType(KeyType.RANGE));

		statusGSI.setKeySchema(statusGSIKeySchema);

		// GSI for customer_id
		GlobalSecondaryIndex customerIdGSI = new GlobalSecondaryIndex().withIndexName("customerIdGSI")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> customerIdGSIKeySchema = new ArrayList<KeySchemaElement>();
		customerIdGSIKeySchema.add(new KeySchemaElement().withAttributeName("customerId").withKeyType(KeyType.HASH));
		customerIdGSIKeySchema.add(new KeySchemaElement().withAttributeName("schemaName").withKeyType(KeyType.RANGE));
		
		customerIdGSI.setKeySchema(customerIdGSIKeySchema);

		return new CreateTableRequest().withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
				.withGlobalSecondaryIndexes(statusGSI, customerIdGSI)
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withTableName("osv_database_schema_map");
	}

	private static CreateTableRequest getCreateTableRequest_osv_schema_migration_audit() {

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("audit_date").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("audit_timestamp").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("reference_id").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("log_level").withAttributeType("S"));
		
		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
		keySchema.add(new KeySchemaElement().withAttributeName("audit_date").withKeyType(KeyType.HASH));
		keySchema.add(new KeySchemaElement().withAttributeName("audit_timestamp").withKeyType(KeyType.RANGE));

		//GSI for reference_id
		GlobalSecondaryIndex referenceIdGSI = new GlobalSecondaryIndex().withIndexName("referenceIdGSI")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> referenceIdGSIKeySchema = new ArrayList<KeySchemaElement>();
		referenceIdGSIKeySchema.add(new KeySchemaElement().withAttributeName("reference_id").withKeyType(KeyType.HASH));
		referenceIdGSIKeySchema.add(new KeySchemaElement().withAttributeName("log_level").withKeyType(KeyType.RANGE));

		referenceIdGSI.setKeySchema(referenceIdGSIKeySchema);

		return new CreateTableRequest().withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
				.withGlobalSecondaryIndexes(referenceIdGSI)
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withTableName("osv_schema_migration_audit");
	}

}