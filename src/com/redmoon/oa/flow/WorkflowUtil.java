package com.redmoon.oa.flow;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.query.QueryScriptUtil;

import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

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
import java.util.Iterator;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		return "";
    }
    
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
     * @param wa WorkflowActionDb
     * @param fd FormDb
     * @param fdao FormDAO
     * @param userName String
     * @param isFormReport 是否用于查看流程时
     * @return String
     */
    public static String doGetViewJSMobile(HttpServletRequest request, FormDb fd, FormDAO fdao, String userName, boolean isForReport) {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(fdao.getFlowId());
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        if (wpd.getViews().equals(""))
            return "";

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
                                
                                str += "var tagName='input';\n";
                                str += "if (o('" + fieldName + "')) { tagName=o('" + fieldName + "').tagName; }\n";
                                str += "if ($(tagName + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";

                                // str += " alert($(\"input[name='" + fieldName + "']:checked\").val() + '" + token + "'+" + ary[1] + ");\n";

                                str += "  if ($(tagName + \"[name='" + fieldName + "']:checked\").val()" + token + ary[1] + ") {\n";
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
                                str += "  if ($(tagName + \"[name='" + fieldName + "']\").val()" + token + ary[1] + ") {\n";
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
                                str += "$(tagName + \"[name='" + fieldName + "']\").change(function(e) {\n";
                                // str += "alert('here');\n";
                                // str += "alert($(\"input[name='" + fieldName + "']\").attr('type'));\n";

                                // 判断是否为radio
                                str += "if ($(tagName + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";
                                str += "  if ($(tagName + \"[name='" + fieldName + "']:checked\").val()" + token + ary[1] + ") {\n";
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
                                str += "  if ($(tagName + \"[name='" + fieldName + "']\").val()" + token + ary[1] + ") {\n";
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
                            else
                                LogUtil.getLog(WorkflowUtil.class).error("condition=" + condition + ", 格式错误！");
                    	}
                    }
                    else {
                        // 如果不以#开头，则通过BranchMatcher.match在服务器端判断条件是否成立，条件表达式同分支线上的脚本表达式
                        // 如果条件为空或者条件为真
                        if (condition.equals("") ||
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
            /** @todo Handle this exception */
            ex.printStackTrace();
        } catch (ErrMsgException ex) {
            /** @todo Handle this exception */
            LogUtil.getLog(WorkflowUtil.class).trace(ex);
        }
        str += "</script>\n";
        return str;
    }    

    /**
     * 取得用来控制是否显示的脚本
     * @param request HttpServletRequest
     * @param wa WorkflowActionDb
     * @param fd FormDb
     * @param fdao FormDAO
     * @param userName String
     * @param isFormReport 是否用于查看流程时
     * @return String
     */
    public static String doGetViewJS(HttpServletRequest request, WorkflowActionDb wa, FormDb fd, FormDAO fdao, String userName, boolean isForReport) {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(fdao.getFlowId());
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        if (wpd.getViews().equals(""))
            return "";

        String str = "<script>\n";
        try {
            SAXBuilder parser = new SAXBuilder();

            org.jdom.Document doc = parser.build(new InputSource(new StringReader(wpd.getViews())));

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
	                            String[] ary = ary = StrUtil.split(condition, token);
	                            if (ary.length==2) {
	                                String fieldName = ary[0].substring(1); // 去掉#号
	                                ary[1] = ary[1].replaceAll("\"", "'");
	
	                                String display = el.getChildText("display");
	                                JSONObject json = new JSONObject(display);
	                                Iterator ir3 = json.keys();
	                                // 处理默认值的情况
	                                // 判断是否为radio
	                                
	                                str += "var tagName='input';\n";
	                                str += "if (o('" + fieldName + "')) { tagName=o('" + fieldName + "').tagName; }\n";
	                                str += "if ($(tagName + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";
	
	                                // str += " alert($(\"input[name='" + fieldName + "']:checked\").val() + '" + token + "'+" + ary[1] + ");\n";
	
	                                str += "  if ($(tagName + \"[name='" + fieldName + "']:checked\").val()" + token + ary[1] + ") {\n";
	                                while (ir3.hasNext()) {
	                                    String key = (String) ir3.next();
	                                    str += "  var obj=$('#" + key + "')[0];\n";
	                                    str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
	                                    str += "  obj." + json.get(key) + "();\n";
	                                }
	                                str += "  }\n";
	                                str += "}else{\n";
	                                str += "  if ($(tagName + \"[name='" + fieldName + "']\").val()" + token + ary[1] + ") {\n";
	                                ir3 = json.keys();
	                                while (ir3.hasNext()) {
	                                    String key = (String) ir3.next();
	                                    str += "  var obj=$('#" + key + "')[0];\n";
	                                    str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
	                                    str += "  obj." + json.get(key) + "();\n";
	                                }
	                                str += "  }\n";
	                                str += "}\n";
	
	                                // 处理事件
	                                // str += "var evt = 'propertychange';\n"; // 如果有多个条件，会出现多次重复定义
	                                str += "$(tagName + \"[name='" + fieldName + "']\").change(function(e) {\n";
	                                // str += "alert('here');\n";
	                                // str += "alert($(\"input[name='" + fieldName + "']\").attr('type'));\n";
	
	                                // 判断是否为radio
	                                str += "if ($(tagName + \"[name='" + fieldName + "']\").attr('type')=='radio') {\n";
	                                str += "  if ($(tagName + \"[name='" + fieldName + "']:checked\").val()" + token + ary[1] + ") {\n";
	                                ir3 = json.keys();
	                                while (ir3.hasNext()) {
	                                    String key = (String) ir3.next();
	                                    str += "  var obj=$('#" + key + "')[0];\n";
	                                    str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
	                                    str += "  obj." + json.get(key) + "();\n";
	                                }
	                                str += "  }\n";
	                                str += "}else{\n";
	                                str += "  if ($(tagName + \"[name='" + fieldName + "']\").val()" + token + ary[1] + ") {\n";
	                                ir3 = json.keys();
	                                while (ir3.hasNext()) {
	                                    String key = (String) ir3.next();
	                                    str += "  var obj=$('#" + key + "')[0];\n";
	                                    str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
	                                    str += "  obj." + json.get(key) + "();\n";
	                                }
	                                str += "  }\n";
	                                str += "}\n";
	                                str += "});\n";
	                            }
	                            else
	                                LogUtil.getLog(WorkflowUtil.class).error("condition=" + condition + ", 格式错误！");
                        	}
                        }
                        else {
                            // 如果不以#开头，则通过BranchMatcher.match在服务器端判断条件是否成立，条件表达式同分支线上的脚本表达式
                            // 如果条件为空或者条件为真
                            if (condition.equals("") ||
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
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JDOMException ex) {
            ex.printStackTrace();
        } catch (JSONException ex) {
            /** @todo Handle this exception */
            ex.printStackTrace();
        } catch (ErrMsgException ex) {
            /** @todo Handle this exception */
            LogUtil.getLog(WorkflowUtil.class).trace(ex);
        }
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
