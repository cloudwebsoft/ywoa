package com.redmoon.oa;

import java.io.OutputStream;
import java.util.Iterator;

import cn.js.fan.module.pvg.UserMgr;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0 
 */
public class LogUtil {  
    public LogDb getLogDb(int id) {
        LogDb ld = new LogDb();
        return ld.getLogDb(id);
    }

    public static boolean log(String userName, String ip, int type, String action) {
        LogDb ld = new LogDb();
        ld.setUserName(userName);
        ld.setIp(ip);
        ld.setType(type);
        ld.setAction(action);
        ld.setUnitCode(DeptDb.ROOTCODE);
        return ld.create();
    }

    public static String get(HttpServletRequest request, String resProp) {
        return SkinUtil.LoadString(request, "res.module.log",
                                           resProp);
    }
    
    public static String format(HttpServletRequest request, String resKey, Object[] objs) {
    	return StrUtil.format(get(request, resKey), objs);
    }

    public static String getTypeDesc(HttpServletRequest request, int type) {
        String desc = "";
        switch (type) {
            case LogDb.TYPE_LOGIN:
                desc = SkinUtil.LoadString(request, "res.module.log",
                                           "TYPE_LOGIN");
                break;
            case LogDb.TYPE_LOGOUT:
                desc = SkinUtil.LoadString(request, "res.module.log",
                                           "TYPE_LOGOUT");
                break;
            case LogDb.TYPE_ACTION:
                desc = SkinUtil.LoadString(request, "res.module.log",
                                           "TYPE_ACTION");
                break;
            case LogDb.TYPE_PRIVILEGE:
                desc = SkinUtil.LoadString(request, "res.module.log",
                						   "TYPE_PRIVILEGE");
                break;
            case LogDb.TYPE_WARN:
                desc = SkinUtil.LoadString(request, "res.module.log",
                                           "TYPE_WARN");
                break;
            case LogDb.TYPE_ERROR:
                desc = SkinUtil.LoadString(request, "res.module.log",
                                           "TYPE_ERROR");
                break;
                
            case LogDb.TYPE_HACK:
                desc = SkinUtil.LoadString(request, "res.module.log",
                                           "TYPE_HACK");
                break;                
            default:;
        }
        return desc;
    }
    
    /**
     * 输出Excel
     *
     * @param os
     */
    public static void writeExcel(HttpServletRequest request, OutputStream os, String sql) {
        try { //创建工作薄
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            //创建工作表
            WritableSheet ws = wwb.createSheet("操作日志", 0);
            LogDb ld = new LogDb();
            Iterator ir = ld.list(sql).iterator();
            
            Label labelA = new Label(0, 0, "时间");
            Label labelB = new Label(1, 0, "用户名");
            Label labelC = new Label(2, 0, "部门");
            Label labelD = new Label(3, 0, "动作");
            Label labelE = new Label(4, 0, "IP");
            Label labelF = new Label(5, 0, "类型");

            ws.addCell(labelA);
            ws.addCell(labelB);
            ws.addCell(labelC);
            ws.addCell(labelD);
            ws.addCell(labelE);
            ws.addCell(labelF);

            int i = 1, j = 1;
            com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
            DeptUserDb dud = new DeptUserDb();
            DeptMgr dm = new DeptMgr();
            while (ir.hasNext()) {
                ld = (LogDb) ir.next();
               
                UserDb ud = um.getUserDb(ld.getUserName());
                Label labelA1 = new Label(0, j, DateUtil.format(ld.getDate(), "yyyy-MM-dd HH:mm:ss"));
                Label labelB1 = new Label(1, j, ud.getRealName());

                Iterator ir2 = dud.getDeptsOfUser(ud.getName()).iterator();
    			int k = 0;
    			String dName = "";
    			while (ir2.hasNext()) {
    				DeptDb dd = (DeptDb)ir2.next();
    				String deptName = "";
    				if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {					
    					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
    				}
    				else
    					deptName = dd.getName();
    				if ("".equals(dName)) {
    					dName = deptName;
    				}
    				else {
    					dName += "  " + deptName;
    				}
    				k++;
    			} 
    			Label labelC1 = new Label(2, j, dName);
    			Label labelD1 = new Label(3, j, ld.getAction());
    			Label labelE1 = new Label(4, j, ld.getIp());
    			Label labelF1 = new Label(5, j, LogUtil.getTypeDesc(request, ld.getType()));
                
                j++;
                ws.addCell(labelA1);
                ws.addCell(labelB1);
                ws.addCell(labelC1);
                ws.addCell(labelD1);
                ws.addCell(labelE1);
                ws.addCell(labelF1);
            }
            wwb.write();
            wwb.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }    
    
    
}
