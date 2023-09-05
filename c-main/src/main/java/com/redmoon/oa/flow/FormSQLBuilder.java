package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimestampValue;

import org.json.JSONException;
import org.json.JSONObject;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.query.QueryScriptUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.ModuleRelateDb;

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
public class FormSQLBuilder {

    public static final String AUI = "AUI";
    public static final String ASI = "ASI";
    public static final String AREW = "AREW";
    public static final String ARES = "ARES";
    public static final String API = "API";
    public static final String AFI = "AFI";
    public static final String AAI = "AAI";
    public static final String AUC = "AUC"; // archive_user_change
    
    /**
     * 选项卡映射时，用来标识表单表格中的主键ID
     */
    public static final String PRIMARY_KEY_ID = "primaryKeyId";
    
    /**
     * 模块关联选项卡
     */
    private boolean isForRelateModule = false;
    
    /**
     * 模块选项卡对应的模块表单的dao
     */
    private com.redmoon.oa.visual.FormDAO moduleFdao;
    /**
     * 模块选项卡中的配置
     */
    private JSONObject jsonTabSetup;
    
    /**
     * 用于通过查询选择字段的映射
     */
    private boolean forSelField = false;
    
    private Map mapConds;

    public FormSQLBuilder() {
    }
    
    public FormSQLBuilder(com.redmoon.oa.visual.FormDAO moduleFdao, JSONObject jsonTabSetup) {
    	this.moduleFdao = moduleFdao;
    	this.jsonTabSetup = jsonTabSetup;
    }

     public static String getArchiveTable(){
         return "select table_code from FORM_TABLE order by table_code desc";
     }

     public static String getArchiveTableField(String table_short_code){
         return "select id from FORM_FIELD where table_short_code = " + StrUtil.sqlstr(table_short_code) + " order by id";
     }

     public static String getArchiveTableField(String table_short_code,String field_code){
         return "select id from FORM_FIELD where table_short_code = " + StrUtil.sqlstr(table_short_code) + " and field_code = " + StrUtil.sqlstr(field_code);
     }

     public static String getFormQuery() {
         return "select id from form_query order by id desc";
     }

     /**
      * 取得用户的报表
      * @param userName String
      * @return String
      */
     public static String getFormQueryReport(String userName) {
         return "select id from form_query_report where user_name=" + StrUtil.sqlstr(userName) + " order by id desc";
     }

     public static String getFormQueryCondition(int query_id) {
         return "select id from FORM_QUERY_CONDITION where query_id = " + query_id + " order by condition_field_code";
     }

     public static String getUserAssess(String userName) {
         return "select id from FORM_ASSESS where username = " +
                      StrUtil.sqlstr(userName);
     }

     public static String getUserProfession(String userName) {
         return "select id from FORM_PROFESSION where username = " +
                      StrUtil.sqlstr(userName);
     }

     public static String getUserRewards(String userName) {
         return "select id from FORM_REWARDS where username = " +
                      StrUtil.sqlstr(userName);
     }

     public static String getUserDuty(String userName) {
         return "select id from FORM_DUTY where username = " +
                      StrUtil.sqlstr(userName) + " order by mydate desc";
     }

     public static String getQueryPrivilege(int queryId, int type) {
         return "select id from FORM_QUERY_PRIVILEGE where query_id = " +
                      queryId + " and priv_type=" + type;
     }

     public static String getQueryPrivilege(int queryId) {
         return "select id from FORM_QUERY_PRIVILEGE where query_id = " +
                      queryId;
     }

     public static String getUserPrivilege(String userName,String deptCode) {
         return "select id from FORM_PRIVILEGE where userName = " +
                      StrUtil.sqlstr(userName) + " and deptCode = " + StrUtil.sqlstr(deptCode);
     }

     /**
      * 去除排序字段中，表没有被用到的那些字段
      * @param orderField String
      * @param tableSimpleCode String
      * @return String
      */
     public static String removeOrderOfTable(String orderField, String tableSimpleCode) {
         String token = tableSimpleCode + ".";
         int p = orderField.indexOf(token);
         if (p==-1) {
             return orderField;
         }
         int firstIndex = p;
         int lastIndex = p;
         while (p>0) {
             lastIndex = p;
             p = orderField.indexOf(token, p + 1);
         }

         // 寻找下一个逗号,
         int dhp = orderField.indexOf(",", lastIndex + 1);
         if (dhp == -1) {
             // 无逗号，表示处于结尾处
             orderField = orderField.substring(0, firstIndex - 1);
         } else {
             // 有逗号
             orderField = orderField.substring(0, firstIndex - 1) +
                          orderField.substring(dhp);
         }
         return orderField;
     }

