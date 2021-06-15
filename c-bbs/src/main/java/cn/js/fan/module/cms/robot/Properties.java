/*    */ package cn.js.fan.module.cms.robot;
/*    */ 
/*    */ import com.cloudwebsoft.framework.util.LogUtil;
/*    */ import java.io.ByteArrayInputStream;
/*    */ import java.io.IOException;
/*    */ 
/*    */ public class Properties extends java.util.Properties
/*    */ {
/* 21 */   private String charset = "utf-8";
/*    */ 
/*    */   public synchronized void load(String props)
/*    */     throws IOException
/*    */   {
/* 28 */     StringBuffer StringBuffer1 = new StringBuffer(props);
/* 29 */     ByteArrayInputStream bai = new ByteArrayInputStream(StringBuffer1.toString().getBytes(this.charset));
/* 30 */     load(bai);
/* 31 */     bai.close();
/*    */   }
/*    */ 
/*    */   public String getProperty(String key) {
/* 35 */     String str = "";
/*    */     try {
/* 37 */       str = new String(super.getProperty(key).getBytes("ISO8859_1"), this.charset);
/*    */     }
/*    */     catch (Exception e) {
/* 40 */       LogUtil.getLog(getClass()).error("getProperty:" + e.getMessage());
/*    */     }
/* 42 */     return str;
/*    */   }
/*    */ }