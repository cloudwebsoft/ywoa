package com.redmoon.oa.message;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 红月亮工作室</p>
 * @author bluewind
 * @version 1.0
 */
import cn.js.fan.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;

public class MessageMgr {
    // public: connection parameters
    boolean debug = true;
    Privilege privilege;
    MessageDb MsgDB = new MessageDb();

    public MessageMgr() {
        privilege = new Privilege();
    }

    /**
     * 取得用户新消息数
     * @param request
     * @return
     */
    public int getNewMsgCount(HttpServletRequest request) {
        String name;
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        name = pvg.getUser(request);
        MessageDb md = new MessageDb();
        return md.getNewMsgCount(name);
    }

    /**
     * 发送消息
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean AddMsg(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        // Logger.getLogger(this.getClass().getName()).info("AddMsg:");
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (pvg.isUserLogin(request)) {
            // Logger.getLogger(this.getClass().getName()).info("AddMsg:has login");
            if (!com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                MsgDB.AddMsg(application, request, pvg.getUser(request));
            }
            else {
                ProxyFactory proxyFactory = new ProxyFactory(
                        "com.redmoon.oa.message.MessageDb");
                Advisor adv = new Advisor();
                MobileAfterAdvice mba = new MobileAfterAdvice();
                adv.setAdvice(mba);
                adv.setPointcut(new MethodNamePointcut("AddMsg", false));
                proxyFactory.addAdvisor(adv);
                IMessage imsg = (IMessage) proxyFactory.getProxy();
                imsg.AddMsg(application, request, pvg.getUser(request));
            }
            
        } else {
            throw new ErrMsgException("您尚未登录！");
        }
        return true;
    }
    
    /**
     * 保存至草稿箱
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean AddDraftMsg(ServletContext application, HttpServletRequest request)
			throws ErrMsgException {
		boolean re = false;
		com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
		if (pvg.isUserLogin(request)) {
			re = MsgDB.AddDraftMsg(application, request, pvg.getUser(request));
		} else {
            throw new ErrMsgException("您尚未登录！");
        }
		return re;
	}

    /**
     * 删除发件箱信息
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean delMsg(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException("您尚未登录!");
        }
        String[] ids = request.getParameterValues("ids");
        if (ids == null) {
            throw new ErrMsgException("请选择消息!");
        }
        if (!privilege.canManage(request, ids)) {
            throw new ErrMsgException("非法操作！");
        }
        if(ids.length == 1){
        	if(ids[0].contains(",")){
        		ids = ids[0].split(",");
        	}
        }
        return MsgDB.delMsg(ids);
    }
    
    /**
     * 删除发件箱信息至垃圾箱
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean delMsgBySenderDustbin(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException("您尚未登录!");
        }
        String[] ids = request.getParameterValues("ids");
        if (ids == null) {
            throw new ErrMsgException("请选择消息!");
        }
        if (!privilege.canManage(request, ids)) {
            throw new ErrMsgException("非法操作！");
        }

        return MsgDB.delMsgBySenderDustbin(ids);
    }
    
    /**
     * 垃圾箱操作
     * @param request
     * @param isDel true 删除 false 恢复
     * @return
     */
    public boolean doDustbin(HttpServletRequest request, boolean isDel) throws ErrMsgException {
        String[] ids = request.getParameterValues("ids");
        if(ids.length == 1){
        	if(ids[0].contains(",")){
        		ids = ids[0].split(",");
        	}
        }
        if (ids == null) {
            throw new ErrMsgException("请选择消息!");
        }
        if (!privilege.canManage(request, ids)) {
            throw new ErrMsgException("非法操作！");
        }

        return MsgDB.doDustbin(ids, isDel);
    }
    
    /**
     * 删除会话操作
     * @param request
     * @param 
     * @return
     */
    public boolean doChat(HttpServletRequest request, boolean isDel) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException("您尚未登录!");
        }
        String[] ids = request.getParameterValues("ids");
        if (ids == null) {
            throw new ErrMsgException("请选择消息!");
        }
        if (!privilege.canManage(request, ids)) {
            throw new ErrMsgException("非法操作！");
        }

        return MsgDB.doChat(ids, isDel);
    }

    public MessageDb getMessageDb(int id) throws ErrMsgException {
        return (MessageDb)MsgDB.getMessageDb(id);
    }

    /**
     * 当在弹出提醒窗口点击“我知道了”时调用
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean IKnow(HttpServletRequest request) throws ErrMsgException {
        String[] strids = request.getParameterValues("ids");
        if (strids==null) {
        	// 用于mydeskop.jsp
        	String ids = ParamUtil.get(request, "msgIds");
        	strids = StrUtil.split(ids, ",");
        	if (strids==null) {
                return false;
            }
        }
        int len = strids.length;
        int[] ids = new int[len];
        MessageDb md;
        boolean re = false;
        for (int i=0; i<len; i++) {
            ids[i] = Integer.parseInt(strids[i]);
            md = getMessageDb(ids[i]);
            md.setReaded(true);
            re = md.save();
        }
        return re;
    }
    
    /**
     * 转发收件箱、发送发件箱及草稿箱中的消息
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */    
    public boolean TransmitMsg(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        boolean re = false;
        if (pvg.isUserLogin(request)) {
            int id = ParamUtil.getInt(request, "id");
        	MessageDb md = getMessageDb(id);
        	
        	boolean isDraft = md.getBox()==MessageDb.DRAFT;
        	
            if (!com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                re = MsgDB.TransmitMsg(application, request, pvg.getUser(request), id);
            }
            else {
                ProxyFactory proxyFactory = new ProxyFactory(
                        "com.redmoon.oa.message.MessageDb");
                Advisor adv = new Advisor();
                MobileAfterAdvice mba = new MobileAfterAdvice();
                adv.setAdvice(mba);
                adv.setPointcut(new MethodNamePointcut("TransmitMsg", false));
                proxyFactory.addAdvisor(adv);
                IMessage imsg = (IMessage) proxyFactory.getProxy();
                re = imsg.TransmitMsg(application, request, pvg.getUser(request), id);
            }            
            
            if (re) {
            	// 草稿箱中删除,如果是转发则不删除
            	if (isDraft) {
                    md.del();
                }
            }
        } else {
            throw new ErrMsgException("您尚未登陆！");
        }
        return re;
    }

    public static String getActionName(String actionType) {
        if ("".equals(actionType)) {
            return "";
        }
        JSONArray arr = getActionTypes();
        for (int i=0; i<arr.size(); i++) {
            JSONObject json = arr.getJSONObject(i);
            if (json.getString("type").equals(actionType)) {
                return json.getString("name");
            }
        }
        return "";
    }

    public static JSONArray getActionTypes() {
        JSONArray jsonArray = new JSONArray();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_FLOW_DISPOSE);
        json.put("name", "流程处理");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_FLOW_SHOW);
        json.put("name", "流程查看");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_NOTICE);
        json.put("name", "通知公告");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_PAPER_DISTRIBUTE);
        json.put("name", "流程抄送");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_PLAN);
        json.put("name", "日程安排");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_MODULE_SHOW);
        json.put("name", "模块查看");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_MODULE_EDIT);
        json.put("name", "模块编辑");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_WORKLOG);
        json.put("name", "工作汇报");
        jsonArray.add(json);

        json = new com.alibaba.fastjson.JSONObject();
        json.put("type", MessageDb.ACTION_FILEARK_NEW);
        json.put("name", "文章发布");
        jsonArray.add(json);
        return jsonArray;
    }

}
