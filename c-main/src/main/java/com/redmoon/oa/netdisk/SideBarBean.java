package com.redmoon.oa.netdisk;

public class SideBarBean {
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
	public SideBarBean(){
		this.user_name = "";
		this.is_show=0;
		this.position= 0;
		this.title= "";
		this.picture= "";
		this.href= "";
		this.type= 0;
		this.custom= 0;
		this.mod_flag=0;
	}
	
	public SideBarBean(long id, String userName, int isShow,
			int position, String title, String picture, String href,int type,int custom,String authKey,int mod_flag) {
		super();
		this.id = id;
		this.user_name = userName;
		this.is_show = isShow;
		this.position = position;
		this.title = title;
		this.picture = picture;
		this.href = href;
		this.type = type;
		this.custom = custom;
		this.authKey = authKey;
		this.mod_flag = mod_flag;
	}
}
