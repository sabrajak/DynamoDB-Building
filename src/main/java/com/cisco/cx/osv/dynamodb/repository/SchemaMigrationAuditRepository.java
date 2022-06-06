package com.cisco.cx.osv.dynamodb.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.cisco.cx.osv.dynamodb.entity.LogLevels;
import com.cisco.cx.osv.dynamodb.entity.SchemaMigrationAudit;

@Repository
public class SchemaMigrationAuditRepository {

	@Autowired
	private DynamoDBMapper mapper;

	public SchemaMigrationAudit addSchemaMigrationAudit(SchemaMigrationAudit schemaMigrationAudit) {
		mapper.save(schemaMigrationAudit);
		return schemaMigrationAudit;
	}

	public SchemaMigrationAudit findSchemaMigrationAuditByAuditDate(String audit_date, String audit_timestamp) {
		return mapper.load(SchemaMigrationAudit.class, audit_date, audit_timestamp);
	}

	public String deleteSchemaMigrationAudit(SchemaMigrationAudit schemaMigrationAudit) {
		mapper.delete(schemaMigrationAudit);
		return "record removed !!";
	}

	public String updateSchemaMigrationAudit(SchemaMigrationAudit schemaMigrationAudit) {
		mapper.save(schemaMigrationAudit, buildExpression(schemaMigrationAudit));
		return "record updated...";
	}

	private DynamoDBSaveExpression buildExpression(SchemaMigrationAudit schemaMigrationAudit) {
		DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
		Map<String, ExpectedAttributeValue> expectedMap = new HashMap<>();
		expectedMap.put("audit_date", new ExpectedAttributeValue(
				new AttributeValue().withS(schemaMigrationAudit.getAudit_date().toString())));
		dynamoDBSaveExpression.setExpected(expectedMap);
		return dynamoDBSaveExpression;
	}

	public List<LogLevels> getLogsByReferenceId(String referenceId) {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#referenceId", "reference_id");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":referenceId", new AttributeValue().withS(referenceId));

		DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
				.withFilterExpression("#referenceId = :referenceId").withExpressionAttributeNames(attributeNames)
				.withExpressionAttributeValues(attributeValues);

		List<LogLevels> result = mapper.scan(SchemaMigrationAudit.class, dynamoDBScanExpression).stream()
				.map(i -> i.getLog_level()).collect(Collectors.toList());

		return result;
	}

	public List<SchemaMigrationAudit> getLRecordsByReferenceIdAndLogLevel(String referenceId, String logLevel) {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#referenceId", "reference_id");
		attributeNames.put("#logLevel", "log_level");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":referenceId", new AttributeValue().withS(referenceId));
		attributeValues.put(":logLevel", new AttributeValue().withS(logLevel));

		DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
				.withFilterExpression("#referenceId = :referenceId AND #logLevel = :logLevel")
				.withExpressionAttributeNames(attributeNames).withExpressionAttributeValues(attributeValues);

		List<SchemaMigrationAudit> result = mapper.scan(SchemaMigrationAudit.class, dynamoDBScanExpression);

		return result;
	}

	public List<SchemaMigrationAudit> getByDate(String audit_date) {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#auditDate", "audit_date");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":auditDate", new AttributeValue().withS(audit_date));

		DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
				.withFilterExpression("#auditDate = :auditDate").withExpressionAttributeNames(attributeNames)
				.withExpressionAttributeValues(attributeValues);

		List<SchemaMigrationAudit> result = mapper.scan(SchemaMigrationAudit.class, dynamoDBScanExpression);

		return result;
	}

	public List<SchemaMigrationAudit> getByDateAndTime(String auditDate, String startTimeStamp, String endTimeStamp) {

		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#auditDate", "audit_date");
		attributeNames.put("#auditTimestamp", "audit_timestamp");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":auditDate", new AttributeValue().withS(auditDate));
		attributeValues.put(":startTimeStamp", new AttributeValue().withS(startTimeStamp));
		attributeValues.put(":endTimeStamp", new AttributeValue().withS(endTimeStamp));

		DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
				.withFilterExpression(
						"#auditDate = :auditDate AND #auditTimestamp BETWEEN :startTimeStamp and :endTimeStamp")
				.withExpressionAttributeNames(attributeNames).withExpressionAttributeValues(attributeValues);

		List<SchemaMigrationAudit> result = mapper.scan(SchemaMigrationAudit.class, dynamoDBScanExpression);

		return result;
	}

	public List<SchemaMigrationAudit> getByRangeDateAndTime(String startDate, String endDate, String startTimeStamp,
			String endTimeStamp) {

		if(startTimeStamp == null) {
			startTimeStamp="00:00:00:000";
		}
		if(endTimeStamp == null) {
			endTimeStamp="24:00:00:000";
		}
		
		Map<String, String> attributeNames = new HashMap<String, String>();
		attributeNames.put("#auditDate", "audit_date");
		attributeNames.put("#auditTimestamp", "audit_timestamp");

		Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
		attributeValues.put(":startDate", new AttributeValue().withS(startDate));
		attributeValues.put(":endDate", new AttributeValue().withS(endDate));
		attributeValues.put(":startTimeStamp", new AttributeValue().withS(startTimeStamp));
		attributeValues.put(":endTimeStamp", new AttributeValue().withS(endTimeStamp));

		DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression().withFilterExpression(
				"#auditDate BETWEEN :startDate AND :endDate AND #auditTimestamp BETWEEN :startTimeStamp and :endTimeStamp")
				.withExpressionAttributeNames(attributeNames).withExpressionAttributeValues(attributeValues);

		List<SchemaMigrationAudit> result = mapper.scan(SchemaMigrationAudit.class, dynamoDBScanExpression);

		return result;
	}

}
