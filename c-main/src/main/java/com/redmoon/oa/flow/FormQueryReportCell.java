package com.redmoon.oa.flow;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormQueryReportCell {

    public static final int DEFAULT_WIDTH = 100;

    /**
     * 标题（表头）
     */
    private String value = "";
    private int width;
    /**
     * 字段名，如果是SQL型的，头以cwsSQL开头加随机数new Date().getTime()
     */
    private String fieldName = "";
    
    public static final String SQL_COL_PREFIX = "cwsSQL";
    public static final String SCRIPT_COL_PREFIX = "cwsScript";
    
    private String formula;
    private String fieldType;
    private String axis;
    /**
     * jqgrid合计，目前直接在代码中赋予sum
     */
    private String sumType;

    public FormQueryReportCell() {
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setSumType(String sumType) {
        this.sumType = sumType;
    }

    public String getValue() {
        return value;
    }

    public String getAxis() {
        return axis;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFormula() {
        return formula;
    }

    public String getFieldType() {
        return fieldType;
    }

    public int getWidth() {
        return width;
    }

    public String getSumType() {
        return sumType;
    }


}
