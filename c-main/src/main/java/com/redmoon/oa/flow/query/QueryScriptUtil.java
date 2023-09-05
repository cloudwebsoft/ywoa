package com.redmoon.oa.flow.query;

import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.cloudweb.oa.api.IQueryScriptUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.sys.DebugUtil;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.sql.Const;
import com.redmoon.oa.sql.SqlNode;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import bsh.EvalError;
import bsh.Interpreter;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SQLUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormQueryDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.visual.FormDAO;

public class QueryScriptUtil {
	HashMap mapIndex; // ResultIterator中存放的字段索引映射，key为结果集中的字段名称，可能为alias
	HashMap mapFieldTitle; // value中存放字段的标题，key为结果集中的字段名称，可能为alias
	// HashMap mapFieldName; // value中存放字段的编码，key为结果集中的字段名称，可能为alias
	
	Map mapFieldType; // value中 存放字段的java.sql类型
	
	/**
	 * 条件字段
	 */
	Vector condFields = new Vector();
	
	public Map getMapFieldType() {
		return mapFieldType;
	}

	public void setMapFieldType(Map mapFieldType) {
		this.mapFieldType = mapFieldType;
	}

	/**
	 * 条件节点
	 */
	Vector condNodes = new Vector();
	
	Map condTitleMap = new HashMap();
	
	long total = 0;
	int page = 1;
	
	/**
	 * 脚本中的sql
	 */
	private String sql = "";
	
	public static final String CWS_OP = "cws_op";
	
	/**
	 * 运行查询
	 * @param request
	 * @param fqd
	 * @return
	 * @throws ErrMsgException
	 */
	public ResultIterator executeQuery(HttpServletRequest request, FormQueryDb fqd) throws ErrMsgException {
    	if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
			;
		} else {
    		throw new ErrMsgException("系统版本中无此功能！");
    	}
    	
		Privilege pvg = new Privilege();
		String myscript = fqd.getScripts();
		Interpreter bsh = new Interpreter();
		try {
			StringBuffer sb = new StringBuffer();

			// 赋值给用户
			sb.append("userName=\"" + pvg.getUser(request) + "\";");
			bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			String orderBy = ParamUtil.get(request, "orderBy");
			String sort = ParamUtil.get(request, "sort");
			
			// 解决sql中需替换的替换符，如：$curDate
			String sql = parseSql(myscript);
			String sqlReplaced = SQLUtil.change(sql, pvg.getUser(request));			
			myscript = myscript.replace(sql, sqlReplaced);
			
			sql = sqlReplaced;

			// 如果有排序，则在SQL语句中加入（原来没有）或替换第一个排序
			if (!orderBy.equals("") && !sort.equals("")) {
				// String sql = parseSql(myscript);
				sqlReplaced = getSqlOrderByReplaced(sql, orderBy, sort);
				LogUtil.getLog(getClass()).info("executeQuery sql:" + sql + " sqlReplaced:" + sqlReplaced);
				if (sqlReplaced==null) {
					throw new ErrMsgException("SQL语句非法，解析失败！");
				}
				
				myscript = myscript.replace(sql, sqlReplaced);
				sql = sqlReplaced;
			}
			
			String action = ParamUtil.get(request, "action");
			if (action.equals("searchInResult")) {
				// String sql = parseSql(myscript);
				sqlReplaced = getSqlSearchInResult(request, fqd, sql);
				LogUtil.getLog(getClass()).info("executeQuery sql:" + sql + " sqlReplaced:" + sqlReplaced);
				if (sqlReplaced==null) {
					throw new ErrMsgException("SQL语句非法，解析失败！");
				}
				
				myscript = myscript.replace(sql, sqlReplaced);				
			}
			
			bsh.eval(myscript);
			Object obj = bsh.get("ri");
			if (obj != null) {
				// sql语句中如果有as，mapFieldTitle中存储的将会是别名
				ResultIterator ri = (ResultIterator) obj;
				
				sql = (String)bsh.get("sql");
				
				total = ((Long)bsh.get("total")).longValue();
				page = ((Integer)bsh.get("page")).intValue();

				this.sql = (String)bsh.get("sql");				

				mapIndex = ri.getMapIndex();
				mapFieldTitle = ri.getMapLabel();
				mapFieldType = ri.getMapType();
				
				if (ri.size()==0) {
					return ri;
				}
				
				/*
				mapFieldTitle = (HashMap)mapIndex.clone();
				Iterator ir = mapFieldTitle.keySet().iterator();
				while (ir.hasNext()) {
					String keyName = (String) ir.next();
					// Integer v = (Integer) mapFieldTitle.get(keyName);
					mapFieldTitle.put(keyName.toUpperCase(), keyName);
					// out.print(keyName + "=" + v + "<BR>");
				}
				
				mapFieldName = (HashMap)mapFieldTitle.clone();
				
				// 从SQL语句中取出相应字段的别名，将mapFieldTitle中key为别名的val置为字段名
				String sql = (String)bsh.get("sql");
				CCJSqlParserManager pm = new CCJSqlParserManager();
				net.sf.jsqlparser.statement.Statement statement;
				try {
					statement = pm.parse(new StringReader(sql));
				} catch (JSQLParserException e) {
					LogUtil.getLog(getClass()).error(e);
					return null;
				}
				
				if (statement instanceof Select) {
					Select selectStatement = (Select) statement;
					TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
					List tableList = tablesNamesFinder.getTableList(selectStatement);
						
					PlainSelect plainSelect = (PlainSelect)selectStatement.getSelectBody();
					// 根据sql语句中的别名，将别名对应的字段名存入mapFieldTitle的value中
					List list = plainSelect.getSelectItems();
					int len = list.size();
					for (int j=0;j<list.size();j++) {
						Object itemObj = list.get(j);
						if(itemObj.getClass()==SelectExpressionItem.class) {
							SelectExpressionItem item = (SelectExpressionItem)itemObj;
							String exp = item.getExpression().toString();
							// sql语句解析出的字段名可能为#T#.GWLX
							int p = exp.indexOf("#.");
							if (p!=-1) {
								exp = exp.substring(p + 2);
							}
							// 取别名
							if (item.getAlias()!=null) {
								mapFieldName.put(item.getAlias().toUpperCase(), exp);
								// out.print("item.getExpression()=" + exp + "&nbsp;&nbsp;item.getAlias()=" + item.getAlias() + "<BR>");
							}
						}else if(AllColumns.class==itemObj.getClass()) {
							// out.println("*" + "<BR>");
						}
					}
					
					for (Iterator iter = tableList.iterator(); iter.hasNext();) {
						String tableName = (String) iter.next();
						tableName = tableName.replaceAll("#", "");
						String formCode = FormDb.getCodeByTableName(tableName);
						// out.print("tableName=" + tableName + "&nbsp;&nbsp;formCode=" + formCode + "<BR>");
						// 将字段的中文名称存在mapFieldTitle的value
						if (formCode!=null) {
							FormDb fd = new FormDb();
							fd = fd.getFormDb(formCode);
							
							Iterator irMap = mapFieldTitle.keySet().iterator();
							while (irMap.hasNext()) {
								String keyName = (String) irMap.next();
								String columnCode = (String) mapFieldName.get(keyName);
								FormField ff = fd.getFormField(columnCode);
								if (ff!=null) {
									mapFieldTitle.put(keyName.toUpperCase(), ff.getTitle());
								}
							}
						}
					}
				}
				*/

				return ri;
			}
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return null;
	}
	
