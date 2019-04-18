package com.redmoon.oa.ui.menu;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.oa.pvg.Privilege;

/**
 * <p>Title: </p>
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
public class PresetLeaf {
    final static String CHAT = "chat";

    public static String getLink(HttpServletRequest request, Leaf lf) {
        if (lf.getPreCode().equals(CHAT)) {
            Privilege pvg = new Privilege();
            String nick,room;
            nick = pvg.getUser(request);
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            room = cfg.get("defaultroom");

            String link = "chat/chatframe.jsp?nick=" + StrUtil.UrlEncode(nick) + "&room=" + StrUtil.UrlEncode(room);
            return link;
        }
        else
            return "";
    }
}
