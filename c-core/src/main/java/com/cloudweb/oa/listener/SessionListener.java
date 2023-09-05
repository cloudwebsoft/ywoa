package com.cloudweb.oa.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import java.util.Map;

import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.LogUtil;
import java.util.Locale;
import cn.js.fan.util.ResBundle;
import java.util.concurrent.ConcurrentHashMap;

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

@WebListener
public class SessionListener implements HttpSessionListener {
    private static int sessionCount = 0;
    private static Map sessionMaps = new ConcurrentHashMap(); // 存放session的集合类

    /**
     * sessionCreated
     *
     * @param httpSessionEvent HttpSessionEvent
     */
    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        String sessionId = session.getId();
        sessionMaps.put(sessionId, session);

        sessionCount++;
    }

    /**
     * sessionDestroyed
     *
     * @param httpSessionEvent HttpSessionEvent
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        String sessionId = session.getId();
        sessionMaps.remove(sessionId); // 利用会话ID标示特定会话
        sessionCount--;

        /*String userName = (String)session.getAttribute(ConstUtil.SESSION_NAME);
        if (userName!=null) {
            // userName=null 表示会话已被invalidate，即用户已在privilege中logout
            Locale locale = (Locale) session.getAttribute("locale");
            String str = "";
            if (locale != null) {
                try {
                    ResBundle rb = new ResBundle("res.module.log", locale);
                    str = rb.get("action_logout");
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).info(this.getClass().getName() + ":" + e.getMessage());
                }
            }

            LogUtil.log((String) session.getAttribute(ConstUtil.SESSION_NAME), "", LogDb.TYPE_LOGOUT, str);
        }*/
    }

    public static int getSessionCount() {
        return sessionCount;
    }

    public static Map getSessionMaps() {
        return sessionMaps;
    }

    public static HttpSession getSession(String sessionId) {
        return (HttpSession)sessionMaps.get(sessionId);
    }
}