package cn.js.fan.module.cms.ext;

import javax.servlet.http.HttpSession;
import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;

public class UserSession {
    public UserSession() {
    }

    public static String getDirCode(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return StrUtil.getNullString((String)session.getAttribute("dirCode"));
    }

    public static void setDirCode(HttpServletRequest request, String boardCode) {
        HttpSession session = request.getSession(true);
        session.setAttribute("dirCode", boardCode);
    }
}
