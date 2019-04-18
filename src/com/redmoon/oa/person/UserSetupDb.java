package com.redmoon.oa.person;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.user.UserService;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.netdisk.RoleTemplateMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.tigase.TigaseConnection;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.mgr.WXUserMgr;

import javax.servlet.http.HttpServletRequest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

public class UserSetupDb extends ObjectDb {
	public String QUERY_MODIFY;
	
	/**
	 * 
	 */
	public static final int UI_MODE_NONE = 0;
	/**
	 * 经典型简洁菜单
	 */
	public static final int UI_MODE_PROFESSION = 1;
	/**
	 * 时尚型
	 */
	public static final int UI_MODE_FASHION = 2;
	/**
	 * 绚丽型
	 */
	public static final int UI_MODE_FLOWERINESS = 3;
	
	/**
	 * 经典型传统菜单，即1.3菜单样式
	 */
	public static final int UI_MODE_PROFESSION_NORMAL = 4;
	
	/**
	 * 轻简型
	 */
	public static final int UI_MODE_LTE = 5;
	
	/**
	 * 2.0菜单样式
	 */
	public static final int MENU_MODE_NEW = 1;
	/**
	 * 1.3菜单样式
	 */
	public static final int MENU_MODE_NORMAL = 0;
	
	int uiMode = UI_MODE_NONE;
	
	private String wallpaper = "";
	
	private boolean showSidebar = true;
	private boolean bindMobile = true;

	public boolean isBindMobile() {
		return bindMobile;
	}

	public void setBindMobile(boolean bindMobile) {
		this.bindMobile = bindMobile;
	}

	/**
	 * {"widgets":[{"id":"note","left":"","top":""},{...}]}
	 */
	private String mydesktopProp = "";
	
	private java.util.Date lastMsgNotifyTime;
	
	private String emailName;
	private String emailPwd;
	private boolean msgChat = true;
	private String local = "zh-CN";
	private int client;   //1：android  0 ：ios
	private String token;  
	private String oldPwd;
	private String myleaders;
	
	private int menuMode;
	
    public int getClient() {
		return client;
	}

