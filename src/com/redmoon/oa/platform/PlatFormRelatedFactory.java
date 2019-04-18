package com.redmoon.oa.platform;

import cn.js.fan.web.Global;

public class PlatFormRelatedFactory {
	 	public static final String PLATFORM_RELATED_ZTE = "ZTE";
	 	private static Object initLock = new Object();
	   

	    public static IPlatformRelated igur = null;

	    public PlatFormRelatedFactory() {
	        super();
	    }

	    public static IPlatformRelated getPlatformGenerator() {
	        if (igur==null) {
	            synchronized (initLock) {
	                if (Global.platformRelated.equalsIgnoreCase(Global.PLATFORM_RELATED_OA)) {
	                	igur = new OAPlatform();
	                }
	                else if (Global.platformRelated.equalsIgnoreCase(PLATFORM_RELATED_ZTE)) {
	                	igur = new ZTEPlatform();
	                }
	                else{
	                	igur = new OAPlatform();
	                }
	              
	            }
	        }
	        return igur;
	    }
}
