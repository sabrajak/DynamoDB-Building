package com.cisco.cx.osv.dynamodb.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "osv_pfm_recommendation")
public class SWRecommendationMetrics {

    @DynamoDBHashKey
    @DynamoDBAttribute(attributeName = "date") /*partition_key*/
    private String date;

    @DynamoDBRangeKey
    @DynamoDBAttribute(attributeName = "timestamp")
    private Long timestamp;

    /*@DynamoDBAttribute(attributeName = "startTime")
    @DynamoDBTypeConverted(converter = DateTimeTypeConverter.class)
    private OffsetDateTime startTime;*/

    /*@DynamoDBAttribute(attributeName = "endTime")
    @DynamoDBTypeConverted(converter = DateTimeTypeConverter.class)
    private OffsetDateTime endTime;*/

    @DynamoDBAttribute(attributeName = "customerId")
    private String customerId;

    @DynamoDBAttribute(attributeName = "ExecutionTime")
    private int executionTime;

    @DynamoDBAttribute(attributeName = "ExecutionTime_advisory")
    private int executionTimeAdvisory;

    @DynamoDBAttribute(attributeName = "ExecutionTime_bug")
    private int executionTimeBug;

    @DynamoDBAttribute(attributeName = "ExecutionTime_etl")
    private int executionTimeETL;

    @DynamoDBAttribute(attributeName = "ExecutionTime_fieldNotice")
    private int executionTimeFieldNotice;

    @DynamoDBAttribute(attributeName = "ExecutionTime_recommendation")
    private int executionTimeRecommendation;

    @DynamoDBAttribute(attributeName = "ExecutionTime_release")
    private int executionTimeRelease;

    @DynamoDBAttribute(attributeName = "mgmtSystemId")
    private String mgmtSystemId;

    @DynamoDBAttribute(attributeName = "status")
    private String status;

    @DynamoDBAttribute(attributeName = "is_new_recommendation")
    private boolean newRecommendation;

    @DynamoDBAttribute(attributeName = "processId")
    private String processId;

    @DynamoDBAttribute(attributeName = "profileName")
    private String profileName;

    @DynamoDBAttribute(attributeName = "Volume_advisory")
    private int volumeAdvisory;

    @DynamoDBAttribute(attributeName = "Volume_feature")
    private int volumeFeature;

    @DynamoDBAttribute(attributeName = "Volume_asset")
    private int volumeAsset;

    @DynamoDBAttribute(attributeName = "Volume_bug")
    private int volumeBug;

    @DynamoDBAttribute(attributeName = "Volume_release")
    private int volumeRelease;

    @DynamoDBAttribute(attributeName = "Volume_fieldNotice")
    private int volumeFieldNotice;
}
