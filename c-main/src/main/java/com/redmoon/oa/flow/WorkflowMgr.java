package com.redmoon.oa.flow;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.cloudweb.oa.api.IMsgProducer;
import com.cloudweb.oa.service.FormArchiveService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.IPUtil;
import com.cloudwebsoft.framework.web.UserAgentParser;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.sys.DebugUtil;
import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.SQLCtl;
import com.redmoon.oa.flow.strategy.IStrategy;
import com.redmoon.oa.flow.strategy.StrategyMgr;
import com.redmoon.oa.flow.strategy.StrategyUnit;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.PlanDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.shell.BSHShell;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.util.BeanShellUtil;

public class WorkflowMgr {

    /**
     * flow_list.jsp的显示模式，流程查询模式
     */
    public static final int DISPLAY_MODE_SEARCH = 0;
    /**
     * 待办流程模式
     */
    public static final int DISPLAY_MODE_DOING = 1;
    /**
     * 我参与的流程模式
     */
    public static final int DISPLAY_MODE_ATTEND = 2;
    /**
     * 我发起的
     */
    public static final int DISPLAY_MODE_MINE = 3;

    /**
     * 我关注的
     */
    public static final int DISPLAY_MODE_FAVORIATE = 4;

    Logger logger = Logger.getLogger(WorkflowMgr.class.getName());
    FileUpload fu = new FileUpload();

    public WorkflowMgr() {
    }

    public FileUpload getFileUpload() {
        return fu;
    }