	public void setClient(int client) {
		this.client = client;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isShowSidebar() {
		return showSidebar;
	}

	public void setShowSidebar(boolean showSidebar) {
		this.showSidebar = showSidebar;
	}

	/**
	 * @return the menuMode
	 */
	public int getMenuMode() {
		return menuMode;
	}

	/**
	 * @param menuMode the menuMode to set
	 */
	public void setMenuMode(int menuMode) {
		this.menuMode = menuMode;
	}

	public int getUiMode() {
		return uiMode;
	}

	public void setUiMode(int uiMode) {
		this.uiMode = uiMode;
	}

	/**
	 * @return the oldPwd
	 */
	public String getOldPwd() {
		return oldPwd;
	}

	/**
	 * @param oldPwd the oldPwd to set
	 */
	public void setOldPwd(String oldPwd) {
		this.oldPwd = oldPwd;
	}

	/**
	 * @return the myleaders
	 */
	public String getMyleaders() {
		return myleaders;
	}

	/**
	 */
	public void setMyleaders(String myleaders) {
		this.myleaders = myleaders;
	}

	public UserSetupDb() {
        init();
        menuMode = MENU_MODE_NEW;
    }
 
    public UserSetupDb(String userName) {
        init();
        this.userName = userName;
        menuMode = MENU_MODE_NEW;        
        load();
    }

    public void initDB() {
        tableName = "user_setup";
        primaryKey = new PrimaryKey("USER_NAME", PrimaryKey.TYPE_STRING);
        objectCache = new UserSetupCache(this);
        this.isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName + " (USER_NAME, MESSAGE_TO_DEPT, MESSAGE_TO_USERGROUP, MESSAGE_TO_USERROLE, MESSAGE_TO_MAX_USER, MESSAGE_USER_MAX_COUNT, is_message_sound_play,skin_code, is_webedit, msg_space_allowed, last_msg_notify_time, is_msg_chat,local,client,token,myleaders,menu_mode) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set MESSAGE_TO_DEPT=?,MESSAGE_TO_USERGROUP=?,MESSAGE_TO_USERROLE=?,MESSAGE_TO_MAX_USER=?,MESSAGE_USER_MAX_COUNT=?,IS_MSG_WIN_POPUP=?, IS_CHAT_SOUND_PLAY=?, IS_CHAT_ICON_SHOW=?,is_message_sound_play=?,skin_code=?,weather_code=?,clock_code=?,calendar_code=?,is_webedit=?,msg_space_allowed=?,msg_space_used=?,ui_mode=?,wallpaper=?,is_show_sidebar=?,mydesktop_prop=?,last_msg_notify_time=?,email_name=?,email_pwd=?,is_msg_chat=?,local=?,key_id=?,is_bind_mobile = ?,client=?,token=?,myleaders=?,menu_mode=? where USER_NAME=?";
        QUERY_LOAD =
                "select MESSAGE_TO_DEPT, MESSAGE_TO_USERGROUP, MESSAGE_TO_USERROLE, MESSAGE_TO_MAX_USER, MESSAGE_USER_MAX_COUNT, IS_MSG_WIN_POPUP, IS_CHAT_SOUND_PLAY, IS_CHAT_ICON_SHOW,is_message_sound_play,skin_code,weather_code,clock_code,calendar_code,is_webedit,msg_space_allowed,msg_space_used,ui_mode,wallpaper,is_show_sidebar,mydesktop_prop,last_msg_notify_time,email_name,email_pwd,is_msg_chat,local,key_id,is_bind_mobile,client,token,myleaders,menu_mode from " + tableName + " where USER_NAME=?";
        QUERY_DEL = "delete from " + tableName + " where USER_NAME=?";
        QUERY_LIST = "select USER_NAME from " + tableName;
        QUERY_MODIFY = "update " + tableName + " set skin_code=? where USER_NAME=?";
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserSetupDb(pk.getStrValue());
    }

    public UserSetupDb getUserSetupDb(String name) {
        UserSetupDb usd = (UserSetupDb) getObjectDb(name);
        if (usd == null || !usd.isLoaded()) {
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            messageToMaxUser = cfg.getInt("message_to_max_user");
            messageUserMaxCount = cfg.getInt("message_user_max_count");
            
/*            String strDiskSpaceAllowed = cfg.get("msg_space_allowed");
            long diskSpaceAllowed = 1024000;
            if (StrUtil.isNumeric(strDiskSpaceAllowed)) {
                diskSpaceAllowed = Long.parseLong(strDiskSpaceAllowed);
            }*/
            
            RoleDb rd = new RoleDb();
            rd = rd.getRoleDb(RoleDb.CODE_MEMBER);   
            long diskSpaceAllowed = rd.getMsgSpaceQuota();
            
            usd = new UserSetupDb();
            usd.setUserName(name);
            usd.setMessageToDept("");
            usd.setMessageToUserGroup("");
            usd.setMessageToUserRole("");
            usd.setMessageToMaxUser(messageToMaxUser);
            usd.setMessageUserMaxCount(messageUserMaxCount);
            usd.setSkinCode("");
            usd.setMsgSpaceAllowed(diskSpaceAllowed);
            usd.create();
        }
        return usd;
    }

	public UserSetupDb getUserSetupDbByKeyid(String keyid) {
     	Conn conn = new Conn(connname);
		ResultSet rs = null;
		try {
			String sql = "select USER_NAME from "
					+ tableName + " where KEY_ID=?";

			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, keyid);
			rs = conn.executePreQuery();
			
			if (rs != null) {
				if (rs.next()) {
					return getUserSetupDb(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			logger.error("load:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}

		return null;
	}

	/**
	 * 当导入用户时
	 * 
	 * @param userName
	 */
	public void onUserCreate(String userName) {
		// 同步到tigase
    	com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
		if (tigaseCfg.getBooleanProperty("isUse")) {		
			TigaseConnection tc = new TigaseConnection();
			tc.addUser(userName);
			tc.addMyFriends(userName);
		}
		UserDb _userDb = new UserDb(userName);
		if(_userDb!=null && _userDb.isLoaded()){
			//同步到微信企业号
            com.redmoon.weixin.Config weixinCfg = Config.getInstance();
			if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
				WXUserMgr _wxUserMgr = new WXUserMgr();
				_wxUserMgr.createWxUser(_userDb);
			}
            com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
			if(dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                UserService _userService = new UserService();
                _userService.createUser(_userDb);
            }
		}
	}

	/**
	 * 当创建用户时
	 * 
	 * @param request
	 * @param fu
	 * @param userName
	 */
	public void onUserCreate(HttpServletRequest request, FileUpload fu,
			String userName) {
		if (fu != null) {
			String keyId = StrUtil.getNullStr(fu.getFieldValue("keyId"));
			if (!"".equals(keyId)){
				setKeyId(keyId);
				String myleaders = StrUtil.getNullStr(fu.getFieldValue("leaderCode"));
				setMyleaders(myleaders);
				save();
			}
		}
		
		// 更新personbasic表
		updatePersonbasic(request, userName);
		
		// 同步到tigase
    	com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
		if (tigaseCfg.getBooleanProperty("isUse")) {			
			TigaseConnection tc = new TigaseConnection();
			String deptCode = ParamUtil.get(request, "deptCode");
			// 新增用户时,部门用户表里还没有用户信息,所以需要将部门code单独处理
			tc.setDeptCode(deptCode);
			tc.addUser(userName);
			tc.addMyFriends(userName);
		}

		UserDb _userDb = new UserDb(userName);
		if(_userDb!=null && _userDb.isLoaded()){
			//同步到微信企业号
            com.redmoon.weixin.Config weixinCfg = Config.getInstance();
			if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
				WXUserMgr _wxUserMgr = new WXUserMgr();
				_wxUserMgr.createWxUser(_userDb);
			}
			// 同步至钉钉
            com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
			if(dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                UserService _userService = new UserService();
                _userService.createUser(_userDb);
            }
		}
	}

	/**
	 * 当保存用户时
	 * 
	 * @param request
	 * @param fu
	 * @param userName
	 */
	public void onUserSave(HttpServletRequest request, FileUpload fu,
			String userName) {
		if (fu != null) {
			String keyId = StrUtil.getNullStr(fu.getFieldValue("keyId"));
			setKeyId(keyId);
			
			String leaders = StrUtil.getNullStr(fu.getFieldValue("leaderCode"));
			if (("," + leaders + ",").indexOf("," + userName + ",")==-1) {
				setMyleaders(leaders);
			}
			save();
		}

		// 更新personbasic表
		updatePersonbasic(request, userName);
		
		// 同步到tigase
    	com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
		if (tigaseCfg.getBooleanProperty("isUse")) {			
			TigaseConnection tc = new TigaseConnection();
			tc.syncUser(userName, oldPwd);
		}

		UserDb _userDb = new UserDb(userName);
		if(_userDb!=null && _userDb.isLoaded()){
			//同步到微信企业号
			com.redmoon.weixin.Config weixinCfg = Config.getInstance();
			if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
				WXUserMgr _wxUserMgr = new WXUserMgr();
				_wxUserMgr.updateWxUser(_userDb);
			}
            com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
			if(dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                UserService _userService = new UserService();
                _userService.updateUser(_userDb);
            }
		}
	}

	public void onUserLeaveOffice(HttpServletRequest request, String userName) {
		// 更新personbasic表
		if (request!=null)
			updatePersonbasic(request, userName);

		// 同步到tigase
		com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
		if (tigaseCfg.getBooleanProperty("isUse")) {
			TigaseConnection tc = new TigaseConnection();
			tc.delUser(userName);
		}
		//同步到微信企业号
		UserDb userDb = new UserDb();
		userDb = userDb.getUserDb(userName);
		com.redmoon.weixin.Config weixinCfg = Config.getInstance();
		if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
			String userId = userName;
			if(weixinCfg.isUserIdUseEmail()){
				userId = userDb.getWeixin();
			}
			else if (weixinCfg.isUserIdUseAccount()) {
				// 使用工号登录
				AccountDb accountDb = new AccountDb();
				accountDb = accountDb.getUserAccount(userId);
				userId = accountDb.getName();
			}
			else if (weixinCfg.isUserIdUseMobile()) {
				userId = userDb.getWeixin();
			}

			WXUserMgr _wxUserMgr = new WXUserMgr();
			_wxUserMgr.deleteUser(userId);
		}
		com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
		if(dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
			String _userId = userName;
			int _useIdUse = dingdingCfg.isUserIdUse();
			switch (_useIdUse) {
				case Enum.emBindAcc.emEmail:
					_userId = userDb.getEmail();
					break;
				case Enum.emBindAcc.emMobile:
					_userId = userDb.getMobile();
					break;
				case Enum.emBindAcc.emUserName:
					_userId = userName;
					break;
			}
			UserService _userService = new UserService();
			_userService.delUser(_userId);
		}
	}

	/**
	 * 当启用时，即离职后重新入职时
	 * @param request
	 * @param userName
	 */
	public void onReEmploryment(HttpServletRequest request, String userName) {
		onUserCreate(userName);
	}

	/**
	 * 更新人员信息表
	 * @param userName 用户
	 */
	private void updatePersonbasic(HttpServletRequest request, String userName) {
		UserDb userDb = new UserDb(userName);
		Privilege privilege = new Privilege();
		// 判断配置中是否设置了同步帐户
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");

		if (isArchiveUserSynAccount) {
			String deptCode = ParamUtil.get(request, "deptCode");
			if (deptCode == null || deptCode.equals("")) {
				DeptUserDb dud = new DeptUserDb(userName);
				if (dud != null && dud.isLoaded()) {
					deptCode = dud.getDeptCode();
				}
			}
			// 人员基本信息表存在,则同步至人员基本信息表
			FormDb fd = new FormDb("personbasic");
			if (fd != null && fd.isLoaded()) {
				try {
					FormDAO fdao = new FormDAO(fd);
					Iterator it = fdao.list("personbasic", "select id from form_table_personbasic where user_name=" + StrUtil.sqlstr(userDb.getName())).iterator();
					if (it.hasNext()) {
						fdao = (FormDAO) it.next();
						fdao.setCreator(privilege.getUser(request));
						fdao.setUnitCode(userDb.getUnitCode());
						fdao.setFieldValue("user_name", userDb.getName());
						fdao.setFieldValue("realname", userDb.getRealNameRaw());
						String birthday = DateUtil.format(userDb.getBirthday(), "yyyy-MM-dd");
						fdao.setFieldValue("csrq", birthday);
						if (birthday != null && !birthday.equals("")) {
							fdao.setFieldValue("age", String.valueOf(DateUtil.getYear(new java.util.Date()) - DateUtil.getYear(userDb.getBirthday())));
						}
						fdao.setFieldValue("sex", userDb.getGender() == 0 ? "男" : "女");
						fdao.setFieldValue("idcard", userDb.getIDCard());
						fdao.setFieldValue("mobile", userDb.getMobile());
						fdao.setFieldValue("address", userDb.getAddress());
						fdao.setFieldValue("dept", deptCode);
						fdao.setFieldValue("zzqk", userDb.isValid() ? "1" : "0");
						fdao.setFieldValue("entry_date", DateUtil.format(userDb.getEntryDate(), "yyyy-MM-dd"));
						fdao.setFieldValue("person_no", userDb.getPersonNo());
						fdao.setUnitCode(userDb.getUnitCode());
						fdao.save();
					} else {
						fdao.setFieldValue("user_name", userDb.getName());
						fdao.setFieldValue("realname", userDb.getRealName());
						String birthday = DateUtil.format(userDb.getBirthday(), "yyyy-MM-dd");
						fdao.setFieldValue("csrq", birthday);
						if (birthday != null && !birthday.equals("")) {
							fdao.setFieldValue("age", String.valueOf(DateUtil.getYear(new java.util.Date()) - DateUtil.getYear(userDb.getBirthday())));
						}
						fdao.setFieldValue("sex", userDb.getGender() == 0 ? "男" : "女");
						fdao.setFieldValue("idcard", userDb.getIDCard());
						fdao.setFieldValue("mobile", userDb.getMobile());
						fdao.setFieldValue("address", userDb.getAddress());
						fdao.setFieldValue("dept", deptCode);
						fdao.setFieldValue("zzqk", "1"); // 在职
						fdao.setFieldValue("entry_date", DateUtil.format(userDb.getEntryDate(), "yyyy-MM-dd"));
						fdao.setFieldValue("person_no", userDb.getPersonNo());
						fdao.setUnitCode(userDb.getUnitCode());
						fdao.create();
					}
				} catch (Exception e) {
					logger.error("updatePersonbasic:" + e.getMessage());
				}
			}
		}
	}

	/**
	 * 当删除时
	 * 
	 * @param userName
	 */
	public void onUserDel(String userName) {
		RoleTemplateMgr rtm = new RoleTemplateMgr();
		rtm.delDirsByUserName(userName);

		UserDb userDb = new UserDb(userName);
		// 判断配置中是否设置了同步帐户
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");

		if (isArchiveUserSynAccount) {
			// 人员基本信息表存在,则同步至人员基本信息表
			FormDb fd = new FormDb("personbasic");
			if (fd != null && fd.isLoaded()) {
				try {
					FormDAO fdao = new FormDAO(fd);
					Iterator it = fdao.list("personbasic", "select id from form_table_personbasic where user_name=" + StrUtil.sqlstr(userDb.getName())).iterator();
					if (it.hasNext()) {
						fdao = (FormDAO) it.next();
						fdao.setFieldValue("zzqk", "0");
						fdao.save();
					}
				} catch (Exception e) {
					logger.error("updatePersonbasic:" + e.getMessage());
				}
			}
		}
		
		// 同步到tigase
    	com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
		if (tigaseCfg.getBooleanProperty("isUse")) {			
			TigaseConnection tc = new TigaseConnection();
			tc.delUser(userName);
		}
		//同步到微信企业号
		com.redmoon.weixin.Config weixinCfg = Config.getInstance();
		if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
			String userId = userName;
			if(weixinCfg.isUserIdUseEmail()){
				userId = userDb.getWeixin();
			}
			else if (weixinCfg.isUserIdUseAccount()) {
				// 使用工号登录
				AccountDb accountDb = new AccountDb();
				accountDb = accountDb.getUserAccount(userId);
				userId = accountDb.getName();
			}
			else if (weixinCfg.isUserIdUseMobile()) {
				userId = userDb.getWeixin();
			}

			WXUserMgr _wxUserMgr = new WXUserMgr();
			_wxUserMgr.deleteUser(userId);
		}
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
		if(dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            String _userId = userName;
            int _useIdUse = dingdingCfg.isUserIdUse();
            switch (_useIdUse) {
                case Enum.emBindAcc.emEmail:
                    _userId = userDb.getEmail();
                    break;
                case Enum.emBindAcc.emMobile:
                    _userId = userDb.getMobile();
                    break;
                case Enum.emBindAcc.emUserName:
                    _userId = userName;
                    break;
            }
            UserService _userService = new UserService();
            _userService.delUser(_userId);
        }

	}

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMessageToDept(String messageToDept) {
        this.messageToDept = messageToDept;
    }