     /**
      * 根据条件Vector，组合出条件
      * @param fqd
      * @param condV Vector
      * @param chartFormField String 饼图或柱状图中需用到的字段编码
      * @param chartFieldValue String 饼图或柱状图中需用到的字段的值
      * @return String
      */
     public String getConditions(FormQueryDb fqd, Vector condV, FormField chartFormField, String chartFieldValue) {
         String old_condition_field_code = "";
         String old_condition_type = "";
         StringBuilder conditions = new StringBuilder();
         Iterator ir = condV.iterator();
         
         FormDb fd = new FormDb();
         fd = fd.getFormDb(fqd.getTableCode());

         while (ir.hasNext()) {
             FormQueryConditionDb aqcd = (FormQueryConditionDb) ir.next();

             // 闭合select型字段的条件
             if (!old_condition_field_code.equals(aqcd.getConditionFieldCode()) &&
                 old_condition_type.equals("SELECTED")) {
                 conditions.append(")");
             }

             if (old_condition_type.equals("SELECTED")) {
                 if (old_condition_field_code.equals(aqcd.getConditionFieldCode())) {
                     conditions.append(" or ");
                 }
                 else {
                     conditions.append(" and ");
                 }
             }
             else {
                 if (!old_condition_field_code.equals("")) {
                     if (!conditions.toString().equals("")) {
                             conditions.append(" and ");
                     }
                 }
             }

             /*
             LogUtil.getLog(getClass()).info(FormSQLBuilder.class +
                                " old_condition_field_code=" +
                                old_condition_field_code + " conditions=" +
                                conditions);
             */
             if ("SELECTED".equals(aqcd.getConditionType()) &&
                 !old_condition_field_code.equals(aqcd.getConditionFieldCode())) {
                 conditions.append("(");
             }
             
             String val = aqcd.getConditionValue();
             
             // 如果是为关联选项卡组装条件
             if (isForRelateModule) {
            	 String condVal = getCondValue(aqcd.getConditionFieldCode(), moduleFdao, jsonTabSetup);
            	 if (condVal!=null) {
            		 val = condVal;
            	 }
             }
             else if (forSelField) {
            	 String condVal = getCondValue(aqcd.getConditionFieldCode(), fd, mapConds);
            	 if (condVal!=null) {
            		 val = condVal;
            	 }            	 
             }

             // 如果like的值为空
             if ("like".equals(aqcd.getConditionSign())) {
                 if (!"'%%'".equals(val) && !"''".equals(val)) {
                     conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(val);
                 } else {
                     /*
                     LogUtil.getLog(getClass()).info(FormSQLBuilder.class +
                                        " getConditionSign=" +
                                        aqcd.getConditionSign() + " conditions=" +
                                        conditions);
                     */
                     // 去除conditionFieldCode之前所加的 " and "
                     int p = conditions.lastIndexOf(" and ");
                     if (p == conditions.length() - " and ".length()) {
                         conditions = new StringBuilder(conditions.substring(0, p));
                     }
                 }
             } else {
                 if ("SELEDATE".equals(aqcd.getConditionType())) {
                     if (chartFormField!=null && chartFormField.getName().equalsIgnoreCase(aqcd.getConditionFieldCode())) {
                         conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(SQLFilter.getDateStr(chartFieldValue, "yyyy-MM-dd"));
                     }
                     else {
                         if (!"".equals(aqcd.getInputValue())) {
                             conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(val);
                         } else {
                             // 去除conditionFieldCode之前所加的 " and "
                             int p = conditions.lastIndexOf(" and ");
                             if (p == conditions.length() - " and ".length()) {
                                 conditions = new StringBuilder(conditions.substring(0, p));
                             }
                         }
                     }
                 }
                 else {
                     // 如果为选择型，且比较模式为不等于，则置sign为<>
                     if ("SELECTED".equals(aqcd.getConditionType())) {
                         if (aqcd.getCompareType() == FormQueryConditionDb.COMPARE_TYPE_NOT_EQUALS) {
                             aqcd.setConditionSign("<>");
                         }
                     }

                     if (chartFormField!=null && chartFormField.getName().equalsIgnoreCase(aqcd.getConditionFieldCode())) {
                         conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(StrUtil.sqlstr(chartFieldValue)); // @task:对于不同类型的字段，应分别处理，不应都加sqlstr
                     }
                     else {
                         if (!"''".equals(val)) {
                             conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(val);
                         }
                         else {
                             // 去除conditionFieldCode之前所加的 " and "
                             int p = conditions.lastIndexOf(" and ");
                             if (p == conditions.length() - " and ".length()) {
                                 conditions = new StringBuilder(conditions.substring(0, p));
                             }
                         }

                         // LogUtil.getLog(getClass()).info(FormSQLBuilder.class + " conditions=" + conditions);
                     }
                 }
             }
             // out.print("cur--" + conditionFieldCode + "<BR>");
             old_condition_type = aqcd.getConditionType();
             old_condition_field_code = aqcd.getConditionFieldCode();
         }

         if ("SELECTED".equals(old_condition_type)) {
             conditions.append(")");
         }
         return conditions.toString();
     }

