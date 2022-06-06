package com.cisco.cx.osv.dynamodb.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.cisco.cx.osv.dynamodb.entity.Cluster;

@Repository
public class ClusterRepository {

	@Autowired
	private DynamoDBMapper mapper;

	public Cluster addcluster(Cluster cluster) {
		mapper.save(cluster);
		return cluster;
	}

	public Cluster findClusterByClusterName(String clusterName) {
		return mapper.load(Cluster.class, clusterName);
	}

	public String deleteCluster(Cluster clusterName) {
		mapper.delete(clusterName);
		return "record removed !!";
	}

	public String updateCluster(Cluster cluster) {
		mapper.save(cluster, buildExpression(cluster));
		return "record updated...";
	}

	private DynamoDBSaveExpression buildExpression(Cluster cluster) {
		DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
		Map<String, ExpectedAttributeValue> expectedMap = new HashMap<>();
		expectedMap.put("clusterName", new ExpectedAttributeValue(new AttributeValue().withS(cluster.getClusterName())));
		dynamoDBSaveExpression.setExpected(expectedMap);
		return dynamoDBSaveExpression;
	}

}