    public void setMessageToUserGroup(String messageToUserGroup) {
        this.messageToUserGroup = messageToUserGroup;
    }

    public void setMessageToUserRole(String messageToUserRole) {
        this.messageToUserRole = messageToUserRole;
    }

    public void setMessageToMaxUser(int messageToMaxUser) {
        this.messageToMaxUser = messageToMaxUser;
    }

    public void setMessageUserMaxCount(int messageUserMaxCount) {
        this.messageUserMaxCount = messageUserMaxCount;
    }

    public void setMsgWinPopup(boolean msgWinPopup) {
        this.msgWinPopup = msgWinPopup;
    }

    public void setChatSoundPlay(boolean chatSoundPlay) {
        this.chatSoundPlay = chatSoundPlay;
    }

    public void setChatIconShow(boolean chatIconShow) {
        this.chatIconShow = chatIconShow;
    }

    public void setMessageSoundPlay(boolean messageSoundPlay) {
        this.messageSoundPlay = messageSoundPlay;
    }

    public void setSkinCode(String skinCode) {
        this.skinCode = skinCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessageToDept() {
        return messageToDept;
    }

    public String getMessageToUserGroup() {
        return messageToUserGroup;
    }

    public String getMessageToUserRole() {
        return messageToUserRole;
    }

    public int getMessageToMaxUser() {
        return messageToMaxUser;
    }

    public int getMessageUserMaxCount() {
        return messageUserMaxCount;
    }

    public boolean isMsgWinPopup() {
        return msgWinPopup;
    }

    public boolean isChatSoundPlay() {
        return chatSoundPlay;
    }

    public boolean isChatIconShow() {
        return chatIconShow;
    }

    public boolean isMessageSoundPlay() {
        return messageSoundPlay;
    }

    public String getSkinCode() {
        return skinCode;
    }

    public boolean create() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setString(1, userName);
            pstmt.setString(2, messageToDept);
            pstmt.setString(3, messageToUserGroup);
            pstmt.setString(4, messageToUserRole);
            pstmt.setInt(5, messageToMaxUser);
            pstmt.setInt(6, messageUserMaxCount);
            pstmt.setInt(7, messageSoundPlay?1:0);
            pstmt.setString(8, skinCode);
            pstmt.setInt(9, webedit?1:0);
            pstmt.setLong(10, msgSpaceAllowed);
            pstmt.setTimestamp(11, new java.sql.Timestamp(new java.util.Date().getTime()));
            pstmt.setInt(12, msgChat ? 1:0);
            pstmt.setString(13, local);
            pstmt.setInt(14, client);
            pstmt.setString(15, token);
            pstmt.setString(16, myleaders);
            pstmt.setInt(17, menuMode);
            
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                UserSetupCache rc = new UserSetupCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, messageToDept);
            pstmt.setString(2, messageToUserGroup);
            pstmt.setString(3, messageToUserRole);
            pstmt.setInt(4, messageToMaxUser);
            pstmt.setInt(5, messageUserMaxCount);
            pstmt.setInt(6, msgWinPopup?1:0);
            pstmt.setInt(7, chatSoundPlay?1:0);
            pstmt.setInt(8, chatIconShow?1:0);
            pstmt.setInt(9, messageSoundPlay?1:0);
            pstmt.setString(10, skinCode);
            
