package com.redmoon.forum.plugin.group;

import com.redmoon.forum.plugin.base.IPluginViewCommon;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;

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
public class GroupViewCommon implements IPluginViewCommon {
    HttpServletRequest request;

    public GroupViewCommon(HttpServletRequest request) {
        this.request = request;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case POS_NAV_BAR:
            str = "<a href=\"" + request.getContextPath() + "/forum/plugin/group/group_list.jsp\">" + GroupSkin.LoadString(request, "group") + "</a>";
            break;
        case POS_BOARD_SELECT_OPTION:
            str = "<option value=\"" + GroupUnit.code + "\">" + GroupSkin.LoadString(request, "name") + "</option>";
            break;
        default:
        }
        return str;
    }
}
