package com.redmoon.oa.person;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.ui.menu.WallpaperDb;
import com.redmoon.oa.usermobile.UserMobileMgr;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

public class UserSetupMgr {
	public boolean modify(HttpServletRequest request, HttpServletResponse response) throws ErrMsgException {
		Privilege pvg = new Privilege();
		UserSetupDb usd = new UserSetupDb();
		usd = usd.getUserSetupDb(pvg.getUser(request));
		int isMsgWinPopup = ParamUtil.getInt(request, "isMsgWinPopup");
		int isChatIconShow = ParamUtil.getInt(request, "isChatIconShow");
		int isChatSoundPlay = ParamUtil.getInt(request, "isChatSoundPlay");
		int isMessageSoundPlay = ParamUtil.getInt(request, "isMessageSoundPlay");
		String skinCode = ParamUtil.get(request, "skinCode");
		
		int isWebedit = ParamUtil.getInt(request, "isWebedit", 0);
		
		int uiMode = ParamUtil.getInt(request, "uiMode", UserSetupDb.UI_MODE_NONE);
		int menuMode = ParamUtil.getInt(request, "menuMode", UserSetupDb.MENU_MODE_NEW);
		
		boolean showSidebar = ParamUtil.getInt(request, "is_show_sidebar", 1)==1;
		
		String emailName = ParamUtil.get(request, "emailName");
		String emailPwd = ParamUtil.get(request, "emailPwd");
		
		usd.setMsgWinPopup(isMsgWinPopup==1);
		usd.setChatIconShow(isChatIconShow==1);
		usd.setChatSoundPlay(isChatSoundPlay==1);
		usd.setMessageSoundPlay(isMessageSoundPlay==1);
		usd.setSkinCode(skinCode);
		usd.setWebedit(isWebedit==1);
		usd.setUiMode(uiMode);
		usd.setShowSidebar(showSidebar);
		
		usd.setEmailName(emailName);
		usd.setEmailPwd(emailPwd);
		
		usd.setMenuMode(menuMode);
		
		boolean re = usd.save();
		if (re) {
			if (uiMode==UserSetupDb.UI_MODE_LTE) {
				UserSet.setSkin(request, response, SkinMgr.SKIN_CODE_LTE);
			}
			else {
				UserSet.setSkin(request, response, skinCode);				
			}
		}
		return re;

	}
	
	public static String getWallpaperPath(String userName) {
		// 如果wallpaper以#号开头，则表示为用户自己上传的壁纸
		UserSetupDb usd = new UserSetupDb();
		usd = usd.getUserSetupDb(userName);
		String wallpaper = usd.getWallpaper();
		// LogUtil.getLog(UserSetupMgr.class).info("wallpaper:" + wallpaper);
		if (wallpaper.equals(""))
			return "images/wallpaper/default.jpg";
		else if (wallpaper.startsWith("#")) {
			WallpaperDb wd = new WallpaperDb();
			String imgPath = wd.getImgPath(userName);
			if (imgPath!=null) {
				imgPath = "upfile/wallpaper/" + imgPath;
				return imgPath;
			}
			else
				return "images/wallpaper/default.jpg";
		}
		else {
			return "images/wallpaper/" + wallpaper;
		}
	}
	
	public boolean modifySkin(HttpServletRequest request, HttpServletResponse response) throws ErrMsgException {
		Privilege pvg = new Privilege();
		UserSetupDb usd = new UserSetupDb();
		usd = usd.getUserSetupDb(pvg.getUser(request));
		String skinCode = ParamUtil.get(request, "skinCode");
		usd.setSkinCode(skinCode);
		boolean re = usd.modifySkin();
		if (re) {
			UserSet.setSkin(request, response, skinCode);
		}
		return re;
	}
	/**
	 * 根据用户名 判断 是否绑定手机
	 * @param userName
	 * @return
	 */
	public boolean isBindMobile(String userName){
		String sql = "SELECT count(user_name)  FROM  user_setup where user_name = "+StrUtil.sqlstr(userName)+" and is_bind_mobile = 1";
		boolean flag = false;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				int result = rr.getInt(1);
				if(result >= 1){
					flag = true;
				}
			}
		} catch (SQLException e) {
			flag = false;
			Logger.getLogger(UserSetupMgr.class).error(e.getMessage());
		}
		return flag;
	}
	
	/**
	 * 根据用户名 判断 是否绑定手机 
	 * @param userName
	 * @author lichao
	 * @return
	 */
	public boolean isBindMobileModify(String userName){
		String sql = "SELECT * FROM  user_setup where user_name = ? and is_bind_mobile = 1";
		boolean flag = true;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql,new Object[]{userName});
			if (!ri.hasNext()){
				flag = false;
			}
		} catch (SQLException e) {
			flag = false;
			Logger.getLogger(UserSetupMgr.class).error(e.getMessage());
		}
		return flag;
	}
	
	/**
	 * 用户解除绑定手机
	 * @param userName
	 * @return
	 */
	public boolean unbindMoible(String userName){
		boolean flag = false;
		UserSetupDb usd = new UserSetupDb(userName);
		UserMobileMgr umm = new UserMobileMgr();
		usd.setBindMobile(false);//解绑
		flag = usd.save();
		if(flag){
			flag = umm.deleteByName(userName) ;
		}
		return flag;
	}
	
	/**
	 * 取得用户头像
	 * @param user
	 * @return
	 */
	public static String getPortrait(UserDb user) {
		String str = "";
		if (user.getPhoto()!=null && !"".equals(user.getPhoto())) {
			str = "img_show.jsp?path=" + user.getPhoto();
		} else {
			if (user.getGender() == 0) {
				str += "images/man.png";
			} else {
				str += "images/woman.png";
			}
		}
		return str;		
	}
	
}
