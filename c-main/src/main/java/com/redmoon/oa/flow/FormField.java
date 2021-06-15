package com.redmoon.oa.flow;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.regex.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.base.*;
import com.redmoon.oa.flow.macroctl.*;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormField implements Cloneable,Serializable {

    public static final String TYPE_TEXTFIELD = "text";
    public static final String TYPE_TEXTAREA = "textarea";
    public static final String TYPE_SELECT = "select";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_CHECKBOX = "checkbox";
    public static final String TYPE_RADIO = "radio";
    public static final String TYPE_DATE = "DATE";
    public static final String TYPE_DATE_TIME = "DATE_TIME";
    public static final String TYPE_MACRO = "macro";

    public static final String TYPE_CALCULATOR = "CALCULATOR";
    public static final String TYPE_SQL = "SQL";
    public static final String TYPE_BUTTON = "BUTTON";

    public static final String MACRO_NOT = "0";

    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static final int FIELD_TYPE_VARCHAR = 0;
    public static final int FIELD_TYPE_TEXT = 1;
    public static final int FIELD_TYPE_INT = 2;
    public static final int FIELD_TYPE_LONG = 3;
    public static final int FIELD_TYPE_BOOLEAN = 4;
    public static final int FIELD_TYPE_FLOAT = 5;
    public static final int FIELD_TYPE_DOUBLE = 6;
    public static final int FIELD_TYPE_DATE = 7;
    public static final int FIELD_TYPE_DATETIME = 8;
    public static final int FIELD_TYPE_PRICE = 9; // 价格型，00.00
    
    /**
     * 不隐藏
     */
    public static final int HIDE_NONE = 0;
    /**
     * 流程中编辑和显示时及模块显示、添加、编辑时均隐藏
     */
    public static final int HIDE_ALWAYS = 1;
    /**
     * 流程及模块中仅编辑时隐藏
     */
    public static final int HIDE_EDIT = 2;

    /**
     * 单行文本框字段的默认长度
     */
    public static final int TEXT_FIELD_DEFAULT_LENGTH = 100;

/*
    public static final String MACRO_USER_SELECT = "macro_user_select";
    public static final String MACRO_BOARDROOM_SELECT =
            "macro_boardroom_select";
    public static final String MACRO_VEHICLE_SELECT = "macro_vehicle_select";
    public static final String MACRO_DRIVER_SELECT = "macro_driver_select";
    public static final String MACRO_DEPT_SELECT = "macro_dept_select";
    public static final String MACRO_CURRENT_USER = "macro_current_user";
    public static final String MACRO_CUSTOMER_LIST_WIN =
            "macro_customer_list_win";
    public static final String MACRO_LINKMAN_LIST_WIN =
            "macro_linkman_list_win";
    public static final String MACRO_PROVIDER_LIST_WIN =
            "macro_provider_list_win";
    public static final String MACRO_PRODUCT_LIST_WIN =
            "macro_product_list_win";
    public static final String MACRO_PRODUCT_SERVICE_LIST_WIN =
            "macro_product_service_list_win";
    public static final String MACRO_SIGN = "macro_sign";
    public static final String MACRO_IDEA = "macro_idea";
    public static final String MACRO_WORKFLOW_SEQUENCE = "macro_flow_sequence";
*/

    public static final String DATE_CURRENT = "CURRENT";

    /**
     * 不唯一，默认
     */
    public static final int UNIQUE_NONE = 0;
    /**
     * 全局唯一
     */
    public static final int UNIQUE_GLOBAL = 1;
    /**
     * 嵌套表（或关联模块）唯一
     */
    public static final int UNIQUE_NEST = 2;

    public FormField() {
    }

    public static String getTypeDesc(String type, String macroType) {
        switch (type) {
            case TYPE_TEXTFIELD:
                return "单行文本框";
            case TYPE_TEXTAREA:
                return "多行文本框";
            case TYPE_SELECT:
                return "选择框";
            case TYPE_LIST:
                return "列表框";
            case TYPE_CHECKBOX:
                return "多选框";
            case TYPE_RADIO:
                return "单选框";
            case TYPE_DATE:
                return "日期";
            case TYPE_DATE_TIME:
                return "详细日期";
            case TYPE_CALCULATOR:
                return "计算控件";
            case TYPE_BUTTON:
                return "按钮";
            case TYPE_MACRO:
                return getInputValueOfMacro(macroType);
            default:
                return "";
        }
    }

    public String getTypeDesc() {
        return getTypeDesc(type, macroType);
    }

    public static String getFieldTypeDesc(int fieldType) {
        if (fieldType == FIELD_TYPE_VARCHAR) {
             return "字符串型";
         } else if (fieldType == FIELD_TYPE_TEXT) {
             return "文本型";
         } else if (fieldType == FIELD_TYPE_INT) {
             return "整型";
         } else if (fieldType == FIELD_TYPE_LONG) {
             return "长整型";
         } else if (fieldType == FIELD_TYPE_FLOAT) {
             return "浮点型";
         } else if (fieldType == FIELD_TYPE_DOUBLE) {
             return "双精度型";
         } else if (fieldType == FIELD_TYPE_DATE) {
             return "日期型";
         } else if (fieldType == FIELD_TYPE_DATETIME) {
             return "日期时间型";
         } else if (fieldType == FIELD_TYPE_PRICE) {
             return "价格型";
         } else if (fieldType == FIELD_TYPE_BOOLEAN) {
             return "布尔型";
         }
         else
            return "";
    }

    public String getFieldTypeDesc() {
        return getFieldTypeDesc(fieldType);
    }

    /**
     * 取得对应于macroType的在表单设计器中赋予的控件值，如 macro_user_select控件的值为 宏控件：用户列表
     * @param macroType String
     * @return String
     */
    public static String getInputValueOfMacro(String macroType) {
        MacroCtlMgr mm = new MacroCtlMgr();
        MacroCtlUnit mu = mm.getMacroCtlUnit(macroType);
        if (mu == null) {
            return macroType + " 不存在";
        } else {
            return mu.getInputValue();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public void setMacroType(String macroType) {
        this.macroType = macroType;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public void setCanNull(boolean canNull) {
        this.canNull = canNull;
    }

    public void setCanQuery(boolean canQuery) {
        this.canQuery = canQuery;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setCanList(boolean canList) {
        this.canList = canList;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getFormCode() {
        return formCode;
    }

    public String getMacroType() {
        return macroType;
    }

    public String getDefaultValueRaw() {
        return StrUtil.getNullStr(defaultValue);
    }

    /**
     * 取得默认值
     * @return String
     */
    public String getDefaultValue() {
        // System.out.println("FormField: getDeafultValue type=" + type + " name=" + name + " title=" + title);
        if (type==null) {
            LogUtil.getLog(getClass()).error("FormField: getDeafultValue type=" + type + " name=" + name + " title=" + title); //
            return "";
        }
        
        // 如果是函数型的字段，则默认值返回空
        if (isFunc()) {
        	return "";
        }

        switch (type) {
            case TYPE_DATE:
                // System.out.println("FormField: getDeafultValue type1=" + type);
                // if (defaultValue.equals(DATE_CURRENT))
                //    return DateUtil.format(new java.util.Date(), FORMAT_DATE);
                return "";
            case TYPE_DATE_TIME:
                // if (defaultValue.equals(DATE_CURRENT))
                //    return DateUtil.format(new java.util.Date(), FORMAT_DATE_TIME);
                return "";
            case TYPE_CALCULATOR:
                return "0.0";
        }
        
        if (fieldType==FIELD_TYPE_VARCHAR) {
        	if (getDefaultValueRaw().startsWith("$")) {
        		return "";
        	}
        }
        else if (fieldType==FIELD_TYPE_INT) {
			if (!StrUtil.isNumeric(getDefaultValueRaw())) {
				return "";
			}
        }
        else if (fieldType==FIELD_TYPE_LONG) {
        	if (!StrUtil.isNumeric(getDefaultValueRaw())) {
        		return "";
        	}
        }
        else if (fieldType==FIELD_TYPE_FLOAT) {
        	try {
        		StrUtil.toFloat(getDefaultValueRaw());
        	}
        	catch (Exception e) {
        		return "";
        	}
        }
        else if (fieldType==FIELD_TYPE_DOUBLE) {
        	try {
        		StrUtil.toDouble(getDefaultValueRaw());
        	}
        	catch (Exception e) {
        		return "";
        	}
        }
        else if (fieldType==FIELD_TYPE_PRICE) {
        	try {
        		StrUtil.toDouble(getDefaultValueRaw());
        	}
        	catch (Exception e) {
        		return "";
        	}
        }        
        return getDefaultValueRaw();
    }

    public String getValue() {
        return value;
    }

    public int getFieldType() {
        return fieldType;
    }

    public String getRule() {
        return rule;
    }

    public boolean isCanNull() {
        return canNull;
    }

    public boolean isCanQuery() {
        return canQuery;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isCanList() {
        return canList;
    }

    public int getOrders() {
        return orders;
    }

    public int getWidth() {
        return width;
    }

    /**
    * 根据控件类型，置数据类型，以免紊乱
    */
    public void checkFieldTypeAccordCtlType() {
        // 使控件类型与其数据类型一致
        if (type.equals(TYPE_DATE)) {
            fieldType = FIELD_TYPE_DATE;
        } else if (getType().equals(TYPE_DATE_TIME)) {
            fieldType = FIELD_TYPE_DATETIME;
        } else if (getType().equals(TYPE_CHECKBOX)) {
            fieldType = FIELD_TYPE_BOOLEAN;
        } else if (type.equals(TYPE_TEXTAREA)) {
            fieldType = FIELD_TYPE_TEXT;
        }
    }

    public static String getFieldValueFromFields(String fieldName, Vector fields) {
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (fieldName.equals(ff.getName())) {
                // System.out.println(getClass() + " " + ff.getName() + " " + ff.getTitle() + " " + ff.getValue());
                return StrUtil.getNullStr(ff.getValue());
            }
        }
        return null;
    }
    
    /**
     * 仅用于com.redmoon.oa.visua.FormDAO.createEmptyForm
     * @param ps
     * @param index
     * @param fields
     * @throws SQLException
     */
    public void createDAO(PreparedStatement ps, int index, Vector fields) throws SQLException {
    	createDAO(-1, ps, index, fields);
    }

    /**
     * 用于FormDAO插入表单域的值，根据字段类型，得出相应的字符串
     * @return String
     */
    public void createDAO(int flowId, PreparedStatement ps, int index, Vector fields) throws SQLException {
        if (getType().equals(TYPE_MACRO)) { //  && ff.getMacroType().equals(ff.MACRO_WORKFLOW_SEQUENCE)) {
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(getMacroType());

            LogUtil.getLog(getClass()).info("createDAO: mu=" + mu + " ff.getMacroType()=" + getMacroType());

            if (mu!=null) {
                IFormMacroCtl ictl = mu.getIFormMacroCtl();
                if (ictl != null) {
                    Object obj = ictl.getValueForCreate(flowId, this);
                    if (obj instanceof Integer) {
                        ps.setInt(index, (Integer) obj);
                    }
                    else if (obj instanceof Long) {
                        ps.setLong(index, (Long)obj);
                    }
                    else if (obj instanceof Double) {
                        ps.setDouble(index, (Double)obj);
                    }
                    else if (obj instanceof Float) {
                        ps.setDouble(index, (Float)obj);
                    }
                    else {
                        ps.setString(index, (String) obj);
                    }
                }
                else {
                    ps.setString(index, "");
                    LogUtil.getLog(getClass()).error(getTitle() + " " + getName() +
                                                     " macroType=" + getMacroType() +
                                                     " is not exist.");
                }
            }
            else {
                ps.setString(index, "");
                LogUtil.getLog(getClass()).error("createDAO: " + getTitle() + " " + getName() +
                                                 " macroType=" + getMacroType() +
                                                     " is not exist.");
            }
        }
        else if (getType().equals(TYPE_SQL)) {
            ps.setString(index, "");
        }
        /*
        因为计算控件中加入了sum公式用以计算嵌套表的列累加值，所以此处的计算仅适用于非公式的情况，已改为在前台计算
        else if (getType().equals(TYPE_CALCULATOR)) {
            // 计算
            String formula = getDefaultValue();
            // 取出计算式子中的各项，包括操作符及括号
            Vector fieldv = FormulaCalculator.getSymbolsWithBracket(formula);
            int flen = fieldv.size();
            for (int i=0; i<flen; i++) {
                String f = (String)fieldv.elementAt(i);
                // 如果不是操作符
                if (!FormulaCalculator.isOperator(f)) {
                    // 如果是表单中的域，则替换
                    String s = getFieldValueFromFields(f, fields); // fDao.getFieldValue(f);
                    LogUtil.getLog(getClass()).info("saveDAO: value=" + s + " fieldName=" + f);
                    if (s!=null) {
                        fieldv.set(i, s);
                    }
                }
            }
            // 重组为算式
            Iterator ir = fieldv.iterator();
            formula = "";
            while (ir.hasNext()) {
                String s = (String)ir.next();
                formula += s;
            }
            // 计算
            FormulaCalculator fc = new FormulaCalculator(formula);
            double r = fc.getResult();
            ps.setDouble(index, r);
        }
        */
        else if (getType().equals(TYPE_BUTTON)) {
            ps.setString(index, "");
        }
        else if (fieldType==FIELD_TYPE_VARCHAR) {
            ps.setString(index, getDefaultValue());
        }
        else if (fieldType==FIELD_TYPE_TEXT) {
            ps.setString(index, getDefaultValue());
        }
        else if (fieldType==FIELD_TYPE_INT) {
            if (getDefaultValue().equals("")) {
                ps.setNull(index, java.sql.Types.INTEGER);
            }
            else {
            	if (StrUtil.isNumeric(getDefaultValue())) {
            		ps.setInt(index, StrUtil.toInt(getDefaultValue()));
            	}
            	else {
                    ps.setNull(index, java.sql.Types.INTEGER);
            	}
            }
        }
        else if (fieldType==FIELD_TYPE_LONG) {
            if (StrUtil.getNullStr(getDefaultValue()).equals("")) {
                ps.setNull(index, java.sql.Types.BIGINT);
            }
            else {
            	if (StrUtil.isNumeric(getDefaultValue())) {
                    ps.setLong(index, StrUtil.toLong(getDefaultValue()));
            	}
            	else {
                    ps.setNull(index, java.sql.Types.BIGINT);
            	}            	
            }
        }
        else if (fieldType==FIELD_TYPE_BOOLEAN) {
            ps.setString(index, getDefaultValue());
        }
        else if (fieldType==FIELD_TYPE_FLOAT) {
            if (StrUtil.getNullStr(getDefaultValue()).equals("")) {
                ps.setNull(index, java.sql.Types.FLOAT);
            }
            else {
                ps.setFloat(index, StrUtil.toFloat(getDefaultValue()));
            }
        }
        else if (fieldType==FIELD_TYPE_DOUBLE) {
            // System.out.println(getClass() + " " + getName() + " " + getDefaultValue());
            // System.out.println(getClass() + " " + getName() + " " + getDefaultValue().equals(""));

            if (StrUtil.getNullStr(getDefaultValue()).equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else {
                ps.setDouble(index, StrUtil.toDouble(getDefaultValue()));
            }
        }
        else if (fieldType==FIELD_TYPE_PRICE) {
            if (getDefaultValue().equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else {
                ps.setDouble(index, StrUtil.toDouble(getDefaultValue()));
            }
        }
        else if (fieldType==FIELD_TYPE_DATE) {
            java.util.Date d = null;
            d = DateUtil.parse(getDefaultValue(), FormField.FORMAT_DATE);
            if (d==null) {
                ps.setDate(index, null);
            } else {
                ps.setDate(index, new java.sql.Date(d.getTime()));
            }
        }
        else if (fieldType==FIELD_TYPE_DATETIME) {
            java.util.Date d = null;
            d = DateUtil.parse(getDefaultValue(), FormField.FORMAT_DATE_TIME);
            if (d==null) {
                ps.setDate(index, null);
            } else {
                ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
            }
        }
        else {
            ps.setString(index, getDefaultValue());
        }
    }

    /**
     * 判断是否为数值型字段
     * @param fieldType
     * @return
     */
    public static boolean isNumeric(int fieldType) {
        if (fieldType == FIELD_TYPE_INT || fieldType == FIELD_TYPE_LONG
                || fieldType == FIELD_TYPE_FLOAT || fieldType == FIELD_TYPE_DOUBLE || fieldType == FIELD_TYPE_PRICE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建智能模块中的表单
     * @param ps PreparedStatement
     * @param index int
     * @throws SQLException
     */
    public void createDAOVisual(PreparedStatement ps, int index, FileUpload fu, FormDb fd) throws SQLException {
        // LogUtil.getLog(getClass()).info("createDAOVisual: " + name + " " + title + " fieldType=" + fieldType);

        if (getType().equals(TYPE_MACRO)) { //  && ff.getMacroType().equals(ff.MACRO_WORKFLOW_SEQUENCE)) {
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(getMacroType());
            // LogUtil.getLog(getClass()).info("createDAOVisual: mu=" + mu + " fieldName=" + name + " value=" + (String)mu.getIFormMacroCtl().getValueForCreate(flowId, this));
            // 赋予值，否则在FormDAOMgr中当调用fdao.create(request, fu);后，在添加事件中保存fdao，就可能会使得attachmentctl的值为空
            IFormMacroCtl iFormMacroCtl = mu.getIFormMacroCtl();
            value = (String)iFormMacroCtl.getValueForCreate(this, fu, fd);
            // 如果为数值型的，但值为空，则置为null
            if (isNumeric(iFormMacroCtl.getFieldType(this))) {
                if ("".equals(value)) {
                    value = null;
                }
            }
            ps.setString(index, value);
        }
        else if (getType().equals(TYPE_SQL)) {
            ps.setString(index, "");
        }
        /*
        else if (getType().equals(TYPE_CALCULATOR)) {
            // 计算
            String formula = getDefaultValue();
            // 取出计算式子中的各项，包括操作符及括号
            Vector fieldv = FormulaCalculator.getSymbolsWithBracket(formula);
            int flen = fieldv.size();
            for (int i=0; i<flen; i++) {
                String f = (String)fieldv.elementAt(i);
                // 如果不是操作符
                if (!FormulaCalculator.isOperator(f)) {
                    // 如果是表单中的域，则替换
                    String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // fDao.getFieldValue(f);
                    LogUtil.getLog(getClass()).info("saveDAO: value=" + s + " fieldName=" + f);
                    if (s!=null) {
                        fieldv.set(i, s);
                    }
                }
            }
            // 重组为算式
            Iterator ir = fieldv.iterator();
            formula = "";
            while (ir.hasNext()) {
                String s = (String)ir.next();
                formula += s;
            }
            // 计算
            FormulaCalculator fc = new FormulaCalculator(formula);
            double r = fc.getResult();
            ps.setDouble(index, r);
        }
        */
        else if (getType().equals(TYPE_BUTTON)) {
            ps.setString(index, "");
        }
        else if (fieldType==FIELD_TYPE_VARCHAR) {
            ps.setString(index, getValue());
        }
        else if (fieldType==FIELD_TYPE_TEXT) {
            ps.setString(index, getValue());
        }
        else if (fieldType==FIELD_TYPE_INT) {
            if (getValue().equals("")) {
                ps.setNull(index, java.sql.Types.INTEGER);
            }
            else
                ps.setInt(index, StrUtil.toInt(getValue()));
        }
        else if (fieldType==FIELD_TYPE_LONG) {
            if (getValue().equals("")) {
                ps.setNull(index, java.sql.Types.BIGINT);
            }
            else
                ps.setLong(index, StrUtil.toLong(getValue()));
        }
        else if (fieldType==FIELD_TYPE_BOOLEAN) {
            ps.setString(index, getValue());
        }
        else if (fieldType==FIELD_TYPE_FLOAT) {
            if (getValue().equals("")) {
                ps.setNull(index, java.sql.Types.FLOAT);
            }
            else
                ps.setFloat(index, StrUtil.toFloat(getValue()));
        }
        else if (fieldType==FIELD_TYPE_DOUBLE) {
            if (getValue().equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else
                ps.setDouble(index, StrUtil.toDouble(getValue()));
        }
        else if (fieldType==FIELD_TYPE_PRICE) {
            if (getValue().equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else
                ps.setDouble(index, StrUtil.toDouble(getValue()));
        }
        else if (fieldType==FIELD_TYPE_DATE) {
            java.util.Date d = null;
            d = DateUtil.parse(getValue(), FormField.FORMAT_DATE);
            if (d==null)
                ps.setDate(index, null);
            else {
                ps.setDate(index, new java.sql.Date(d.getTime()));
            }
        }
        else if (fieldType==FIELD_TYPE_DATETIME) {
        	String dateStr = "";
        	// 手机端有可能会传来没有秒的格式，如：2018-10-29 10:55，长度为16
			if (getValue()!=null && getValue().length() == 16) {
				dateStr = getValue() + ":00";
			} else if (getValue()!=null && getValue().length() == 10) {
				// 2018-10-29
				dateStr = getValue() + " 00:00:00";
			} else {
				dateStr = getValue();
			}        	
            java.util.Date d = DateUtil.parse(dateStr, FormField.FORMAT_DATE_TIME);
            if (d==null)
                ps.setDate(index, null);
            else {
                ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
            }
        }
        else
            ps.setString(index, getValue());
    }

    public void createDAOVisualForLog(PreparedStatement ps, int index, FormField ff) throws SQLException {
        if (fieldType==FIELD_TYPE_INT) {
            if (ff.getValue()==null || ff.getValue().equals("")) {
                ps.setNull(index, java.sql.Types.INTEGER);
            }
            else
                ps.setInt(index, StrUtil.toInt(ff.getValue()));
        }
        else if (fieldType==FIELD_TYPE_LONG) {
            if (ff.getValue()==null || ff.getValue().equals("")) {
                ps.setNull(index, java.sql.Types.BIGINT);
            }
            else
                ps.setLong(index, StrUtil.toLong(ff.getValue()));
        }
        else if (fieldType==FIELD_TYPE_FLOAT) {
            if (ff.getValue()==null || ff.getValue().equals("")) {
                ps.setNull(index, java.sql.Types.FLOAT);
            }
            else
                ps.setFloat(index, StrUtil.toFloat(ff.getValue()));
        }
        else if (fieldType==FIELD_TYPE_DOUBLE) {
            if (ff.getValue()==null || ff.getValue().equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else
                ps.setDouble(index, StrUtil.toDouble(ff.getValue()));
        }
        else if (fieldType==FIELD_TYPE_PRICE) {
            if (ff.getValue()==null || ff.getValue().equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else
                ps.setDouble(index, StrUtil.toDouble(ff.getValue()));
        }
        else if (fieldType==FIELD_TYPE_DATE) {
            java.util.Date d = null;
            d = DateUtil.parse(ff.getValue(), FormField.FORMAT_DATE);
            if (d==null)
                ps.setDate(index, null);
            else {
                ps.setDate(index, new java.sql.Date(d.getTime()));
            }
        }
        else if (fieldType==FIELD_TYPE_DATETIME) {
        	String dateStr = "";
        	// 手机端有可能会传来没有秒的格式，如：2018-10-29 10:55，长度为16
			if (ff.getValue()!=null && ff.getValue().length() == 16) {
				dateStr = getValue() + ":00";
			} else if (ff.getValue()!=null && ff.getValue().length() == 10) {
				// 2018-10-29
				dateStr = ff.getValue() + " 00:00:00";
			} else {
				dateStr = ff.getValue();
			}            	
            java.util.Date d = DateUtil.parse(dateStr, FormField.FORMAT_DATE_TIME);
            if (d==null)
                ps.setDate(index, null);
            else {
                ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
            }
        }
        else
            ps.setString(index, ff.getValue());
    }

    /**
     * 创建智能模块中的表单，用于导入时，创建数据
     * @param ps PreparedStatement
     * @param index int
     * @throws SQLException
     */
    public void createDAOVisual(PreparedStatement ps, int index) throws SQLException {
        // LogUtil.getLog(getClass()).info("createDAOVisual: " + name + " " + title + " fieldType=" + fieldType);

    	String val = getValue();
    	
    	// 如果为空，则赋以默认值
    	if (val==null || "".equals(val)) {
    		if (!"".equals(getDefaultValue())) {
    			val = getDefaultValue();
    		}
    	}
    	
        if (getType().equals(TYPE_MACRO)) { //  && ff.getMacroType().equals(ff.MACRO_WORKFLOW_SEQUENCE)) {
//            MacroCtlMgr mm = new MacroCtlMgr();
//            MacroCtlUnit mu = mm.getMacroCtlUnit(getMacroType());
            ps.setString(index, val);
        }
        else if (getType().equals(TYPE_SQL)) {
            ps.setString(index, "");
        }
        /*
        else if (getType().equals(TYPE_CALCULATOR)) {
            // 计算
            String formula = getDefaultValue();
            // 取出计算式子中的各项，包括操作符及括号
            Vector fieldv = FormulaCalculator.getSymbolsWithBracket(formula);
            int flen = fieldv.size();
            for (int i=0; i<flen; i++) {
                String f = (String)fieldv.elementAt(i);
                // 如果不是操作符
                if (!FormulaCalculator.isOperator(f)) {
                    // 如果是表单中的域，则替换
                    String s = com.redmoon.oa.visual.FormDAOMgr.getFieldValue(f, fu); // fDao.getFieldValue(f);
                    LogUtil.getLog(getClass()).info("saveDAO: value=" + s + " fieldName=" + f);
                    if (s!=null) {
                        fieldv.set(i, s);
                    }
                }
            }
            // 重组为算式
            Iterator ir = fieldv.iterator();
            formula = "";
            while (ir.hasNext()) {
                String s = (String)ir.next();
                formula += s;
            }
            // 计算
            FormulaCalculator fc = new FormulaCalculator(formula);
            double r = fc.getResult();
            ps.setDouble(index, r);
        }
        */
        else if (getType().equals(TYPE_BUTTON)) {
            ps.setString(index, "");
        }
        else if (fieldType==FIELD_TYPE_VARCHAR) {
            ps.setString(index, val);
        }
        else if (fieldType==FIELD_TYPE_TEXT) {
            ps.setString(index, val);
        }
        else if (fieldType==FIELD_TYPE_INT) {
            if (val==null || val.equals("")) {
                ps.setNull(index, java.sql.Types.INTEGER);
            }
            else
                ps.setInt(index, StrUtil.toInt(val));
        }
        else if (fieldType==FIELD_TYPE_LONG) {
            if (val==null || val.equals("")) {
                ps.setNull(index, java.sql.Types.BIGINT);
            }
            else
                ps.setLong(index, StrUtil.toLong(val));
        }
        else if (fieldType==FIELD_TYPE_BOOLEAN) {
            ps.setString(index, val);
        }
        else if (fieldType==FIELD_TYPE_FLOAT) {
            if (val==null || val.equals("")) {
                ps.setNull(index, java.sql.Types.FLOAT);
            }
            else
                ps.setFloat(index, StrUtil.toFloat(val));
        }
        else if (fieldType==FIELD_TYPE_DOUBLE) {
            if (val==null || val.equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else
                ps.setDouble(index, StrUtil.toDouble(val));
        }
        else if (fieldType==FIELD_TYPE_PRICE) {
            if (val==null || val.equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            }
            else
                ps.setDouble(index, StrUtil.toDouble(val));
        }
        else if (fieldType==FIELD_TYPE_DATE) {
            java.util.Date d = null;
            d = DateUtil.parse(val, FormField.FORMAT_DATE);
            if (d==null)
                ps.setDate(index, null);
            else {
                ps.setDate(index, new java.sql.Date(d.getTime()));
            }
        }
        else if (fieldType==FIELD_TYPE_DATETIME) {
            java.util.Date d = null;
            d = DateUtil.parse(val, FormField.FORMAT_DATE_TIME);
            if (d==null)
                ps.setDate(index, null);
            else {
                ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
            }
        }
        else {
            ps.setString(index, val);
        }
    }

    /**
     * 用于FormDAO中保存表单域的值，根据字段类型，得出相应的字符串
     * @return String
     */
    public void saveDAO(FormDAO fDao, PreparedStatement ps, int index, int flowId, FormDb fd, FileUpload fu) throws SQLException {
        // 在2.2版中增加表单中的字段类型后，将判断getType为TYPE_MACRO类型与其它类型区别开
        if (getType().equals(TYPE_MACRO)) {
            // LogUtil.getLog(getClass()).info("saveDAO1 " + getName() + " getType()=" + getType() + " getMacroType=" + this.getMacroType());
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(getMacroType());
            if (mu==null) {
                DebugUtil.e(getClass(), "saveDAO", "MacroCtl is not exist, macroType=" + getMacroType());
        		// LogUtil.getLog(getClass()).error("MacroCtl is not exist, macroType=" + getMacroType());
            }
            else {
                IFormMacroCtl iFormMacroCtl = mu.getIFormMacroCtl();
	            value = (String) iFormMacroCtl.getValueForSave(this, flowId, fd, fu);
                fieldType = iFormMacroCtl.getFieldType(this);
                if (fieldType == FormField.FIELD_TYPE_INT
                        || fieldType == FormField.FIELD_TYPE_LONG
                        || fieldType == FormField.FIELD_TYPE_PRICE
                        || fieldType == FormField.FIELD_TYPE_FLOAT
                        || fieldType == FormField.FIELD_TYPE_DOUBLE) {
                    if (value==null || "".equals(value)) {
                        ps.setNull(index, java.sql.Types.INTEGER);
                    } else {
                        if (fieldType == FormField.FIELD_TYPE_INT) {
                            ps.setInt(index, StrUtil.toInt(value));
                        } else if (fieldType == FormField.FIELD_TYPE_LONG) {
                            ps.setLong(index, StrUtil.toLong(value));
                        }
                        else if (fieldType == FormField.FIELD_TYPE_FLOAT) {
                            ps.setFloat(index, StrUtil.toFloat(value));
                        }
                        else {
                            ps.setDouble(index, StrUtil.toDouble(value));
                        }
                    }
                }
                else if (fieldType == FIELD_TYPE_DATE) {
                    java.util.Date d = null;
                    d = DateUtil.parse(getValue(), FormField.FORMAT_DATE);
                    if (d == null) {
                        ps.setDate(index, null);
                    } else {
                        ps.setDate(index, new java.sql.Date(d.getTime()));
                    }
                } else if (fieldType == FIELD_TYPE_DATETIME) {
                    String dateStr = "";
                    // 手机端有可能会传来没有秒的格式，如：2018-10-29 10:55，长度为16
                    if (getValue()!=null && getValue().length() == 16) {
                        dateStr = getValue() + ":00";
                    } else if (getValue()!=null && getValue().length() == 10) {
                        // 2018-10-29
                        dateStr = getValue() + " 00:00:00";
                    } else {
                        dateStr = getValue();
                    }
                    java.util.Date d = DateUtil.parse(dateStr, FormField.FORMAT_DATE_TIME);
                    // System.out.println(getClass() + " getValue()=" + getValue() + " d=" + d);
                    if (d == null) {
                        ps.setDate(index, null);
                    } else {
                        ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
                    }
                }
                else {
                    ps.setString(index, value);
                }
            }
        }
        else if (getType().equals(TYPE_SQL)) {
            String sql = getDefaultValue();
            if (!sql.equals("")) {
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord)ri.next();
                    ps.setString(index, rr.getString(1));
                }
            }
            else {
                ps.setString(index, "");
            }
        }
        /*
        else if (getType().equals(TYPE_CALCULATOR)) {
            // 计算
            String formula = getDefaultValue();
            // 取出计算式子中的各项，包括操作符及括号
            Vector fieldv = FormulaCalculator.getSymbolsWithBracket(formula);
            int flen = fieldv.size();
            for (int i=0; i<flen; i++) {
                String f = (String)fieldv.elementAt(i);
                // 如果不是操作符
                if (!FormulaCalculator.isOperator(f)) {
                    // 如果是表单中的域，则替换
                    String s = fDao.getFieldValue(f);
                    LogUtil.getLog(getClass()).info("saveDAO: value=" + s + " fieldName=" + f);
                    if (s!=null) {
                        fieldv.set(i, s);
                    }
                }
            }
            // 重组为算式
            Iterator ir = fieldv.iterator();
            formula = "";
            while (ir.hasNext()) {
                String s = (String)ir.next();
                formula += s;
            }
            // 计算
            FormulaCalculator fc = new FormulaCalculator(formula);
            double r = fc.getResult();
            ps.setDouble(index, r);
        }
        */
        else if (getType().equals(TYPE_BUTTON)) {
            ps.setString(index, "");
        }
        else {
            if (fieldType == FIELD_TYPE_VARCHAR) {
                ps.setString(index, getValue());
            } else if (fieldType == FIELD_TYPE_TEXT) {
                ps.setString(index, getValue());
            } else if (fieldType == FIELD_TYPE_INT) {
                if (getValue()==null || getValue().equals("")) {
                    ps.setNull(index, java.sql.Types.INTEGER);
                } else
                    ps.setInt(index, StrUtil.toInt(getValue()));
            } else if (fieldType == FIELD_TYPE_LONG) {
                if (getValue()==null || getValue().equals("")) {
                    ps.setNull(index, java.sql.Types.BIGINT);
                } else
                    ps.setLong(index, StrUtil.toLong(getValue()));
            } else if (fieldType == FIELD_TYPE_BOOLEAN) {
                ps.setString(index, getValue());
            } else if (fieldType == FIELD_TYPE_FLOAT) {
                if (getValue()==null || getValue().equals("")) {
                    ps.setNull(index, java.sql.Types.FLOAT);
                } else
                    ps.setFloat(index, StrUtil.toFloat(getValue()));
            } else if (fieldType == FIELD_TYPE_DOUBLE) {
                if (getValue()==null || getValue().equals("")) {
                    ps.setNull(index, java.sql.Types.DOUBLE);
                } else
                    ps.setDouble(index, StrUtil.toDouble(getValue()));
            } else if (fieldType == FIELD_TYPE_PRICE) {
                if (getValue()==null || getValue().equals("")) {
                    ps.setNull(index, java.sql.Types.DOUBLE);
                } else
                    ps.setDouble(index, StrUtil.toDouble(getValue()));
            } else if (fieldType == FIELD_TYPE_DATE) {
                java.util.Date d = null;
                d = DateUtil.parse(getValue(), FormField.FORMAT_DATE);
                if (d == null)
                    ps.setDate(index, null);
                else {
                    ps.setDate(index, new java.sql.Date(d.getTime()));
                }
            } else if (fieldType == FIELD_TYPE_DATETIME) {
            	String dateStr = "";
            	// 手机端有可能会传来没有秒的格式，如：2018-10-29 10:55，长度为16
				if (getValue()!=null && getValue().length() == 16) {
					dateStr = getValue() + ":00";
				} else if (getValue()!=null && getValue().length() == 10) {
					// 2018-10-29
					dateStr = getValue() + " 00:00:00";
				} else {
					dateStr = getValue();
				}
                java.util.Date d = DateUtil.parse(dateStr, FormField.FORMAT_DATE_TIME);
                // System.out.println(getClass() + " getValue()=" + getValue() + " d=" + d);
                if (d == null)
                    ps.setDate(index, null);
                else {
                    ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
                }
            }
            else
                ps.setString(index, getValue());
        }
        // LogUtil.getLog(getClass()).info("saveDAO3 fieldType=" + fieldType + " name=" + getName() + " getType().equals(TYPE_MACRO)=" + getType().equals(TYPE_MACRO) + " getType()=" + getType() + " getMacroType=" + this.getMacroType());
    }

    /**
     * 保存智能表单模块表单中的数据
     * @param ps PreparedStatement
     * @param index int
     * @param formDAOId int
     * @param fd FormDb
     * @param fu FileUpload
     * @throws SQLException
     */
    public void saveDAOVisual(com.redmoon.oa.visual.FormDAO fDao, PreparedStatement ps, int index, long formDAOId, FormDb fd, FileUpload fu) throws SQLException {
        // 在2.2版中增加表单中的字段类型后，将判断getType为TYPE_MACRO类型与其它类型区别开
        if (getType().equals(TYPE_MACRO)) {
            // LogUtil.getLog(getClass()).info("saveDAO1 " + getName() + " getType()=" + getType() + " getMacroType=" + this.getMacroType());
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(getMacroType());
            IFormMacroCtl iFormMacroCtl = mu.getIFormMacroCtl();
            value = (String)iFormMacroCtl.getValueForSave(this, fd, formDAOId, fu);
            fieldType = iFormMacroCtl.getFieldType(this);
            if (fieldType == FormField.FIELD_TYPE_INT
                    || fieldType == FormField.FIELD_TYPE_LONG
                    || fieldType == FormField.FIELD_TYPE_PRICE
                    || fieldType == FormField.FIELD_TYPE_FLOAT
                    || fieldType == FormField.FIELD_TYPE_DOUBLE) {
                if (value==null || "".equals(value)) {
                    ps.setNull(index, java.sql.Types.INTEGER);
                } else {
                    if (fieldType == FormField.FIELD_TYPE_INT) {
                        ps.setInt(index, StrUtil.toInt(value));
                    } else if (fieldType == FormField.FIELD_TYPE_LONG) {
                        ps.setLong(index, StrUtil.toLong(value));
                    }
                    else if (fieldType == FormField.FIELD_TYPE_FLOAT) {
                        ps.setFloat(index, StrUtil.toFloat(value));
                    }
                    else {
                        ps.setDouble(index, StrUtil.toDouble(value));
                    }
                }
            }
            else if (fieldType == FIELD_TYPE_DATE) {
                java.util.Date d = null;
                d = DateUtil.parse(getValue(), FormField.FORMAT_DATE);
                if (d == null) {
                    ps.setDate(index, null);
                } else {
                    ps.setDate(index, new java.sql.Date(d.getTime()));
                }
            } else if (fieldType == FIELD_TYPE_DATETIME) {
                String dateStr = "";
                // 手机端有可能会传来没有秒的格式，如：2018-10-29 10:55，长度为16
                if (getValue()!=null && getValue().length() == 16) {
                    dateStr = getValue() + ":00";
                } else if (getValue()!=null && getValue().length() == 10) {
                    // 2018-10-29
                    dateStr = getValue() + " 00:00:00";
                } else {
                    dateStr = getValue();
                }
                java.util.Date d = DateUtil.parse(dateStr, FormField.FORMAT_DATE_TIME);
                // System.out.println(getClass() + " getValue()=" + getValue() + " d=" + d);
                if (d == null) {
                    ps.setDate(index, null);
                } else {
                    ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
                }
            }
            else {
                ps.setString(index, value);
            }
        }
        else if (getType().equals(TYPE_SQL)) {
            String sql = getDefaultValue();
            if (!"".equals(sql)) {
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord)ri.next();
                    ps.setString(index, rr.getString(1));
                }
            }
            else {
                ps.setString(index, "");
            }
        }
        else if (getType().equals(TYPE_BUTTON)) {
            ps.setString(index, "");
        }
        /*
        else if (getType().equals(TYPE_CALCULATOR)) {
            // 计算
            String formula = getDefaultValue();
            // 取出计算式子中的各项，包括操作符及括号
            Vector fieldv = FormulaCalculator.getSymbolsWithBracket(formula);
            int flen = fieldv.size();
            for (int i=0; i<flen; i++) {
                String f = (String)fieldv.elementAt(i);
                // 如果不是操作符
                if (!FormulaCalculator.isOperator(f)) {
                    // 如果是表单中的域，则替换
                    String s = fDao.getFieldValue(f);
                    LogUtil.getLog(getClass()).info("saveDAO: value=" + s + " fieldName=" + f);
                    if (s!=null) {
                        fieldv.set(i, s);
                    }
                }
            }
            // 重组为算式
            Iterator ir = fieldv.iterator();
            formula = "";
            while (ir.hasNext()) {
                String s = (String)ir.next();
                formula += s;
            }
            // 计算
            FormulaCalculator fc = new FormulaCalculator(formula);
            double r = fc.getResult();
            ps.setDouble(index, r);
        }
        */
        else {
            if (fieldType == FIELD_TYPE_VARCHAR) {
                ps.setString(index, getValue());
            } else if (fieldType == FIELD_TYPE_TEXT) {
                ps.setString(index, getValue());
            } else if (fieldType == FIELD_TYPE_INT) {
                if (getValue()==null || "".equals(getValue())) {
                    ps.setNull(index, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(index, StrUtil.toInt(getValue()));
                }
            } else if (fieldType == FIELD_TYPE_LONG) {
                if (getValue()==null || "".equals(getValue())) {
                    ps.setNull(index, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(index, StrUtil.toLong(getValue()));
                }
            } else if (fieldType == FIELD_TYPE_BOOLEAN) {
                ps.setString(index, getValue());
            } else if (fieldType == FIELD_TYPE_FLOAT) {
                if (getValue()==null || "".equals(getValue())) {
                    ps.setNull(index, java.sql.Types.FLOAT);
                } else {
                    ps.setFloat(index, StrUtil.toFloat(getValue()));
                }
            } else if (fieldType == FIELD_TYPE_DOUBLE) {
                if (getValue()==null || "".equals(getValue())) {
                    ps.setNull(index, java.sql.Types.DOUBLE);
                } else {
                    ps.setDouble(index, StrUtil.toDouble(getValue()));
                }
            } else if (fieldType == FIELD_TYPE_PRICE) {
                if (getValue()==null || "".equals(getValue())) {
                    ps.setNull(index, java.sql.Types.DOUBLE);
                } else {
                    ps.setDouble(index, StrUtil.toDouble(getValue()));
                }
            } else if (fieldType == FIELD_TYPE_DATE) {
                java.util.Date d = null;
                d = DateUtil.parse(getValue(), FormField.FORMAT_DATE);
                if (d == null)
                    ps.setDate(index, null);
                else {
                    ps.setDate(index, new java.sql.Date(d.getTime()));
                }
            } else if (fieldType == FIELD_TYPE_DATETIME) {
            	String dateStr = "";
            	// 手机端有可能会传来没有秒的格式，如：2018-10-29 10:55，长度为16
				if (getValue()!=null && getValue().length() == 16) {
					dateStr = getValue() + ":00";
				} else if (getValue()!=null && getValue().length() == 10) {
					// 2018-10-29
					dateStr = getValue() + " 00:00:00";
				} else {
					dateStr = getValue();
				}
            	
                java.util.Date d = DateUtil.parse(dateStr, FormField.FORMAT_DATE_TIME);
                // System.out.println(getClass() + " getValue()=" + getValue() + " d=" + d);
                if (d == null)
                    ps.setDate(index, null);
                else {
                    ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
                }
            } else
                ps.setString(index, getValue());
        }
        // LogUtil.getLog(getClass()).info("saveDAO3 fieldType=" + fieldType + " name=" + getName() + " getType().equals(TYPE_MACRO)=" + getType().equals(TYPE_MACRO) + " getType()=" + getType() + " getMacroType=" + this.getMacroType());
    }

    /**
     * 保存智能表单模块表单中的数据，用于FormDAO.save()
     * @param ps PreparedStatement
     * @param index int
     * @throws SQLException
     */
    public void saveDAOVisual(PreparedStatement ps, int index) throws SQLException {
        String v = StrUtil.getNullStr(getValue());
        if (fieldType == FIELD_TYPE_INT) {
            if (v.equals("")) {
                ps.setNull(index, java.sql.Types.INTEGER);
            } else
                ps.setInt(index, StrUtil.toInt(getValue()));
        } else if (fieldType == FIELD_TYPE_LONG) {
            if (v.equals("")) {
                ps.setNull(index, java.sql.Types.BIGINT);
            } else
                ps.setLong(index, StrUtil.toLong(getValue()));
        } else if (fieldType == FIELD_TYPE_FLOAT) {
            if (v.equals("")) {
                ps.setNull(index, java.sql.Types.FLOAT);
            } else
                ps.setFloat(index, StrUtil.toFloat(getValue()));
        } else if (fieldType == FIELD_TYPE_DOUBLE) {
            if (v.equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            } else
                ps.setDouble(index, StrUtil.toDouble(getValue()));
        } else if (fieldType == FIELD_TYPE_PRICE) {
            if (v.equals("")) {
                ps.setNull(index, java.sql.Types.DOUBLE);
            } else
                ps.setDouble(index, StrUtil.toDouble(getValue()));
        } else if (fieldType == FIELD_TYPE_DATE) {
            java.util.Date d = null;
            d = DateUtil.parse(getValue(), FormField.FORMAT_DATE);

            LogUtil.getLog(getClass()).info(" ff.getName()=" + getName() + " getValue=" + getValue());

            if (d == null)
                ps.setDate(index, null);
            else {
                ps.setDate(index, new java.sql.Date(d.getTime()));
            }
        } else if (fieldType == FIELD_TYPE_DATETIME) {
        	String dateStr = "";
        	// 手机端有可能会传来没有秒的格式，如：2018-10-29 10:55，长度为16
			if (getValue()!=null && getValue().length() == 16) {
				dateStr = getValue() + ":00";
			} else if (getValue()!=null && getValue().length() == 10) {
				// 2018-10-29
				dateStr = getValue() + " 00:00:00";
			} else {
				dateStr = getValue();
			}
            java.util.Date d = DateUtil.parse(dateStr,
                                              FormField.FORMAT_DATE_TIME);
            // System.out.println(getClass() + " getValue()=" + getValue() + " d=" + d);
            if (d == null)
                ps.setDate(index, null);
            else {
                ps.setTimestamp(index, new java.sql.Timestamp(d.getTime()));
            }
        } else
            ps.setString(index, getValue());

        // LogUtil.getLog(getClass()).info("saveDAO3 fieldType=" + fieldType + " name=" + getName() + " getType().equals(TYPE_MACRO)=" + getType().equals(TYPE_MACRO) + " getType()=" + getType() + " getMacroType=" + this.getMacroType());
    }

    /**
     * 得到创建字段的SQL语句
     * 20070425
     * MYSQL的极限长度 innodb表中的varchar()合计长度不能超过65535，因此将varchar(250)改为varchar(100)
     * ERROR http-80-Processor23 com.redmoon.oa.flow.FormDb - create:Row size too large. The maximum row size for the used table type, not counting BLOBs, is 65535. You have to change some columns to TEXT or BLOBs. Now transaction rollback
     * @return String
     */
    /*
    public String toStrForCreate() {
        String typeStr = "";
        if (type.equals(this.TYPE_MACRO)) {
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(macroType);
            typeStr = mu.getFieldType();
        }
        else if (fieldType==FIELD_TYPE_VARCHAR) {
            typeStr = "varchar(100)";
        }
        else if (fieldType==FIELD_TYPE_INT) {
            typeStr = "INTEGER";
        }
        else if (fieldType==FIELD_TYPE_LONG) {
            typeStr = "BIGINT";
        }
        else if (fieldType==FIELD_TYPE_FLOAT) {
            typeStr = "FLOAT";
        }
        else if (fieldType==FIELD_TYPE_DOUBLE) {
            typeStr = "DOUBLE";
        }
        else if (fieldType==FIELD_TYPE_PRICE) {
            typeStr = "DOUBLE";
        }
        else if (fieldType==FIELD_TYPE_BOOLEAN) {
            typeStr = "char(1)";
        }
        else if (fieldType==FIELD_TYPE_TEXT) {
            typeStr = "text";
        }
        else if (fieldType==FIELD_TYPE_DATE) {
            typeStr = "date";
        }
        else if (fieldType==FIELD_TYPE_DATETIME) {
            typeStr = "datetime";
        }
        else
            typeStr = "varchar(100)";

        String defaultStr = StrUtil.sqlstr("");
        if (defaultValue != null) {
            // System.out.println("FormField.java defaultValue=" + defaultValue +
            //                   " type=" + type);
            if (type.equals(TYPE_DATE)) {
                if (defaultValue.equals(DATE_CURRENT) || defaultStr.equals(""))
                    defaultStr = StrUtil.sqlstr("0000-00-00");
                else
                    defaultStr = StrUtil.sqlstr(defaultValue);
            } else if (type.equals(TYPE_DATE_TIME)) {
                if (defaultValue.equals(DATE_CURRENT))
                    defaultStr = StrUtil.sqlstr("");
                else
                    defaultStr = StrUtil.sqlstr(defaultValue);
            } else
                defaultStr = StrUtil.sqlstr(defaultValue);
        }
        String str = "";
        if (!typeStr.equals("text"))
            str = "`" + name + "` " + typeStr + " default " + defaultStr +
                  " COMMENT " + StrUtil.sqlstr(title);
        else
            str = "`" + name + "` " + typeStr + " COMMENT " +
                  StrUtil.sqlstr(title);
        // System.out.println(getClass() + " toStrForCreate name=" + name + " fieldType=" + fieldType);
        // System.out.println(getClass() + " toStrForCreate:" + str);
        return str;
    }
    */

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static String toHtml(String str) {
        if (str == null || str.equals(""))
            return "";
        java.lang.StringBuffer buf = new java.lang.StringBuffer(str.length() +
                6);
        char ch = ' ';
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (ch == '<')
                buf.append("&lt;");
            else {
                if (ch == '>')
                    buf.append("&gt;");
                else {
                    if (ch == ' ')
                        buf.append("&nbsp;");
                    else {
                        if (ch == '\n')
                            buf.append("<br>");
                        else {
                            if (ch == '\'')
                                buf.append("&#039;");
                            else {
                                if (ch == '\"')
                                    buf.append("&quot;");
                                else
                                    buf.append(ch);
                            }
                        }
                    }
                }
            }
        }
        str = buf.toString();
        return str;
    }

    /**
      * 获取用来保存控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
      * @return String
      */
     /**
      *
      * @param request HttpServletRequest
      * @param IFormDao IFormDAO 当本方法用于visula.Render中的rendForAdd时，IFormDao为null
      * @param ffFromDb FormField
      * @param formElementId String
      * @return String
      */
     public static String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ffFromDb,
                                               String formElementId) {
         // 此处修改是为了使日期型控件，能够在被禁用时，且原来为空值时，继续为空
         // 而当能被写时，原来为空值时，则显示当前值，而当原来有值时，则值不变
         // 为达到此效果，禁用了getDefaultValue()中初始化为当前值，因为这样会使得数据库中被写入当前值
         FormField ffDate = null;
         try {
             ffDate = (FormField) ffFromDb.clone();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         if (ffFromDb.getType().equals(FormField.TYPE_DATE) ||
             ffFromDb.getType().equals(FormField.TYPE_DATE_TIME)) {
             // LogUtil.getLog("FormField").info("getSetCtlValueScript:" + ffFromDb.getTitle() + "=" + ffFromDb.getValue() + " getDefaultValueRaw=" + ffFromDb.getDefaultValueRaw());

             // 如果为日期型，且从数据库中取出的值为空
             if (StrUtil.getNullStr(ffFromDb.getValue()).equals("")) {
                 // 此时ff是来自于数据库，值getDefaultValueRaw为空，则表示默认值为空
                 if (ffFromDb.getDefaultValueRaw().equals(DATE_CURRENT)) {
                     if (ffFromDb.getType().equals(FormField.TYPE_DATE_TIME)) {
                         ffDate.setValue(DateUtil.format(new java.util.Date(),
                                 FORMAT_DATE_TIME));
                     } else {
                         ffDate.setValue(DateUtil.format(new java.util.Date(),
                                 FORMAT_DATE));
                     }
                    //  LogUtil.getLog("FormField").info("getSetCtlValueScript2:" + ffFromDb.getTitle() + "=" + ffFromDb.getValue());
                 }
             }
         }

         if (ffFromDb.getType().equals(FormField.TYPE_DATE)) {
             java.util.Date dt = null;
             try {
                 dt = DateUtil.parse(ffDate.getValue(), FormField.FORMAT_DATE);
             } catch (Exception e) {
                 LogUtil.getLog("com.redmoon.oa.flow.FormField").error("rend0:" +
                         e.getMessage());
             }
             String d = DateUtil.format(dt, FormField.FORMAT_DATE);
             String str = "setCtlValue('" + ffFromDb.getName() + "', '" + ffFromDb.getType() +
                          "', '" +
                          d + "');\n";
             return str;
         }
         else if (ffFromDb.getType().equals(FormField.TYPE_DATE_TIME)) {
			java.util.Date dt = null;
			try {
				dt = DateUtil.parse(ffDate.getValue(),
						FormField.FORMAT_DATE_TIME);
			} catch (Exception e) {
				LogUtil.getLog("com.redmoon.oa.flow.FormField").error(
						"rend1:" + e.getMessage());
			}
			String d = DateUtil.format(dt, FormField.FORMAT_DATE);
			String t = DateUtil.format(dt, "HH:mm:ss");
			String str = "if (o('" + ffFromDb.getName()
					+ "_time')!= null){setCtlValue('" + ffFromDb.getName()
					+ "', '" + ffFromDb.getType() + "', '" 
					+ DateUtil.format(dt, FormField.FORMAT_DATE_TIME) + "');\n"
					+ "setCtlValue('" + ffFromDb.getName() + "_time" + "', '"
					+ ffFromDb.getType() + "', '" + t
					+ "');\n}else{setCtlValue('" + ffFromDb.getName() + "', '"
					+ ffFromDb.getType() + "', '"
					+ DateUtil.format(dt, FormField.FORMAT_DATE_TIME)
					+ "');\n}";
			return str;
         }
         else {
             if (ffFromDb.getType().equals(FormField.TYPE_BUTTON)) {
                 // 生成脚本
                 String script = ffFromDb.getDefaultValueRaw();
				 // 20200419 向下兼容
                 if (script==null || "".equals(script)) {
                     script = ffFromDb.getDescription();
                 }
                 // LogUtil.getLog("com.redmoon.oa.flow.FormField").info("script=" + script);
                 // 替换脚本中的值
                 String pat = "\\{\\$([^\\} ]+)\\}";

                 Pattern p = Pattern.compile(pat,
                                             Pattern.DOTALL |
                                             Pattern.CASE_INSENSITIVE);
                 Matcher m = p.matcher(script);
                 boolean result = m.find();
                 while (result) {
                     String fieldName = m.group(1);
                     String value = "";
                     if (fieldName.equals("id")) {
                         value = String.valueOf(IFormDao.getIdentifier());
                     } else if (fieldName.equals("cwsId")) {
                         value = IFormDao.getCwsId();
                     }
                     else if (fieldName.equals("flowId")) {
                         value = String.valueOf(IFormDao.getFlowId());
                     }
                     else if (fieldName.startsWith("request.")) {
                         String f = fieldName.substring(8);
                         value = ParamUtil.get(request, f);
                         if (value.equals("")) {
                             value = StrUtil.getNullStr((String) request.getAttribute(f));
                         }
                     } else {
                         if (IFormDao != null) {
                             LogUtil.getLog("com.redmoon.oa.flow.FormField").info(
                                     "IFormDao.getFieldValue(fieldName)=" + fieldName +
                                     "=" + IFormDao.getFieldValue(fieldName));
                             value = StrUtil.UrlEncode(StrUtil.getNullStr(
                                     IFormDao.
                                     getFieldValue(fieldName)));
                         }
                     }
                     if (value==null) {
                         LogUtil.getLog(FormField.class).info("script=" + script + " fieldName=" + fieldName + " value=" + value);
                         value = "";
                     }
                     script = m.replaceFirst(value);
                     m = p.matcher(script);
                     result = m.find();
                 }
                 // 当处于visual模块的添加页面时，IFormDao=null, 不显示button
                 // return formElementId + "." + ffFromDb.getName() + ".style.display='none';";

                // 考虑到如果以前的表单中没有按钮，那么新增按钮后，以往表单不能报错误，所以要判断一下
                String str = "if (" + formElementId + "." + ffFromDb.getName() + ")\n";
                str += "o('" + ffFromDb.getName() + "').onclick=function(){" + script + "}\n";
                return str;
            } else {
                 String str = "setCtlValue('" + ffFromDb.getName() + "', '" +
                              ffFromDb.getType() +
                              "', " + 
                              "o('cws_textarea_" + ffFromDb.getName() + "').value);\n";
                 return str;
             }
         }

     }

     /**
      * 取得用来保存控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
      * @return String
      */
     public static String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ffFromDb) {
         return getOuterHTMLOfElementWithRAWValue(request, ffFromDb) + getOuterHTMLOfElementWithHTMLValue(request, ffFromDb);
     }

     /**
      * 取得用来保存控件原始值的表单中的HTML元素，通常为textarea
      * @return String
      */
     public static String getOuterHTMLOfElementWithRAWValue(HttpServletRequest request, FormField ff) {
         // LogUtil.getLog(FormField.class).info(ff.getType() + " name=" + ff.getName() + " value =" + ff.getValue());
         if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
             // 使计算控件不显示初始值
             String val = StrUtil.getNullStr(ff.getValue());
             if ("".equals(val) || val.equals(ff.getDescription())) {
                 return "<textarea style='display:none' id='cws_textarea_" +
                 ff.getName() + "' name='cws_textarea_" + ff.getName() +
                 "'></textarea>";
             }
         }

         String val;
         // 使价格型控件在编辑状态时，也能显示为两位小数
         if (ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
             val = NumberUtil.round(StrUtil.toDouble(ff.getValue(), 0), 2);
         }
         else {
             val = StrUtil.getNullStr(ff.getValue());
         }
         return "<textarea style='display:none' id='cws_textarea_" + ff.getName() + "' name='cws_textarea_" + ff.getName() + "'>" + val + "</textarea>";
     }

     /**
      * 当在模块中添加记录时，置默认值，而getOuterHTMLOfElementsWithRAWValueAndHTMLValue不能置默认值，只能用于编辑
      * @param request HttpServletRequest
      * @param ffFromDb FormField
      * @return String
      */
     public static String getOuterHTMLOfElementsWithRAWValueAndHTMLValueForVisualAdd(HttpServletRequest request, FormField ffFromDb) {
         return getOuterHTMLOfElementWithRAWValueForVisualAdd(request, ffFromDb) + getOuterHTMLOfElementWithHTMLValue(request, ffFromDb);
     }

     /**
      * 当在模块中添加记录时，用来置默认值
      * @param request HttpServletRequest
      * @param ff FormField
      * @return String
      */
     public static String getOuterHTMLOfElementWithRAWValueForVisualAdd(HttpServletRequest request, FormField ff) {
         if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
             // 使计算控件不显示初始值
             String str =
                     "<textarea style='display:none' id='cws_textarea_" +
                     ff.getName() + "' name='cws_textarea_" + ff.getName() +
                     "'></textarea>";
             return str;
         }
         
         // String str = "<textarea style='display:none' id='cws_textarea_" + ff.getName() + "' name='cws_textarea_" + ff.getName() + "'>" + StrUtil.getNullStr(ff.getDefaultValueRaw())  + "</textarea>";
         String str = "<textarea style='display:none' id='cws_textarea_" + ff.getName() + "' name='cws_textarea_" + ff.getName() + "'>" + StrUtil.getNullStr(ff.getDefaultValue())  + "</textarea>";
         return str;
     }

     /**
      * 当表单域不可写或显示详情页时，用来取得用来显示控件值的字符串
      * @param request HttpServletRequest
      * @param ff FormField
      * @return String
      */
     public static String getOuterHTMLOfElementWithHTMLValue(HttpServletRequest request, FormField ff) {
         //  20130710 fgf 应大亚要求修改
    	 // 如果数字大于10的7次方或者小于10的-3次方，就会使用科学计数法
    	 // 根据字段类型，自动转换为精确到小数点后两位的字符串
    	 String val = ff.getValue();
    	 if (val != null && (ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_PRICE)) {
    		 double v = StrUtil.toDouble(ff.getValue(), 0.0); 
    		 if (v>Math.pow(10, 7)) {
    			 val = NumberUtil.roundRMB(v);
    		 }
    	 }
    	 else {
    		 val = FormField.toHtml(val);
    	 }
    	 String str = "<span style='display:none' id='cws_span_" + ff.getName() +
                      "' name='cws_span_" + ff.getName() + "'>" +
                      val +
                      "</span>";
         return str;
     }

     /**
      * 当report时，取得用来替换控件的脚本
      * @param ff FormField
      * @return String
     */
     public static String getReplaceCtlWithValueScript(FormField ff) {
         String str = "";
         str = "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "'," + "cws_span_" + ff.getName() + ".innerHTML);\n";
         /*if (ff.getType().equals(FormField.TYPE_DATE)) {
             String v = ff.getValue();
             Date d = DateUtil.parse(v, FormField.FORMAT_DATE);
             v = DateUtil.format(d, "yyyy-MM-dd");
             str = "ReplaceCtlWithValue('" + ff.getName() + "', '" +
                   ff.getType() + "'," + "'" + v + "');\n";
         }
         else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
             String v = ff.getValue();
             Date d = DateUtil.parse(v, FormField.FORMAT_DATE + " HH:mm:ss");
             v = DateUtil.format(d, "yyyy-MM-dd HH:mm:ss");
             str = "ReplaceCtlWithValue('" + ff.getName() + "', '" +
                   ff.getType() + "'," + "'" + v + "');\n";
         }
         else {
             str = "ReplaceCtlWithValue('" + ff.getName() + "', '" +
                   ff.getType() + "'," + "cws_span_" + ff.getName() +
                   ".innerHTML);\n";
         }*/
         return str;
     }

     /**
      * 获取用来保存控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
      * @return String
      */
     public static String getDisableCtlScript(FormField ff, String formElementId) {
         String str = "";

         // System.out.println("com.redmoon.oa.flow.FormField ff.getName()=" + ff.getName() + " ff.getType()=" + ff.getType() + " ff.getValue()=" + ff.getValue());

         if (ff.getType().equals(FormField.TYPE_DATE)) {
             String v = ff.getValue();
             Date d = DateUtil.parse(v, FormField.FORMAT_DATE);
             v = DateUtil.format(d, "yyyy-MM-dd");
             str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                          "'," + "'" + v + "', " +
                          "o('cws_textarea_" + ff.getName() +
                          "').value);\n";
         }
         else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
             java.util.Date dt = null;
             try {
                 dt = DateUtil.parse(ff.getValue(),
                         FormField.FORMAT_DATE_TIME);
             }
             catch (Exception e) {
                 LogUtil.getLog("com.redmoon.oa.flow.FormField").error("getDisableCtlScript:" + e.getMessage());
             }
             String d = DateUtil.format(dt, FormField.FORMAT_DATE);
             String t = DateUtil.format(dt, "HH:mm:ss");
             str = "if(o('" + ff.getName() + "_time" + "')!=null){DisableCtl('" + 
             		ff.getName() + "', '" + ff.getType() + 
             		"', '" + d + "','" + d + "');\nDisableCtl('" + 
             		ff.getName() + "_time" + "', '" + ff.getType() +
                     "', '" + t + "','" + t + "');}else{DisableCtl('" + 
             		ff.getName() + "', '" + ff.getType() + 
             		"', '" + DateUtil.format(dt, "yyyy-MM-dd HH:mm:ss") + 
             		"','" + ff.getValue() + "');}\n";
         }
         else {
             str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                          "', " + "o('cws_span_" + ff.getName() + "').innerHTML, " +
                          "o('cws_textarea_" + ff.getName() +
                          "').value);\n";
         }
         return str;
    }

    /**
     * 获取用来隐藏控件的脚本
     * @return String
     */
    public static String getHideCtlScript(FormField ff, String formElementId) {
        String str = "";

        // System.out.println("com.redmoon.oa.flow.FormField ff.getName()=" + ff.getName() + " ff.getType()=" + ff.getType() + " ff.getValue()=" + ff.getValue());
        if (ff==null) {
        	LogUtil.getLog(FormField.class).error("getHideCtlScript:字段已被删除");
        	return "";
        }
        if (FormField.TYPE_DATE_TIME.equals(ff.getType())) {
            str += "HideCtl('" + ff.getName() + "', '" + ff.getType() +
                    "', '" + ff.getMacroType() + "');\n";
            str += "if(o('" + ff.getName() + "_time" + "')!=null){HideCtl('" + ff.getName() + "_time" + "', '" +
                    ff.getType() +
                    "', '" + ff.getMacroType() + "');}\n";
        }
        else {
            str = "HideCtl('" + ff.getName() + "', '" + ff.getType() +
                         "', '" + ff.getMacroType() + "');\n";
        }
        return str;
    }

    public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public void setAttribute(Object key, Object val) {
		props.put(key, val);
	}
	
	public Object getAttribute(Object key) {
		return props.get(key);
	}
	
	public void setProps(Map props) {
		this.props = props;
	}
	
	public Map getProps() {
		return props;
	}

	public void setMobileDisplay(boolean mobileDisplay) {
		this.mobileDisplay = mobileDisplay;
	}

	public boolean isMobileDisplay() {
		return mobileDisplay;
	}

	public void setHide(int hide) {
		this.hide = hide;
	}

	public int getHide() {
		return hide;
	}

	/**
	 * @return the moreThan
	 */
	public String getMoreThan() {
		return moreThan;
	}

	/**
	 * @param moreThan the moreThan to set
	 */
	public void setMoreThan(String moreThan) {
		this.moreThan = moreThan;
	}

	public void setMorethanMode(String morethanMode) {
		this.morethanMode = morethanMode;
	}

	public String getMorethanMode() {
		return morethanMode;
	}

	public void setFunc(boolean func) {
		this.func = func;
	}

	public boolean isFunc() {
		return func;
	}

	public void setCssWidth(String cssWidth) {
		this.cssWidth = cssWidth;
	}

	public String getCssWidth() {
		return cssWidth;
	}

	public void setCondType(String condType) {
		this.condType = condType;
	}

	public String getCondType() {
		return condType;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isReadonly() {
		return readonly;
	}

    /**
     * 一般用于在module_list.jsp列中展现
     * @return
     */
	public String convertToHtml() {
	    if (type.equals(TYPE_CHECKBOX)) {
            if (StringUtils.isBlank(present)) {
                return value;
            }
            else {
                String[] ary = StrUtil.split(present, "\\|");
                if (ary.length==2) {
                    // checkbox的value为空或者1，当不选时，值为空（注意导入时会因默认值为0从而生成0，而添加时，不选中则数据库中值为空）
                    if ("1".equals(value)) {
                        return ary[1];
                    }
                    else {
                        return ary[0];
                    }
                }
                else if (ary.length==1) {
                    if ("0".equals(value)) {
                        return present;
                    }
                    else {
                        return "";
                    }
                }
                else {
                    return value;
                }
            }
        }
	    else if (getFieldType()==FormField.FIELD_TYPE_DATETIME || getFieldType()==FormField.FIELD_TYPE_DATE) {
	        if (description!=null && !"".equals(description)) {
	            Date d;
                if (fieldType==FormField.FIELD_TYPE_DATETIME) {
                    d = DateUtil.parse(value, FormField.FORMAT_DATE_TIME);
                }
                else {
                    d = DateUtil.parse(value, FormField.FORMAT_DATE);
                }
                return DateUtil.format(d, description);
            }
	        else {
	            return StrUtil.getNullStr(value);
            }
        }
	    else if (type.equals(TYPE_CALCULATOR)) {
	        FormDb fd = new FormDb();
	        fd = fd.getFormDb(formCode);
            return convertToHtmlForCalculate(fd, value);
        }
	    else {
            if (fieldType==FormField.FIELD_TYPE_PRICE) {
                value = NumberUtil.round(StrUtil.toDouble(value, 0), 2);
            }
            else {
                value = StrUtil.getNullStr(value);
            }
	        return value;
        }
    }

    public String convertToHtmlForCalculate(FormDb fd, String value) {
        FormParser fp = new FormParser();
        // 如果计算控件是四舍五入，小数点后位数digit才有效
        String isroundto5 = fp.getFieldAttribute(fd, this, "isroundto5"); // 0或1
        if ("1".equals(isroundto5)) {
            int digit = StrUtil.toInt(fp.getFieldAttribute(fd, this, "digit"), -1);
            if (digit != -1) {
                return NumberUtil.round(StrUtil.toDouble(value, 0), digit);
            } else {
                return StrUtil.getNullStr(value);
            }
        }
        else {
            return StrUtil.getNullStr(value);
        }
    }

	private String name;
    private String type;
    private String title;
    private String formCode;
    private String macroType = MACRO_NOT;
    private String defaultValue = "";
    private String value;

    /**
     * 数据类型
     */
    private int fieldType = FIELD_TYPE_VARCHAR;
    private String rule;
    private boolean canNull = true;
    private boolean canQuery = true;
    private int orders = 0;
    /**
     * 查询列表中字段显示的宽度
     */
    private int width = 100;

    /**
     * 是否为表单可写域
     */
    private boolean editable = true;
    private boolean canList = false;
    /**
     * 用于流程及手机客户端辅助处理隐藏字段，如在SQLCtl中需用到
     */
    private boolean hidden = false;
    
    private String description;
    
    /**
     * 用于手机端后台处理时赋予属性，如：置属性值flowId，用于getControlText等，暂未用上备用
     */
    private Map props = new HashMap();
    
    /**
     * CRM中判断是否显示于手机
     */
    private boolean mobileDisplay = true;
    
    /**
     * 用于控制字段是否显示，如字段仅作为标志位，或者因表单修改，需要逻辑删除保留历史数据的情况
     */
    private int hide = HIDE_NONE;

    /**
     * 用于比较的字段
     */
    private String moreThan;
    
    /**
     * 用于比较的方式，如：> >= < <= =
     */
    private String morethanMode;
    
    /**
     * 是否为算式
     */
    private boolean func = false;
    
    /**
     * 控件的宽度
     */
    private String cssWidth = "";
    
    /**
     * 查询类型，0表示模糊，用于查询时辅助传值
     */
    private String condType = "0";
    
    private boolean readonly = false;

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    /**
     * 显示，如checkbox为0时，显示否，为1时，显示是，另外，用于保存计算控件的formCode，当为sum型时，对应嵌套表的表单编码
     */
    private String present = "";

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * 是否唯一，可以与其它字段一起限定唯一
     */
    private boolean unique = false;

    /**
     * 嵌套表（或关联模块）中是否唯一
     */
    private boolean uniqueNest = false;

    private boolean helper = false;

    public boolean isHelper() {
        return helper;
    }

    public void setHelper(boolean helper) {
        this.helper = helper;
    }

    /**
     * 根据rule返回varchar字符串型字段的长度
     * @return
     */
    public int getLengthByRule() {
        if (StringUtils.isEmpty(rule)) {
            return TEXT_FIELD_DEFAULT_LENGTH;
        }

        String[] ary = StrUtil.split(rule, ",");
        Pattern pattern = Pattern.compile("([<=]+)(\\d+)");
        if (ary.length == 1) {
            // 如果长度为1，说明只做了必须大于或等于某长度的限定，或只做了必须小于或等于某长度的限定
            Matcher matcher = pattern.matcher(ary[0]);
            if (matcher.find()) {
                String operator = matcher.group(1);
                if ("=".equals(operator) || "<=".equals(operator) || "<".equals(operator)) {
                    return StrUtil.toInt(matcher.group(2), TEXT_FIELD_DEFAULT_LENGTH);
                }
            }
        }
        else {
            Matcher matcher = pattern.matcher(ary[1]);
            if (matcher.find()) {
                /*DebugUtil.i(getClass(), "getLengthByRule0", matcher.group(0));
                DebugUtil.i(getClass(), "getLengthByRule1", matcher.group(1));
                DebugUtil.i(getClass(), "getLengthByRule2", matcher.group(2));*/
                return StrUtil.toInt(matcher.group(2), TEXT_FIELD_DEFAULT_LENGTH);
            }
        }
        return TEXT_FIELD_DEFAULT_LENGTH;
    }

    public boolean isUniqueNest() {
        return uniqueNest;
    }

    public void setUniqueNest(boolean uniqueNest) {
        this.uniqueNest = uniqueNest;
    }
}