    public boolean doUpload(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        // String[] extnames = {"jpg", "gif", "xls", "rar", "doc", "rm", "avi", "bmp", "swf"};
        Config cfg = new Config();
        String exts = cfg.get("flowFileExt");
        String[] extAry = StrUtil.split(exts, ",");
        fu.setValidExtname(extAry); // 设置可上传的文件类型
        fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret!=FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }
        }
        catch (IOException e) {
        	e.printStackTrace();
            logger.error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        return true;
    }


    /**
     * 初始化自由流程
     * @param typeCode String
     * @param flowTitle String
     * @param userName String
     * @return long
     * @throws ErrMsgException
     */
    public long initWorkflowFree(String userName, String typeCode, String flowTitle, long projectId, int level) throws
            ErrMsgException {
        long startActionId = -1;

        Leaf lf = new Leaf();
        lf = lf.getLeaf(typeCode);

        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());
        if (fd == null || !fd.isLoaded()) {
            throw new ErrMsgException("表单不存在！");
        }

        // 检查用户是否有发起流程的权限
        WorkflowPredefineDb wfp = new WorkflowPredefineDb();
        UserDb user = new UserDb();
        user = user.getUserDb(userName);

        if (!wfp.canUserDo(user, typeCode, "start")) {
            throw new ErrMsgException("您没有发起流程的权限！");
        }

        WorkflowDb wf = new WorkflowDb();
        wf.setProjectId(projectId);
        wf.setLevel(level);

        // 记录表单归档ID
        FormArchiveService formArchiveService = SpringUtil.getBean(FormArchiveService.class);
        IFormDAO fdao = formArchiveService.getCurFormArchiveOrInit(lf.getFormCode());
        long formArchiveId = fdao.getId();
        wf.setFormArchiveId(formArchiveId);

        boolean re = wf.create(typeCode, flowTitle, userName);
        if (!re) {
            throw new ErrMsgException("初始化流程失败！");
        }

        int id = wf.getId();
        WorkflowDb wfd = getWorkflowDb(id);

        if (re) {
            WorkflowActionDb wa = new WorkflowActionDb();
            wa.setFlowId(id);
            wa.setStatus(WorkflowActionDb.STATE_DOING);
            wa.setUserName(userName);
            wa.setUserRealName(user.getRealName());
            wa.setInternalName(RandomSecquenceCreator.getId(30));
            wa.setIsStart(1);            
            wa.create();
            // 给发起者一条待办记录
            startActionId = wfd.notifyUser(userName, new java.util.Date(),-1, null,
                                           wa, WorkflowActionDb.STATE_DOING, id).getId();
        }

        return startActionId;
    }

    public long initWorkflow(String userName, String typeCode, String flowTitle, long projectId, int level) throws ErrMsgException {
        return initWorkflow(userName, typeCode, flowTitle, projectId, level, WorkflowDb.PARENT_ACTION_ID_NONE);
    }

    /**
     * 初始化预置流程
     * @param typeCode String
     * @param flowTitle String
     * @param userName String
     * @return long
     * @throws ErrMsgException
     */
    public long initWorkflow(String userName, String typeCode, String flowTitle, long projectId, int level, long parentActionId) throws
            ErrMsgException {
        long startActionId = -1;

        Leaf lf = new Leaf();
        lf = lf.getLeaf(typeCode);

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(typeCode);
        boolean isPredefined = wpd != null && wpd.isLoaded();

        if (!isPredefined) {
            throw new ErrMsgException(lf.getName() + " 预定义流程不存在!");
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());
        if (fd == null || !fd.isLoaded()) {
            throw new ErrMsgException("表单不存在！");
        }

        WorkflowDb wf = new WorkflowDb();
        wf.setProjectId(projectId);
        wf.setLevel(level);

        if (parentActionId!=WorkflowDb.PARENT_ACTION_ID_NONE) {
            wf.setParentActionId(parentActionId);
        }

        FormArchiveService formArchiveService = SpringUtil.getBean(FormArchiveService.class);
        IFormDAO fdao = formArchiveService.getCurFormArchiveOrInit(lf.getFormCode());
        long formArchiveId = fdao.getId();
        wf.setFormArchiveId(formArchiveId);

        boolean re = wf.create(typeCode, flowTitle, userName);
        int id = -1;
        if (!re) {
            throw new ErrMsgException("初始化流程失败！");
        }

        id = wf.getId();

        WorkflowDb wfd = getWorkflowDb(id);

        // 套用流程
        String flowString = wpd.getFlowString();
        String flowJson = wpd.getFlowJson();

        // System.out.println(getClass() + " " + flowString);
        try {
            // 替换其中的“本人”节点，检查发起人是否合法等
            flowString = wfd.regeneratePredefinedFlowString(userName, flowString);
            startActionId = wfd.createFromString(flowString, flowJson);
        } catch (ErrMsgException e) {
            wfd.del();
            throw e;
        }

        return startActionId;
    }

    /**
     * 在调度发起流程或者手机短信自动发起流程中，自动完成第一个节点（即发起人节点）
     * 注意不支持发起人节点为发散节点的情况
     * @param wf WorkflowDb
     * @param myAction WorkflowActionDb
     * @param myActionId long 发起人的myActionId
     * @param starter String 如果在系统中没有帐户，则以手机号替代，如果有，则根据手机号匹配用户名
     * @param nextActionUserName String 下一节点处理人员
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean finishFirstAction(WorkflowDb wf, WorkflowActionDb myAction, long myActionId, String starter, String nextActionUserName) throws ErrMsgException {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        if (!mad.isLoaded()) {
            throw new ErrMsgException("该待办流程已不存在，可能已被删除或者撤回！");
        }

        // 更新后续节点上选择的用户名和用户真实姓名
        UserMgr um = new UserMgr();
        UserDb user = um.getUserDb(nextActionUserName);

        WorkflowActionDb wfa = null;
        Vector vto = myAction.getLinkToActions();
        // 流程中不能有分支
        Iterator irto = vto.iterator();
        if (irto.hasNext()) {
            wfa = (WorkflowActionDb)irto.next();
            // 置节点上的用户名
            wfa.setUserName(nextActionUserName);
            wfa.setUserRealName(user.getRealName());
            wfa.save();
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        boolean re = false;
        String result = "";
        int resultValue = 0;

        HttpServletRequest request = null;

        // 检查流程是否已开始，如果未开始且用户有开始流程的权限，则开始流程
        if (!wf.isStarted()) {
            re = wf.start(request, starter, fu, myAction, myActionId);
        } else {
            re = myAction.changeStatus(request, wf,
                                       starter,
                                       WorkflowActionDb.STATE_FINISHED,
                                       "", result, resultValue, myActionId);
        }

        if (re) {
            Config cfg = new Config();
            IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
            boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");

            boolean isUseMsg = true; // getFieldValue("isUseMsg").equals("true");
            boolean isToMobile = true; // getFieldValue("isToMobile").equals("true");
            Iterator ir = myAction.getTmpUserNameActived().iterator();
            while (ir.hasNext()) {
                MyActionDb mad2 = (MyActionDb) ir.next();

                MessageDb md = new MessageDb();
                String t = SkinUtil.LoadString(request,
                                               "res.module.flow",
                                               "msg_user_actived_title");
                String c = SkinUtil.LoadString(request,
                                               "res.module.flow",
                                               "msg_user_actived_content");
                t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                c = c.replaceFirst("\\$flowTitle", wf.getTitle());
                c = c.replaceFirst("\\$fromUser", myAction.getUserRealName());

                if (isToMobile) {
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    if (imu != null) {
                        if (mqIsOpen) {
                            msgProducer.sendSms(wf.getUserName(), c);
                        }
                        else {
                            UserDb ud = um.getUserDb(wf.getUserName());
                            imu.send(ud, c, MessageDb.SENDER_SYSTEM);
                        }
                    }
                }

                if (isUseMsg) {
                    // 发送信息
                    c += WorkflowMgr.getFormAbstractTable(wf);
                    if (mqIsOpen) {
                        msgProducer.sendSysMsg(mad2.getUserName(), t, c, "");
                    }
                    else {
                        md.sendSysMsg(mad2.getUserName(), t, c);
                    }
                }
            }
        }
        return re;
    }

    /**
     * 根据流程code和title创建流程
     * @param request HttpServletRequest
     * @return int
     * @throws ErrMsgException
     */
    public int create(HttpServletRequest request) throws ErrMsgException {
        WorkflowCheck wc = new WorkflowCheck();
        wc.checkCreate(request);

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wc.typeCode);
        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());
        if (fd==null || !fd.isLoaded())
            throw new ErrMsgException("表单不存在！");

        WorkflowDb wf = new WorkflowDb();
        Privilege privilege = new Privilege();
        //privilege.isUserPrivValid(reqeust, "createworkflow");
        boolean re = wf.create(wc.typeCode, wc.title, privilege.getUser(request));
        //清缓存
        //WorkflowCacheMgr wcm = new WorkflowCacheMgr();
        //wcm.refreshcreate(dir_code);
        if (re)
            return wf.getId();
        else
            return -1;
    }

    public WorkflowDb getWorkflowDb(int id) {
        WorkflowDb wf = new WorkflowDb();
        return wf.getWorkflowDb(id);
    }

    /**
     * 取得表单的摘要表，用于发送短消息及邮件
     * @param wf WorkflowDb
     * @return String
     */
    public static String getFormAbstractTable(WorkflowDb wf) {
        StringBuffer sb = new StringBuffer();
        sb.append("<style>");
        sb.append(".flowTable{");
        sb.append("width:50%;");
        sb.append("margin:0px auto;");
        sb.append("border-collapse:collapse;");
        sb.append("font-size:12px;");
        sb.append("margin-bottom:10px;");
        sb.append("margin-top:0px;");
        sb.append("float:left;");
        sb.append("clear:both;");
        sb.append("}");
        sb.append(".flowTable td{");
        sb.append("border:1px solid #cccccc;");
        sb.append("padding:1px 3px;");
        sb.append("height:22px;");
        sb.append("}");
        sb.append("</style>");

        sb.append("<div>");
        sb.append("<table class='flowTable' width='60%'>");

        String sql = "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList from form_field where formCode=? and canList=1";
        sql += " order by orders desc";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            // WorkflowDb wf = new WorkflowDb();
            // wf = wf.getWorkflowDb((int)flowId);

            com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
            lf = lf.getLeaf(wf.getTypeCode());

            FormDb fd = new FormDb();
            fd = fd.getFormDb(lf.getFormCode());

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(wf.getId(), fd);

            UserMgr um = new UserMgr();

            sb.append("<tr><td style='width:50%'>等级：</td><td style='width:50%'>" + WorkflowDb.getLevelDesc(wf.getLevel()) + "</td></tr>");
            sb.append("<tr><td>发起人：</td><td>" + um.getUserDb(wf.getUserName()).getRealName() + "</td></tr>");
            sb.append("<tr><td>发起时间：</td><td>" + DateUtil.format(wf.getMydate(), "yyyy-MM-dd HH:mm:ss") + "</td></tr>");

            MacroCtlMgr mm = new MacroCtlMgr();

            ri = jt.executeQuery(sql, new Object[] {lf.getFormCode()});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                FormField ff = fd.getFormField(rr.getString("name"));
                String val = fdao.getFieldValue(ff.getName());
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.
                                                         getMacroType());
                    if (mu != null) {
                        val = mu.getIFormMacroCtl().converToHtml(null, ff,
                                                           fdao.getFieldValue(ff.getName()));
                    }
                }
                sb.append("<tr><td>" + rr.getString("title") + "：</td><td>" +
                          val + "</td></tr>");
            }
        } catch (SQLException ex) {
            LogUtil.getLog(WorkflowActionDb.class).error(StrUtil.trace(ex));
        }

        sb.append("</table>");
        sb.append("</div>");
        sb.append("<div style='clear:both; font-size:1px; height:1px'></div>");
        return sb.toString();
    }

    /**
     * 运行返回事件脚本
     * @param request
     * @param curUserName
     * @param flowId
     * @param fdao
     * @param script 脚本
     * @return
     * @throws ErrMsgException
     */
    public BSHShell runReturnScript(HttpServletRequest request, String curUserName, int flowId, FormDAO fdao, String script) throws ErrMsgException {
        BSHShell bs = new BSHShell();
        StringBuffer sb = new StringBuffer();

        BeanShellUtil.setFieldsValue(fdao, sb);

        // 赋值当前用户
        sb.append("String userName=\"" + curUserName + "\";");
        sb.append("int flowId=" + flowId + ";");

        bs.set(ConstUtil.SCENE, ConstUtil.SCENE_FLOW_ACTION_RETURN);
        bs.set("request", request);
        bs.set("fileUpload", fu);
        bs.set("fdao", fdao);

        bs.eval(BeanShellUtil.escape(sb.toString()));

        bs.eval(script);
        Object obj = bs.get("ret");
        if (obj != null) {
            boolean ret = ((Boolean) obj).booleanValue();
            if (!ret) {
                String errMsg = (String) bs.get("errMsg");
                LogUtil.getLog(getClass()).error("ReturnAction bsh errMsg=" + errMsg);
            }
        }
        return bs;
    }

    /**
     * 返回上一节点
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     * @param myAction WorkflowActionDb
     * @param myActionId long 当前动作节点上正在办理的记录id
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean ReturnAction(HttpServletRequest request, WorkflowDb wf,
                                WorkflowActionDb myAction, long myActionId) throws ErrMsgException {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        Privilege privilege = new Privilege();
        FormDAOMgr fdm = new FormDAOMgr(wf.getId(), lf.getFormCode(), fu, myAction);
        // 返回时不进行表单域的有效性检查
        if (fdm.update(request, true)) {
            String reason = getFieldValue("result");
            // 将被返回的action的id
            // 返回时是通过radio选节点，所以只能返回给一个节点            
            String[] returnIds = fu.getFieldValues("returnId");
            myAction.setReturnIds(returnIds);
            boolean re = false;
            if (lf.getType()==Leaf.TYPE_FREE) {
                re = myAction.changeStatusFree(request, wf,
                                           privilege.getUser(request),
                                           WorkflowActionDb.STATE_NOTDO, reason, "",
                                           myAction.getResultValue(),
                                           myActionId);
            }
            else {
                re = myAction.changeStatus(request, wf,
                                           privilege.getUser(request),
                                           WorkflowActionDb.STATE_NOTDO, reason, "",
                                           myAction.getResultValue(),
                                           myActionId);
            }
            if (re) {
                MyActionDb mad = new MyActionDb();
                mad = mad.getMyActionDb(myActionId);

                mad.setIp(IPUtil.getRemoteAddr(request));
                mad.setIp(IPUtil.getRemoteAddr(request));
                String ua = request.getHeader("User-Agent");
                mad.setOs(UserAgentParser.getOS(ua));
                mad.setBrowser(UserAgentParser.getBrowser(ua));

                // 置其它当前正在办理的节点的状态为因返回而忽略
                mad.returnMyAction();
                
				// 流程被返回时，节点上的事件脚本处理
                WorkflowActionDb wad = new WorkflowActionDb();
                wad = wad.getWorkflowActionDb(StrUtil.toInt(returnIds[0]));

                // 处理返回事件
				WorkflowPredefineDb wpd = new WorkflowPredefineDb();
				wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
				WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
				String script = wpm.getActionReturnScript(wpd.getScripts(), wad.getInternalName());
				if (!StringUtils.isEmpty(script)) {
                    FormDAO fdao = new FormDAO();
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(lf.getFormCode());
                    fdao = fdao.getFormDAO(wf.getId(), fd);
                    runReturnScript(request, privilege.getUser(request), wf.getId(), fdao, script);
				}

				// 如果流程可变更
                if (wpd.isReactive()) {
                    // 如果流程状态为已结束，而因为变更的原因又启动了，则需重置流程状态
                    if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
                        // 需重新获得wf，因为flowstring在上面的myAction.changeStatus中的save()中被改变了，而wf不更新，继续往下作为afterChangeStatus参数传递，就会出现问题
                        wf = wf.getWorkflowDb(wf.getId());

                        wf.setStatus(WorkflowDb.STATUS_STARTED);
                        wf.setResultValue(WorkflowActionDb.RESULT_VALUE_NOT_ACCESSED);
                        wf.save();

                        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                        FormDb fd = new FormDb();
                        fd = fd.getFormDb(lf.getFormCode());
                        fdao = fdao.getFormDAO(wf.getId(), fd);
                        fdao.setStatus(FormDAO.STATUS_NOT);
                        fdao.save();
                    }
                }
                
				// 调试模式不发送消息及邮件
				if (!lf.isDebug()) {
                    boolean isUseMsg = "true".equals(getFieldValue("isUseMsg"));
	                boolean isToMobile = "true".equals(getFieldValue("isToMobile"));
	
	                UserMgr um = new UserMgr();
	                String userRealName = um.getUserDb(mad.getUserName()).getRealName();
	
	                Config cfg = new Config();
                    IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                    boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
                    boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
	                String charset = Global.getSmtpCharset();
	                cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
	                String senderName = StrUtil.GBToUnicode(Global.AppName);
	                senderName += "<" + Global.getEmail() + ">";
	                if (flowNotifyByEmail) {
	                    String mailserver = Global.getSmtpServer();
	                    int smtp_port = Global.getSmtpPort();
	                    String name = Global.getSmtpUser();
	                    String pwd_raw = Global.getSmtpPwd();
	                    boolean isSsl = Global.isSmtpSSL();
	                    try {
	                        sendmail.initSession(mailserver, smtp_port, name,
	                                             pwd_raw, "", isSsl);
	                    } catch (Exception ex) {
	                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
	                    }
	                }
	
	                com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
	
	                UserDb user = new UserDb();
	                String t = SkinUtil.LoadString(request, "res.module.flow", "msg_user_actived_title");
                    String c = SkinUtil.LoadString(request, "res.module.flow", "msg_user_returned_content");
	
	                String tail = WorkflowMgr.getFormAbstractTable(wf);
	
	                MessageDb md = new MessageDb();
                    for (Object o : myAction.getTmpUserNameActived()) {
                        MyActionDb mad2 = (MyActionDb) o;
                        t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                        String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
                        fc = fc.replaceFirst("\\$fromUser", userRealName);

                        if (isToMobile) {
                            IMsgUtil imu = SMSFactory.getMsgUtil();
                            if (imu != null) {
                                if (mqIsOpen) {
                                    msgProducer.sendSms(mad2.getUserName(), fc);
                                } else {
                                    UserDb ud = um.getUserDb(mad2.getUserName());
                                    imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                                }
                            }
                        }

                        fc += tail;
                        if (isUseMsg) {
                            // 发送信息
                            String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad2.getId();
                            if (mqIsOpen) {
                                msgProducer.sendSysMsg(mad2.getUserName(), t, fc, action);
                            } else {
                                md.sendSysMsg(mad2.getUserName(), t, fc, action);
                            }
                        }

                        if (flowNotifyByEmail) {
                            user = user.getUserDb(mad2.getUserName());
                            if (!"".equals(user.getEmail())) {
                                String action = "userName=" + user.getName() + "|" + "myActionId=" + mad2.getId();
                                action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
                                UserSetupDb usd = new UserSetupDb(mad2.getUserName());
                                fc += "<BR />>>&nbsp;<a href='" + Global.getFullRootPath(request) +
                                        "/public/flow_dispose.jsp?action=" + action +
                                        "' target='_blank'>" +
                                        ("en-US".equals(usd.getLocal()) ? "Click here to apply" : "请点击此处办理") + "</a>";
                                if (mqIsOpen) {
                                    msgProducer.sendEmail(user.getEmail(), senderName, t, fc);
                                } else {
                                    sendmail.initMsg(user.getEmail(), senderName, t, fc, true);
                                    sendmail.send();
                                    sendmail.clear();
                                }
                            }
                        }
                    }
				}
            }
            return re;
        } else {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "faileSaveForm");
            throw new ErrMsgException(str);
        }
    }

    /**
     * 选择排序算法，按orders从小到大排列
     * @param nextActionUsers String[]
     * @return expireHours String[]
     */
    public static int[] selectSort(int[] orders, String[] nextActionUsers, String[] expireHours) {
        for (int i = 0; i < orders.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < orders.length; j++) {
                if (orders[min] > orders[j]) {
                    min = j;
                }
            }
            if (min != i) {
                int temp = orders[i];
                orders[i] = orders[min];
                orders[min] = temp;

                String t = nextActionUsers[i];
                nextActionUsers[i] = nextActionUsers[min];
                nextActionUsers[min] = t;

                t = expireHours[i];
                expireHours[i] = expireHours[min];
                expireHours[min] = t;
            }
        }
        return orders;
    }

    /**
     * 转交自由流程节点
     * @param request HttpServletRequest
     * @param wf WorkflowDb 流程
     * @param myAction WorkflowActionDb 当前动作节点
     * @param myActionId long 当前动作节点上正在办理的记录id
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean FinishActionFree(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction, long myActionId) throws ErrMsgException {
        // 检查锁
        checkLock(request, wf);

        // 保存流程的标题等
        if (!wf.isStarted()) {
            saveWorkflowProps(request, wf, myAction, fu);
        }

        String[] attachmentFiles = fu.getFieldValues("attachmentFiles");
        if (attachmentFiles!=null) {//外部邮箱转发@流程带附件
        	Privilege privilege = new Privilege();
        	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();//从XML文件里取出文件存入路径
			String file_flow = cfg.get("file_flow");
			Calendar cal = Calendar.getInstance();
			String year = "" + (cal.get(cal.YEAR));
			String month = "" + (cal.get(cal.MONTH) + 1);
			
			String vpath = file_flow + "/" + year + "/" + month;
			String filepath = Global.getRealPath() + vpath;//拼凑路径
            File f = new File(filepath);
            if (!f.isDirectory())
                f.mkdirs();

        	int len = attachmentFiles.length;
        	com.redmoon.oa.emailpop3.Attachment  attMail;
        	for (int i=0; i<len; i++) {
        		attMail = new com.redmoon.oa.emailpop3.Attachment(StrUtil.toInt(attachmentFiles[i]));//取得外部邮箱附件对象
        		
        		String fullPath = Global.getRealPath() +
				"/" + attMail.getVisualPath() + "/" +
				attMail.getDiskName();
        		
        		String newDiskName = RandomSecquenceCreator.getId() + "." +
				StrUtil.getFileExt(attMail.getDiskName());
				String newFullPath = filepath + "/" + newDiskName;

				// 拷贝文件
				FileUtil.CopyFile(fullPath, newFullPath);//将附件从邮箱复制到流程下
				
				com.redmoon.oa.flow.Attachment flowAttachment = new com.redmoon.oa.flow.Attachment();
				flowAttachment.setFullPath(newFullPath);
				flowAttachment.setDocId(wf.getDocId());
				flowAttachment.setName(attMail.getName());
				flowAttachment.setDiskName(newDiskName);
				flowAttachment.setVisualPath(vpath);
				flowAttachment.setSize(attMail.getFileSize());
				flowAttachment.setCreator(privilege.getUser(request));
				flowAttachment.setOrders((i+1));
				flowAttachment.setPageNum(1);
				flowAttachment.create();//保存数据
        	}
        }
        
        WorkflowPredefineDb wfd = new WorkflowPredefineDb();
        wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());
        
        String[] nextActionUsers = null;
        String[] expireHours = null;
        int[] orders = null;
        int ordersLen = 0;
        // 如果是@流程
        if (false && wfd.isLight()) {
            Vector v = new Vector();
            String patternStr = "@(.*?)[&|<| |　]"; // 注意全角空格
            Pattern pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            String cwsWorkflowResult = fu.getFieldValue("cwsWorkflowResult");
            // 后面加个空格，以免最后一个@*** 无法解析
            Matcher matcher = pattern.matcher(cwsWorkflowResult + " ");
            boolean re = matcher.find();
            while (re) {
            	String u = matcher.group(1);
            	// 避免重复
            	if (!v.contains(u))
            		v.addElement(u);
                re = matcher.find();
            }
            if (v.size()>0) {
            	nextActionUsers = new String[v.size()];
            	Iterator ir = v.iterator();
            	int i = 0;
            	while (ir.hasNext()) {
            		String u = (String)ir.next();
            		nextActionUsers[i] = u;
            		i ++;
            	}
            	
            	expireHours = new String[v.size()];
            	for (i=0; i<v.size(); i++) {
            		expireHours[i] = "0";
            	}
            	
            	orders = new int[v.size()];
            	for (i=0; i<v.size(); i++) {
            		orders[i] = 1;
            	}
            	
            	ordersLen = v.size();
            }
            else {
            	// throw new ErrMsgException("请选择用户！");
            }
        }
        else {
	        // String[] nextActionUsers = StrUtil.split(fu.getFieldValue("nextActionUsers"), ",");
            // 获取被选中的用户
            nextActionUsers = fu.getFieldValues("nextUsers");
            if (nextActionUsers!=null) {
                int aryLen = 0;
                aryLen = nextActionUsers.length;
                for (int i=0; i<aryLen; i++) {
                    nextActionUsers[i] = nextActionUsers[i].trim();
                }
            }
            else {
                nextActionUsers = new String[0];
            }

            expireHours = fu.getFieldValues("expireHours");
            if (expireHours!=null) {
                int aryLen = 0;
                aryLen = expireHours.length;
                for (int i=0; i<aryLen; i++) {
                    expireHours[i] = expireHours[i].trim();
                }
            }

            // 手机端自由流程中expireHours只会有1个元素，当选择多个用户的时候，需将expireHours的长度扩展为与nextActionUsers一致
            if (expireHours==null || expireHours.length!=nextActionUsers.length) {
                expireHours = new String[nextActionUsers.length];
                int aryLen = 0;
                aryLen = expireHours.length;
                for (int i=0; i<aryLen; i++) {
                    expireHours[i] = "0";
                }
            }

            // 用户名前面的checkbox不显示，因为后面有删除键，保留的意义似乎不大
            // toMes如果不选，在服务器端不太好匹配，且前台操作复杂，易引起混淆，所以在此也去掉
            // String[] toMes = fu.getFieldValues("toMes");

            String[] ordersStr = fu.getFieldValues("orders");
            if (nextActionUsers!=null && ordersStr!=null) // 手机端当@流程未选择下一步的用户时，orders仍为字符串1
                ordersLen = ordersStr.length;
            orders = new int[ordersLen];
            for (int i=0; i<orders.length; i++) {
                orders[i] = StrUtil.toInt(ordersStr[i].trim(), 1);
            }

            if (orders.length!=nextActionUsers.length) {
                orders = new int[nextActionUsers.length];
                int aryLen = orders.length;
                for (int i=0; i<aryLen; i++) {
                    orders[i] = 1; // 全部为1，表示一次提交给多个用户，否则按顺序流转
                }
                ordersLen = nextActionUsers.length;
            }

	        // 注意需要按照顺序创建action，从大到小，否则会因为排在前面的action未创建而出错
	        selectSort(orders, nextActionUsers, expireHours);	        
        }


        // 当上一节点同时转交给多个人，其中有用户办理完毕，而其它人未办理完毕时，该用户却因为没有选择下一节点的用户，而无法继续
        // 此时如果点击“结束流程”按钮，则会使得流程状态为已结束，且其它人的待办记录也会变为已办理
        // if (nextActionUsers==null)
        //    throw new ErrMsgException("请选择用户！");

        // LogUtil.getLog(getClass()).info("FinishActionFree:" + nextActionUsers);
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        Vector vto = myAction.getLinkToActions();

        // 如果流程状态为已结束，而因为重激活的原因又启动了，则需重置流程状态
        if (wf.getStatus()==WorkflowDb.STATUS_FINISHED) {
            WorkflowPredefineDb wfp = new WorkflowPredefineDb();
            wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());
            if (wfp.isReactive()) {
                wf.setStatus(WorkflowDb.STATUS_STARTED);
                wf.setResultValue(WorkflowActionDb.RESULT_VALUE_NOT_ACCESSED);
                wf.save();

                com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                FormDb fd = new FormDb();
                fd = fd.getFormDb(lf.getFormCode());
                fdao = fdao.getFormDAO(wf.getId(), fd);
                fdao.setStatus(FormDAO.STATUS_NOT);
                fdao.save();
            }
            else{
            	String str = LocalUtil.LoadString(request,"res.flow.Flow","processHaveBeenComplete");
            	 throw new ErrMsgException(str);
            }
               
        }

        UserMgr um = new UserMgr();
        Privilege privilege = new Privilege();

        // 创建后续节点和连接线
        // WorkflowActionDb privAction = myAction;
        // boolean isOrder = StrUtil.toInt(fu.getFieldValue("isOrder"), -1)==1;

        Map map = new HashMap();
        for (int i=0; i<ordersLen; i++) {
            // 取得顺序号
            int order = orders[i];

            String strExpire = expireHours[i];
            // LogUtil.getLog(getClass()).info("FinishActionFree: StrUtil.escape(nextActionUsers[i]).toUpperCase() + \"_expireHour\"=" +StrUtil.escape(nextActionUsers[i]).toUpperCase() + "_expireHour");

            /* 20121209 fgf 改为允许重复
            try {
                // 当有重复用户时，getFieldValue会返回Vector型，会出现异常
                strExpire = fu.getFieldValue(StrUtil.escape(nextActionUsers[
                        i]).toUpperCase() + "_expireHour");
            } catch (ClassCastException e) {
                throw new ErrMsgException("请检查是否有重名用户！");
            }
            */

            // 如果原来的状态是被返回，则检查被选的下一环节的用户是否已在于后续节点中
            // 还要考虑到当前节点状态为已处理时，此时再次激活了本节点的情况
            // 还要考虑到当前节点状态因为表单验证合法性未通过，从flow_dispose_free.jsp返回时，此时节点状态仍旧为未处理
            // 而nextActionUsers后续节点和连接已被创建，此时也需检查
            if (myAction.getStatus()==WorkflowActionDb.STATE_RETURN ||
                    myAction.getStatus()==WorkflowActionDb.STATE_FINISHED || myAction.getStatus()==WorkflowActionDb.STATE_DOING) {

                // LogUtil.getLog(getClass()).info("FinishActionFree: myAction.getStatus()=" + myAction.getStatus() + "nextActionUsers[" + i + "]=" + nextActionUsers[i]);

                Iterator irto = vto.iterator();
                boolean isUserExist = false;
                WorkflowActionDb toAction = null;
                while (irto.hasNext()) {
                    toAction = (WorkflowActionDb)irto.next();
                    if (toAction.getUserName().equals(nextActionUsers[i])) {
                        isUserExist = true;
                        break;
                    }
                }

                // LogUtil.getLog(getClass()).info("FinishActionFree: isUserExist=" + isUserExist);

                if (isUserExist) {
                    // 检查连接线上的到期时间有没有更改，如有更改，则重置到期时间
                    WorkflowLinkDb wld = new WorkflowLinkDb();
                    wld = wld.getWorkflowLinkDbForward(myAction, toAction);
                    // LogUtil.getLog(getClass()).info("FinishActionFree: strExpire=" + strExpire);
                    double dbExpire = StrUtil.toDouble(strExpire, 0);
                    if (wld.getExpireHour()!= dbExpire) {
                        wld.setExpireHour(dbExpire);
                        wld.save();
                    }
                    // 如果原来下一节点上已有myaction(待办记录)，则删除原有待办记录，否则会导致重复出现待办记录
                    MyActionDb mad = new MyActionDb();
                    mad = mad.getMyActionDbOfActionDoingByUser(toAction, nextActionUsers[i]);
                    if (mad != null) {
                        mad.del();
                    }
                    continue;
                }
            }

            // LogUtil.getLog(getClass()).info("nextActionUsers[i]=" + nextActionUsers[i]);

            // 创建节点
            WorkflowActionDb wa = new WorkflowActionDb();
            wa.setFlowId(wf.getId());
            wa.setStatus(WorkflowActionDb.STATE_DOING);
            wa.setUserName(nextActionUsers[i]);
            UserDb user = um.getUserDb(nextActionUsers[i]);
            wa.setUserRealName(user.getRealName());
            wa.setInternalName(RandomSecquenceCreator.getId(20));
            boolean r = wa.create();
            // 以 用户+顺序号 为键名，记录action
            map.put(nextActionUsers[i] + "_" + order, wa);

            if (r) {
                // 根据顺序号取出privAction，可能会有多个
                String privUsers = "";
                int searchOrder = order - 1;
                if (order==1) {
                    // 创建连接线
                    WorkflowLinkDb wl = new WorkflowLinkDb();
                    wl.setFlowId(wf.getId());
                    wl.setFrom(myAction.getInternalName());
                    wl.setTo(wa.getInternalName());
                    // 获取到期时间
                    // LogUtil.getLog(getClass()).info("expireHour=" + fu.getFieldValue(nextActionUsers[i] + "_expireHour"));
                    wl.setExpireHour(StrUtil.toInt(strExpire, 0));
                    wl.create();
                }
                else {
                    while (searchOrder != 0) {
                        boolean isFound = false;
                        // 往前找上一节点，确定from
                        for (int j = 0; j < ordersLen; j++) {
                            if (i != j) {
                                // 考虑到顺序号不连续的情况
                                if (orders[j] == searchOrder) {
                                    isFound = true;
                                    if (privUsers.equals(""))
                                        privUsers = nextActionUsers[j];
                                    else
                                        privUsers += "," + nextActionUsers[j];

                                    // 创建连接线
                                    WorkflowLinkDb wl = new WorkflowLinkDb();
                                    wl.setFlowId(wf.getId());
                                    // 取出 用户名 + order 对应的WorkflowActionDb
                                    WorkflowActionDb w = (WorkflowActionDb)map.get(nextActionUsers[j] + "_" + searchOrder);
                                    if (w != null) {
	                                    wl.setFrom(w.getInternalName());
	                                    wl.setTo(wa.getInternalName());
	                                    wl.setExpireHour(StrUtil.toInt(strExpire, 0));
	                                    wl.create();
                                    }
                                }
                            }
                        }
                        if (isFound) {
                            break;
                        }
                        searchOrder--;
                    }
                }
            }

            /*
            int toMe = StrUtil.toInt(toMes[i], 0);
            if (toMe==1) {
                // 创建回到自己的节点及连线
                WorkflowActionDb waMe = new WorkflowActionDb();
                waMe.setFlowId(wf.getId());
                waMe.setStatus(WorkflowActionDb.STATE_DOING);
                waMe.setUserName(privilege.getUser(request));
                user = um.getUserDb(privilege.getUser(request));
                waMe.setUserRealName(user.getRealName());
                waMe.setInternalName(RandomSecquenceCreator.getId(20));
                r = waMe.create();
                if (r) {
                    // 创建连接线
                    WorkflowLinkDb wlMe = new WorkflowLinkDb();
                    wlMe.setFlowId(wf.getId());
                    wlMe.setFrom(wa.getInternalName());
                    wlMe.setTo(waMe.getInternalName());
                    // 获取到期时间
                    // LogUtil.getLog(getClass()).info("expireHour=" + fu.getFieldValue(nextActionUsers[i] + "_expireHour"));
                    wlMe.setExpireHour(0);
                    wlMe.create();
                }
            }
            */
        }

        FormDAOMgr fdm = new FormDAOMgr(wf.getId(), lf.getFormCode(), fu, myAction);
        boolean re = fdm.update(request);
        if (re) {
            String result = "";
            int resultValue = 0;
            // 检查流程是否已开始，如果未开始且用户有开始流程的权限，则开始流程
            if (!wf.isStarted()) {
                re = start(request, wf.getId(), myAction, myActionId);
            }
            else {
                re = myAction.changeStatusFree(request, wf,
                        privilege.getUser(request),
                        WorkflowActionDb.STATE_FINISHED,
                        "", result, resultValue, myActionId);
            }
            if (re) {
				// 流程转交下一步时事件处理
				FormValidatorConfig fvc = new FormValidatorConfig();
				IFormValidator ifv = fvc.getIFormValidatorOfForm(lf.getFormCode());
				if (ifv != null && ifv.isUsed()) {
					ifv.onActionFinished(request, wf.getId(), fu);
				}   	       
            }
        } else{
        	String str = LocalUtil.LoadString(request,"res.flow.Flow","faileSaveForm");
        	throw new ErrMsgException(str);
        }
        if (re) {
            // 解锁流程
            unlock(wf.getId());

            // 自动存档
            String flag = myAction.getFlag();
            if (flag.length() >= 5 && flag.substring(4, 5).equals("2")) {
                String formReportContent = StrUtil.getNullStr(fu.getFieldValue(
                        "formReportContent"));
                saveDocumentArchive(wf, privilege.getUser(request),
                                    formReportContent);
            }

            boolean isUseMsg = getFieldValue("isUseMsg").equals("true");
            boolean isToMobile = getFieldValue("isToMobile").equals("true");

            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(myActionId);
            String userRealName = um.getUserDb(mad.getUserName()).getRealName();
            
            // 置消息中心中当前处理人当前流程的消息为已读
            MessageDb md = new MessageDb();
            md.setUserFlowReaded(mad.getUserName(), myActionId);

            // 调试模式不发送消息及邮件
            if (!lf.isDebug()) {
	            Config cfg = new Config();
                IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
	            boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
	            String charset = Global.getSmtpCharset();
	            cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
	            String senderName = StrUtil.GBToUnicode(Global.AppName);
	            senderName += "<" + Global.getEmail() + ">";
	            if (flowNotifyByEmail) {
	                String mailserver = Global.getSmtpServer();
	                int smtp_port = Global.getSmtpPort();
	                String name = Global.getSmtpUser();
	                String pwd_raw = Global.getSmtpPwd();
	                boolean isSsl = Global.isSmtpSSL();
	                try {
	                    sendmail.initSession(mailserver, smtp_port, name,
	                                         pwd_raw, "", isSsl);
	                } catch (Exception ex) {
	                    LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
	                }
	            }
	
	            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
	            String t = SkinUtil.LoadString(request,
	                                           "res.module.flow",
	                                           "msg_user_actived_title");
	            String c = SkinUtil.LoadString(request,
	                                           "res.module.flow",
	                                                   "msg_user_actived_content");
	            String tail = WorkflowMgr.getFormAbstractTable(wf);
	
	            boolean isReplyToMe = "1".equals(fu.getFieldValue("isReplyToMe"));
	            Iterator ir = myAction.getTmpUserNameActived().iterator();
	            while (ir.hasNext()) {
	                MyActionDb mad2 = (MyActionDb) ir.next();
	                
	                // 是否回复给我
	                if (isReplyToMe) {
	                	mad2.setResult("@" + privilege.getUser(request) + "&nbsp;");
	                	mad2.save();
	                }
	                
	                t = t.replaceFirst("\\$flowTitle", wf.getTitle().replace("$", "\\$"));
	                String fc = c.replaceFirst("\\$flowTitle", wf.getTitle().replace("$", "\\$"));
	                fc = fc.replaceFirst("\\$fromUser", userRealName);
	
	                if (isToMobile) {
	                    IMsgUtil imu = SMSFactory.getMsgUtil();
	                    if (imu != null) {
                            if (mqIsOpen) {
                                msgProducer.sendSms(mad2.getUserName(), fc);
                            }
                            else {
                                UserDb ud = um.getUserDb(mad2.getUserName());
                                imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                            }
	                    }
	                }
	
	                fc += tail;
	                if (isUseMsg) {
	                    // 发送信息
	                    String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad2.getId();
                        if (mqIsOpen) {
                            msgProducer.sendSysMsg(mad2.getUserName(), t, fc, action);
                        }
                        else {
                            md.sendSysMsg(mad2.getUserName(), t, fc, action);
                        }
	                }
	                if (flowNotifyByEmail) {
	                    UserDb user = um.getUserDb(mad2.getUserName());
	                    if (!user.getEmail().equals("")) {
	                        String action = "userName=" + user.getName() + "|" + "myActionId=" + mad2.getId();
	                        action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
	                        UserSetupDb usd = new UserSetupDb(mad2.getUserName());
	                        fc += "<BR />>>&nbsp;<a href='" + Global.getFullRootPath(request) +
	                                "/public/flow_dispose.jsp?action=" + action +
	                                "' target='_blank'>" + 
	                                (usd.getLocal().equals("en-US") ? "Click here to apply" : "请点击此处办理") + "</a>";
                            if (mqIsOpen) {
                                msgProducer.sendEmail(user.getEmail(), senderName, t, fc);
                            }
                            else {
                                sendmail.initMsg(user.getEmail(), senderName, t, fc, true);
                                sendmail.send();
                                sendmail.clear();
                            }
	                    }
	                }
	            }
	            // 如果流程完成了，则通知发起者
	            wf = wf.getWorkflowDb(myAction.getFlowId());
	            if (wf.getStatus()==WorkflowDb.STATUS_FINISHED) {
	                String ft = SkinUtil.LoadString(request,
	                                               "res.module.flow",
	                                               "msg_flow_finished_title");
	                String fc = SkinUtil.LoadString(request,
	                                               "res.module.flow",
	                                               "msg_flow_finished_content");
	                ft = StrUtil.format(ft, new String[] {wf.getTitle()});
	                fc = StrUtil.format(fc, new String[] {wf.getTitle(), myAction.getUserRealName(), DateUtil.format(myAction.getCheckDate(), "yyyy-MM-dd")});
	
	                if (isToMobile) {
	                    IMsgUtil imu = SMSFactory.getMsgUtil();
	                    if (imu != null) {
                            if (mqIsOpen) {
                                msgProducer.sendSms(wf.getUserName(), fc);
                            }
                            else {
                                UserDb ud = um.getUserDb(wf.getUserName());
                                imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                            }
	                    }
	                }
	
	                if (isUseMsg) {
	                    // 发送信息
	                    String action = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + wf.getId();
                        if (mqIsOpen) {
                            msgProducer.sendSysMsg(wf.getUserName(), ft, fc, action);
                        }
                        else {
                            md.sendSysMsg(wf.getUserName(), ft, fc, action);
                        }
	                }
	                // 发邮件
	                if (flowNotifyByEmail) {
	                    UserDb user = um.getUserDb(wf.getUserName());
	                    if (!user.getEmail().equals("")) {
	                        String action = "op=show|userName=" + user.getName() + "|" +
	                                        "flowId=" + wf.getId();
	                        action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
	                        UserSetupDb usd = new UserSetupDb(user.getName());
	                        fc += "<BR />>>&nbsp;<a href='" +
	                                Global.getFullRootPath(request) +
	                                "/public/flow_dispose.jsp?action=" + action +
	                                "' target='_blank'>" + 
	                                (usd.getLocal().equals("en-US") ? "Click here to view" : "请点击此处查看") + "</a>";
                            if (mqIsOpen) {
                                msgProducer.sendEmail(user.getEmail(), senderName, ft, fc);
                            }
                            else {
                                sendmail.initMsg(user.getEmail(), senderName, ft, fc, true);
                                sendmail.send();
                                sendmail.clear();
                            }
	                    }
	                }
	            }
            }
        }
        return re;
    }

    /**
     * 忽略wad节点所在的分支，wad为分支上的第一个节点
     * @param wad WorkflowActionDb
     */
    public void ignoreBranch(WorkflowActionDb wad, WorkflowActionDb endAction) throws ErrMsgException {
    	// 到终止节点前的节点全部忽略
    	if (endAction != null && wad.getInternalName().equalsIgnoreCase(endAction.getInternalName())) {
    		// 20180721 如果wad与endAction为同一节点，则有可能是刚进入函数，还没进入下一步的递归，所以也需检查处理
    		if (endAction.getStatus()!=WorkflowActionDb.STATE_IGNORED) {
    			endAction.setStatus(WorkflowActionDb.STATE_IGNORED);
    			endAction.save();
    		}
    		return;
    	}
        wad.setStatus(WorkflowActionDb.STATE_IGNORED);
        wad.save();
        WorkflowActionDb wfa = null;
        Vector vto = wad.getLinkToActions(); // 出度
        /*
        if (vto.size()==1) {
            wfa = (WorkflowActionDb)vto.elementAt(0);
            // 入度大于1
            if (wfa.getLinkFromActions().size()>1) {
                wfa = null;
            }
        }
        while (wfa!=null) {
            wfa.setStatus(WorkflowActionDb.STATE_IGNORED);
            wfa.save();
            vto = wfa.getLinkToActions();
            if (vto.size()==1) {
                wfa = (WorkflowActionDb)vto.elementAt(0);
                if (wfa.getLinkFromActions().size()>1) {
                    wfa = null;
                }
            }
            else
                wfa = null;
        }*/
        
        // 用递归来做
        Iterator it = vto.iterator();
        while (it.hasNext()) {
        	wfa = (WorkflowActionDb) it.next();
        	// 已经置为忽略的话,则不再执行,防止多次操作
        	if (wfa.getStatus() != WorkflowActionDb.STATE_IGNORED) {
            	ignoreBranch(wfa, endAction);
        	}
        }
    }


    /**
     * 流程移交
     * @param request HttpServletRequest
     * @return int
     * @throws ErrMsgException
     */
    public int handover(HttpServletRequest request) throws ErrMsgException {
        String oldUserName = ParamUtil.get(request, "oldUserName");
        String newUserName = ParamUtil.get(request, "newUserName");
        if (oldUserName.equals(newUserName)) {
            throw new ErrMsgException("原用户与移交的用户不能相同！");
        }
        if (newUserName.equals("")) {
            throw new ErrMsgException("请选择将要移交的用户！");
        }

        String[] typeCode = ParamUtil.getParameters(request, "typeCode");
        java.util.Date beginDate = DateUtil.parse(ParamUtil.get(request, "begin_date"), "yyyy-MM-dd");
        java.util.Date endDate = DateUtil.parse(ParamUtil.get(request, "end_date"), "yyyy-MM-dd");
        String typeStr = "";
        if (typeCode != null) {
            for (int i = 0; i < typeCode.length; i++) {
                if (typeStr.equals(""))
                    typeStr = StrUtil.sqlstr(typeCode[i]);
                else
                    typeStr += "," + StrUtil.sqlstr(typeCode[i]);
            }
        }
        if (!typeStr.equals(""))
            typeStr = "(" + typeStr + ")";

        String sql = "select a.id from flow_my_action a where a.user_name=" + StrUtil.sqlstr(oldUserName) + " and (a.is_checked=0 or a.is_checked=" + MyActionDb.CHECK_STATUS_SUSPEND + ")";
        if (!typeStr.equals("")) {
            sql = "select a.id from flow_my_action a, flow f where f.id=a.flow_id and a.user_name=" +
                  StrUtil.sqlstr(oldUserName) + " and f.type_code in " + typeStr + " and (a.is_checked=0 or a.is_checked=" + MyActionDb.CHECK_STATUS_SUSPEND + ")";
        }
        if (beginDate != null)
            sql += " and a.receive_date>=" +
                    SQLFilter.getDateStr(DateUtil.format(beginDate, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
        if (endDate != null)
            sql += " and a.receive_date<" +
                    SQLFilter.getDateStr(DateUtil.format(DateUtil.addDate(endDate, 1), "yyyy-MM-dd HH:mm:ss"),
                                         "yyyy-MM-dd HH:mm:ss");

        // System.out.println(getClass() + " sql=" + sql);

        MyActionDb mad = new MyActionDb();
        Vector v = mad.list(sql);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            mad = (MyActionDb) ir.next();
            handover(request, mad, newUserName);
        }
        return v.size();
    }

    /**
     * 移交
     * @param request HttpServletRequest
     * @param mad MyActionDb
     * @param toUserName String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean handover(HttpServletRequest request, MyActionDb mad, String toUserName) throws ErrMsgException {
        Privilege privilege = new Privilege();
        // mad.setChecked(true);
        mad.setCheckStatus(MyActionDb.CHECK_STATUS_HANDOVER);
        // 任务移交的时候，request为null
        if (request!=null) {
        	mad.setChecker(privilege.getUser(request));
        }
        boolean re = mad.save();
        if (re) {
            long privMyActionId = mad.getPrivMyActionId();
            MyActionDb privMyActionDb = new MyActionDb();
            privMyActionDb = privMyActionDb.getMyActionDb(privMyActionId);

            long privActionId = privMyActionDb.getActionId();
            WorkflowActionDb privWa = new WorkflowActionDb();
            privWa = privWa.getWorkflowActionDb((int) privActionId);

            WorkflowActionDb wa = new WorkflowActionDb();
            wa = wa.getWorkflowActionDb((int) mad.getActionId());
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb((int)mad.getFlowId());
            MyActionDb nextMyActionDb = wf.notifyUser(toUserName,
                                                      new java.util.Date(),
                                                      mad.getPrivMyActionId(),
                                                      privWa, wa,
                                                      WorkflowActionDb.STATE_HANDOVER,
                                                      mad.getFlowId());
            if (re) {
                boolean isUseMsg = true;
                boolean isToMobile = true;
                if (request!=null) {
                	ParamUtil.get(request, "isUseMsg").equals("true");
                	ParamUtil.get(request, "isToMobile").equals("true");
                }
                
                Config cfg = new Config();
                IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
                boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
                String charset = Global.getSmtpCharset();
                cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
                String senderName = StrUtil.GBToUnicode(Global.AppName);
                senderName += "<" + Global.getEmail() + ">";
                if (flowNotifyByEmail) {
                    String mailserver = Global.getSmtpServer();
                    int smtp_port = Global.getSmtpPort();
                    String name = Global.getSmtpUser();
                    String pwd_raw = Global.getSmtpPwd();
                    boolean isSsl = Global.isSmtpSSL();
                    try {
                        sendmail.initSession(mailserver, smtp_port, name, pwd_raw, "", isSsl);
                    } catch (Exception ex) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                    }
                }

                com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

                String t = SkinUtil.LoadString(request,
                                               "res.module.flow",
                                               "msg_user_actived_title");
                String c = SkinUtil.LoadString(request,
                                               "res.module.flow",
                                               "msg_user_actived_content");
                MessageDb md = new MessageDb();

                t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
                fc = fc.replaceFirst("\\$fromUser", wa.getUserRealName());

                if (isToMobile) {
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    if (imu != null) {
                        if (mqIsOpen) {
                            msgProducer.sendSms(nextMyActionDb.getUserName(), fc);
                        }
                        else {
                            UserDb ud = new UserDb();
                            ud = ud.getUserDb(nextMyActionDb.getUserName());
                            imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                        }
                    }
                }

                fc += WorkflowMgr.getFormAbstractTable(wf);
                if (isUseMsg) {
                    // 发送信息
                    String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE +
                                    "|myActionId=" + nextMyActionDb.getId();
                    if (mqIsOpen) {
                        msgProducer.sendSysMsg(nextMyActionDb.getUserName(), t, fc, action);
                    }
                    else {
                        md.sendSysMsg(nextMyActionDb.getUserName(), t, fc, action);
                    }
                }

                if (flowNotifyByEmail) {
                    UserMgr um = new UserMgr();
                    UserDb user = um.getUserDb(nextMyActionDb.getUserName());
                    if (!user.getEmail().equals("")) {
                        String action = "userName=" + user.getName() + "|" +
                                        "myActionId=" + nextMyActionDb.getId();
                        action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(
                                ssoCfg.getKey(), action);
                        fc += "<BR />>>&nbsp;<a href='" +
                                Global.getFullRootPath(request) +
                                "/public/flow_dispose.jsp?action=" + action +
                                "' target='_blank'>请点击此处办理</a>";
                        if (mqIsOpen) {
                            msgProducer.sendEmail(user.getEmail(), senderName, t, fc);
                        }
                        else {
                            sendmail.initMsg(user.getEmail(), senderName, t, fc, true);
                            sendmail.send();
                            sendmail.clear();
                        }
                    }
                }
            }
        }
        return re;
    }

    /**
     * 自由流程，在flow_modify.jsp中转交
     * @param request HttpServletRequest
     * @param flowId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean deliverFree(HttpServletRequest request, int flowId) throws ErrMsgException {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        // 检查用户权限
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        if (!userName.equals(wf.getUserName())) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        MyActionDb mad = new MyActionDb();
        mad = mad.getFirstMyActionDbOfFlow(flowId);

        WorkflowActionDb myAction = new WorkflowActionDb();
        myAction = myAction.getWorkflowActionDb((int)mad.getActionId());
        
        WorkflowPredefineDb wfd = new WorkflowPredefineDb();
        wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());
        
        String[] nextActionUsers = null;
        String cwsWorkflowResult = ParamUtil.get(request, "cwsWorkflowResult");
        
        // 如果是@流程
        if (wfd.isLight()) {
	        /*Vector v = new Vector();
	        String patternStr = "@(.*?)[&|<| |　]"; // 注意全角空格
	        Pattern pattern = Pattern.compile(patternStr,
	                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	        // 后面加个空格，以免最后一个@*** 无法解析
	        Matcher matcher = pattern.matcher(cwsWorkflowResult + " ");
	        boolean re = matcher.find();
	        while (re) {
	        	String u = matcher.group(1);
	        	// 避免重复
	        	if (!v.contains(u))
	        		v.addElement(u);
	            re = matcher.find();
	        }    
	        
	        if (v.size()>0) {
	        	nextActionUsers = new String[v.size()];
	        	Iterator ir = v.iterator();
	        	int i = 0;
	        	while (ir.hasNext()) {
	        		String u = (String)ir.next();
	        		nextActionUsers[i] = u;
	        		i ++;
	        	}
	        }   */  
        	
            String userNames = ParamUtil.get(request, "nextActionUsers");
            nextActionUsers = StrUtil.split(userNames, ",");
        }
        else {
            String userNames = ParamUtil.get(request, "nextUsers");
            nextActionUsers = StrUtil.split(userNames, ",");
        }
        
        if (nextActionUsers==null){
        	String str = LocalUtil.LoadString(request, "res.flow.Flow","selectPersonnel");
        	throw new ErrMsgException(str);
        }

        // 创建后续节点和连接线
        int len = 0;
        if (nextActionUsers!=null) {
            len = nextActionUsers.length;
        }
        for (int i=0; i<len; i++) {
            String strExpire = "";
            if (!wfd.isLight()) {
	            try {
	                // 当有重复用户时，getFieldValue会返回Vector型，会出现异常
	                strExpire = ParamUtil.get(request, StrUtil.escape(nextActionUsers[i]).toUpperCase() + "_expireHour");
	            } catch (ClassCastException e) {
	            	String str = LocalUtil.LoadString(request, "res.flow.Flow","checkUser");
	                throw new ErrMsgException(str);
	            }
            }

            WorkflowActionDb wa = new WorkflowActionDb();
            wa.setFlowId(wf.getId());
            wa.setStatus(WorkflowActionDb.STATE_DOING);
            wa.setUserName(nextActionUsers[i]);
            wa.setInternalName(RandomSecquenceCreator.getId(20));
            boolean r = wa.create();

            if (r) {
                WorkflowLinkDb wl = new WorkflowLinkDb();
                wl.setFlowId(wf.getId());
                wl.setFrom(myAction.getInternalName());
                wl.setTo(wa.getInternalName());
                // 获取到期时间
                LogUtil.getLog(getClass()).info("expireHour=" + strExpire);
                wl.setExpireHour(StrUtil.toDouble(strExpire, 0));
                wl.create();
            }

            String toUserName = nextActionUsers[i];

            MyActionDb nextMyActionDb = wf.notifyUser(toUserName,
                                                      new java.util.Date(),
                                                      mad.getId(),
                                                      myAction, wa,
                                                      WorkflowActionDb.STATE_DOING,
                                                      wf.getId());
            // 置备注
            if (!cwsWorkflowResult.equals("")) {
                nextMyActionDb.setResult(cwsWorkflowResult);
                nextMyActionDb.save();
            }

            // 重置流程状态
            // 20170406 fgf 如果修改状态，则应同时修改表单及子表的cws_status，所以此处注释掉，保留原来的状态
            // wf.setStatus(WorkflowDb.STATUS_STARTED);
            // wf.setResultValue(WorkflowActionDb.RESULT_VALUE_NOT_ACCESSED);
            // wf.save();

            boolean isUseMsg = ParamUtil.get(request, "isUseMsg").equals("true");
            boolean isToMobile = ParamUtil.get(request, "isToMobile").equals("true");

            Config cfg = new Config();
            IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
            boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
            boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
            String charset = Global.getSmtpCharset();
            cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
            String senderName = StrUtil.GBToUnicode(Global.AppName);
            senderName += "<" + Global.getEmail() + ">";
            if (flowNotifyByEmail) {
                String mailserver = Global.getSmtpServer();
                int smtp_port = Global.getSmtpPort();
                String name = Global.getSmtpUser();
                String pwd_raw = Global.getSmtpPwd();
                boolean isSsl = Global.isSmtpSSL();
                try {
                    sendmail.initSession(mailserver, smtp_port, name, pwd_raw, "", isSsl);
                } catch (Exception ex) {
                    LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                }
            }

            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            String t = SkinUtil.LoadString(request,
                                           "res.module.flow",
                                           "msg_user_actived_title");
            String c = SkinUtil.LoadString(request,
                                           "res.module.flow",
                                           "msg_user_actived_content");
            MessageDb md = new MessageDb();

            t = t.replaceFirst("\\$flowTitle", wf.getTitle());
            String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
            fc = fc.replaceFirst("\\$fromUser", myAction.getUserRealName());

            if (isToMobile) {
                IMsgUtil imu = SMSFactory.getMsgUtil();
                if (imu != null) {
                    if (mqIsOpen) {
                        msgProducer.sendSms(nextMyActionDb.getUserName(), fc);
                    }
                    else {
                        UserDb ud = new UserDb();
                        ud = ud.getUserDb(nextMyActionDb.getUserName());
                        imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                    }
                }
            }

            fc += WorkflowMgr.getFormAbstractTable(wf);
            if (isUseMsg) {
                // 发送信息
                String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + nextMyActionDb.getId();
                if (mqIsOpen) {
                    msgProducer.sendSysMsg(nextMyActionDb.getUserName(), t, fc, action);
                }
                else {
                    md.sendSysMsg(nextMyActionDb.getUserName(), t, fc, action);
                }
            }

            if (flowNotifyByEmail && !nextMyActionDb.getUserName().equals("")) {
                UserMgr um = new UserMgr();
                UserDb user = um.getUserDb(nextMyActionDb.getUserName());
                if (user.isLoaded() && !user.getEmail().equals("")) {
                    String action = "userName=" + user.getName() + "|" +
                                    "myActionId=" + nextMyActionDb.getId();
                    action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(
                            ssoCfg.getKey(), action);
                    UserSetupDb usd = new UserSetupDb(user.getName());
                    fc += "<BR />>>&nbsp;<a href='" +
                            Global.getFullRootPath(request) +
                            "/public/flow_dispose.jsp?action=" + action +
                            "' target='_blank'>" + 
                            (usd.getLocal().equals("en-US") ? "Click here to apply" : "请点击此处办理") + "</a>";
                    if (mqIsOpen) {
                        msgProducer.sendEmail(user.getEmail(), senderName, t, fc);
                    }
                    else {
                        sendmail.initMsg(user.getEmail(), senderName, t, fc, true);
                        sendmail.send();
                        sendmail.clear();
                    }
                }
            }
        }

        return true;
    }

    /**
     * 转办(指派)
     * @param request HttpServletRequest
     * @param myActionId long
     * @param toUserName String
     * @return long
     * @throws ErrMsgException
     */
    public boolean transfer(HttpServletRequest request, long myActionId, String toUserName) throws ErrMsgException {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        if (!mad.isLoaded()) {
            throw new ErrMsgException("该待办流程已不存在，可能已被删除或者撤回！");
        }
        Privilege privilege = new Privilege();
        // mad.setChecked(true);
        mad.setCheckStatus(MyActionDb.CHECK_STATUS_TRANSFER);
        mad.setChecker(privilege.getUser(request));
        //解决指派备注乱码问题 ajax
        String result = request.getParameter("cwsWorkflowResult");//ParamUtil.get(request, "cwsWorkflowResult");
        mad.setResult(result);
        boolean re = mad.save();
        if (re) {
            mad.onChecked();
        	
            long privMyActionId = mad.getPrivMyActionId();
            MyActionDb privMyActionDb = new MyActionDb();
            privMyActionDb = privMyActionDb.getMyActionDb(privMyActionId);

            long privActionId = privMyActionDb.getActionId();
            WorkflowActionDb privWa = new WorkflowActionDb();
            privWa = privWa.getWorkflowActionDb((int) privActionId);

            WorkflowActionDb wa = new WorkflowActionDb();
            wa = wa.getWorkflowActionDb((int) mad.getActionId());
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb((int)mad.getFlowId());
            MyActionDb nextMyActionDb = wf.notifyUser(toUserName, new java.util.Date(), mad.getPrivMyActionId(), privWa, wa, WorkflowActionDb.STATE_TRANSFERED, mad.getFlowId());
            if (re) {
                boolean isUseMsg = ParamUtil.get(request, "isUseMsg").equals("true");
                boolean isToMobile = ParamUtil.get(request, "isToMobile").equals("true");

                Config cfg = new Config();
                IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
                boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
                String charset = Global.getSmtpCharset();
                cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
                String senderName = StrUtil.GBToUnicode(Global.AppName);
                senderName += "<" + Global.getEmail() + ">";
                if (flowNotifyByEmail) {
                    String mailserver = Global.getSmtpServer();
                    int smtp_port = Global.getSmtpPort();
                    String name = Global.getSmtpUser();
                    String pwd_raw = Global.getSmtpPwd();
                    boolean isSsl = Global.isSmtpSSL();
                    try {
                        sendmail.initSession(mailserver, smtp_port, name, pwd_raw, "", isSsl);
                    } catch (Exception ex) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                    }
                }

                com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

                String t = SkinUtil.LoadString(request, "res.module.flow", "msg_user_actived_title");
                String c = SkinUtil.LoadString(request, "res.module.flow", "msg_user_actived_content");
                MessageDb md = new MessageDb();

                t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
                fc = fc.replaceFirst("\\$fromUser", wa.getUserRealName());

                if (isToMobile) {
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    if (imu != null) {
                        if (mqIsOpen) {
                            msgProducer.sendSms(nextMyActionDb.getUserName(), fc);
                        }
                        else {
                            UserDb ud = new UserDb();
                            ud = ud.getUserDb(nextMyActionDb.getUserName());
                            imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                        }
                    }
                }

                fc += WorkflowMgr.getFormAbstractTable(wf);
                if (isUseMsg) {
                    // 发送信息
                    String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + nextMyActionDb.getId();
                    if (mqIsOpen) {
                        msgProducer.sendSysMsg(nextMyActionDb.getUserName(), t, fc, action);
                    }
                    else {
                        md.sendSysMsg(nextMyActionDb.getUserName(), t, fc, action);
                    }
                }

                if (flowNotifyByEmail) {
                    UserMgr um = new UserMgr();
                    UserDb user = um.getUserDb(nextMyActionDb.getUserName());
                    if (!user.getEmail().equals("")) {
                        String action = "userName=" + user.getName() + "|" + "myActionId=" + nextMyActionDb.getId();
                        action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
                        fc += "<BR />>>&nbsp;<a href='" + Global.getFullRootPath(request) +
                                "/public/flow_dispose.jsp?action=" + action + "' target='_blank'>请点击此处办理</a>";
                        if (mqIsOpen) {
                            msgProducer.sendEmail(user.getEmail(), senderName, t, fc);
                        }
                        else {
                            sendmail.initMsg(user.getEmail(), senderName, t, fc, true);
                            sendmail.send();
                            sendmail.clear();
                        }
                    }
                }
            }
        }
        return re;
    }

    /**
     * 挂起
     * @param request HttpServletRequest
     * @param myActionId long
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean suspend(HttpServletRequest request, long myActionId) throws ErrMsgException {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        if (!mad.isLoaded()) {
            throw new ErrMsgException("该待办流程已不存在，可能已被删除或者撤回！");
        }
        mad.setCheckStatus(MyActionDb.CHECK_STATUS_SUSPEND);
        return mad.save();
    }

    /**
     * 挂起恢复
     * @param request HttpServletRequest
     * @param myActionId long
     * @return boolean
     * @throws ErrMsgException
     */
    public long resume(HttpServletRequest request, long myActionId) throws ErrMsgException {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        if (!mad.isLoaded()) {
            throw new ErrMsgException("该待办流程已不存在，可能已被删除或者撤回！");
        }
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        return mad.resume(userName);
    }


    /**
     * 在flow_initiate1_do.jsp中发起流程时生成流程标题，替换部门简称{dept}及发起人姓名{user}{date:format},format默认为yyyyMMdd
     * 去掉{fieldCode}或{fieldTitle}，因为在流程发起时表单域还没有替换
     * @param request HttpServletRequest
     * @param pvg Privilege
     * @param lf Leaf
     * @return String
     */
    public static String makeTitle(HttpServletRequest request, Privilege pvg, Leaf lf) {
        return makeTitle(request, pvg, lf, true);
    }  
    
    /**
     * 生成标题（用于任务分配时自动发起流程的标题）
     * @Description: 
     * @param request
     * @param userName
     * @param lf
     * @param isClearField
     * @return
     */
    public static String makeTitle(HttpServletRequest request, String userName, Leaf lf, boolean isClearField) {
    	String title = lf.getName(request);
        // 如果设置了默认标题
        if (lf.getDescription() != null && !lf.getDescription().equals("")) {
            title = lf.getDescription();
            if (title.indexOf("{dept}") != -1) {
                String deptShortName;
                DeptUserDb dud = new DeptUserDb();
                Iterator ir = dud.getDeptsOfUser(userName).iterator();
                if (ir.hasNext()) {
                    DeptDb dd = (DeptDb) ir.next();
                    deptShortName = dd.getShortName(); // 发起人所在部门的简称
                    if (deptShortName==null || deptShortName.equals(""))
                        deptShortName = dd.getName();
                    title = title.replaceAll("\\{dept\\}", deptShortName);
                }
            }

            if (title.indexOf("{user}") != -1) {
                UserDb user = new UserDb();
                user = user.getUserDb(userName);
                title = title.replaceAll("\\{user\\}", user.getRealName());
            }

            int p = title.indexOf("{date");
            if (p != -1) {
                int q = title.indexOf("}", p);
                String d = title.substring(p, q);
                String[] ary = d.split(":");
                String format = "yyyyMMdd";
                if (ary.length == 2) {
                    format = ary[1];
                }
                try {
                	d = DateUtil.format(new java.util.Date(), format);
                }
                catch(Exception e) {
                	LogUtil.getLog(WorkflowMgr.class).error(e.getMessage());
                }
                title = title.substring(0, p) + d + title.substring(q + 1);
            }
            
            if (isClearField) {
	            // 去掉{fieldCode}{fieldTitle}
	            title = title.replaceAll("\\{([A-Z0-9a-z_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", "");
            }
        }
        return title;    	
    }    
    
    /**
     * 在saveWorkflowProps中调用，以替换 {fieldCode}或{fieldTitle}
     * @param request HttpServletRequest
     * @param pvg Privilege
     * @param lf Leaf
     * @return String
     */    
    public static String makeTitle(HttpServletRequest request, Privilege pvg, Leaf lf, boolean isClearField) {
        String userName = pvg.getUser(request);
        return makeTitle(request, userName, lf, isClearField);
    }        
    
    /**
     * 在FinishAction、FinishActionFree中调用，保存流程状态、标题等
     * @param request
     * @param wf
     * @param fu
     * @return
     */
    public boolean saveWorkflowProps(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb wa, FileUpload fu) {
        // 置流程等级
        int level = StrUtil.toInt(fu.getFieldValue("cwsWorkflowLevel"), WorkflowDb.LEVEL_NORMAL);
        wf.setLevel(level);

        // 如果流程原来的状态为无效，则改为未开始（在发起节点上保存草稿）
        if (wf.getStatus()==WorkflowDb.STATUS_NONE) {
            wf.setStatus(WorkflowDb.STATUS_NOT_STARTED);
            
            // 当保存草稿时，添加待办记录
            Config cfg = new Config();
            String flowActionPlan = cfg.get("flowActionPlan");
            String flowActionPlanContent = cfg.get("flowActionPlanContent");
            PlanDb pd = new PlanDb();
            pd.setTitle(StrUtil.format(flowActionPlan, new Object[]{wf.getTitle()}));
            UserMgr um = new UserMgr();
            UserDb user = um.getUserDb(wf.getUserName());
            String cont = StrUtil.format(flowActionPlanContent, new Object[]{user.getRealName(), user.getRealName()});
            pd.setContent(cont);
            pd.setMyDate(new java.util.Date());
            pd.setEndDate(new java.util.Date());
            
            MyActionDb mad = new MyActionDb();
            mad = mad.getFirstMyActionDbOfFlow(wf.getId());
            pd.setActionData("" + mad.getId());
            pd.setActionType(PlanDb.ACTION_TYPE_FLOW);
            pd.setUserName(wf.getUserName());
            pd.setRemind(false);
            pd.setRemindBySMS(false);
            pd.setRemindDate(new java.util.Date());
            try {
				pd.create();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
			
        	if (wa.isDistribute()) {
        		// 如果是分发节点，则置该节点状态为允许分发，用于公文分发列表
        		wa.setCanDistribute(true);
                try {
					wa.save();
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
        	}
        }
        
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        
        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());
        
        // 上传的标题，在发起流程时，已经通过makeTitle生成
        String title = fu.getFieldValue("cwsWorkflowTitle");
        
		// 如果默认标题不为空，则替换默认标题，即当存在默认标题时，用户自己不能改标题，只能自动生成
		if (lf.getDescription() != null &&  !lf.getDescription().equals("")) {
			Privilege pvg = new Privilege();
			// 生成的标题
			String mkTitle = makeTitle(request, pvg, lf, false);

			// 将标题中的{fildName}或{fieldTitle}，默认标题
			Pattern p = Pattern.compile(
					"\\{([A-Z0-9a-z_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
					Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(mkTitle);
			StringBuffer sb = new StringBuffer();

			boolean isFound = false;
			while (m.find()) {
				isFound = true;
				String fieldTitle = m.group(1);
				// 制作大亚表单时发现，差旅费报销单中字段名称会有重复，所以这里先找编码，不行再找名称，防止名称重复
				FormField field = fd.getFormField(fieldTitle);
				if (field == null) {
					field = fd.getFormFieldByTitle(fieldTitle);
					if (field == null) {
						LogUtil.getLog(getClass()).error(
								"saveWorkflowProps中流程：" + wf.getTitle()
										+ " 表单：" + fd.getName() + "，字段："
										+ fieldTitle + " 不存在！");
						continue;
					}
				}
				String v = StrUtil.getNullString(fu.getFieldValue(field.getName()));
				
				if (field.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlMgr mm = new MacroCtlMgr();
					MacroCtlUnit mu = mm.getMacroCtlUnit(field.getMacroType());
					v = mu.getIFormMacroCtl().converToHtml(request, field, v);
				}
				
				// 如果不为空则替换，否则当保存草稿时，该表单域可能为空
				// if (!"".equals(v))
					m.appendReplacement(sb, v);
			}
			m.appendTail(sb);

			if (isFound) {
                title = sb.toString();
            }
        }
		else {
			if (lf.getType() == Leaf.TYPE_FREE) {
				WorkflowPredefineDb wpd = new WorkflowPredefineDb();
				wpd = wpd.getPredefineFlowOfFree(lf.getCode());
				// 如果是@流程，则取cwsWorkflowResult的摘要
				if (wpd.isLight()) {
					String str = fu.getFieldValue("cwsWorkflowResult");
					
					int len = 50;
					boolean isImg = false;
					title = StrUtil.getAbstract(request, str, len, " ", isImg);
					
					// 取摘要的时候，&nbsp;会被保留，因此需替换
					title = title.replaceAll("&nbsp;", " ");
					
					// 如果有附件，则把附件名称也贴上去
					Iterator ir = fu.getFiles().iterator();
					if (ir.hasNext()) {
						FileInfo fi = (FileInfo)ir.next();
						title += " [附件：" + fi.getName() + "]";
					}					
					
				}
			}
		}
        
        wf.setTitle(title);
        
        return wf.save();
    }

    public static String makeTitleWithField(HttpServletRequest request, WorkflowDb wf, FormDAO fdao, String title) {
        // 将标题中的{fildName}或{fieldTitle}，默认标题
        Pattern p = Pattern.compile(
                "\\{([A-Z0-9a-z_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(title);
        StringBuffer sb = new StringBuffer();

        FormDb fd = fdao.getFormDb();
        boolean isFound = false;
        while (m.find()) {
            isFound = true;
            String fieldTitle = m.group(1);
            // 制作大亚表单时发现，差旅费报销单中字段名称会有重复，所以这里先找编码，不行再找名称，防止名称重复
            FormField field = fd.getFormField(fieldTitle);
            if (field == null) {
                field = fd.getFormFieldByTitle(fieldTitle);
                if (field == null) {
                    LogUtil.getLog(WorkflowMgr.class).error(
                            "saveWorkflowProps中流程：" + wf.getTitle()
                                    + " 表单：" + fd.getName() + "，字段："
                                    + fieldTitle + " 不存在！");
                    continue;
                }
            }
            String v = StrUtil.getNullString(fdao.getFieldValue(field.getName()));

            if (field.getType().equals(FormField.TYPE_MACRO)) {
                MacroCtlMgr mm = new MacroCtlMgr();
                MacroCtlUnit mu = mm.getMacroCtlUnit(field.getMacroType());
                v = mu.getIFormMacroCtl().converToHtml(request, field, v);
            }

            // 如果不为空则替换，否则当保存草稿时，该表单域可能为空
            // if (!"".equals(v))
            m.appendReplacement(sb, v);
        }
        m.appendTail(sb);

        if (isFound) {
            return sb.toString();
        }
        else {
            return title;
        }
    }

    /**
     * 置异或发散的分支线，不满足条件的分支线上的节点置为被忽略，满足条件的分支线上的节点置为STATE_NOTDO
     * @param myAction WorkflowActionDb
     * @param XorNextActionInternalNames String
     * @throws ErrMsgException
     */
    public void setXorRadiateNextBranch(WorkflowActionDb myAction, String XorNextActionInternalNames) throws ErrMsgException {
        if (myAction.isXorRadiate()) {
            LogUtil.getLog(getClass()).info("XorNextActionInternalName=" +
                                            XorNextActionInternalNames + " :" +
                                            myAction.getTitle());
            if (XorNextActionInternalNames.equals("")) { // NODE_SELECTED在flow_dispose.jsp表示存在被忽略的节点，可以认为已有后继节点被选中
                throw new ErrMsgException("请选择后续节点或检查是否本节点设为了条件分支但未设置条件");
            } else {
                String[] selectedNames = StrUtil.split(XorNextActionInternalNames, ",");
                int selLen = selectedNames.length;
                Vector vto = myAction.getLinkToActions();
                Iterator ir = vto.iterator();
                while (ir.hasNext()) {
                    WorkflowActionDb wad = (WorkflowActionDb) ir.next();
                    boolean isFound = false;
                    for (int i = 0; i < selLen; i++) {
                        if (wad.getInternalName().equals(selectedNames[i])) {
                            isFound = true;
                            // 2009.5.31异或发散时，如果下一节点打回，而此时另有分支首节点为被忽略状态，当该节点被重新选中时，需重置该分支首点的状态为未办理状态
                            // 以便在afterChangeStatus方法中对该节点进行处理
                            // 2012.2.6还有一种情况，详见《20120206条件分支上节点被忽略后再被激活.doc》
                            if (wad.getStatus() == WorkflowActionDb.STATE_IGNORED) {
                                wad.setStatus(WorkflowActionDb.STATE_NOTDO);
                                wad.save();
                            }
                            break;
                        }
                    }
                    // 如果在选择中的分支中未找到该节点，则认为该节点及后续节点被忽略
                    if (!isFound) {
                        // 如果不是审阅分支，则忽略
                        // if (wad.getKind()==WorkflowActionDb.KIND_READ)
                    	for (String selectedName : selectedNames) {
	                    	WorkflowActionDb selectedAction = wad.getWorkflowActionDbByInternalName(selectedName, wad.getFlowId());
	                    	// 取得被忽略的分支线节点与被选中节点之间存在的相交节点
	                    	WorkflowActionDb endAction = getRelationOfTwoActions(wad, selectedAction);
	                    	if (endAction!=null) {
                                // 如果相交节点不是被选中节点本身，则执行忽略操作
                                LogUtil.getLog(getClass()).info("selectedAction title=" + selectedAction.getTitle() + " endAction title=" + endAction.getTitle());
                                if (!endAction.getInternalName().equals(selectedName)) {
                                    ignoreBranch(wad, endAction);
                                }
                                else {
                                    ignoreBranch(wad, wad);
                                }
                            }
	                    	else {
                                LogUtil.getLog(getClass()).info("endAction=null " + wad.getTitle() + "->" + selectedAction.getTitle());
                                ignoreBranch(wad, wad);
                            }
                    	}
                    }
                }
            }
        }
    }
    
    /**
     * 取得两个节点之间的连接点，会包含firstAction或secondAction本身
     * @Description: first/second在一条线上,则谁在后返回谁,不在一条线上,如果有交点则返回交点,没有则返回null
     * @param firstAction WorkflowActionDb
     * @param secondAction WorkflowActionDb
     * @return
     */
    public WorkflowActionDb getRelationOfTwoActions(WorkflowActionDb firstAction, WorkflowActionDb secondAction) {
    	Vector v1 = new Vector();
    	HashMap<String ,Boolean> map1 = new HashMap<String, Boolean>();
    	v1 = getALLFollowupActions(firstAction, v1, map1);
    	Vector v2 = new Vector();
    	HashMap<String ,Boolean> map2 = new HashMap<String, Boolean>();
    	v2 = getALLFollowupActions(secondAction, v2, map2);
    	Iterator it1 = v1.iterator();
    	if (it1.hasNext()) {
	    	while (it1.hasNext()) {
	    		WorkflowActionDb wa1 = (WorkflowActionDb) it1.next();
	    		if (wa1.getInternalName().equals(secondAction.getInternalName())) {
	    			return secondAction;
	    		}
	    		Iterator it2 = v2.iterator();
	    		while (it2.hasNext()) {
	    			WorkflowActionDb wa2 = (WorkflowActionDb) it2.next();
	    			if (wa2.getInternalName().equals(firstAction.getInternalName())) {
	    				return firstAction;
	    			}
	    			if (wa1.getInternalName().equalsIgnoreCase(wa2.getInternalName())) {
	    				return wa1;
	    			}
	    		}
	    	}
    	} else {
    		Iterator it2 = v2.iterator();
    		while (it2.hasNext()) {
	    		WorkflowActionDb wa2 = (WorkflowActionDb) it2.next();
	    		if (wa2.getInternalName().equals(firstAction.getInternalName())) {
	    			return firstAction;
	    		}
	    	}
	    }
    	return null;
    }
    
    private Vector getALLFollowupActions(WorkflowActionDb wfa, Vector v, HashMap<String, Boolean> map) {
    	if (map.containsKey(wfa.getInternalName())) {
    		// 防止循环流程
    		return v;
    	} else {
    		map.put(wfa.getInternalName(), true);
    	}
    	Vector nextActions = wfa.getLinkToActions();
    	v.addAll(nextActions);
    	Iterator it = nextActions.iterator();
    	while (it.hasNext()) {
    		WorkflowActionDb wa = (WorkflowActionDb) it.next();
    		getALLFollowupActions(wa ,v, map);
    	}
    	return v;
    }

    /**
     * 锁定流程，只允许userName处理
     * @param wf WorkflowDb
     * @param userName String
     */
    public void lock(WorkflowDb wf, String userName) {
        wf.lock(userName);
    }

    /**
     * 解锁流程，当流程节点处理完时解锁流程
     * @param flowId int
     */
    public void unlock(int flowId) {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        wf.setLocker("");
        wf.save();
    }

    /**
     * 判断能否提交，即锁是否为本人锁的
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean checkLock(HttpServletRequest request, WorkflowDb wf) throws ErrMsgException {
        if (wf.getLocker().equals(""))
            return true;

        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        if (wf.getLocker().equals(userName))
            return true;
        else {
            UserDb user = new UserDb();
            user = user.getUserDb(wf.getLocker());
            throw new ErrMsgException(user.getRealName() + "正在审批处理中，请重新打开再审批！");
        }
    }
    
   	public BSHShell runDeliverScript(HttpServletRequest request, String curUserName, WorkflowDb wf, FormDAO fdao, MyActionDb mad, String script, boolean isTest) throws ErrMsgException {
	   	BSHShell bs = new BSHShell();
		StringBuffer sb = new StringBuffer();
		
		BeanShellUtil.setFieldsValue(fdao, sb);

		// 赋值用户
		sb.append("String userName=\"" + curUserName
				+ "\";");
		sb.append("int flowId=" + wf.getId() + ";");

        bs.set(ConstUtil.SCENE, ConstUtil.SCENE_FLOW_ACTION_FINISH);

        bs.set("fdao", fdao);
		bs.set("mad", mad);
		bs.set("request", request);
		bs.set("fileUpload", fu);

		bs.eval(BeanShellUtil.escape(sb.toString()));

		bs.eval(script);
		Object obj = bs.get("ret");
		if (obj != null) {
			boolean ret = ((Boolean) obj).booleanValue();
			if (!ret) {
				String errMsg = (String) bs.get("errMsg");
				LogUtil.getLog(getClass()).error(
						"bsh errMsg=" + errMsg);
			}
		}

		return bs;
   	}    

    /**
     * 处理完毕节点
     * @param request HttpServletRequest
     * @param wf WorkflowDb 流程
     * @param myAction WorkflowActionDb 当前正在处理的WorkflowActionDb
     * @param myActionId long 当前的待办记录
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean FinishAction(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction, long myActionId) throws ErrMsgException {
    	Config cfg = new Config();
    	long tDebug = System.currentTimeMillis();

        // 如果流程是被删除状态，则不允许处理
    	if (wf.getStatus()==WorkflowDb.STATUS_DELETED) {
    		String str = LocalUtil.LoadString(request,"res.flow.Flow","flowDeleted");
    		throw new ErrMsgException(str);
    	}

    	Privilege pvg = new Privilege();
    	String userName = pvg.getUser(request);
    	
    	checkLock(request, wf);
        
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        if (!mad.isLoaded()) {
        	String str = LocalUtil.LoadString(request, "res.flow.Flow","toDoProcess");
            throw new ErrMsgException(str);
        }

    	WorkflowPredefineDb wfp = new WorkflowPredefineDb();
    	wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

        canSubmit(request, wf, myAction, mad, userName, wfp);

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());

        boolean isAltering = false;
    	
        // 防止因为浏览器卡住而多次提交，下一个节点产生多个待办记录
        if (!(myAction.getStatus()==WorkflowActionDb.STATE_DOING || myAction.getStatus()==WorkflowActionDb.STATE_RETURN)) {
        	// 有可能会是重激活的情况，或者是异或聚合的情况
        	if (!wfp.isReactive() && !myAction.isXorAggregate()) {
        		String str1 = LocalUtil.LoadString(request, "res.flow.Flow","mayHaveBeenProcess");
				throw new ErrMsgException(str1);
        	}
        	else {
        	    if (wfp.isReactive()) {
                    // 如果流程状态为已结束，而因为重激活的原因又启动了，则需重置流程状态
                    if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
                        isAltering = true;

                        wf.setStatus(WorkflowDb.STATUS_STARTED);
                        wf.setResultValue(WorkflowActionDb.RESULT_VALUE_NOT_ACCESSED);
                        wf.setAlter(true);
                        wf.setAlterTime(new java.util.Date());
                        wf.setAlterUser(userName);
                        wf.save();

                        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                        FormDb fd = new FormDb();
                        fd = fd.getFormDb(lf.getFormCode());
                        fdao = fdao.getFormDAO(wf.getId(), fd);
                        fdao.setStatus(FormDAO.STATUS_NOT);
                        fdao.save();
                    }
                }
            }
        }
        
        // 如果是套红节点，则检查是否已套红
        if (myAction.canReceiveRevise()) {
	        Document doc = new Document();
	        doc = doc.getDocument(wf.getDocId());
	        if (doc.getTemplateId()==Document.NOTEMPLATE) {
	        	// 请先套红
	        	String str = LocalUtil.LoadString(request, "res.flow.Flow","err_template");
	        	throw new ErrMsgException(str);
	        }
        }

        // 如果流程未开始，则保存流程的标题等
        if (!wf.isStarted()) {
            saveWorkflowProps(request, wf, myAction, fu);
        }

        // 用于指派直送操作
        String flowAction = StrUtil.getNullStr(fu.getFieldValue("flowAction"));
        request.setAttribute("flowAction", flowAction);

        UserMgr um = new UserMgr();
        UserDb user = null;
        WorkflowActionDb wfa = null;

        // 加签节点不需要检查后续处理用户
        if (mad.getActionStatus() != WorkflowActionDb.STATE_PLUS) {
            // 如果是异或节点，则检查是否有被选中的后继节点
            String XorNextActionInternalNames = "";
            // 判断是否来自手机端
			boolean isMobile = false;
            OperatingSystem os = null;
            try {
                // 当从第三方接口对接时，因没有User-Agent，会导致出现异常
                UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
                os = ua.getOperatingSystem();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
	        if(os==null || DeviceType.MOBILE.equals(os.getDeviceType())) {
	            isMobile = true;
	        }
	        if (isMobile) {
	        	// 手机端传来的XorNextActionInternalNames有可能会重复，因为手机端当存在条件时，显示全部符合条件的用户，在拼接时，会根据选择的用户属性拼接XorNextActionInternalNames，所以可能重复
	        	String[] ary = fu.getFieldValues("XorNextActionInternalNames");
	        	if (ary!=null) {
                    for (String s : ary) {
                        if (XorNextActionInternalNames.equals("")) {
                            XorNextActionInternalNames = s;
                        } else {
                            // 防止重复
                            String tmpString = "," + XorNextActionInternalNames + ",";
                            if (!tmpString.contains("," + s + ",")) {
                                XorNextActionInternalNames += "," + s;
                            }
                        }
                    }
	        	}
	        }
	        else {
	            XorNextActionInternalNames = StrUtil.getNullString(fu.getFieldValue("XorNextActionInternalNames"));
	        }
	        
			if (XorNextActionInternalNames.contains(",")) {
				WorkflowPredefineDb wpd = new WorkflowPredefineDb();
				wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());	        
				int branchMode = StrUtil.toInt(WorkflowActionDb.getActionProperty(wpd, myAction.getInternalName(), "branchMode"), WorkflowActionDb.BRANCH_MODE_MULTI);
				if (branchMode == WorkflowActionDb.BRANCH_MODE_SINGLE) {
					throw new ErrMsgException("只能选择一条分支");
				}
			}	   	        
            
            LogUtil.getLog(getClass()).info("FinishAction XorNextActionInternalNames=" + XorNextActionInternalNames);
            // 置异或发散的分支线
            setXorRadiateNextBranch(myAction, XorNextActionInternalNames);
            
	        // 更新后续节点上选择的用户名和用户真实姓名，以便转交给下一节点用户
	        String prefix = "WorkflowAction_";
	        Vector<WorkflowActionDb> vto = myAction.getLinkToActions();
	        Iterator<WorkflowActionDb> irto = vto.iterator();
	        boolean isRecycle = false;
	        // 判断是否为回路
	        while (irto.hasNext() && !isRecycle) {
	        	wfa = irto.next();
	        	// 根据priv_myaction_id获取前置节点,判断前置节点是否和后继节点为同一节点,从而判断是否为回路
            	long prevMyActionId = mad.getPrivMyActionId();
            	if (prevMyActionId != -1) {
            		MyActionDb prevMad = new MyActionDb(prevMyActionId);
            		if (prevMad.isLoaded()) {
            			WorkflowActionDb prevWad = new WorkflowActionDb((int) prevMad.getActionId());
            			if (prevWad.isLoaded()) {
            				if (prevWad.getInternalName().equals(wfa.getInternalName())) {
            					isRecycle = true;
            					break;
            				}
            			}
            		}
            	}
            	
            	// 按理说上面的逻辑是可以完全解决两点之间回路问题的
            	if (wfa.getUserName() != null && !wfa.getUserName().equals("")) {
	            	Vector from = myAction.getLinkFromActions();
                    for (Object o : from) {
                        WorkflowActionDb waDb = (WorkflowActionDb) o;
                        if (waDb.getInternalName().equals(wfa.getInternalName())) {
                            isRecycle = true;
                            break;
                        }
                    }
            	}
            	
            	// 跨节点的回路
            	if (wfa.getUserName() != null && !"".equals(wfa.getUserName())) {
            		WorkflowActionDb tempWfa = getRelationOfTwoActions(myAction, wfa);
            		// 20170504 fgf 之前已经判断了两个节点是否直接相连并存在回路
            		// 此处只需判断是否存在其它相交的共同节点
            		if (tempWfa != null) {
            			if (!(tempWfa.getInternalName().equals(myAction.getInternalName())
            					|| tempWfa.getInternalName().equals(wfa.getInternalName()))) {
            				isRecycle = true;
            				break;
            			}
            		}
            	}
	        }
	        
	        irto = vto.iterator();
	        while (irto.hasNext()) {
	            wfa = irto.next();

 				boolean isUserSelectable = true;
	            String strategy = wfa.getStrategy();
	            IStrategy ist = null;
	            if (!"".equals(strategy)) {
	                StrategyMgr sm = new StrategyMgr();
	                StrategyUnit su = sm.getStrategyUnit(strategy);
	                ist = su.getIStrategy();
	                // 策略是否可以选人
                    // 如果是自选用户，则忽略策略是否可以选人
                    if (!wfa.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT)) {
                        isUserSelectable = ist.isSelectable();
                    }
	            }	            

	            // 循环节点或者返回节点或者重激活清空原来选择的人员
	            if (isUserSelectable) {
		            if (isRecycle || myAction.getStatus() == WorkflowActionDb.STATE_RETURN || wfp.isReactive()) {
		            	wfa.setUserName("");
		            	wfa.setUserRealName("");
		            	wfa.save();
		            }
	            }
	            
	            // 置节点上选择的用户名
	            String uNames = "", uRealNames = "";
	            String[] userNames = fu.getFieldValues(prefix + wfa.getId());
	            
                // 注意此处，如果放在if (wfa.getStatus() == WorkflowActionDb.STATE_IGNORED)之后将不起作用，因为wfa所在的分支在流程处理时此分支没有选人，分支没被选中，所以节点将会被忽略
                if (wfa.getStatus() != WorkflowActionDb.STATE_IGNORED) {
                    if (ist != null) {
                        // 策略校验
                        ist.validate(request, wf, myAction, myActionId, fu, userNames, wfa);
                    }
                }

	            if (wfa.getStatus() == WorkflowActionDb.STATE_IGNORED) {
	            	// 返回后的非条件分支是可以走忽略节点的(这里的返回可以是该节点,也可以是前面的节点)
	            	if (!myAction.isXorRadiate()) {
	            		boolean isReturn = false;
	            		if (myAction.getStatus() == WorkflowActionDb.STATE_RETURN) {
	            			isReturn = true;
	            		} else {
		            		long prevMyActionId = mad.getPrivMyActionId();
	            			while (!isReturn && prevMyActionId != -1) {
	            				MyActionDb prevMad = new MyActionDb(prevMyActionId);
	            				prevMyActionId = prevMad.getPrivMyActionId();
	            				if (prevMad.isLoaded() && prevMad.getCheckStatus() == MyActionDb.CHECK_STATUS_RETURN) {
	            					isReturn = true;
	            				}
	            			}
	            		}
	            		if (!isReturn) {
	            		    // 判断是否为唯一的后续节点，如果是，则允许继续往下流转，如多起点，起点分支聚合至某一节点时
                           if (vto.size()!=1) {
                               continue;
                           }
	            		}
	            	} else {
	            		continue;
	            	}
	            }
	                      
	            LogUtil.getLog(getClass()).info("FinishAction: prefix + wfa.getId()=" + prefix + wfa.getId());
	            int len = 0;
	            if (userNames!=null) {
	                len = userNames.length;
	            } else {
	            	continue;
	            }
	            
	            for (int i=0; i<len; i++) {
	            	// 2013-08-01 android客户端传过来以后，userNames[i]之前会有换行符，所以需trim
	                user = um.getUserDb(userNames[i].trim());
	                String userRealName = user.getRealName();
	                if ("".equals(uNames)) {
	                    uNames = userNames[i].trim();
	                    uRealNames = userRealName;
	                }
	                else {
	                    uNames += "," + userNames[i].trim();
	                    uRealNames += "," + userRealName;
	                }
	            }
	
	            LogUtil.getLog(getClass()).info("FinishAction: " + prefix + wfa.getId() + "=" + prefix + wfa.getId() + " uNames=" + uNames + "--wfa.getUserName()=" + wfa.getUserName());

	            if (isUserSelectable) {
                	// 如果不相等，则说明选择的用户有变化
    	            if (!wfa.getUserName().equals(uNames)) {
    	                int status = wfa.getStatus();
    	                // 如果下一节点为异步退回或当前节点为异步提交模式，而下一节点状态为办理中，则允许更改选择下一节点用户
    	                if (!wfa.isXorReturn() && !myAction.isXorFinish() && status == WorkflowActionDb.STATE_DOING) {
    	                	String str = LocalUtil.LoadString(request, "res.flow.Flow","action");
    	                	String str1 = LocalUtil.LoadString(request, "res.flow.Flow","canNotEdit");
    	                    throw new ErrMsgException(str + wfa.getTitle() + str1);
    	                }
    	                
    	                // 20180929 fgf 应以选择的用户为准，故注释掉上面的部分
		                wfa.setUserName(uNames);
		                wfa.setUserRealName(uRealNames);
    	                wfa.save();
    	
    	                LogUtil.getLog(getClass()).info("FinishAction: " + "wfa.getUserName()=" + wfa.getUserName());
    	            }
	            }
	        }

	        if (isMobile) {
	            FormDb fd = new FormDb();
	            fd = fd.getFormDb(lf.getFormCode());
	            // 手机端提交流程时，重新对节点上的用户进行匹配
	            WorkflowActionDb.reMatchUserOnMobileFinish(request, fd, myAction);
            }
        }

        Privilege privilege = new Privilege();
        FormDAOMgr fdm = new FormDAOMgr(wf.getId(), lf.getFormCode(), fu, myAction);

        if (cfg.getBooleanProperty("isDebugFlow")) {
            DebugUtil.i(getClass(), "finishAction", "flowId:" + wf.getId() + " " + wf.getTitle() + " before update: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
            tDebug = System.currentTimeMillis();
        }

        boolean isAfterSaveformvalueBeforeXorCondSelect = StrUtil.getNullStr(fu.getFieldValue("isAfterSaveformvalueBeforeXorCondSelect")).equals("true");
        LogUtil.getLog(getClass()).info("FinishAction: isAfterSaveformvalueBeforeXorCondSelect=" + isAfterSaveformvalueBeforeXorCondSelect);
        boolean re = true;
        // 如果不是在条件选择后提交，则保存表单，否则因为表单在条件匹配之前已AJAX保存，这儿再次保存，会致附件出现重复
        if (!isAfterSaveformvalueBeforeXorCondSelect) {
            re = fdm.update(request);
        }

        if (re) {
            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction", "flowId:" + wf.getId() + " " + wf.getTitle() + " after update: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
                tDebug = System.currentTimeMillis();
            }

            mad.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);
            mad.setIp(IPUtil.getRemoteAddr(request));
            String ua = request.getHeader("User-Agent");
            mad.setOs(UserAgentParser.getOS(ua));
            mad.setBrowser(UserAgentParser.getBrowser(ua));
            // 是否在变更
            if (isAltering) {
                mad.setAlter(true);
                mad.setAlterTime(new java.util.Date());
            }
            mad.save();

            String result = "";
            int resultValue = 0;
            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction", "flowId:" + wf.getId() + " " + wf.getTitle() + " before changeStatus: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
                tDebug = System.currentTimeMillis();
            }
            // 检查流程是否已开始，如果未开始且用户有开始流程的权限，则开始流程
            if (!wf.isStarted()) {
                re = start(request, wf.getId(), myAction, myActionId);
            }
            else {
                re = myAction.changeStatus(request, wf,
                        privilege.getUser(request),
                        WorkflowActionDb.STATE_FINISHED,
                        "", result, resultValue, myActionId);
            }
        } else{
        	String str = LocalUtil.LoadString(request, "res.flow.Flow","faileSaveForm");
        	throw new ErrMsgException(str);
        }
        if (re) {
            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction", "flowId:" + wf.getId() + " " + wf.getTitle() + " after changeStatus: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
                tDebug = System.currentTimeMillis();
            }

            // 解锁流程，这里直接保存时，会存在脏数据，因为之前WorkflowActionDb在save时，已经更新了flowString
            // 所以不能以WorkflowDb为参数，而改用flowId
            unlock(wf.getId());

            onFinishAction(request, wf, myAction, lf, mad, tDebug);

            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction", "after 发送流程提醒消息: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
                // tDebug = System.currentTimeMillis();
            }
        }
        return re;
    }

    public void onFinishAction(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction, Leaf lf, MyActionDb mad, long tDebug) throws ErrMsgException {
        Privilege privilege = new Privilege();
        // 流程转交下一步时事件处理插件
        FormValidatorConfig fvc = new FormValidatorConfig();
        IFormValidator ifv = fvc.getIFormValidatorOfForm(lf.getFormCode());
        if (ifv != null && ifv.isUsed()) {
            ifv.onActionFinished(request, wf.getId(), fu);
        }

        Config cfg = Config.getInstance();
        if (cfg.getBooleanProperty("isDebugFlow")) {
            DebugUtil.i(getClass(), "finishAction", "flowId:" + wf.getId() + " " + wf.getTitle() + " after onActionFinished: " + (System.currentTimeMillis() - tDebug) + " ms " + new Privilege().getUser(request));
            tDebug = System.currentTimeMillis();
        }

        // 流程转交下一步时，节点上的事件脚本处理
        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
        String script = wpm.getActionFinishScript(wpd.getScripts(), myAction.getInternalName());
        if (script != null) {
            FormDAO fdao = new FormDAO();
            FormDb fd = new FormDb();
            fd = fd.getFormDb(lf.getFormCode());
            fdao = fdao.getFormDAO(wf.getId(), fd);

            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction", "flowId:" + wf.getId() + " " + wf.getTitle() + " before runDeliverScript: " + (System.currentTimeMillis() - tDebug) + " ms " + new Privilege().getUser(request));
                tDebug = System.currentTimeMillis();
            }

            runDeliverScript(request, privilege.getUser(request), wf, fdao, mad, script, false);

            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction","flowId:" + wf.getId() + " " + wf.getTitle() + " after runDeliverScript: " + (System.currentTimeMillis() - tDebug) + " ms " + new Privilege().getUser(request));
                tDebug = System.currentTimeMillis();
            }
        }

        // 回写表单处理开始
        String writeProp = wpd.getWriteProp();
        if (writeProp!=null && !"".equals(writeProp)){
            try {
                SAXBuilder parser = new SAXBuilder();
                org.jdom.Document docu = parser.build(new InputSource(new StringReader(writeProp)));
                Element root = docu.getRootElement();
                List nodeFinishList = root.getChildren("nodeFinish");
                Iterator ir = null;
                if (nodeFinishList!=null) {
                    ir = nodeFinishList.iterator();
                }
                while (ir!=null&&ir.hasNext()) {
                    Element nodeFinish = (Element)ir.next();
                    String  internalName = nodeFinish.getChildText("internalName");
                    if (internalName.equals(myAction.getInternalName())) {
                        writeBack(wf, lf, nodeFinish);
                    }
                }
            } catch (SQLException | JDOMException e) {
                LogUtil.getLog(getClass()).error("parse writeProp field error1");
                e.printStackTrace();
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error("parse writeProp field error2");
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 回写数据库处理开始
        String writeDbProp = wpd.getWriteDbProp();
        if (writeDbProp!=null && !"".equals(writeDbProp)){
            try {
                SAXBuilder parser = new SAXBuilder();
                org.jdom.Document docu = parser.build(new InputSource(new StringReader(writeDbProp)));
                Element root = docu.getRootElement();
                List nodeFinishList = root.getChildren("nodeFinish");
                Iterator ir = null;
                if (nodeFinishList!=null) {
                    ir = nodeFinishList.iterator();
                }
                while (ir!=null&&ir.hasNext()) {
                    Element nodeFinish = (Element)ir.next();
                    String  internalName = nodeFinish.getChildText("internalName");
                    if (internalName.equals(myAction.getInternalName())){

                        String dbSource = nodeFinish.getChildText("dbSource");
                        String writeBackTable = nodeFinish.getChildText("writeBackForm");
                        StringBuilder sb = new StringBuilder ("");                       				 //用于拼接回写字段
                        StringBuilder cond = new StringBuilder("");                       			//用于拼接条件字段
                        List writeBackFieldList = nodeFinish.getChildren("writeBackField");
                        Element condition = nodeFinish.getChild("condition");
                        List conditionFieldList = null;
                        Iterator conditionIr = null;
                        if (condition!=null) {
                            conditionFieldList = condition.getChildren("conditionField");
                        }
                        if (conditionFieldList!=null) {
                            conditionIr = conditionFieldList.iterator();
                        }
                        while (conditionIr!=null&&conditionIr.hasNext()) {                         //解析条件字段
                            Element conditionField = (Element)conditionIr.next();
                            String conditionFieldName = conditionField.getAttributeValue("fieldName");

                            int fieldType = WorkflowUtil.getColumnType(dbSource, writeBackTable, conditionFieldName);

                            String beginBracket = conditionField.getAttributeValue("beginBracket");
                            String endBracket = conditionField.getAttributeValue("endBracket");
                            String compare = conditionField.getChildText("compare");
                            String compareVal = conditionField.getChildText("compareVal");

                            if (fieldType==FormField.FIELD_TYPE_TEXT||fieldType==FormField.FIELD_TYPE_VARCHAR||fieldType==FormField.FIELD_TYPE_DATE) {
                                compareVal = SQLFilter.sqlstr(compareVal);
                            }

                            String logical = conditionField.getChildText("logical");
                            cond.append(" ").append(beginBracket).append(" ").append(conditionFieldName).append(" ").append(compare).append(" ").append(compareVal).append(" ").append(endBracket).append(" ").append(logical);
                        }
                        String condString = cond.toString();
                        Iterator writeBackFieldIr = null;
                        if (writeBackFieldList!=null) {
                            writeBackFieldIr= writeBackFieldList.iterator();
                        }
                        while (writeBackFieldIr!=null&&writeBackFieldIr.hasNext()) {            //解析回写字段
                            Element writeBackField = (Element)writeBackFieldIr.next();
                            String writeBackFieldStr = writeBackField.getAttributeValue("fieldName");
                            String writeBackMathStr = writeBackField.getChildText("writeBackMath");
                            sb.append(writeBackFieldStr).append(" = ").append(writeBackMathStr).append(",");
                        }
                        String setField = sb.toString();
                        if (!"".equals(setField)) {
                            WorkflowMgr wMgr = new WorkflowMgr();
                            setField = setField.substring(0, setField.lastIndexOf(","));
                            String sql = "update " + writeBackTable + " set " + setField;
                            if (!"".equals(condString)) {
                                condString = condString.substring(0, condString.lastIndexOf(" "));
                                sql += " where "+condString;
                            }
                            sql = wMgr.parseWriteBackDbField(sql);            //过滤{}，用于回写值
                            if (condString.indexOf("{$nest.")==-1) {
                                sql = wMgr.parseAndSetMainFieldValue(lf.getFormCode(), sql, wf.getId());     //过滤{$}，用于条件值
                                JdbcTemplate jt = new JdbcTemplate(dbSource);
                                jt.executeUpdate(sql);
                            }
                            else {
                                // 取得当前主表中的子表中对应字段的值，并赋予
                                List ary = wMgr.parseAndSetNestFieldValue(lf.getFormCode(), sql, wf.getId());     //过滤{$}，用于条件值
                                if (ary.size()==0) {
                                    LogUtil.getLog(getClass()).error("parseAndSetNestFieldValue error");
                                }
                                else {
                                    JdbcTemplate jt = new JdbcTemplate(dbSource);
                                    for (int i=0; i<ary.size(); i++) {
                                        sql = wMgr.parseAndSetMainFieldValue(lf.getFormCode(), (String)ary.get(i), wf.getId());     //过滤{$}，用于条件值
                                        jt.addBatch(sql);
                                    }
                                    jt.executeBatch();
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error("parse writeProp field error");
                e.printStackTrace();
            } catch (JDOMException e) {
                LogUtil.getLog(getClass()).error("parse writeProp field error");
                e.printStackTrace();
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error("parse writeProp field error");
                e.printStackTrace();
            }
        }
        // 回写数据库处理结束

        // 发送消息
        if (myAction.isMsg()) {
            String msgProp = wpd.getMsgProp();
            if (myAction.isMsg() && msgProp != null && !"".equals(msgProp)) {
                try {
                    SAXBuilder parser = new SAXBuilder();
                    org.jdom.Document docu = parser.build(new InputSource(new StringReader(msgProp)));
                    Element root = docu.getRootElement();
                    List actions = root.getChildren("action");
                    Iterator ir = null;
                    if (actions != null) {
                        ir = actions.iterator();
                    }
                    while (ir != null && ir.hasNext()) {
                        Element action = (Element) ir.next();
                        String internalName = action.getAttributeValue("internalName");
                        if (internalName.equals(myAction.getInternalName())) {
                            sendRemindMsg(request, wf, lf, myAction, action);
                        }
                    }
                } catch (JDOMException e) {
                    LogUtil.getLog(getClass()).error("parse msgProp field error");
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).error("parse msgProp field error");
                    e.printStackTrace();
                }
            }
        }

        if (cfg.getBooleanProperty("isDebugFlow")) {
            DebugUtil.i(getClass(), "finishAction", "after 脚本、回写及发送节点上配置的消息: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
            tDebug = System.currentTimeMillis();
        }

        if (cfg.getBooleanProperty("isDebugFlow")) {
            DebugUtil.i(getClass(), "finishAction", "after unlock: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
            tDebug = System.currentTimeMillis();
        }
            /*
            // 自动存档
            String flag = myAction.getFlag();
            if (flag.length() >= 5 && flag.substring(4, 5).equals("2")) {
                String formReportContent = StrUtil.getNullStr(fu.getFieldValue(
                        "formReportContent"));
                saveDocumentArchive(wf, privilege.getUser(request),
                                    formReportContent);
            }
            */

        // 置消息中心中当前处理人当前流程的消息为已读
        MessageDb md = new MessageDb();
        md.setUserFlowReaded(mad.getUserName(), mad.getId());
        if (cfg.getBooleanProperty("isDebugFlow")) {
            DebugUtil.i(getClass(), "finishAction", "after setUserFlowReaded: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
            tDebug = System.currentTimeMillis();
        }
        // 调试模式不发送消息及邮件
        if (!lf.isDebug() && myAction.isMsg()) {
            boolean isUseMsg = getFieldValue("isUseMsg").equals("true");

            // @task: 20170615 fgf 此处IOS端，因为没有发送isToMobile，所以暂置为true
            // boolean isToMobile = getFieldValue("isToMobile").equals("true");
            boolean isToMobile = true;

            boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
            boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
            String charset = Global.getSmtpCharset();
            cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
            String senderName = StrUtil.GBToUnicode(Global.AppName);
            senderName += "<" + Global.getEmail() + ">";
            if (flowNotifyByEmail && !mqIsOpen) {
                String mailserver = Global.getSmtpServer();
                int smtp_port = Global.getSmtpPort();
                String name = Global.getSmtpUser();
                String pwd_raw = Global.getSmtpPwd();
                boolean isSsl = Global.isSmtpSSL();
                try {
                    sendmail.initSession(mailserver, smtp_port, name, pwd_raw, "", isSsl);
                } catch (Exception ex) {
                    LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                }
            }

            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

            String t = SkinUtil.LoadString(request,
                    "res.module.flow",
                    "msg_user_actived_title");
            String c = SkinUtil.LoadString(request,
                    "res.module.flow",
                    "msg_user_actived_content");
            String tail = getFormAbstractTable(wf);

            UserMgr um = new UserMgr();
            IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
            Iterator ir = myAction.getTmpUserNameActived().iterator();
            while (ir.hasNext()) {
                MyActionDb mad2 = (MyActionDb) ir.next();
                t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
                fc = fc.replaceFirst("\\$fromUser", myAction.getUserRealName());

                if (isToMobile) {
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    if (imu != null) {
                        if (mqIsOpen) {
                            msgProducer.sendSms(mad2.getUserName(), fc);
                        }
                        else {
                            UserDb ud = um.getUserDb(mad2.getUserName());
                            imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                        }
                    }
                }

                fc += tail;
                if (isUseMsg) {
                    // 发送信息
                    String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad2.getId();
                    if (mqIsOpen) {
                        msgProducer.sendSysMsg(mad2.getUserName(), t, fc, action);
                    }
                    else {
                        md.sendSysMsg(mad2.getUserName(), t, fc, action);
                    }
                }

                if (flowNotifyByEmail) {
                    UserDb user = um.getUserDb(mad2.getUserName());
                    if (user.getEmail()!=null && !user.getEmail().equals("")) {
                        String action = "userName=" + user.getName() + "|" + "myActionId=" + mad2.getId();
                        action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
                        fc += "<BR />>>&nbsp;<a href='" + Global.getFullRootPath(request) +
                                "/public/flow_dispose.jsp?action=" + action + "' target='_blank'>" +
                                SkinUtil.LoadString(request, "res.module.flow", "link_dispose") + "</a>";
                        if (mqIsOpen) {
                            msgProducer.sendEmail(user.getEmail(), senderName, t, fc);
                        }
                        else {
                            sendmail.initMsg(user.getEmail(), senderName, t, fc, true);
                            sendmail.send();
                            sendmail.clear();
                        }
                    }
                }
            }

            wf = wf.getWorkflowDb(myAction.getFlowId());

            // 如果流程完成了，则通知发起者
            if (wf.getStatus()==WorkflowDb.STATUS_FINISHED) {
                String ft = SkinUtil.LoadString(request, "res.module.flow", "msg_flow_finished_title");
                String fc = SkinUtil.LoadString(request, "res.module.flow", "msg_flow_finished_content");
                ft = StrUtil.format(ft, new String[] {wf.getTitle()});
                fc = StrUtil.format(fc, new String[] {wf.getTitle(), myAction.getUserRealName(), DateUtil.format(myAction.getCheckDate(), "yyyy-MM-dd")});

                if (isToMobile) {
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    if (imu != null) {
                        if (mqIsOpen) {
                            msgProducer.sendSms(wf.getUserName(), fc);
                        }
                        else {
                            UserDb ud = um.getUserDb(wf.getUserName());
                            imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                        }
                    }
                }
                if (isUseMsg) {
                    // 发送信息
                    String action = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + wf.getId();
                    if (mqIsOpen) {
                        msgProducer.sendSysMsg(wf.getUserName(), ft, fc, action);
                    } else {
                        md.sendSysMsg(wf.getUserName(), ft, fc, action);
                    }
                }

                if (flowNotifyByEmail) {
                    UserDb user = um.getUserDb(wf.getUserName());
                    if (user.getEmail()!=null && !user.getEmail().equals("")) {
                        String action = "op=show|userName=" + user.getName() + "|" + "flowId=" + wf.getId();
                        action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
                        fc += "<BR />>>&nbsp;<a href='" + Global.getFullRootPath(request) +
                                "/public/flow_dispose.jsp?action=" + action + "' target='_blank'>" +
                                SkinUtil.LoadString(request, "res.module.flow", "link_show") + "</a>";

                        if (mqIsOpen) {
                            msgProducer.sendEmail(user.getEmail(), senderName, ft, fc);
                        }
                        else {
                            sendmail.initMsg(user.getEmail(), senderName, ft, fc, true);
                            sendmail.send();
                            sendmail.clear();
                        }
                    }
                }
            }

            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction", "after send msg: " + (System.currentTimeMillis()-tDebug) + " ms " + new Privilege().getUser(request));
                tDebug = System.currentTimeMillis();
            }
        }
    }
    
    public void writeBack(WorkflowDb wf, Leaf lf, Element nodeFinish) throws SQLException, JSONException  {
		String writeBackForm = nodeFinish.getChildText("writeBackForm");
		int writeBackType = StrUtil.toInt(StrUtil.getNullStr(nodeFinish.getChildText("writeBackType")), WorkflowPredefineDb.WRITE_BACK_UPDATE);

		// 更新字段
		if (writeBackType==WorkflowPredefineDb.WRITE_BACK_UPDATE || writeBackType==WorkflowPredefineDb.WRITE_BACK_UPDATE_INSERT) {
			FormDb fd = new FormDb();
			fd = fd.getFormDb(writeBackForm);
			
			StringBuilder sb = new StringBuilder ("");
			StringBuilder cond = new StringBuilder("");
			List writeBackFieldList = nodeFinish.getChildren("writeBackField");
			Element condition = nodeFinish.getChild("condition");
			List conditionFieldList = null;
			Iterator conditionIr = null;
			if (condition!=null)
				conditionFieldList = condition.getChildren("conditionField");
			if (conditionFieldList!=null)
				conditionIr = conditionFieldList.iterator();
			// 拼接条件字段
			while (conditionIr!=null&&conditionIr.hasNext()) {                   
				Element conditionField = (Element)conditionIr.next();
				String conditionFieldName = conditionField.getAttributeValue("fieldName");
				FormField ff = fd.getFormField(conditionFieldName);
				String beginBracket = conditionField.getAttributeValue("beginBracket");
				String endBracket = conditionField.getAttributeValue("endBracket");
				String compare = conditionField.getChildText("compare");
				String compareVal = conditionField.getChildText("compareVal");
				if (ff!=null) {
					int fieldType = ff.getFieldType();
					if (fieldType==FormField.FIELD_TYPE_TEXT||fieldType==FormField.FIELD_TYPE_VARCHAR||fieldType==FormField.FIELD_TYPE_DATE)
						compareVal = SQLFilter.sqlstr(compareVal);
				}
				String logical = conditionField.getChildText("logical");
				cond.append(" ").append(beginBracket).append(" ").append(conditionFieldName).append(" ").append(compare).append(" ").append(compareVal).append(" ").append(endBracket).append(" ").append(logical);
			}
			String condString = cond.toString();
			
			// 对所设置的回写字段赋值
			Iterator writeBackFieldIr = null;
			if (writeBackFieldList!=null)
				writeBackFieldIr= writeBackFieldList.iterator();
			while (writeBackFieldIr!=null && writeBackFieldIr.hasNext()) {        // 拼接回写字段
				Element writeBackField = (Element)writeBackFieldIr.next();
				String writeBackFieldStr = writeBackField.getAttributeValue("fieldName");
				String writeBackMathStr = writeBackField.getChildText("writeBackMath");
				sb.append(writeBackFieldStr).append(" = ").append(writeBackMathStr).append(",");
			}
			String setField = sb.toString();
			if (!"".equals(setField)){
				setField = setField.substring(0, setField.lastIndexOf(","));
				String sql = "update form_table_" + writeBackForm + " set " + setField;
				if (!"".equals(condString)){
					condString = condString.substring(0, condString.lastIndexOf(" "));
					sql += " where "+condString;
				}									
				sql = parseAndSetFieldValue(writeBackForm, sql);            //过滤{}，用于回写值
				// 如果条件中带有嵌套表中的字段
				if (condString.indexOf("{$nest.")==-1) {
					sql = parseAndSetMainFieldValue(lf.getFormCode(), sql, wf.getId());     //过滤{$}，用于条件值
					JdbcTemplate jt = new JdbcTemplate();
					jt.executeUpdate(sql);										
				}
				else {
					// 取得当前主表中的子表中对应字段的值，并赋予
					List ary = parseAndSetNestFieldValue(lf.getFormCode(), sql, wf.getId());     //过滤{$}，用于条件值
					if (ary.size()==0) {
						LogUtil.getLog(getClass()).error("parseAndSetNestFieldValue error");
					} 
					else {
						JdbcTemplate jt = new JdbcTemplate();											
						for (int i=0; i<ary.size(); i++) {
							sql = parseAndSetMainFieldValue(lf.getFormCode(), (String)ary.get(i), wf.getId());     //过滤{$}，用于条件值
							jt.addBatch(sql);
						}
						jt.executeBatch();
					}
				}
			}
		}

		// 插入数据处理
		if (writeBackType==WorkflowPredefineDb.WRITE_BACK_INSERT || writeBackType==WorkflowPredefineDb.WRITE_BACK_UPDATE_INSERT) {
			WorkflowMgr wMgr = new WorkflowMgr();

			String primaryKey = nodeFinish.getChildText("primaryKey");
			boolean hasPrimaryKey = !"".equals(primaryKey) && !"empty".equals(primaryKey);
			String primaryKeyVal = "";
			
			String insertFields = nodeFinish.getChildText("insertFields");
			if (insertFields!=null) {
				boolean hasNest = false; // 是否取自嵌套表，如果是，则需插入多条数据
				JSONObject insFields = new JSONObject(insertFields);
				StringBuffer fields = new StringBuffer();
				StringBuffer values = new StringBuffer();
				Iterator irKeys = insFields.keys();
				while (irKeys.hasNext()) {
					String fieldName = (String)irKeys.next();
					
					StrUtil.concat(fields, ",", fieldName);
					String val = insFields.getString(fieldName);
					if (val.indexOf("{$nest.")!=-1) {
						hasNest = true;
					}
					
					// 得到主键字段被赋予的值
					if (hasPrimaryKey) {
						if (primaryKey.equals(fieldName)) {
							primaryKeyVal = val;
						}
					}
					
					if ("".equals(val)) {
						// 日期型如果预置为空字符串将变为null
						StrUtil.concat(values, ",", "null");										
					}
					else {
						StrUtil.concat(values, ",", val);
					}
				}
				
				if (!hasNest) {
					// 判断主键字段是否存在
					boolean isPrimaryKeyExist = false;
					if (hasPrimaryKey) {
						String sql = "select id from form_table_" + writeBackForm + " where " + primaryKey + "=" + primaryKeyVal;
						JdbcTemplate jt = new JdbcTemplate();
						ResultIterator ri = jt.executeQuery(sql, 1, 1);
						if (ri.size()>0) {
							isPrimaryKeyExist = true;
						}
					}
					// 如果主键字段未设置，或者主键字段不存在则插入数据
					if (!isPrimaryKeyExist) {
						String flowTypeCode = String.valueOf(System.currentTimeMillis());
						String sql = "insert into form_table_" + writeBackForm;
						sql += " (unit_code, cws_status, flowTypeCode, " + fields + ") values ('root', 1, " + StrUtil.sqlstr(flowTypeCode) + "," + values + ")";
						sql = wMgr.parseAndSetMainFieldValue(lf.getFormCode(), sql, wf.getId());     //过滤{$}，用于条件值
						JdbcTemplate jt = new JdbcTemplate();					
						jt.executeUpdate(sql);
					}
				}
				else {
					// 如果字段中有嵌套表
					boolean isPrimaryKeyValHasNest = false;
					if (hasPrimaryKey) {
						if (primaryKeyVal.indexOf("{$nest.")!=-1) {
							isPrimaryKeyValHasNest = true;
						}
					}
					List aryVal = null;
					if (isPrimaryKeyValHasNest) {
						aryVal = wMgr.parseAndSetNestFieldValue(lf.getFormCode(), primaryKeyVal, wf.getId());
					}
					List ary = wMgr.parseAndSetNestFieldValue(lf.getFormCode(), values.toString(), wf.getId());     //过滤{$}，用于条件值
					if (ary.size()==0) {
						LogUtil.getLog(getClass()).error("changeStatus3:parseAndSetNestFieldValue error");
					}
					else {
						JdbcTemplate jtBatch = new JdbcTemplate();
						JdbcTemplate jt = new JdbcTemplate();
						jt.setAutoClose(false);
						try {
							boolean isAddBatch = false;
							for (int i=0; i<ary.size(); i++) {
								// 判断主键字段是否存在
								boolean isPrimaryKeyExist = false;
								if (hasPrimaryKey) {
									String sql;
									if (isPrimaryKeyValHasNest) {
										sql = "select id from form_table_" + writeBackForm + " where " + primaryKey + "=" + aryVal.get(i);
									}
									else {
										sql = "select id from form_table_" + writeBackForm + " where " + primaryKey + "=" + primaryKeyVal;									
									}
									sql = wMgr.parseAndSetMainFieldValue(lf.getFormCode(), sql, wf.getId());												
									ResultIterator ri = jt.executeQuery(sql, 1, 1);
									if (ri.size()>0) {
										isPrimaryKeyExist = true;
									}
								}											
								
								if (!isPrimaryKeyExist) {
									String flowTypeCode = String.valueOf(System.currentTimeMillis());
									String sql = "insert into form_table_" + writeBackForm;
									sql += " (unit_code, cws_status, flowTypeCode, " + fields + ") values ('root', 1, " + StrUtil.sqlstr(flowTypeCode) + "," + ary.get(i) + ")";											
									sql = wMgr.parseAndSetMainFieldValue(lf.getFormCode(), sql, wf.getId());
									jtBatch.addBatch(sql);
									
									isAddBatch = true;
								}
							}
							
							if (isAddBatch) {
								jtBatch.executeBatch();
							}
						}
						finally {
							jt.close();
							jtBatch.close();
						}
					}									
				}
			}
		}		    	
    }
    
    public void sendRemindMsg(HttpServletRequest request, WorkflowDb wf, Leaf lf, WorkflowActionDb myAction, Element action) {
		String actionNames = action.getChildText("actionNames");
		String deptFields = action.getChildText("deptFields");
		String userFields = action.getChildText("userFields");
		String title = action.getChildText("title");
		String content = action.getChildText("content");
		String users = action.getChildText("users");
		String roles = action.getChildText("roles");
		
		title = title.replaceFirst("\\$flowTitle", wf.getTitle());
		title = title.replaceFirst("\\$fromUser", myAction.getUserRealName());		
		content = content.replaceFirst("\\$flowTitle", wf.getTitle());
		content = content.replaceFirst("\\$fromUser", myAction.getUserRealName());
		
		FormDAO fdao = new FormDAO();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(lf.getFormCode());
		fdao = fdao.getFormDAO(wf.getId(), fd);
		MacroCtlMgr mm = new MacroCtlMgr();

        // 处理表单域
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(title);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            FormField ff = fd.getFormField(fieldName);
            if(ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null) {
					m.appendReplacement(sb, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
				}
				else {
					m.appendReplacement(sb, "控件不存在");
				}
            }
            else {
            	m.appendReplacement(sb, fdao.getFieldValue(fieldName));
            }
        }
        m.appendTail(sb);
        title = sb.toString();
        
        m = p.matcher(content);
        sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            FormField ff = fd.getFormField(fieldName);
            if(ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null) {
					m.appendReplacement(sb, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
				}
				else {
					m.appendReplacement(sb, "控件不存在");
				}
            }
            else {
            	m.appendReplacement(sb, fdao.getFieldValue(fieldName));
            }
        }
        m.appendTail(sb);
        content = sb.toString();        
		
		boolean isMsg = "true".equals(action.getChildText("isMsg"));
		boolean isMail = "true".equals(action.getChildText("isMail"));
		boolean isSms = "true".equals(action.getChildText("isSms"));
		boolean isFlowShow = "true".equals(action.getChildText("isFlowShow"));
		
		UserMgr um = new UserMgr();
		
		Vector v = new Vector();
		Map map = new HashMap();
		String[] aNames = StrUtil.split(actionNames, ",");
		if (aNames!=null) {
			for (int i=0; i<aNames.length; i++) {
				String internalName = aNames[i];
				WorkflowActionDb wa = myAction.getWorkflowActionDbByInternalName(internalName, wf.getId());
				
				// 如果已处理过
				if (wa.getStatus()!=WorkflowActionDb.STATE_NOTDO) {
					String strUser = wa.getUserName();
					String[] ary = StrUtil.split(strUser, ",");
					for (int j=0; j<ary.length; j++) {
						if (!map.containsKey(ary[j])) {
							v.addElement(um.getUserDb(ary[j]));
							map.put(ary[j], "");
						}
					}
				}
				else {
                    // 如果未处理过
                    WorkflowRouter workflowRouter = new WorkflowRouter();
                    try {
                        v = workflowRouter.matchActionUser(null, wa, myAction, false, null);
                    } catch (ErrMsgException e) {
                        e.printStackTrace();
                        LogUtil.getLog(getClass()).info(StrUtil.trace(e));
                    } catch (MatchUserException e) {
                        e.printStackTrace();
                        LogUtil.getLog(getClass()).info(StrUtil.trace(e));
                    }
				}
			}
		}
		
		// 取出部门
		DeptUserDb dud = new DeptUserDb();
		String[] dNames = StrUtil.split(deptFields, ",");
		if (dNames!=null) {
			for (int i=0; i<dNames.length; i++) {
				Vector vt = dud.list(dNames[i]);
				Iterator ir = vt.iterator();
				while (ir.hasNext()) {
					dud = (DeptUserDb)ir.next();
					if (!map.containsKey(dud.getUserName())) {
						v.addElement(um.getUserDb(dud.getUserName()));
						map.put(dud.getUserName(), "");
					}					
				}
			}
		}
		
		// 取出用户域
		String[] uNames = StrUtil.split(userFields, ",");
		if (uNames!=null) {
			for (int i=0; i<uNames.length; i++) {
				if (!map.containsKey(uNames[i])) {
					v.addElement(um.getUserDb(uNames[i]));
					map.put(uNames[i], "");
				}				
			}
		}		

		// 取出角色
		String[] roleAry = StrUtil.split(roles, ",");
		if (roleAry!=null) {
			RoleDb rd = new RoleDb();
			for (int i=0; i<roleAry.length; i++) {
				rd = rd.getRoleDb(roleAry[i]);
				if (rd==null) {
					rd = new RoleDb();
					continue;
				}
				Iterator ir = rd.getAllUserOfRole().iterator();
				while (ir.hasNext()) {
					UserDb ud = (UserDb)ir.next();
					if (!map.containsKey(ud.getName())) {
						v.addElement(ud);
						map.put(ud.getName(), "");
					}	
				}
			}
		}
		
		// 取出用户
		uNames = StrUtil.split(users, ",");
		if (uNames!=null) {
			for (int i=0; i<uNames.length; i++) {
				if (!map.containsKey(uNames[i])) {
					v.addElement(um.getUserDb(uNames[i]));
					map.put(uNames[i], "");
				}				
			}
		}		
		
        String charset = Global.getSmtpCharset();
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
        String senderName = StrUtil.GBToUnicode(Global.AppName);
        senderName += "<" + Global.getEmail() + ">";	
        String mailserver = Global.getSmtpServer();
        int smtp_port = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwd_raw = Global.getSmtpPwd();
        boolean isSsl = Global.isSmtpSSL();
        try {
            sendmail.initSession(mailserver, smtp_port, name,
                                 pwd_raw, "", isSsl);
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }
        
		// String tail = getFormAbstractTable(wf);
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
		MessageDb md = new MessageDb();
        Config cfg = Config.getInstance();
        IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
        boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");

		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			UserDb ud = (UserDb) ir.next();

			if (isSms && SMSFactory.isUseSMS()) {
				IMsgUtil imu = SMSFactory.getMsgUtil();
				if (imu != null) {
                    if (mqIsOpen) {
                        msgProducer.sendSms(ud.getName(), title);
                    }
                    else {
                        try {
                            imu.send(ud, title, MessageDb.SENDER_SYSTEM);
                        } catch (ErrMsgException e) {
                            e.printStackTrace();
                        }
                    }
				}
			}

			// 发送信息
			if (isMsg) {
				String strAtion = "";
				if (isFlowShow) {
					strAtion = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + myAction.getFlowId();
				}

                try {
                    if (mqIsOpen) {
                        msgProducer.sendSysMsg(ud.getName(), title, content, strAtion);
                    } else {
                        md.sendSysMsg(ud.getName(), title, content, strAtion);
                    }
				} catch (ErrMsgException e) {
					e.printStackTrace();
				}
			}

			if (isMail && ud.getEmail() != null && !ud.getEmail().equals("")) {				
				UserSetupDb usd = new UserSetupDb(ud.getName());
				String mailCont = content; // + tail;
				
				if (isFlowShow) {
					String strAction = "op=show|userName=" + ud.getName() + "|" +
                		"flowId=" + wf.getId();
					strAction = cn.js.fan.security.ThreeDesUtil.encrypt2hex(
							ssoCfg.getKey(), strAction);				
					mailCont += "<BR />>>&nbsp;<a href='"
							+ Global.getFullRootPath()
							+ "/public/flow_dispose.jsp?action="
							+ strAction
							+ "' target='_blank'>"
							+ (usd.getLocal().equals("en-US") ? "Click here to apply"
									: "请点击此处查看") + "</a>";					
				}
                if (mqIsOpen) {
                    msgProducer.sendEmail(ud.getEmail(), senderName, title, mailCont);
                }
                else {
                    sendmail.initMsg(ud.getEmail(), senderName, title, mailCont, true);
                    sendmail.send();
                    sendmail.clear();
                }
			}
		}
		
    }
    
    /**
     * 解析带$字符串并赋值{$}
     * @param formcode
     * @param strWithFields
     * @param flowId
     * @return
     */
    public String parseAndSetMainFieldValue(String formcode, String strWithFields, int flowId) {
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb(formcode);
    	
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(flowId, fd);
        
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(strWithFields);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);

            if ("id".equals(fieldTitle)) {
            	m.appendReplacement(sb, String.valueOf(fdao.getId()));
            	continue;
            }
            FormField field = fd.getFormField(fieldTitle);
            if (field == null) {
                field = fd.getFormFieldByTitle(fieldTitle);
                if (field == null) {
                    LogUtil.getLog(SQLCtl.class).error("表单：" + fd.getName() + "，脚本：" + strWithFields + "中，字段：" + fieldTitle + " 不存在！");
                }
            }
            // System.out.println(getClass() + " scriptStr=" + scriptStr + " fieldTitle=" + fieldTitle + " ff=" + ff);
            String val = "";
            if (field!=null) {
	        	val = fdao.getFieldValue(field.getName());
	            if (field.getFieldType()==FormField.FIELD_TYPE_DATE) {
	            	val = SQLFilter.getDateStr(val, "yyyy-MM-dd");
	            }
	            else if (field.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
	            	val = SQLFilter.getDateStr(val, "yyyy-MM-dd HH:mm:ss");
	            }
            }
            m.appendReplacement(sb, val);
        }
        m.appendTail(sb);
        return sb.toString();
    }    
    
    /**
     * 解析嵌套表的带$字符串并赋值{$}
     * @param formcode
     * @param strWithFields
     * @param flowId
     * @return
     */
    public List parseAndSetNestFieldValue(String formcode, String strWithFields, int flowId) {
    	WorkflowDb wf = new WorkflowDb();
    	wf = wf.getWorkflowDb(flowId);

    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(wf.getTypeCode()); // wf为内置变量，可以直接引用

    	FormDb fdScript = new FormDb();
    	fdScript = fdScript.getFormDb(lf.getFormCode());

    	// 取出当前流程的表单记录
    	FormDAO fdaoScript = new FormDAO();
    	fdaoScript = fdaoScript.getFormDAO(wf.getId(), fdScript);    

    	long fdaoScriptId = fdaoScript.getId();
    	
    	// FormDb fd = new FormDb();
    	// fd = fd.getFormDb(formcode);
    	
    	boolean isNestFound = false;
    	String nestFormCode = "";
    	
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_.\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(strWithFields);
        // sql语句中限定只能有一个嵌套表，如果有两个，逻辑太复杂
        while (m.find()) {
            String fieldTitle = m.group(1);
            int index = fieldTitle.indexOf(".");
            int q = fieldTitle.indexOf(".", index+1);
            if (q!=-1) {
            	nestFormCode = fieldTitle.substring(index + 1, q);
            	isNestFound = true;
            	break;
            }
        }
        
        FormDb fdNest = new FormDb();
        fdNest = fdNest.getFormDb(nestFormCode);
    	
        List list = new ArrayList();
        JdbcTemplate jt = new JdbcTemplate();
        String sql2 = "select * from form_table_" + nestFormCode + " where cws_id=" + fdaoScriptId + " order by id desc";
        ResultIterator ri;
		try {
			ri = jt.executeQuery(sql2);
	        while (ri.hasNext()) {
	        	ResultRecord rr = (ResultRecord)ri.next();
	            
	            StringBuffer sb = new StringBuffer();
	            m = p.matcher(strWithFields);	            
	            while (m.find()) {
	                String fieldTitle = m.group(1);
	                // 当插入时，可能SQL语句中会出现主表字段，所以需过滤
	                if (!fieldTitle.startsWith("nest.")) {
	                	continue;
	                }
	                
	                String nestFormField = fieldTitle.substring(fieldTitle.lastIndexOf(".")+1);
	                String nestFieldName = nestFormField;
	                
	                FormField ff = fdNest.getFormField(nestFormField);
	                if (ff==null) {
	                	// 插入数据时，格式为：{$nest.sksqmx.付款说明}
	                	ff = fdNest.getFormFieldByTitle(nestFormField);
	                	if (ff!=null) {
	                		nestFieldName = ff.getName();
	                	}
	                }
	                
	                String val = rr.getString(nestFieldName);
	                m.appendReplacement(sb, val);
	            }
	            m.appendTail(sb);
	            list.add(sb.toString());
	        }    	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

    }    
    
    /**
     * 解析回写数据库时的SQL（不带$字符串）并赋值{}
     * @Description: 
     * @param strWithFields
     * @return
     */
    public String parseWriteBackDbField(String strWithFields) {
        Pattern p = Pattern.compile(
                "\\{([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(strWithFields);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String colName = m.group(1);
            m.appendReplacement(sb, colName);
        }
        m.appendTail(sb);
        return sb.toString();
    }       
    
    /**
     * 解析不带$字符串并赋值{}
     * @param writeBackCode
     * @param strWithFields
     * @return
     */
    public String parseAndSetFieldValue( String writeBackCode, String strWithFields) {
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb(writeBackCode);
        Pattern p = Pattern.compile(
                "\\{([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(strWithFields);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);
            // 制作大亚表单时发现，差旅费报销单中字段名称会有重复，所以这里先找编码，不行再找名称，防止名称重复
            FormField field = fd.getFormField(fieldTitle);
            if (field == null) {
                field = fd.getFormFieldByTitle(fieldTitle);
                if (field == null) {
                    LogUtil.getLog(SQLCtl.class).error("表单：" + fd.getName() + "，脚本：" + strWithFields + "中，字段：" + fieldTitle + " 不存在！");
                }
            }
            m.appendReplacement(sb, field.getName());
        }
        m.appendTail(sb);
        return sb.toString();
    }   
    /**
     * 自动存档 2013-01-16 作废 fgf
     * @param request HttpServletRequest
     * @param action WorkflowActionDb
     * @param wf WorkflowDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean autoSaveArchiveXXX(HttpServletRequest request, WorkflowDb wf,
                                   WorkflowActionDb action) throws
            ErrMsgException {
        // 自动存档
        Privilege privilege = new Privilege();
        String flag = action.getFlag();
        if (flag.length() >= 5 && flag.substring(4, 5).equals("2")) {
            String formReportContent = ParamUtil.get(request, "formReportContent");
            return saveDocumentArchive(wf, privilege.getUser(request),
                                       formReportContent);
        }
        return false;
    }

    /**
     * 自动存档
     * @param request HttpServletRequest
     * @param wf WorkflowDb
     * @param action WorkflowActionDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean autoSaveArchive(HttpServletRequest request, WorkflowDb wf,
                                   WorkflowActionDb action) throws
            ErrMsgException {
        // 自动存档
        String flag = action.getFlag();
        if (flag.length() >= 5 && flag.substring(4, 5).equals("2")) {
        	return doAutoSaveArchive(request, wf);
        }
        return false;
    }
    
	public boolean doAutoSaveArchive(HttpServletRequest request, WorkflowDb wf) throws ErrMsgException {
		// 自动存档
		Privilege privilege = new Privilege();

		FormDb fd = new FormDb();
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		fd = fd.getFormDb(lf.getFormCode());

		int doc_id = wf.getDocId();
		DocumentMgr dm = new DocumentMgr();
		Document doc = dm.getDocument(doc_id);
		Render render = new Render(request, wf, doc);
		String formReportContent = render.reportForArchive(wf, fd);
		
    	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        String dirCode = wpd.getDirCode();
        if (dirCode.equals("")) {
            logger.error("saveDocumentArchive:" + "存档所选的节点为空！");
            return false;
        }
        
		return saveDocumentArchive(wf, privilege.getUser(request), formReportContent, dirCode, wpd.getExamine()==Document.EXAMINE_PASS);
	}
	
	/**
	 * 用于流程结束事件中归档
	 * @param request
	 * @param wf
	 * @param dirCode
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean saveArchive(HttpServletRequest request, WorkflowDb wf, String dirCode, boolean isExamined) throws ErrMsgException {
		// 自动存档
		Privilege privilege = new Privilege();

		FormDb fd = new FormDb();
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		fd = fd.getFormDb(lf.getFormCode());

		int doc_id = wf.getDocId();
		DocumentMgr dm = new DocumentMgr();
		Document formDoc = dm.getDocument(doc_id);
		Render render = new Render(request, wf, formDoc);
		String formReportContent = render.reportForArchive(wf, fd);

		return saveDocumentArchive(wf, privilege.getUser(request), formReportContent, dirCode, isExamined);
	}	

    public boolean start(HttpServletRequest request, int flowId, WorkflowActionDb wad, long myActionId) throws ErrMsgException {
        WorkflowRuler wr = new WorkflowRuler();
        WorkflowDb wd = getWorkflowDb(flowId);
        boolean re = wr.canUserStartFlow(request, wd);

        if (!re) {
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(myActionId);
            // 转办
            if (mad.getActionStatus() == WorkflowActionDb.STATE_TRANSFERED) {
                re = true;
            }
        }

        if (re) {
            Privilege privilege = new Privilege();
            return wd.start(request, privilege.getUser(request), fu, wad, myActionId);
        }
        else {
            throw new ErrMsgException(wr.getErrMsg());
        }
    }

    public String getFieldValue(String fieldName) {
         return StrUtil.getNullStr(fu.getFieldValue(fieldName));
    }

    /**
     * 保存草稿
     * @param request
     * @param wf
     * @param myAction
     * @return
     * @throws ErrMsgException
     */
    public boolean saveFormValue(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction) throws ErrMsgException {
        /*
    	boolean isFlowModified = StrUtil.getNullString(fu.getFieldValue("isFlowModified")).equals("1"); // 流程中的节点是否被修改
        boolean isModifyFlow = false;
        if (isFlowModified) {
            isModifyFlow = true;
        }
        */

        // 保存流程的标题等
        if (!wf.isStarted()) {
            saveWorkflowProps(request, wf, myAction, fu);
        }
        /*
        if (!isModifyFlow) {
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            String canUserModifyFlow = cfg.get("canUserModifyFlow");
            if (canUserModifyFlow.equals("true"))
                isModifyFlow = true;
        }
        */
        /*
        if (isModifyFlow) {
            String flowstring = StrUtil.getNullStr(fu.getFieldValue(
                    "flowstring"));
            if (!wf.modifyDBActionByFlowString(request, flowstring))
                throw new ErrMsgException("更新流程图失败！");
        }
        */

       String op = getFieldValue("op");
       // XorCondNodeCommit可能已无用 20130917 fgf
       if (op.equals("XorCondNodeCommit")) {
           // 更新节点上选择的用户名和用户真实姓名
           String prefix = "WorkflowAction_";
           UserMgr um = new UserMgr();
           UserDb user = null;
           WorkflowActionDb wfa = null;
           Vector vto = myAction.getLinkToActions();
           Iterator irto = vto.iterator();
           while (irto.hasNext()) {
               wfa = (WorkflowActionDb) irto.next();
               // 取节点上选择的用户名
               String uNames = "", uRealNames = "";
               String[] userNames = fu.getFieldValues(prefix + wfa.getId());
               LogUtil.getLog(getClass()).info(
                       "saveFormValue: prefix + wfa.getId()=" + prefix + wfa.getId());
               int len = 0;
               if (userNames != null) {
                   len = userNames.length;
               }
               for (int i = 0; i < len; i++) {
                   user = um.getUserDb(userNames[i]);
                   String userRealName = user.getRealName();
                   if (uNames.equals("")) {
                       uNames = userNames[i];
                       uRealNames = userRealName;
                   } else {
                       uNames += "," + userNames[i];
                       uRealNames += "," + userRealName;
                   }
               }

               LogUtil.getLog(getClass()).info("saveFormValue: " + prefix +
                                               wfa.getId() + "=" + prefix +
                                               wfa.getId() + " uNames=" + uNames +
                                               "--" + wfa.getUserName());

               // 如果不相等，则说明选择的用户有变化
               if (!wfa.getUserName().equals(uNames)) {
                   int status = wfa.getStatus();
                   if (status == WorkflowActionDb.STATE_FINISHED ||
                       status == WorkflowActionDb.STATE_DOING) {
                	   String str = LocalUtil.LoadString(request,"res.flow.Flow","canNotEdit");
                	   String str1 = LocalUtil.LoadString(request,"res.flow.Flow","action");
                       throw new ErrMsgException("saveFormValue:"+str1 + wfa.getTitle() +
                    		   str);
                   }
                   wfa.setUserName(uNames);
                   wfa.setUserRealName(uRealNames);
                   wfa.save();
               }
           }
       }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        FormDAOMgr fdm = new FormDAOMgr(wf.getId(), lf.getFormCode(), fu, myAction);
        // 置保存的时间
        myAction.setResult(DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm"));
        myAction.save();
        
        // 条件判断前，先保存草稿，再判定条件，所以也需validate
        if ("saveformvalueBeforeXorCondSelect".equals(op)) {
            return fdm.update(request, false);
        } else {
            return fdm.update(request, true); // 参数true表示保存草稿
        }
    }

    public boolean addNewDocument(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        String[] extnames = {"jpg", "gif", "xls", "rar", "doc", "rm", "avi",
                            "bmp", "swf"};
        FileUpload TheBean = new FileUpload();
        TheBean.setValidExtname(extnames); // 设置可上传的文件类型
        TheBean.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K
        int ret = 0;
        try {
            ret = TheBean.doUpload(application, request);
            if (ret!=FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(TheBean.getErrMessage());
            }
        }
        catch (Exception e) {
            logger.error("addNewDocument:" + e.getMessage());
        }
        if (ret == 1) {
            WorkflowDb wf = new WorkflowDb();
            return wf.addNewDocument(application, TheBean);
        }
        else {
            return false;
        }
    }

    /**
     * 拟文
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return boolean
     */
    public int writeDocument(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        FileUpload TheBean = new FileUpload();

        Config cfg = new Config();
        String exts = cfg.get("flowFileExt");
        String[] extAry = StrUtil.split(exts, ",");

        TheBean.setValidExtname(extAry); // 设置可上传的文件类型
        TheBean.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K
        int ret = 0;
        try {
            ret = TheBean.doUpload(application, request);
            if (ret == -3) {
                throw new ErrMsgException("您上传的文件太大,请把文件大小限制在" + Global.FileSize + "K以内!");
            }
            if (ret == -4) {
                throw new ErrMsgException("文件非法！");
            }
        }
        catch (Exception e) {
            logger.error("writeDocument:" + e.getMessage());
        }

        int flowId = ParamUtil.getInt(request, "flowId", -1);
        if (flowId==-1) {
            // 20111101为适应ntko控件增加
            flowId = StrUtil.toInt(TheBean.getFieldValue("flowId"), -1);
            if (flowId==-1) {
                throw new ErrMsgException("缺少流程编号！");
            }
        }

        Privilege privilege = new Privilege();

        WorkflowDb wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(flowId);

        if (ret == 1) {
            String creator = privilege.getUser(request);
            return wfd.writeDocument(creator, TheBean);
        }
        else
            return -1;
    }

    /**
     * 判断能否提交，即锁是否为本人锁的
     * @param userName String
     * @param att Attachment
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean checkAttachmentLock(String userName, com.redmoon.oa.flow.Attachment att) throws ErrMsgException {
        if (att.getLockUser().equals("")) {
            return true;
        }

        if (att.getLockUser().equals(userName)) {
            return true;
        } else {
            UserDb user = new UserDb();
            user = user.getUserDb(att.getLockUser());
            throw new ErrMsgException(user.getRealName() + "正在编辑中，无法保存。请关闭窗口，重新打开再编辑！");
        }
    }

    /**
     * 编辑或审批文件
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean uploadDocument(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        FileUpload TheBean = new FileUpload();

        Config cfg = new Config();
        String exts = cfg.get("flowFileExt");
        String[] extAry = StrUtil.split(exts, ",");

        TheBean.setValidExtname(extAry); // 设置可上传的文件类型
        TheBean.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K
        int ret = 0;
        try {
            ret = TheBean.doUpload(application, request);
            if (ret == -3) {
                throw new ErrMsgException("您上传的文件太大,请把文件大小限制在" + Global.FileSize + "K以内!");
            }
            if (ret == -4) {
                throw new ErrMsgException("文件非法！");
            }
        }
        catch (Exception e) {
            logger.error("uploadDocument:" + e.getMessage());
        }
        if (ret == 1) {
            Privilege pvg = new Privilege();
            String userName = pvg.getUser(request);

            String strdocId = StrUtil.getNullStr(TheBean.getFieldValue("doc_id"));
            String strfileId = StrUtil.getNullStr(TheBean.getFieldValue("file_id"));

            int docId = Integer.parseInt(strdocId);
            int fileId = Integer.parseInt(strfileId);
            Document doc = new Document();
            doc = doc.getDocument(docId);
            com.redmoon.oa.flow.Attachment att = doc.getAttachment(1, fileId);
            // 判断是否是自己上的锁，在flow_ntko_edit.jsp中上锁，保存文件后解锁
            if (checkAttachmentLock(userName, att)) {
                WorkflowDb wf = new WorkflowDb();
                boolean re = wf.uploadDocument(TheBean);
                if (re) {
                    att.setLockUser(""); // 解锁
                    att.save();
                    // 保存日志
                    AttachmentLogMgr.log(userName, doc.getFlowId(), att.getId(), AttachmentLogDb.TYPE_EDIT);
                }
                return re;
            }
            else {
                return false;
            }
        }
        else
            return false;
    }

    /**
     * 手工存档
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean saveDocumentArchive(HttpServletRequest request) throws ErrMsgException {
        int flowId = ParamUtil.getInt(request, "flowId");
        WorkflowDb wf = getWorkflowDb(flowId);
        // int actionId = ParamUtil.getInt(request, "actionId");
        String title = ParamUtil.get(request, "title");
        String content = ParamUtil.get(request, "content");
        if (title.equals("")) {
            throw new ErrMsgException("标题不能为空！");
        }
        String dirCode = ParamUtil.get(request, "dirCode");
        com.redmoon.oa.fileark.Leaf lf = new com.redmoon.oa.fileark.Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf==null || !lf.isLoaded()) {
            throw new ErrMsgException("对不起，节点不存在！");
        }
        // 检查用户是否有往节点dirCode中写入的权限
        com.redmoon.oa.fileark.LeafPriv lp = new com.redmoon.oa.fileark.LeafPriv(dirCode);
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        boolean re = lp.canUserAppend(userName);
        if (!re)
            throw new ErrMsgException("非法操作");

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());

        com.redmoon.oa.fileark.Document doc = new com.redmoon.oa.fileark.Document();
        doc.setExamine(wpd.getExamine());
        doc.create(dirCode, title, content, com.redmoon.oa.fileark.Document.TYPE_DOC, "", "", userName, com.redmoon.oa.fileark.Document.NOTEMPLATE, userName);

        // 根据流程中需要保存的文档创建附件
        String[] attachIds = ParamUtil.getParameters(request, "attachIds");
        int len;
        if (attachIds==null)
            len = 0;
        else
            len = attachIds.length;
        if (len == 0)
            return true;
        doc = doc.getDocument(doc.getID());
        com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment();

        // 置文件目录
        Config cfg = new Config();
        String filepath = cfg.get("file_folder");
        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(Calendar.YEAR));
        String month = "" + (cal.get(Calendar.MONTH) + 1);
        String vpath = filepath + "/" +
                       year + "/" + month + "/";

        int orders = 1;
        for (int i = 0; i < len; i++) {
            // 取得流程中的附件
        	com.redmoon.oa.flow.Attachment a = new com.redmoon.oa.flow.Attachment(Integer.parseInt(attachIds[i]));
            String diskName = FileUpload.getRandName() + "." + StrUtil.getFileExt(a.getDiskName());
            String fullPath = Global.getRealPath() + vpath + diskName;
            att.setFullPath(fullPath);
            // logger.info("fullPath1=" + fullPath);
            att.setDocId(doc.getID());
            att.setName(a.getName());
            att.setDiskName(diskName);
            att.setVisualPath(vpath);
            att.setPageNum(1);
            att.setOrders(a.getOrders());
            att.setUploadDate(new java.util.Date());
            re = att.create();
            if (re) {
                // 将流程中的文件拷贝至存档文件目录
                if (i == 0) {
                    File f = new File(Global.getRealPath() + vpath);
                    if (!f.isDirectory()) {
                        f.mkdirs();
                    }
                }
                // logger.info("fullPath=" + fullPath);
                FileUtil.CopyFile(a.getFullPath(), fullPath);
            }
            orders++;
        }
        return true;
    }
    

    /**
     * 自动存档
     * @param wf WorkflowDb
     * @param userName String
     * @param formReportContent String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean saveDocumentArchive(WorkflowDb wf, String userName, String formReportContent) throws ErrMsgException {
        // logger.info("saveDocumentArchive:" + wf.getTitle() + " dirCode=" + wf.getTypeCode());

    	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        String dirCode = wpd.getDirCode();
        if (dirCode.equals("")) {
            logger.error("saveDocumentArchive:" + "存档所选的节点为空！");
            return false;
        }

        return saveDocumentArchive(wf, userName, formReportContent, wpd.getDirCode(), wpd.getExamine()==Document.EXAMINE_PASS);
    }    

    /**
     * 自动存档
     * @param wf WorkflowDb
     * @param userName String
     * @param formReportContent String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean saveDocumentArchive(WorkflowDb wf, String userName, String formReportContent, String dirCode, boolean isExamined) throws ErrMsgException {
        // logger.info("saveDocumentArchive:" + wf.getTitle() + " dirCode=" + wf.getTypeCode());
        /*
    	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        String dirCode = wpd.getDirCode();
        if (dirCode.equals("")) {
            logger.error("saveDocumentArchive:" + "存档所选的节点为空！");
            return false;
        }
        */
        com.redmoon.oa.fileark.Leaf lf = new com.redmoon.oa.fileark.Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf==null || !lf.isLoaded()) {
            logger.error("saveDocumentArchive:" + "存档所选的节点不存在！");
            // throw new ErrMsgException("对不起，存档所选的节点不存在！");
            return false;
        }
        int doc_id = wf.getDocId();
        DocumentMgr dm = new DocumentMgr();
        Document formDoc = dm.getDocument(doc_id);

        // logger.info("saveDocumentArchive: formDoc.title=" + formDoc.getTitle() + " lf.name=" + lf.getName());

        com.redmoon.oa.fileark.Document doc = new com.redmoon.oa.fileark.Document();
        // doc.setExamine(wpd.getExamine());
        doc.setExamine(isExamined?Document.EXAMINE_PASS:Document.EXAMINE_NOT);
        doc.setFlowId(wf.getId());
        boolean ret = doc.create(dirCode, wf.getTitle(), formReportContent, com.redmoon.oa.fileark.Document.TYPE_DOC, "", "", userName, doc.NOTEMPLATE, userName);
        // logger.info("saveDocumentArchive: ret=" + ret);
        doc = doc.getDocument(doc.getID());

        // 置文件目录
        Config cfg = new Config();
        String filepath = cfg.get("file_folder");
        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(Calendar.YEAR));
        String month = "" + (cal.get(Calendar.MONTH) + 1);
        String vpath = filepath + "/" +
                       year + "/" + month + "/";

        int orders = 1;
        com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment();
        // 取得流程中的附件
        java.util.Vector attachments = formDoc.getAttachments(1);
        java.util.Iterator ir = attachments.iterator();
        while (ir.hasNext()) {
        	com.redmoon.oa.flow.Attachment a = (com.redmoon.oa.flow.Attachment) ir.next();
            String diskName = FileUpload.getRandName() + "." + StrUtil.getFileExt(a.getDiskName());
            String fullPath = Global.getRealPath() + vpath + diskName;
            att.setFullPath(fullPath);
            // logger.info("fullPath1=" + fullPath);
            att.setDocId(doc.getID());
            att.setName(a.getName());
            att.setDiskName(diskName);
            att.setVisualPath(vpath);
            att.setPageNum(1);
            att.setOrders(a.getOrders());
            att.setUploadDate(new java.util.Date());
            boolean re = att.create();
            if (re) {
                // 将流程中的文件拷贝至存档文件目录
                if (orders == 1) {
                    File f = new File(Global.getRealPath() + vpath);
                    if (!f.isDirectory()) {
                        f.mkdirs();
                    }
                }
                // logger.info("fullPath=" + fullPath);
                FileUtil.CopyFile(a.getFullPath(), fullPath);
            }
            orders++;
        }
        return ret;
    }

    public boolean addMonitor(HttpServletRequest request) throws ErrMsgException {
        int flowId = ParamUtil.getInt(request, "flowId");
        String userName = ParamUtil.get(request, "userName");
        if (userName.equals("")) {
            throw new ErrMsgException("请选择流程监控人员！");
        }
        WorkflowDb wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(flowId);

        WorkflowRuler wr = new WorkflowRuler();
        if (!wr.canMonitor(request, wfd))
            throw new ErrMsgException("权限非法！");
        return wfd.addMonitor(userName);
    }

    public boolean delMonitor(HttpServletRequest request) throws ErrMsgException {
        int flowId = ParamUtil.getInt(request, "flowId");
        String userName = ParamUtil.get(request, "userName");
        WorkflowDb wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(flowId);

        // WorkflowRuler wr = new WorkflowRuler();
        // if (!wr.canMonitor(request, wfd))
        //    throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        return wfd.delMonitor(userName);
    }

    public BSHShell runDiscardScript(HttpServletRequest request, String curUserName, int flowId, FormDAO fdao, String script) throws ErrMsgException {
        BSHShell bs = new BSHShell();
        StringBuffer sb = new StringBuffer();
        BeanShellUtil.setFieldsValue(fdao, sb);
        // 赋值当前用户
        sb.append("String userName=\"" + curUserName + "\";");
        sb.append("int flowId=" + flowId + ";");

        bs.set(ConstUtil.SCENE, ConstUtil.SCENE_FLOW_DISCARD);
        bs.set("request", request);
        bs.set("fileUpload", fu);

        bs.eval(BeanShellUtil.escape(sb.toString()));
        bs.eval(script);
        Object obj = bs.get("ret");
        if (obj != null) {
            boolean ret = ((Boolean) obj).booleanValue();
            if (!ret) {
                String errMsg = (String) bs.get("errMsg");
                LogUtil.getLog(getClass()).error("Discard bsh errMsg=" + errMsg);
            }
        }
        return bs;
    }

    /**
     * 放弃流程，流程不会被删除
     * @param request HttpServletRequest
     * @param flowId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean discard(HttpServletRequest request, String userName, int flowId) throws ErrMsgException {
        WorkflowDb wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(flowId);

        // WorkflowRuler wr = new WorkflowRuler();
        // if (!wr.canMonitor(request, wfd))
        //    throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        Privilege privilege = new Privilege();
        boolean re = wfd.discard(userName);
        if (re) {
			FormDAO fdao = new FormDAO();
			FormDb fd = new FormDb();
			Leaf lf = new Leaf();
			lf = lf.getLeaf(wfd.getTypeCode());
			fd = fd.getFormDb(lf.getFormCode());
			fdao = fdao.getFormDAO(wfd.getId(), fd);
			fdao.setStatus(FormDAO.STATUS_DISCARD);
			fdao.save();
			
			WorkflowPredefineDb wpd = new WorkflowPredefineDb();
			wpd = wpd.getDefaultPredefineFlow(wfd.getTypeCode());
			WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
			String script = wpm.getDiscardScript(wpd.getScripts());
			if (script != null && !"".equals(script)) {
                runDiscardScript(request, userName, wfd.getId(), fdao, script);
			}               	
        }
        return re;
    }

    /**
     * 审阅
     * @param request HttpServletRequest
     * @param actionId int
     * @param myActionId long
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean read(HttpServletRequest request, int actionId, long myActionId) throws ErrMsgException {
        Privilege privilege = new Privilege();
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        mad.setCheckDate(new java.util.Date());
        mad.setChecked(true);
        mad.setChecker(privilege.getUser(request));
        mad.setResultValue(WorkflowActionDb.RESULT_VALUE_READED);
        boolean re = mad.save();
        if (re) {
            mad.onChecked();

            if (mad.isAllUserOfActionChecked()) {
                WorkflowActionDb wad = new WorkflowActionDb();
                wad = wad.getWorkflowActionDb(actionId);
                wad.setStatus(WorkflowActionDb.STATE_FINISHED);
                re = wad.save();

                // 如果该节点为结束节点
                if ("1".equals(wad.getItem1())) {
                    WorkflowDb wf = new WorkflowDb();
                    wf = wf.getWorkflowDb((int) mad.getFlowId());
                    // 如果流程未结束，则结束流程
                    if (wf.getStatus() != WorkflowDb.STATUS_FINISHED) {
                        wad.doWorkflowFinished(request, wf);
                    }
                }
            }

        }
        return re;
    }

    /**
     * 拒绝流程，或者当op为manualFinishAgree时手工结束流程
     * @param request HttpServletRequest
     * @param flowId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean ManualFinish(HttpServletRequest request, int flowId, long myActionId) throws ErrMsgException {
        WorkflowDb wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(flowId);

        // WorkflowRuler wr = new WorkflowRuler();
        // if (!wr.canMonitor(request, wfd))
        //    throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        Privilege privilege = new Privilege();
        
        String op = getFieldValue("op");
        boolean isFinishAgree = false;
        if ("manualFinishAgree".equals(op)) {
        	isFinishAgree = true;
        }
        boolean re = wfd.ManualFinish(request, privilege.getUser(request), myActionId, isFinishAgree);

        /*
        // 自动存档
        int actionId = StrUtil.toInt(getFieldValue("actionId"), -1);
        if (actionId != -1) {
            WorkflowActionDb wad = new WorkflowActionDb();
            wad = wad.getWorkflowActionDb(actionId);
            String flag = wad.getFlag();
            if (flag.length() >= 5 && flag.substring(4, 5).equals("2")) {
                String formReportContent = StrUtil.getNullStr(fu.getFieldValue(
                        "formReportContent"));
                saveDocumentArchive(wfd, privilege.getUser(request),
                                    formReportContent);
            }
        } else {
            LogUtil.getLog(getClass()).error("ManualFinish: actionId is not found.");
        }
        */

       WorkflowDb wf = new WorkflowDb();
       wf = wf.getWorkflowDb(flowId);

       boolean isUseMsg = getFieldValue("isUseMsg").equals("true");
       boolean isToMobile = getFieldValue("isToMobile").equals("true");

       Config cfg = new Config();
       IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
       boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
       boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
       String charset = Global.getSmtpCharset();
       cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
       String senderName = StrUtil.GBToUnicode(Global.AppName);
       senderName += "<" + Global.getEmail() + ">";
       if (flowNotifyByEmail) {
           String mailserver = Global.getSmtpServer();
           int smtp_port = Global.getSmtpPort();
           String name = Global.getSmtpUser();
           String pwd_raw = Global.getSmtpPwd();
           boolean isSsl = Global.isSmtpSSL();
           try {
               sendmail.initSession(mailserver, smtp_port, name,
                                    pwd_raw, "", isSsl);
           } catch (Exception ex) {
               LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
           }
       }

       com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

       String tail = WorkflowMgr.getFormAbstractTable(wf);

       MessageDb md = new MessageDb();
       UserMgr um = new UserMgr();
       UserDb user = um.getUserDb(privilege.getUser(request));

       // 通知发起人
       if (wf.getStatus() == WorkflowDb.STATUS_REFUSED) {
           String ft = SkinUtil.LoadString(request,
                                           "res.module.flow",
                                           "msg_flow_finished_title");
           String fc = SkinUtil.LoadString(request,
                                           "res.module.flow",
                                           "msg_flow_finished_content");
           ft = StrUtil.format(ft, new String[] {wf.getTitle()});
           fc = StrUtil.format(fc, new String[] {wf.getTitle(),
                               user.getRealName(),
                               DateUtil.format(new java.util.Date(),
                                               "yyyy-MM-dd HH:mm")});

           if (isToMobile) {
               IMsgUtil imu = SMSFactory.getMsgUtil();
               if (imu != null) {
                   if (mqIsOpen) {
                       msgProducer.sendSms(wf.getUserName(), fc);
                   }
                   else {
                       UserDb ud = um.getUserDb(wf.getUserName());
                       imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                   }
               }
           }

           fc += tail;

           if (isUseMsg) {
               // 发送信息
               String action = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + wf.getId();
               if (mqIsOpen) {
                   msgProducer.sendSysMsg(wf.getUserName(), ft, fc, action);
               }
               else {
                   md.sendSysMsg(wf.getUserName(), ft, fc, action);
               }
           }

           if (flowNotifyByEmail) {
               if (!user.getEmail().equals("")) {
                   String action = "op=show|userName=" + user.getName() + "|" +
                                   "flowId=" + wf.getId();
                   action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(
                           ssoCfg.getKey(), action);
                   UserSetupDb usd = new UserSetupDb(user.getName());
                   fc += "<BR /><BR />>>&nbsp;<a href='" +
                           Global.getFullRootPath(request) +
                           "/public/flow_dispose.jsp?action=" + action +
                           "' target='_blank'>" + 
                           (usd.getLocal().equals("en-US") ? "Click here to view" : "请点击此处查看") + "</a>";
                   if (mqIsOpen) {
                       msgProducer.sendEmail(user.getEmail(), senderName, ft, fc);
                   }
                   else {
                       sendmail.initMsg(user.getEmail(), senderName, ft, fc, true);
                       sendmail.send();
                       sendmail.clear();
                   }
               }
           }
       }
       return re;
    }

    /**
     * 撤回待办记录myActionId对应的下一节点
     * @param myActionId long
     * @return boolean
     */
    public boolean recallMyAction(HttpServletRequest request, long myActionId) throws ErrMsgException {
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);

        Privilege privilege = new Privilege();
        String myname = privilege.getUser(request);
        if (!privilege.isUserPrivValid(request, "admin")) {
	        if (!mad.getUserName().equals(myname) &&
	            !mad.getProxyUserName().equals(myname)) {
	            // 权限检查
	            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
	        }
        }

        if (!mad.isChecked()) {
        	String str = LocalUtil.LoadString(request,"res.flow.Flow","upcomingItem");
            throw new ErrMsgException(str);
        }

        boolean re = false;

        Directory dir = new Directory();
        int flowId = (int)mad.getFlowId();
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        Leaf ft = dir.getLeaf(wf.getTypeCode());
        boolean isFree = ft.getType()!=Leaf.TYPE_LIST;

        int k = 0;
        if (isFree) {
            k = mad.recallMyActionsByPrivMyActionFree(myActionId);
        }
        else {
            // 检查下一节点是否处于延时状态，如果是，则撤回
            WorkflowActionDb wad = new WorkflowActionDb();
            wad = wad.getWorkflowActionDb((int)mad.getActionId());

            // 撤回事件，事件在撤回处理之前运行，以取得与其相关的节点信息。
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
            WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
            String script = wpm.getRecallScript(wpd.getScripts());
            if (script != null && !"".equals(script)) {
                Interpreter bsh = new Interpreter();
                try {
                    StringBuffer sb = new StringBuffer();

                    FormDAO fdao = new FormDAO();
                    fdao = fdao.getFormDAO(flowId, new FormDb(ft.getFormCode()));

                    // 赋值当前用户
                    sb.append("String userName=\"" + myname + "\";");
                    sb.append("int flowId=" + wf.getId() + ";");

                    bsh.set("request", request);
                    bsh.set("mad", mad);
                    bsh.set("action", wad);
                    bsh.set("fdao", fdao);

                    bsh.eval(BeanShellUtil.escape(sb.toString()));

                    bsh.eval(script);
                    Object obj = bsh.get("ret");
                    if (obj != null) {
                        boolean ret = ((Boolean) obj).booleanValue();
                        if (!ret) {
                            String errMsg = (String) bsh.get("errMsg");
                            LogUtil.getLog(getClass()).error("Recall bsh errMsg=" + errMsg);
                        }
                    }
                } catch (EvalError e) {
                    e.printStackTrace();
                }
            }

            boolean isDelayed = false;
            Vector va = wad.getLinkToActions();
            if (va.size()==1) {
                wad = (WorkflowActionDb)va.elementAt(0);
                if (wad.getStatus()==WorkflowActionDb.STATE_DELAYED) {
                    isDelayed = true;
                }
            }

            if (isDelayed) {
                if (wad.isCanPrivUserModifyDelayDate()) {
                    wad.setStatus(WorkflowActionDb.STATE_NOTDO);
                    wad.save();
                    k = 1;
                }
                else {
                	String str = LocalUtil.LoadString(request,"res.flow.Flow","illegalOperation");
                    throw new ErrMsgException(str);
                }
            }
            else {
                k = mad.recallMyActionsByPrivMyAction(myActionId);
            }
        }

        // LogUtil.getLog(getClass()).info("recallMyAction k=" + k + " mad.id=" + mad.getId());
        // 如果是自由流程，则允许最后一个节点的人撤回，即允许重新处理
        if (k>0 || (k==0 && isFree)) {
            mad.setChecked(false);
            re = mad.save();
            if (re) {
                // 置节点状态为正在办理状态
                WorkflowActionDb wad = new WorkflowActionDb();
                wad = wad.getWorkflowActionDb((int)mad.getActionId());
                wad.setStatus(WorkflowActionDb.STATE_DOING);
                wad.save();
                
                // 置流程为处理状态
                if (wf.getStatus()==WorkflowDb.STATUS_FINISHED) {
                	wf.setStatus(WorkflowDb.STATUS_STARTED);
                	wf.save();
                }
            }
        }
        return re;
    }

    public static String getLevelImg(HttpServletRequest request, WorkflowDb wf) {
        if (wf.getLevel()==WorkflowDb.LEVEL_IMPORTANT) {
            return "<img src='" + request.getContextPath() + "/images/important.png' align='absmiddle' title='"+ LocalUtil.LoadString(request, "res.flow.Flow","impor") +"'>&nbsp;";
        }
        else if (wf.getLevel()==WorkflowDb.LEVEL_URGENT) {
            return "<img src='" + request.getContextPath() + "/images/urgent.png' align='absmiddle' title='"+ LocalUtil.LoadString(request, "res.flow.Flow","emergent") +"'>&nbsp;";
        }
        else {
            return "<img src='" + request.getContextPath() + "/images/general.png' align='absmiddle' title='"+ LocalUtil.LoadString(request, "res.flow.Flow","ordi") +"'>&nbsp;";
        }
    }
    
    /**
     * 从FinishActionBatch中分离，因为在任务管理中需自动提交流程给受命人
     * @Description: 
     * @param request
     * @param mad 提交人的代办记录
     * @param userName 提交人
     * @param sb 用于收集出错信息
     * @return
     * @throws ErrMsgException
     */
    public boolean finishActionSingle(HttpServletRequest request, MyActionDb mad, String userName, StringBuffer sb) throws ErrMsgException {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb((int) mad.getFlowId());
        
        if (!wf.isStarted()) {
        	//errMsg += wf.getTitle() + " 尚未开始，不能批量提交！\r\n";
        	String str = LocalUtil.LoadString(request, "res.flow.Flow", "canNotSubmitBatchForNotStarted"); //%s 尚未开始，%s不能批量提交！\r\n
        	sb.append( StrUtil.format(str, new Object[]{wf.getTitle()}) + "\\r");
        	return false;            	
        }
        
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        if (lf==null) {
            return false;
        }

        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb((int)mad.getActionId());
        
        String reason = "";
        String result = "";
        int resultValue = 0; // WorkflowActionDb.RESULT_VALUE_AGGREE;

        if (lf.getType()==Leaf.TYPE_FREE) {
            WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
        	wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());
        	// @流程
        	if (wfpd.isLight()) {
        		 try {
                 	wa.changeStatusFree(request, wf, userName, WorkflowActionDb.STATE_FINISHED, reason, result, resultValue, mad.getId());
                 }
                 catch(ErrMsgException e) {
                 	sb.append( wf.getTitle() + "，" + e.getMessage() + "\r\n" );
                	return false;            	
                 }
                 
        		java.util.Date date = new java.util.Date();
        		if (!mad.isReaded()) {
        			mad.setReaded(true);
        			mad.setReadDate(date);
        		}
        		mad.setChecked(true);
        		mad.setCheckDate(date);
        		mad.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);
                mad.save();
        	} else {
            	String str = LocalUtil.LoadString(request, "res.flow.Flow", "notSubmitForFreeFlow"); //%s 尚未开始，%s不能批量提交！\r\n
            	sb.append( StrUtil.format(str, new Object[]{wf.getTitle()}) + "\r\n");
            	//errMsg += wf.getTitle() + " 是自由流程，不能批量提交！\r\n";
            	return false;            	
        	}
        } else {
        	Vector v = wa.getLinkToActions();
        	if (v.size() >= 2) {
        		String str = LocalUtil.LoadString(request, "res.flow.Flow", "distributionFlow"); //%s 分支流程，%s不能批量提交！\r\n
            	sb.append( StrUtil.format(str, new Object[]{wf.getTitle()}) + "\r\n");
            	return false;
        	}

            Config cfg = Config.getInstance();
            long tDebug = System.currentTimeMillis();
            try {
                boolean re = wa.changeStatus(request, wf, userName, WorkflowActionDb.STATE_FINISHED, reason, result, resultValue, mad.getId());
                if (re) {
                    if (cfg.getBooleanProperty("isDebugFlow")) {
                        DebugUtil.i(getClass(), "finishAction", "before onFinishAction: " + (System.currentTimeMillis() - tDebug) + " ms " + new Privilege().getUser(request));
                    }
                    onFinishAction(request, wf, wa, lf, mad, System.currentTimeMillis());
                    if (cfg.getBooleanProperty("isDebugFlow")) {
                        DebugUtil.i(getClass(), "finishAction", "after onFinishAction: " + (System.currentTimeMillis() - tDebug) + " ms " + new Privilege().getUser(request));
                    }
                }
            }
            catch(ErrMsgException e) {
            	sb.append( wf.getTitle() + "，" + e.getMessage() + "\r\n" );
            	e.printStackTrace();
            	// 20161229 fgf 如果批处理有问题返回false的话，则该myaction的处理状态为：已处理()，
            	// 而正常应为：已处理（同意），所以此处不应返回，以置为RESULT_VALUE_AGGREE
            	// 还是保留原样吧，要不然出现此类反馈的时候，会导致无法定位这个问题，另外，把批处理置为默认不启用
            	return false;
            }

            if (cfg.getBooleanProperty("isDebugFlow")) {
                DebugUtil.i(getClass(), "finishAction", "after onFinishAction: " + (System.currentTimeMillis() - tDebug) + " ms " + new Privilege().getUser(request));
            }

            // 虽然在wa.changeStatus中调用了WorkflowActionDb.changeMyActionDb对由id获取mad进行了setChecked(true)操作（但未置为RESULT_VALUE_AGGREE）
            // 但是为避免因参数为MyActionDb对象从而因缓存导致的脏数据问题，且为了保存RESULT_VALUE_AGGREE，此处再save一下
            mad.setChecked(true);
            mad.setCheckDate(new java.util.Date());
            mad.setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);
            mad.save();
        }
        // 自动存档
        if (wa != null && wa.isLoaded()) {
        	autoSaveArchive(request, wf, wa);
        }
                
        return true;
    }

    /**
     * 判断能否提交
     * @return
     * @throws ErrMsgException
     */
    public static boolean canSubmit(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb wa, MyActionDb mad, String myName, WorkflowPredefineDb wfp) throws ErrMsgException {
        if (!mad.isLoaded()) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "errorFlow");
            throw new ErrMsgException(str);
        } else if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_PASS) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "nodeByOtherPersonal");
            throw new ErrMsgException(str);
        } else if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "upcomingProcess");
            throw new ErrMsgException(str);
        } else if (mad.getCheckStatus() == MyActionDb.CHECK_STATUS_TRANSFER) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "beenAssigned");
            throw new ErrMsgException(str);
        }

        // 如果存在子流程，则处理子流程
        if (mad.getSubMyActionId() != MyActionDb.SUB_MYACTION_ID_NONE) {
            throw new ErrMsgException("节点上存在子流程");
        }

        if (!mad.getUserName().equalsIgnoreCase(myName) && !mad.getProxyUserName().equalsIgnoreCase(myName)) {
            // 权限检查
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

        if (wa == null || !wa.isLoaded()) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "actionNotExist");
            throw new ErrMsgException(str);
        }

        if (wf.getStatus() == WorkflowDb.STATUS_DELETED) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "flowDeleted");
            throw new ErrMsgException(str);
        }
        // 注释掉的原因：如果有结束节点，归档分支得继续往下走
