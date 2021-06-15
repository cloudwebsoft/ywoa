package com.redmoon.forum.plugin.remark;

import javax.servlet.http.*;

import com.redmoon.forum.plugin.base.*;

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
public class RemarkViewCommon implements IPluginViewCommon {
    HttpServletRequest request;

    public RemarkViewCommon(HttpServletRequest request) {
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
