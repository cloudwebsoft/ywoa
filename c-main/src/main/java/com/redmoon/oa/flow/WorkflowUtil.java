package com.redmoon.oa.flow;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import cn.js.fan.util.*;
import com.alibaba.fastjson.JSONArray;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.flow.query.QueryScriptUtil;

import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.ModuleUtil;
import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONException;
import java.io.StringReader;
import org.jdom.JDOMException;
import org.xml.sax.InputSource;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.input.SAXBuilder;
import org.jdom.Element;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * XML格式
 * <?xml version="1.0"?>
 <actions>
   <action internalName="0fc9eb3304494d53b3787a0a6e9cda70">
     <view>
       <condition>title=="abc"</condition><display>{"title":"show","ztc":"show"}</display>
     </view>
     <view>
       <condition>#fee>=5000</condition><display>{"ztc":"show"}</display>
     </view>
   </action>
 </actions>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowUtil {
    public WorkflowUtil() {
    }
    
    /**
     * 判断请求是否手机端
     * @param req
     * @return
     */
    public static boolean isMobile(HttpServletRequest req) {
        UserAgent ua = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
        OperatingSystem os = ua.getOperatingSystem();
        if(DeviceType.MOBILE.equals(os.getDeviceType())) {
            return true;
        }
        return false;
    }

    /**
     * 解析出表单内容中含有ID的HTML元素，并生成控制显示的JS脚本
     * @param content
     * @param id
     * @param func
     * @return
     */
    public static String getViewJSOfInnerHtml(String content, String id, String func) {
		Parser parser;
		try {
			parser = new Parser(content);
			parser.setEncoding("utf-8");//
			HasAttributeFilter filter = new HasAttributeFilter("id", id);
			NodeList nodes = parser.parse(filter);//
			
			if (nodes == null || nodes.size() == 0) {
				return "";
			}

			Node node = nodes.elementAt(0);
	    	StringBuffer sb = new StringBuffer();
			getViewJS(node, func, sb);
			return sb.toString();
		} catch (ParserException e) {
			e.printStackTrace();
		}    	
		return "";
    }

    /**
     * 手机端中，若规则中node若为区域，则将其内部的控件也隐藏
     * @param node
     * @param func
     * @param sb
     */
    public static void getViewJS(Node node, String func, StringBuffer sb) {
		NodeList nodesChildren = node.getChildren();
		int size = 0;
		if (nodesChildren!=null) {
			size = nodesChildren.size();
		}
		if (size==0) {
			if (node instanceof InputTag) {
				InputTag it = (InputTag)node;
				String idSub = it.getAttribute("name");
				sb.append("$('#row_" + idSub + "')." + func + "();\n");
			}
			else if (node instanceof SelectTag) {
				SelectTag it = (SelectTag)node;
				String idSub = it.getAttribute("name");
				sb.append("$('#row_" + idSub + "')." + func + "();\n");
			}
			else if (node instanceof TextareaTag) {
				TextareaTag it = (TextareaTag)node;
				String idSub = it.getAttribute("name");
				// System.out.println(WorkflowUtil.class.getName() + " idSub=" + idSub);
				sb.append("$('#row_" + idSub + "')." + func + "();\n");				
			}
		}
		else {
			for (int i=0; i<size; i++) {
				Node nd = nodesChildren.elementAt(i);
				getViewJS(nd, func, sb);
			}
		}
    }

    /**
     * 取得用来控制是否显示的脚本
     * @param request HttpServletRequest
     * @param fd FormDb
     * @param fdao FormDAO
     * @param userName String
     * @param isForReport 是否用于查看流程时
     * @return String
     */
    public static String doGetViewJSMobile(HttpServletRequest request, FormDb fd, FormDAO fdao, String userName, boolean isForReport) {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(fdao.getFlowId());
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        if ("".equals(wpd.getViews())) {
            return "";
        }

        String str = "<script>\n";
        try {
            SAXBuilder parser = new SAXBuilder();

            org.jdom.Document doc = parser.build(new InputSource(new StringReader(wpd.getViews())));

            Element root = doc.getRootElement();
            Iterator<Element> ir = root.getChildren().iterator();
            while (ir.hasNext()) {
                Element e = ir.next();
                Iterator<Element> ir2 = e.getChildren().iterator();
                while (ir2.hasNext()) {
                    Element el = ir2.next();
                    String condition = el.getChildText("condition").trim();
                    // 如果以#开头且不在流程查看时，对前台事件进行处理
                    if (condition.startsWith("#")) {
                    	// 非流程查看时
                    	if (!isForReport) {
                            String token = "==";
                            if (condition.indexOf(">=")!=-1)
                                token = ">=";
                            else if (condition.indexOf("<=")!=-1)
                                token = "<=";
                            else if (condition.indexOf("!=")!=-1)
                                token = "!=";
                            else if (condition.indexOf(">")!=-1)
                                token = ">";
                            else if (condition.indexOf("<")!=-1)
                                token = "<";
                            String[] ary = StrUtil.split(condition, token);
                            if (ary.length==2) {
                                String fieldName = ary[0].substring(1); // 去掉#号
                                ary[1] = ary[1].replaceAll("\"", "'");

                                String display = el.getChildText("display");
                                JSONObject json = new JSONObject(display);
                                Iterator<String> ir3 = json.keys();
                                // 处理默认值的情况
                                // 判断是否为radio
                                String rand = RandomSecquenceCreator.getId(10);

                                str += "var tagName" + rand + "='input';\n";
                                str += "if (o('" + fieldName + "')) { tagName" + rand + "=o('" + fieldName + "').tagName; }\n";
                                str += "if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";

                                // str += " alert($(\"input[name='" + fieldName + "']:checked\").val() + '" + token + "'+" + ary[1] + ");\n";

                                str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']:checked\").val()" + token + ary[1] + ") {\n";
                                while (ir3.hasNext()) {
                                    String id = ir3.next();
                                    str += getViewJSOfInnerHtml(fd.getContent(), id, json.getString(id));
/*                                    
                                    str += "  var obj=$('#" + key + "')[0];\n";
                                    str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
                                    str += "  obj." + json.get(key) + "();\n";*/
                                }
                                str += "  }\n";
                                str += "}else{\n";
                                str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").val()" + token + ary[1] + ") {\n";
                                ir3 = json.keys();
                                while (ir3.hasNext()) {
                                    String id = (String) ir3.next();
                                    str += getViewJSOfInnerHtml(fd.getContent(), id, json.getString(id));
/*
                                    str += "  var obj=$('#" + id + "')[0];\n";
                                    str += "  if (!obj) obj = o('" + id + "'); obj=$(obj);\n";
                                    str += "  obj." + json.get(id) + "();\n";*/
                                }
                                str += "  }\n";
                                str += "}\n";

                                // 处理事件
                                // str += "var evt = 'propertychange';\n"; // 如果有多个条件，会出现多次重复定义
                                str += "$(tagName" + rand + " + \"[name='" + fieldName + "']\").change(function(e) {\n";
                                // str += "alert('here');\n";
                                // str += "alert($(\"input[name='" + fieldName + "']\").attr('type'));\n";

                                // 判断是否为radio
                                str += "if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";
                                str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']:checked\").val()" + token + ary[1] + ") {\n";
                                ir3 = json.keys();
                                while (ir3.hasNext()) {
                                    String id = (String) ir3.next();
                                    str += getViewJSOfInnerHtml(fd.getContent(), id, json.getString(id));

/*                                    str += "  var obj=$('#" + key + "')[0];\n";
                                    str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
                                    str += "  obj." + json.get(key) + "();\n";*/
                                }
                                str += "  }\n";
                                str += "}else{\n";
                                str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").val()" + token + ary[1] + ") {\n";
                                ir3 = json.keys();
                                while (ir3.hasNext()) {
                                    String id = (String) ir3.next();
                                    str += getViewJSOfInnerHtml(fd.getContent(), id, json.getString(id));
/*                                    
                                    str += "  var obj=$('#" + key + "')[0];\n";
                                    str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
                                    str += "  obj." + json.get(key) + "();\n";*/
                                }
                                str += "  }\n";
                                str += "}\n";
                                str += "});\n";
                            }
                            else {
                                LogUtil.getLog(WorkflowUtil.class).error("condition=" + condition + ", 格式错误！");
                            }
                    	}
                    }
                    else {
                        // 如果不以#开头，则通过BranchMatcher.match在服务器端判断条件是否成立，条件表达式同分支线上的脚本表达式
                        // 如果条件为空或者条件为真
                        if ("".equals(condition) ||
                            BranchMatcher.match(condition, fd, fdao, userName)) {
                            String display = el.getChildText("display");
                            JSONObject json = new JSONObject(display);
                            Iterator ir3 = json.keys();
                            while (ir3.hasNext()) {
                                String id = (String) ir3.next();

                                str += getViewJSOfInnerHtml(fd.getContent(), id, json.getString(id));

/*                              str += "  var obj=$('#" + key + "')[0];\n";
                                str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
                                str += "  obj." + json.get(key) + "();\n";*/
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (ErrMsgException ex) {
            ex.printStackTrace();
            LogUtil.getLog(WorkflowUtil.class).trace(ex);
        }
        str += "</script>\n";
        return str;
    }

    public static String makeViewJS(FormDb fd, String fieldName, String token, String val, JSONObject json) throws JSONException {
        FormField ff = fd.getFormField(fieldName);
        if (ff == null) {
            DebugUtil.e(WorkflowUtil.class, "makeViewJS", "表单：" + fd.getName() + "（" + fd.getCode() + "）中的字段：" + fieldName + " 已不存在");
            return "";
        }

        String str = "";
        String rand = RandomSecquenceCreator.getId(10);
        Iterator ir3 = json.keys();

        str += "var tagName" + rand + "='input';\n";
        str += "if (o('" + fieldName + "')) { tagName" + rand + "=o('" + fieldName + "').tagName; }\n";

        str += "if ($(o('" + fieldName + "')).attr('type')=='radio' || $(o('" + fieldName + "')).attr('type')=='checkbox') {\n";
        // str += "if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";
        str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']:checked\").val()" + token + val + ") {\n";
        while (ir3.hasNext()) {
            String key = (String) ir3.next();
            str += "  var obj=$('#" + key + "')[0];\n";
            str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";

            // 当字段不可写时，会生成带有_show后缀的元素
            str += "  obj=$('#" + key + "_show')[0];\n";
            str += "  if (!obj) obj = o('" + key + "_show'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";
        }
        str += "  }\n";
        str += "}else{\n";
        str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").val()" + token + val + ") {\n";
        ir3 = json.keys();
        while (ir3.hasNext()) {
            String key = (String) ir3.next();
            str += "  var obj=$('#" + key + "')[0];\n";
            str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";

            // 当字段不可写时，会生成带有_show后缀的元素
            str += "  obj=$('#" + key + "_show')[0];\n";
            str += "  if (!obj) obj = o('" + key + "_show'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";
        }
        str += "  }\n";
        str += "}\n";

        // 处理事件
        // str += "var evt = 'propertychange';\n"; // 如果有多个条件，会出现多次重复定义
        if (ff.getType().equals(FormField.TYPE_RADIO) || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
            str += "$(tagName" + rand + " + \"[name='" + fieldName + "']\").click(function(e) {\n";
        }
        else {
            str += "$(tagName" + rand + " + \"[name='" + fieldName + "']\").change(function(e) {\n";
        }

        // 判断是否为radio
        str += "if ($(o('" + fieldName + "')).attr('type')=='radio' || $(o('" + fieldName + "')).attr('type')=='checkbox') {\n";
        // str += "if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";
        str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']:checked\").val()" + token + val + ") {\n";
        ir3 = json.keys();
        while (ir3.hasNext()) {
            String key = (String) ir3.next();
            str += "  var obj=$('#" + key + "')[0];\n";
            str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";

            // 当字段不可写时，会生成带有_show后缀的元素
            str += "  obj=$('#" + key + "_show')[0];\n";
            str += "  if (!obj) obj = o('" + key + "_show'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";
        }
        str += "  }\n";
        str += "}else{\n";
        str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").val()" + token + val + ") {\n";
        ir3 = json.keys();
        while (ir3.hasNext()) {
            String key = (String) ir3.next();
            str += "  var obj=$('#" + key + "')[0];\n";
            str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";

            // 当字段不可写时，会生成带有_show后缀的元素
            str += "  obj=$('#" + key + "_show')[0];\n";
            str += "  if (!obj) obj = o('" + key + "_show'); obj=$(obj);\n";
            str += "  obj." + json.get(key) + "();\n";
        }
        str += "  }\n";
        str += "}\n";
        str += "});\n";
        return str;
    }

    /**
     * 取得用来控制是否显示的脚本
     * @param request HttpServletRequest
     * @param wa WorkflowActionDb
     * @param fd FormDb
     * @param fdao FormDAO
     * @param userName String
     * @param isForReport 是否用于查看流程时
     * @return String
     */
    public static String doGetViewJS(HttpServletRequest request, WorkflowActionDb wa, FormDb fd, FormDAO fdao, String userName, boolean isForReport) {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(fdao.getFlowId());
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        if (wpd.getViews().equals("")) {
            return "";
        }

        String scriptOnLoad = "";
        String str = "<script>\n";
        str += "function doViewJS() {\n";
        try {
            SAXBuilder parser = new SAXBuilder();

            org.jdom.Document doc = parser.build(new InputSource(new StringReader(wpd.getViews())));
            DebugUtil.i(WorkflowUtil.class, "doGetViewJS", wpd.getViews());

            Element root = doc.getRootElement();
            Iterator<Element> ir = root.getChildren().iterator();
            while (ir.hasNext()) {
                Element e = ir.next();
                LogUtil.getLog(WorkflowUtil.class).info("doGetViewJS: internalName=" + e.getAttribute("internalName").getValue());
                // fgf 20161014 不再针对每个节点作显示控制，而是改为全局型的控制，internalName在flow_designer_action_view.jsp始终置为defaultNode
                if (true || e.getAttribute("internalName").getValue().equals(wa.getInternalName())) {
                    Iterator<Element> ir2 = e.getChildren().iterator();
                    while (ir2.hasNext()) {
                        Element el = ir2.next();
                        String condition = el.getChildText("condition").trim();
                        if (!"".equals(condition)) {
                            // 如果以#开头且不在流程查看时，对前台事件进行处理
                            if (condition.startsWith("#")) {
                                // 非流程查看时
                                if (!isForReport) {
                                    String token = "==";
                                    if (condition.indexOf(">=")!=-1)
                                        token = ">=";
                                    else if (condition.indexOf("<=")!=-1)
                                        token = "<=";
                                    else if (condition.indexOf("!=")!=-1)
                                        token = "!=";
                                    else if (condition.indexOf(">")!=-1)
                                        token = ">";
                                    else if (condition.indexOf("<")!=-1)
                                        token = "<";
                                    String[] ary = StrUtil.split(condition, token);
                                    if (ary.length==2) {
                                        String fieldName = ary[0].substring(1); // 去掉#号
                                        ary[1] = ary[1].replaceAll("\"", "'");
                                        String val = ary[1];

                                        String display = el.getChildText("display");
                                        JSONObject json = new JSONObject(display);
                                        str += makeViewJS(fd, fieldName, token, val, json);
                                    }
                                    else
                                        LogUtil.getLog(WorkflowUtil.class).error("condition=" + condition + ", 格式错误！");
                                }
                            }
                            else {
                                // 如果不以#开头，则通过BranchMatcher.match在服务器端判断条件是否成立，条件表达式同分支线上的脚本表达式
                                // 如果条件为空或者条件为真
                                if ("".equals(condition) ||
                                        BranchMatcher.match(condition, fd, fdao, userName)) {
                                    String display = el.getChildText("display");
                                    JSONObject json = new JSONObject(display);
                                    Iterator ir3 = json.keys();
                                    while (ir3.hasNext()) {
                                        String key = (String) ir3.next();
                                        // str += "$('#" + key + "')." + json.get(key) + "();\n";
                                        str += "  var obj=$('#" + key + "')[0];\n";
                                        str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
                                        str += "  obj." + json.get(key) + "();\n";
                                    }
                                }
                            }
                        }
                        else {
                            Privilege pvg = new Privilege();
                            List filedList = new ArrayList();
                            Iterator ir1 = fd.getFields().iterator();
                            while (ir1.hasNext()) {
                                FormField ff = (FormField) ir1.next();
                                filedList.add(ff.getName());
                            }

                            // 5.0版后
                            boolean formFlag = true;

                            String fieldName = el.getChildText("fieldName");
                            String token = el.getChildText("operator");

                            token = token.replaceAll("&lt;", "<");
                            token = token.replaceAll("&gt;", ">");
                            if (token.equals("=")) {
                                token = "==";
                            }
                            else if (token.equals("<>")) {
                                token = "!=";
                            }

                            String val = el.getChildText("value");

                            formFlag = filedList.contains(fieldName);
                            if (!formFlag && !"cws_id".equals(fieldName) && !"cws_status".equals(fieldName) && "!cws_flag".equals(fieldName)) {
                                break;
                            }

                            if (val.equals(ModuleUtil.FILTER_CUR_USER)) {
                                val = pvg.getUser(request);
                            } else if (val.equals(ModuleUtil.FILTER_CUR_DATE)) {
                                val = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
                            } else if (val.startsWith("{$")) {
                                Pattern p = Pattern.compile(
                                        "\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                                Matcher m = p.matcher(val);
                                while (m.find()) {
                                    String fName = m.group(1);
                                    if (fName.startsWith("request.")) {
                                        String key = fName.substring("request.".length());
                                        val = ParamUtil.get(request, key);
                                    }
                                }
                            }

                            String display = el.getChildText("display");
                            JSONObject json = new JSONObject(display);
                            FormField ff = fd.getFormField(fieldName);
                            if (ff == null) {
                                DebugUtil.e(WorkflowUtil.class, "doGetViewJS", "字段：" + fieldName + " 不存在");
                                continue;
                            }
                            // 如果是字符串型，则需加上双引号
                            if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
                                // 如当radio默认未选择时，其值为null，故 JS 判别时无需加双引号
                                if (!"null".equals(val)) {
                                    val = "\"" + val + "\"";
                                }
                            }

                            // 为前端生成 JS
                            String pageType = (String)request.getAttribute("pageType");
                            // 如果不是在flow_modify.jsp页面，则生成 JS
                            if (!"flowShow".equals(pageType)) {
                                str += makeViewJS(fd, fieldName, token, val, json);
                            }

                            // 为加载页面时根据数据库中记录值生成JS，scriptOnLoad不能放在doViewJS中，否则当表单域选择宏控件重新选择后，调用doViewJS时，scriptOnLoad脚本会有冲突
                            String condStr = "{$" + fieldName + "}" + token + val;
                            // 判断是否为字符串型，如果是则需要变成equals
                            if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
                                if (!"null".equals(val)) {
                                    if ("==".equals(token)) {
                                        condStr = val + ".equals({$" + fieldName + "})";
                                    } else if ("<>".equals(token)) {
                                        condStr = "!" + val + ".equals({$" + fieldName + "})";
                                    }
                                }
                                else {
                                    if ("==".equals(token)) {
                                        condStr = val + "=={$" + fieldName + "} || \"\".equals({$" + fieldName + "})";
                                    } else if ("<>".equals(token)) {
                                        condStr = val + "!={$" + fieldName + "} || \"\".equals({$" + fieldName + "})";
                                    }
                                }
                            }
                            else if (ff.getType().equals(FormField.TYPE_CHECKBOX)){
                                if ("<>".equals(token)) {
                                    condStr = val + "!={$" + fieldName + "}";
                                }
                            }
                            else if (ff.getType().equals(FormField.TYPE_DATE)) {
                                Date d;
                                if (val.toLowerCase().equals("current")) {
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
                                if (val.toLowerCase().equals("current")) {
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

                            // DebugUtil.i(WorkflowUtil.class, "doGetViewJS", "condStr=" + condStr);
                            if (fdao!=null && BranchMatcher.match(condStr, fd, fdao, userName)) {
                                Iterator ir3 = json.keys();
                                while (ir3.hasNext()) {
                                    String key = (String) ir3.next();
                                    scriptOnLoad += "  var obj=$('#" + key + "')[0];\n";
                                    scriptOnLoad += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
                                    scriptOnLoad += "  obj." + json.get(key) + "();\n";
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (ErrMsgException ex) {
            LogUtil.getLog(WorkflowUtil.class).trace(ex);
        }
        str += "}\n";
        str += "doViewJS();\n";
        str += scriptOnLoad + "\n";
        str += "</script>\n";
        return str;
    }
    
    public static int getColumnType(String dbSource, String tableCode, String columnName) {
    	String sql = "select * from " + tableCode;
    	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
    	try {
    		conn.setMaxRows(1); //尽量减少内存的使用
    		ResultSet rs = conn.executeQuery(sql);
    		ResultSetMetaData rm = rs.getMetaData();
    		int colCount = rm.getColumnCount();
    		for (int i = 1; i <= colCount; i++) {
    			if (rm.getColumnName(i).equals(columnName)) {
    				return QueryScriptUtil.getFieldTypeOfDBType(rm.getColumnType(i));		
    			}
    		}
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	}
    	finally {
    		conn.close();
    	}
    	return -1;
    }
}
