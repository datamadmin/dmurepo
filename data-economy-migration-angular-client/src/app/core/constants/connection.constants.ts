export const enum CONNECTION_GROUP {
    AWS_TO_S3 = "AWS_TO_S3",
    HDFS = "HDFS",
    TARGET_FILE_PROPS = "TARGET_FILE_PROPS",
    OTHER_PROPS = "OTHER_PROPS"
}

export const enum CONNECTION_TYPE {
    DIRECT_HDFS = "DIRHDFS",
    DIRECT_LC = "DIRECT LC",
    DIRECT_SC = "DIRECT SC"
}

export const enum SC_CREDENTIALS_ACCESS_TYPE {
    ASSUME = 'Assume',
    AWS_FEDERATED_USER = 'AWS_FEDERATED_USER',
    ASSUME_SAML = 'AssumeSAML'
}

export enum AUTHENTICATION_TYPE {
    UNSECURED = "UNSCRD",
    SECURED = "SCRD"
}

export enum CREDENTIAL_STORAGE_TYPE {
    LDAP = 'LDAP',
    KERBEROS = 'KERBEROS'
}

export enum FORMAT_TYPE {
    SOURCE = "SOURCE",
    TEXT = "TEXT",
    SEQUENCE = "SEQUENCE",
    RECORD_COLUMNAR = "RECORD_COLUMNAR",
    AVRO = "AVRO",
    ORC = "ORC",
    PARQUET = "PARQUET"
}

export enum COMPRESSION_TYPE {
    SOURCE = "SRC_COMPRESSION",
    UN_COMPRESSED = "UN_COMPRESSED",
    GZIP = "GZIP"
}

export enum YES_OR_NO_OPTIONS {
    YES = 'Y',
    NO = 'N'
}