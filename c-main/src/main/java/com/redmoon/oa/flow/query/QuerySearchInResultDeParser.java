package com.redmoon.oa.flow.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormQueryDb;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

/**
 * 用于拉单时，在结果中进一步搜索
 * @author lenovo
 *
 */
public class QuerySearchInResultDeParser extends SelectDeParser {
	HttpServletRequest request;
	FormQueryDb fqd;
	
	public QuerySearchInResultDeParser(HttpServletRequest request, FormQueryDb fqd, ExpressionVisitor expressionVisitor, StringBuilder buffer) {
		this.request = request;
		this.setBuffer(buffer);
		this.setExpressionVisitor(expressionVisitor);
		this.fqd = fqd;
	}
	
	public void deparseOrderBy(List orderByElements) {
		getBuffer().append(" ORDER BY ");
		for (Iterator iter = orderByElements.iterator(); iter.hasNext();) {
			OrderByElement orderByElement = (OrderByElement) iter.next();
			orderByElement.getExpression().accept(getExpressionVisitor());
			
			if (iter.hasNext()) {
				getBuffer().append(", ");
			}
		}
	}
	
	public void visit(OrderByElement orderBy) {		
		orderBy.getExpression().accept(getExpressionVisitor());

		if (orderBy.isAsc()) {
			getBuffer().append(" ASC");
		} else {
			getBuffer().append(" DESC");
		}
	}
	
	@Override
	public void visit(PlainSelect plainSelect) {
		getBuffer().append("SELECT ");
		Top top = plainSelect.getTop();
		if (top != null) {
			top.toString();
		}
		if (plainSelect.getDistinct() != null) {
			getBuffer().append("DISTINCT ");
			if (plainSelect.getDistinct().getOnSelectItems() != null) {
				getBuffer().append("ON (");
				for (Iterator iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter.hasNext();) {
					SelectItem selectItem = (SelectItem) iter.next();
					selectItem.accept(this);
					if (iter.hasNext()) {
						getBuffer().append(", ");
					}
				}
				getBuffer().append(") ");
			}
		}

		for (Iterator iter = plainSelect.getSelectItems().iterator(); iter.hasNext();) {
			SelectItem selectItem = (SelectItem) iter.next();
			selectItem.accept(this);
			if (iter.hasNext()) {
				getBuffer().append(", ");
			}
		}

		getBuffer().append(" ");
		
		if (plainSelect.getFromItem() != null) {
			getBuffer().append("FROM ");
			plainSelect.getFromItem().accept(this);
		}

		if (plainSelect.getJoins() != null) {
			for (Iterator iter = plainSelect.getJoins().iterator(); iter.hasNext();) {
				Join join = (Join) iter.next();
				deparseJoin(join);		
			}
		}

		// 接收传入的参数
		String conds = "";
		QueryScriptUtil qsu = new QueryScriptUtil();
		Map map = qsu.getCols(request, fqd);
		Iterator irMap = map.keySet().iterator();

		Map mapFieldType = qsu.getMapFieldType();
		Map mapFieldTitle = qsu.getMapFieldTitle();	
		
		while (irMap.hasNext()) {
			String keyName = (String)irMap.next();
			keyName = keyName.toUpperCase();
			
			Integer iType = (Integer)mapFieldType.get(keyName);				
			int fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());
			
			String value = ParamUtil.get(request, keyName);
			String name_cond = ParamUtil.get(request, keyName + "_cond");
			if (fieldType==FormField.FIELD_TYPE_DATE || fieldType==FormField.FIELD_TYPE_DATETIME) {
				String fDate = ParamUtil.get(request, keyName + "FromDate");
				String tDate = ParamUtil.get(request, keyName + "ToDate");
				if (!fDate.equals("")) {
					conds += " and " + keyName + ">=" + StrUtil.sqlstr(fDate);
				}
				if (!tDate.equals("")) {
					conds += " and " + keyName + "<=" + StrUtil.sqlstr(tDate);
				}
			}
			else {
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						conds += " and " + keyName + " like " + StrUtil.sqlstr("%" + value + "%");
					}
				}
				else if (name_cond.equals("1")) {
					if (!value.equals("")) {
						conds += " and " + keyName + "=" + StrUtil.sqlstr(value);
					}
				}
				else {
					if (!value.equals("")) {
						conds += " and " + keyName + name_cond + value;
					}				
				}
			}
		}				
		if (plainSelect.getWhere() != null) {
			getBuffer().append(" WHERE ");
			plainSelect.getWhere().accept(getExpressionVisitor());
			// buffer.append(" and 1=1 ");		

			getBuffer().append(conds);
		}
		else {
			getBuffer().append(" WHERE 1=1 ");
			getBuffer().append(conds);
		}

		if (plainSelect.getGroupByColumnReferences() != null) {
			getBuffer().append(" GROUP BY ");
			for (Iterator iter = plainSelect.getGroupByColumnReferences().iterator(); iter.hasNext();) {
				Expression columnReference = (Expression) iter.next();
				columnReference.accept(getExpressionVisitor());
				if (iter.hasNext()) {
					getBuffer().append(", ");
				}
			}
		}

		if (plainSelect.getHaving() != null) {
			getBuffer().append(" HAVING ");
			plainSelect.getHaving().accept(getExpressionVisitor());
		}

		if (plainSelect.getOrderByElements() != null) {
			deparseOrderBy(plainSelect.getOrderByElements());
		}

		if (plainSelect.getLimit() != null) {
			// deparseLimit(plainSelect.getLimit());
		}
		
		LogUtil.getLog(getClass()).info("visit:" + getBuffer().toString());

	}	
}