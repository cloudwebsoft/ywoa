package com.redmoon.oa.flow.query;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormQueryConditionDb;
import com.redmoon.oa.flow.FormQueryDb;
import com.redmoon.oa.flow.FormSQLBuilder;
import com.redmoon.oa.visual.FormDAO;

import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a
 * string) an {@link net.sf.jsqlparser.expression.Expression}
 */
public class ExpressionDeParser implements ExpressionVisitor, ItemsListVisitor {

	protected StringBuffer buffer;
	protected SelectVisitor selectVisitor;
	protected boolean useBracketsInExprList = true;

	JSONObject json;
	FormDAO fdao;
	
	/**
	 * 主表单
	 */
	FormDb parentFormDb;
	
	/**
	 * 主表中的值与来源结果集中条件的键值
	 */
	Map mapCondValue;

	/**
	 * 嵌套表宏控件中配置的主表与来源结果集中条件字段的映射关系，来源结果集中的条件为sourceField
	 */
	JSONArray mapsCond;
	
	Map mapCondChangeValue;
	Map mapCondType;

	/**
	 * 脚本查询的选项卡关联
	 * @param fdao
	 * @param json
	 */
	public ExpressionDeParser(FormDAO fdao, JSONObject json) {
		this.fdao = fdao;
		this.json = json;
	}
	
	/**
	 * 嵌套表2通过脚本查询拉单
	 * @param parentFormDb
	 * @param mapsCond
	 * @param mapCondValue
	 */
	public ExpressionDeParser(FormDb parentFormDb, JSONArray mapsCond, HashMap mapCondValue) {
		this.parentFormDb = parentFormDb;
		this.mapCondValue = mapCondValue;
		this.mapsCond = mapsCond;
	}

	/**
	 * 更改条件字段的值，用于查询列表中的搜索按钮
	 * @param mapCondChangeValue 条件字段的值的映射
	 * @param mapCondType 条件字段的类型映射
	 */
	public ExpressionDeParser(Map mapCondChangeValue, Map mapCondType) {
		this.mapCondChangeValue = mapCondChangeValue;
		this.mapCondType = mapCondType;
	}

