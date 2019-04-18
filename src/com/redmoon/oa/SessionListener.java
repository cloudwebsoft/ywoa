package com.redmoon.oa;

import javax.servlet.http.*;
import java.util.Map;
import java.util.HashMap;
import com.redmoon.oa.pvg.Privilege;
import java.util.Locale;
import cn.js.fan.util.ResBundle;
import java.util.Hashtable;

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
public class SessionListener implements HttpSessionListener {
    private static int sessionCount = 0;
    private static Map sessionMaps = new Hashtable(); // 存放session的集合类

    /**
     * sessionCreated
     *
     * @param httpSessionEvent HttpSessionEvent
     * @todo Implement this javax.servlet.http.HttpSessionListener method
     */
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        String sessionId = session.getId();
        // System.out.println(getClass() + " Create a session:" + sessionId);
        sessionMaps.put(sessionId, session);

        sessionCount++;
    }

    /**
     * sessionDestroyed
     *
     * @param httpSessionEvent HttpSessionEvent
     * @todo Implement this javax.servlet.http.HttpSessionListener method
     */
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        String sessionId = session.getId();
        sessionMaps.remove(sessionId); // 利用会话ID标示特定会话
        sessionCount--;

        String userName = (String)session.getAttribute(Privilege.NAME);
        if (userName!=null) {
            // userName=null 表示会话已被invalidate，即用户已在privilege中logout
            Locale locale = (Locale) session.getAttribute("locale");
            String str = "";
            if (locale != null) {
                try {
                    ResBundle rb = new ResBundle("res.module.log", locale);
                    str = rb.get("action_logout");
                } catch (Exception e) {
                    System.out.println(this.getClass().getName() + ":" +
                                       e.getMessage());
                }
            }

            LogUtil.log((String) session.getAttribute(Privilege.NAME), "",
                        LogDb.TYPE_LOGOUT, str);
        }
        else
            ; // System.out.println(this.getClass().getName() + ": userName=null sessionId=" + session.getId());

        // System.out.println("Destroy a session:" + sessionId);
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
