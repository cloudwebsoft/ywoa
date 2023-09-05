package com.redmoon.oa.flow;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import cn.js.fan.web.Global;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.dept.DeptDb;
import com.cloudwebsoft.framework.util.LogUtil;

public class FormForm extends AbstractForm {
    private String direction;
    FormDb ftd = new FormDb();

    public FormForm() {
    }

    public FormDb getFormDb() {
        return ftd;
    }

    public String chkCode(HttpServletRequest request) {
    	// 20170526 fgf 转为小写，以免linux上表名出现大小写问题
        String code = ParamUtil.get(request, "code").trim().toLowerCase();
        if ("".equals(code)) {
            log("编码必须填写！");
        }
        
        /*if (!StrUtil.isNotCN(code)) {
            log("编码中不能含有中文字符！");
        }*/

        if (!StrUtil.isSimpleCode(code)) {
            log("编码只能为字母、数字及符号：-_");
        }
        
        if (code.indexOf("@")!=-1) {
            log("编码中不能含有@字符！");   	
        }
        
        if ("code".equals(code)) {
        	log("编码不能为code！");
        }
        
        if ("id".equals(code)) {
        	log("编码不能为id！");
        }
        
        // if (!StrUtil.isCharOrNum(code)) {
        //    log("编码只允许字母或数字！");
        // }

        // if (!SecurityUtil.isValidSqlParam(code))
        //     log("请勿使用' ; 等字符！");
        ftd.setCode(code);
        return code;
    }

    public String chkName(HttpServletRequest request) {
        String name = ParamUtil.get(request, "name");
        if (name.equals("")) {
            log("名称必须填写！");
        }
        // if (!SecurityUtil.isValidSqlParam(name))
        //    log("请勿使用' ; 等字符！");
        ftd.setName(name);
        return name;
    }
    
    public boolean chkIsOnlyCamera(HttpServletRequest request) {
    	boolean isOnlyCamera = ParamUtil.get(request, "isOnlyCamera").equals("true");
        ftd.setOnlyCamera(isOnlyCamera);
        return isOnlyCamera;
    }    
    
    public boolean chkLog(HttpServletRequest request) {
        boolean isLog = ParamUtil.getInt(request, "isLog", 0)==1;
        ftd.setLog(isLog);
        return isLog;
    }    

    /**
     * 用于在setup时修复图片路径，注意图片路径是不带有http://的，因为在form编辑的时候作了处理
     * @param content String
     * @return String
     */
    public static String initImgLink(String content) {
        if (content==null)
            return "";
        String patternStr =
                "src=\"([^\"]*?)(\\/images\\/([^\"]*?)calendar.gif)";

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceAll("src=\"" + Global.getRootPath() + "$2");

        patternStr =
               "src=\"([^\"]*?)(\\/images\\/([^\"]*?)clock.gif)";

        // src="/oa/images/form/calendar.gif"
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("src=\"" + Global.getRootPath() + "$2");

        return content;
    }

    /**
     * 修复表单中图片的路径，当在编辑器编辑完时调用
     * @param content String
     * @param ieVersion String 可以为null，备用
     * @return String
     */
    public static String repairImgLink(String content, String ieVersion) {

        if ( true || ieVersion.equals("8")) {
            /*
            String patternStr =
                    "(\\/.*?)?(\\/images\\/.*?calendar.gif)";
            // @task:http://localhost:8080/oa似乎会变为http:/oa...，但有时测又没有
            // src="/oa2/images/form/calendar.gif"
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(content);
            content = matcher.replaceAll(
                Global.getRootPath() + "$2");
            */
           String patternStr =
                   "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\)[^<]*?:?[0-9]*(\\/[^<]*?)?(\\/images\\/form\\/calendar\\.gif))";
           Pattern pattern = Pattern.compile(patternStr);
           Matcher matcher = pattern.matcher(content);
           content = matcher.replaceAll(Global.getRootPath() + "$5");
        }
        else {
            String patternStr =
                    "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\).*?:?[0-9]*(\\/.*?)?(\\/images\\/form\\/calendar.gif))";

            // 在非IE8下，表单编辑器会自动补全图片的路径
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(content);
            content = matcher.replaceAll(Global.getRootPath() + "$5");
        }

        // LogUtil.getLog(getClass()).info("chkContent eVersion=" + ieVersion);
        // LogUtil.getLog(getClass()).info("chkContent content=" + content);

        if (true || ieVersion.equals("8")) {
            String patternStr =
                    // "(\\/.*?)?(\\/images\\/.*?clock.gif)"; // 与calendar.gif一样的方式，却无效，比较奇怪
                    // "src=\"((http|https)[^\\.]*?)?(\\/images\\/([^\\.]*?)clock\\.gif)"; // 20130326注释掉fgf 如果用这句，则应该replaceAll $3
                    // "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\).*?:?[0-9]*(\\/.*?)?(\\/images\\/.*?clock.gif))";
                    "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\)[^<]*?:?[0-9]*([^<]*?)?(\\/images\\/form\\/clock\\.gif))";

            // src="/oa2/images/form/calendar.gif"
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(content);
            content = matcher.replaceAll(
                Global.getRootPath() + "$5");
        }
        else {
            String patternStr =
                    "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\).*?:?[0-9]*(\\/.*?)?(\\/images\\/.*?clock.gif))";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(content);
            content = matcher.replaceAll(
                    Global.getRootPath() + "$5");
        }
        return content;
    }