            pstmt.setString(11, weatherCode);
            pstmt.setString(12, clockCode);
            pstmt.setString(13, calendarCode);
            
            pstmt.setInt(14, webedit?1:0);
            pstmt.setLong(15, msgSpaceAllowed);
            pstmt.setLong(16, msgSpaceUsed);
            
            pstmt.setInt(17, uiMode);
            pstmt.setString(18, wallpaper);
            pstmt.setInt(19, showSidebar?1:0);
            pstmt.setString(20, mydesktopProp);
            pstmt.setInt(14, webedit?1:0);
            if (lastMsgNotifyTime==null)
            	pstmt.setTimestamp(21, null);
            else
            	pstmt.setTimestamp(21, new Timestamp(lastMsgNotifyTime.getTime()));
            
            pstmt.setString(22, emailName);
            pstmt.setString(23, emailPwd);
            pstmt.setInt(24, msgChat ? 1:0);
            pstmt.setString(25, local);
			pstmt.setString(26, keyId);
			pstmt.setInt(27, bindMobile ? 1 : 0);
			pstmt.setInt(28,client);
			pstmt.setString(29,token);
			pstmt.setString(30, myleaders);
			pstmt.setInt(31, menuMode);
			pstmt.setString(32,userName);

            re = conn.executePreUpdate()==1?true:false;
            
