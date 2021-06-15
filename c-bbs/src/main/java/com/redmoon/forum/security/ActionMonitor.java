package com.redmoon.forum.security;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.CookieBean;
import com.redmoon.forum.Config;
import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Title: ����������</p>
 *
 * <p>Description: ����Ƿ�Ƶ��ˢ��</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ActionMonitor {
    public ActionMonitor() {
    }

    /**
     * ����Ƿ�Ƶ��ˢ��
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return boolean
     */
    public static boolean canVisit(HttpServletRequest request, HttpServletResponse response) {
        Config cfg = Config.getInstance();
        int interval = cfg.getVisitInterval();
        if (interval==0)
            return true;

        String strTime = CookieBean.getCookieValue(request, "lastVisitTime");
        if (strTime.equals(""))
            strTime = "0";

        long curTime = System.currentTimeMillis();
        long lastTime = StrUtil.toLong(strTime);

        // System.out.println("ActionMonitor: lastVisitTime=" + cb.getCookieValue(request, "lastVisitTime") + " curInterval=" + (curTime - lastTime) + " interval=" + interval);

        if (curTime - lastTime <= interval)
            return false;
        else
        	CookieBean.addCookie(response, "lastVisitTime", "" + curTime);
        return true;
    }
}