     /**
      * 取得条件，根据模块表单中的值及json中的对应关系
      * 用于根据模块中选项卡的配置，从查询中获取列表生成选项卡
      * @param condV
      * @param json
      * @return
      */
	public static String getConditionsWithModuleFormValueXXX(Vector condV, com.redmoon.oa.visual.FormDAO fdao,
			JSONObject json) {
		String old_condition_field_code = "";
		StringBuilder conditions = new StringBuilder();
		Iterator ir = condV.iterator();

		while (ir.hasNext()) {
			FormQueryConditionDb aqcd = (FormQueryConditionDb) ir.next();
			String condField = aqcd.getConditionFieldCode();
			// 从json中判断是否存在映射关系，没有的话则忽略这些条件
			boolean isFound = false;
			String queryField = "";
			String formField = "";
			Iterator irJson = json.keys();
			while (irJson.hasNext()) {
				formField = (String) irJson.next();
				try {
					queryField = json.getString(formField);
				} catch (JSONException e) {
					LogUtil.getLog(FormSQLBuilder.class).error(e);
					break;
				}
				if (queryField.equals(condField)) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
                continue;
            }

			// 从fields中找对相应的值
			String val = "";
			if (formField.equals(PRIMARY_KEY_ID)) {
                val = "" + fdao.getId();
            } else {
                val = fdao.getFieldValue(formField);
            }
			FormField ff = fdao.getFormField(formField);

			if (old_condition_field_code.equals(aqcd.getConditionFieldCode())) {
				continue;
			} else {
				if (!"".equals(old_condition_field_code)) {
                    conditions.append(" and ");
                }
			}

			// 如果like的值为空
			if ("like".equals(aqcd.getConditionSign())) {
				conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(StrUtil.sqlstr(val));
			} else {
				if ("SELEDATE".equals(aqcd.getConditionType())) {
					String formatStr = "yyyy-MM-dd HH:mm:ss";
					if (ff.getType().equals(FormField.TYPE_DATE))
						formatStr = "yyyy-MM-dd";
					conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(SQLFilter.getDateStr(val, formatStr));
				} else {
					// 如果为选择型，且比较模式为不等于，则置sign为<>
					if ("SELECTED".equals(aqcd.getConditionType())) {
						if (aqcd.getCompareType() == FormQueryConditionDb.COMPARE_TYPE_NOT_EQUALS) {
							aqcd.setConditionSign("<>");
						}
					}
					conditions.append(aqcd.getConditionFieldCode()).append(" ").append(aqcd.getConditionSign()).append(" ").append(StrUtil.sqlstr(val));
				}
			}
			old_condition_field_code = aqcd.getConditionFieldCode();
		}

		return conditions.toString();
	}
	
	/**
	 * 将主表字段值代入查询条件中的相应字段
	 * @param condField 查询条件字段
	 * @param fdao
	 * @param json 选项卡映射关系
	 * @return
	 */
	public String getCondValue(String condField, com.redmoon.oa.visual.FormDAO fdao, JSONObject json) {
		boolean isFound = false;
		String queryField = ""; // 查询条件
		String formField = ""; // 关联主表的字段
		Iterator irJson = json.keys();
		while (irJson.hasNext()) {
			formField = (String) irJson.next();
			try {
				queryField = json.getString(formField);
			} catch (JSONException e) {
				LogUtil.getLog(getClass()).error(e);
				break;
			}
			if (queryField.equals(condField)) {
				isFound = true;
				break;
			}
		}

		if (!isFound) {
            return null;
        }

		// 从fields中找对相应的值
		String val = "";
		if (formField.equals(PRIMARY_KEY_ID)) {
            val = "" + fdao.getId();
        } else {
			val = fdao.getFieldValue(formField);
		
			FormField ff = fdao.getFormField(formField);
			int fieldType = ff.getFieldType();
			
			if (fieldType == FormField.FIELD_TYPE_INT) {
				;
			} else if (fieldType == FormField.FIELD_TYPE_LONG) {
				;
			} else if (fieldType == FormField.FIELD_TYPE_FLOAT) {
				;
			} else if (fieldType == FormField.FIELD_TYPE_DOUBLE) {
				;
			} else if (fieldType == FormField.FIELD_TYPE_PRICE) {
				;
			} else if (fieldType == FormField.FIELD_TYPE_DATE) {
				val = SQLFilter.getDateStr(val, "yyyy-MM-dd");
			} else if (fieldType == FormField.FIELD_TYPE_DATETIME) {
				val = SQLFilter.getDateStr(val, "yyyy-MM-dd HH:mm:ss");
			} else {
                val = StrUtil.sqlstr(val);
            }
		}

		return val;
	}
     
