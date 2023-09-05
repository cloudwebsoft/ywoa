package com.redmoon.oa.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.sql.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;

import org.json.*;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.query.QueryScriptUtil;
import com.redmoon.oa.person.UserDb;

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
public class FormQueryReportRender {
	com.redmoon.oa.visual.FormDAO moduleFdao;
	JSONObject jsonTabSetup;
	
    public FormQueryReportRender() {
    }
    
    public FormQueryReportRender(com.redmoon.oa.visual.FormDAO moduleFdao, JSONObject jsonTabSetup) {
    	this.moduleFdao = moduleFdao;
    	this.jsonTabSetup = jsonTabSetup;
    }    

    /**
     * 取得Y轴元素的SQL值，用于遍历Y轴元素时
     * @param fqrcX FormQueryReportCell
     * @param fieldType int
     * @param rr ResultRecord
     * @return String
     */
    public String getSQLValueOfY(FormQueryReportCell fqrcX, int fieldType, ResultRecord rr) {
        String valY = rr.getString(1);

        String sqlValY = StrUtil.sqlstr(valY);
        if (fieldType == FormField.FIELD_TYPE_TEXT || fieldType == FormField.FIELD_TYPE_VARCHAR) {
            ;
        } else if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG ||
                   fieldType == FormField.FIELD_TYPE_BOOLEAN ||
                   fieldType == FormField.FIELD_TYPE_FLOAT ||
                   fieldType == FormField.FIELD_TYPE_DOUBLE ||
                   fieldType == FormField.FIELD_TYPE_PRICE
                ) {
            sqlValY = valY;
        } else if (fieldType == FormField.FIELD_TYPE_DATE) {
            java.util.Date d = rr.getDate(1);
            String dtStr = DateUtil.format(d, "yyyy-MM-dd");
            sqlValY = SQLFilter.getDateStr(dtStr, "yyyy-MM-dd");
        } else if (fieldType == FormField.FIELD_TYPE_DATETIME) {
            java.util.Date d = rr.getDate(1);
            String dtStr = DateUtil.format(d, "yyyy-MM-dd HH:mm:ss");
            sqlValY = SQLFilter.getDateStr(dtStr, "yyyy-MM-dd HH:mm:ss");
        } else
            throw new IllegalArgumentException(fqrcX.getFieldName() + " fieldType=" + fieldType +
                                               " 不支持！");
        return sqlValY;
    }
    
	public String makeSQLWithYFieldValue(String sqlX, String sqlRelX,
			String fieldNameY, String sqlValY) {
		// 拼装上Y轴的字段值
		int p = sqlX.indexOf("where");

		/*
		// 不支持关联查詢，在設計器中便不允許出現关联字段
		// 如果一定要關聯，則用自由SQL
        if (fieldNameY.startsWith("rel.")) {
            FormField ff = fqrcY.getFieldName().substring("rel.".length());
        }
        */
		if (p != -1)
			sqlX += " and " + fieldNameY + "=" + sqlValY;
		else
			sqlX += " where " + fieldNameY + "=" + sqlValY;

		return sqlX;
	}

    public static Vector[] parseCell(String content) {
        String patTd = "<td>(.*?)</td>";
        Pattern pTd = Pattern.compile(patTd,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher mTd = pTd.matcher(content);
        Vector vtX = new Vector();
        Vector vtY = new Vector();
        while (mTd.find()) {
            String tdInnerHtml = mTd.group(1);

            LogUtil.getLog(FormQueryReportCell.class).info("tdInnerHtml=" + tdInnerHtml);

            FormQueryReportCell fqrc = new FormQueryReportCell();

            // tinymce3.x版
            // String pat = "<input( style=\"[^'\">]*\")? name=\"(\\S*)\" value=\"([^>]*?)\"( width=\"([^'\" >]*)\")? .*?fieldname=\"(.*?)\"([^>]*?)/>";

            // tinymce4.x版
            // tdInnerHtml	"&nbsp;<input name="xCol" style="width: 162px; height: 22px;" type="text" size="19" value="[ 审批结果 ]" formula="count" axis="X" fieldname="resu" />" (id=3868)	

            String pat = "<input name=\"(\\S*)\"( style=\"[^'\">]*\")?( type=\"text\")?( size=\".*?\")? value=\"([^>]*?)\"( width=\"([^'\" >]*)\")? .*?fieldname=\"(.*?)\"([^>]*?)/>";

            Pattern p = Pattern.compile(pat,
                                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(tdInnerHtml);

            if (m.find()) {
                String group0 = m.group(0);
                // LogUtil.getLog(FormQueryReportRender.class).info("m.group(0)=" + m.group(0));

                String value = m.group(5);
                int width = StrUtil.toInt(m.group(6), FormQueryReportCell.DEFAULT_WIDTH);
                String fieldName = m.group(8);

                String axis = "X";
                String fieldType = "";
                String formula = "";
                String sumType = "";

                // 对str进一步切分，取得fieldtype、axis
                String patAxis = "axis=\"(.*?)\"";
                Pattern pAxis = Pattern.compile(patAxis,
                                                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher mAxis = pAxis.matcher(group0);
                if (mAxis.find()) {
                    axis = mAxis.group(1);
                }

                LogUtil.getLog(FormQueryReportRender.class).info("group0=" + group0 + " value=" + value + " fieldName=" + fieldName + " axis=" + axis);

                if (axis.equals("X")) {
                    String patFormula = "formula=\"(.*?)\"";
                    Pattern pFormula = Pattern.compile(patFormula,
                                                       Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    Matcher mFormula = pFormula.matcher(group0);
                    if (mFormula.find()) {
                        formula = mFormula.group(1);
                    }

                    String patSumType = "sumType=\"(.*?)\"";
                    Pattern pSumType = Pattern.compile(patSumType,
                                                       Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    Matcher mSumType = pSumType.matcher(group0);
                    if (mSumType.find()) {
                        sumType = mSumType.group(1);
                    }
                } else {
                    String patFieldType = "fieldtype=\"(.*?)\"";
                    Pattern pFieldType = Pattern.compile(patFieldType,
                                                         Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    Matcher mFieldType = pFieldType.matcher(group0);
                    if (mFieldType.find()) {
                        fieldType = mFieldType.group(1);
                    }
                    
                    String patFormula = "formula=\"(.*?)\"";
                    Pattern pFormula = Pattern.compile(patFormula,
                                                       Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    Matcher mFormula = pFormula.matcher(group0);
                    if (mFormula.find()) {
                        formula = mFormula.group(1);
                    }                    
                }

                fqrc.setAxis(axis);
                fqrc.setFieldName(fieldName);
                fqrc.setFieldType(fieldType);
                fqrc.setFormula(formula);
                fqrc.setValue(value);
                fqrc.setWidth(width);
                fqrc.setSumType(sumType);

                LogUtil.getLog(FormQueryReportCell.class).info("axis=" + axis + " fieldName=" + fieldName + " fieldType=" + fieldType + " value=" + value);

                if (axis.equals("X"))
                    vtX.addElement(fqrc);
                else
                    vtY.addElement(fqrc);
            } else
                vtX.addElement(fqrc);
        }
        Vector[] ary = new Vector[2];
        ary[0] = vtX;
        ary[1] = vtY;
        return ary;
    }

    /**
     * 直接显示，未加入SQL列的处理
     * @param request HttpServletRequest
     * @param id long
     * @return String
     * @throws ErrMsgException
     */
    public String rend(HttpServletRequest request, long id) throws ErrMsgException {
        // <input name="yCol" value="Y- 拟稿人" type="text" fieldname="rel.ngr" fieldtype="field" axis="Y" />
        // <input name="xCol" value="秘密等级" type="text" fieldname="rel.dj" formula="sum" axis="X" />

        FormQueryReportDb fqrd = new FormQueryReportDb();
        fqrd = (FormQueryReportDb) fqrd.getQObjectDb(new Long(id));
        String content = fqrd.getString("content");

        // 取得最后一行的tr
        int i = content.lastIndexOf("<tr>");
        int j = content.lastIndexOf("</tr>");

        String cellContent = content.substring(i + 4, j);

        content = content.substring(0, i);

        LogUtil.getLog(getClass()).info("cellContent=" + cellContent);
        Vector[] ary = parseCell(cellContent);
        Vector vtX = ary[0];
        Vector vtY = ary[1];

        if (vtY.size() > 0) {
            // 取得第一个Y轴的元素
            FormQueryReportCell fqrcY = (FormQueryReportCell) vtY.elementAt(0);
            String fieldNameY = fqrcY.getFieldName();
            if (fieldNameY == null) {
                throw new ErrMsgException("Y轴元素设置非法，字段名称解析为空！");
            }
            String sqlY = "";
            FormField ff = null;
            // 取得报表中的查询
            int queryId = fqrd.getInt("query_id");
            FormQueryDb fqd = new FormQueryDb();
            fqd = fqd.getFormQueryDb(queryId);

            // 如果有关联查询，则取得关联查询的SQL语句
            String sqlRelX = "";
            String queryRelated = fqd.getQueryRelated();
            if (!queryRelated.equals("")) {
                // String[] ary = StrUtil.split(queryRelated, ",");
                int queryRelatedId = StrUtil.toInt(queryRelated, -1);
                if (queryRelatedId != -1) {
                	FormSQLBuilder fsb = new FormSQLBuilder();
                    sqlRelX = fsb.getQueryRelated(queryRelatedId);
                    int p = sqlRelX.indexOf("id");
                    sqlRelX = sqlRelX.substring(0, p) + "flowId" + sqlRelX.substring(p + 2);
                }
            }

            // 取得用于遍历Y轴元素的语句
            if (fieldNameY.startsWith("rel.")) {
                if (!queryRelated.equals("")) {
                    int queryRelatedId = StrUtil.toInt(queryRelated, -1);
                    FormQueryDb aqdRelated = fqd.getFormQueryDb(queryRelatedId);
                    FormDb fdRelated = new FormDb();
                    fdRelated = fdRelated.getFormDb(aqdRelated.getTableCode());
                    String relFieldName = fieldNameY.substring("rel.".length());
                    ff = fdRelated.getFormField(relFieldName);
                    sqlY = "select distinct(" + relFieldName + ") from " + FormDb.getTableName(fdRelated.getCode());
                } else {
                    LogUtil.getLog(getClass()).error("查询ID：" + queryRelated + "无关联查询！");
                }
            } else {
                FormDb fd = new FormDb();
                fd = fd.getFormDb(fqd.getTableCode());
                ff = fd.getFormField(fieldNameY);

                sqlY = "select distinct(" + fieldNameY + ") from " + FormDb.getTableName(fqd.getTableCode());
            }

            if (!sqlY.equals("")) {
                String smartSQL = "";
                try {
                	FormSQLBuilder fsb = new FormSQLBuilder();
                    smartSQL = fsb.getSmaryQueryWithoutOrderBy(request, fqd.getId());
                } catch (ErrMsgException ex1) {
                    ex1.printStackTrace();
                }

                // 遍历Y轴的元素
                try {
                    int fieldType = ff.getFieldType();
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sqlY);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        content += "<tr>";
                        content += "<td>" + rr.getString(1) + "</td>";
                        // 通过X轴条件取得X轴上求值的SQL
                        Iterator ir = vtX.iterator();
                        while (ir.hasNext()) {
                            FormQueryReportCell fqrcX = (FormQueryReportCell) ir.next();
                            // 如果为空元素
                            if (fqrcX.getFieldName().equals("")) {
                                content += "<td></td>";
                                continue;
                            }

                            // 替换SQL语句，加入条件fqrcY.getFieldName()=...，将select部分换为formula
                            String func = fqrcX.getFormula();
                            if (func.equals("average")) {
                                func = "sum";
                            }

                            // X轴不应该允许用关联的主表的字段，这样会使得生成SQL语句时难以与smartSQL对应
                            // if (fqrcX.getFieldName().startsWith("rel.")) {
                            String sqlX = smartSQL.replaceFirst("id", func + "(" + fqrcX.getFieldName() + ")");
                            String sqlValY = getSQLValueOfY(fqrcX, fieldType, rr);
                            // 拼装上Y轴的字段值
                            sqlX = makeSQLWithYFieldValue(sqlX, sqlRelX, fqrcY.getFieldName(), sqlValY);

                            LogUtil.getLog(getClass()).info("smartSQL=" + smartSQL + "  sqlX=" + sqlX);
                            // LogUtil.getLog(getClass()).info(getClass() + " sqlX=" + sqlX);
                            // 取得X轴的值
                            ResultIterator riX = jt.executeQuery(sqlX);
                            String tdVal = "";
                            if (riX.hasNext()) {
                                ResultRecord rrX = (ResultRecord) riX.next();
                                if (fqrcX.getFormula().equals("average")) {
                                    double dblVal = rrX.getDouble(1);
                                    String sqlCountX = smartSQL.replaceFirst("id",
                                                                             "count(" + fqrcX.getFieldName() + ")");
                                    sqlCountX = makeSQLWithYFieldValue(sqlCountX, sqlRelX, fqrcY.getFieldName(), sqlValY);
                                    ResultIterator riCountX = jt.executeQuery(sqlCountX);
                                    if (riCountX.hasNext()) {
                                        ResultRecord rrCountX = (ResultRecord) riCountX.next();
                                        int c = rrCountX.getInt(1);
                                        tdVal = "" + NumberUtil.round(dblVal / c, 2);
                                    }
                                } else {
                                    tdVal = rrX.getString(1);
                                }
                            }
                            content += "<td>" + tdVal + "</td>";
                        }
                        content += "</tr>";
                    }
                } catch (SQLException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }
            } else {
                throw new ErrMsgException("Y轴元素的SQL语句为空！");
            }
        }
        content += "</table>";
        return content;
    }

    public static String getSortType(String func) {
        if (func.equals("sum"))
            return "float";
        else if (func.equals("average"))
            return "float";
        else
            return "int";
    }
    
    public static String getSortType(int formFieldType) {
        String sorttype;
        switch (formFieldType) {
        case FormField.FIELD_TYPE_VARCHAR:
            sorttype = "text";
            break;
        case FormField.FIELD_TYPE_TEXT:
            sorttype = "text";
            break;
        case FormField.FIELD_TYPE_INT:
            sorttype = "int";
            break;
        case FormField.FIELD_TYPE_LONG:
            sorttype = "int";
            break;
        case FormField.FIELD_TYPE_BOOLEAN:
            sorttype = "int";
            break;
        case FormField.FIELD_TYPE_FLOAT:
            sorttype = "float";
            break;
        case FormField.FIELD_TYPE_DOUBLE:
            sorttype = "double";
            break;
        case FormField.FIELD_TYPE_DATE:
            sorttype = "date";
            break;
        case FormField.FIELD_TYPE_DATETIME:
            sorttype = "date";
            break;
        case FormField.FIELD_TYPE_PRICE:
            sorttype = "double";
            break;
        default:
            sorttype = "text";
        }
        return sorttype;
    }

    public static String getSortType(FormField ff) {
    	return getSortType(ff.getFieldType());
    }
    
    public String[] getJqGridDesc(HttpServletRequest request, long id) throws ErrMsgException {

        FormQueryReportDb fqrd = new FormQueryReportDb();
        fqrd = (FormQueryReportDb) fqrd.getQObjectDb(new Long(id));
        
        FormQueryDb fqd = new FormQueryDb();
        int queryId = fqrd.getInt("query_id");
        fqd = fqd.getFormQueryDb(queryId);
        
        if (fqd.isScript()) {
        	return getJqGridDescScript(request, fqrd, fqd);
        }
        else {
        	return getJqGridDescNormal(request, fqrd, fqd);
        }
    }    

    /**
     * 显示为jqGrid
     * @param request HttpServletRequest
     * @param id long
     * @return String[] 0为colNames 1为data
     * @throws ErrMsgException
     */
    public String[] getJqGridDescNormal(HttpServletRequest request, FormQueryReportDb fqrd, FormQueryDb fqd) throws ErrMsgException {
        String[] ret = new String[3];

        String content = fqrd.getString("content");

        // 取得最后一行的tr
        int i = content.lastIndexOf("<tr>");
        int j = content.lastIndexOf("</tr>");

        if (i==-1 || j==-1) {
            throw new ErrMsgException("报表格式错误!");
        }

        String cellContent = content.substring(i + 4, j);

        LogUtil.getLog(getClass()).info("cellContent=" + cellContent);
        Vector[] ary = parseCell(cellContent);
        Vector vtX = ary[0];
        Vector vtY = ary[1];
        LogUtil.getLog(getClass()).info("vtY.size=" + vtY.size());

        if (vtY.size() > 0) {
            // 取得第一个Y轴的元素
            FormQueryReportCell fqrcY = (FormQueryReportCell) vtY.elementAt(0);
            String fieldNameY = fqrcY.getFieldName();
            if (fieldNameY == null) {
                throw new ErrMsgException("Y轴元素设置非法，字段名称解析为空！");
            }

            String colNames = StrUtil.sqlstr(fqrcY.getValue());

            String formCode = fqd.getTableCode();
            if (formCode==null || formCode.equals(""))
                throw new ErrMsgException("请先选择查询");

            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            String queryRelated = fqd.getQueryRelated();
            int queryRelatedId = StrUtil.toInt(queryRelated, -1);
            FormQueryDb aqdRelated;
            FormDb fdRelated = new FormDb();
            if (queryRelatedId!=-1) {
                aqdRelated = fqd.getFormQueryDb(queryRelatedId);
                fdRelated = fdRelated.getFormDb(aqdRelated.getTableCode());
            }

            JSONArray jsonAry = new JSONArray();
            JSONObject json = new JSONObject();
            try {
                json.put("name", fieldNameY);
                json.put("index", fieldNameY);
                json.put("width", fqrcY.getWidth());
                if (fieldNameY.startsWith("rel.")) {
                    FormField ff = fdRelated.getFormField(fqrcY.getFieldName().substring("rel.".length()));
                    json.put("sorttype", getSortType(ff));
                }
                else {
                    FormField ff = fd.getFormField(fqrcY.getFieldName());
                    json.put("sorttype", getSortType(ff));
                }
                jsonAry.put(json);
            } catch (JSONException ex2) {
                ex2.printStackTrace();
            }

            String completeStr = "function completeMethod() {";
            String completeStr2 = "";

            Iterator irX = vtX.iterator();
            while (irX.hasNext()) {
                FormQueryReportCell fqrcX = (FormQueryReportCell) irX.next();
                // 有些列在配置时可能为空
                if (fqrcX.getFieldName().equals(""))
                    continue;

                // 生成合计javascript
                // 统计时利用getCol方法，第一个参数为colMode的name值，第二个设为false，否则会返回一个数组而不是但一个数据，第三个是设置统计方式，有'sum','avg'和'count'。
                fqrcX.setSumType("sum");
                if (!fqrcX.getSumType().equals("")) {
                    completeStr += "var sum_" + fqrcX.getFieldName() + "=$(\"#gridTable\").getCol('" + fqrcX.getFieldName() + "',false,'" + fqrcX.getSumType() + "');";
                    // 必须加单引号，因为嵌套表rel.mytitle字段中的.号，会使得json字段串出错
                    completeStr2 += ",'" + fqrcX.getFieldName() + "':sum_" + fqrcX.getFieldName();
                }
                colNames += "," + StrUtil.sqlstr(fqrcX.getValue());
                try {
                    json = new JSONObject();
                    json.put("name", fqrcX.getFieldName());
                    json.put("index", fqrcX.getFieldName());
                    json.put("width", fqrcX.getWidth());

                    json.put("sorttype", getSortType(fqrcX.getFormula()));

                    jsonAry.put(json);
                } catch (JSONException ex3) {
                    ex3.printStackTrace();
                }
            }

            ret[0] = "colNames:[" + colNames + "], colModel:" + jsonAry.toString() + ",";
            if (!completeStr2.equals(""))
                completeStr += "$(\"#gridTable\").footerData('set', { '" + fqrcY.getFieldName() + "': '合计'" + completeStr2 + " });";

            completeStr += "}";
            ret[2] = completeStr;

            // 置Y列
            String sqlY = "";
            FormField ff = null;

            // 如果有关联查询，则取得关联查询的SQL语句
            String sqlRelX = "";
            if (!queryRelated.equals("")) {
                // String[] ary = StrUtil.split(queryRelated, ",");
                if (queryRelatedId != -1) {
                	FormSQLBuilder fsb = new FormSQLBuilder();
                    sqlRelX = fsb.getQueryRelated(queryRelatedId);
                    int p = sqlRelX.indexOf("id");
                    sqlRelX = sqlRelX.substring(0, p) + "flowId" + sqlRelX.substring(p + 2);
                }
            }

            // 取得用于遍历Y轴元素的语句
            if (fieldNameY.startsWith("rel.")) {
                if (!queryRelated.equals("")) {
                    String relFieldName = fieldNameY.substring("rel.".length());
                    ff = fdRelated.getFormField(relFieldName);
                    sqlY = "select distinct(" + relFieldName + ") from " + FormDb.getTableName(fdRelated.getCode());
                } else {
                    LogUtil.getLog(getClass()).error("查询ID：" + queryRelated + "无关联查询！");
                }
            } else {
                ff = fd.getFormField(fieldNameY);

                sqlY = "select distinct(" + fieldNameY + ") from " + FormDb.getTableName(fqd.getTableCode());
            }

            LogUtil.getLog(getClass()).info("sqlY=" + sqlY);

            if (!sqlY.equals("")) {
                String smartSQL = "";
                try {
                	// 关联选项卡
                	if (isForRelateModule) {
	                	FormSQLBuilder fsb = new FormSQLBuilder(moduleFdao, jsonTabSetup);     
	                    smartSQL = fsb.getSmaryQueryWithoutOrderBy(request, fqd.getId());                		
                	}
                	else {
	                	FormSQLBuilder fsb = new FormSQLBuilder();     
	                    smartSQL = fsb.getSmaryQueryWithoutOrderBy(request, fqd.getId());
                	}
                } catch (ErrMsgException ex1) {
                    ex1.printStackTrace();
                }

                // 遍历Y轴的元素
                try {
                    UserDb user = new UserDb();
                    DeptDb dd = new DeptDb();

                    jsonAry = new JSONArray();
                    int fieldType = ff.getFieldType();
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sqlY);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();

                        json = new JSONObject();
                        Map mapColTitleVal = new HashMap();

                        // {id:"1",userName:"flySky",gender:"男",email:"skyfly@163.com",QQ:"8000000",mobilePhone:"13223423424",birthday:"1985-10-01"},
                        try {
                            if (fqrcY.getFieldType().equals(FormQueryReportDb.TYPE_USER)) {
                                user = user.getUserDb(rr.getString(1));
                                json.put(fqrcY.getFieldName(), user.getRealName());
                            }
                            else if (fqrcY.getFieldType().equals(FormQueryReportDb.TYPE_DEPT)) {
                                dd = dd.getDeptDb(rr.getString(1));
                                json.put(fqrcY.getFieldName(), dd.getName());
                            }
                            else
                                json.put(fqrcY.getFieldName(), rr.getString(1));
                        } catch (JSONException ex4) {
                            ex4.printStackTrace();
                        }

                        // 通过X轴条件取得X轴上求值的SQL
                        Iterator ir = vtX.iterator();
                        while (ir.hasNext()) {
                            FormQueryReportCell fqrcX = (FormQueryReportCell) ir.next();
                            // 如果为空元素
                            if (fqrcX.getFieldName().equals("")) {
                                continue;
                            }
                            
                            if (fqrcX.getFieldName().startsWith(FormQueryReportCell.SQL_COL_PREFIX)) {
                            	String valY = rr.getString(1);
                            	String tdVal = getSQLColValue(fqrcX, valY);
                                try {
                                    json.put(fqrcX.getFieldName(), tdVal);
                                } catch (JSONException ex5) {
                                    ex5.printStackTrace();
                                }
                            }
                            else if (fqrcX.getFieldName().startsWith(FormQueryReportCell.SCRIPT_COL_PREFIX)) {
                            	String valY = rr.getString(1);
                            	String tdVal = getScriptColValue(fqrcX, valY, mapColTitleVal);
                                try {
                                    json.put(fqrcX.getFieldName(), tdVal);
                                } catch (JSONException ex5) {
                                    ex5.printStackTrace();
                                }
                            }
                            else {
                                // 替换SQL语句，加入条件fqrcY.getFieldName()=...，将select部分换为formula
                                String func = fqrcX.getFormula();
                                if (func.equals("average")) {
                                    func = "sum";
                                }
                                else if (func.equals("count")) {
                                    func = "count";
                                }

                                // X轴不应该允许用关联的主表的字段，这样会使得生成SQL语句时难以与smartSQL对应
                                // if (fqrcX.getFieldName().startsWith("rel.")) {
                                // 用正则表达式 (.*?)\\.id的原因是因为可能会出现theForm.id
                                String sqlX = smartSQL.replaceFirst(" ((.*?)\\.)?id ", " " + func + "(" + fqrcX.getFieldName() + ") ");
                                String sqlValY = getSQLValueOfY(fqrcX, fieldType, rr);
                                // 拼装上Y轴的字段值
                                sqlX = makeSQLWithYFieldValue(sqlX, sqlRelX, fqrcY.getFieldName(), sqlValY);

                                LogUtil.getLog(getClass()).info("sqlX=" + sqlX);
                                // LogUtil.getLog(getClass()).info(getClass() + " sqlX=" + sqlX);
                                // 取得X轴的值
                                ResultIterator riX = jt.executeQuery(sqlX);
                                String tdVal = "";
                                if (riX.hasNext()) {
                                    ResultRecord rrX = (ResultRecord) riX.next();
                                    if (fqrcX.getFormula().equals("average")) {
                                        double dblVal = rrX.getDouble(1);
                                        /*
                                        String sqlCountX = smartSQL.replaceFirst("id",
                                                                                 "count(" + fqrcX.getFieldName() + ")");
                                        */
                                        String sqlCountX = smartSQL.replaceFirst(" ((.*?)\\.)?id ", " count(" + fqrcX.getFieldName() + ") ");
                                        
                                        sqlCountX = makeSQLWithYFieldValue(sqlCountX, sqlRelX, fqrcY.getFieldName(), sqlValY);
                                        ResultIterator riCountX = jt.executeQuery(sqlCountX);
                                        if (riCountX.hasNext()) {
                                            ResultRecord rrCountX = (ResultRecord) riCountX.next();
                                            int c = rrCountX.getInt(1);
                                            if (c==0)
                                            	tdVal = "0";
                                            else
                                            	tdVal = "" + NumberUtil.round(dblVal / c, 2);
                                        }
                                    } else {
                                        tdVal = rrX.getString(1);
                                    }
                                }
                                try {
                                    json.put(fqrcX.getFieldName(), tdVal);
                                } catch (JSONException ex5) {
                                    ex5.printStackTrace();
                                }                            
                            }
                            
                            try {
								mapColTitleVal.put(fqrcX.getValue(), (String)json.get(fqrcX.getFieldName()));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								LogUtil.getLog(getClass()).error(e);
							}
                        }
                        jsonAry.put(json);
                    }
                    ret[1] = jsonAry.toString();
                } catch (SQLException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }
            } else {
                throw new ErrMsgException("Y轴元素的SQL语句为空！");
            }
        }
        else {
        	throw new ErrMsgException("Y轴元素不存在！");
        }
        return ret;
    }
    
    /**
     * 基于脚本型查询的报表
     * @param request
     * @param fqrd
     * @param fqd
     * @return
     * @throws ErrMsgException
     */
    public String[] getJqGridDescScript(HttpServletRequest request, FormQueryReportDb fqrd, FormQueryDb fqd) throws ErrMsgException {
        String[] ret = new String[3];

        // FormQueryReportDb fqrd = new FormQueryReportDb();
        // fqrd = (FormQueryReportDb) fqrd.getQObjectDb(new Long(id));
        
        String content = fqrd.getString("content");

        // 取得最后一行的tr
        int i = content.lastIndexOf("<tr>");
        int j = content.lastIndexOf("</tr>");

        if (i==-1 || j==-1) {
            throw new ErrMsgException("报表格式错误!");
        }

        String cellContent = content.substring(i + 4, j);

        LogUtil.getLog(getClass()).info("cellContent=" + cellContent);
        Vector[] ary = parseCell(cellContent);
        Vector vtX = ary[0];
        Vector vtY = ary[1];
        LogUtil.getLog(getClass()).info("vtY.size=" + vtY.size());

        if (vtY.size() > 0) {
            // 取得第一个Y轴的元素
            FormQueryReportCell fqrcY = (FormQueryReportCell) vtY.elementAt(0);
            String fieldNameY = fqrcY.getFieldName();
            if (fieldNameY == null) {
                throw new ErrMsgException("Y轴元素设置非法，字段名称解析为空！");
            }

            String colNames = StrUtil.sqlstr(fqrcY.getValue());

            // String formCode = fqd.getTableCode();
            // if (formCode==null || formCode.equals(""))
            //    throw new ErrMsgException("请先选择查询");

            /*
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            String queryRelated = fqd.getQueryRelated();
            int queryRelatedId = StrUtil.toInt(queryRelated, -1);
            FormQueryDb aqdRelated;
            FormDb fdRelated = new FormDb();
            if (queryRelatedId!=-1) {
                aqdRelated = fqd.getFormQueryDb(queryRelatedId);
                fdRelated = fdRelated.getFormDb(aqdRelated.getTableCode());
            }
            */

            JSONArray jsonAry = new JSONArray();
            JSONObject json = new JSONObject();
            try {
                json.put("name", fieldNameY);
                json.put("index", fieldNameY);
                json.put("width", fqrcY.getWidth());
                /*
                if (fieldNameY.startsWith("rel.")) {
                    FormField ff = fdRelated.getFormField(fqrcY.getFieldName().substring("rel.".length()));
                    json.put("sorttype", getSortType(ff));
                }
                else {
                    FormField ff = fd.getFormField(fqrcY.getFieldName());
                    json.put("sorttype", getSortType(ff));
                }
                */
                
                // Y值元素一般爲text型，但是其實可以通過QueryScriptUtil.getFieldTypeOfDBType取到
                json.put("sorttype", "text");
                jsonAry.put(json);
            } catch (JSONException ex2) {
                ex2.printStackTrace();
            }

            String completeStr = "function completeMethod() {";
            String completeStr2 = "";

            Iterator irX = vtX.iterator();
            while (irX.hasNext()) {
                FormQueryReportCell fqrcX = (FormQueryReportCell) irX.next();
                // 有些列在配置时可能为空
                if (fqrcX.getFieldName().equals(""))
                    continue;

                // 生成合计javascript
                // 统计时利用getCol方法，第一个参数为colMode的name值，第二个设为false，否则会返回一个数组而不是但一个数据，第三个是设置统计方式，有'sum','avg'和'count'。
                fqrcX.setSumType("sum");
                if (!fqrcX.getSumType().equals("")) {
                    completeStr += "var sum_" + fqrcX.getFieldName() + "=$(\"#gridTable\").getCol('" + fqrcX.getFieldName() + "',false,'" + fqrcX.getSumType() + "');";
                    // 必须加单引号，因为嵌套表rel.mytitle字段中的.号，会使得json字段串出错
                    completeStr2 += ",'" + fqrcX.getFieldName() + "':sum_" + fqrcX.getFieldName();
                }
                colNames += "," + StrUtil.sqlstr(fqrcX.getValue());
                try {
                    json = new JSONObject();
                    json.put("name", fqrcX.getFieldName());
                    json.put("index", fqrcX.getFieldName());
                    json.put("width", fqrcX.getWidth());

                    json.put("sorttype", getSortType(fqrcX.getFormula()));

                    jsonAry.put(json);
                } catch (JSONException ex3) {
                    ex3.printStackTrace();
                }
            }

            ret[0] = "colNames:[" + colNames + "], colModel:" + jsonAry.toString() + ",";
            if (!completeStr2.equals(""))
                completeStr += "$(\"#gridTable\").footerData('set', { '" + fqrcY.getFieldName() + "': '合计'" + completeStr2 + " });";

            completeStr += "}";
            ret[2] = completeStr;

            // 置Y列
            String sqlY = "";

            String sqlRelX = "";
            /*
            // 如果有关联查询，则取得关联查询的SQL语句             
            if (!queryRelated.equals("")) {
                // String[] ary = StrUtil.split(queryRelated, ",");
                if (queryRelatedId != -1) {
                	FormSQLBuilder fsb = new FormSQLBuilder();
                    sqlRelX = fsb.getQueryRelated(queryRelatedId);
                    int p = sqlRelX.indexOf("id");
                    sqlRelX = sqlRelX.substring(0, p) + "flowId" + sqlRelX.substring(p + 2);
                }
            }
            */

            // 取得用于遍历Y轴元素的语句
			QueryScriptUtil qsu = new QueryScriptUtil();
			String scriptSql = qsu.parseSql(fqd.getScripts()).toUpperCase();
			
			// 取from之后的sql语句
			int p = scriptSql.indexOf(" FROM ");
			String scriptSqlSub = scriptSql.substring(p);
            // 去掉末尾的 order by
            p = scriptSqlSub.indexOf(" ORDER BY");
            scriptSqlSub = scriptSqlSub.substring(0, p);
            
            // 如果是关联选项卡
            if (isForRelateModule) {
            	scriptSqlSub = qsu.getSqlExpressionReplacedWithFieldValue(scriptSqlSub, moduleFdao, jsonTabSetup);
            }
            
			ResultIterator ri = null;
			// Y轴为脚本型
            if (fqrcY.getFieldName().startsWith(FormQueryReportCell.SCRIPT_COL_PREFIX)) {
            	ri = getResultIteratorOfY(fqrcY);
            	Map mapIndex = ri.getMapIndex();
            	Iterator ir = mapIndex.keySet().iterator();
            	if (ir.hasNext()) {
            		fieldNameY = (String)ir.next();
            	}
            }
            else {
            	// Y轴为字段型
                sqlY = "select distinct(" + fieldNameY + ")" + scriptSqlSub;
                JdbcTemplate jt = new JdbcTemplate();
            	try {
					ri = jt.executeQuery(sqlY);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(getClass()).error(e);
				}

                LogUtil.getLog(getClass()).info("sqlY=" + sqlY);            	
            }

            if (ri!=null && ri.hasNext()) {
                // 遍历Y轴的元素
                try {
                    UserDb user = new UserDb();
                    DeptDb dd = new DeptDb();
                    
                    Integer iType = (Integer)ri.getMapType().get(fieldNameY.toUpperCase());
                    int fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());
                    // 置Y軸字段的排序類型
                    ((JSONObject)jsonAry.get(0)).put("sorttype", getSortType(fieldType));
                    
                    JdbcTemplate jt = new JdbcTemplate();
                    
                    jsonAry = new JSONArray();

                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();

                        json = new JSONObject();
                        Map mapColTitleVal = new HashMap();

                        // {id:"1",userName:"flySky",gender:"男",email:"skyfly@163.com",QQ:"8000000",mobilePhone:"13223423424",birthday:"1985-10-01"},
                        try {
                            if (fqrcY.getFieldType().equals(FormQueryReportDb.TYPE_USER)) {
                                user = user.getUserDb(rr.getString(1));
                                json.put(fqrcY.getFieldName(), user.getRealName());
                            }
                            else if (fqrcY.getFieldType().equals(FormQueryReportDb.TYPE_DEPT)) {
                                dd = dd.getDeptDb(rr.getString(1));
                                json.put(fqrcY.getFieldName(), dd.getName());
                            }
                            else
                                json.put(fqrcY.getFieldName(), rr.getString(1));
                        } catch (JSONException ex4) {
                            ex4.printStackTrace();
                        }

                        // 通过X轴条件取得X轴上求值的SQL
                        Iterator ir = vtX.iterator();
                        while (ir.hasNext()) {
                            FormQueryReportCell fqrcX = (FormQueryReportCell) ir.next();
                            // 如果为空元素
                            if (fqrcX.getFieldName().equals("")) {
                                continue;
                            }
                            
                            if (fqrcX.getFieldName().startsWith(FormQueryReportCell.SQL_COL_PREFIX)) {
                            	String valY = rr.getString(1);
                            	String tdVal = getSQLColValue(fqrcX, valY);
                                try {
                                    json.put(fqrcX.getFieldName(), tdVal);
                                } catch (JSONException ex5) {
                                    ex5.printStackTrace();
                                }
                            }
                            else if (fqrcX.getFieldName().startsWith(FormQueryReportCell.SCRIPT_COL_PREFIX)) {
                            	String valY = rr.getString(1);
                            	String tdVal = getScriptColValue(fqrcX, valY, mapColTitleVal);
                                try {
                                    json.put(fqrcX.getFieldName(), tdVal);
                                } catch (JSONException ex5) {
                                    ex5.printStackTrace();
                                }
                            }
                            else {
                                // 替换SQL语句，加入条件fqrcY.getFieldName()=...，将select部分换为formula
                                String func = fqrcX.getFormula();
                                if (func.equals("average")) {
                                    func = "sum";
                                }
                                else if (func.equals("count")) {
                                    func = "count";
                                }

                                // X轴不应该允许用关联的主表的字段，这样会使得生成SQL语句时难以与smartSQL对应
                                // if (fqrcX.getFieldName().startsWith("rel.")) {
                                // 用正则表达式 (.*?)\\.id的原因是因为可能会出现theForm.id
                                String sqlX = "select " + func + "(" + fqrcX.getFieldName() + ") " + scriptSqlSub;
                                String sqlValY = getSQLValueOfY(fqrcX, fieldType, rr);
                                // 拼装上Y轴的字段值
                                sqlX = makeSQLWithYFieldValue(sqlX, sqlRelX, fieldNameY, sqlValY);

                                LogUtil.getLog(getClass()).info("sqlX=" + sqlX);
                                // LogUtil.getLog(getClass()).info(getClass() + " sqlX=" + sqlX);
                                // 取得X轴的值
                                ResultIterator riX = jt.executeQuery(sqlX);
                                String tdVal = "";
                                if (riX.hasNext()) {
                                    ResultRecord rrX = (ResultRecord) riX.next();
                                    if (fqrcX.getFormula().equals("average")) {
                                        double dblVal = rrX.getDouble(1);
                                        /*
                                        String sqlCountX = smartSQL.replaceFirst("id",
                                                                                 "count(" + fqrcX.getFieldName() + ")");
                                        */
                                        String sqlCountX = "select count(" + fqrcX.getFieldName() + ")" + scriptSqlSub;
                                        
                                        sqlCountX = makeSQLWithYFieldValue(sqlCountX, sqlRelX, fqrcY.getFieldName(), sqlValY);
                                        ResultIterator riCountX = jt.executeQuery(sqlCountX);
                                        if (riCountX.hasNext()) {
                                            ResultRecord rrCountX = (ResultRecord) riCountX.next();
                                            int c = rrCountX.getInt(1);
                                            if (c==0)
                                            	tdVal = "0";
                                            else
                                            	tdVal = "" + NumberUtil.round(dblVal / c, 2);
                                        }
                                    } else {
                                        tdVal = rrX.getString(1);
                                    }
                                }
                                try {
                                    json.put(fqrcX.getFieldName(), tdVal);
                                } catch (JSONException ex5) {
                                    ex5.printStackTrace();
                                }                            
                            }
                            
                            try {
								mapColTitleVal.put(fqrcX.getValue(), (String)json.get(fqrcX.getFieldName()));
							} catch (JSONException e) {
								LogUtil.getLog(getClass()).error(e);
							}
                        }
                        jsonAry.put(json);
                    }
                    ret[1] = jsonAry.toString();
                } catch (SQLException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                } catch (JSONException e) {
					LogUtil.getLog(getClass()).error(e);
				}
            } else {
                throw new ErrMsgException("Y轴元素的SQL语句为空！");
            }
        }
        else {
        	throw new ErrMsgException("Y轴元素不存在！");
        }
        return ret;
    }
    

    /**
     * 显示为jqGrid
     * @param request HttpServletRequest
     * @param id long
     * @return String[] 0为colNames 1为data
     * @throws ErrMsgException
     */
    public Vector[] export(HttpServletRequest request, long id) throws ErrMsgException {
        FormQueryReportDb fqrd = new FormQueryReportDb();
        fqrd = (FormQueryReportDb) fqrd.getQObjectDb(new Long(id));
        String content = fqrd.getString("content");

        // 取得最后一行的tr
        int i = content.lastIndexOf("<tr>");
        int j = content.lastIndexOf("</tr>");

        String cellContent = content.substring(i + 4, j);

        LogUtil.getLog(getClass()).info("cellContent=" + cellContent);
        Vector[] ary = parseCell(cellContent);
        Vector vtX = ary[0];
        Vector vtY = ary[1];
        LogUtil.getLog(getClass()).info("vtY.size=" + vtY.size());

        Vector[] ret = null;

        if (vtY.size() > 0) {
            // 取得第一个Y轴的元素
            FormQueryReportCell fqrcY = (FormQueryReportCell) vtY.elementAt(0);
            String fieldNameY = fqrcY.getFieldName();
            if (fieldNameY == null) {
                throw new ErrMsgException("Y轴元素设置非法，字段名称解析为空！");
            }

            int queryId = fqrd.getInt("query_id");
            FormQueryDb fqd = new FormQueryDb();
            fqd = fqd.getFormQueryDb(queryId);
            String formCode = fqd.getTableCode();
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            String queryRelated = fqd.getQueryRelated();
            int queryRelatedId = StrUtil.toInt(queryRelated, -1);
            FormQueryDb aqdRelated;
            FormDb fdRelated = new FormDb();
            if (queryRelatedId!=-1) {
                aqdRelated = fqd.getFormQueryDb(queryRelatedId);
                fdRelated = fdRelated.getFormDb(aqdRelated.getTableCode());
            }

            Vector vTitle = new Vector();
            vTitle.addElement(fqrcY.getValue());

            Iterator irX = vtX.iterator();
            while (irX.hasNext()) {
                FormQueryReportCell fqrcX = (FormQueryReportCell) irX.next();
                // 有些列在配置时可能为空
                if (fqrcX.getFieldName().equals(""))
                    continue;

                vTitle.addElement(fqrcX.getValue());

            }

            // 置Y列
            String sqlY = "";
            FormField ff = null;

            // 如果有关联查询，则取得关联查询的SQL语句
            String sqlRelX = "";
            if (!queryRelated.equals("")) {
                // String[] ary = StrUtil.split(queryRelated, ",");
                if (queryRelatedId != -1) {
                	FormSQLBuilder fsb = new FormSQLBuilder();                	
                    sqlRelX = fsb.getQueryRelated(queryRelatedId);
                    int p = sqlRelX.indexOf("id");
                    sqlRelX = sqlRelX.substring(0, p) + "flowId" + sqlRelX.substring(p + 2);
                }
            }

            // 取得用于遍历Y轴元素的语句
            if (fieldNameY.startsWith("rel.")) {
                if (!queryRelated.equals("")) {
                    String relFieldName = fieldNameY.substring("rel.".length());
                    ff = fdRelated.getFormField(relFieldName);
                    sqlY = "select distinct(" + relFieldName + ") from " + FormDb.getTableName(fdRelated.getCode());
                } else {
                    LogUtil.getLog(getClass()).error("查询ID：" + queryRelated + "无关联查询！");
                }
            } else {
                ff = fd.getFormField(fieldNameY);
                sqlY = "select distinct(" + fieldNameY + ") from " + FormDb.getTableName(fqd.getTableCode());
            }

            LogUtil.getLog(getClass()).info("sqlY=" + sqlY);
            Vector vValue = new Vector();

            if (!sqlY.equals("")) {
                String smartSQL = "";
                try {
                	FormSQLBuilder fsb = new FormSQLBuilder();                	
                    smartSQL = fsb.getSmaryQueryWithoutOrderBy(request, fqd.getId());
                } catch (ErrMsgException ex1) {
                    ex1.printStackTrace();
                }
                
                LogUtil.getLog(getClass()).info("smartSQL=" + smartSQL);

                // 遍历Y轴的元素
                try {
                    UserDb user = new UserDb();
                    DeptDb dd = new DeptDb();

                    int fieldType = ff.getFieldType();
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sqlY);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();

                        String[] aryV = new String[vTitle.size()];
                        int n = 0;

                        if (fqrcY.getFieldType().equals(FormQueryReportDb.TYPE_USER)) {
                            user = user.getUserDb(rr.getString(1));
                            aryV[n] = user.getRealName();
                        }
                        else if (fqrcY.getFieldType().equals(FormQueryReportDb.TYPE_DEPT)) {
                            dd = dd.getDeptDb(rr.getString(1));
                            aryV[n] = dd.getName();
                        }
                        else
                            aryV[n] = rr.getString(1);
                        n++;
                        
                        Map mapColTitleVal = new HashMap();

                        // 通过X轴条件取得X轴上求值的SQL
                        Iterator ir = vtX.iterator();
                        while (ir.hasNext()) {
                            FormQueryReportCell fqrcX = (FormQueryReportCell) ir.next();
                            // 如果为空元素
                            if (fqrcX.getFieldName().equals("")) {
                                continue;
                            }
                            // 处理SQL型的列
                            if (fqrcX.getFieldName().startsWith(FormQueryReportCell.SQL_COL_PREFIX)) {
                            	String valY = rr.getString(1);
                            	aryV[n] = getSQLColValue(fqrcX, valY);
                            }
                            else if (fqrcX.getFieldName().startsWith(FormQueryReportCell.SCRIPT_COL_PREFIX)) {
                            	String valY = rr.getString(1);                            	
                            	aryV[n] = getScriptColValue(fqrcX, valY, mapColTitleVal);
                            }
                            else {
                                // 替换SQL语句，加入条件fqrcY.getFieldName()=...，将select部分换为formula
                                String func = fqrcX.getFormula();
                                if (func.equals("average")) {
                                    func = "sum";
                                }
                                else if (func.equals("count")) {
                                    func = "count";
                                }

                                // X轴不应该允许用关联的主表的字段，这样会使得生成SQL语句时难以与smartSQL对应
                                // if (fqrcX.getFieldName().startsWith("rel.")) {
                                
                                // 不能直接替换id，因为SQL可能为：select theForm.id from ft_nest theForm, flow f where f.id=theForm.cws_id
                                // String sqlX = smartSQL.replaceFirst("id", func + "(" + fqrcX.getFieldName() + ")");
                                /*
                                smartSQL = smartSQL.toLowerCase();
                                int p = smartSQL.indexOf(" ");
                                int q = smartSQL.indexOf(" from");
                                String sqlX = smartSQL.substring(0, p) + " " + func + "(" + fqrcX.getFieldName() + ")" + smartSQL.substring(q);
                                */
                                String sqlX = smartSQL.replaceFirst(" ((.*?)\\.)?id ", " " + func + "(" + fqrcX.getFieldName() + ") ");
                                
                                String sqlValY = getSQLValueOfY(fqrcX, fieldType, rr);
                                // 拼装上Y轴的字段值
                                sqlX = makeSQLWithYFieldValue(sqlX, sqlRelX, fqrcY.getFieldName(), sqlValY);

                                LogUtil.getLog(getClass()).info("export: sqlX=" + sqlX);
                                // LogUtil.getLog(getClass()).info(getClass() + " sqlX=" + sqlX);
                                // 取得X轴的值
                                ResultIterator riX = jt.executeQuery(sqlX);
                                String tdVal = "";
                                if (riX.hasNext()) {
                                    ResultRecord rrX = (ResultRecord) riX.next();
                                    if (fqrcX.getFormula().equals("average")) {
                                        double dblVal = rrX.getDouble(1);
                                        /*
                                        String sqlCountX = smartSQL.replaceFirst("id",
                                                                                 "count(" + fqrcX.getFieldName() + ")");
                                        */
                                        String sqlCountX = smartSQL.replaceFirst(" ((.*?)\\.)?id ", " count(" + fqrcX.getFieldName() + ") ");
 
                                        sqlCountX = makeSQLWithYFieldValue(sqlCountX, sqlRelX, fqrcY.getFieldName(), sqlValY);
                                        ResultIterator riCountX = jt.executeQuery(sqlCountX);
                                        if (riCountX.hasNext()) {
                                            ResultRecord rrCountX = (ResultRecord) riCountX.next();
                                            int c = rrCountX.getInt(1);
                                            if (c==0)
                                            	tdVal = "0";
                                            else                                            
                                            	tdVal = "" + NumberUtil.round(dblVal / c, 2);
                                        }
                                    } else {
                                        tdVal = rrX.getString(1);
                                    }
                                }

                                aryV[n] = tdVal;                            	
                            }

                            mapColTitleVal.put(fqrcX.getValue(), aryV[n]);
                            
                            n++;
                        }
                        
                        vValue.addElement(aryV);
                    }
                } catch (SQLException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }

                ret = new Vector[2];
                ret[0] = vTitle;
                ret[1] = vValue;
            } else {
                throw new ErrMsgException("Y轴元素的SQL语句为空！");
            }
        }
        else {
            LogUtil.getLog(getClass()).info("Y轴元素不存在！");
        }
        return ret;
    }
    
    public String getSQLColValue(FormQueryReportCell fqrcX, String valY) throws ErrMsgException {
        if (valY==null)
        	valY = "";
        
    	// 判断是否存在外部数据源
    	String sql = StrUtil.unescape(fqrcX.getFormula());
        Pattern pat = Pattern.compile(
                "(\\[([A-Za-z0-9-_]+)\\])?(.*+)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        LogUtil.getLog(getClass()).info("sql=" + sql);
        String db = null;
        Matcher mat = pat.matcher(sql);
        if (mat.find()) {
            db = mat.group(2);
            sql = mat.group(3);
            
            // LogUtil.getLog(getClass()).info("sql2=" + sql);
        }
        
        // 替换Y轴的值
        sql = sql.toUpperCase();
        sql = sql.replace("{$Y}", valY);
        LogUtil.getLog(getClass()).info("sql=" + sql);

        ResultIterator riSql = null;
        try {
        	if (db!=null && !"".equals(db)) {
        		Connection conn = new Connection(db);
        		if (conn.getCon()==null) {
        			throw new ErrMsgException("数据库连接不存在");
        		}
        		else {
		            JdbcTemplate jtSql = new JdbcTemplate(conn);
		            riSql = jtSql.executeQuery(sql);
        		}
        	}
        	else {
	            JdbcTemplate jtSql = new JdbcTemplate();
	            riSql = jtSql.executeQuery(sql);
        	}
        	
            // 如果返回值只有一行一列
            if (riSql.size() >= 1) {
                ResultRecord rrSql = (ResultRecord) riSql.next();
                return rrSql.getString(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            throw new ErrMsgException(e.getMessage());
        }
        return "";
    }
    
    /**
     * 取得脚本字段的值
     * @param fqrcX
     * @param valY
     * @param mapColTitleVal 同一行中排在其之前的列的值
     * @return
     * @throws ErrMsgException
     */
    public String getScriptColValue(FormQueryReportCell fqrcX, String valY, Map mapColTitleVal) throws ErrMsgException {
        if (valY==null)
        	valY = "";
        
    	// 赋值给Y轴元素替换符
    	String myscript = StrUtil.unescape(fqrcX.getFormula());
    	myscript.replaceAll("\\{\\$Y\\}", valY);

		Interpreter bsh = new Interpreter();
		try {
			StringBuffer sb = new StringBuffer();

			// 替换其它列，注意这些列须在本列之前
	    	Iterator ir = mapColTitleVal.keySet().iterator();
	    	while (ir.hasNext()) {
	    		String key = (String)ir.next();
	    		sb.append("$" + key + "=" + mapColTitleVal.get(key) + ";");
	    	}
	    	
			// 赋值给用户
			// sb.append("userName=\"" + pvg.getUser(request) + "\";");
			// bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			// myscript = myscript.replaceAll("LogUtil.getLog(getClass()).info\\(", "out.print\\(\"<BR>\" + ");
			bsh.eval(myscript);
			Object obj = bsh.get("ret");
			if (obj != null) {
				return String.valueOf(obj);
			}
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			// LogUtil.getLog(getClass()).error(e);
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return "None";
    }
    
    /**
     * 取得Y轴脚本运行后返回的ResultIterator
     * @param fqrcY
     * @return
     * @throws ErrMsgException
     */
    public ResultIterator getResultIteratorOfY(FormQueryReportCell fqrcY) throws ErrMsgException {
    	// 赋值给Y轴元素替换符
    	String myscript = StrUtil.unescape(fqrcY.getFormula());

		Interpreter bsh = new Interpreter();
		try {
			// myscript = myscript.replaceAll("LogUtil.getLog(getClass()).info\\(", "out.print\\(\"<BR>\" + ");
			bsh.eval(myscript);
			Object obj = bsh.get("ret");
			if (obj != null) {
				return (ResultIterator)obj;
			}
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			// LogUtil.getLog(getClass()).error(e);
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return null;
    }    
    
	public void setForRelateModule(boolean isForRelateModule) {
		this.isForRelateModule = isForRelateModule;
	}

	public boolean isForRelateModule() {
		return isForRelateModule;
	}
	
	private boolean isForRelateModule = false;
    
}
