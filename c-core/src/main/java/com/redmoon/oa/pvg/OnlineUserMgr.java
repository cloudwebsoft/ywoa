package com.redmoon.oa.pvg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.kernel.License;

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
public class OnlineUserMgr {
    public OnlineUserMgr() {
        super();
    }

    /**
     * 获取许可证中的单位，用于online_notify.jsp中
     * @return String
     */
    public static String getJSOrganization() {
    	License lic = License.getInstance();
        if (lic.isValid()) {
        	if (lic.isTrial()) {
	            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	            String appName = cfg.get("enterprise"); 
                return "window.top.document.title='" + appName + " - (试用版，注册后可永久免费使用)';";
        	}
        	else {
	            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	            String appName = cfg.get("enterprise");
	            
	            // 2016-12-02 fgf 如果name是OA，且是带有试用期限的测试版，则显示company(以便于打临时许可证)，否则显示name
	            java.util.Date eDate = lic.getExpiresDate();
	            String showName = lic.getName();
/*	            // 3年以内，则认为是临时许可证
	            if (DateUtil.datediff(eDate, new java.util.Date()) < 3*365) {
	            	showName = lic.getName();
	            }
	            else {
	            	showName = lic.getName();
	            }*/
	            
	            return "window.top.document.title='" + appName + (lic.isFree() ? "(专业版)" : "") + " - " +
	                    	showName + "';";
        	}
        }
        else {
            return "window.top.document.title='(试用版)';";
        }
    }

    public static long getStayTime(HttpServletRequest req) {
        long st = 0;
        Authorization auth = Privilege.getAuthorization(req);
        if (auth!=null) {
            st = auth.getStayTime();
        }
        return st;
    }

    public static void refreshStayTime(HttpServletRequest req, HttpServletResponse res) throws
            ErrMsgException {
        // long staytime = System.currentTimeMillis();
        setStayTime(req, res);
        /*
        long t = (staytime - getStayTime(req)) / 5000; // (1000 * 5);
        if (t > 5) {
            // 大于5秒则刷新
            setStayTime(req, res);
        }
        */
    }

    public static void setStayTime(HttpServletRequest req, HttpServletResponse res) throws
                ErrMsgException {
            Authorization auth = Privilege.getAuthorization(req);
            if (auth==null) {
                return;
            }
            auth.setStayTime(System.currentTimeMillis());
            Privilege.setAuthorization(req, auth);

            OnlineUserDb ou = new OnlineUserDb();
            ou = ou.getOnlineUserDb(auth.getName());
            ou.setStayTime(new java.util.Date());
            // 如果用户在线
            if (ou.isLoaded()) {
            	// 如果
                ou.save();
            } else {
                // 如果不在线，即超时被刷新掉了，则再加入在线列表
                // int isguest = 0;
                ou.setName(auth.getName());
                ou.setIp(StrUtil.getIp(req));
                ou.setSessionId(req.getSession().getId());
                // ou.setGuest(isguest == 1 ? true : false);
                ou.create();
                // logger.info("setStayTime: create " + username + " isguest=" + isguest);
            }
    }

}
