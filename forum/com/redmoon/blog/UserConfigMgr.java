package com.redmoon.blog;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import javax.servlet.ServletContext;

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
public class UserConfigMgr {
    long blogId;

    public UserConfigMgr() {
    }
    
    /**
     * 为用户自动创建博客，用于个人网站中需要音乐时，如果已存在，则返回博客ID
     * @param userName
     * @return
     * @throws ErrMsgException
     */
    public static long getOrCreateForUser(String userName, boolean isOpen) throws ErrMsgException {
		UserConfigDb ucd = new UserConfigDb();
		long blogId = ucd.getBlogIdByUserName(userName);
		if (blogId!=UserConfigDb.NO_BLOG) {
			return blogId;
		}
		ucd.create(userName, isOpen);
		return ucd.getId();
    }

    public boolean create(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
    	String userName = Privilege.getUser(request);

    	int blogType = ParamUtil.getInt(request, "blogType");
    	if (blogType==UserConfigDb.TYPE_PERSON) {
    		UserConfigDb ucd = new UserConfigDb();
    		long blogId = ucd.getBlogIdByUserName(userName);
    		if (blogId!=UserConfigDb.NO_BLOG) {
    			throw new ErrMsgException(SkinUtil.LoadString(request,"res.label.blog.user.userconfig", "activate_blog"));
    		}
    	}
    	
        if (Privilege.isUserLogin(request)) {
            UserConfigForm ucf = new UserConfigForm(application, request);
            ucf.checkCreate();
            UserConfigDb ucd = ucf.getUserConfigDb();
            boolean re = ucd.create(ucf.fileUpload);
            if (re) {
                BlogDb bd = BlogDb.getInstance();
                bd.setNewBlogId(ucd.getId());
                bd.save();
                blogId = ucd.getId();
            }
            return re;
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login")); // "您尚未登陆！");
    }

    public UserConfigDb getUserConfigDb(long blogId) {
        UserConfigDb ucd = new UserConfigDb();
        return ucd.getUserConfigDb(blogId);
    }

    public boolean modify(ServletContext application, HttpServletRequest request) throws
            ErrMsgException, ResKeyException {
        com.redmoon.forum.Privilege pvg = new com.redmoon.forum.Privilege();
        if (!pvg.isMasterLogin(request)) {
            if (!pvg.isUserLogin(request)) {
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "err_not_login")); // "您尚未登陆！");
            }
        }

        UserConfigForm ucf = new UserConfigForm(application, request);
        ucf.checkModify();
        UserConfigDb ucd = ucf.getUserConfigDb();
        return ucd.save(ucf.fileUpload);
    }

    public long getBlogId() {
        return blogId;
    }
    
    public static String getMapPath(HttpServletRequest request, UserConfigDb ucd) {
    	com.redmoon.blog.Config cfg = com.redmoon.blog.Config.getInstance();
		if (cfg.getBooleanProperty("isDomainMapToPath")) {
			return Global.getFullRootPath(request) + "/blog/" + (ucd.getDomain().equals("") ? "" + ucd.getId() : ucd.getDomain());
		} else {
			return Global.getFullRootPath(request) + "/blog/myblog.jsp?blogId=" + ucd.getId();
		}
    }    
    
    /**
     * 取得博客链接
     * @param request
     * @param ucd
     * @return
     */
    public static String getUrl(HttpServletRequest request, UserConfigDb ucd) {
		if (Global.isSubDomainSupported) {
			String serverName = request.getServerName();
			String baseDomain = "";
			// 取得本站主机名
			String[] domainParts = StrUtil.split(serverName, "\\.");
			int len = domainParts.length;
			if (len == 1 || StrUtil.isNumeric(domainParts[len - 1])) {
				// 如果是IP地址或localhost
				return getMapPath(request, ucd);
			} else {
				// 取得一级域名，如 zjrj.cn
				if (domainParts[len - 2].equalsIgnoreCase("gov") && domainParts[len - 1].equalsIgnoreCase("cn")) {
					baseDomain = domainParts[len - 3] + "." + domainParts[len - 2] + "." + domainParts[len - 1];
				} else if (domainParts[len - 2].equalsIgnoreCase("com") && domainParts[len - 1].equalsIgnoreCase("cn")) {
					baseDomain = domainParts[len - 3] + "." + domainParts[len - 2] + "." + domainParts[len - 1];
				} else
					baseDomain = domainParts[len - 2] + "." + domainParts[len - 1];

				return "http://" + (ucd.getDomain().equals("")?""+ucd.getId():ucd.getDomain()) + ".blog." + baseDomain;
			}
		}
		return getMapPath(request, ucd);
	}    
}