            if (re) {
                UserSetupCache rc = new UserSetupCache(this);
                primaryKey.setValue(userName);
                rc.refreshSave(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    messageToDept = StrUtil.getNullStr(rs.getString(1));
                    messageToUserGroup = StrUtil.getNullStr(rs.getString(2));
                    messageToUserRole = StrUtil.getNullStr(rs.getString(3));
                    messageToMaxUser = rs.getInt(4);
                    messageUserMaxCount = rs.getInt(5);
                    msgWinPopup = rs.getInt(6)==1;
                    chatSoundPlay = rs.getInt(7)==1;
                    chatIconShow = rs.getInt(8)==1;
                    messageSoundPlay = rs.getInt(9)==1;
                    skinCode = StrUtil.getNullStr(rs.getString(10));
                    weatherCode = StrUtil.getNullStr(rs.getString(11));
                    clockCode = StrUtil.getNullStr(rs.getString(12));
                    calendarCode = StrUtil.getNullStr(rs.getString(13));
                    webedit = rs.getInt(14)==1;
                    msgSpaceAllowed = rs.getLong(15);
                    msgSpaceUsed = rs.getLong(16);
                    uiMode = rs.getInt(17);
                    wallpaper = StrUtil.getNullStr(rs.getString(18));
                    showSidebar = rs.getInt(19)==1;
                    mydesktopProp = StrUtil.getNullStr(rs.getString(20));
                    lastMsgNotifyTime = rs.getTimestamp(21);
                    
                    emailName = StrUtil.getNullStr(rs.getString(22));
                    emailPwd = StrUtil.getNullStr(rs.getString(23));
                    
                    msgChat = rs.getInt(24) == 1;
                    local = StrUtil.getNullStr(rs.getString(25));
					keyId = rs.getString(26);
					bindMobile = rs.getInt(27) == 1;
                    
					client = rs.getInt(28);
					token = StrUtil.getNullStr(rs.getString(29));
					
					myleaders = StrUtil.getNullStr(rs.getString(30));
					
					menuMode = rs.getInt(31);
                    
                    loaded = true;
                    primaryKey.setValue(userName);
                }
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setString(1, userName);
            re = conn.executePreUpdate()==1?true:false;

            if (re) {
                re = conn.executePreUpdate() >= 0 ? true : false;
                UserSetupCache rc = new UserSetupCache(this);
                rc.refreshDel(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean modifySkin(){
    	 Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement pstmt = conn.prepareStatement(QUERY_MODIFY);
             pstmt.setString(1, skinCode);
             pstmt.setString(2, userName);
             re = conn.executePreUpdate()==1?true:false;

             if (re) {
            	 UserSetupCache rc = new UserSetupCache(this);
                 primaryKey.setValue(userName);
                 rc.refreshSave(primaryKey);
                 return true;
             }
         } catch (SQLException e) {
             logger.error("modifySkin:" + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
         return re;
    }
    
    public void setWeatherCode(String weatherCode) {
		this.weatherCode = weatherCode;
	}

	public String getWeatherCode() {
		return weatherCode;
	}

	private String userName;
	private String messageToDept;
	private String messageToUserGroup;
	private String messageToUserRole;
	private int messageToMaxUser;
	private int messageUserMaxCount;
	private boolean msgWinPopup = true;
	private boolean chatSoundPlay = true;
	private boolean chatIconShow = false;
	private boolean messageSoundPlay = true;
	private String skinCode;
	private String keyId;

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public String getClockCode() {
		return clockCode;
	}

	public void setClockCode(String clockCode) {
		this.clockCode = clockCode;
	}

	public String getCalendarCode() {
		return calendarCode;
	}

	public void setCalendarCode(String calendarCode) {
		this.calendarCode = calendarCode;
	}

	private String weatherCode;
    public boolean isWebedit() {
		return webedit;
	}

	public void setWebedit(boolean webedit) {
		this.webedit = webedit;
	}

	private String clockCode;
    private String calendarCode;
    private boolean webedit = false;
    private long msgSpaceAllowed = 0;
    private long msgSpaceUsed = 0;

	public long getMsgSpaceAllowed() {
		return msgSpaceAllowed;
	}

	public void setMsgSpaceAllowed(long msgSpaceAllowed) {
		this.msgSpaceAllowed = msgSpaceAllowed;
	}

	public long getMsgSpaceUsed() {
		return msgSpaceUsed;
	}

	public void setMsgSpaceUsed(long msgSpaceUsed) {
		this.msgSpaceUsed = msgSpaceUsed;
	}
	
    /**
     * 获取邮箱空间配额
     * @return long
     */
    public long getMsgSpaceQuota() {
        // 根据角色得到允许的磁盘空间
    	UserDb user = new UserDb();
    	user = user.getUserDb(userName);
        RoleDb[] rd = user.getRoles();

        long dq = getMsgSpaceAllowed();
        // System.out.println(getClass() + " dq=" + dq);
        
        for (int i = 0; i < rd.length; i++) {
            // System.out.println(getClass() + " " + rd[i].getCode() + " " + rd[i].getMsgSpaceQuota());
            if (rd[i].getMsgSpaceQuota() > dq) {
                dq = rd[i].getMsgSpaceQuota();
            }
        }
        return dq;
    }

	public void setWallpaper(String wallpaper) {
		this.wallpaper = wallpaper;
	}

	public String getWallpaper() {
		return wallpaper;
	}
 
	public void setMydesktopProp(String mydesktopProp) {
		this.mydesktopProp = mydesktopProp;
	}
  
	public String getMydesktopProp() {
		return mydesktopProp;
	}

	public void setLastMsgNotifyTime(java.util.Date lastMsgNotifyTime) {
		this.lastMsgNotifyTime = lastMsgNotifyTime;
	}

	public java.util.Date getLastMsgNotifyTime() {
		return lastMsgNotifyTime;
	}

	public void setEmailName(String emailName) {
		this.emailName = emailName;
	}

	public String getEmailName() {
		return emailName;
	}

	public void setEmailPwd(String emailPwd) {
		this.emailPwd = emailPwd;
	}

	public String getEmailPwd() {
		return emailPwd;
	}

	public boolean isMsgChat() {
		return msgChat;
	}

	public void setMsgChat(boolean msgChat) {
		this.msgChat = msgChat;
	}


	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}	
	
	public Vector getMySubordinates() {
		String sql = "select user_name from user_setup where myleaders like " + StrUtil.sqlstr("%" + userName + "%");
		return list(sql);
	}
}