	public String getCondValue(String condField, FormDb fd, Map mapConds) {
		boolean isFound = false;
		String queryField = ""; // 查询条件
		String field = "";
        for (Object o : mapConds.keySet()) {
            field = (String) o;
            if (field.equals(condField)) {
                isFound = true;
                break;
            }
        }

		String val = (String)mapConds.get(field);
		
		FormField ff = fd.getFormField(field);
		int fieldType = ff.getFieldType();
		
		if (fieldType == FormField.FIELD_TYPE_INT) {
			;
		} else if (fieldType == FormField.FIELD_TYPE_LONG) {
			;
		} else if (fieldType == FormField.FIELD_TYPE_FLOAT) {
			;
		} else if (fieldType == FormField.FIELD_TYPE_DOUBLE) {
			;
		} else if (fieldType == FormField.FIELD_TYPE_PRICE) {
			;
		} else if (fieldType == FormField.FIELD_TYPE_DATE) {
			val = SQLFilter.getDateStr(val, "yyyy-MM-dd");
		} else if (fieldType == FormField.FIELD_TYPE_DATETIME) {
			val = SQLFilter.getDateStr(val, "yyyy-MM-dd HH:mm:ss");
		} else {
            val = StrUtil.sqlstr(val);
        }

		return val;
	}	
	
     /**
      * 遍历条件，将条件归类，置于HashMap中，map中键值为表的缩写，存储的是Vector，Vector中为与该表相关的条件
      * @param aqd ArchiveQueryDb
      * @param chartFieldCode String 饼图或柱状图中用到的selectd类型的字段，该字段如果不为空，则只能在条件Map中保留一个，以便于赋值，并组合成SQL语句数组
      * @return Map
      */
     public static Vector getSmartQueryCond(FormQueryDb aqd, String chartFieldCode) {
         int cc = 0;

         String conditionFieldSql = getFormQueryCondition(aqd.getId());

         FormQueryConditionDb aqcd = new FormQueryConditionDb();
         Iterator iConditionField = aqcd.list(conditionFieldSql).iterator();
         Vector v = new Vector();
         while (iConditionField.hasNext()) {
             aqcd = (FormQueryConditionDb) iConditionField.next();
             // 去除与chart需要用到的字段重复的条件
             if (aqcd.getConditionFieldCode().equalsIgnoreCase(chartFieldCode)) {
                 // LogUtil.getLog(getClass()).info("ArchiveSQLBuilder:aqcd.getConditionFieldCode()=" + aqcd.getConditionFieldCode());
                 cc++;
                 if (cc > 1) {
                     iConditionField.remove();
                     continue;
                 }
             }
             v.addElement(aqcd);
         }
         return v;
     }

     public String getSmartQuerySQL(FormQueryDb aqd, Vector condV, FormField chartFormField, String chartFieldValue, String calcFieldCode, String calcFunc) {
         boolean isSubForm = false;
         ModuleRelateDb mrd = new ModuleRelateDb();
         // 判断是否为嵌套表
         Vector v = mrd.getModuleReverseRelated(aqd.getTableCode());
         // LogUtil.getLog(getClass()).info(FormSQLBuilder.class + " getSmartQuerySQL v.size()=" + v.size());
         if (v.size()>0) {
             isSubForm = true;
         }

         String beginDateSql = "";
         java.util.Date flowBeginDate1 = aqd.getFlowBeginDate1();
         java.util.Date flowBeginDate2 = aqd.getFlowBeginDate2();
         if (flowBeginDate2!=null) {
             flowBeginDate2 = DateUtil.addDate(flowBeginDate2, 1);
         }
         if (flowBeginDate1!=null && flowBeginDate2!=null) {
             beginDateSql = "f.begin_date>=" + SQLFilter.getDateStr(DateUtil.format(flowBeginDate1, "yyyy-MM-dd"), "yyyy-MM-dd") + " and f.begin_date<" + SQLFilter.getDateStr(DateUtil.format(flowBeginDate2, "yyyy-MM-dd"), "yyyy-MM-dd");
         }
         else if (flowBeginDate1!=null) {
             beginDateSql = "f.begin_date>=" + SQLFilter.getDateStr(DateUtil.format(flowBeginDate1, "yyyy-MM-dd"), "yyyy-MM-dd");
         }
         else if (flowBeginDate2!=null) {
             beginDateSql = "f.begin_date<" + SQLFilter.getDateStr(DateUtil.format(flowBeginDate2, "yyyy-MM-dd"), "yyyy-MM-dd");
         }

         if (condV.size() > 0) {
             String conds = "";
             // if (!isForRelateModule) {
            	 conds = getConditions(aqd, condV, chartFormField, chartFieldValue);
             // }
             // else {
             //	 conds = getConditionsWithModuleFormValue(condV, moduleFdao, jsonTabSetup);
             // }
             if (!"".equals(conds)) {
                 String sql = "select id from " + FormDb.getTableName(aqd.getTableCode()) + " where " + conds;
                 if (!"".equals(beginDateSql)) {
                     sql = "select theForm.id from " + FormDb.getTableName(aqd.getTableCode()) + " theForm, flow f where f.id=theForm.flowId and f.status<>" + WorkflowDb.STATUS_NONE + " and " + conds;
                 }

                 if (aqd.getFlowStatus()!=1000) {
                     sql = "select theForm.id from " + FormDb.getTableName(aqd.getTableCode()) + " theForm, flow f where f.id=theForm.flowId and f.status=" + aqd.getFlowStatus() + " and " + conds;
                 }

                 // 求和
                 if (!"".equals(calcFieldCode)) {
                     sql = "select sum(" + calcFieldCode + ") from " + FormDb.getTableName(aqd.getTableCode()) + " where " + conds;
                     if (aqd.getFlowStatus()!=1000) {
                         sql = "select sum(" + calcFieldCode + ") from " + FormDb.getTableName(aqd.getTableCode()) + " theForm, flow f where f.id=theForm.flowId and f.status=" + aqd.getFlowStatus() + " and " + conds;
                     }
                 }

                 if (!"".equals(beginDateSql)) {
                     sql += " and " + beginDateSql;
                 }
                 
                 if (aqd.getFlowStatus()==2000) {
                     sql = "select id from " + FormDb.getTableName(aqd.getTableCode()) + " where cws_status=" + FormDAO.STATUS_DONE + " and " + conds;
                 }

                 return sql;
             }
         }

         String sql = "select id from " + FormDb.getTableName(aqd.getTableCode());
         if (!"".equals(beginDateSql)) {
             sql = "select theForm.id from " + FormDb.getTableName(aqd.getTableCode()) + " theForm, flow f where f.id=theForm.flowId and f.status<>" + WorkflowDb.STATUS_NONE;
         }
         if (aqd.getFlowStatus()!=1000) {
             sql = "select theForm.id from " + FormDb.getTableName(aqd.getTableCode()) + " theForm, flow f where f.id=theForm.flowId and f.status=" + aqd.getFlowStatus();
         }

         if (!"".equals(beginDateSql)) {
             sql += " and " + beginDateSql;
         }
         
         if (aqd.getFlowStatus()==2000) {
             sql = "select id from " + FormDb.getTableName(aqd.getTableCode()) + " where cws_status=" + FormDAO.STATUS_DONE;
         }         

         return sql;
     }

