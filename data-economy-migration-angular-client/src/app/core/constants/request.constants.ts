export const enum REQUEST_TYPE {
    HIVE_TO_S3 = 'HIVE TO S3',
    TERADATA_TO_S3 = 'Teradata TO S3',
    TERADATA_TO_RED_SHIFT = 'Teradata TO RedShift',
    TERADATA_TO_SNOWFLAKE = 'Teradata TO Snowflake'
}

export const enum MIGRATION_TYPE {
    FULL_DATABASE = 'Full Database',
    LIST_OF_TABLE_FROM_FILE = 'List of table from speadsheet (csv file)'
}


export const enum FILEPATH_COLUMNS {
    SR_NO = 'Sr.No',
    DATABASE_NAME = 'Database Name',
    TABLE_NAME = 'Table Name',
    FILTER_CONDITION = 'Filter Condition',
    TARGET_BUCKET_NAME = 'Target Bucket Name',
    INCREMENTAL_FLAG = 'Incremental Flag',
    INCREMENTAL_COLUMN = 'Incremental Column'
}