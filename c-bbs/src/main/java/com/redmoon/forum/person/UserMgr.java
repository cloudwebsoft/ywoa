package com.redmoon.forum.person;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.forum.Privilege;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.forum.util.ForumFileUpload;

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
public class UserMgr {
    public UserMgr() {
    }

    public boolean DIYMyface(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        UserForm uf = new UserForm();
        ForumFileUpload fu = uf.checkDIYMyface(application, request);
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        UserDb ud = getUser(privilege.getUser(request));
        boolean re = false;
        try {
            re = ud.DIYMyface(application, fu);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public UserDb getUser(String name) {
        UserDb ud = new UserDb();
        return ud.getUser(name);
    }

    public UserDb getUserDbByNick(String nick) {
        UserDb ud = new UserDb();
        return ud.getUserDbByNick(nick);
    }
    
    /**
     * 取得用户头像的URL
     * @param request
     * @param user
     * @return
     */
    public static String getFaceUrl(HttpServletRequest request, UserDb user) {
		String RealPic = user.getRealPic();
		String myface = user.getMyface();
		if (myface==null || myface.equals(""))
			return request.getContextPath() + "/forum/images/face/" + RealPic;
        else
        	return user.getMyfaceUrl(request);    	
    }    
}
