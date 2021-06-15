package com.redmoon.oa.person;

import javax.servlet.http.*;
import javax.swing.*;

import cn.js.fan.util.*;

import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;

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
public class UserSet {
    public static String defaultSkin = SkinMgr.DEFAULT_SKIN_CODE;
 
    static {
        SkinMgr sm = new SkinMgr();
        defaultSkin = sm.getDefaultSkinCode();
    }

    public UserSet() {
    }

    /**
     * 取得用户所选的皮肤
     * @param request
     * @return
     */
    public static String getSkin(HttpServletRequest request) {
    	com.redmoon.oa.Config cfg = Config.getInstance();
		boolean isSpecified = "2".equals(cfg.get("styleMode"));
		int styleSpecified = -1;
		// 指定风格
		if (isSpecified) {
			styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
			if (styleSpecified!=-1) {
				if (styleSpecified == ConstUtil.UI_MODE_LTE) {
					return SkinMgr.SKIN_CODE_LTE;
				}
			}
		}
    	
    	String skinByCookie = StrUtil.getNullString(CookieBean.getCookieValue(request, "oa_skin"));
		AuthUtil authUtil = SpringUtil.getBean(AuthUtil.class);
		boolean isLogin = authUtil.isUserLogin(request);
    	if (!isLogin && !"".equals(skinByCookie)) {
			if ((isSpecified && styleSpecified != ConstUtil.UI_MODE_LTE) && !SkinMgr.SKIN_CODE_LTE.equals(skinByCookie)) {
				return skinByCookie;
			}
    	}

		IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
    	UserSetup userSetup = null;
    	if (isLogin) {
    		Privilege pvg = new Privilege();
			userSetup = userSetupService.getUserSetup(pvg.getUser(request));
		}
    	if (userSetup==null || "".equals(userSetup.getSkinCode())) {
    		SkinMgr sm = new SkinMgr();
    		return sm.getDefaultSkinCode();
    	}else{
    		if(!userSetup.getSkinCode().equals(skinByCookie)){
				userSetup.setSkinCode(skinByCookie);
    			userSetupService.updateByUserName(userSetup);
    			return userSetup.getSkinCode();
    		}
    		return userSetup.getSkinCode();
    	}
    }

	public static void setSkin(HttpServletRequest request, HttpServletResponse response, String skinCode) {
	    CookieBean.addCookie(response, "oa_skin", skinCode, "/", 60*60*24*365); // 保存365天
	}    
}
