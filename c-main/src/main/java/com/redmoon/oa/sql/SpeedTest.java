package com.redmoon.oa.sql;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.io.StringReader;
/*     */ import net.sf.jsqlparser.JSQLParserException;
/*     */ import net.sf.jsqlparser.expression.Expression;
/*     */ import net.sf.jsqlparser.expression.Parenthesis;
/*     */ import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
/*     */ import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
/*     */ import net.sf.jsqlparser.expression.operators.relational.Between;
/*     */ import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
/*     */ import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
/*     */ import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
/*     */ import net.sf.jsqlparser.expression.operators.relational.InExpression;
/*     */ import net.sf.jsqlparser.expression.operators.relational.MinorThan;
/*     */ import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
/*     */ import net.sf.jsqlparser.parser.CCJSqlParserManager;
/*     */ import net.sf.jsqlparser.statement.Statement;
/*     */ import net.sf.jsqlparser.statement.select.PlainSelect;
/*     */ import net.sf.jsqlparser.statement.select.Select;
/*     */ import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
/*     */ import net.sf.jsqlparser.util.deparser.SelectDeParser;
/*     */ 
/*     */ public class SpeedTest
/*     */ {
/*     */   private static void checkNull(SqlNode node, boolean left)
/*     */   {
/*  34 */     if (left)
/*     */     {
/*  36 */       if (node.getLeftchild() == null) {
/*  37 */         node.setLeftchild(new SqlNode());
/*     */       }
/*     */     }
/*  40 */     else if (node.getRightchild() == null)
/*  41 */       node.setRightchild(new SqlNode());
/*     */   }
/*     */ 
/*     */   private static void parseWhere(Expression expression, SqlNode node)
/*     */   {
/*  46 */     if (expression == null)
/*  47 */       return;
/*  48 */     Class cls = expression.getClass();
/*  49 */     if (cls == AndExpression.class) {
/*  50 */       AndExpression and = (AndExpression)expression;
/*  51 */       node.setCondition("and");
/*  52 */       node.setConditionInt(0);
/*  53 */       checkNull(node, true);
/*  54 */       parseWhere(and.getLeftExpression(), node.getLeftchild());
/*     */ 
/*  56 */       checkNull(node, false);
/*  57 */       parseWhere(and.getRightExpression(), node.getRightchild());
/*     */ 
/*  59 */       return;
/*  60 */     }if (OrExpression.class == cls) {
/*  61 */       OrExpression or = (OrExpression)expression;
/*     */ 
/*  64 */       node.setCondition("or");
/*  65 */       node.setConditionInt(1);
/*  66 */       checkNull(node, true);
/*  67 */       parseWhere(or.getLeftExpression(), node.getLeftchild());
/*     */ 
/*  69 */       checkNull(node, false);
/*  70 */       parseWhere(or.getRightExpression(), node.getRightchild());
/*     */ 
/*  72 */       return;
/*  73 */     }if (Parenthesis.class == cls)
/*     */     {
/*  75 */       Parenthesis pt = (Parenthesis)expression;
/*  76 */       checkNull(node, true);
/*  77 */       parseWhere(pt.getExpression(), node);
/*     */ 
/*  79 */       return;
/*     */     }
/*     */ 
/*  82 */     checkNull(node, true);
/*  83 */     if (!processAtom(expression, node))
/*     */     {
/*  85 */       node.setLeftchild(null);
/*     */     }
/*     */   }
/*     */ 
/*     */   private static boolean processAtom(Expression expression, SqlNode node)
/*     */   {
/*  97 */     if (expression == null)
/*     */     {
/*  99 */       node = null;
/* 100 */       return false;
/*     */     }
/*     */ 
/* 103 */     Class cls = expression.getClass();
/*     */ 
/* 105 */     if (EqualsTo.class == cls)
/*     */     {
/* 107 */       EqualsTo et = (EqualsTo)expression;
/* 108 */       node.setCondition("=");
/* 109 */       node.setConditionInt(4);
/* 110 */       node.setLeft(et.getLeftExpression().toString());
/* 111 */       node.setRight(et.getRightExpression().toString());
/*     */     }
/* 113 */     else if (GreaterThan.class == cls) {
/* 114 */       GreaterThan gt = (GreaterThan)expression;
/* 115 */       node.setCondition(">");
/* 116 */       node.setConditionInt(5);
/* 117 */       node.setLeft(gt.getLeftExpression().toString());
/* 118 */       node.setRight(gt.getRightExpression().toString());
/* 119 */     } else if (GreaterThanEquals.class == cls) {
/* 120 */       GreaterThanEquals gte = (GreaterThanEquals)expression;
/* 121 */       node.setCondition(">=");
/* 122 */       node.setConditionInt(6);
/* 123 */       node.setLeft(gte.getLeftExpression().toString());
/* 124 */       node.setRight(gte.getRightExpression().toString());
/* 125 */     } else if (MinorThan.class == cls) {
/* 126 */       MinorThan mt = (MinorThan)expression;
/* 127 */       node.setCondition("<");
/* 128 */       node.setConditionInt(7);
/* 129 */       node.setLeft(mt.getLeftExpression().toString());
/* 130 */       node.setRight(mt.getRightExpression().toString());
/* 131 */     } else if (MinorThanEquals.class == cls) {
/* 132 */       MinorThanEquals mte = (MinorThanEquals)expression;
/* 133 */       node.setCondition("<=");
/* 134 */       node.setConditionInt(8);
/* 135 */       node.setLeft(mte.getLeftExpression().toString());
/* 136 */       node.setRight(mte.getRightExpression().toString());
/* 137 */     } else if (Between.class == cls)
/*     */     {
/* 139 */       Between bet = (Between)expression;
/* 140 */       node.setCondition("between");
/* 141 */       node.setConditionInt(2);
/* 142 */       node.setLeft(bet.getLeftExpression().toString());
/* 143 */       node.setBetweenStart(bet.getBetweenExpressionStart().toString());
/* 144 */       node.setBetweenEnd(bet.getBetweenExpressionEnd().toString());
/* 145 */     } else if (InExpression.class == cls)
/*     */     {
/* 147 */       InExpression in = (InExpression)expression;
/* 148 */       node.setLeft(in.getLeftExpression().toString());
/* 149 */       node.setConditionInt(3);
/* 150 */       node.setCondition("in");
/* 151 */       System.out.println(in.getRightItemsList());
/* 152 */       String[] arr = in.getRightItemsList().toString().split(",");
/* 153 */       node.setInList(arr);
/*     */     }
/*     */ 
/* 157 */     System.out.println(node.getLeft());
/*     */ 
/* 159 */     return true;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args) throws JSQLParserException
/*     */   {
/* 164 */     CCJSqlParserManager parserManager = new CCJSqlParserManager();
/*     */ 
/* 166 */     String statement = "SELECT * FROM tab1 WHERE";
/* 167 */     String where = " a='1' order by id desc";
/* 168 */     statement = statement + where;
/* 169 */     Statement parsed = parserManager.parse(new StringReader(statement));
/*     */ 
/* 171 */     PlainSelect plainSelect = (PlainSelect)((Select)parsed).getSelectBody();
/*     */ 
/* 175 */     StringBuilder stringBuffer = new StringBuilder();
/*     */ 
/* 178 */     ExpressionDeParser expressionDeParser = new ExpressionDeParser();
/* 179 */     expressionDeParser.setBuffer(stringBuffer);
/*     */ 
/* 181 */     SelectDeParser deParser = new SelectDeParser(expressionDeParser, stringBuffer);
/*     */ 
/* 186 */     System.out.println("stringBuffer=" + stringBuffer);
/*     */ 
/* 188 */     System.out.println("SelectDeParser deParser.getBuffer()=" + deParser.getBuffer());
/*     */ 
/* 190 */     System.out.println(plainSelect.toString());
/*     */ 
/* 192 */     plainSelect.accept(deParser);
/* 193 */     System.out.println(plainSelect.toString());
/* 194 */     System.out.println("stringBuffer2=" + stringBuffer);
/*     */   }
/*     */ }

/* Location:           E:\jsqlparser.jar
 * Qualified Name:     test.SpeedTest
 * JD-Core Version:    0.6.0
 */