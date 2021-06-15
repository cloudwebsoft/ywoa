package com.redmoon.oa.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.*;

import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

public class CSSUtil {

	private CSSStyleSheet sheet = null;
	
	public CSSStyleSheet getSheet() {
		return sheet;
	}

	public void setSheet(CSSStyleSheet sheet) {
		this.sheet = sheet;
	}

	/**
	 * 指定文件流
	 * @param stream
	 */
	public CSSUtil(InputStream stream){
		InputSource source = new InputSource(new InputStreamReader(stream));
		
		CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
		try {
			 sheet = parser.parseStyleSheet(source, null, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 获取样式信息
	 * @param className 样式名
	 * @return 返回该样式的信息
	 */
	public String getClass(String className){
		if(sheet == null){
			return null;
		}
		String res = "";
		CSSRuleList rules = sheet.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
		    CSSRule rule = rules.item(i);
		    Pattern pattern = Pattern.compile("\\."+className + ".*([^\\{]\\{.*[^\\}]\\})");
			Matcher matcher = pattern.matcher(rule.getCssText());
			while(matcher.find()){
				res = res + matcher.group(1) + "\n";
			}
		}
		return res;
	}
	
	public static ArrayList getFlexigridBtn() {
		ArrayList<String[]> al = new ArrayList<String[]>();

		String appRealPath = CSSUtil.class.getResource("/").getPath();
		int k = appRealPath.indexOf("WEB-INF");
		appRealPath = appRealPath.substring(0, k);

		String URIPath;
		String os = System.getProperty("os.name");
		if(os.toLowerCase().startsWith("win")) {
			// windows上appRealPath如下：/D:/oa/target/oa-0.0.1-SNAPSHOT/，故去掉第1个/
			if (appRealPath.startsWith("/")) {
				appRealPath = appRealPath.substring(1);
			}
			URIPath = "file:///" + appRealPath + "skin/lte/flexigrid/flexigrid.css";
		}
		else {
			URIPath = appRealPath + "skin/lte/flexigrid/flexigrid.css";
		}

		// String URIPath = "file:///" + Global.getRealPath() + "skin/bluethink/flexigrid/flexigrid.css";
		CSSOMParser cssparser = new CSSOMParser();
		CSSStyleSheet css = null;
		try {
			css = cssparser.parseStyleSheet(new InputSource(URIPath), null, null);
		} catch (IOException e) {
			System.out.println("解析css文件异常:" + e);
			return al;
		}
		/*
		 * .flexigrid div.fbutton .import1 {
		 * background: url(images/import.png) no-repeat center left;
		 * }
		 */
		
		if (css != null) {
			CSSRuleList cssrules = css.getCssRules();
			for (int i = 0; i < cssrules.getLength(); i++) {
				CSSRule rule = cssrules.item(i);
				if (rule instanceof CSSStyleRule) {
					CSSStyleRule cssrule = (CSSStyleRule) rule;
					String selector = cssrule.getSelectorText();
					if (selector.indexOf("div.fbutton .")!=-1) {
						int m = selector.indexOf(" .");
						selector = selector.substring(m+2).trim();
						
						String[] ary = new String[2];
						ary[0] = selector;
						CSSStyleDeclaration styles = cssrule.getStyle();
						for (int j = 0, n = styles.getLength(); j < n; j++) {
							if (styles.item(j).equalsIgnoreCase("background")) {
								String prop = styles.getPropertyValue(styles.item(j));
								int p = prop.indexOf("(");
								int q = prop.indexOf(")");
								if (p!=-1 && q!=-1) {
									ary[1] = prop.substring(p + 1, q);
								}
								break;
							}
						}
						al.add(ary);
					}
				}
			}
		}		
		return al;
	}

	public static ArrayList getFontBefores() {
		ArrayList<String> al = new ArrayList<String>();
		String path = Global.getRealPath() + "lte/css/font-awesome.min.css";
		String URIPath = "file:///" + path;
		CSSOMParser cssparser = new CSSOMParser();
		CSSStyleSheet css = null;
		try {
			css = cssparser.parseStyleSheet(new InputSource(URIPath), null, null);
		} catch (IOException e) {
			System.out.println("解析css文件异常:" + e);
			return al;
		}
		/*
		.fa-home:before {
		  content: "\f015";
		}
		*/
		String cont = "";
		try {
			cont = FileUtil.ReadFile(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (css != null) {
			CSSRuleList cssrules = css.getCssRules();
			for (int i = 0; i < cssrules.getLength(); i++) {
				CSSRule rule = cssrules.item(i);
				if (rule instanceof CSSStyleRule) {
					CSSStyleRule cssrule = (CSSStyleRule) rule;
					String selector = cssrule.getSelectorText();
					if (selector.indexOf(".fa-")==0) {
						int m = selector.indexOf(":");
						if (m==-1) {
							continue;
						}
						selector = selector.substring(1, m).trim();
						al.add(selector);
					}
				}
			}
		}
		return al;
	}

	public static ArrayList getFontBefore() {
		ArrayList<String[]> al = new ArrayList<String[]>();
		String path = Global.getRealPath() + "lte/css/font-awesome.min.css";
		String URIPath = "file:///" + path;
		CSSOMParser cssparser = new CSSOMParser();
		CSSStyleSheet css = null;
		try {
			css = cssparser.parseStyleSheet(new InputSource(URIPath), null, null);
		} catch (IOException e) {
			System.out.println("解析css文件异常:" + e);
			return al;
		}
		/*
		.fa-home:before {
		  content: "\f015";
		}
		*/
		String cont = "";
		try {
			cont = FileUtil.ReadFile(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (css != null) {
			CSSRuleList cssrules = css.getCssRules();
			for (int i = 0; i < cssrules.getLength(); i++) {
				CSSRule rule = cssrules.item(i);
				if (rule instanceof CSSStyleRule) {
					CSSStyleRule cssrule = (CSSStyleRule) rule;
					String selector = cssrule.getSelectorText();
					if (selector.indexOf(".fa-")==0) {
						int m = selector.indexOf(":");
						if (m==-1) {
							continue;
						}
						selector = selector.substring(1, m).trim();
						String[] ary = new String[2];
						ary[0] = selector;
						CSSStyleDeclaration styles = cssrule.getStyle();
						for (int j = 0, n = styles.getLength(); j < n; j++) {
							if (styles.item(j).equalsIgnoreCase("content")) {
								// 无法读取content的内容，读出的为空
								// String prop = styles.getPropertyValue(styles.item(j));
								int p = cont.indexOf(selector);
								p = cont.indexOf("{", p);
								p = cont.indexOf(":", p+1);
								if (p==-1) {
									continue;
								}
								int q = cont.indexOf("}", p+1);
								String prop = cont.substring(p+1, q);
								// System.out.println(CSSUtil.class + " prop=" + prop);
								ary[1] = prop;
								break;
							}
						}
						al.add(ary);
					}
				}
			}
		}		
		return al;
	}	
	
	/**
	 * 获取某个样式类下的某个属性信息，没有返回空
	 * @param className 样式类名称
	 * @param attrName 属性名称
	 * @return
	 */
	public String getCssAttribute(String className, String attrName){
		String classContent = this.getClass(className);
		Pattern pattern = Pattern.compile("[^a-zA-Z\\-]" + attrName + ":\\s*([^;\\}]{1,100})\\s*[;\\}]{1}");
		Matcher matcher = pattern.matcher(classContent);
		String res = "";
		while(matcher.find()){
			res = matcher.group(1);
		}
		return res;
	}

	public static void main(String[] args) {
		String URIPath = "file:///" + "d:/oa/WebRoot/skin/bluethink/flexigrid/flexigrid.css";
		CSSOMParser cssparser = new CSSOMParser();
		CSSStyleSheet css = null;
		try {
			css = cssparser.parseStyleSheet(new InputSource(URIPath), null,
					null);
		} catch (IOException e) {
			System.out.println("解析css文件异常:" + e);
		}
		if (css != null) {
			CSSRuleList cssrules = css.getCssRules();
			for (int i = 0; i < cssrules.getLength(); i++) {
				CSSRule rule = cssrules.item(i);
				if (rule instanceof CSSStyleRule) {
					CSSStyleRule cssrule = (CSSStyleRule) rule;
					// System.out.println("cssrule.getCssText:" + cssrule.getCssText());
					// System.out.println("cssrule.getSelectorText:" + cssrule.getSelectorText());
					
					String selector = cssrule.getSelectorText();
					if (selector.indexOf("div.fbutton")!=-1) {
						System.out.println("selector:" + selector);
						
						CSSStyleDeclaration styles = cssrule.getStyle();
						for (int j = 0, n = styles.getLength(); j < n; j++) {
							System.out.println(styles.item(j) + ":"
									+ styles.getPropertyValue(styles.item(j)));
						}
					}

				} else if (rule instanceof CSSImportRule) {
					CSSImportRule cssrule = (CSSImportRule) rule;
					System.out.println(cssrule.getHref());
				}
			}
		}

	}

	
}
