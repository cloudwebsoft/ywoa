package com.redmoon.oa.sql;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.io.StringReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
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
/*     */ import net.sf.jsqlparser.statement.delete.Delete;
/*     */ import net.sf.jsqlparser.statement.insert.Insert;
/*     */ import net.sf.jsqlparser.statement.select.FromItem;
/*     */ import net.sf.jsqlparser.statement.select.PlainSelect;
/*     */ import net.sf.jsqlparser.statement.select.Select;
/*     */ import net.sf.jsqlparser.statement.select.SelectExpressionItem;
/*     */ import net.sf.jsqlparser.statement.update.Update;
/*     */ 
/*     */ public class SqlRoot
/*     */ {
/*     */   private boolean select;
/*     */   private boolean update;
/*     */   private boolean delete;
/*     */   private boolean insert;
/*     */   private Select selectObj;
/*     */   private Update updateObj;
/*     */   private Delete deleteObj;
/*     */   private Insert insertObj;
/*  48 */   private List<FromItem> selectCols = new ArrayList(10);
/*     */ 
/*  50 */   private SqlNode whereNode = new SqlNode();
/*     */   private String tableName;
/*  54 */   private CCJSqlParserManager parserManager = new CCJSqlParserManager();
/*     */ 
/*     */   public SqlRoot(String sql) throws JSQLParserException
/*     */   {
/*  58 */     Statement statement = this.parserManager.parse(new StringReader(sql));
/*     */ 
/*  60 */     Class cls = statement.getClass();
/*     */ 
/*  62 */     if (cls == Update.class) {
/*  63 */       this.update = true;
/*     */ 
/*  65 */       this.updateObj = ((Update)statement);
/*     */     }
/*  67 */     else if (cls == Select.class)
/*     */     {
/*  69 */       this.select = true;
/*  70 */       this.selectObj = ((Select)statement);
/*     */ 
/*  72 */       parseSelect((PlainSelect)this.selectObj.getSelectBody());
/*     */     }
/*  74 */     else if (cls == Delete.class) {
/*  75 */       this.delete = true;
/*  76 */       this.deleteObj = ((Delete)statement);
/*  77 */     } else if (cls == Insert.class) {
/*  78 */       this.insert = true;
/*  79 */       this.insertObj = ((Insert)statement);
/*     */     }
/*     */     else {
/*  82 */       throw new UnsupportedOperationException("unsupport sql:" + sql);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void parseSelect(PlainSelect plainSelect)
/*     */   {
/*  89 */     List list = plainSelect.getSelectItems();
/*  90 */     int len = list.size();
/*  91 */     for (int i = 0; i < list.size(); i++) {
/*  92 */       Object obj = list.get(i);
/*     */       SelectExpressionItem item;
/*  93 */       if (obj.getClass() == SelectExpressionItem.class)
/*     */       {
/*  95 */         item = (SelectExpressionItem)obj;
/*     */       }
/*     */       else {
/*  98 */         obj.getClass();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 106 */     this.tableName = plainSelect.getFromItem().toString();
/*     */ 
/* 109 */     parseWhere(plainSelect.getWhere(), this.whereNode);
/*     */   }
/*     */ 
/*     */   public String dumpSelect()
/*     */   {
/* 115 */     StringBuffer sb = new StringBuffer();
/* 116 */     sb.append("table name is:" + this.tableName);
/* 117 */     sb.append("\r\n");
/* 118 */     sb.append("from item is:" + this.selectCols);
/* 119 */     sb.append("\r\n");
/* 120 */     sb.append("where is:");
/* 121 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   private void checkNull(SqlNode node, boolean left)
/*     */   {
/* 127 */     if (left)
/*     */     {
/* 129 */       if (node.getLeftchild() == null) {
/* 130 */         node.setLeftchild(new SqlNode());
/*     */       }
/*     */     }
/* 133 */     else if (node.getRightchild() == null)
/* 134 */       node.setRightchild(new SqlNode());
/*     */   }
/*     */ 
/*     */   private void parseWhere(Expression expression, SqlNode node)
/*     */   {
/* 139 */     if (expression == null)
/* 140 */       return;
/* 141 */     Class cls = expression.getClass();
/* 142 */     if (cls == AndExpression.class) {
/* 143 */       AndExpression and = (AndExpression)expression;
/* 144 */       node.setCondition("and");
/* 145 */       node.setConditionInt(0);
/* 146 */       checkNull(node, true);
/* 147 */       parseWhere(and.getLeftExpression(), node.getLeftchild());
/*     */ 
/* 149 */       checkNull(node, false);
/* 150 */       parseWhere(and.getRightExpression(), node.getRightchild());
/*     */ 
/* 152 */       return;
/* 153 */     }if (OrExpression.class == cls) {
/* 154 */       OrExpression or = (OrExpression)expression;
/*     */ 
/* 157 */       node.setCondition("or");
/* 158 */       node.setConditionInt(1);
/* 159 */       checkNull(node, true);
/* 160 */       parseWhere(or.getLeftExpression(), node.getLeftchild());
/*     */ 
/* 162 */       checkNull(node, false);
/* 163 */       parseWhere(or.getRightExpression(), node.getRightchild());
/*     */ 
/* 165 */       return;
/* 166 */     }if (Parenthesis.class == cls)
/*     */     {
/* 168 */       Parenthesis pt = (Parenthesis)expression;
/*     */ 
/* 170 */       parseWhere(pt.getExpression(), node);
/*     */ 
/* 172 */       return;
/*     */     }
/*     */ 
/* 176 */     if (!processAtom(expression, node))
/*     */     {
/* 178 */       node.setLeftchild(null);
/*     */     }
/*     */   }
/*     */ 
/*     */   private boolean processAtom(Expression expression, SqlNode node)
/*     */   {
/* 189 */     if (expression == null)
/*     */     {
/* 191 */       node = null;
/* 192 */       return false;
/*     */     }
/*     */ 
/* 195 */     Class cls = expression.getClass();
/*     */ 
/* 197 */     if (EqualsTo.class == cls)
/*     */     {
/* 199 */       EqualsTo et = (EqualsTo)expression;
/* 200 */       node.setCondition("=");
/* 201 */       node.setConditionInt(4);
/* 202 */       node.setLeft(et.getLeftExpression().toString());
/* 203 */       node.setRight(et.getRightExpression().toString());
/*     */     }
/* 205 */     else if (GreaterThan.class == cls) {
/* 206 */       GreaterThan gt = (GreaterThan)expression;
/* 207 */       node.setCondition(">");
/* 208 */       node.setConditionInt(5);
/* 209 */       node.setLeft(gt.getLeftExpression().toString());
/* 210 */       node.setRight(gt.getRightExpression().toString());
/* 211 */     } else if (GreaterThanEquals.class == cls) {
/* 212 */       GreaterThanEquals gte = (GreaterThanEquals)expression;
/* 213 */       node.setCondition(">=");
/* 214 */       node.setConditionInt(6);
/* 215 */       node.setLeft(gte.getLeftExpression().toString());
/* 216 */       node.setRight(gte.getRightExpression().toString());
/* 217 */     } else if (MinorThan.class == cls) {
/* 218 */       MinorThan mt = (MinorThan)expression;
/* 219 */       node.setCondition("<");
/* 220 */       node.setConditionInt(7);
/* 221 */       node.setLeft(mt.getLeftExpression().toString());
/* 222 */       node.setRight(mt.getRightExpression().toString());
/* 223 */     } else if (MinorThanEquals.class == cls) {
/* 224 */       MinorThanEquals mte = (MinorThanEquals)expression;
/* 225 */       node.setCondition("<=");
/* 226 */       node.setConditionInt(8);
/* 227 */       node.setLeft(mte.getLeftExpression().toString());
/* 228 */       node.setRight(mte.getRightExpression().toString());
/* 229 */     } else if (Between.class == cls)
/*     */     {
/* 231 */       Between bet = (Between)expression;
/* 232 */       node.setCondition("between");
/* 233 */       node.setConditionInt(2);
/* 234 */       node.setLeft(bet.getLeftExpression().toString());
/* 235 */       node.setBetweenStart(bet.getBetweenExpressionStart().toString());
/* 236 */       node.setBetweenEnd(bet.getBetweenExpressionEnd().toString());
/* 237 */     } else if (InExpression.class == cls)
/*     */     {
/* 239 */       InExpression in = (InExpression)expression;
/* 240 */       node.setLeft(in.getLeftExpression().toString());
/* 241 */       node.setConditionInt(3);
/* 242 */       node.setCondition("in");
/* 243 */       System.out.println(in.getRightItemsList());
/* 244 */       String[] arr = in.getRightItemsList().toString().split(",");
/* 245 */       node.setInList(arr);
/*     */     }
/*     */ 
/* 248 */     return true;
/*     */   }
/*     */ 
/*     */   @Override
public String toString()
/*     */   {
/* 253 */     return this.updateObj.toString();
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */     throws JSQLParserException
/*     */   {
/* 262 */     SqlRoot root = new SqlRoot("select *   from  aa  where  a>=1 and b<100 or c>=100 and d=100");
/*     */   }
/*     */ 
/*     */   public SqlNode getRoot()
/*     */   {
/* 269 */     return this.whereNode;
/*     */   }
/*     */ }

/* Location:           E:\jsqlparser.jar
 * Qualified Name:     test.SqlRoot
 * JD-Core Version:    0.6.0
 */