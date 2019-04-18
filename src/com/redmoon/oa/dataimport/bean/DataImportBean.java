package com.redmoon.oa.dataimport.bean;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Description:
 * @author: 古月圣
 * @Date: 2015-11-27上午09:46:52
 */
public class DataImportBean {
	public final static int ERR_TYPE = 6;
	public final static int ERR_IS_NULL = 0;
	public final static int ERR_UN_UNIQUE = 1;
	public final static int ERR_NOT_FLOAT = 2;
	public final static int ERR_NOT_INTEGER = 3;
	public final static int ERR_NOT_UNIT = 4;
	public final static int ERR_OUT_RANGE = 5;

	private String code;
	private String name;
	private String author;
	private String table;
	private String className;
	private ArrayList<String> fields;
	private ArrayList<String> fieldsName;
	private HashMap<String, String> splitFields;
	private ArrayList<String> notEmptyFields;
	private ArrayList<String> uniqueFields;
	private ArrayList<String> floatFields;
	private ArrayList<String> integerFields;
	private ArrayList<String> unitFields;
	private HashMap<String, String> rangeFields;
	private String hint;
	private String returnURL;

	public void setCode(String code) {
		this.code = code;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getAuthor() {
		return author;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return name;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public ArrayList<String> getFields() {
		return fields;
	}

	public void setFields(ArrayList<String> fields) {
		this.fields = fields;
	}

	public ArrayList<String> getFieldsName() {
		return fieldsName;
	}

	public void setFieldsName(ArrayList<String> fieldsName) {
		this.fieldsName = fieldsName;
	}

	public HashMap<String, String> getSplitFields() {
		return splitFields;
	}

	public void setSplitFields(HashMap<String, String> splitFields) {
		this.splitFields = splitFields;
	}

	public ArrayList<String> getNotEmptyFields() {
		return notEmptyFields;
	}

	public void setNotEmptyFields(ArrayList<String> notEmptyFields) {
		this.notEmptyFields = notEmptyFields;
	}

	public ArrayList<String> getUniqueFields() {
		return uniqueFields;
	}

	public void setUniqueFields(ArrayList<String> uniqueFields) {
		this.uniqueFields = uniqueFields;
	}

	public ArrayList<String> getFloatFields() {
		return floatFields;
	}

	public void setFloatFields(ArrayList<String> floatFields) {
		this.floatFields = floatFields;
	}

	public ArrayList<String> getIntegerFields() {
		return integerFields;
	}

	public void setIntegerFields(ArrayList<String> integerFields) {
		this.integerFields = integerFields;
	}

	public ArrayList<String> getUnitFields() {
		return unitFields;
	}

	public void setUnitFields(ArrayList<String> unitFields) {
		this.unitFields = unitFields;
	}

	public HashMap<String, String> getRangeFields() {
		return rangeFields;
	}

	public void setRangeFields(HashMap<String, String> rangeFields) {
		this.rangeFields = rangeFields;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public String getReturnURL() {
		return returnURL;
	}

	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}

	public static String getErrMsg(int errNo) {
		switch (errNo) {
		case ERR_IS_NULL:
			return "不能为空";
		case ERR_UN_UNIQUE:
			return "已经存在";
		case ERR_NOT_FLOAT:
			return "必须为数字";
		case ERR_NOT_INTEGER:
			return "必须为整数";
		case ERR_NOT_UNIT:
			return "不存在";
		default:
			break;
		}
		return "";
	}

	public static String getRangeMsg(String name, String value) {
		String[] values = value.split(",");
		String ret = "";
		for (String v : values) {
			v = v.trim();
			if (!ret.equals("")) {
				ret += "且";
			}
			if (v.startsWith("min")) {
				ret += v.replaceAll("min", name);
			} else if (v.startsWith("max")) {
				ret += v.replaceAll("max", name);
			}
		}
		return ret;
	}
}
