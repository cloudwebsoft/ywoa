package com.redmoon.oa.person;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.DateUtil;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.SpringUtil;

import java.util.List;
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

    @Override
	public void initDB() {
        tableName = "user_setup";
		isInitFromConfigDB = false;
    }

    @Override
	public ObjectDb getObjectRaw(PrimaryKey pk) {
		return null;
    }


	public UserSetupDb getFromUserSetup(UserSetup userSetup, UserSetupDb usd) {
		if (userSetup == null) {
			return usd;
		}

		usd.setUserName(userSetup.getUserName());
		usd.setMessageToDept(userSetup.getMessageToDept());
		usd.setMessageToUserGroup(userSetup.getMessageToUsergroup());
		usd.setMessageToUserRole(userSetup.getMessageToUserrole());
		usd.setMessageToMaxUser(userSetup.getMessageToMaxUser());
		usd.setMessageUserMaxCount(userSetup.getMessageUserMaxCount());
		usd.setMsgWinPopup(userSetup.getIsMsgWinPopup());
		usd.setChatSoundPlay(userSetup.getIsChatSoundPlay());
		usd.setChatIconShow(userSetup.getIsChatIconShow());
		usd.setMessageSoundPlay(userSetup.getIsMessageSoundPlay()==1);
		usd.setSkinCode(userSetup.getSkinCode());
		usd.setWeatherCode(userSetup.getWeatherCode());
		usd.setClockCode(userSetup.getClockCode());
		usd.setCalendarCode(userSetup.getCalendarCode());
		usd.setWebedit(userSetup.getIsWebedit()==1);
		usd.setMsgSpaceAllowed(userSetup.getMsgSpaceAllowed());
		usd.setMsgSpaceUsed(userSetup.getMsgSpaceUsed());
		usd.setUiMode(userSetup.getUiMode());
		usd.setWallpaper(userSetup.getWallpaper());
		usd.setShowSidebar(userSetup.getIsShowSidebar()==1);
		usd.setMydesktopProp(userSetup.getMydesktopProp());
		usd.setLastMsgNotifyTime(DateUtil.asDate(userSetup.getLastMsgNotifyTime()));
		usd.setEmailName(userSetup.getEmailName());
		usd.setEmailPwd(userSetup.getEmailPwd());
		usd.setMsgChat("1".equals(userSetup.getIsMsgChat()));
		usd.setLocal(userSetup.getLocal());
		usd.setKeyId(userSetup.getKeyId());
		usd.setBindMobile(userSetup.getIsBindMobile()==1);
		usd.setClient(userSetup.getClient());
		usd.setToken(userSetup.getToken());
		usd.setMyleaders(userSetup.getMyleaders());
		usd.setMenuMode(userSetup.getMenuMode());
		usd.setLoaded(true);
		return usd;
	}

    public UserSetupDb getUserSetupDb(String name) {
		this.userName = name;
		com.cloudweb.oa.cache.UserSetupCache userSetupCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserSetupCache.class);
		UserSetup userSetup = userSetupCache.getUserSetup(name);
		return getFromUserSetup(userSetup, new UserSetupDb());
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

    @Override
    public boolean create() {
        return false;
    }

    @Override
    public boolean save() {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = new UserSetup();
        userSetup.setMessageToDept(messageToDept);
        userSetup.setMessageToUsergroup(messageToUserGroup);
        userSetup.setMessageToUserrole(messageToUserRole);
        userSetup.setMessageToMaxUser(messageToMaxUser);
        userSetup.setMessageUserMaxCount(messageUserMaxCount);
        userSetup.setIsMsgWinPopup(msgWinPopup);
        userSetup.setIsChatSoundPlay(chatSoundPlay);
        userSetup.setIsChatIconShow(chatIconShow);
        userSetup.setIsMessageSoundPlay(messageSoundPlay?1:0);
        userSetup.setSkinCode(skinCode);
        userSetup.setWeatherCode(weatherCode);
        userSetup.setClockCode(clockCode);
        userSetup.setCalendarCode(calendarCode);
        userSetup.setIsWebedit(webedit?1:0);
        userSetup.setMsgSpaceAllowed(msgSpaceAllowed);
        userSetup.setMsgSpaceUsed(msgSpaceUsed);
        userSetup.setUiMode(uiMode);
        userSetup.setWallpaper(wallpaper);
        userSetup.setIsShowSidebar(showSidebar?1:0);
        userSetup.setMydesktopProp(mydesktopProp);
        userSetup.setLastMsgNotifyTime(DateUtil.toLocalDateTime(lastMsgNotifyTime));
        userSetup.setEmailName(emailName);
        userSetup.setEmailPwd(emailPwd);
        userSetup.setIsMsgChat(msgChat?"1":"");
        userSetup.setLocal(local);
        userSetup.setKeyId(keyId);
        userSetup.setIsBindMobile(bindMobile?1:0);
        userSetup.setClient(client);
        userSetup.setToken(token);
        userSetup.setMyleaders(myleaders);
        userSetup.setMenuMode(menuMode);
        userSetup.setUserName(userName);
        return userSetupService.updateByUserName(userSetup);
    }

    @Override
    public void load() {
        com.cloudweb.oa.cache.UserSetupCache userSetupCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserSetupCache.class);
        UserSetup userSetup = userSetupCache.getUserSetup(userName);
        getFromUserSetup(userSetup, new UserSetupDb());
    }

    @Override
    public boolean del() {
        return true;
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
	
	public Vector<UserSetupDb> getMySubordinates() {
		IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
		List<String> list = userSetupService.getMySubordinates(userName);
		Vector<UserSetupDb> v = new Vector<>();
		for (String name : list) {
			v.addElement(getUserSetupDb(name));
		}
		return v;
	}
}
