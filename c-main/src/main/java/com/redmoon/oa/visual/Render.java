package com.redmoon.oa.visual;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IModuleRender;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.shell.BSHShell;
import com.redmoon.oa.util.BeanShellUtil;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.*;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Render {
    HttpServletRequest request;
    FormDb fd;
    long visualObjId;
    ModuleSetupDb msd;

    public static final String FORM_FLEMENT_ID = "visualForm";
    
    /**
     * 不可写字段
     */
    Vector<FormField> vdisable;
    ModulePrivDb mpd;

    public Render(HttpServletRequest request, FormDb fd) {
        this.request = request;
        this.fd = fd;
    }

    public Render(HttpServletRequest request, long visualObjId, FormDb fd) {
        this.request = request;
        this.fd = fd;
        this.visualObjId = visualObjId;
    }
    
    public String getContentMacroReplaced(FormDAO fdao, String content, Vector<FormField> fields) {
		IModuleRender moduleRender = (IModuleRender) SpringUtil.getBean("moduleRender");
		// 当实参为null时，其实，它依然是一个值传递，同基本类型，所以此处如果不new，即便在moduleRender.getContentMacroReplaced进行了new，在该方法返回时，vdisable依然为null
		vdisable = new Vector<>();
		return moduleRender.getContentMacroReplaced(msd, fdao, content, fields, vdisable);
    }

    public String rendForAdd() {
    	FormDAO fdao = null;
        String content = getContentMacroReplaced(fdao, fd.getContent(), fd.getFields());
        return rendForAdd(content, fd.getFields());
    }
    
    /**
     * 添加
     * @param msd
     * @return
     */
    public String rendForAdd(ModuleSetupDb msd) {
    	this.msd = msd;

    	String formCode = msd.getString("form_code");
    	
    	try {
			License.getInstance().checkSolution(formCode);
		} catch (ErrMsgException e) {
			return e.getMessage();
		}    	
    	
    	int viewEdit = msd.getInt("view_edit");
    	if (viewEdit==ModuleSetupDb.VIEW_DEFAULT || viewEdit==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
    		return rendForAdd();
    	}
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewEdit);
    	if (fvd==null) {
    		LogUtil.getLog(getClass()).error("视图ID=" + viewEdit + "不存在");
    		return rendForAdd();
    	}

        String form = fvd.getString("form");

        String ieVersion = fvd.getString("ie_version");
    	FormParser fp = new FormParser();
    	Vector<FormField> fields = fp.parseCtlFromView(fvd.getString("content"), ieVersion, fd);
        String content = getContentMacroReplaced(null, form, fields);
        
        return rendForAdd(content, fields);
    }
    
    public String rendForAdd(String content, Vector<FormField> fields) {
		String script = msd.getScript("preProcess");
		// LogUtil.getLog(getClass()).info("rendForAdd 模块预处理事件 formCode=" + fd.getCode() + " script=" + script);
		if (script != null) {
			BSHShell bs = new BSHShell();
			bs.set(ConstUtil.SCENE, ConstUtil.SCENE_MODULE_PRE_PROCESS);
			String pageType = ParamUtil.get(request, "pageType");
			bs.set("action", "add");
			bs.set("request", request);
			bs.set("moduleCode", msd.getCode());
			bs.set("formCode", fd.getCode());
			bs.set("fields", fields);
			bs.set("userName", SpringUtil.getUserName());
			bs.set("request", request);
			bs.set("pageType", pageType);
			bs.set("parentId", ParamUtil.getLong(request, "parentId", -1));
			bs.eval(script);
		}

		IModuleRender moduleRender = (IModuleRender) SpringUtil.getBean("moduleRender");
		return moduleRender.rendForAdd(msd, fd, content, fields);
    }

	public String[] rendForNestTable(String content, Vector<FormField> fields, String FORM_FLEMENT_ID, boolean isAdd, IFormDAO fdao) {
		IModuleRender moduleRender = (IModuleRender) SpringUtil.getBean("moduleRender");
		return moduleRender.rendForNestTable(fd, content, fields, FORM_FLEMENT_ID, isAdd, fdao);
	}

    /**
     * 用于一些老版中定制的页面
     * @return
     */
    public String rend() {
        return rend(FORM_FLEMENT_ID);
    }
    
    /**
     * 编辑时渲染
     * @param msd
     * @return
     */
    public String rend(ModuleSetupDb msd) {
    	this.msd = msd;
    	
    	String formCode = msd.getString("form_code");
    	
    	try {
			License.getInstance().checkSolution(formCode);
		} catch (ErrMsgException e) {
			return e.getMessage();
		}    	
    			
    	int viewEdit = msd.getInt("view_edit");
    	if (viewEdit==ModuleSetupDb.VIEW_DEFAULT || viewEdit==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
			return rend();
		}
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewEdit);
    	if (fvd==null) {
    		LogUtil.getLog(getClass()).error("视图ID=" + viewEdit + "不存在");
    	}
    	
        FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
        
        String cnt = fvd.getString("content");

        String ieVersion = fvd.getString("ie_version");
    	FormParser fp = new FormParser();
    	Vector<FormField> fields = fp.parseCtlFromView(cnt, ieVersion, fd);
    	
    	// 将fields改为已fdao中已取得值的FormField
    	Vector<FormField> v = new Vector<>();
		for (FormField ff : fields) {
			v.addElement(fdao.getFormField(ff.getName()));
		}
    	
    	fields = v;
       	
        // String content = doc.getContent(1); // 取得表单内容
        String content = getContentMacroReplaced(fdao, fvd.getString("form"), fields);
		return rend(fdao, FORM_FLEMENT_ID, content, fields);
    }
    
    public String rend(FormDAO fdao, String formElementId, String content, Vector<FormField> fields) {
		IModuleRender moduleRender = (IModuleRender) SpringUtil.getBean("moduleRender");
		return moduleRender.rend(msd, fdao, formElementId, content, fields, vdisable);
    }

    /**
     * 用于老版的页面，编辑时置表单中的各个输入框的值，置用户已操作的值，渲染显示表单，当用于显示嵌套表单时，需置formElementId为flowForm
     * @param formElementId String
     * @return String
     */
    public String rend(String formElementId) {
        FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
        
        Vector<FormField> fields = fdao.getFields();
        long t = System.currentTimeMillis();
        String content = getContentMacroReplaced(fdao, fd.getContent(), fields);
        if (Global.getInstance().isDebug()) {
			LogUtil.getLog(getClass()).info("Render getContentMacroReplaced " + (System.currentTimeMillis() - t) + " ms");
		}
        String r = rend(fdao, formElementId, content, fields);
		if (Global.getInstance().isDebug()) {
			LogUtil.getLog(getClass()).info("Render rend " + (System.currentTimeMillis() - t) + " ms");
		}
        return r;
    }

    /**
     * 为表单生成报表
     * @param isNest boolean 是否为嵌套表单
     * @return String
     */
    public String report(boolean isNest) {
		long t = System.currentTimeMillis();
		FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
		if (Global.getInstance().isDebug()) {
			LogUtil.getLog(getClass()).info("Render report after getFormDAO take " + (System.currentTimeMillis() - t) + " ms");
		}
        Vector<FormField> fields = fdao.getFields();

		String content = getContentMacroReplaced(fdao, fd.getContent(), fields);
		if (Global.getInstance().isDebug()) {
			LogUtil.getLog(getClass()).info("Render report after getContentMacroReplaced take " + (System.currentTimeMillis() - t) + " ms");
		}
		IModuleRender moduleRender = (IModuleRender) SpringUtil.getBean("moduleRender");
		String re = moduleRender.report(fdao, content, isNest);
		if (Global.getInstance().isDebug()) {
			LogUtil.getLog(getClass()).info("Render report after report take " + (System.currentTimeMillis() - t) + " ms");
		}
		return re;
    }

    public String report() {
        return report(false);
    }
    
    /**
     * 查看
     * @param msd
     * @return
     */
    public String report(ModuleSetupDb msd) {
    	String formCode = msd.getString("form_code");

    	try {
			License.getInstance().checkSolution(formCode);
		} catch (ErrMsgException e) {
			return e.getMessage();
		}   
		
    	int viewShow = msd.getInt("view_show");
    	if (viewShow==ModuleSetupDb.VIEW_DEFAULT || viewShow==ModuleSetupDb.VIEW_SHOW_CUSTOM) {
			return report();
		}
    	
    	FormViewDb fvd = new FormViewDb();
    	fvd = fvd.getFormViewDb(viewShow);
    	if (fvd==null) {
    		LogUtil.getLog(getClass()).error("视图ID=" + viewShow + "不存在");
    	}
    	
        FormDAOMgr fdm = new FormDAOMgr(fd);
        FormDAO fdao = fdm.getFormDAO(visualObjId);
        Vector<FormField> fields = fdao.getFields();
        
        String form = fvd.getString("form");

        String content = getContentMacroReplaced(fdao, form, fields);
        boolean isNest = false;

		IModuleRender moduleRender = (IModuleRender) SpringUtil.getBean("moduleRender");
		return moduleRender.report(fdao, content, isNest);
    }

	public String reportForArchive(IFormDAO fdao, String content) {
		IModuleRender moduleRender = (IModuleRender) SpringUtil.getBean("moduleRender");
		return moduleRender.reportForArchive(fdao, content);
	}

	/**
	 * 取得用来控制是否显示的脚本
	 * @param request HttpServletRequest
	 * @param fd FormDb
	 * @param fdao FormDAO
	 * @param userName String
	 * @return
	 */
	public static String applyShowRule(HttpServletRequest request, FormViewDb formViewDb, FormDb fd, IFormDAO fdao, String userName) {
		String formContent = formViewDb.getString("form");

		String showRule = formViewDb.getString("show_rule");
		if (showRule==null || "".equals(showRule)) {
			return formContent;
		}

		try {
			Privilege pvg = new Privilege();
			List<String> filedList = new ArrayList<>();
			Iterator<FormField> ir1 = fd.getFields().iterator();
			while (ir1.hasNext()) {
				FormField ff = (FormField) ir1.next();
				filedList.add(ff.getName());
			}

			SAXBuilder parser = new SAXBuilder();
			org.jdom.Document doc = parser.build(new InputSource(new StringReader(showRule)));
			Element root = doc.getRootElement();
			Iterator ir2 = root.getChild("views").getChildren().iterator();
			while (ir2.hasNext()) {
				Element el = (Element)ir2.next();

				boolean formFlag = true;

				String fieldName = el.getChildText("fieldName");
				if (fieldName==null) {
					// 可能为空的<view/>
					continue;
				}
				String token = el.getChildText("operator");

				token = token.replaceAll("&lt;", "<");
				token = token.replaceAll("&gt;", ">");
				if ("=".equals(token)) {
					token = "==";
				}
				else if ("<>".equals(token)) {
					token = "!=";
				}

				String val = el.getChildText("value");

				formFlag = filedList.contains(fieldName);
				if (!formFlag && !"cws_id".equals(fieldName) && !"cws_status".equals(fieldName) && "!cws_flag".equals(fieldName)) {
					break;
				}

				boolean isField = false;
				if (val.equals(ModuleUtil.FILTER_CUR_USER)) {
					val = pvg.getUser(request);
				} else if (val.equals(ModuleUtil.FILTER_CUR_DATE)) {
					val = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
				} else if (val.startsWith("{$")) {
					// 前为utf8中文范围，后为gb2312中文范围
					Pattern p = Pattern.compile(
							"\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}",
							Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(val);
					while (m.find()) {
						String fName = m.group(1);
						if (fName.startsWith("request.")) {
							String key = fName.substring("request.".length());
							val = ParamUtil.get(request, key);
						}
						else {
							// 判断fName是否为表单中的字段
							if (fd.getFormField(fName)!=null) {
								val = fName;
								isField = true;
							}
						}
					}
				}

				String display = el.getChildText("display");
				JSONObject json = new JSONObject(display);
				FormField ff = fd.getFormField(fieldName);
				// 如果是字符串型，则需加上双引号
				if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
					// 如当radio默认未选择时，其值为null，故 JS 判别时无需加双引号
					if (!"null".equals(val) && !isField) {
						val = "\"" + val + "\"";
					}
				}

				if ("null".equals(val)) {
					// 如果值为null，则只处理前台脚本，如当radio默认未选择时，其值为null
					continue;
				}
				if (isField) {
					val = "{$" + val + "}";
				}
				String condStr = "{$" + fieldName + "}" + token + val;
				// 判断是否为字符串型，如果是则需要变成equals
				if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
					if ("==".equals(token)) {
						condStr = val + ".equals({$" + fieldName + "})";
					}
					else if ("<>".equals(token)) {
						condStr = "!" + val + ".equals({$" + fieldName + "})";
					}
				}
				else if (ff.getType().equals(FormField.TYPE_CHECKBOX)){
					if ("<>".equals(token)) {
						condStr = val + "!={$" + fieldName + "}";
					}
				}
				else if (ff.getType().equals(FormField.TYPE_DATE)) {
					Date d;
					if ("current".equals(val.toLowerCase())) {
						d = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
					}
					else {
						d = DateUtil.parse(val, "yyyy-MM-dd");
						if (d==null) {
							throw new ErrMsgException(val + " 格式非法");
						}
					}
					String t = String.valueOf(d.getTime());
					condStr = "cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd\")!=null ? cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd\").getTime()" + token + t + "L : false";
				}
				else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
					Date d;
					if ("current".equals(val.toLowerCase())) {
						d = new Date();
					}
					else {
						d = DateUtil.parse(val, "yyyy-MM-dd HH:mm:ss");
						if (d==null) {
							throw new ErrMsgException(val + " 格式非法");
						}
					}
					String t = String.valueOf(d.getTime());
					condStr = "cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd HH:mm:ss\")!=null ? cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd HH:mm:ss\").getTime()" + token + t + "L : false";
				}

				if (fdao!=null && BranchMatcher.match(condStr, fd, fdao, userName)) {
					Iterator<String> ir3 = json.keys();
					while (ir3.hasNext()) {
						String idOrName = ir3.next();
						formContent = clearElement(formContent, idOrName);
					}
				}
			}
		} catch (IOException | JDOMException | JSONException | ParserException ex) {
			LogUtil.getLog(Render.class).error(ex);
		} catch (ErrMsgException ex) {
			LogUtil.getLog(Render.class).error(ex);
		}

		return formContent;
	}

	/**
	 * 从content中清除id或name为指定值的元素
	 * @param html
	 * @param idOrName
	 * @return
	 * @throws ParserException
	 */
	public static String clearElement(String html, String idOrName) throws ParserException {
		boolean isFound = false;
		Parser parser;
		TagNameFilter filter = new TagNameFilter("span");
		do {
			isFound = false;
			parser = new Parser(html);
			parser.setEncoding("utf-8");
			NodeList nodes = parser.parse(filter);
			if (nodes != null && nodes.size() > 0) {
				for (int i=0; i < nodes.size(); i++) {
					Span node = (Span) nodes.elementAt(i);
					if (node.getAttribute("id")!=null && node.getAttribute("id").equals(idOrName)) {
						isFound = true;
					}
					else if (node.getAttribute("name")!=null && node.getAttribute("name").equals(idOrName)) {
						isFound = true;
					}

					if (isFound) {
						int s = node.getStartPosition();
						int e = node.getEndTag().getEndPosition();
						String c = html.substring(0, s);
						c += html.substring(e);
						html = c;
						break;
					}
				}
			}
		} while (isFound);

		filter = new TagNameFilter("tr");
		do {
			isFound = false;
			parser = new Parser(html);
			parser.setEncoding("utf-8");
			NodeList nodes = parser.parse(filter);
			if (nodes != null && nodes.size() > 0) {
				for (int i=0; i < nodes.size(); i++) {
					TableRow node = (TableRow) nodes.elementAt(i);
					if (node.getAttribute("id")!=null && node.getAttribute("id").equals(idOrName)) {
						isFound = true;
					}
					else if (node.getAttribute("name")!=null && node.getAttribute("name").equals(idOrName)) {
						isFound = true;
					}

					if (isFound) {
						int s = node.getStartPosition();
						int e = node.getEndTag().getEndPosition();
						String c = html.substring(0, s);
						c += html.substring(e);
						html = c;
						break;
					}
				}
			}
		} while (isFound);

		filter = new TagNameFilter("div");
		do {
			isFound = false;
			parser = new Parser(html);
			parser.setEncoding("utf-8");
			NodeList nodes = parser.parse(filter);
			if (nodes != null && nodes.size() > 0) {
				for (int i=0; i < nodes.size(); i++) {
					Div node = (Div) nodes.elementAt(i);
					if (node.getAttribute("id")!=null && node.getAttribute("id").equals(idOrName)) {
						isFound = true;
					}
					else if (node.getAttribute("name")!=null && node.getAttribute("name").equals(idOrName)) {
						isFound = true;
					}

					if (isFound) {
						int s = node.getStartPosition();
						int e = node.getEndTag().getEndPosition();
						String c = html.substring(0, s);
						c += html.substring(e);
						html = c;
						break;
					}
				}
			}
		} while (isFound);

		filter = new TagNameFilter("input");
		do {
			isFound = false;
			parser = new Parser(html);
			parser.setEncoding("utf-8");
			NodeList nodes = parser.parse(filter);
			if (nodes != null && nodes.size() > 0) {
				for (int i=0; i < nodes.size(); i++) {
					InputTag node = (InputTag) nodes.elementAt(i);
					if (node.getAttribute("id")!=null && node.getAttribute("id").equals(idOrName)) {
						isFound = true;
					}
					else if (node.getAttribute("name")!=null && node.getAttribute("name").equals(idOrName)) {
						isFound = true;
					}

					if (isFound) {
						int s = node.getStartPosition();
						int e = node.getEndPosition();
						String c = html.substring(0, s);
						c += html.substring(e);
						html = c;
						break;
					}
				}
			}
		} while (isFound);

		filter = new TagNameFilter("textarea");
		do {
			isFound = false;
			parser = new Parser(html);
			parser.setEncoding("utf-8");
			NodeList nodes = parser.parse(filter);
			if (nodes != null && nodes.size() > 0) {
				for (int i=0; i < nodes.size(); i++) {
					TextareaTag node = (TextareaTag) nodes.elementAt(i);
					if (node.getAttribute("id")!=null && node.getAttribute("id").equals(idOrName)) {
						isFound = true;
					}
					else if (node.getAttribute("name")!=null && node.getAttribute("name").equals(idOrName)) {
						isFound = true;
					}

					if (isFound) {
						int s = node.getStartPosition();
						int e = node.getEndTag().getEndPosition();
						String c = html.substring(0, s);
						c += html.substring(e);
						html = c;
						break;
					}
				}
			}
		} while (isFound);

		filter = new TagNameFilter("select");
		do {
			isFound = false;
			parser = new Parser(html);
			parser.setEncoding("utf-8");
			NodeList nodes = parser.parse(filter);
			if (nodes != null && nodes.size() > 0) {
				for (int i=0; i < nodes.size(); i++) {
					SelectTag node = (SelectTag) nodes.elementAt(i);
					if (node.getAttribute("id")!=null && node.getAttribute("id").equals(idOrName)) {
						isFound = true;
					}
					else if (node.getAttribute("name")!=null && node.getAttribute("name").equals(idOrName)) {
						isFound = true;
					}

					if (isFound) {
						int s = node.getStartPosition();
						int e = node.getEndTag().getEndPosition();
						String c = html.substring(0, s);
						c += html.substring(e);
						html = c;
						break;
					}
				}
			}
		} while (isFound);

		return html;
	}
}