     /**
      * 取得关联查询的SQL语句（不排序）
      * @param queryId int
      * @return String
      */
     public String getQueryRelated(int queryId) {
         FormQueryDb aqd = new FormQueryDb();
         aqd = aqd.getFormQueryDb(queryId);

         Vector condV = getSmartQueryCond(aqd, "");
         return getSmartQuerySQL(aqd, condV, null, "", "", "");
     }

     /**
      * 取得不带关联查询及order by子句的SQL语句，可用于报表设计中组装SQL语句
      * @param request HttpServletRequest
      * @param queryId int
      * @return String
      * @throws ErrMsgException
      */
     public String getSmaryQueryWithoutOrderBy(HttpServletRequest request, int queryId) throws ErrMsgException {
         FormQueryDb aqd = new FormQueryDb();
         aqd = aqd.getFormQueryDb(queryId);

         Vector condV = getSmartQueryCond(aqd, "");

         // 分离出部门宏控件
         FormDb fd = new FormDb();
         fd = fd.getFormDb(aqd.getTableCode());
         MacroCtlMgr mm = new MacroCtlMgr();
         Vector deptV = new Vector();
         Vector elseV = new Vector();
         Iterator ir = condV.iterator();
         while (ir.hasNext()) {
             FormQueryConditionDb aqcd = (FormQueryConditionDb) ir.next();

             FormField ff = fd.getFormField(aqcd.getFieldCode());
             if (ff==null) {
            	 LogUtil.getLog(getClass()).error("Field " + aqcd.getFieldCode() + " is null.");
            	 continue;
             }
             MacroCtlUnit mu = null;
             String macroCode = "";
             boolean isDept = false;
             if (ff.getType().equals(FormField.TYPE_MACRO)) {
                 mu = mm.getMacroCtlUnit(ff.getMacroType());
                 macroCode = mu.getCode();
                 if (macroCode.equalsIgnoreCase("macro_dept_select") ||
                     macroCode.equalsIgnoreCase("macro_my_dept_select")) {
                     if (aqcd.getCompareType() == FormQueryConditionDb.COMPARE_TYPE_UNDER) {
                         deptV.addElement(aqcd);
                         isDept = true;
                     }
                 }
             }

             if (!isDept) {
                 elseV.addElement(aqcd);
             }
         }

         String sql = getSmartQuerySQL(aqd, elseV, null, "", "", "");

         // 处理部门条件为含子部门的情况
         ir = deptV.iterator();
         while (ir.hasNext()) {
             FormQueryConditionDb aqcd = (FormQueryConditionDb) ir.next();
             // 如果为空则跳过
             String inputValue = aqcd.getInputValue();
             if ("".equals(inputValue)) {
                 continue;
             }

             if (aqcd.getCompareType() == FormQueryConditionDb.COMPARE_TYPE_UNDER) {
                 StringBuilder deptStr = new StringBuilder(StrUtil.sqlstr(inputValue));

                 DeptDb dd = new DeptDb();
                 dd = dd.getDeptDb(inputValue);
                 Vector vt = new Vector();
                 dd.getAllChild(vt, dd);

                 for (Object o : vt) {
                     dd = (DeptDb) o;
                     deptStr.append(",").append(StrUtil.sqlstr(dd.getCode()));
                 }

                 if (sql.indexOf("where") != -1) {
                     sql += " and " + aqcd.getFieldCode() + " in (" + deptStr + ")";
                 } else {
                     sql += " where " + aqcd.getFieldCode() + " in (" + deptStr + ")";
                 }
             }
         }
         return sql;
     }

