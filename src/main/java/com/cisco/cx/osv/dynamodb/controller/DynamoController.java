package com.cisco.cx.osv.dynamodb.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cisco.cx.osv.dynamodb.entity.Cluster;
import com.cisco.cx.osv.dynamodb.entity.DBSchemaMap;
import com.cisco.cx.osv.dynamodb.entity.LogLevels;
import com.cisco.cx.osv.dynamodb.entity.SchemaMigrationAudit;
import com.cisco.cx.osv.dynamodb.repository.ClusterRepository;
import com.cisco.cx.osv.dynamodb.repository.DBSchemaMapRepository;
import com.cisco.cx.osv.dynamodb.repository.SchemaMigrationAuditRepository;

@RestController
public class DynamoController {

	@Autowired
	private ClusterRepository clusterRepo;

	@Autowired
	private DBSchemaMapRepository dbSchemaMapRepo;

	@Autowired
	private SchemaMigrationAuditRepository schemaMigrationAuditRepo;

	@GetMapping("/")
	public String display() {
		
		System.out.println(LocalDateTime.now());
		
		return "Hello";
	}

	// Table 1
	@PostMapping("/saveCluster")
	public Cluster saveCluster(@RequestBody Cluster cluster) {
		return clusterRepo.addcluster(cluster);
	}

	@GetMapping("/getCluster/{clusterName}")
	public Cluster getCluster(@PathVariable String clusterName) {
		return clusterRepo.findClusterByClusterName(clusterName);
	}

	@DeleteMapping("/deleteCluster")
	public String deleteCluster(@RequestBody Cluster cluster) {
		return clusterRepo.deleteCluster(cluster);
	}

	@PutMapping("/updateCluster")
	public String updateCluster(@RequestBody Cluster cluster) {
		return clusterRepo.updateCluster(cluster);
	}

	// Table 2
	@PostMapping("/saveDBSchemaMap")
	public DBSchemaMap saveCluster(@RequestBody DBSchemaMap dbSchemaMap) {
		return dbSchemaMapRepo.addDBSchemaMap(dbSchemaMap);
	}

	@GetMapping("/getDBSchemaMap/{clusterName}/{schemaName}")
	public DBSchemaMap getDBSchemaMap(@PathVariable String clusterName, @PathVariable String schemaName) {
		return dbSchemaMapRepo.findDBSchemaMapByClusterAndSchema(clusterName, schemaName);
	}

	@GetMapping("/scanByClusterName/{clusterName}")
	public List<DBSchemaMap> getDBSchemaMap1(@PathVariable String clusterName) {
		return dbSchemaMapRepo.scanByClusterName(clusterName);
	}

	@GetMapping("/scanByCustomerId/{customerId}")
	public List<DBSchemaMap> scanByCustomerId(@PathVariable String customerId) {
		return dbSchemaMapRepo.scanByCustomerId(customerId);
	}

	@DeleteMapping("/deleteDBSchemaMap")
	public String deleteDBSchemaMap(@RequestBody DBSchemaMap dbSchemaMap) {
		return dbSchemaMapRepo.deleteDBSchemaMap(dbSchemaMap);
	}

	@PutMapping("/updateDBSchemaMap")
	public String updateDBSchemaMap(@RequestBody DBSchemaMap dbSchemaMap) {
		return dbSchemaMapRepo.updateDBSchemaMap(dbSchemaMap);
	}

	@PutMapping("/lock")
	public String lock(@RequestBody DBSchemaMap dbSchemaMap) throws InterruptedException {
		return dbSchemaMapRepo.lock(dbSchemaMap);
	}

	@PutMapping("/lock2")
	public String lock2(@RequestBody DBSchemaMap dbSchemaMap) throws InterruptedException {
		return dbSchemaMapRepo.lock2(dbSchemaMap);
	}