	/**
	 * 更改条件字段的值，用于查询列表中更改条件按钮及嵌入流程中的查询
	 * @param sql
	 * @param mapCondValue
	 * @return
	 */
	public String getSqlExpressionReplacedWithFieldValue(String sql, Map mapCondValue, Map mapCondType) {
		IQueryScriptUtil queryScriptUtil = SpringUtil.getBean(IQueryScriptUtil.class);
		return queryScriptUtil.getSqlExpressionReplacedWithFieldValue(sql, mapCondValue, mapCondType);
	}	
	
	/**
	 * 关联选项卡时，以主表中的字段值，替换相应条件字段的值
	 * @param sql
	 * @param moduleFdao
	 * @param jsonTabSetup
	 * @return
	 */
	public String getSqlExpressionReplacedWithFieldValue(String sql, FormDAO moduleFdao, JSONObject jsonTabSetup) {
		IQueryScriptUtil queryScriptUtil = SpringUtil.getBean(IQueryScriptUtil.class);
		return queryScriptUtil.getSqlExpressionReplacedWithFieldValue(sql, moduleFdao, jsonTabSetup);
	}
	
	/**
	 * 当拉单时，根据主表中的条件字段的值去替换SQL
	 * @param sql
	 * @param parentFormDb
	 * @param mapsCond
	 * @param mapCondValue
	 * @return
	 */
	public String getSqlExpressionReplacedWithFieldValue(String sql, FormDb parentFormDb, JSONArray mapsCond, HashMap mapCondValue) {
		IQueryScriptUtil queryScriptUtil = SpringUtil.getBean(IQueryScriptUtil.class);
		return queryScriptUtil.getSqlExpressionReplacedWithFieldValue(sql, parentFormDb, mapsCond, mapCondValue);
	}	
	
	/**
	 * 查询列表页中form_query_script_list.jsp中，用于排序时替换sql语句中的order by
	 * @param sql
	 * @param orderBy
	 * @param sort
	 * @return
	 */
	public String getSqlOrderByReplaced(String sql, String orderBy, String sort) {
		IQueryScriptUtil queryScriptUtil = SpringUtil.getBean(IQueryScriptUtil.class);
		return queryScriptUtil.getSqlOrderByReplaced(sql, orderBy, sort);
	}
	
	public String getSqlSearchInResult(HttpServletRequest request, FormQueryDb fqd, String sql) {
		IQueryScriptUtil queryScriptUtil = SpringUtil.getBean(IQueryScriptUtil.class);
		return queryScriptUtil.getSqlSearchInResult(request, fqd, sql);
	}	
	