	/**
	 * @param selectVisitor
	 *            a SelectVisitor to de-parse SubSelects. It has to share the
	 *            same<br>
	 *            StringBuffer as this object in order to work, as:
	 * 
	 *            <pre>
	 * <code>
	 * StringBuffer myBuf = new StringBuffer();
	 * MySelectDeparser selectDeparser = new  MySelectDeparser();
	 * selectDeparser.setBuffer(myBuf);
	 * ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeparser, myBuf);
	 * </code>
	 * </pre>
	 * @param buffer
	 *            the buffer that will be filled with the expression
	 */
	public ExpressionDeParser(SelectVisitor selectVisitor, StringBuffer buffer) {
		this.selectVisitor = selectVisitor;
		this.buffer = buffer;
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void visit(BitwiseRightShift bitwiseRightShift) {}

	@Override
	public void visit(BitwiseLeftShift bitwiseLeftShift) {

	}

	@Override
	public void visit(Addition addition) {
		visitBinaryExpression(addition, " + ");
	}

	@Override
	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression, " AND ");
	}

	@Override
	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		if (between.isNot()) {
			buffer.append(" NOT");
		}

		buffer.append(" BETWEEN ");
		between.getBetweenExpressionStart().accept(this);
		buffer.append(" AND ");
		between.getBetweenExpressionEnd().accept(this);
	}

	@Override
	public void visit(Division division) {
		visitBinaryExpression(division, " / ");

	}

	@Override
	public void visit(DoubleValue doubleValue) {
		buffer.append(doubleValue.getValue());

	}

	@Override
	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo, " = ");
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan, " > ");
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals, " >= ");
	}

	@Override
	public void visit(InExpression inExpression) {
		inExpression.getLeftExpression().accept(this);
		if (inExpression.isNot()) {
			buffer.append(" NOT");
		}
		buffer.append(" IN ");

		inExpression.getRightItemsList().accept(this);
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		isNullExpression.getLeftExpression().accept(this);
		if (isNullExpression.isNot()) {
			buffer.append(" IS NOT NULL");
		} else {
			buffer.append(" IS NULL");
		}
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		buffer.append("?");
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {

	}

	@Override
	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression, " LIKE ");

	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		if (existsExpression.isNot()) {
			buffer.append(" NOT EXISTS ");
		} else {
			buffer.append(" EXISTS ");
		}
		existsExpression.getRightExpression().accept(this);
	}

	@Override
	public void visit(LongValue longValue) {
		buffer.append(longValue.getStringValue());
	}

	@Override
	public void visit(HexValue hexValue) {

	}

	@Override
	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan, " < ");
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals, " <= ");
	}

	@Override
	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication, " * ");
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo, " <> ");
	}

	@Override
	public void visit(NullValue nullValue) {
		buffer.append("NULL");
	}

	@Override
	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression, " OR ");
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		if (parenthesis.isNot()) {
			buffer.append(" NOT ");
		}

		buffer.append("(");
		parenthesis.getExpression().accept(this);
		buffer.append(")");
	}

	@Override
	public void visit(StringValue stringValue) {
		buffer.append("'" + stringValue.getValue() + "'");
	}

	@Override
	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction, "-");
	}

	private void visitBinaryExpression(BinaryExpression binaryExpression,
			String operator) {
		if (binaryExpression.isNot()) {
			buffer.append(" NOT ");
		}

		String item = binaryExpression.getLeftExpression().toString();
		item = item.replaceAll("#", "");
		
		// 替换条件中的字段值，用于查询列表中的搜索
		if (mapCondChangeValue!=null) {
			Iterator ir = mapCondChangeValue.keySet().iterator();
			while (ir.hasNext()) {
				String condFieldName = (String) ir.next();
				String val = (String)mapCondChangeValue.get(condFieldName);
				if (condFieldName.equalsIgnoreCase(item)) {
					if (condFieldName.equals(FormSQLBuilder.PRIMARY_KEY_ID)) {
						binaryExpression.setRightExpression(new LongValue(val));
					} else {
						// 取条件字段数据类型
						int p = condFieldName.indexOf(".");
						if (p!=-1) {
							condFieldName = condFieldName.substring(p+1);
						}
				        Integer iType = (Integer)mapCondType.get(condFieldName.toUpperCase());
				        int fieldType = FormField.FIELD_TYPE_VARCHAR;
				        fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());

						setRightExpression(binaryExpression, operator, fieldType, val);
					}
				}
			}
		}
		
		// 替换脚本查询选项卡关联中设置的条件
		if (json!=null) {
			Iterator irJson = json.keys();
			while (irJson.hasNext()) {
				// 模块中的字段
				String formField = (String) irJson.next();
				try {
					// 查询中的条件字段
					String queryField = json.getString(formField);
					if (queryField.equalsIgnoreCase(item)) {
						// 从fields中找对相应的值
						String val = "";
						if (formField.equals(FormSQLBuilder.PRIMARY_KEY_ID)) {
							val = "" + fdao.getId();
							binaryExpression.setRightExpression(new LongValue(val));						
						}
						else {
							val = fdao.getFieldValue(formField);
							FormField ff = fdao.getFormField(formField);
							int fieldType = ff.getFieldType();
							setRightExpression(binaryExpression, operator, fieldType, val);					
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
		}
				
		// 替换脚本查询拉单选择中的条件
		if (mapCondValue!=null) {
			Iterator ir = mapCondValue.keySet().iterator();
			while (ir.hasNext()) {
				String condFieldName = (String) ir.next();

				String parentWinFieldValue = (String) mapCondValue
						.get(condFieldName);
				if (condFieldName.equalsIgnoreCase(item)) {
					FormField ff = null;
					String destField = "";
					for (int i = 0; i < mapsCond.length(); i++) {
						JSONObject j = null;
						try {
							j = mapsCond.getJSONObject(i);
							// 主表中的字段
							destField = (String) j.get("destField");
							// 查询条件对应的字段
							String sourceField = (String) j.get("sourceField");

							if (sourceField.equals(condFieldName)) {
								ff = parentFormDb.getFormField(destField);
								break;
							}

						} catch (JSONException ex) {
							ex.printStackTrace();
						}
					}

					if (ff == null) {
						throw new IllegalArgumentException("字段：" + destField
								+ "在表单" + parentFormDb.getName() + "中不存在");
					}

					String val = parentWinFieldValue;

					if (condFieldName.equals(FormSQLBuilder.PRIMARY_KEY_ID)) {
						binaryExpression.setRightExpression(new LongValue(val));
					} else {
						int fieldType = ff.getFieldType();
						setRightExpression(binaryExpression, operator, fieldType, val);
					}
				}
			}			
		}

		binaryExpression.getLeftExpression().accept(this);
		buffer.append(operator);
		binaryExpression.getRightExpression().accept(this);

		// System.out.println(binaryExpression.getLeftExpression() +
		// ", operator = "+operator+",  "+
		// binaryExpression.getRightExpression());
	}
	
	public void setRightExpression(BinaryExpression binaryExpression, String operator, int fieldType, String val) {
		if (operator.trim().equalsIgnoreCase("LIKE")) {
			val = "%" + val + "%";
		}
		
		if (fieldType == FormField.FIELD_TYPE_INT) {
			if (!val.equals("")) {
				try {
					Long.parseLong(val);
					binaryExpression.setRightExpression(new LongValue(val));
				}
				catch(Exception e) {
					// 有时条件可能为id=T.cws_id，则值为T.cws_id
					// binaryExpression.setRightExpression(new StringValue(val));
					// throw new IllegalArgumentException("传入的条件参数为：" + val + "，该参数应该为整数！");
				}
				
			}
		} else if (fieldType == FormField.FIELD_TYPE_LONG) {
			if (!val.equals("")) {		
				try {
					Long.parseLong(val);
				}
				catch(Exception e) {
					throw new IllegalArgumentException("传入的条件参数为：" + val + "，该参数应该为整数！");
				}
								
				binaryExpression.setRightExpression(new LongValue(
						val));
			}
		} else if (fieldType == FormField.FIELD_TYPE_FLOAT) {
			if (!val.equals("")) {		
				try {
					Double.parseDouble(val);
				}
				catch(Exception e) {
					throw new IllegalArgumentException("传入的条件参数为：" + val + "，该参数应该为双精度型！");
				}
								
				binaryExpression
						.setRightExpression(new DoubleValue(val));
			}
		} else if (fieldType == FormField.FIELD_TYPE_DOUBLE) {
			try {
				Double.parseDouble(val);
			}
			catch(Exception e) {
				throw new IllegalArgumentException("传入的条件参数为：" + val + "，该参数应该为双精度型！");
			}			
			if (!val.equals("")) {					
				binaryExpression
						.setRightExpression(new DoubleValue(val));
			}
		} else if (fieldType == FormField.FIELD_TYPE_PRICE) {
			try {
				Double.parseDouble(val);
			}
			catch(Exception e) {
				throw new IllegalArgumentException("传入的条件参数为：" + val + "，该参数应该为价格型！");
			}			
			if (!val.equals("")) {					
				binaryExpression
						.setRightExpression(new DoubleValue(val));
			}
		} else if (fieldType == FormField.FIELD_TYPE_DATE) {
			// 此处存在bug，日期型的替换后得到的是qjkssj >= net.sf.jsqlparser.expression.DateValue@1e4ef12
			// 也可能binaryExpression不能直接toString();
			// binaryExpression.setRightExpression(new DateValue(
			// 		StrUtil.sqlstr(val)));
			
			binaryExpression.setRightExpression(new StringValue(
					 		SQLFilter.getDateStr(val, "yyyy-MM-dd")));
		} else if (fieldType == FormField.FIELD_TYPE_DATETIME) {
			/*
			binaryExpression
					.setRightExpression(new TimestampValue(
							StrUtil.sqlstr(val)));
			*/
			binaryExpression.setRightExpression(new StringValue(
			 		SQLFilter.getDateStr(val, "yyyy-MM-dd HH:mm:ss")));			
		} else {
			binaryExpression
					.setRightExpression(new StringValue(StrUtil
							.sqlstr(val)));
		}
	}

	@Override
	public void visit(SubSelect subSelect) {
		buffer.append("(");
		subSelect.getSelectBody().accept(selectVisitor);
		buffer.append(")");
	}

	@Override
	public void visit(Column tableColumn) {
		String tableName = tableColumn.getTable().getFullyQualifiedName();
		if (tableName != null) {
			buffer.append(tableName + ".");
		}

		// 更改列名
		// if (tableColumn.getColumnName().equals("a")) {
		// tableColumn.setColumnName("b");
		// }

		buffer.append(tableColumn.getColumnName());
	}

	@Override
	public void visit(Function function) {
		if (function.isEscaped()) {
			buffer.append("{fn ");
		}

		buffer.append(function.getName());
		if (function.isAllColumns()) {
			buffer.append("(*)");
		} else if (function.getParameters() == null) {
			buffer.append("()");
		} else {
			boolean oldUseBracketsInExprList = useBracketsInExprList;
			if (function.isDistinct()) {
				useBracketsInExprList = false;
				buffer.append("(DISTINCT ");
			}
			visit(function.getParameters());
			useBracketsInExprList = oldUseBracketsInExprList;
			if (function.isDistinct()) {
				buffer.append(")");
			}
		}

		if (function.isEscaped()) {
			buffer.append("}");
		}
	}

	@Override
	public void visit(SignedExpression signedExpression) {

	}

	@Override
	public void visit(ExpressionList expressionList) {
		if (useBracketsInExprList) {
			buffer.append("(");
		}
		for (Iterator iter = expressionList.getExpressions().iterator(); iter
				.hasNext();) {
			Expression expression = (Expression) iter.next();
			expression.accept(this);
			if (iter.hasNext()) {
				buffer.append(", ");
			}
		}
		if (useBracketsInExprList) {
			buffer.append(")");
		}
	}

	@Override
	public void visit(MultiExpressionList multiExpressionList) {

	}

	public SelectVisitor getSelectVisitor() {
		return selectVisitor;
	}

	public void setSelectVisitor(SelectVisitor visitor) {
		selectVisitor = visitor;
	}

	@Override
	public void visit(DateValue dateValue) {
		buffer.append("{d '" + dateValue.getValue().toString() + "'}");
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		buffer.append("{ts '" + timestampValue.getValue().toString() + "'}");
	}

	@Override
	public void visit(TimeValue timeValue) {
		buffer.append("{t '" + timeValue.getValue().toString() + "'}");
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		buffer.append("CASE ");
		Expression switchExp = caseExpression.getSwitchExpression();
		if (switchExp != null) {
			switchExp.accept(this);
		}

		List clauses = caseExpression.getWhenClauses();
		for (Iterator iter = clauses.iterator(); iter.hasNext();) {
			Expression exp = (Expression) iter.next();
			exp.accept(this);
		}

		Expression elseExp = caseExpression.getElseExpression();
		if (elseExp != null) {
			// zzf 修复bug(漏掉了else关键字)
			buffer.append(" ELSE ");
			elseExp.accept(this);
		}

		buffer.append(" END");
	}

	@Override
	public void visit(WhenClause whenClause) {
		buffer.append(" WHEN ");
		whenClause.getWhenExpression().accept(this);
		buffer.append(" THEN ");
		whenClause.getThenExpression().accept(this);
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		buffer.append(" ALL ");
		allComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		buffer.append(" ANY ");
		anyComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
	}

	@Override
	public void visit(Concat concat) {
		visitBinaryExpression(concat, " || ");
	}

	@Override
	public void visit(Matches matches) {
		visitBinaryExpression(matches, " @@ ");
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd, " & ");
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr, " | ");
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor, " ^ ");
	}

	@Override
	public void visit(CastExpression castExpression) {

	}

	@Override
	public void visit(Modulo modulo) {

	}

	@Override
	public void visit(AnalyticExpression analyticExpression) {

	}

	@Override
	public void visit(ExtractExpression extractExpression) {

	}

	@Override
	public void visit(IntervalExpression intervalExpression) {

	}

	@Override
	public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {

	}

	@Override
	public void visit(RegExpMatchOperator regExpMatchOperator) {

	}

	@Override
	public void visit(JsonExpression jsonExpression) {

	}

	@Override
	public void visit(JsonOperator jsonOperator) {

	}

	@Override
	public void visit(RegExpMySQLOperator regExpMySQLOperator) {

	}

	@Override
	public void visit(UserVariable userVariable) {

	}

	@Override
	public void visit(NumericBind numericBind) {

	}

	@Override
	public void visit(KeepExpression keepExpression) {

	}

	@Override
	public void visit(MySQLGroupConcat mySQLGroupConcat) {

	}

	@Override
	public void visit(ValueListExpression valueListExpression) {

	}

	@Override
	public void visit(RowConstructor rowConstructor) {

	}

	@Override
	public void visit(OracleHint oracleHint) {

	}

	@Override
	public void visit(TimeKeyExpression timeKeyExpression) {

	}

	@Override
	public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

	}

	@Override
	public void visit(NotExpression notExpression) {

	}

}