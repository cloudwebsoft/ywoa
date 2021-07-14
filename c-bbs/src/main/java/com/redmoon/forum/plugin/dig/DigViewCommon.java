package com.redmoon.forum.plugin.dig;

import com.redmoon.forum.plugin.base.IPluginViewCommon;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DigViewCommon implements IPluginViewCommon {
    HttpServletRequest request;

    public DigViewCommon(HttpServletRequest request) {
        this.request = request;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case POS_NAV_BAR:
            break;
        case POS_BOARD_SELECT_OPTION:
            break;
        case POS_TOPIC_TOOLBAR:

            break;
        default:
        }
        return str;
    }
}
