package com.cloudweb.oa.service;

public class DataDictService {
    public static String getDataType(int dataType) {
        String type = "varchar";
        if (dataType == java.sql.Types.VARCHAR) {
            type = "varchar";
        } else if (dataType == java.sql.Types.BOOLEAN) {
            type = "boolean";
        } else if (dataType == java.sql.Types.TIMESTAMP) {
            type = "timestamp";
        } else if (dataType == java.sql.Types.DATE) {
            type = "date";
        } else if (dataType == java.sql.Types.LONGVARCHAR) { // text类型
            type = "longvarchar";
        } else if (dataType == java.sql.Types.TINYINT) {
            type = "tinyint";
        } else if (dataType == java.sql.Types.INTEGER) {
            type = "int";
        } else if (dataType == java.sql.Types.BIT) {
            type = "boolean";
        } else if (dataType == java.sql.Types.BIGINT) {
            type = "bigint";
        } else if (dataType == java.sql.Types.DECIMAL) {
            type = "float";
        } else if (dataType == java.sql.Types.CHAR) {
            type = "char";
        } else if (dataType == java.sql.Types.REAL) {
            type = "float";
        } else if (dataType == java.sql.Types.DOUBLE) {
            type = "double";
        }
        return type;
    }
}
