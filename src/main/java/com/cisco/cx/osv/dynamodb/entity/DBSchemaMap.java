package com.cisco.cx.osv.dynamodb.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "osv_database_schema_map")
public class DBSchemaMap {

	@DynamoDBHashKey(attributeName = "clusterName")
	private String clusterName;

	@DynamoDBRangeKey(attributeName = "schemaName")
	@DynamoDBAttribute
	@CustomGeneratedKey(prefix="TS_") 
	@DynamoDBIndexRangeKey(globalSecondaryIndexNames = { "statusGSI", "customerIdGSI" })
	private String schemaName;
	
	@DynamoDBAttribute
	@DynamoDBTypeConvertedEnum
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "statusGSI")
	private SchemaStatus status;

	@DynamoDBAttribute
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "customerIdGSI")
	private String customerId;
	
	@DynamoDBVersionAttribute
	 private Long version;
}