    public String chkContent(HttpServletRequest request) {
        String content = ParamUtil.get(request, "content");
        if (content.equals(""))
            log("内容必须填写！");

        // 将content中包含有calendar.gif、clock.gif图片的http路径全改为相对路径
        String ieVersion = ParamUtil.get(request, "ieVersion");
        content = repairImgLink(content, ieVersion);

        ftd.setContent(content);
        return content;
    }

    public boolean chkFlow(HttpServletRequest request) {
        int isFlow = ParamUtil.getInt(request, "isFlow", 1);
        ftd.setFlow(isFlow==1);
        return isFlow==1;
    }

    public boolean chkHasAttachment(HttpServletRequest request) {
        int hasAttachment = ParamUtil.getInt(request, "hasAttachment", 1);
        ftd.setHasAttachment(hasAttachment==1);
        return hasAttachment==1;
    }
    
    public boolean chkIsProgress(HttpServletRequest request) {
        int isProgress = ParamUtil.getInt(request, "isProgress", 1);
        ftd.setProgress(isProgress==1);
        return isProgress==1;
    }
    

    public String chkFlowTypeCode(HttpServletRequest request) {
        String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
        if (flowTypeCode.equals(""))
            log("流程类型必须填写！");
        // if (flowTypeCode.equals(Leaf.CODE_ROOT))
        //    log("请选择正确的流程类型！");
        ftd.setFlowTypeCode(flowTypeCode);
        return flowTypeCode;
    }

    public String chkUnitCode(HttpServletRequest request) {
    	/*
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        DeptUserDb dud = new DeptUserDb();
        DeptDb dd = dud.getUnitOfUser(userName);
        */
    	String unitCode = ParamUtil.get(request, "unitCode");
        
        ftd.setUnitCode(unitCode);
        return unitCode;
    }

    public boolean checkCreate(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkName(request);
        chkContent(request);
        chkFlowTypeCode(request);
        chkFlow(request);
        chkHasAttachment(request);
        chkUnitCode(request);
        chkLog(request);
        chkIsProgress(request);
        chkIsOnlyCamera(request);
        report();
        return true;
    }

    public boolean checkModify(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkName(request);
        chkContent(request);
        chkFlow(request);
        chkFlowTypeCode(request);
        chkHasAttachment(request);
        chkLog(request);
        chkUnitCode(request);   
        chkIsProgress(request);        
        chkIsOnlyCamera(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        // chkCode(request); // 其中可能含有@流程
        String code = ParamUtil.get(request, "code").trim();
        if ("".equals(code)) {
            log("编码必须填写！");
        }

        ftd.setCode(code);

        // if (!SecurityUtil.isValidSqlParam(code))
        //    log("请勿使用' ; 等字符！");        
        report();
        return true;
    }

    public String chkDirection(HttpServletRequest request) {
        direction = ParamUtil.get(request, "direction");
        if ("".equals(direction)) {
            log("方向必须填写！");
        }
        return direction;
    }

    public boolean checkMove(HttpServletRequest request) throws ErrMsgException {
        init();
        chkCode(request);
        chkDirection(request);
        report();
        return true;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

}
