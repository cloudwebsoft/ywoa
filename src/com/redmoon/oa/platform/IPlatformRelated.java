package com.redmoon.oa.platform;

import java.util.List;
/**
 * 获取用户角色接口,可根据配置调用不同的获取方式
 * @author Administrator
 *
 */
public interface IPlatformRelated {
	/**
	 * 获取角色信息
	 * @param name
	 * @return
	 */
	public List<String> getRoles(String name);
	/**
	 * 获取文件路径
	 * @return
	 */
	public String getFilePath();
	/**
	 * 获取企业ID
	 */
	public String getEcid();
}
