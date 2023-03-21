package com.cisco.cx.osv.dynamodb.entity;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "osv_database_cluster")
public class Cluster implements Serializable {

  @DynamoDBHashKey private String clusterName;
  @DynamoDBAttribute ClusterSecrets clusterSecrets;
  @DynamoDBAttribute String clusterTag;
  @DynamoDBAttribute private int tenantCapacity;
}
