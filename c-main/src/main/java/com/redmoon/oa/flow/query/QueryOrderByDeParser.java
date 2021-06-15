package com.redmoon.oa.flow.query;

import java.util.Iterator;
import java.util.List;

import com.cloudwebsoft.framework.util.LogUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class QueryOrderByDeParser extends SelectDeParser {
	
	String orderByCol;
	boolean isAsc;
	
	public QueryOrderByDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
		this.setBuffer(buffer);
		this.setExpressionVisitor(expressionVisitor);
	}
	
	public void setOrderBy(String orderByCol, boolean isAsc) {
		this.orderByCol = orderByCol;
		this.isAsc = isAsc;
	}
	
	public void deparseOrderBy(List orderByElements) {
		getBuffer().append(" ORDER BY ");
		for (Iterator iter = orderByElements.iterator(); iter.hasNext();) {
			OrderByElement orderByElement = (OrderByElement) iter.next();
			orderByElement.getExpression().accept(getExpressionVisitor());
			// 只解析SQL语句中第一个order by 
			if (true) {
				break;
			}
			
			if (iter.hasNext()) {
				getBuffer().append(", ");
			}
		}
	}
	
	public void visit(OrderByElement orderBy) {
		// System.out.println("visit order by:" + orderBy.getExpression().toString());
		
		// if (orderBy.getExpression().toString().equals("id")) {
			// 替换用以排序的列
			Column c = new Column();
			c.setColumnName(orderByCol);
			orderBy.setExpression(c);
			orderBy.setAsc(isAsc);
		// }		
		
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

		if (plainSelect.getWhere() != null) {
			getBuffer().append(" WHERE ");
			plainSelect.getWhere().accept(getExpressionVisitor());
			
			// buffer.append(" and 1=1 ");
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
		else {
			// 如果原SQL语句中没有排序，则加上
			getBuffer().append(" ORDER BY " + orderByCol + " " + (isAsc?"ASC":"DESC"));
		}

		if (plainSelect.getLimit() != null) {
			// deparseLimit(plainSelect.getLimit());
		}
		
		// LogUtil.getLog(getClass()).info("visit:" + buffer.toString());

	}	
}