package com.redmoon.forum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import cn.js.fan.util.StrUtil;

/**
 *
 * <p>Title: 管理论坛中用到的session</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UserSession {
    public UserSession() {
    }

    public static String getBoardCode(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return StrUtil.getNullString((String)session.getAttribute("boardCode"));
    }

    public static void setBoardCode(HttpServletRequest request, String boardCode) {
        HttpSession session = request.getSession(true);
        session.setAttribute("boardCode", boardCode);
    }
}
