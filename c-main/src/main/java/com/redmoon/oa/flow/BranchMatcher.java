package com.redmoon.oa.flow;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Vector;
import java.util.Iterator;
import bsh.Interpreter;
import java.util.Date;
import bsh.*;

import cn.js.fan.web.Global;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.BeanShellUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.visual.ModuleUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * script脚本：{$表单域编码}.equals("a") && {$表单域名称}.equals("b") && 部门=="b" && 角色=="c" || 用户=="d" || "发起人角色"=="d"
 * 例如：{$电话}.equals("phone") && {$电脑}>3 && 部门=="office" && 发起人角色="role"
 * 表单域的值如果为字符串型，则必须要加双引号
 * 部门、角色的值必须要加双引号
 * 角色的值需为角色编码
 * 部门的值需为部门编码
 * 不等于符号为!=（部门不支持）

 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BranchMatcher {
    FormDb fd;
    FormDAO fdao;
    String userName;

    public BranchMatcher(FormDb fd, FormDAO fdao,
                         String userName) {
        this.fd = fd;
        this.fdao = fdao;
        this.userName = userName;
    }

    public static String parse(String scriptStr, FormDb fd, IFormDAO fdao, Vector fields, String userName) throws ErrMsgException {
        // 处理表单域
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(scriptStr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);
            // 制作大亚表单时发现，差旅费报销单中字段名称会有重复，所以这里先找编码，不行再找名称，防止名称重复
            FormField ff = fd.getFormField(fieldTitle);
            if (ff==null) {
                ff = fd.getFormFieldByTitle(fieldTitle);
                if (ff==null)
                    throw new ErrMsgException("表单：" + fd.getName() + "，脚本：" + scriptStr + "中，字段：" + fieldTitle + " 不存在！");
            }

            fields.addElement(ff);
            
            m.appendReplacement(sb, "\\$" + ff.getName());
        }
        m.appendTail(sb);

        LogUtil.getLog(BranchMatcher.class).info("sb=" + sb + " scriptStr=" + scriptStr);

        String s = sb.toString();
        s = s.replaceAll("部门 ?==", "dept==");
        s = s.replaceAll("部门 ?!=", "dept!=");
        s = s.replace("发起人角色", "starterRole");
        s = s.replaceAll("角色", "role");
        s = s.replaceAll("用户", "userName");

        LogUtil.getLog(BranchMatcher.class).info("s=" + s);

        // 处理角色
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        RoleDb[] rds = user.getRoles();

        // 处理角色为等于的情况，注意等式后面字符串在写的时候，不能有空格
        p = Pattern.compile("(role) ?== ?([\"A-Z0-9a-z]+)");
        m = p.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String str = "";
            for (int i=0; i<rds.length; i++) {
                if (str.equals(""))
                    str = "\"" + rds[i].getCode() + "\".equals($2)";
                else
                    str += " || \"" + rds[i].getCode() + "\".equals($2)";
            }
            str = "(" + str + ")";
            m.appendReplacement(sb, str);
        }
        
        m.appendTail(sb);
        s = sb.toString();

        // 处理角色为不等于的情况
        p = Pattern.compile("(role) ?!= ?([\"A-Z0-9a-z]+)");
        m = p.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String str = "";
            for (int i=0; i<rds.length; i++) {
                if (str.equals(""))
                    str = "!\"" + rds[i].getCode() + "\".equals($2)";
                else
                    str += "&& !\"" + rds[i].getCode() + "\".equals($2)";
            }
            str = "(" + str + ")";
            m.appendReplacement(sb, str);
        }
        m.appendTail(sb);
        s = sb.toString();

        // 处理发起人角色 2012-2-11 fgf，应大亚要求
        int flowId = fdao.getFlowId();
        if (flowId!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
	        WorkflowDb wf = new WorkflowDb();
	        wf = wf.getWorkflowDb(flowId);
	        user = user.getUserDb(wf.getUserName());
	        rds = user.getRoles();
        }

        // 处理发起人角色为等于的情况
        p = Pattern.compile("(starterRole) ?== ?([\"A-Z0-9a-z]+)");
        m = p.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String str = "";
            for (int i=0; i<rds.length; i++) {
                if (str.equals(""))
                    str = "\"" + rds[i].getCode() + "\".equals($2)";
                else
                    str += "|| \"" + rds[i].getCode() + "\".equals($2)";
            }
            str = "(" + str + ")";
            m.appendReplacement(sb, str);
        }
        m.appendTail(sb);
        s = sb.toString();

        // 处理发起人角色为不等于的情况
        p = Pattern.compile("(starterRole) ?!= ?([\"A-Z0-9a-z]+)");
        m = p.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String str = "";
            for (int i=0; i<rds.length; i++) {
                if (str.equals(""))
                    str = "!\"" + rds[i].getCode() + "\".equals($2)";
                else
                    str += "&& !\"" + rds[i].getCode() + "\".equals($2)";
            }
            str = "(" + str + ")";
            m.appendReplacement(sb, str);
        }
        m.appendTail(sb);
        s = sb.toString();

        // 处理部门
        DeptUserDb dud = new DeptUserDb();
        Vector deptV = dud.getDeptsOfUser(userName);
        p = Pattern.compile("(dept) ?== ?([\"A-Z0-9a-z]+)");
        m = p.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String str = "";
            Iterator deptIr = deptV.iterator();
            while (deptIr.hasNext()) {
                DeptDb dd = (DeptDb) deptIr.next();
                if (str.equals(""))
                    str = "\"" + dd.getCode() + "\".equals($2)";
                else
                    str += "|| \"" + dd.getCode() + "\".equals($2)";
            }
            // 如果未分配部门，则说明条件不满足，如果不处理，会生成()，会造成脚本报错 fgf 20160923
            if ("".equals(str)) {
            	str = "(1==0)";
            }
            else {
            	str = "(" + str + ")";
            }
            m.appendReplacement(sb, str);
        }
        m.appendTail(sb);
        s = sb.toString();
        
        p = Pattern.compile("(dept) ?!= ?([\"A-Z0-9a-z]+)");
        m = p.matcher(s);
        sb = new StringBuffer();
        while (m.find()) {
            String str = "";
            Iterator deptIr = deptV.iterator();
            while (deptIr.hasNext()) {
                DeptDb dd = (DeptDb) deptIr.next();
                if (str.equals(""))
                    str = "\"" + dd.getCode() + "\".equals($2)";
                else
                    str += "|| \"" + dd.getCode() + "\".equals($2)";
            }
            // 如果未分配部门，则说明条件满足
            if ("".equals(str)) {
            	str = "(1==0)";
            }
            else {            
            	str = "(!" + str + ")";
            }
            m.appendReplacement(sb, str);
        }        
        m.appendTail(sb);

        LogUtil.getLog(BranchMatcher.class).info("parse: " + sb.toString());

        return sb.toString();
    }

    public static void main(String args[]) {
    	String s = "dept==\"00020001\"";
    	Pattern p = Pattern.compile("(dept) ?== ?([\"A-Z0-9a-z]+)");
    	Matcher m = p.matcher(s);
    	StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String str = "";

            str = "(" + str + ")";
            m.appendReplacement(sb, str);
        }
        
        if (true) return;
    	
        String str1 = "<P class=MsoNormal align=left><SPAN lang=EN-US><INPUT title=aaa name=aaa canNull=\"1\" maxV=\"\" maxT=\"x=\" minV=\"\" minT=\"d=\" kind=\"DATE_TIME\"><IMG style=\"CURSOR: hand\" onclick='SelectDate(\"aaa\",\"yyyy-mm-dd\")' name=aaa_btnImg align=absMiddle src=\"http://localhost:8080/oa/images/form/calendar.gif\" width=26 height=26><INPUT style=\"WIDTH: 50px\" name=aaa_time value=12:30:30>&nbsp;<IMG style=\"CURSOR: hand\" onclick='SelectDateTime(\"aaa\")' name=aaa_time_btnImg align=absMiddle src=\"http://localhost:8080/oa/images/form/clock.gif\"></SPAN></P>";
        String patternStr1 =
                // "(\\/.*?)?(\\/images\\/.*?clock.gif)"; // 与calendar.gif一样的方式，却无效，比较奇怪
                // "src=\"((http|https)[^\\.]*?)?(\\/images\\/([^\\.]*?)clock\\.gif)"; // 20130326注释掉fgf 如果用这句，则应该replaceAll $3
                "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\)[^<]*?:?[0-9]*([^<]*?)?(\\/images\\/form\\/clock\\.gif))";

        // src="/oa2/images/form/calendar.gif"
        Pattern pattern1 = Pattern.compile(patternStr1);
        Matcher matcher1 = pattern1.matcher(str1);
        str1 = matcher1.replaceAll(
                "src=\"/oa/$5");

            // if (true)
            //    return;


        // String myStr = "aaa[br]aa";

        // myStr = myStr.replaceAll("\\[br\\]", "\n");

        String content = "src=\"http:/testoa/testoa/oa/images/form/calendar.gif\" _time_btnImg align=absMiddlealign=absMiddle src=\"/oa/images/form/clock.gif\">  ";

        String patternStr =
                "src=\"([^\"]*?)(\\/images\\/([^\"]*?)calendar.gif)";

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceAll("src=\"aaa" + "$2");

       patternStr =
               "src=\"([^\"]*?)(\\/images\\/([^\"]*?)clock.gif)";

       // src="/oa/images/form/calendar.gif"
       pattern = Pattern.compile(patternStr);
       matcher = pattern.matcher(content);
       content = matcher.replaceAll(
                "src=\"bbb$2");

        /*
        String patternStr =
                "src=\"(.*?)(\\/images\\/.*?calendar.gif)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceAll("src=\"aaa$2");

        patternStr =
                "src=\"(.*?)(\\/images\\/.*?clock.gif)";

        // src="/oa/images/form/calendar.gif"
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "src=\"bbb$2");
        */

        // if (true)
        //    return;

        Pattern SCRIPT_TAG_PATTERN = Pattern.compile("<script[^>]*>.*</script[^>]*>",
                                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        String str = "abc<script>function a() {}\r\n</script>de";
        Matcher matScript = SCRIPT_TAG_PATTERN.matcher(str);
        str = matScript.replaceAll("");

        Interpreter bsh = new Interpreter();

        // Evaluate statements and expressions
        // bsh.eval("foo=Math.sin(0.5)");
        try {
			BranchMatcher.bean();
		} catch (Exception e) {
            LogUtil.getLog(BranchMatcher.class).error(e);
		}
    }

    public static void bean() throws Exception {
        Interpreter bsh = new Interpreter();

        // Evaluate statements and expressions
        bsh.eval("foo=Math.sin(0.5)");
        bsh.eval("bar=foo*5; bar=Math.cos(bar);");
        bsh.eval("for(i=0; i<10; i++) { LogUtil.getLog(getClass()).info(\"hello\"); }");
        // same as above using java syntax and apis only
        bsh.eval("for(int i=0; i<10; i++) { LogUtil.getLog(getClass()).info(\"hello\"); }");

        //Source from files or streams
        // bsh.source("myscript.bsh");  // or bsh.eval("source(\"myscript.bsh\")");

        // Use set() and get() to pass objects in and out of variables
        bsh.set("date", new Date());
        Date date = (Date) bsh.get("date");
        // This would also work:
        Date date2 = (Date) bsh.eval("date");

        bsh.eval("LogUtil.getLog(getClass()).info(year = date.getYear())");
        Integer year = (Integer) bsh.get("year"); // primitives use wrappers

        // bsh.set("thisObj", this); // 将this object 赋给变量thisObject
        // String str = "LogUtil.getLog(getClass()).info(thisObj.lineChart())";

        String str = "import com.redmoon.oa.ofc.*;";
        str += "Test t = new Test();LogUtil.getLog(getClass()).info(t.pieChart());";
        str += "a=2;";
        str += "LogUtil.getLog(getClass()).info(1>a);";
        bsh.eval(str);
    }

    public static boolean match(String scriptStr, FormDb fd, IFormDAO fdao, String userName) throws ErrMsgException {
        LogUtil.getLog(BranchMatcher.class).info("match:scriptStr=" + scriptStr);	            

    	Vector fields = new Vector();
    	long t = System.currentTimeMillis();
        String sc = parse(scriptStr, fd, fdao, fields, userName);

        long t2 = System.currentTimeMillis();
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(ModuleUtil.class, "match after parse", (t2-t) + " ms");
        }

        boolean re = false;
        StringBuffer sb = new StringBuffer();
        // 20131219 fgf 改为给所有的字段赋值，而不仅仅是通过{$title}提取的字段
        // fields = fd.getFields();
        // 20200508 恢复为仅给parse方法中得到的fields中的元素赋值，否则效率太低，在bsh.eval(BeanShellUtil.escape(sb.toString()));时的耗时会达到230ms

		BeanShellUtil.setFieldsValue(fdao, fields, sb);

        long t3 = System.currentTimeMillis();
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(ModuleUtil.class, "match after setFieldsValue", (t3-t2) + " ms");
        }

        // 赋值给用户
        sb.append("userName=\"" + userName + "\";");

        Interpreter bsh = new Interpreter();
        try {
            LogUtil.getLog(BranchMatcher.class).info("match:" + sb.toString());
            LogUtil.getLog(BranchMatcher.class).info("match2:" + sc);
            
            bsh.set("fdao", fdao);

            bsh.eval(BeanShellUtil.escape(sb.toString()));

            long t4 = System.currentTimeMillis();
            if (Global.getInstance().isDebug()) {
                DebugUtil.i(ModuleUtil.class, "match after eval", (t4 - t3) + " ms");
            }

            // 查找ret=是为了保证跟以往版本的兼容性20131219 fgf
            int p = sc.lastIndexOf("ret=");
            if (p==-1) {
            	p = sc.lastIndexOf("ret =");
            }
            if (p==-1) {
	            bsh.eval("re=(" + sc + ");");
	            LogUtil.getLog(BranchMatcher.class).info("match3:" + bsh.get("re"));	            
	            re = (Boolean) bsh.get("re");
            }
            else {
				bsh.eval(sc);
				Object obj = bsh.get("ret");
				if (obj == null) {
					throw new ErrMsgException("请赋值给ret");
				}
				else {
		            re = (Boolean) obj;
				}
            }

            long t5 = System.currentTimeMillis();
            if (Global.getInstance().isDebug()) {
                DebugUtil.i(ModuleUtil.class, "match  after eval2", (t5-t4) + " ms");
            }
        } 
        catch (java.lang.ClassCastException e) {
            LogUtil.getLog(BranchMatcher.class).error(e);
            throw new ErrMsgException(e.getMessage());        	
        }
        catch (EvalError ex) {
            DebugUtil.e(BranchMatcher.class, "match eval", sb.toString());
            LogUtil.getLog(BranchMatcher.class).error(ex);
            throw new ErrMsgException(ex.getMessage());
        }
        DebugUtil.i(ModuleUtil.class, "match end ", (System.currentTimeMillis()-t) + " ms");
        return re;
    }

    public boolean doMatch(String scriptStr) throws ErrMsgException {
        return match(scriptStr, fd, fdao, userName);
    }

}
