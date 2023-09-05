package com.redmoon.oa.tag;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.base.QObjectDb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForEachQObject extends SimpleTagSupport {
	private Object items;
	private String var;
	
	private String varStatus;

	public Object getItems() {
		return items;
	}

	public void setItems(Object items) {
		this.items = items;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	@Override
	public void doTag() throws JspException, IOException {
		Vector v = (Vector) items;

		JspFragment jf = this.getJspBody();// 获取jsp文件中的内容
	    StringWriter sw = new StringWriter();
	    jf.invoke(sw);
	    String strBody = sw.toString(); // 把jsp内容转成字符串
	    
	    int index = 0;
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			Object obj = ir.next();
			
			// this.getJspContext().setAttribute(var, obj);

		    String str = filter(obj, strBody, index);// 获取进行转义之后的字符
		    this.getJspContext().getOut().write(str);//写入浏览器
			
		    index++;
			// getJspBody().invoke(null);
		}
	}
	
	public String filter(Object obj, String str, int index) {
		QObjectDb qo = (QObjectDb)obj;
		
		Pattern p = Pattern.compile(
				"\\$\\{" + var + ".([A-Z0-9a-z-_]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String fieldName = m.group(1);
			String val = StrUtil.getNullStr(String.valueOf(qo.get(fieldName)));
			m.appendReplacement(sb, val);
		}

		m.appendTail(sb);

		str = sb.toString();		
		
		p = Pattern.compile(
				"\\$\\{" + varStatus + ".([A-Z0-9a-z-_]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		sb = new StringBuffer();
		while (m.find()) {
			String fieldName = m.group(1);
			if ("index".equals(fieldName)) {
				m.appendReplacement(sb, String.valueOf(index));
			}
			else {
				m.appendReplacement(sb, "");
			}
		}		
		m.appendTail(sb);
		
		str = sb.toString();
		
		HttpServletRequest request=(HttpServletRequest) ((PageContext)this.getJspContext()).getRequest();

		// 解析其它EL表达式
		p = Pattern.compile(
				"\\$\\{([A-Z0-9a-z-_]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		sb = new StringBuffer();
		while (m.find()) {
			String fieldName = m.group(1);
			String val = (String)request.getAttribute(fieldName);
			m.appendReplacement(sb, StrUtil.getNullStr(val));
		}		
		m.appendTail(sb);
		
		str = sb.toString();
		
		// 解析三目表达式 ${item.is_bold == 1?"是":"否" }
		p = Pattern.compile(				
				"\\$\\{" + var + ".([A-Z0-9a-z-_]+) *?== *?(.*?)\\?(.*?):(.*?)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = p.matcher(str);
		sb = new StringBuffer();
		while (m.find()) {
			String fieldName = m.group(1);
			String v = m.group(2).trim();
			String a = m.group(3).trim();
			String b = m.group(4).trim();
			if (a.startsWith("\"") && a.endsWith("\"")) {
				a = a.substring(1, a.length()-1);
			}
			if (b.startsWith("\"") && b.endsWith("\"")) {
				b = b.substring(1, b.length()-1);
			}			
			String val = String.valueOf(qo.get(fieldName));
			if (v.equals(val)) {
				val = a;
			}
			else {
				val = b;
			}
			m.appendReplacement(sb, StrUtil.getNullStr(val));
		}		
		m.appendTail(sb);		
		return sb.toString();
	}

	public void setVarStatus(String varStatus) {
		this.varStatus = varStatus;
	}

	public String getVarStatus() {
		return varStatus;
	}
}
