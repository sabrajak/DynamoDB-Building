package com.cisco.cx.osv.dynamodb;

import java.io.IOException;
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
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class OsvDynamodbService3Application {

	private static Gson gson = new Gson();

	public static void main(String[] args) {

		SpringApplication.run(OsvDynamodbService3Application.class, args);

		final AmazonDynamoDB amazonDynamoDBClient = getAmazonDynamoDBClient();

		List<CreateTableRequest> requestList = new ArrayList();
		requestList.add(getCreateTableRequest_osv_database_cluster());
		requestList.add(getCreateTableRequest_osv_database_schema_map());
		requestList.add(osv_audit_new());

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

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("clusterName").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("defaultCluster").withAttributeType("N"));

		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
		keySchema.add(new KeySchemaElement().withAttributeName("clusterName").withKeyType(KeyType.HASH));

		// GSI for isdefault
		GlobalSecondaryIndex defaultGSI = new GlobalSecondaryIndex().withIndexName("defaultClusterGSI")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> defaultGSIKeySchema = new ArrayList<KeySchemaElement>();
		defaultGSIKeySchema.add(new KeySchemaElement().withAttributeName("defaultCluster").withKeyType(KeyType.HASH));

		defaultGSI.setKeySchema(defaultGSIKeySchema);

		return new CreateTableRequest().withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
				.withGlobalSecondaryIndexes(defaultGSI)
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withTableName("osv_database_cluster");
	}

	private static CreateTableRequest getCreateTableRequest_osv_database_schema_map() {

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("clusterName").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("schemaName").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("status").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("tenantId").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("tenancy").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("schemaType").withAttributeType("S"));

		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
		keySchema.add(new KeySchemaElement().withAttributeName("clusterName").withKeyType(KeyType.HASH));
		keySchema.add(new KeySchemaElement().withAttributeName("schemaName").withKeyType(KeyType.RANGE));

		// LSI for statusLSI
		LocalSecondaryIndex statusLSI = new LocalSecondaryIndex().withIndexName("statusLSI")
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> statusLSIKeySchema = new ArrayList<KeySchemaElement>();
		statusLSIKeySchema.add(new KeySchemaElement().withAttributeName("clusterName").withKeyType(KeyType.HASH));
		statusLSIKeySchema.add(new KeySchemaElement().withAttributeName("status").withKeyType(KeyType.RANGE));

		statusLSI.setKeySchema(statusLSIKeySchema);

		// LSI for tenantIdLSI
		LocalSecondaryIndex customerIdLSI = new LocalSecondaryIndex().withIndexName("tenantIdLSI")
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> customerIdLSIKeySchema = new ArrayList<KeySchemaElement>();
		customerIdLSIKeySchema.add(new KeySchemaElement().withAttributeName("clusterName").withKeyType(KeyType.HASH));
		customerIdLSIKeySchema.add(new KeySchemaElement().withAttributeName("tenantId").withKeyType(KeyType.RANGE));

		customerIdLSI.setKeySchema(customerIdLSIKeySchema);

		// LSI for tenancyLSI
		LocalSecondaryIndex tenancyLSI = new LocalSecondaryIndex().withIndexName("tenancyLSI")
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> tenancyLSIKeySchema = new ArrayList<KeySchemaElement>();
		tenancyLSIKeySchema.add(new KeySchemaElement().withAttributeName("clusterName").withKeyType(KeyType.HASH));
		tenancyLSIKeySchema.add(new KeySchemaElement().withAttributeName("tenancy").withKeyType(KeyType.RANGE));

        tenancyLSI.setKeySchema(tenancyLSIKeySchema);

		// LSI for schemaTypeLSI
		LocalSecondaryIndex schemaTypeLSI = new LocalSecondaryIndex().withIndexName("schemaTypeLSI")
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> schemaTypeLSIKeySchema = new ArrayList<KeySchemaElement>();
		schemaTypeLSIKeySchema.add(new KeySchemaElement().withAttributeName("clusterName").withKeyType(KeyType.HASH));
		schemaTypeLSIKeySchema.add(new KeySchemaElement().withAttributeName("schemaType").withKeyType(KeyType.RANGE));

		schemaTypeLSI.setKeySchema(schemaTypeLSIKeySchema);

		return new CreateTableRequest().withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
				.withLocalSecondaryIndexes(statusLSI, customerIdLSI,tenancyLSI,schemaTypeLSI)
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withTableName("osv_database_schema");
	}
	private static CreateTableRequest osv_audit() {

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("requestId").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("auditTimestamp").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("datetime").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("requestType").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("requestStatus").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("referenceId").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("requestDepth").withAttributeType("N"));
		attributeDefinitions
				.add(new AttributeDefinition().withAttributeName("requestStatusType").withAttributeType("S"));

		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
		keySchema.add(new KeySchemaElement().withAttributeName("requestId").withKeyType(KeyType.HASH));
		keySchema.add(new KeySchemaElement().withAttributeName("auditTimestamp").withKeyType(KeyType.RANGE));

		// GSI for parent_request-by-datetime
		GlobalSecondaryIndex mainRequestGSI = new GlobalSecondaryIndex().withIndexName("parent_request-by-datetime")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> mainRequestGSIKeySchema = new ArrayList<KeySchemaElement>();
		mainRequestGSIKeySchema.add(new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.HASH));
		mainRequestGSIKeySchema.add(new KeySchemaElement().withAttributeName("datetime").withKeyType(KeyType.RANGE));
		mainRequestGSI.setKeySchema(mainRequestGSIKeySchema);

		// GSI for parent_request-by-request_status
		GlobalSecondaryIndex mainRequestStatusGSI = new GlobalSecondaryIndex()
				.withIndexName("parent_request-by-request_status")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> mainRequestStatusGSIKeySchema = new ArrayList<KeySchemaElement>();
		mainRequestStatusGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.HASH));
		mainRequestStatusGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("requestStatus").withKeyType(KeyType.RANGE));
		mainRequestStatusGSI.setKeySchema(mainRequestStatusGSIKeySchema);

		// GSI for request_status-by-datetime

		GlobalSecondaryIndex RequestStatusGSI = new GlobalSecondaryIndex().withIndexName("request_status-by-datetime")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> RequestStatusGSIKeySchema = new ArrayList<KeySchemaElement>();
		RequestStatusGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("requestStatus").withKeyType(KeyType.HASH));
		RequestStatusGSIKeySchema.add(new KeySchemaElement().withAttributeName("datetime").withKeyType(KeyType.RANGE));
		RequestStatusGSI.setKeySchema(RequestStatusGSIKeySchema);

		// GSI for request_type-by-datetime

		GlobalSecondaryIndex RequestTypeGSI = new GlobalSecondaryIndex().withIndexName("request_type-by-datetime")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> RequestTypeGSIKeySchema = new ArrayList<KeySchemaElement>();
		RequestTypeGSIKeySchema.add(new KeySchemaElement().withAttributeName("requestType").withKeyType(KeyType.HASH));
		RequestTypeGSIKeySchema.add(new KeySchemaElement().withAttributeName("datetime").withKeyType(KeyType.RANGE));
		RequestTypeGSI.setKeySchema(RequestTypeGSIKeySchema);

		// GSI for parent_request-by-timestamp
		GlobalSecondaryIndex RequestTimestampGSI = new GlobalSecondaryIndex()
				.withIndexName("parent_request-by-timestamp")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> RequestTimestampGSIKeySchema = new ArrayList<KeySchemaElement>();
		RequestTimestampGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.HASH));
		RequestTimestampGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("auditTimestamp").withKeyType(KeyType.RANGE));
		RequestTimestampGSI.setKeySchema(RequestTimestampGSIKeySchema);

		// GSI for main_request-by-timestamp
		GlobalSecondaryIndex mainRequestTimestampGSI = new GlobalSecondaryIndex()
				.withIndexName("main_request-by-timestamp")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> mainRequestTimestampGSIKeySchema = new ArrayList<KeySchemaElement>();
		mainRequestTimestampGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("requestDepth").withKeyType(KeyType.HASH));
		mainRequestTimestampGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("auditTimestamp").withKeyType(KeyType.RANGE));
		mainRequestTimestampGSI.setKeySchema(mainRequestTimestampGSIKeySchema);

		// GSI for parent_request-by-status_type
		GlobalSecondaryIndex GSI_1 = new GlobalSecondaryIndex().withIndexName("parent_request-by-status_type")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> GSI_1_KeySchema = new ArrayList<KeySchemaElement>();
		GSI_1_KeySchema.add(new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.HASH));
		GSI_1_KeySchema.add(new KeySchemaElement().withAttributeName("requestStatusType").withKeyType(KeyType.RANGE));

		GSI_1.setKeySchema(GSI_1_KeySchema);

		return new CreateTableRequest().withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)

				.withGlobalSecondaryIndexes(mainRequestGSI, mainRequestStatusGSI, RequestStatusGSI, RequestTypeGSI,
						RequestTimestampGSI, mainRequestTimestampGSI, GSI_1)

				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withTableName("osv_audit");
	}

	private static CreateTableRequest osv_audit_new() {

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("requestId").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("auditTimestamp").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("referenceId").withAttributeType("S"));
		attributeDefinitions
				.add(new AttributeDefinition().withAttributeName("requestStatusType").withAttributeType("S"));

		ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
		keySchema.add(new KeySchemaElement().withAttributeName("requestId").withKeyType(KeyType.HASH));
		keySchema.add(new KeySchemaElement().withAttributeName("auditTimestamp").withKeyType(KeyType.RANGE));

		// GSI for parent_request-by-timestamp
		GlobalSecondaryIndex RequestTimestampGSI = new GlobalSecondaryIndex()
				.withIndexName("parentRequestTimestampGSI")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> RequestTimestampGSIKeySchema = new ArrayList<KeySchemaElement>();
		RequestTimestampGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.HASH));
		RequestTimestampGSIKeySchema
				.add(new KeySchemaElement().withAttributeName("auditTimestamp").withKeyType(KeyType.RANGE));
		RequestTimestampGSI.setKeySchema(RequestTimestampGSIKeySchema);

		// GSI for parent_request-by-status_type
		GlobalSecondaryIndex GSI_1 = new GlobalSecondaryIndex().withIndexName("parentRequestStatusTypeGSI")
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withProjection(new Projection().withProjectionType(ProjectionType.ALL));

		ArrayList<KeySchemaElement> GSI_1_KeySchema = new ArrayList<KeySchemaElement>();
		GSI_1_KeySchema.add(new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.HASH));
		GSI_1_KeySchema.add(new KeySchemaElement().withAttributeName("requestStatusType").withKeyType(KeyType.RANGE));

		GSI_1.setKeySchema(GSI_1_KeySchema);

		return new CreateTableRequest().withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)

				.withGlobalSecondaryIndexes(RequestTimestampGSI, GSI_1)

				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withTableName("osv_audit");
	}

	/*private static JsonNode getSecrets() {
		String secretName = "OSV_DB_CREDENTIALS_SECRET_NAME1";
		String endpoints = "http://localhost:4566";
		String AWSRegion = Regions.US_WEST_2.getName();
		AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoints,
				AWSRegion);
		AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
		clientBuilder.setEndpointConfiguration(config);
		AWSSecretsManager client = clientBuilder.build();

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode secretsJson = null;

		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);

		GetSecretValueResult getSecretValueResponse = null;

		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
		}

		catch (ResourceNotFoundException e) {
			log.error("The requested secret " + secretName + " was not found");
		}

		catch (InvalidRequestException e) {
			log.error("The request was invalid due to: " + e.getMessage());
		}

		catch (InvalidParameterException e) {
			log.error("The request had invalid params: " + e.getMessage());
		}
		if (getSecretValueResponse == null) {
			return null;
		} // Decrypted secret using the associated KMS key // Depending on whether the
			// secret was a string or binary, one of these fields will be populated

		String secret = getSecretValueResponse.getSecretString();

		if (secret != null) {
			try {
				secretsJson = objectMapper.readTree(secret);
			}

			catch (IOException e) {
				log.error("Exception while retrieving secret values: " + e.getMessage());
			}
		}

		else {
			log.error("The Secret String returned is null");

			return null;

		}
		String dbname = secretsJson.get("dbname").textValue();
		String username = secretsJson.get("username").textValue();
		String password = secretsJson.get("password").textValue();

		log.info("dbname {}", dbname);
		log.info("username {}", username);
		log.info("password {}", password);

		return secretsJson;

	}

	private static JsonNode getSecretsTest() {
		String secretName = "OSV_DB_CREDENTIALS_SECRET_NAME1";
		// String endpoints = "http://localhost:4566";
		String AWSRegion = Regions.US_WEST_2.getName();
		// AwsClientBuilder.EndpointConfiguration config = new
		// AwsClientBuilder.EndpointConfiguration(endpoints,
		// AWSRegion);
		AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
		// clientBuilder.setEndpointConfiguration(config);
		AWSSecretsManager client = clientBuilder.build();

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode secretsJson = null;

		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);

		GetSecretValueResult getSecretValueResponse = null;

		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
		}

		catch (ResourceNotFoundException e) {
			log.error("The requested secret " + secretName + " was not found");
		}

		catch (InvalidRequestException e) {
			log.error("The request was invalid due to: " + e.getMessage());
		}

		catch (InvalidParameterException e) {
			log.error("The request had invalid params: " + e.getMessage());
		}
		if (getSecretValueResponse == null) {
			return null;
		} // Decrypted secret using the associated KMS key // Depending on whether the
			// secret was a string or binary, one of these fields will be populated

		String secret = getSecretValueResponse.getSecretString();

		if (secret != null) {
			try {
				secretsJson = objectMapper.readTree(secret);
			}

			catch (IOException e) {
				log.error("Exception while retrieving secret values: " + e.getMessage());
			}
		}

		else {
			log.error("The Secret String returned is null");

			return null;

		}
		String dbname = secretsJson.get("dbname").textValue();
		String username = secretsJson.get("username").textValue();
		String password = secretsJson.get("password").textValue();

		log.info("dbname {}", dbname);
		log.info("username {}", username);
		log.info("password {}", password);

		return secretsJson;

	}*/

}