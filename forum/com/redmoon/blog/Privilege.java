package com.redmoon.blog;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;

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
public class Privilege extends com.redmoon.forum.Privilege {
    public static final String PRIV_ENTER = "enter"; // 进入后台
    public static final String PRIV_ALL = "priv_all"; // 管理博客，在团队博客中表示全部权限即最高权限

    public Privilege() {
    }

    public static boolean canUserDo(HttpServletRequest request, long blogId,
                                    String priv) throws ErrMsgException {
        if (isMasterLogin(request)) {
            return true;
        }
        if (!isUserLogin(request))
            return false;
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        if (!ucd.isLoaded()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.label.blog.user.userconfig",
                    "activate_blog_fail"));
        }
        String user = getUser(request);
        if (ucd.getType() == UserConfigDb.TYPE_PERSON) {
            if (!ucd.getUserName().equals(user)) {
                return false;
            } else
                return true;
        } else if (ucd.getType() == UserConfigDb.TYPE_GROUP) {
            // 检查用户权限
            return BlogGroupUserDb.canUserDo(request, blogId, priv);
        } else
            return false;
    }
}