     public String getSmartQuery(HttpServletRequest request, int queryId) throws SQLException, ResKeyException,
             ErrMsgException {

         String sql = getSmaryQueryWithoutOrderBy(request, queryId);

         FormQueryDb aqd = new FormQueryDb();
         aqd = aqd.getFormQueryDb(queryId);
         // 如果有关联查询
         String queryRelated = aqd.getQueryRelated();
         if (!"".equals(queryRelated)) {
             // String[] ary = StrUtil.split(queryRelated, ",");
             int queryRelatedId = StrUtil.toInt(queryRelated, -1);
             if (queryRelatedId!=-1) {
                 String s = getQueryRelated(queryRelatedId);
                 int p = s.indexOf("id");
                 // s = s.substring(0, p) + "flowId" + s.substring(p + 2);
                 if (sql.contains("where")) {
                     sql += " and cws_id in (" + s + ")";
                 }
                 else {
                     sql += " where cws_id in (" + s + ")";
                 }
             }
         }

         String orderField = aqd.getOrderFieldCode();

         // 加入flexigrid中点击的排序表头
         String orderBy = ParamUtil.get(request, "orderBy");
         String sort = ParamUtil.get(request, "sort");
         if ("".equals(sort)) {
             sort = "desc";
         }

         if (!"".equals(orderBy)) {
             orderField = orderBy + " " + sort;
         }

         if ("".equals(orderField)) {
        	 if (sql.contains(" id ")) {
        		 orderField = "id desc";
        	 }
        	 else {
        		 orderField = "f.id desc";
        	 }
         }

         sql += " order by " + orderField;
         return sql;
     }

     /**
      * 为查询设计的饼图生成所需要的SQL语句
      * @param queryId int
      * @return String[]
      * @throws SQLException
      * @throws ResKeyException
      * @throws ErrMsgException
      */
     public String[] getSmartQueryChartPie(HttpServletRequest request, int queryId) throws SQLException,ResKeyException,ErrMsgException  {
         FormQueryDb aqd = new FormQueryDb();
         aqd = aqd.getFormQueryDb(queryId);

         String fieldDesc = aqd.getChartPie();
         boolean isSeted = !"".equals(fieldDesc);
         if (!isSeted) {
             return null;
         }

         String fieldOptDb = "";
         String[] ary = StrUtil.split(fieldDesc, ";");
         String chartFieldCode = ary[0];
         fieldOptDb = ary[1];
         String[] opts = StrUtil.split(fieldOptDb, ",");

         String calcFieldCode = "";
         String calcFunc = "0";
         if (ary.length > 2) {
             calcFieldCode = ary[2];
             calcFunc = ary[3];
         }

         FormDb fd = new FormDb();
         fd = fd.getFormDb(aqd.getTableCode());
         FormField chartFormField = fd.getFormField(chartFieldCode);

         // LogUtil.getLog(ArchiveSQLBuilder.class).info("fieldOptDb=" + fieldOptDb + " queryId=" + queryId);

         String[] ret = new String[opts.length];
         Vector condV = getSmartQueryCond(aqd, "");

         for (int i = 0; i < opts.length; i++) {
             ret[i] = getSmartQuerySQL(aqd, condV, chartFormField, opts[i], calcFieldCode, calcFunc);
         }
         return ret;
     }