	/**
	 * 查询结果中更改条件后再搜索
	 * @param request
	 * @param fqd
	 * @return
	 * @throws ErrMsgException
	 */
	public ResultIterator executeQueryOnChangCondValue(HttpServletRequest request, FormQueryDb fqd) throws ErrMsgException {
    	if (com.redmoon.oa.kernel.License.getInstance().isPlatform())
    		;
    	else {
    		throw new ErrMsgException("系统版本中无此功能！");
    	}
    	
		Privilege pvg = new Privilege();
		String myscript = fqd.getScripts();
		Interpreter bsh = new Interpreter();
		try {
			StringBuffer sb = new StringBuffer();
						
			Map mapCondValue = getCondFields(request, fqd);
			
	    	// 取得条件字段的类型
			String fields = "";
			Iterator ir = mapCondValue.keySet().iterator();
			while (ir.hasNext()) {
				String fieldName = (String)ir.next();
				mapCondValue.put(fieldName, ParamUtil.get(request, fieldName));
				if (fields.equals("")) {
					fields = fieldName;
				}
				else {
					fields += "," + fieldName;
				}
			}

			sql = parseSql(myscript);
			int p = sql.toLowerCase().indexOf(" from ");
			String sqlCond = "select " + fields + sql.substring(p);
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ret = null;
			try {
				// 解决sql中需替换的替换符，如：$curDate
				sqlCond = SQLUtil.change(sqlCond, pvg.getUser(request));
				ret = jt.executeQuery(sqlCond, 1, 1);
			} catch (SQLException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			Map mapType = ret.getMapType();
	    	
	    	// 运行一次，以取得mapType，即字段类型
	    	// ResultIterator ret = executeQuery(request, fqd);
	    	// Map mapType = ret.getMapType();
			
			
			// 解决sql中需替换的替换符，如：$curDate
			String sqlReplaced = SQLUtil.change(sql, pvg.getUser(request));				
			sqlReplaced = getSqlExpressionReplacedWithFieldValue(sqlReplaced, mapCondValue, mapType);
			
			DebugUtil.i(getClass(), "executeQueryOnChangCondValue", "sql:" + sql + " sqlReplaced:" + sqlReplaced);
			
			if (sqlReplaced==null) {
				throw new ErrMsgException("SQL语句非法，解析失败！");
			}
			
			myscript = myscript.replace(sql, sqlReplaced);
			// fqd.setScripts(myscript);
			// fqd.save();
			
			// 赋值给用户
			sb.append("userName=\"" + pvg.getUser(request) + "\";");
			bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			bsh.eval(myscript);
			Object obj = bsh.get("ri");
			if (obj != null) {
				// sql语句中如果有as，mapFieldTitle中存储的将会是别名
				ResultIterator ri = (ResultIterator) obj;
				
				sql = (String)bsh.get("sql");				
				mapIndex = ri.getMapIndex();
				mapFieldType = ri.getMapType();
				
				total = ((Long)bsh.get("total")).longValue();
				page = ((Integer)bsh.get("page")).intValue();
				return ri;
			}
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return null;
	}
	
	/**
	 * 流程中嵌入的查询，更换主表单条件后再次搜索
	 * @param request
	 * @param fqd
	 * @param lf 流程类型
	 * @return
	 * @throws ErrMsgException
	 */
	public ResultIterator executeQueryOnQueryInFlowChangCondValue(HttpServletRequest request, FormQueryDb fqd, Leaf lf) throws ErrMsgException {
    	if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
			;
		} else {
    		// 注释掉，否则会议申请会报错
    		// throw new ErrMsgException("系统版本中无此功能！");
    	}
    	
		Privilege pvg = new Privilege();
		String myscript = fqd.getScripts();
		Interpreter bsh = new Interpreter();
		try {
			StringBuffer sb = new StringBuffer();
			
			Map mapCondValue = new HashMap();
			try {
				JSONObject json = new JSONObject(lf.getQueryCondMap());
				Iterator irJson = json.keys();							
				while (irJson.hasNext()) {
					String qField = (String) irJson.next();
					String condField = json.getString(qField);
					
					String val = ParamUtil.get(request, qField);
					// String val = request.getParameter(qField);
					if (val == null || "".equals(val)) {
						return null;
					}
					mapCondValue.put(condField, val);
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
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

			// 如果条件字段为空，则fields将为空
			if ("".equals(fields)) {
				fields = "*";
			}

			// 取得mapType，即字段类型
			sql = parseSql(myscript);
			int p = sql.toLowerCase().indexOf(" from ");
			String sqlCond = "select " + fields + sql.substring(p);
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ret = null;
			try {
				// 解决sql中需替换的替换符，如：$curDate
				sqlCond = SQLUtil.change(sqlCond, pvg.getUser(request));				
				ret = jt.executeQuery(sqlCond, 1, 1);
			} catch (SQLException e) {
				LogUtil.getLog(getClass()).error(" sqlCond=" + sqlCond + " SQL 语句异常");
				LogUtil.getLog(getClass()).error(e);
				throw new ErrMsgException("SQL 语句异常");
			}
			
			Map mapType = ret.getMapType();
			
			// 解决sql中需替换的替换符，如：$curDate						
			String sqlReplaced = SQLUtil.change(sql, pvg.getUser(request));			
			
			sqlReplaced = getSqlExpressionReplacedWithFieldValue(sqlReplaced, mapCondValue, mapType);
			
			LogUtil.getLog(getClass()).info("sql:" + sql + " sqlReplaced:" + sqlReplaced);
			
			if (sqlReplaced==null) {
				throw new ErrMsgException("SQL语句非法，解析失败！");
			}
						
			myscript = myscript.replace(sql, sqlReplaced);
			// fqd.setScripts(myscript);
			// fqd.save();
			
			// 赋值给用户
			sb.append("userName=\"" + pvg.getUser(request) + "\";");
			bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			bsh.eval(myscript);
			Object obj = bsh.get("ri");
			if (obj != null) {
				// sql语句中如果有as，mapFieldTitle中存储的将会是别名
				ResultIterator ri = (ResultIterator) obj;
				
				sql = (String)bsh.get("sql");				
				mapIndex = ri.getMapIndex();
				mapFieldType = ri.getMapType();
				
				total = ((Long)bsh.get("total")).longValue();
				page = ((Integer)bsh.get("page")).intValue();
				return ri;
			}
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return null;
	}			
	
	/**
	 * 关联选项卡查询
	 * @param request
	 * @param fqd
	 * @param moduleFdao
	 * @param jsonTabSetup
	 * @return
	 * @throws ErrMsgException
	 */
	public ResultIterator executeQuery(HttpServletRequest request, FormQueryDb fqd, FormDAO moduleFdao, JSONObject jsonTabSetup) throws ErrMsgException {
    	if (!com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
    		throw new ErrMsgException("系统版本中无此功能！");
    	}
    	
		Privilege pvg = new Privilege();
		String myscript = fqd.getScripts();
		Interpreter bsh = new Interpreter();
		try {
			StringBuilder sb = new StringBuilder();
			
			sql = parseSql(myscript);
			
			// 解决sql中需替换的替换符，如：$curDate
			String sqlReplaced = SQLUtil.change(sql, pvg.getUser(request));				
			sqlReplaced = getSqlExpressionReplacedWithFieldValue(sqlReplaced, moduleFdao, jsonTabSetup);
			
			LogUtil.getLog(getClass()).info("sql:" + sql + " sqlReplaced:" + sqlReplaced);
			
			if (sqlReplaced==null) {
				throw new ErrMsgException("SQL语句非法，解析失败！");
			}
			
			// 解决sql中需替换的替换符，如：$curDate
			sqlReplaced = SQLUtil.change(sqlReplaced, pvg.getUser(request));
			
			myscript = myscript.replace(sql, sqlReplaced);
			
			// 赋值给用户
			sb.append("userName=\"" + pvg.getUser(request) + "\";");
			bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			bsh.eval(myscript);
			Object obj = bsh.get("ri");
			if (obj != null) {
				// sql语句中如果有as，mapFieldTitle中存储的将会是别名
				ResultIterator ri = (ResultIterator) obj;
				
				sql = (String)bsh.get("sql");				
				mapIndex = ri.getMapIndex();
				mapFieldType = ri.getMapType();
				
				total = ((Long)bsh.get("total")).longValue();
				page = ((Integer)bsh.get("page")).intValue();
				return ri;
			}
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return null;
	}	
	
	/**
	 * 用于嵌套表格2拉单
	 * @param request
	 * @param fqd
	 * @param parentFormDb
	 * @param mapsCond
	 * @param mapCondValue
	 * @return
	 * @throws ErrMsgException
	 */
	public ResultIterator executeQuery(HttpServletRequest request, FormQueryDb fqd, FormDb parentFormDb, JSONArray mapsCond, HashMap mapCondValue) throws ErrMsgException {
    	if (com.redmoon.oa.kernel.License.getInstance().isPlatform())
    		;
    	else {
    		throw new ErrMsgException("系统版本中无此功能！");
    	}		
		Privilege pvg = new Privilege();
		String myscript = fqd.getScripts();
		Interpreter bsh = new Interpreter();
		try {
			myscript = SQLUtil.parseScript(request, myscript);
			
			StringBuffer sb = new StringBuffer();
			
			sql = parseSql(myscript);
			
			// 解决sql中需替换的替换符，如：$curDate
			String sqlReplaced = SQLUtil.change(sql, pvg.getUser(request));				
			sqlReplaced = getSqlExpressionReplacedWithFieldValue(sqlReplaced, parentFormDb, mapsCond, mapCondValue);
			
			LogUtil.getLog(getClass()).info("sql:" + sql + "   sqlReplaced:" + sqlReplaced);
			
			if (sqlReplaced==null) {
				throw new ErrMsgException("SQL语句非法，解析失败！");
			}
			
			String orderBy = ParamUtil.get(request, "orderBy");
			String sort = ParamUtil.get(request, "sort");
			// 如果有排序，则在SQL语句中加入（原来没有）或替换第一个排序
			if (!orderBy.equals("") && !sort.equals("")) {
				sqlReplaced = getSqlOrderByReplaced(sqlReplaced, orderBy, sort);
				LogUtil.getLog(getClass()).info("executeQuery sql:" + sql + "   sqlReplaced:" + sqlReplaced);
				if (sqlReplaced==null) {
					throw new ErrMsgException("SQL语句非法，解析失败！");
				}
			}
			
			// 如果是在结果中查询中，则增加条件
			String action = ParamUtil.get(request, "action");
			if (action.equals("searchInResult")) {			
				sqlReplaced = getSqlSearchInResult(request, fqd, sqlReplaced);
			}
				
			// 解决sql中需替换的替换符，如：$curDate
			sqlReplaced = SQLUtil.change(sqlReplaced, pvg.getUser(request));	
			
			myscript = myscript.replace(sql, sqlReplaced);
			
			// 赋值给用户
			sb.append("userName=\"" + pvg.getUser(request) + "\";");
			bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			bsh.eval(myscript);
			Object obj = bsh.get("ri");
			if (obj != null) {
				// sql语句中如果有as，mapFieldTitle中存储的将会是别名
				ResultIterator ri = (ResultIterator) obj;
				
				sql = (String)bsh.get("sql");
				
				mapIndex = ri.getMapIndex();
				mapFieldType = ri.getMapType();
				
				total = ((Long)bsh.get("total")).longValue();
				page = ((Integer)bsh.get("page")).intValue();
				return ri;
			}
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return null;
	}		
	
	public long getTotal() {
		return total;
	}

	public int getPage() {
		return page;
	}
	
	/**
	 * 取得字段名称，用于报表设计器
	 * @param request
	 * @param fqd
	 * @return
	 */
	public HashMap getCols(HttpServletRequest request, FormQueryDb fqd) {
		Privilege pvg = new Privilege();
		String myscript = fqd.getScripts();
		Interpreter bsh = new Interpreter();
		try {
			StringBuffer sb = new StringBuffer();

			String sql = parseSql(myscript);
			if (sql==null) {
				return new HashMap();
			}
			String sqlReplaced = SQLUtil.change(sql, pvg.getUser(request));			
			myscript = myscript.replace(sql, sqlReplaced);
			
			// 赋值给用户
			sb.append("userName=\"" + pvg.getUser(request) + "\";");
			bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			bsh.eval(myscript);
			Object obj = bsh.get("ri");
			if (obj != null) {
				// sql语句中如果有as，mapFieldTitle中存储的将会是别名
				ResultIterator ri = (ResultIterator)obj;
				
				mapIndex = ri.getMapIndex();
				mapFieldTitle = ri.getMapLabel();
				mapFieldType = ri.getMapType();

				// 从SQL语句中取出相应字段的别名，将mapFieldTitle中key为别名的val置为字段名
				sql = (String)bsh.get("sql");
				CCJSqlParserManager pm = new CCJSqlParserManager();
				net.sf.jsqlparser.statement.Statement statement;
				try {
					statement = pm.parse(new StringReader(sql));
				} catch (JSQLParserException e) {
					LogUtil.getLog(getClass()).error(e);
					return null;
				}
				
				if (statement instanceof Select) {
					Select selectStatement = (Select) statement;
					TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
					List tableList = tablesNamesFinder.getTableList(selectStatement);
					
					// 遍历表格，如果其中有表单型的，则置其字段别名为字段名称
					for (Iterator iter = tableList.iterator(); iter.hasNext();) {
						String tableName = (String) iter.next();
						tableName = tableName.replaceAll("#", "");
						String formCode = FormDb.getCodeByTableName(tableName);
						// out.print("tableName=" + tableName + "&nbsp;&nbsp;formCode=" + formCode + "<BR>");
						// 将字段的中文名称存在mapFieldTitle的value
						if (formCode!=null) {
							FormDb fd = new FormDb();
							fd = fd.getFormDb(formCode);
							
							Iterator irMap = mapFieldTitle.keySet().iterator();
							while (irMap.hasNext()) {
								String keyName = (String) irMap.next();
								FormField ff = fd.getFormField(keyName);
								if (ff!=null) {
									mapFieldTitle.put(keyName.toUpperCase(), ff.getTitle());
								}
							}
						}
					}
				}
			}
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			// LogUtil.getLog(getClass()).error(e);
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return mapFieldTitle;		
	}

	public String getColProps(HttpServletRequest request, FormQueryDb fqd) {
		Privilege pvg = new Privilege();
		String myscript = fqd.getScripts();
		Interpreter bsh = new Interpreter();
		try {
			StringBuffer sb = new StringBuffer();

			// 赋值给用户
			sb.append("userName=\"" + pvg.getUser(request) + "\";");
			bsh.set("request", request);
			// bsh.set("out", out);
			bsh.eval(sb.toString());

			// 解决sql中需替换的替换符，如：$curDate
			String sql = parseSql(myscript);
			String sqlReplaced = SQLUtil.change(sql, pvg.getUser(request));
			myscript = myscript.replace(sql, sqlReplaced);

			bsh.eval(myscript);
			Object obj = bsh.get("ri");
			if (obj != null) {
				// sql语句中如果有as，mapFieldTitle中存储的将会是别名
				ResultIterator ri = (ResultIterator)obj;
				if (ri.size()==0) {
					return "[]";
				}
				mapIndex = ri.getMapIndex();
				mapFieldTitle = ri.getMapLabel();
				
				/*
				mapFieldTitle = (HashMap)mapIndex.clone();
				Iterator ir = mapFieldTitle.keySet().iterator();
				while (ir.hasNext()) {
					String keyName = (String) ir.next();
					// Integer v = (Integer) mapFieldTitle.get(keyName);
					mapFieldTitle.put(keyName.toUpperCase(), keyName);
					// out.print(keyName + "=" + v + "<BR>");
				}

				HashMap mapFieldName = (HashMap)mapFieldTitle.clone();
				*/
				
				
				// 从SQL语句中取出相应字段的别名，将mapFieldTitle中key为别名的val置为字段名
				sql = (String)bsh.get("sql");
				CCJSqlParserManager pm = new CCJSqlParserManager();
				net.sf.jsqlparser.statement.Statement statement;
				try {
					statement = pm.parse(new StringReader(sql));
				} catch (JSQLParserException e) {
					LogUtil.getLog(getClass()).error(e);
					return null;
				}
				
				if (statement instanceof Select) {
					Select selectStatement = (Select) statement;
					TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
					List tableList = tablesNamesFinder.getTableList(selectStatement);

					/*
					// 根据sql语句中的别名，将别名对应的字段名存入mapFieldTitle的value中
					PlainSelect plainSelect = (PlainSelect)selectStatement.getSelectBody();
					List list = plainSelect.getSelectItems();
					int len = list.size();
					for (int j=0;j<list.size();j++) {
						Object itemObj = list.get(j);
						if(itemObj.getClass()==SelectExpressionItem.class) {
							SelectExpressionItem item = (SelectExpressionItem)itemObj;
							String exp = item.getExpression().toString();
							// sql语句解析出的字段名可能为#T#.GWLX
							int p = exp.indexOf("#.");
							if (p!=-1) {
								exp = exp.substring(p + 2);
							}
							if (item.getAlias()!=null) {
								mapFieldName.put(item.getAlias().toUpperCase(), exp);
								// out.print("item.getExpression()=" + exp + "&nbsp;&nbsp;item.getAlias()=" + item.getAlias() + "<BR>");
							}
						}else if(AllColumns.class==itemObj.getClass()) {
							// out.println("*" + "<BR>");
						}
					}
					*/
					
					// 遍历表格，如果其中有表单型的，则置其字段别名为字段名称
					for (Iterator iter = tableList.iterator(); iter.hasNext();) {
						String tableName = (String) iter.next();
						tableName = tableName.replaceAll("#", "");
						String formCode = FormDb.getCodeByTableName(tableName);
						// out.print("tableName=" + tableName + "&nbsp;&nbsp;formCode=" + formCode + "<BR>");
						// 将字段的中文名称存在mapFieldTitle的value
						if (formCode!=null) {
							FormDb fd = new FormDb();
							fd = fd.getFormDb(formCode);
							
							Iterator irMap = mapFieldTitle.keySet().iterator();
							while (irMap.hasNext()) {
								String keyName = (String) irMap.next();
								FormField ff = fd.getFormField(keyName.toLowerCase());
								if (ff!=null) {
									mapFieldTitle.put(keyName.toUpperCase(), ff.getTitle());
								}
							}
						}
					}
				}
				
				String colProps = "";

				Iterator ir = mapFieldTitle.keySet().iterator();
				while (ir.hasNext()) {
					String keyName = (String) ir.next();
					/*
					// 不能用mapFieldName，因为当导出Excel时，需要根据colProps中的name来从ResultRecord中获取值
					// 因此name只能为结果集中的keyName，否则当结果集中有别名时，将无法从ResultRecord中取值
					if (colProps.equals(""))
						colProps = "{display: '" + mapFieldTitle.get(keyName) + "', name : '" + mapFieldName.get(keyName) + "', width : 50, sortable : true, align: 'center', hide: false}";
					else
						colProps += ",{display: '" + mapFieldTitle.get(keyName) + "', name : '" + mapFieldName.get(keyName) + "', width : 50, sortable : true, align: 'center', hide: false}";
					*/
					if (colProps.equals("")) {
						colProps = "{display: '" + mapFieldTitle.get(keyName) + "', name : '" + keyName + "', width : 50, sortable : true, align: 'center', hide: false}";
					} else {
						colProps += ",{display: '" + mapFieldTitle.get(keyName) + "', name : '" + keyName + "', width : 50, sortable : true, align: 'center', hide: false}";
					}
					
				}
				
				// 加上操作列
				colProps += ",{display: '操作', name : '" + CWS_OP + "', width : 50, sortable : true, align: 'center', hide: false}";

				colProps = "[" + colProps + "]";
				
				return colProps;
				
				/*
				Iterator irMap = mapFieldTitle.keySet().iterator();
				while (irMap.hasNext()) {
					String keyName = (String) irMap.next();
					out.print("map:" + keyName + "=" + mapFieldTitle.get(keyName) + "<BR>");
				}
				*/		
				
				// 根据mapFieldTitle中val的字段，从对应的tableNames中取出字段名
				
				/*
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					out.println(" " + rr.getString("id") + " ztc=" + rr.getString("ztc"));
				}
				*/
			}
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return null;
	}
	
	/**
	 * 获取最后一个sql语句
	 * @param scripts
	 * @return
	 */
	public String parseSql(String scripts) {
		String pat = "sql[ ]*=[ ]*\"(.*?)\"";

		Pattern p = Pattern.compile(pat, Pattern.DOTALL
				| Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(scripts); // "<select style=\"color\" name=ss' id=\"color\" title=hh' style=\"color\" value=蓝风><option value=xx selected>xx</option></select>");
		boolean result = m.find();
		String sql = null;
		
		while (result) {
			sql = m.group(1);
			result = m.find();
		}
		
		return sql;
	}
	
	/**
	 * 取得条件字段，用于关联
	 * @param request
	 * @param fqd
	 * @return
	 */
	public HashMap getCondFields(HttpServletRequest request, FormQueryDb fqd) throws ErrMsgException {
		condFields = new Vector();
		condNodes = new Vector();
		condTitleMap.clear();

		HashMap map = new HashMap();
		
		String sql = parseSql(fqd.getScripts());
		if (sql==null) {
			throw new ErrMsgException("脚本格式非法：未找到SQL语句！");
		}
		
		Privilege pvg = new Privilege();
		sql = SQLUtil.change(sql, pvg.getUser(request));			

		// 从SQL语句中取出相应字段的别名，将mapFieldTitle中key为别名的val置为字段名
		CCJSqlParserManager pm = new CCJSqlParserManager();
		net.sf.jsqlparser.statement.Statement statement;
		try {
			statement = pm.parse(new StringReader(sql));
		} catch (JSQLParserException e) {
			LogUtil.getLog(getClass()).error(e);
			return null;
		}
				
		if (statement instanceof Select) {
			Select selectStatement = (Select) statement;	
			
			TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
			List tableList = tablesNamesFinder.getTableList(selectStatement);
			PlainSelect plainSelect = (PlainSelect)selectStatement.getSelectBody();

			// 取得别名与表名的对应关系
			FromItem fromItem = plainSelect.getFromItem();
			List<Join> joins = plainSelect.getJoins();
			Map<String, String> tableMap = new HashMap<String, String>();
			if(joins != null) {
				for (Join join : joins) {
					Expression onExpression = join.getOnExpression();
					FromItem rightItem = join.getRightItem();
					if (rightItem instanceof Table) {
						Table table = (Table) rightItem;
						tableMap.put(table.getAlias().getName().toUpperCase(), table.getName());
					}
				}
			}
			if (fromItem instanceof Table) {
				Table table = (Table) fromItem;
				if (table.getAlias()!=null) {
					tableMap.put(table.getAlias().getName().toUpperCase(), table.getName());
				}
			}

			Expression expression = plainSelect.getWhere();
			SqlNode whereNode = new SqlNode();
			parseWhere(expression, whereNode);	
			
			// 填充hashmap
/*			Iterator ir = condFields.iterator();
			while (ir.hasNext()) {
				String fieldName = (String)ir.next();
				map.put(fieldName, fieldName);
			}*/

			if (tableMap.size()==0) {
				for (Iterator iter = tableList.iterator(); iter.hasNext(); ) {
					String tableName = (String) iter.next();
					tableName = tableName.replaceAll("#", "");
					String formCode = FormDb.getCodeByTableName(tableName);
					// out.print("tableName=" + tableName + "&nbsp;&nbsp;formCode=" + formCode + "<BR>");
					// 将字段的中文名称存在mapFieldTitle的value
					if (formCode != null) {
						FormDb fd = new FormDb();
						fd = fd.getFormDb(formCode);
						Iterator ir = condFields.iterator();
						while (ir.hasNext()) {
							String fieldName = (String) ir.next();
							// fieldName中可能带有别名
							fieldName = fieldName.replaceAll("#", "");
							if (fieldName.equalsIgnoreCase("cws_id")) {
								map.put(fieldName, "cws_id关联");
							} else {
								FormField ff = fd.getFormField(fieldName);
								if (ff != null) {
									map.put(fieldName, ff.getTitle());
								} else {
									map.put(fieldName, fieldName);
								}
							}
						}
					}
				}
			}
			else {
				Iterator ir = condFields.iterator();
				while (ir.hasNext()) {
					String fieldName = (String) ir.next();
					// fieldName中可能带有别名
					fieldName = fieldName.replaceAll("#", "");
					if (fieldName.indexOf("cws_id")!=-1) {
						map.put(fieldName, "cws_id关联");
					} else {
						int p = fieldName.indexOf(".");
						// 如果有别名
						if (p!=-1) {
							String alias = fieldName.substring(0, p).toUpperCase();
							String tableName = tableMap.get(alias);
							String fName = fieldName.substring(p+1);
							if (fName.equalsIgnoreCase("id")) {
								map.put(fieldName, fName);
							}
							else {
								String formCode = FormDb.getCodeByTableName(tableName);
								if (formCode != null) {
									FormDb fd = new FormDb();
									fd = fd.getFormDb(formCode);
									FormField ff = fd.getFormField(fName);
									if (ff != null) {
										map.put(fieldName, ff.getTitle());
									} else {
										map.put(fieldName, fieldName);
									}
								}
							}
						}
						else {
							// 如果没有别名，则遍历表，取出相应字段的名称
							for (Iterator iter = tableList.iterator(); iter.hasNext(); ) {
								String tableName = (String) iter.next();
								tableName = tableName.replaceAll("#", "");
								String formCode = FormDb.getCodeByTableName(tableName);
								if (formCode != null) {
									FormDb fd = new FormDb();
									fd = fd.getFormDb(formCode);
									if (fieldName.equalsIgnoreCase("cws_id")) {
										map.put(fieldName, "cws_id关联");
									} else {
										FormField ff = fd.getFormField(fieldName);
										if (ff != null) {
											map.put(fieldName, ff.getTitle());
										} else {
											map.put(fieldName, fieldName);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		condTitleMap = map;

		return map;
	}
	
    public Vector getCondFields() {
		return condFields;
	}

	public void setCondFields(Vector condFields) {
		this.condFields = condFields;
	}

	public Vector getCondNodes() {
		return condNodes;
	}

	public void setCondNodes(Vector condNodes) {
		this.condNodes = condNodes;
	}

	public Map getCondTitleMap() {
		return condTitleMap;
	}

	public void setCondTitleMap(Map condTitleMap) {
		this.condTitleMap = condTitleMap;
	}

	private static void checkNull(SqlNode node,boolean left) {
        if(left)
        {
             if(node.getLeftchild()==null) {
				 node.setLeftchild(new SqlNode());
			 }
        }else
        {
            if(node.getRightchild()==null) {
				node.setRightchild(new SqlNode());
			}
        }
    }
    
    private void parseWhere(Expression expression, SqlNode node) {
        if (expression == null) {
			return;
		}
        Class cls = expression.getClass();
        if (cls == AndExpression.class) {
            AndExpression and = (AndExpression) expression;
            node.setCondition("and");
            node.setConditionInt(Const.AND);
            checkNull(node, true);
            parseWhere(and.getLeftExpression(), node.getLeftchild());
 
            checkNull(node, false);
            parseWhere(and.getRightExpression(), node.getRightchild());
 
            return;
        } else if (OrExpression.class == cls) {
            OrExpression or = (OrExpression) expression;
 
            // node.setLeft(left)
            node.setCondition("or");
            node.setConditionInt(Const.OR);
            checkNull(node, true);
            parseWhere(or.getLeftExpression(), node.getLeftchild());
 
            checkNull(node, false);
            parseWhere(or.getRightExpression(), node.getRightchild());
 
            return;
        }
        else if(Parenthesis.class==cls){//逻辑子单元
            Parenthesis pt = (Parenthesis) expression;
            checkNull(node, true);
            parseWhere(pt.getExpression(), node);
 
            return;
        }
 
        checkNull(node, true);
        
        if(!processAtom(expression, node)){
            node.setLeftchild(null);
        }
 
    }
    

    /**
     * 最终颗粒处理 > >= = < <= <>
     * 
     * @param expression
     */
    private boolean processAtom(Expression expression, SqlNode node) {
        if (expression == null)
        {
            node = null;
            return false;
        }
 
        Class cls = expression.getClass();
                
        if (EqualsTo.class == cls) {
            EqualsTo et = (EqualsTo)expression;
            node.setCondition("=");
            node.setConditionInt(Const.EQUAL);
            node.setLeft(et.getLeftExpression().toString());
            node.setRight(et.getRightExpression().toString());
        } else if (GreaterThan.class == cls) {
            GreaterThan gt = (GreaterThan)expression;
            node.setCondition(">");
            node.setConditionInt(Const.GREATER);
            node.setLeft(gt.getLeftExpression().toString());
            node.setRight(gt.getRightExpression().toString());
        } else if (GreaterThanEquals.class == cls) {
            GreaterThanEquals gte = (GreaterThanEquals)expression;
            node.setCondition(">=");
            node.setConditionInt(Const.GREATER_EQUAL);
            node.setLeft(gte.getLeftExpression().toString());
            node.setRight(gte.getRightExpression().toString());
        } else if (MinorThan.class == cls) {
            MinorThan mt = (MinorThan)expression;
            node.setCondition("<");
            node.setConditionInt(Const.MINOR);
            node.setLeft(mt.getLeftExpression().toString());
            node.setRight(mt.getRightExpression().toString());
        } else if (MinorThanEquals.class == cls) {
            MinorThanEquals mte = (MinorThanEquals)expression;
            node.setCondition("<=");
            node.setConditionInt(Const.MINOR_EQUAL);
            node.setLeft(mte.getLeftExpression().toString());
            node.setRight(mte.getRightExpression().toString());
        }else if(Between.class==cls)
        {
            Between bet = (Between)expression;
            node.setCondition("between");
            node.setConditionInt(Const.BETWEEN);
            node.setLeft(bet.getLeftExpression().toString());
            node.setBetweenStart(bet.getBetweenExpressionStart().toString());
            node.setBetweenEnd(bet.getBetweenExpressionEnd().toString());
        }else if(InExpression.class == cls){
            InExpression in = (InExpression)expression;
            node.setLeft(in.getLeftExpression().toString());
            node.setConditionInt(Const.IN);
            node.setCondition("in");
			// String [] arr = in.getItemsList().toString().split(",");
			String [] arr = in.getRightItemsList().toString().split(",");
            node.setInList(arr);
        }
        else if (LikeExpression.class == cls) {
        	LikeExpression lk = (LikeExpression)expression;
        	node.setLeft(lk.getLeftExpression().toString());
        	node.setCondition("like");
            node.setRight(lk.getRightExpression().toString());        	
        }
        else if (NotEqualsTo.class == cls) { // 20180614 fgf 增加不等于的情况 
        	NotEqualsTo nt = (NotEqualsTo)expression;
        	node.setLeft(nt.getLeftExpression().toString());
        	node.setCondition("<>");
        	node.setRight(nt.getRightExpression().toString());
        }
        
        condFields.addElement(node.getLeft());
        
        condNodes.addElement(node);

        return true;
    }    	

	public HashMap getMapIndex() {
		return mapIndex;
	}

	public void setMapIndex(HashMap mapIndex) {
		this.mapIndex = mapIndex;
	}

	public HashMap getMapFieldTitle() {
		return mapFieldTitle;
	}

	public void setMapFieldTitle(HashMap mapFieldTitle) {
		this.mapFieldTitle = mapFieldTitle;
	}

	/*
	public HashMap getMapFieldName() {
		return mapFieldName;
	}

	public void setMapFieldName(HashMap mapFieldName) {
		this.mapFieldName = mapFieldName;
	}
	*/
	
	/**
	 * 检查脚本是否合法
	 * 暂时无用
	 * @param script
	 * @throws ErrMsgException
	 */
	public void check(String script) throws ErrMsgException {
		if (script.indexOf("ResultIterator ")==-1) {
			throw new ErrMsgException("脚本中没有返回结果集ResultIterator ！");
		}
		
	}
	
	public String getSql() {
		return sql;
	}
	
	/**
	 * 判断SQL中的表是否为表单
	 * @return
	 */
	public String getFormCode() {
		CCJSqlParserManager pm = new CCJSqlParserManager();
		net.sf.jsqlparser.statement.Statement statement;
		try {
			statement = pm.parse(new StringReader(sql));
		} catch (JSQLParserException e) {
			LogUtil.getLog(getClass()).error(e);
			return null;
		}
		
		if (statement instanceof Select) {
			Select selectStatement = (Select) statement;
			TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
			List tableList = tablesNamesFinder.getTableList(selectStatement);
			
			for (Iterator iter = tableList.iterator(); iter.hasNext();) {
				String tableName = (String) iter.next();
				tableName = tableName.replaceAll("#", "");
				String formCode = FormDb.getCodeByTableName(tableName);
				// 将字段的中文名称存在mapFieldTitle的value
				if (formCode!=null) {
					return formCode;
				}
			}
		}
		return null;
	}
	
    /**
     * 根据类型，自动获取相应表單字段类型對應的值
     * @param t int
     * @return Object
     * @throws SQLException
     */
    public static int getFieldTypeOfDBType(int t) {
    	int type = FormField.FIELD_TYPE_TEXT;
        switch (t) {
          case Types.ARRAY: break;
          case Types.BIGINT: type = FormField.FIELD_TYPE_INT; break;
          case Types.BINARY:  break;
          case Types.BIT: type = FormField.FIELD_TYPE_INT; break;
          case Types.BLOB: break;
          case Types.BOOLEAN: type = FormField.FIELD_TYPE_INT; break;
          case Types.CHAR:  break;
          case Types.CLOB:  break;
          case Types.DATALINK:  break;
          case Types.DATE: type = FormField.FIELD_TYPE_DATE; break;
          case Types.DECIMAL: type = FormField.FIELD_TYPE_DOUBLE; break;
          case Types.DISTINCT:  break;
          case Types.DOUBLE: type = FormField.FIELD_TYPE_DOUBLE; break;
          case Types.FLOAT: type = FormField.FIELD_TYPE_FLOAT; break;
          case Types.INTEGER: type = FormField.FIELD_TYPE_INT; break;
          case Types.JAVA_OBJECT: break;
          case Types.LONGVARBINARY: break;
          case Types.LONGVARCHAR: break;
          case Types.NULL: break;
          case Types.NUMERIC:type = FormField.FIELD_TYPE_DOUBLE;
              break;
          case Types.OTHER: break;
          case Types.REAL:
              if (Global.db.equals(Global.DB_MYSQL)) {
            	  type = FormField.FIELD_TYPE_FLOAT;
              }

              break; // MySQL的float对应于REAL，而SQLServer的REAL对应于REAL
          case Types.REF: break;
          case Types.SMALLINT: type = FormField.FIELD_TYPE_INT; break;
          case Types.STRUCT: break;
          case Types.TIME: type = FormField.FIELD_TYPE_DATETIME; break;
          case Types.TIMESTAMP: type = FormField.FIELD_TYPE_DATETIME; break;
          case Types.TINYINT: type = FormField.FIELD_TYPE_INT; break;
          case Types.VARBINARY: break;
          case Types.VARCHAR: break;
          default: ;
        }
        return type;
    }
    
    /**
     * 用于查询拉单的时候，将需要request传入的参数，预先解析出
     * @param queryId
     * @param parentFormCode
     * @return
     */
    public Vector<String> parseField(int queryId, String parentFormCode) {
    	Vector<String> v = new Vector<String>();
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb(parentFormCode);
    	
    	FormQueryDb fqd = new FormQueryDb();
    	fqd = fqd.getFormQueryDb(queryId);
    	
    	String scripts = fqd.getScripts();
    	
    	Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(scripts);
        while (m.find()) {
            String str = m.group(1);
            if (str.startsWith("request.")) {
            	String fieldName = str.substring("request.".length());
            	FormField ff = fd.getFormField(fieldName);
            	if (ff!=null) {
            		v.addElement(fieldName);
            	}
            }
        }
        return v;
    }

}
