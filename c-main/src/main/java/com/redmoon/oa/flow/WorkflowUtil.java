package com.redmoon.oa.flow;

import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.query.QueryScriptUtil;
import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * XML格式
 * <?xml version="1.0"?>
 <actions>
   <action internalName="0fc9eb3304494d53b3787a0a6e9cda70">
     <view>
       <condition>title=="abc"</condition><display>{"title":"show","ztc":"show"}</display>
     </view>
     <view>
       <condition>#fee>=5000</condition><display>{"ztc":"show"}</display>
     </view>
   </action>
 </actions>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowUtil {

    public static final String OP_FLOW_PROCESS = "flowProcess";
    public static final String OP_FLOW_SHOW = "flowShow";
    public static final String OP_RESET_PWD = "resetPwd";

    public WorkflowUtil() {
    }
    
    /**
     * 判断请求是否手机端
     * @param req
     * @return
     */
    public static boolean isMobile(HttpServletRequest req) {
        UserAgent ua = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
        OperatingSystem os = ua.getOperatingSystem();
        if(DeviceType.MOBILE.equals(os.getDeviceType())) {
            return true;
        }
        return false;
    }

    
    public static int getColumnType(String dbSource, String tableCode, String columnName) {
    	String sql = "select * from " + tableCode;
    	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
    	try {
    		conn.setMaxRows(1); //尽量减少内存的使用
    		ResultSet rs = conn.executeQuery(sql);
    		ResultSetMetaData rm = rs.getMetaData();
    		int colCount = rm.getColumnCount();
    		for (int i = 1; i <= colCount; i++) {
    			if (rm.getColumnName(i).equals(columnName)) {
    				return QueryScriptUtil.getFieldTypeOfDBType(rm.getColumnType(i));		
    			}
    		}
    	}
    	catch (SQLException e) {
    		LogUtil.getLog(WorkflowUtil.class).error(e);
    	}
    	finally {
    		conn.close();
    	}
    	return -1;
    }

    /**
     * 取得跳转的路由
     * @param op
     * @param action
     * @return
     */
    public static String getJumpUrl(String op, String action) {
        SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
        return sysUtil.getFrontPath() +
                "jump?op=" + op + "&action=" + action;
    }
}
