package com.redmoon.oa.person;

import javax.servlet.http.*;

import cn.js.fan.util.*;

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

    public static String getSkin(HttpServletRequest request) {
    	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isSpecified = cfg.get("styleMode").equals("2"); 
		// 指定风格
		if (isSpecified) {
			int styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
			if (styleSpecified!=-1) {
				if (styleSpecified==UserSetupDb.UI_MODE_LTE) {
					return SkinMgr.SKIN_CODE_LTE;
				}
			}
		}
    	
    	String skinByCookie = StrUtil.getNullString(CookieBean.getCookieValue(request, "oa_skin"));
    	Privilege pvg = new Privilege();
    	if (!pvg.isUserLogin(request)) {
	        return skinByCookie;
    	}
    	UserSetupDb usd = new UserSetupDb();
    	usd = usd.getUserSetupDb(pvg.getUser(request));
    	if (usd.getSkinCode().equals("")) {
    		SkinMgr sm = new SkinMgr();
    		return sm.getDefaultSkinCode();
    	}else{
    		if(!usd.getSkinCode().equals(skinByCookie)){
    			usd.setSkinCode(skinByCookie);
    			usd.save();
    			return usd.getSkinCode();
    		}
    		return usd.getSkinCode();
    	}
    }

	public static void setSkin(HttpServletRequest request, HttpServletResponse response, String skinCode) {
	    CookieBean.addCookie(response, "oa_skin", skinCode, "/", 60*60*24*365); // 保存365天
	}    
}