     /**
      * 为查询设计的柱状图生成所需要的SQL语句
      * @param queryId int
      * @return String[]
      * @throws SQLException
      * @throws ResKeyException
      * @throws ErrMsgException
      */
     public String[] getSmartQueryChartHistogram(HttpServletRequest request, int queryId) throws SQLException,ResKeyException,ErrMsgException  {
         FormQueryDb aqd = new FormQueryDb();
         aqd = aqd.getFormQueryDb(queryId);

         String fieldDesc = aqd.getChartHistogram();
         boolean isSeted = !"".equals(fieldDesc);
         if (!isSeted)
             return null;
         String fieldOptDb = "";

         String[] ary = StrUtil.split(fieldDesc, ";");

         String chartFieldCode = ary[0];
         fieldOptDb = ary[1];
         String[] opts = StrUtil.split(fieldOptDb, ",");

         String calcFieldCode = "";
         String calcFunc = "0";
         if (ary.length > 2) {
             calcFieldCode = ary[2];
             calcFunc = ary[3];
         }

         FormDb fd = new FormDb();
         fd = fd.getFormDb(aqd.getTableCode());
         FormField chartFormField = fd.getFormField(chartFieldCode);

         String[] ret = new String[opts.length];

         Vector condV = getSmartQueryCond(aqd, "");

         for (int i = 0; i < opts.length; i++) {
             ret[i] = getSmartQuerySQL(aqd, condV, chartFormField, opts[i], calcFieldCode, calcFunc);
         }
         return ret;
     }

     /**
      * 为查询设计的折线图生成所需要的SQL语句
      * @param queryId int
      * @return String[]
      * @throws SQLException
      * @throws ResKeyException
      * @throws ErrMsgException
      */
     public String[] getSmartQueryChartLine(HttpServletRequest request,
                                                   int queryId) throws
             SQLException, ResKeyException, ErrMsgException {
         FormQueryDb aqd = new FormQueryDb();
         aqd = aqd.getFormQueryDb(queryId);

         String fieldDesc = aqd.getChartLine();
         boolean isSeted = !"".equals(fieldDesc);
         if (!isSeted) {
             return null;
         }
         int beginYear, endYear;

         String fieldOptDb = "";
         String[] ary = StrUtil.split(fieldDesc, ";");
         String chartFieldCode = ary[0];
         fieldOptDb = ary[1];

         String calcFieldCode = "";
         String calcFunc = "0";
         if (ary.length > 2) {
             calcFieldCode = ary[2];
             calcFunc = ary[3];
         }

         FormDb fd = new FormDb();
         fd = fd.getFormDb(aqd.getTableCode());
         FormField chartFormField = fd.getFormField(chartFieldCode);

         String[] ary2 = fieldOptDb.split("-");
         beginYear = StrUtil.toInt(ary2[0]);
         endYear = StrUtil.toInt(ary2[1]);

         Vector condV = getSmartQueryCond(aqd, "");

         String[] ret = new String[endYear - beginYear + 1];
         int k = 0;
         Calendar cal = Calendar.getInstance();
         java.util.Date d = DateUtil.parse("2001-01-01", "yyyy-MM-dd");
         for (int y = beginYear; y <= endYear; y++) {
             cal.setTime(d);
             cal.set(y, 11, 31);

             ret[k] = getSmartQuerySQL(aqd, condV, chartFormField, DateUtil.format(cal, "yyyy-MM-dd"), calcFieldCode, calcFunc);
             k++;
         }
         return ret;
     }

     /**
      * 为同比报表生成SQL语句
      * @param queryId int
      * @return String[]
      * @throws SQLException
      * @throws ResKeyException
      * @throws ErrMsgException
      */
     public String[][] getSmartQueryChartTb(HttpServletRequest request, int queryId) throws SQLException,ResKeyException,ErrMsgException  {
         FormQueryDb aqd = new FormQueryDb();
         aqd = aqd.getFormQueryDb(queryId);

         Vector condV = getSmartQueryCond(aqd, "");

         String fieldDesc = aqd.getChartTb();

         String fieldOptDb = "";
         String[] ary = StrUtil.split(fieldDesc, ";");
         String chartFieldCode = ary[0];
         fieldOptDb = ary[1];

         String calcFieldCode = "";
         String calcFunc = "0";
         if (ary.length > 2) {
             calcFieldCode = ary[2];
             calcFunc = ary[3];
         }

         FormDb fd = new FormDb();
         fd = fd.getFormDb(aqd.getTableCode());
         FormField chartFormField = fd.getFormField(chartFieldCode);

         boolean isSeted = !fieldDesc.equals("");
         if (!isSeted) {
             return null;
         }
         int year1,year2;

         String[] ary2 = fieldOptDb.split("-");
         year1 = StrUtil.toInt(ary2[0]);
         year2 = StrUtil.toInt(ary2[1]);
         Calendar cal = Calendar.getInstance();

         java.util.Date d = DateUtil.parse("2001-01-01", "yyyy-MM-dd");
         String[][] ret = new String[2][12];
         int[] year = {year1, year2};
         for (int n=0; n<year.length; n++) {
             for (int m = 0; m <= 11; m++) {
                 cal.setTime(d);
                 cal.set(year[n], m, DateUtil.getDayCount(year[n], m));

                 ret[n][m] = getSmartQuerySQL(aqd, condV, chartFormField, DateUtil.format(cal, "yyyy-MM-dd"), calcFieldCode, calcFunc);
             }
         }

         return ret;
     }

