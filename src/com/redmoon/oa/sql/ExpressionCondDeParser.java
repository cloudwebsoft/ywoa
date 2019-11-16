package com.redmoon.oa.sql;

import java.io.PrintStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

public class ExpressionCondDeParser
  implements ExpressionVisitor, ItemsListVisitor
{
  protected StringBuffer buffer;
  protected SelectVisitor selectVisitor;
  protected boolean useBracketsInExprList = true;

  public ExpressionCondDeParser()
  {
  }

  public ExpressionCondDeParser(SelectVisitor selectVisitor, StringBuffer buffer)
  {
    this.selectVisitor = selectVisitor;
    this.buffer = buffer;
  }

  public StringBuffer getBuffer() {
    return this.buffer;
  }

  public void setBuffer(StringBuffer buffer) {
    this.buffer = buffer;
  }

  public void visit(Addition addition) {
    visitBinaryExpression(addition, " + ");
  }

  public void visit(AndExpression andExpression) {
    visitBinaryExpression(andExpression, " AND ");
  }

  public void visit(Between between) {
    between.getLeftExpression().accept(this);
    if (between.isNot()) {
      this.buffer.append(" NOT");
    }
    this.buffer.append(" BETWEEN ");
    between.getBetweenExpressionStart().accept(this);
    this.buffer.append(" AND ");
    between.getBetweenExpressionEnd().accept(this);
  }

  public void visit(Division division)
  {
    visitBinaryExpression(division, " / ");
  }

  public void visit(DoubleValue doubleValue)
  {
    this.buffer.append(doubleValue.getValue());
  }

  public void visit(EqualsTo equalsTo)
  {
    visitBinaryExpression(equalsTo, " = ");
  }

  public void visit(GreaterThan greaterThan) {
    visitBinaryExpression(greaterThan, " > ");
  }

  public void visit(GreaterThanEquals greaterThanEquals) {
    visitBinaryExpression(greaterThanEquals, " >= ");
  }

  public void visit(InExpression inExpression)
  {
    inExpression.getLeftExpression().accept(this);
    if (inExpression.isNot())
      this.buffer.append(" NOT");
    this.buffer.append(" IN ");

    inExpression.getItemsList().accept(this);
  }

  public void visit(InverseExpression inverseExpression) {
    this.buffer.append("-");
    inverseExpression.getExpression().accept(this);
  }

  public void visit(IsNullExpression isNullExpression) {
    isNullExpression.getLeftExpression().accept(this);
    if (isNullExpression.isNot())
      this.buffer.append(" IS NOT NULL");
    else
      this.buffer.append(" IS NULL");
  }

  public void visit(JdbcParameter jdbcParameter)
  {
    this.buffer.append("?");
  }

  public void visit(LikeExpression likeExpression)
  {
    visitBinaryExpression(likeExpression, " LIKE ");
  }

  public void visit(ExistsExpression existsExpression)
  {
    if (existsExpression.isNot())
      this.buffer.append(" NOT EXISTS ");
    else {
      this.buffer.append(" EXISTS ");
    }
    existsExpression.getRightExpression().accept(this);
  }

  public void visit(LongValue longValue) {
    this.buffer.append(longValue.getStringValue());
  }

  public void visit(MinorThan minorThan)
  {
    visitBinaryExpression(minorThan, " < ");
  }

  public void visit(MinorThanEquals minorThanEquals)
  {
    visitBinaryExpression(minorThanEquals, " <= ");
  }

  public void visit(Multiplication multiplication)
  {
    visitBinaryExpression(multiplication, " * ");
  }

  public void visit(NotEqualsTo notEqualsTo)
  {
    visitBinaryExpression(notEqualsTo, " <> ");
  }

  public void visit(NullValue nullValue)
  {
    this.buffer.append("NULL");
  }

  public void visit(OrExpression orExpression)
  {
    visitBinaryExpression(orExpression, " OR ");
  }

  public void visit(Parenthesis parenthesis)
  {
    if (parenthesis.isNot()) {
      this.buffer.append(" NOT ");
    }
    this.buffer.append("(");
    parenthesis.getExpression().accept(this);
    this.buffer.append(")");
  }

  public void visit(StringValue stringValue)
  {
    this.buffer.append("'" + stringValue.getValue() + "'");
  }

  public void visit(Subtraction subtraction)
  {
    visitBinaryExpression(subtraction, "-");
  }

  private void visitBinaryExpression(BinaryExpression binaryExpression, String operator)
  {
    if (binaryExpression.isNot()) {
      this.buffer.append(" NOT ");
    }
    String item = binaryExpression.getLeftExpression().toString();
    if (item.equals("a"))
    {
      binaryExpression.setRightExpression(new JdbcParameter());
    }

    binaryExpression.getLeftExpression().accept(this);
    this.buffer.append(operator);
    binaryExpression.getRightExpression().accept(this);
    System.out.println(binaryExpression.getLeftExpression() + ", operator = " + operator + ",  " + binaryExpression.getRightExpression());
  }

  public void visit(SubSelect subSelect) {
    this.buffer.append("(");
    subSelect.getSelectBody().accept(this.selectVisitor);
    this.buffer.append(")");
  }

  public void visit(Column tableColumn) {
    String tableName = tableColumn.getTable().getWholeTableName();
    if (tableName != null) {
      this.buffer.append(tableName + ".");
    }

    this.buffer.append(tableColumn.getColumnName());
  }

  public void visit(Function function) {
    if (function.isEscaped()) {
      this.buffer.append("{fn ");
    }

    this.buffer.append(function.getName());
    if (function.isAllColumns()) {
      this.buffer.append("(*)");
    } else if (function.getParameters() == null) {
      this.buffer.append("()");
    } else {
      boolean oldUseBracketsInExprList = this.useBracketsInExprList;
      if (function.isDistinct()) {
        this.useBracketsInExprList = false;
        this.buffer.append("(DISTINCT ");
      }
      visit(function.getParameters());
      this.useBracketsInExprList = oldUseBracketsInExprList;
      if (function.isDistinct()) {
        this.buffer.append(")");
      }
    }

    if (function.isEscaped())
      this.buffer.append("}");
  }

  public void visit(ExpressionList expressionList)
  {
    if (this.useBracketsInExprList)
      this.buffer.append("(");
    for (Iterator iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
      Expression expression = (Expression)iter.next();
      expression.accept(this);
      if (iter.hasNext())
        this.buffer.append(", ");
    }
    if (this.useBracketsInExprList)
      this.buffer.append(")");
  }

  public SelectVisitor getSelectVisitor()
  {
    return this.selectVisitor;
  }

  public void setSelectVisitor(SelectVisitor visitor) {
    this.selectVisitor = visitor;
  }

  public void visit(DateValue dateValue) {
    this.buffer.append("{d '" + dateValue.getValue().toString() + "'}");
  }
  public void visit(TimestampValue timestampValue) {
    this.buffer.append("{ts '" + timestampValue.getValue().toString() + "'}");
  }
  public void visit(TimeValue timeValue) {
    this.buffer.append("{t '" + timeValue.getValue().toString() + "'}");
  }

  public void visit(CaseExpression caseExpression) {
    this.buffer.append("CASE ");
    Expression switchExp = caseExpression.getSwitchExpression();
    if (switchExp != null) {
      switchExp.accept(this);
    }

    List clauses = caseExpression.getWhenClauses();
    for (Iterator iter = clauses.iterator(); iter.hasNext(); ) {
      Expression exp = (Expression)iter.next();
      exp.accept(this);
    }

    Expression elseExp = caseExpression.getElseExpression();
    if (elseExp != null)
    {
      this.buffer.append(" ELSE ");
      elseExp.accept(this);
    }

    this.buffer.append(" END");
  }

  public void visit(WhenClause whenClause) {
    this.buffer.append(" WHEN ");
    whenClause.getWhenExpression().accept(this);
    this.buffer.append(" THEN ");
    whenClause.getThenExpression().accept(this);
  }

  public void visit(AllComparisonExpression allComparisonExpression) {
    this.buffer.append(" ALL ");
    allComparisonExpression.GetSubSelect().accept((ExpressionVisitor)this);
  }

  public void visit(AnyComparisonExpression anyComparisonExpression) {
    this.buffer.append(" ANY ");
    anyComparisonExpression.GetSubSelect().accept((ExpressionVisitor)this);
  }

  public void visit(Concat concat) {
    visitBinaryExpression(concat, " || ");
  }

  public void visit(Matches matches) {
    visitBinaryExpression(matches, " @@ ");
  }

  public void visit(BitwiseAnd bitwiseAnd) {
    visitBinaryExpression(bitwiseAnd, " & ");
  }

  public void visit(BitwiseOr bitwiseOr) {
    visitBinaryExpression(bitwiseOr, " | ");
  }

  public void visit(BitwiseXor bitwiseXor) {
    visitBinaryExpression(bitwiseXor, " ^ ");
  }
}