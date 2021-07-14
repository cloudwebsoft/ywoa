package com.redmoon.oa.netdisk;

import org.apache.log4j.Logger;

import com.cloudwebsoft.framework.base.QObjectDb;

public class SideBarDb extends QObjectDb {
	public final static int PICTURE_FIRST = 1;
	public final static int PICTURE_SECOND = 2;
	public final static int PICTURE_THIRD = 3;
	public final static int PICTURE_FOURTH = 4;
	public final static int PICTURE_FIFTH = 5;
	public final static int PICTURE_SIXTH = 6;
	public final static int PICTURE_SEVENTH = 7;
	public final static int PICTURE_EIGHTH = 8;
	public final static int PICTURE_NINTH = 9;
	public final static int PICTURE_TENTH = 10;
	public final static int PICTURE_ELEVENTH = 11;
	public final static int PICTURE_TWELFTH = 12;
	public final static int UP_NOTICE = 0;
	public final static int UP_BUTTON = 999;
	public final static int NOTICE_TOPIC = 666;
	public final static int IS_SHOW = 1;
	public final static int NOT_SHOW = 0;
	public final static int NOT_CUSTOM = 0;
	public final static int IS_CUSTOM = 1;
	public final static int TYPE_NOTICE = 0;
	public final static int TYPE_PICTURE = 1;
	public final static int TYPE_NOTICE_TOPIC = 2;
	public final static int MOD_FLAG_INIT = 0;

	transient Logger logger = Logger.getLogger(SideBarDb.class.getName());

	public SideBarDb() {
		super();
	}

	public SideBarDb getSideBarDb(long id) {
		SideBarDb sbDb = (SideBarDb) getQObjectDb(new Long(id));
		sbDb.setId(id);
		sbDb.setUser_name(sbDb.getString("user_name"));
		sbDb.setHref(sbDb.getString("href"));
		sbDb.setIs_show(sbDb.getInt("is_show"));
		sbDb.setPosition(sbDb.getInt("position"));
		sbDb.setTitle(sbDb.getString("title"));
		sbDb.setPicture(sbDb.getString("picture"));
		sbDb.setType(sbDb.getInt("type"));
		sbDb.setCustom(sbDb.getInt("custom"));
		return sbDb;
	}

	private long id;
	private String user_name;
	private int is_show;
	private int position;
	private String title;
	private String picture;
	private String href;
	private int type;
	private int custom;
	private String authKey;
	private int mod_flag;

	public int getMod_flag() {
		return mod_flag;
	}

	public void setMod_flag(int modFlag) {
		mod_flag = modFlag;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String userName) {
		user_name = userName;
	}

	public int getIs_show() {
		return is_show;
	}

	public void setIs_show(int isShow) {
		is_show = isShow;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCustom() {
		return custom;
	}

	public void setCustom(int custom) {
		this.custom = custom;
	}

}