	// Table 3
	@PostMapping("/saveSchemaMigrationAudit")
	public SchemaMigrationAudit saveSchemaMigrationAudit(@RequestBody SchemaMigrationAudit schemaMigrationAudit) {
		return schemaMigrationAuditRepo.addSchemaMigrationAudit(schemaMigrationAudit);
	}

	@GetMapping("/getSchemaMigrationAudit/{auditDate}/{referenceId}")
	public SchemaMigrationAudit getSchemaMigrationAudit(@PathVariable String auditDate,
			@PathVariable String auditTimestamp) {
		return schemaMigrationAuditRepo.findSchemaMigrationAuditByAuditDate(auditDate, auditTimestamp);
	}

	@DeleteMapping("/deleteDSchemaMigrationAudit")
	public String deleteDSchemaMigrationAudit(@RequestBody SchemaMigrationAudit schemaMigrationAudit) {
		return schemaMigrationAuditRepo.deleteSchemaMigrationAudit(schemaMigrationAudit);
	}

	@PutMapping("/updateDSchemaMigrationAudit")
	public String updateDBSchemaMap(@RequestBody SchemaMigrationAudit schemaMigrationAudit) {
		return schemaMigrationAuditRepo.updateSchemaMigrationAudit(schemaMigrationAudit);
	}

	@GetMapping("/getLogsByReferenceId/{referneceID}")
	public List<LogLevels> getLogsByReferenceId(@PathVariable String referneceID) {
		return schemaMigrationAuditRepo.getLogsByReferenceId(referneceID);
	}

	@GetMapping("/getLRecordsByReferenceIdAndLogLevel/{referneceID}/{logLevel}")
	public List<SchemaMigrationAudit> getLRecordsByReferenceIdAndLogLevel(@PathVariable String referneceID,
			@PathVariable String logLevel) {
		return schemaMigrationAuditRepo.getLRecordsByReferenceIdAndLogLevel(referneceID, logLevel);
	}

	@GetMapping("/getByDate/{date}")
	public List<SchemaMigrationAudit> getByDate(@PathVariable String date) {
		return schemaMigrationAuditRepo.getByDate(date);
	}

	@GetMapping("/getByDateAndTime/{date}/{startTime}/{endTime}")
	public List<SchemaMigrationAudit> getByDateAndTime(@PathVariable String date, @PathVariable String startTime,
			@PathVariable String endTime) {
		return schemaMigrationAuditRepo.getByDateAndTime(date, startTime, endTime);
	}

	@GetMapping({ "/getByRangeDateAndTime/{startDate}/{endDate}",
			"/getByRangeDateAndTime/{startDate}/{endDate}/{startTime}/{endTime}" })
	public List<SchemaMigrationAudit> getByRangeDateAndTime(@PathVariable String startDate,
			@PathVariable String endDate, @PathVariable(required = false) String startTime,
			@PathVariable(required = false) String endTime) {
		return schemaMigrationAuditRepo.getByRangeDateAndTime(startDate, endDate, startTime, endTime);
	}

	/*@Autowired
	AWSSecrets awsSecrets;
	@Autowired
	SecretsManagerClient awsSecretsConfig;

	@GetMapping("/getSecrets")
	public String getSecrets() {
		String secretName = "OSV_DB_CREDENTIALS_SECRET_NAME";
		awsSecrets.getValue(awsSecretsConfig, secretName);
		return "SUCCESS";
	}
*/
	
	
	@PostMapping("/transaction")
	@Transactional
	public List<DBSchemaMap> t(@RequestBody DBSchemaMap dbSchemaMap) throws InterruptedException {
		
		dbSchemaMapRepo.lock(dbSchemaMap);
		System.out.println("scan: ");
		dbSchemaMapRepo.scanByCustomerId("test").forEach(i-> System.out.println(i.getCustomerId()));
		
		return dbSchemaMapRepo.scanByCustomerId("test");
	}
	
	
	/*
	 * @Autowired SecretManager secretManager;
	 * 
	 * @GetMapping("/secretManager") public void secretManager() {
	 * secretManager.getSecret(); }
	 */
	
	
}