/*        else if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
            // 如果流程不能变更
            if (!wfp.isReactive()) {
                throw new ErrMsgException("流程已结束，不能再处理");
            }
        }*/

        // 如果流程已结束，有可能待办流程窗口开着，但只需其中一人处理
        if (wa.getStatus() == WorkflowActionDb.STATE_DOING || wa.getStatus() == WorkflowActionDb.STATE_RETURN) {
            if (wf.getStatus() == WorkflowDb.STATUS_DISCARDED) {
                String str = LocalUtil.LoadString(request, "res.flow.Flow", "flowDiscarded");
                throw new ErrMsgException(str);
            }
        }
        else {
            // 有可能会是变更的情况，或者是异或聚合的情况
            if (!wfp.isReactive() && !wa.isXorAggregate()) {
                // 如果是wa已处理的情况，有可能出现当前用户提交后再反复刷新页面致进入此处
                /*
                if (mad.getCheckStatus() != MyActionDb.CHECK_STATUS_CHECKED) {
                            mad.setCheckStatus(MyActionDb.CHECK_STATUS_CHECKED);
                            mad.setCheckDate(new java.util.Date());
                            mad.setChecker(UserDb.SYSTEM);
                            mad.setResult(LocalUtil.LoadString(request, "res.flow.Flow", "systemAutoAccessedWhenDisposeNotDoing"));
                            mad.save();
                        }
                */
                String str = LocalUtil.LoadString(request, "res.flow.Flow", "mayHaveBeenProcess");
                throw new ErrMsgException(str);
            } else {
                if (wfp.isReactive()) {
                    if (wf.getStatus() != WorkflowDb.STATUS_FINISHED && wf.getStatus()!=WorkflowDb.STATUS_DISCARDED) {
                        // 流程未结束或放弃，不能变更
                        String str = LocalUtil.LoadString(request, "res.flow.Flow", "alterWhenNotFinish");
                        throw new ErrMsgException(str);
                    }

                    // 如果是变更，则检查本节点相连的其它节点上是否有待办记录
                    Vector vtDoing = mad.getMyActionDbOfToActionDoing();
                    if (vtDoing.size() > 0) {
                        // 后续节点尚未处理，不能变更流程！
                        String str = LocalUtil.LoadString(request, "res.flow.Flow", "reactiveErrSomemyactiondoing");
                        throw new ErrMsgException(str);
                    }
                }
                // 如果流程可以变更且流程状态为已放弃
                if (wfp.isReactive() && wf.getStatus() == WorkflowDb.STATUS_DISCARDED) {
                    if (mad.getCheckStatus() != MyActionDb.CHECK_STATUS_CHECKED) {
                        mad.setCheckStatus(MyActionDb.CHECK_STATUS_CHECKED);
                        mad.setCheckDate(new java.util.Date());
                        mad.setChecker(UserDb.SYSTEM);
                        mad.setResult(LocalUtil.LoadString(request, "res.flow.Flow", "systemAutoAccessedWhenDisposeIsReactive"));
                        mad.save();
                    }
                    wf.setStatus(WorkflowDb.STATUS_NOT_STARTED);
                    wf.save();
                }
            }
        }
        return true;
    }
    
    /**
     * 批量同意流程
     * @param request
     * @throws ErrMsgException 
     */
    public int FinishActionBatch(HttpServletRequest request) throws ErrMsgException {
    	Privilege pvg = new Privilege();
    	String userName = pvg.getUser(request);
    	String[] ids = StrUtil.split(ParamUtil.get(request, "ids"), ",");
    	
    	if (ids==null) {
    		return 0;
    	}
    	
    	int c = 0;
    	StringBuffer sb = new StringBuffer();

    	MyActionDb mad = new MyActionDb();
        WorkflowActionDb wa = new WorkflowActionDb();
        WorkflowPredefineDb wfp = new WorkflowPredefineDb();

        int len = ids.length;
        for (int i=0; i<len; i++) {
            // 检查能否提交
            mad = mad.getMyActionDb(StrUtil.toLong(ids[i]));

            int flowId = (int)mad.getFlowId();
            WorkflowDb wf = getWorkflowDb(flowId);

            int actionId = (int) mad.getActionId();
            wa = wa.getWorkflowActionDb(actionId);

            wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

            try {
                canSubmit(request, wf, wa, mad, userName, wfp);
            }
            catch (ErrMsgException e) {
                throw new ErrMsgException(wf.getTitle() + "，\r\n" + e.getMessage());
            }
        }

    	for (int i=0; i<len; i++) {
        	mad = mad.getMyActionDb(StrUtil.toLong(ids[i]));
        	finishActionSingle(request, mad, userName, sb);
            c++;
    	}
    	
    	if (sb.length()>0) {
    		throw new ErrMsgException(sb.toString());
    	}
    	
    	return c;
    }
    
    /**
     * 为某用户发起某类流程
     * @Description: 
     * @param userName
     * @param flowCode
     */
    public int startWorkflow(String userName, String flowCode) {
    	Leaf lf = new Leaf();
        lf = lf.getLeaf(flowCode);

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(flowCode);
        boolean isPredefined = wpd != null && wpd.isLoaded();
        if (!isPredefined) {
            LogUtil.getLog(getClass()).error(flowCode + " 预定义流程不存在！");
            return -1;
        }

        WorkflowDb wf = new WorkflowDb();
        MyActionDb mad = new MyActionDb();
        
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        
        int flowId = -1;
        
        WorkflowMgr wm = new WorkflowMgr();
        long myActionId = -1;
        try {
        	myActionId = wm.initWorkflow(userName, flowCode, lf.getName(), -1, WorkflowDb.LEVEL_NORMAL);
    		
        	mad = mad.getMyActionDb(myActionId);
            wf = wf.getWorkflowDb((int)mad.getFlowId());
    		wf.setStatus(WorkflowDb.STATUS_NOT_STARTED);
            wf.save();
            
            flowId = wf.getId();
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("execute:" + e.getMessage());
        }

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        Config cfg = new Config();
        IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
        boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
        String charset = Global.getSmtpCharset();
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
        String senderName = StrUtil.GBToUnicode(Global.AppName) + "<" + Global.getEmail() + ">";
        if (flowNotifyByEmail) {
            String mailserver = Global.getSmtpServer();
            int smtp_port = Global.getSmtpPort();
            String name = Global.getSmtpUser();
            String pwd_raw = Global.getSmtpPwd();
            boolean isSsl = Global.isSmtpSSL();
            try {
                sendmail.initSession(mailserver, smtp_port, name, pwd_raw, "", isSsl);
            } catch (Exception ex) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
            }
        }

        // 发送消息通知
        boolean isToMobile = SMSFactory.isUseSms;
        // 发送信息
        MessageDb md = new MessageDb();
        String t = "系统调度：" + lf.getName();
        String c = "系统已自动为您发起流程：" + lf.getName() + " 请及时办理！";
        try {
            String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + myActionId;
            if (mqIsOpen) {
                msgProducer.sendSysMsg(wf.getUserName(), t, c, action);
            }
            else {
                md.sendSysMsg(ud.getName(), t, c, action);
            }

            if (isToMobile) {
                if (mqIsOpen) {
                    msgProducer.sendSms(ud.getName(), c);
                }
                else {
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    if (imu != null) {
                        imu.send(ud, c, MessageDb.SENDER_SYSTEM);
                    }
                }
            }

            if (flowNotifyByEmail) {
                if (!ud.getEmail().equals("")) {
                    action = "op=show|userName=" + ud.getName() + "|" +
                            "flowId=" + wf.getId();
                    action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
                    UserSetupDb usd = new UserSetupDb(ud.getName());
                    c += "<BR /><BR />>>&nbsp;<a href='" +
                            Global.getFullRootPath() +
                            "/public/flow_dispose.jsp?action=" + action +
                            "' target='_blank'>" +
                            (usd.getLocal().equals("en-US") ? "Click here to view" : "请点击此处查看") + "</a>";
                    if (mqIsOpen) {
                        msgProducer.sendEmail(ud.getEmail(), senderName, t, c);
                    }
                    else {
                        sendmail.initMsg(ud.getEmail(), senderName, t, c, true);
                        sendmail.send();
                        sendmail.clear();
                    }
                }
            }
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("execute2:" + e.getMessage());
        }
        return flowId;
    }

    /**
     * 判断在流程处理时，节点上是否可以删除流程
     * @param request
     * @param wf
     * @param wa
     * @param mad
     * @return
     */
    public static boolean canDelFlowOnAction(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb wa, MyActionDb mad) {
        boolean canDel = false;
        if (wa.isStart==1 && wf.getStatus()==WorkflowDb.STATUS_NOT_STARTED) {
            canDel = true;
        }
        else {
            String flag = wa.getFlag();
            // 如果设了允许删除标志位
            if (flag.length()>=4 && flag.substring(3, 4).equals("1")) {
                WorkflowPredefineDb wfp = new WorkflowPredefineDb();
                wfp = wfp.getDefaultPredefineFlow(wf.getTypeCode());

                // 如果是被退回
                if (mad.getActionStatus()==WorkflowActionDb.STATE_RETURN) {
                    if (wfp.isCanDelOnReturn()) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    canDel = true;
                }
            }
        }
        return canDel;
    }
   
    /**
     * 删除流程
     * @param request
     * @param id
     * @return
     */
    public boolean del(HttpServletRequest request, int id) throws ErrMsgException {
    	WorkflowMgr wm = new WorkflowMgr();
    	WorkflowDb wf = wm.getWorkflowDb(id);
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());
        fdao = fdao.getFormDAO(wf.getId(), fd);

        LeafPriv lp = new LeafPriv(wf.getTypeCode());
        boolean canDel = lp.canUserExamine(pvg.getUser(request));

        if (!canDel) {
            // 判断用户当前所处理的节点是否有删除权限
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDbOfFlowDoingByUser(id, userName);
            WorkflowActionDb wa = new WorkflowActionDb();
            if (mad!=null) {
                wa = wa.getWorkflowActionDb((int)mad.getActionId());
                canDel = canDelFlowOnAction(request, wf, wa, mad);
            }

            if (canDel) {
                // 运行删除验证脚本
                FormDAOMgr formDAOMgr = new FormDAOMgr();
                formDAOMgr.runDeleteValidateScript(request, pvg, wf, fdao, wa, false);
            }
        }

        // 判断用户是否拥有管理权
        if (!canDel) {
            throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        }
    	// 20170508 fgf 逻辑删除
    	wf.setStatus(WorkflowDb.STATUS_DELETED);
        wf.setDelUser(userName);
        wf.setDelDate(new java.util.Date());
    	boolean re = wf.save();

        // 将表单置为被删除状态
        fdao.setStatus(com.redmoon.oa.flow.FormDAO.STATUS_DELETED);
        try {
            fdao.save();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return re;
    }


    /**
     * 流程干预，转交给某个节点
     * @param request
     * @param flowId 流程ID
     * @param myActionId 被转交的办理记录ID
     * @param users 转交给的用户名，如有多个以逗号分隔
     * @param userRealNames 转交给的用户姓名，如有多个以逗号分隔
     * @param internalName 将要转交至的节点的名称
     * @return
     * @throws ErrMsgException
     */
    public boolean deliverTo(HttpServletRequest request, int flowId, long myActionId, String users, String userRealNames, String internalName) throws ErrMsgException {
        boolean re = false;
        WorkflowActionDb nextwa = new WorkflowActionDb();
        nextwa = nextwa.getWorkflowActionDbByInternalName(internalName, flowId);

        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);

        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb((int)mad.getActionId());

        nextwa.setUserName(users);
        nextwa.setUserRealName(userRealNames);
        nextwa.save();

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        String deptOfUserWithMultiDept = null;
        wa.initTmpUserNameActived();
        wa.deliverToNextAction(request, wf, nextwa, myActionId, deptOfUserWithMultiDept);

        mad.setCheckDate(new Date());
        mad.setCheckStatus(MyActionDb.CHECK_STATUS_CHECKED);
        re = mad.save();

        // 因为deliverToNextAction可能会改变流程的状态，所以需重新获取
        wf = wf.getWorkflowDb(flowId);
        // 如果流程状态为“已结束”，则置为“处理中”
        if (wf.getStatus()==WorkflowDb.STATUS_FINISHED) {
            mad = mad.getLastMyActionDbOfFlow(wf.getId());
            long actionId = mad.getActionId();
            WorkflowActionDb lastAction = new WorkflowActionDb();
            lastAction = lastAction.getWorkflowActionDb((int)actionId);
            wf.changeStatus(request, WorkflowDb.STATUS_STARTED, lastAction);
        }

        boolean isUseMsg = true;
        boolean isToMobile = com.redmoon.oa.sms.SMSFactory.isUseSMS();

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail();
        String senderName = StrUtil.GBToUnicode(Global.AppName);
        senderName += "<" + Global.getEmail() + ">";
        if (flowNotifyByEmail) {
            String mailserver = Global.getSmtpServer();
            int smtp_port = Global.getSmtpPort();
            String name = Global.getSmtpUser();
            String pwd_raw = Global.getSmtpPwd();
            try {
                sendmail.initSession(mailserver, smtp_port, name, pwd_raw);
            } catch (Exception ex) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
            }
        }

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        String t = SkinUtil.LoadString(request,
                "res.module.flow",
                "msg_user_actived_title");
        String c = SkinUtil.LoadString(request,
                "res.module.flow",
                "msg_user_actived_content");
        String tail = WorkflowMgr.getFormAbstractTable(wf);

        UserMgr um = new UserMgr();

        MessageDb md = new MessageDb();
        Iterator ir = wa.getTmpUserNameActived().iterator();
        while (ir.hasNext()) {
            MyActionDb mad2 = (MyActionDb) ir.next();
            t = t.replaceFirst("\\$flowTitle", wf.getTitle());
            String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
            fc = fc.replaceFirst("\\$fromUser", wa.getUserRealName());

            if (isToMobile) {
                IMsgUtil imu = SMSFactory.getMsgUtil();
                if (imu != null) {
                    UserDb ud = um.getUserDb(mad2.getUserName());
                    imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                }
            }

            fc += tail;
            if (isUseMsg) {
                // 发送信息
                String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad2.getId();
                md.sendSysMsg(mad2.getUserName(), t, fc, action);
            }

            if (flowNotifyByEmail) {
                UserDb user = um.getUserDb(mad2.getUserName());
                if (user.getEmail()!=null && !user.getEmail().equals("")) {
                    String action = "userName=" + user.getName() + "|" +
                            "myActionId=" + mad2.getId();
                    action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(
                            ssoCfg.getKey(), action);
                    fc += "<BR />>>&nbsp;<a href='" +
                            Global.getFullRootPath(request) +
                            "/public/flow_dispose.jsp?action=" + action +
                            "' target='_blank'><lt:Label res='res.flow.Flow' key='clickHere'/></a>";
                    sendmail.initMsg(user.getEmail(),
                            senderName,
                            t, fc, true);
                    sendmail.send();
                    sendmail.clear();
                }
            }
        }
        return re;
    }

    /**
     * 流程干预，置待办记录的状态
     * @param myActionId
     * @param checkStatus
     * @return
     */
    public int setMyActionStatus(HttpServletRequest request, long myActionId, int checkStatus) throws ErrMsgException {
        boolean re = false;
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        int actionStatus = -1;
        if (mad.isLoaded()) {
            mad.setCheckStatus(checkStatus);
            re = mad.save();
            if (re) {
                WorkflowActionDb wa = new WorkflowActionDb();
                wa = wa.getWorkflowActionDb((int) mad.getActionId());
                // 如果新状态为未处理
                if (checkStatus == MyActionDb.CHECK_STATUS_NOT) {
                    DebugUtil.i(getClass(), "setMyActionStatus", String.valueOf(wa.getId()));
                    if (wa.getStatus() != WorkflowActionDb.STATE_DOING) {
                        actionStatus = WorkflowActionDb.STATE_DOING;
                        wa.setStatus(WorkflowActionDb.STATE_DOING);
                        try {
                            wa.save();
                        } catch (ErrMsgException e) {
                            e.printStackTrace();
                        }
                    }

                    // 如果流程状态为“已结束”，则置为“处理中”
                    WorkflowDb wf = new WorkflowDb();
                    wf = wf.getWorkflowDb((int) mad.getFlowId());
                    if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
                        mad = mad.getLastMyActionDbOfFlow(wf.getId());
                        long actionId = mad.getActionId();
                        WorkflowActionDb lastAction = new WorkflowActionDb();
                        lastAction = lastAction.getWorkflowActionDb((int) actionId);
                        re = wf.changeStatus(request, WorkflowDb.STATUS_STARTED, lastAction);
                    }
                } else if (checkStatus == MyActionDb.CHECK_STATUS_CHECKED) {
                    // 如果该节点上没有其它待办记录，则说明应置节点状态为已处理
                    Vector v = mad.getOthersOfActionDoing();
                    if (v.size() == 0) {
                        actionStatus = WorkflowActionDb.STATE_FINISHED;
                        wa.setStatus(WorkflowActionDb.STATE_FINISHED);
                        re = wa.save();
                    }
                }
            }
        }
        return actionStatus;
    }
}
