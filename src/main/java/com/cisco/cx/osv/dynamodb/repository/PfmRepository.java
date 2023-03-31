package com.cisco.cx.osv.dynamodb.repository;

import java.util.HashMap;
import java.util.Map;

import com.cisco.cx.osv.dynamodb.entity.SWRecommendationMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.cisco.cx.osv.dynamodb.entity.Cluster;

@Repository
public class PfmRepository {

	@Autowired
	private DynamoDBMapper mapper;

	public SWRecommendationMetrics addcluster(SWRecommendationMetrics pfm) {
		mapper.save(pfm);
		return pfm;
	}



	private DynamoDBSaveExpression buildExpression(SWRecommendationMetrics pfm) {
		DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
		Map<String, ExpectedAttributeValue> expectedMap = new HashMap<>();
		expectedMap.put("date", new ExpectedAttributeValue(new AttributeValue().withS(pfm.getDate())));
		dynamoDBSaveExpression.setExpected(expectedMap);
		return dynamoDBSaveExpression;
	}

}