     /**
      * 替换sql中的id为某字段名，取得某字段的累加值
      * @param sql String
      * @param field String
      * @return double
      */
     public static double getSUMOfSQL(String sql, String field) {
         sql = sql.toLowerCase();
         field = field.toLowerCase();
         // 从sql语句中找到field，判断其是否含有别名
         // 但sql可能为：select id from ft_bxdjkzb t1 where cws_id='57' and flowid=1497 and 1=1 and t1.jkje>3 order by id desc，field为jkje
         // 此时select ***部分无jkje，所以需在第一个where之前去寻找有无别名
         int p = sql.indexOf(" where ");
         String sqlBeforeWhere = sql.substring(0, p);
         int m = sqlBeforeWhere.indexOf("." + field);
         if (m!=-1) {
             boolean isNotBlankOrComma = true;
             StringBuilder sb = new StringBuilder();
             while (isNotBlankOrComma) {
                String str = sqlBeforeWhere.substring(m-1, m);
                if ("".equals(str) || ",".equals(str)) {
                    isNotBlankOrComma = false;
                }
                else {
                    sb.insert(0, str);
                }
                m--;
                if (m<=1) {
                    break;
                }
             }
             field = sb.toString() + "." + field;
         }

         p = sql.indexOf(" from ");
         sql = sql.substring(0, "select ".length()) + "sum(" + field + ")"
                 + sql.substring(p);
         JdbcTemplate jt = new JdbcTemplate();
         ResultIterator ri = null;
         try {
             ri = jt.executeQuery(sql);
             if (ri.hasNext()) {
                 ResultRecord rr = (ResultRecord) ri.next();
                 return rr.getDouble(1);
             }
         } catch (SQLException ex) {
             LogUtil.getLog(FormSQLBuilder.class).error(ex);
         }

         return 0;
     }
     
 	/**
 	 * 流程中嵌入的查询，更换主表单条件后再次搜索
 	 * @param request
 	 * @param fqd
 	 * @param lf
 	 * @return
 	 * @throws ErrMsgException
 	 */
 	public String getSmartQueryOnFlowChangCondValue(HttpServletRequest request, FormQueryDb fqd, Leaf lf) throws ErrMsgException {
     	if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
            ;
        } else {
     		throw new ErrMsgException("系统版本中无此功能！");
     	}
     	
     	String sql = null;

 		try {
 			FormSQLBuilder fsb = new FormSQLBuilder();
 			sql = fsb.getSmartQuery(request, fqd.getId());  
 			
 			StringBuffer sb = new StringBuffer();
 			
 			Map mapCondValue = new HashMap();
 			try {
 				JSONObject json = new JSONObject(lf.getQueryCondMap());
 				Iterator irJson = json.keys();							
 				while (irJson.hasNext()) {
 					String qField = (String) irJson.next();
 					String condField = json.getString(qField);
 					
 					mapCondValue.put(condField, ParamUtil.get(request, qField));
 				}
 			} catch (JSONException e1) {
 				throw new ErrMsgException("请检查流程属性中表单与查询条件字段的映射关系是否正确！");
 			}

 	    	// 取得条件字段的类型
 			String fields = "";
 			Iterator ir = mapCondValue.keySet().iterator();
 			while (ir.hasNext()) {
 				String fieldName = (String)ir.next();
 				if (fields.equals("")) {
 					fields = fieldName;
 				}
 				else {
 					fields += "," + fieldName;
 				}
 			}

 			// 取得mapType，即字段类型
 			int p = sql.toLowerCase().indexOf(" from ");
 			String sqlCond = "select " + fields + sql.substring(p);
 			JdbcTemplate jt = new JdbcTemplate();
 			ResultIterator ret = null;
 			try {
 				ret = jt.executeQuery(sqlCond, 1, 1);
 			} catch (SQLException e) {
 				LogUtil.getLog(getClass()).error(e);
 			}
 			Map mapType = ret.getMapType();
 			
 			QueryScriptUtil qsu = new QueryScriptUtil();
 			
 			LogUtil.getLog(getClass()).info("sql:" + sql);
 			
 			sql = qsu.getSqlExpressionReplacedWithFieldValue(sql, mapCondValue, mapType);
 			
 			LogUtil.getLog(getClass()).info("sqlReplaced:" + sql);
 			
 			if (sql==null) {
                throw new ErrMsgException("SQL语句非法，解析失败！");
            }
 		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(e);
		}

 		return sql;
 	}	     

	public void setForRelateModule(boolean isForRelateModule) {
		this.isForRelateModule = isForRelateModule;
	}

	public boolean isForRelateModule() {
		return isForRelateModule;
	}

	public void setForSelField(boolean forSelField) {
		this.forSelField = forSelField;
	}

	public boolean isForSelField() {
		return forSelField;
	}

	public void setMapConds(Map mapConds) {
		this.mapConds = mapConds;
	}

	public Map getMapConds() {
		return mapConds;
	}

}
