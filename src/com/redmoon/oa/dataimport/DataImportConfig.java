package com.redmoon.oa.dataimport;

import java.io.FileInputStream;
import java.net.URL;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.apache.log4j.Logger;

import com.redmoon.oa.dataimport.bean.DataImportBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;

public class DataImportConfig {
	static Logger logger;
	public final String FILENAME = "config_data_import.xml";

	public static Document doc = null;
	public static Element root = null;
	public static String xmlPath;
	public static boolean isInited = false;
	public static URL confURL;

	public DataImportConfig() {
		logger = Logger.getLogger(this.getClass().getName());
		confURL = getClass().getClassLoader().getResource(FILENAME);
	}

	@SuppressWarnings("deprecation")
	public static void init() {
		if (!isInited) {
			xmlPath = confURL.getPath();
			xmlPath = URLDecoder.decode(xmlPath);

			SAXBuilder sb = new SAXBuilder();
			try {
				FileInputStream fin = new FileInputStream(xmlPath);
				doc = sb.build(fin);
				root = doc.getRootElement();
				fin.close();
				isInited = true;
			} catch (org.jdom.JDOMException e) {
				logger.error(e.getMessage());
			} catch (java.io.IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public Element getRootElement() {
		return root;
	}

	public DataImportMgr getDataImportMgr(String code) {
		DataImportMgr dim = null;
		init();
		List list = root.getChildren();
		if (list != null) {
			Iterator ir = list.iterator();
			while (ir.hasNext()) {
				Element child = (Element) ir.next();
				String ecode = child.getAttributeValue("code");
				if (ecode.equals(code)) {
					DataImportBean dib = new DataImportBean();
					dib.setCode(code);
					dib.setName(child.getChildText("name"));
					dib.setAuthor(child.getChildText("author"));
					dib.setClassName(child.getChildText("className"));
					dib.setTable(child.getChildText("table"));
					String fields = StrUtil.getNullStr(child
							.getChildText("fields"));
					String fieldsName = StrUtil.getNullStr(child
							.getChildText("fieldsName"));
					String splitFields = StrUtil.getNullStr(child
							.getChildText("splitFields"));
					String notEmptyFields = StrUtil.getNullStr(child
							.getChildText("notEmptyFields"));
					String uniqueFields = StrUtil.getNullStr(child
							.getChildText("uniqueFields"));
					String floatFields = StrUtil.getNullStr(child
							.getChildText("floatFields"));
					String integerFields = StrUtil.getNullStr(child
							.getChildText("integerFields"));
					String unitFields = StrUtil.getNullStr(child
							.getChildText("unitCodeFields"));
					String hint = StrUtil
							.getNullStr(child.getChildText("hint"));
					String returnURL = StrUtil.getNullStr(child
							.getChildText("returnURL"));
					HashMap<String, String> rangeFields = new HashMap<String, String>();
					Element e = child.getChild("ranges");
					if (e != null) {
						Iterator ir1 = e.getChildren().iterator();
						while (ir1.hasNext()) {
							Element s = ((Element) ir1.next());
							String c = s.getAttributeValue("code");
							String t = s.getText();
							rangeFields.put(c, t);
						}
					}

					String[] fieldAry = fields.split(",");
					String[] fieldNameAry = fieldsName.split(",");

					if (fieldAry.length != fieldNameAry.length) {
						return null;
					}

					HashMap<String, String> splitmap = new HashMap<String, String>();
					String[] fieldSplitAry = splitFields.split(",");
					for (String field : fieldSplitAry) {
						splitmap.put(field, "");
					}

					ArrayList<String> fieldList = new ArrayList<String>();
					ArrayList<String> fieldNameList = new ArrayList<String>();
					for (int i = 0; i < fieldAry.length; i++) {
						fieldList.add(fieldAry[i]);
						fieldNameList.add(fieldNameAry[i]);
						if (splitmap.containsKey(fieldAry[i])) {
							splitmap.put(fieldAry[i], fieldNameAry[i]);
						}
					}

					ArrayList<String> notEmptyFieldList = new ArrayList<String>();
					String[] notEmptyfieldsAry = notEmptyFields.split(",");
					for (String str : notEmptyfieldsAry) {
						notEmptyFieldList.add(str);
					}

					ArrayList<String> uniqueFieldList = new ArrayList<String>();
					String[] uniquefieldsAry = uniqueFields.split(",");
					for (String str : uniquefieldsAry) {
						uniqueFieldList.add(str);
					}

					ArrayList<String> floatFieldList = new ArrayList<String>();
					String[] floatfieldsAry = floatFields.split(",");
					for (String str : floatfieldsAry) {
						floatFieldList.add(str);
					}

					ArrayList<String> integerFieldList = new ArrayList<String>();
					String[] integerfieldsAry = integerFields.split(",");
					for (String str : integerfieldsAry) {
						integerFieldList.add(str);
					}

					ArrayList<String> unitFieldList = new ArrayList<String>();
					String[] unitfieldsAry = unitFields.split(",");
					for (String str : unitfieldsAry) {
						unitFieldList.add(str);
					}

					dib.setFields(fieldList);
					dib.setFieldsName(fieldNameList);
					dib.setSplitFields(splitmap);
					dib.setNotEmptyFields(notEmptyFieldList);
					dib.setUniqueFields(uniqueFieldList);
					dib.setFloatFields(floatFieldList);
					dib.setIntegerFields(integerFieldList);
					dib.setUnitFields(unitFieldList);
					dib.setRangeFields(rangeFields);
					dib.setHint(hint);
					dib.setReturnURL(returnURL);

					dim = new DataImportMgr(dib);
					return dim;
				}
			}
		}

		return dim;
	}
}
