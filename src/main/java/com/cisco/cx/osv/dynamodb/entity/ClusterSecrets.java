package com.cisco.cx.osv.dynamodb.entity;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DynamoDBDocument
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterSecrets implements Serializable {
  @DynamoDBAttribute private String host;
  @DynamoDBAttribute private String credential;
}
