package com.redmoon.oa.platform;

import java.util.List;
/**
 * 调用zte接口获取角色
 * @author Administrator
 *
 */
public class ZTEPlatform implements IPlatformRelated{

	/**
	 * 自定义角色，和云端无关
	 */
	@Override
	public List<String> getRoles(String name) {
		
		return null;
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getEcid() {
		// TODO Auto-generated method stub
		return "";
	}

}
