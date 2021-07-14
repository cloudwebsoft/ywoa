/*    */ package cn.js.fan.module.cms.robot;
/*    */ 
/*    */ import cn.js.fan.db.ResultRecord;
/*    */ import cn.js.fan.util.ErrMsgException;
/*    */ import cn.js.fan.util.ResKeyException;
/*    */ import com.cloudwebsoft.framework.base.QObjectDb;
/*    */ import com.redmoon.forum.SequenceMgr;
/*    */ import java.io.PrintStream;
/*    */ import java.sql.SQLException;
/*    */ 
/*    */ public class RobotDb extends QObjectDb
/*    */   implements Cloneable
/*    */ {
/*    */   public Object clone()
/*    */   {
/* 28 */     Object o = null;
/*    */     try {
/* 30 */       o = super.clone();
/*    */     } catch (CloneNotSupportedException e) {
/* 32 */       e.printStackTrace();
/*    */     }
/* 34 */     return o;
/*    */   }
/*    */ 
/*    */   public boolean copy() throws ResKeyException, ErrMsgException {
/* 38 */     RobotDb rd = (RobotDb)clone();
/* 39 */     rd.set("name", rd.getString("name") + " copy");
/* 40 */     rd.set("id", new Integer((int)SequenceMgr.nextID(26)));
/* 41 */     return rd.create();
/*    */   }
/*    */ 
/*    */   public boolean Import(String robotName, String str) throws ResKeyException, ErrMsgException {
/* 45 */     Properties p = new Properties();
/*    */     try {
/* 47 */       p.load(str);
/*    */     }
/*    */     catch (Exception e) {
/* 50 */       System.out.println("Import: " + e.getMessage());
/*    */     }
/*    */ 
/* 55 */     String[] ary = getFieldsFromQueryCreate();
/* 56 */     int len = ary.length;
/* 57 */     RobotDb rd = new RobotDb();
/*    */     try {
/* 59 */       rd.initQObject(new Object[] { new Integer(0) });
/*    */     }
/*    */     catch (SQLException e) {
/* 62 */       throw new ResKeyException("err_db");
/*    */     }
/* 64 */     for (int i = 0; i < len; i++) {
/* 65 */       rd.resultRecord.put(ary[i], RobotUtil.decode(p.getProperty(ary[i])));
/*    */     }
/*    */ 
/* 68 */     rd.resultRecord.put("name", robotName);
/* 69 */     rd.resultRecord.put("id", new Integer((int)SequenceMgr.nextID(26)));
/* 70 */     return rd.create();
/*    */   }
/*    */ 
/*    */   public String Export() {
/* 74 */     StringBuffer sb = new StringBuffer();
/* 75 */     String[] ary = getFieldsFromQueryCreate();
/* 76 */     int len = ary.length;
/* 77 */     for (int i = 0; i < len; i++) {
/* 78 */       if (!ary[i].equals("id"))
/* 79 */         sb.append(ary[i] + "=" + RobotUtil.encode(this.resultRecord.getString(ary[i])) + "\n");
/*    */     }
/* 81 */     return sb.toString();
/*    */   }
/*    */ }