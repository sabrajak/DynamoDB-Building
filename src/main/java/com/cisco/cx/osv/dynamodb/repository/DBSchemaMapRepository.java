package com.cisco.cx.osv.dynamodb.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTransactionWriteExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.cisco.cx.osv.dynamodb.entity.DBSchemaMap;
import com.cisco.cx.osv.dynamodb.entity.SchemaStatus;

@Repository
public class DBSchemaMapRepository {

	@Autowired
	private DynamoDBMapper mapper;

	public DBSchemaMap addDBSchemaMap(DBSchemaMap dbSchemaMap) {
		mapper.save(dbSchemaMap);
		return dbSchemaMap;
	}

	public DBSchemaMap findDBSchemaMapByClusterAndSchema(String clusterName, String schemaName) {
		return mapper.load(DBSchemaMap.class, clusterName, schemaName);
	}

	public String deleteDBSchemaMap(DBSchemaMap clusterName) {
		mapper.delete(clusterName);
		return "record removed !!";
	}

	public String updateDBSchemaMap(DBSchemaMap dbSchemaMap) {
		mapper.save(dbSchemaMap, buildExpression(dbSchemaMap));
		return "record updated...";
	}

	private DynamoDBSaveExpression buildExpression(DBSchemaMap dbSchemaMap) {
		DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
		Map<String, ExpectedAttributeValue> expectedMap = new HashMap<>();
		expectedMap.put("clusterName",
				new ExpectedAttributeValue(new AttributeValue().withS(dbSchemaMap.getClusterName())));
		dynamoDBSaveExpression.setExpected(expectedMap);
		return dynamoDBSaveExpression;
	}

	public List<DBSchemaMap> scanByClusterName(String clusterName) {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#clusterName", "clusterName");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":clusterName", new AttributeValue().withS(clusterName));

		DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
				.withFilterExpression("#clusterName = :clusterName").withExpressionAttributeNames(attributeNames)
				.withExpressionAttributeValues(attributeValues);

		List<DBSchemaMap> result = mapper.scan(DBSchemaMap.class, dynamoDBScanExpression);

		return result;
	}

	public List<DBSchemaMap> scanByCustomerId(String customerId) {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#customerId", "customerId");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":customerId", new AttributeValue().withS(customerId));

		DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
				.withFilterExpression("#customerId = :customerId").withExpressionAttributeNames(attributeNames)
				.withExpressionAttributeValues(attributeValues);

		List<DBSchemaMap> result = mapper.scan(DBSchemaMap.class, dynamoDBScanExpression);

		return result;
	}
	TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
	
	public String lock(DBSchemaMap dbSchemaMap) throws InterruptedException {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#status", "status");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":status", new AttributeValue().withS("AVAILABLE"));

		/*
		 * DynamoDBTransactionWriteExpression conditionExpressionForConditionCheck = new
		 * DynamoDBTransactionWriteExpression()
		 * .withConditionExpression("#status = :status").withExpressionAttributeNames(
		 * attributeNames) .withExpressionAttributeValues(attributeValues);
		 */

		/*
		 * TransactionWriteRequest transactionWriteRequest = new
		 * TransactionWriteRequest(); transactionWriteRequest.addPut(dbSchemaMap,
		 * conditionExpressionForConditionCheck);
		 */

		
		//TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
		transactionWriteRequest.addPut(dbSchemaMap);
		
		Thread.sleep(30000);
		mapper.transactionWrite(transactionWriteRequest);
		return "String updated!!";

	}
	
	public String lock2(DBSchemaMap dbSchemaMap) throws InterruptedException {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#status", "status");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":status", new AttributeValue().withS("AVAILABLE"));

		//DynamoDBTransactionWriteExpression conditionExpressionForConditionCheck = new DynamoDBTransactionWriteExpression();
				//.withConditionExpression("#status = :status").withExpressionAttributeNames(attributeNames)
				//.withExpressionAttributeValues(attributeValues);

		//TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
		transactionWriteRequest.addPut(dbSchemaMap);
		
		//mapper.transactionWrite(transactionWriteRequest);
		
		transactionWriteRequest.addPut(new DBSchemaMap("a", "b", SchemaStatus.AVAILABLE, null, null));
		
		mapper.transactionWrite(transactionWriteRequest);
		
		return "String updated!!";

	}
	
}
