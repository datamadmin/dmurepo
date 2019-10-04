package com.dataeconomy.migration.app.util;

import java.util.ArrayList;
import java.util.List;

public final class DmuConstants {

	private DmuConstants() {
		super();
	}

	public static final String HIVE = "HIVE";
	public static final String IMPALA = "IMPALA";
	public static final String PROVIDE = "PROVIDE";
	public static final String DIRECT_LC = "DIRECT LC";
	public static final String ASSUME = "Assume";
	public static final String ASSUME_SAML = "AssumeSAML";
	public static final String FEDERATED = "Federated";
	public static final String DIRECT_SC = "DIRECT SC";
	public static final String CLIENT_REGION = "us-east-2";
	public static final String YES = "Y";
	public static final String UNSECURED = "UnSecured";
	public static final String LDAP = "ldap";
	public static final String KERBEROS = "kerberos";
	public static final String IN_PROGRESS = "In Progress";
	public static final String FAILED = "Failed";
	public static final String HDFS_DELIMETER = "-";
	public static final String HDFS_LOCATION = "LOCATION";
	public static final String NO = "N";
	public static final String NEW_SCENARIO = "New Scenario";
	public static final String UNKNOWN_CASE = "Unknown Case";
	public static final String SPARK = "spark";
	public static final String AWS_LC = "AWS_LC";
	public static final String AWS_SC = "AWS_SC";
	public static final String AWS_HDFS = "AWS_HDFS";
	public static final String AWS_FEDERATED_USER = "AWS_FEDERATED_USER";
	public static final String AWS_ASSUME_ROLE = "AWS_ASSUME_ROLE";
	public static final String END_POINT_EAST2 = "sts.us-east-2.amazonaws.com";

	public static final String HIVE_CONN_POOL = "HiveConnectionPool";
	public static final String IMPALA_CONN_POOL = "ImpalaHikariConfigPool";
	public static final String SPARK_CONN_POOL = "SparkHikariConfigPool";
	public static final String HIVE_DRIVER_CLASS_NAME = "com.cloudera.hive.jdbc41.HS2Driver";
	public static final String REGULAR = "REGULAR";
	public static final String LARGEQUERY = "LARGEQUERY";
	public static final String MEDIUMQUERY = "MEDIUMQUERY";
	public static final String SMALLQUERY = "SMALLQUERY";
	public static final String DEFAULT_HIVE_POOL = "DefaultHiveConnectionPool";
	public static final String IMPALA_DRIVER_CLASS_NAME = "com.cloudera.impala.jdbc41.Driver";
	public static final String SUBMITTED = "Submitted";
	public static final String NOT_STARTED = "Not Started";
	public static final String SECURED = "SCRD";

	public static final String AWS_TO_S3 = "AWS_TO_S3";
	public static final String HDFS = "HDFS";
	public static final String TARGET_FILE_PROPS = "TARGET_FILE_PROPS";
	public static final String OTHER_PROPS = "OTHER_PROPS";

	public static final String SOURCE = "SOURCE";
	public static final String TEXT = "TEXT";
	public static final String SEQUENCE = "SEQUENCE";
	public static final String RECORD_COLUMNAR = "RECORD_COLUMNAR";
	public static final String AVRO = "AVRO";
	public static final String ORC = "ORC";
	public static final String PARQUET = "PARQUET";
	public static final String UN_COMPRESSED = "UN_COMPRESSED";
	public static final String GZIP = "GZIP";
	public static final String SRC_COMPRESSION = "SRC_COMPRESSION";
	public static final String STORED_AS = "STORED_AS";
	public static final String DIRECT_HDFS = "DIRHDFS";
	public static final String SUCCESS = "Successful";
	public static final String DATA_ECONOMY_CACHE = "dataEconomyCache";

	public static final List<String> STATUS_LIST = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(DmuConstants.SUBMITTED);
		}
	};

	public static final List<String> SUBMITTED_LIST = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(DmuConstants.SUBMITTED);
		}
	};

	public static final String SRC_CMPRSN_FLAG = "SRC_CMPRSN_FLAG";
	public static final String UNCMPRSN_FLAG = "UNCMPRSN_FLAG";
	public static final Object HADOOP_INSTALL_DIR = "HADOOP_INSTALL_DIR";
	public static final Object GZIP_CMPRSN_FLAG = "GZIP_CMPRSN_FLAG";
	public static final Object TEMP_HDFS_DIR = "TEMP_HDFS_DIR";
	public static final String SRC_FORMAT_FLAG = "SRC_FORMAT_FLAG";
	public static final String FILTER_CONDITION = "FILTER_CONDITION";
	public static final String SHOW_CREATE_TABLE = "SHOW CREATE TABLE ";
	public static final String DOT_OPERATOR = ".";
	public static final String HDFS_PEM_LOCATION = "HDFS_PEM_LOCATION";
	public static final String HDFS_USER_NAME = "HDFS_USER_NAME";
	public static final String HDFS_EDGE_NODE = "HDFS_EDGE_NODE";
	public static final String FAILURE = "Failed";
	public static final Object CREDENTIAL_STRG_TYPE = "CREDENTIAL_STRG_TYPE";
	public static final Object AWS_ACCESS_ID_LC = "AWS_ACCESS_ID_LC";
	public static final Object AWS_SECRET_KEY_LC = "AWS_SECRET_KEY_LC";
	public static final String PARALLEL_JOBS = "PARALLEL_JOBS";
	public static final String PARALLEL_USR_RQST = "PARALLEL_USR_RQST";
	public static final String REQUEST_NO = "requestNo";

}