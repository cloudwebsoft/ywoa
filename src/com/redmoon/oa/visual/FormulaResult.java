package com.redmoon.oa.visual;

import java.util.Date;

import com.redmoon.oa.flow.FormField;

import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;

public class FormulaResult {

	private int fieldType = FormField.FIELD_TYPE_INT;
	
	private String result = "";
	
	private int decimals = 2;
	
	public FormulaResult(int fieldType, int decimals) {
		this.fieldType = fieldType;
		this.decimals = decimals;
	}

	public int getIntVal() {
		return StrUtil.toInt(result, 0);
	}
	
	public float getFloatVal() {
		return StrUtil.toFloat(result, 0);
	}
	
	public double getPriceVal() {
		return StrUtil.toDouble(result, 0);
	}
	
	public double getDoubleVal() {
		return StrUtil.toDouble(result, 0);
	}
	
	/**
	 * 取值，返回字符串
	 * @return
	 */	
	public String getValue() {
		if (decimals >=0 ) {
			if (fieldType== FormField.FIELD_TYPE_DOUBLE || fieldType== FormField.FIELD_TYPE_FLOAT || fieldType==FormField.FIELD_TYPE_PRICE) {
				double r = getDoubleVal();
				return NumberUtil.round(r, decimals);
			}
		}
		return result;		
	}

	public int getFieldType() {
		return fieldType;
	}

	public void setFieldType(int fieldType) {
		this.fieldType = fieldType;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
}
