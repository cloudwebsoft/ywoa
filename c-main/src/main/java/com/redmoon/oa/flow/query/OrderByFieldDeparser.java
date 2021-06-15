package com.redmoon.oa.flow.query;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import java.util.Iterator;
import java.util.List;

public class OrderByFieldDeparser extends SelectDeParser {
    StringBuffer buffer;

    public OrderByFieldDeparser(ExpressionVisitor expressionVisitor, StringBuffer buffer) {
        this.buffer = buffer;
    }

    public void deparseOrderBy(List orderByElements) {
        for (Iterator iter = orderByElements.iterator(); iter.hasNext();) {
            OrderByElement orderByElement = (OrderByElement) iter.next();
            buffer.append(orderByElement.getExpression().toString());
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
    }

    public void visit(OrderByElement orderBy) {
        return;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getOrderByElements() != null) {
            deparseOrderBy(plainSelect.getOrderByElements());
        }
    }
}
