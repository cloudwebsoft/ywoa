package com.redmoon.oa.util;

import com.cloudwebsoft.framework.util.LogUtil;

import java.io.*;

/** 
 * 用于处理Runtime.getRuntime().exec产生的错误流及输出流 
 * @author shaojing 
 * 
 */  
public class StreamGobbler extends Thread {  
    InputStream is;  
    String type;  
    OutputStream os;  
          
    StreamGobbler(InputStream is, String type) {  
        this(is, type, null);  
    }  
  
    StreamGobbler(InputStream is, String type, OutputStream redirect) {  
        this.is = is;  
        this.type = type;  
        this.os = redirect;  
    }  
      
    public void run() {  
        InputStreamReader isr = null;  
        BufferedReader br = null;  
        PrintWriter pw = null;  
        try {  
            if (os != null)  
                pw = new PrintWriter(os);  
            isr = new InputStreamReader(is);  
            br = new BufferedReader(isr);  
            String line=null;  
            while ( (line = br.readLine()) != null) {  
                if (pw != null) {
                    pw.println(line);
                }
                LogUtil.getLog(getClass()).info(type + ">" + line);
            }  
            if (pw != null) {
                pw.flush();
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally{  
            try {  
                if(pw!=null) {
                    pw.close();
                }
                if(br!=null) {
                    br.close();
                }
                if(isr!=null) {
                    isr.close();
                }
            } catch (IOException e) {  
                LogUtil.getLog(getClass()).error(e);  
            }  
        }  
    }  
}   