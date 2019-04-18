package com.redmoon.oa.util;

import java.util.List;

import org.jdom.Element;

import cn.js.fan.util.XMLConfig;
import cn.js.fan.web.Config;

import com.cloudwebsoft.framework.util.LogUtil;
/**
 * 修改文件路径配置
 * @author jfy
 * @date Jan 8, 2015
 */
public class ModifyFilePath {
	/**
	 * 修改文件配置路径 config_cws.xml、reportConfig.xml
	 * @param path
	 * @return
	 */
	public boolean modifyFilePath(String path){
		boolean flag = false;
		try{
			Config cwsCfg = new Config();
			cwsCfg.setProperty("Application.realPath", path);
			modifyReportConfig(path);
			flag = true;
		}
		catch(Exception ex){
			LogUtil.getLog(getClass()).error("modify file path error :" + ex.getMessage());
		}
		
		return flag;
	}
	/**
	 * 修改reportConfig.xml中文件路径配置
	 * @param path
	 */
	@SuppressWarnings("unchecked")
	private void modifyReportConfig(String path){
		XMLConfig cfg = new XMLConfig("../reportConfig.xml",false,"utf-8");
    	Element cRoot = cfg.getRoot();
    	List<Element> list = cRoot.getChildren();
    	for (int i =0; i < list.size();i++){
    		Element el = (Element)list.get(i);
    		if (el.getChildText("name").equals("reportFileHome")){
    			System.out.println( el.getChild("value").getText());
    			el.getChild("value").setText(path);
    		}
    		
    	}
	}
}
