package com.cloudweb.oa.controller;

import cn.js.fan.db.Paginator;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.cache.UserSetupCache;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.IPUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.*;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.ui.SkinMgr;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/message_oa")
public class MessageController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    private IFileService fileService;

    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private UserCache userCache;

    @RequestMapping("/download")
    public void download(HttpServletResponse response, @RequestParam(required = true) Integer msgId, @RequestParam(required = true) Integer attachId) throws IOException, ErrMsgException {
        MessageMgr mm = new MessageMgr();
        MessageDb md = mm.getMessageDb(msgId);
        Attachment att = md.getAttachment(attachId);
        if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
            try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
                /*因为不支持<img src='...'/>，故注释掉
                response.setContentType("text/html;charset=utf-8");
                String str = "<img src=\"" + request.getContextPath() + "/showImg.do?path=" + att.getVisualPath() + "/" + att.getDiskName() + "\" />";
                bos.write(str.getBytes(StandardCharsets.UTF_8));*/
                fileService.preview(response, att.getVisualPath() + "/" + att.getDiskName());
            } catch (final IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return;
        }

        fileService.download(response, att.getName(), att.getVisualPath(), att.getDiskName());
    }

    @ApiOperation(value = "删除垃圾箱", notes = "删除垃圾箱", httpMethod = "GET")
    @ResponseBody
    @RequestMapping(value = "/delToDustbin", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delToDustbin() {
        boolean re;
        try {
            MessageMgr messageMgr = new MessageMgr();
            re = messageMgr.doDustbin(request, true);
        } catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "删除", notes = "删除", httpMethod = "GET")
    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> del() {
        boolean re;
        try {
            MessageMgr messageMgr = new MessageMgr();
            re = messageMgr.delMsg(request);
        } catch (ErrMsgException e) {
            return new Result<>(false);
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "删除聊天", notes = "删除聊天", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/delChat", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delChat() {
        boolean re;
        try {
            MessageMgr messageMgr = new MessageMgr();
            re = messageMgr.doChat(request, true);
        } catch (ErrMsgException e) {
            return new Result<>(false);
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "设置已读", notes = "删除聊天室", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isReaded", value = "是否已读", dataType = "Boolean"),
    })
    @ResponseBody
    @RequestMapping(value = "/setReaded", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> setReaded(@RequestParam(required = true) Boolean isReaded) {
        String ids = ParamUtil.get(request, "ids");
        if (ids == null) {
            return new Result<>(false);
        }
        MessageDb md = new MessageDb();
        String[] newIds = ids.split(",");
        for (String newId : newIds) {
            md = (MessageDb) md.getMessageDb(StrUtil.toInt(newId));
            md.setReaded(isReaded);
            md.save();
        }

        JSONObject json = responseUtil.getResultJson(true);

        // 取出未读的数量，用于收件箱
        boolean isSys = ParamUtil.getBoolean(request, "isSys", false);
        if (!isSys) {
            Privilege privilege = new Privilege();
            String name = privilege.getUser(request);
            String sql = "select id from oa_message where isreaded=0 and box=0 and is_dustbin=0 and type=0 and receiver=" + StrUtil.sqlstr(name);
            int total = md.getObjectCount(sql);
            json.put("total", total);
        }

        return new Result<>(json);
    }

    @ApiOperation(value = "恢复", notes = "恢复", httpMethod = "GET")
    @ResponseBody
    @RequestMapping(value = "/restore", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> restore() {
        boolean re;
        try {
            MessageMgr messageMgr = new MessageMgr();
            re = messageMgr.doDustbin(request, false);
        } catch (ErrMsgException e) {
            return new Result<>(false);
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "发送接收", notes = "删除聊天室", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发送接收", dataType = "Integer"),
    })
    @ResponseBody
    @RequestMapping(value = "/sendReceipt", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> sendReceipt(Integer id) {
        boolean re = false;
        try {
            MessageMgr messageMgr = new MessageMgr();
            MessageDb md = messageMgr.getMessageDb(id);
            if (md==null || !md.isLoaded()) {
                return new Result<>(false);
            }
            md.setReceiptState(MessageDb.RECEIPT_RETURNED);
            re = md.save();
            if (re) {
                re = md.sendSysMsg(md.getSender(), "消息回执：" + md.getTitle(), "用户" + md.getReceiver() + "已经阅读了您的消息！");
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "删除信息通过发送人的垃圾箱", notes = "删除信息通过发送人的垃圾箱", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发送接收", dataType = "Integer"),
    })
    @ResponseBody
    @RequestMapping(value = "/delMsgBySenderDustbin", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delMsgBySenderDustbin(Integer id) {
        boolean re = false;
        try {
            MessageMgr messageMgr = new MessageMgr();
            re = messageMgr.delMsgBySenderDustbin(request);
        } catch (ErrMsgException e) {
            return new Result<>(false);
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "系统信息列表", notes = "系统信息列表", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isRecycle", value = "是否回收利用", dataType = "Integer"),
    })
    @RequestMapping(value = "/sysMessageListPage")
    @ResponseBody
    public Result<Object> sysMessageListPage(@RequestParam(defaultValue = "0") Integer isRecycle) {
        JSONObject object = new JSONObject();
        object.put("isRecycle", isRecycle);
        String actionType = ParamUtil.get(request, "actionType");
        String action = ParamUtil.get(request, "action");
        object.put("actionType", actionType);
        object.put("action", action);
        JSONArray arr = MessageMgr.getActionTypes();
        object.put("actionTypes", arr);
        boolean isNav = ParamUtil.getBoolean(request, "isNav", true);
        object.put("isNav", isNav);
        return new Result<>(object);
    }

    @ApiOperation(value = "系统信息列表", notes = "系统信息列表", httpMethod = "GET")
    @ResponseBody
    @RequestMapping(value = "/sysMessageList", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> sysMessageList() {
        Privilege privilege = new Privilege();
        String name = privilege.getUser(request);
        String op = ParamUtil.get(request, "op");
        String action = ParamUtil.get(request, "action");
        String orderBy = ParamUtil.get(request, "orderBy");
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }
        String kind = ParamUtil.get(request, "kind");
        if ("".equals(kind)) {
            kind = "title";
        }
        String actionType = ParamUtil.get(request, "actionType");
        String what = ParamUtil.get(request, "what");

        MessageDb md = new MessageDb();
        int curpage = ParamUtil.getInt(request, "page", 1);
        int pagesize = ParamUtil.getInt(request, "pageSize", 20);

        JSONArray ary = new JSONArray();
        int isRecycle = ParamUtil.getInt(request, "isRecycle", 0);
        String sql = md.getSqlOfSystem(name, isRecycle, action, what, kind, orderBy, sort, actionType);
        int total = md.getObjectCount(sql);

        Vector v = md.list(sql, (curpage - 1) * pagesize, curpage * pagesize - 1);
        for (Object o : v) {
            md = (MessageDb) o;
            int id = md.getId();
            String title = md.getTitle();
            String sender = md.getSender();
            String sendTime = md.getSendTime();
            boolean isReaded = md.isReaded();
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("isReaded",isReaded);
            if (!sender.equals(MessageDb.SENDER_SYSTEM)) {
                sender = userCache.getUser(sender).getRealName();
            }
            json.put("sender", sender);
            json.put("sendTime", sendTime);
            json.put("kind", MessageMgr.getActionName(md.getActionType()));
            json.put("isReaded", isReaded);
            json.put("title", title);
            ary.add(json);
        }

        JSONObject json = new JSONObject();
        json.put("list",ary);
        json.put("total", total);
        json.put("page", curpage);

        return new Result<>(json);
    }

    @ApiOperation(value = "系统信息展示列表", notes = "系统信息展示列表", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isRecycle", value = "是否回收利用", dataType = "Integer"),
    })
    @RequestMapping(value = "/sysMessageShowPage")
    @ResponseBody
    public Result<Object> sysMessageShowPage(@RequestParam(defaultValue = "0") Integer isRecycle) {
        MessageMgr messageMgr = new MessageMgr();
        int id = ParamUtil.getInt(request, "id", -1);
        MessageDb md = null;
        try {
            md = messageMgr.getMessageDb(id);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        if (md == null || !md.isLoaded()) {
            return new Result<>(false);
        }

        // 防止水平越权访问
        Privilege privilege = new Privilege();
        if (!md.getReceiver().equals(privilege.getUser(request)) && !privilege.isUserPrivValid(request, ConstUtil.PRIV_ADMIN)) {
            return new Result<>(false, "权限非法");
        }

        String title, content, rq, receiver, sender;
        int type;
        id = md.getId();
        title = md.getTitle();
        content = md.getContent();
        type = md.getType();
        rq = md.getRq();
        receiver = md.getReceiver();
        sender = md.getSender();

        UserMgr um = new UserMgr();
        String senderName = sender;
        if (!sender.equals(MessageDb.SENDER_SYSTEM)) {
            senderName = um.getUserDb(sender).getRealName();
        }

        if (md.getBox() == MessageDb.INBOX && receiver.equals(privilege.getUser(request))) {
            md.setReaded(true);
            md.save();
        }

        JSONObject object = new JSONObject();

        object.put("title", title);
        object.put("content", cn.js.fan.util.StrUtil.ubb(request, MessageDb.toHtml(content), true));
        object.put("receiver", receiver);
        object.put("senderName", senderName);

        object.put("actionType", md.getActionType());
        object.put("action", md.getAction());
        // 兼容6.0前旧版本
        String[] ary = StrUtil.split(md.getAction(), "\\|");
        Map<String, String> map = null;
        if (ary != null) {
            int len = ary.length;
            if (len >= 1) {
                map = new HashMap<>();
                for (String s : ary) {
                    String[] pair = s.split("=");
                    if (pair.length == 2) {
                        map.put(pair[0], pair[1]);
                    }
                }
            }
        }

        if (MessageDb.ACTION_FLOW_DISPOSE.equals(md.getActionType())) {
            String strMyActionId;
            if (map != null) {
                strMyActionId = map.get("myActionId");
            } else {
                strMyActionId = md.getAction();
            }
            object.put("visitKey", SecurityUtil.makeVisitKey(md.getAction()));
            // 判断待办流程是否已被处理
            long myActionId = StrUtil.toLong(strMyActionId, -1);
            if (myActionId != -1) {
                MyActionDb mad = new MyActionDb();
                mad = mad.getMyActionDb(myActionId);
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb((int)mad.getFlowId());
                if (wf != null) {
                    Leaf lf = new Leaf();
                    lf = lf.getLeaf(wf.getTypeCode());
                    if (lf != null) {
                        object.put("flowType", lf.getType());
                        if (mad.isChecked()) {
                            if (mad.getCheckStatus() != MyActionDb.CHECK_STATUS_NOT) {
                                object.put("actionType", MessageDb.ACTION_FLOW_SHOW);
                                object.put("action", mad.getFlowId());
                                object.put("actionName", "点击查看流程处理过程");
                            }
                        } else {
                            object.put("actionType", MessageDb.ACTION_FLOW_DISPOSE);
                            object.put("action", mad.getId());
                            object.put("actionName", "点击处理流程");
                        }
                    } else {
                        LogUtil.getLog(getClass()).error("流程: " + mad.getFlowId() + " 的类型: " + wf.getTypeCode() + " 不存在");
                    }
                } else {
                    LogUtil.getLog(getClass()).error("流程: " + mad.getFlowId() + " 不存在");
                }
            }
            else {
                object.put("actionType", "");
                object.put("action", -1);
                object.put("actionName", "待办记录已不存在");
            }
        }
        else if (MessageDb.ACTION_FLOW_SHOW.equals(md.getActionType())) {
            String strFlowId;
            if (map != null) {
                strFlowId = map.get("flowId");
            } else {
                strFlowId = md.getAction();
            }
            int flowId = StrUtil.toInt(strFlowId, -1);
            object.put("action", flowId);
            if (flowId == -1) {
                object.put("actionName", "流程已被删除");
            }
            else {
                object.put("actionName", "点击查看流程处理过程");
            }
        }

        /*String actionLink = md.renderAction(request);
        object.put("hasLink", !StrUtil.isEmpty(actionLink));
        object.put("actionLink", actionLink);*/

        object.put("attachments", md.getAttachments());
        object.put("id", id);
        object.put("rq", rq);

        return new Result<>(object);
    }

    /**
     * 获取信息类型
     */
    @ApiOperation(value = "获取信息类型", notes = "获取信息类型", httpMethod = "GET")
    @RequestMapping(value = "/getMessageType")
    @ResponseBody
    public Result<Object> getMessageType() {
        JSONArray arr = MessageMgr.getActionTypes();
        return new Result<>(arr);
    }

/*    @ApiOperation(value = "获取信息用户", notes = "获取信息用户", httpMethod = "GET")
	@ResponseBody
    @RequestMapping(value = "/getNewMsgsOfUser", method = {RequestMethod.POST, RequestMethod.GET}, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public List<IMessage> getNewMsgsOfUser() {
        MessageDb md = new MessageDb();
        return md.getNewMsgsOfUser(new Privilege().getUser(request));
    }*/

    @ApiOperation(value = "获取信息用户", notes = "获取信息用户", httpMethod = "GET")
    @ResponseBody
    @RequestMapping(value = "/getNewMsgsOfUser", method = {RequestMethod.POST, RequestMethod.GET}, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> getNewMsgsOfUser() {
        MessageDb md = new MessageDb();
        return new Result<>(md.getNewMsgsOfUser(new Privilege().getUser(request)));
    }
}
