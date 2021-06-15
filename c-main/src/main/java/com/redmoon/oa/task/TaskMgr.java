package com.redmoon.oa.task;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.aop.*;
import com.cloudwebsoft.framework.aop.Pointcut.*;
import com.cloudwebsoft.framework.aop.base.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.message.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.Privilege;
import org.apache.log4j.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TaskMgr {
    String connname = Global.getDefaultDB();

    Logger logger = Logger.getLogger(TaskMgr.class.getName());
    Privilege privilege = null;
    FileUpload fileUpload = null;
    int ret = 0;
    String title, content, person, ip;
    int type;
    int expression;
    String filenm;
    String extname;
    int parentid;
    String filepath;
    String initiator;
    String privurl;
    String remark;
    int rootid;
    TaskPrivilege taskprivilege = null;

    public TaskMgr() {
        privilege = new Privilege();
        taskprivilege = new TaskPrivilege();
    }

    public void ReceiveData(ServletContext application, HttpServletRequest request) {
        fileUpload = new FileUpload();
        String[] extnames = {
                            "jpg", "gif", "zip", "rar", "doc", "rm", "avi", "xls",
                            "bmp",
                            "swf", "txt", "htm", "html", "png"};
        fileUpload.setValidExtname(extnames); // 设置可上传的文件类型
        fileUpload.setMaxFileSize(Global.FileSize); // 最大50K
        Config cfg = new Config();
        filepath = cfg.get("file_task");

        java.util.Date currentTime = new java.util.Date();
        filenm = String.valueOf(currentTime.getTime());
        try {
            ret = fileUpload.doUpload(application, request);
        } catch (IOException e) {
            logger.error("ReceiveData: " + e.getMessage());
        }
    }

    public boolean Check(HttpServletRequest req) throws ErrMsgException {
        if (ret == -3) {
            throw new ErrMsgException("您上传的文件太大,请把文件大小限制在" + Global.FileSize + "K以内!");
        }
        if (ret == -4) {
            throw new ErrMsgException(
                    "扩展名非法，注意扩展名必须为：jpg,gif,zip,rar,doc,rm,avi,bmp,swf,txt,htm,html,png");
        }

        String errMsg = "";

        String sisUseMsg = StrUtil.getNullStr(fileUpload.getFieldValue("isUseMsg"));
        if (sisUseMsg.equals("true"))
            isUseMsg = true;

        title = fileUpload.getFieldValue("title");
        if (title == null || title.trim().equals(""))
            errMsg += "请输入标题！";
        if (title!=null && title.length()>=255) {
            errMsg += "标题长度不能大于255！";
        }
        content = fileUpload.getFieldValue("content");
        try {
            type = Integer.parseInt(StrUtil.getNullStr(fileUpload.getFieldValue(
                    "type")));
            expression = Integer.parseInt(StrUtil.getNullStr(fileUpload.
                    getFieldValue("expression")));
        } catch (NumberFormatException e) {

        }
        ip = req.getRemoteAddr();
        person = StrUtil.getNullStr(fileUpload.getFieldValue("person"));
        privurl = StrUtil.getNullStr(fileUpload.getFieldValue("privurl"));

        beginDate = DateUtil.parse(fileUpload.getFieldValue("beginDate"), "yyyy-MM-dd");
        endDate = DateUtil.parse(fileUpload.getFieldValue("endDate"), "yyyy-MM-dd");

        if (type==TaskDb.TYPE_TASK || type==TaskDb.TYPE_SUBTASK) {
            if (beginDate==null || endDate==null)
                errMsg += "开始日期与结束日期不能为空！";
            if (DateUtil.compare(beginDate, endDate) == 1)
                errMsg += "开始日期不能大于结束日期！";
        }

        remark = StrUtil.getNullStr(fileUpload.getFieldValue("remark"));
        if (remark.length()>255) {
            errMsg += "备注长度不能大于255！";
        }

        secret = StrUtil.getNullStr(fileUpload.getFieldValue("secret")).equals("true");
        
        projectId = StrUtil.toLong(fileUpload.getFieldValue("projectId"), -1);

        if (!errMsg.equals("")) {
            throw new ErrMsgException(errMsg);
        }
        return true;
    }

    public String getprivurl() {
        return privurl;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getFilename() {
        return filenm;
    }

    public String getPerson() {
        return person;
    }

    public String getExtname() {
        return extname;
    }

    public int getType() {
        return type;
    }

    public int getRootid() {
        return rootid;
    }

    public int getExpression() {
        return expression;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getJobCode() {
        return jobCode;
    }

    public int getId() {
        return id;
    }

    public java.util.Date getBeginDate() {
        return beginDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public boolean getSecret() {
        return secret;
    }

    public boolean CheckReply(HttpServletRequest request) throws
            ErrMsgException {
        Check(request);
        String strparentid = fileUpload.getFieldValue("parentid");
        String sisUseMsg = StrUtil.getNullStr(fileUpload.getFieldValue("isUseMsg"));
        if (sisUseMsg.equals("true"))
            isUseMsg = true;
        if (strparentid == null)
            throw new ErrMsgException("缺少任务标识！");
        parentid = Integer.parseInt(strparentid);
        if (type == 2)
            if (!taskprivilege.canAddResult(request, parentid))
                throw new ErrMsgException("您无权汇报结果！");
        if (type == 3)
            if (!taskprivilege.canHurry(request, parentid))
                throw new ErrMsgException("您无权催办！");

        if (person.equals(""))
            throw new ErrMsgException("用户名不能为空！");
        secret = StrUtil.getNullStr(fileUpload.getFieldValue("secret")).equals("true");

        return true;
    }


    public boolean uploadDocument(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        FileUpload fu = new FileUpload();
        fu.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K
        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret != fu.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage());
            }
        }
        catch (Exception e) {
            logger.error("uploadDocument:" + e.getMessage());
        }

        if (ret == fu.RET_SUCCESS) {
            String strId = fu.getFieldValue("taskId");
            String strAttachId = fu.getFieldValue("attachId");

            if (!StrUtil.isNumeric(strId))
                throw new ErrMsgException("id 必须为数字");

            if (!taskprivilege.canEdit(request, StrUtil.toInt(strId)))
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));

            if (!StrUtil.isNumeric(strAttachId))
                throw new ErrMsgException("attachId 必须为数字");

            TaskDb td = new TaskDb();
            td = td.getTaskDb(StrUtil.toInt(strId));
            if (td==null || !td.isLoaded()) {
                throw new ErrMsgException("任务不存在！");
            }
            Attachment att = td.getAttachment(StrUtil.toInt(strAttachId));

            com.redmoon.kit.util.FileInfo fi = (com.redmoon.kit.util.FileInfo)(fu.getFiles().get(0));
            return fi.write(Global.getRealPath() + att.getVisualPath() + "/", att.getDiskName());
        }
        else
            return false;
    }

    public boolean Add(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        ReceiveData(application, request);
        String op = fileUpload.getFieldValue("op");
        initiator = privilege.getUser(request);

        if (op.equals("new")) {
            return AddNew(request);
        } else if (op.equals("newflowtask")) {
            return AddNewOfWorkflowAction(request);
        } else
            return AddReply(request);
    }

    /**
     * 添加对应于流程中action的任务
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean AddNewOfWorkflowAction(HttpServletRequest request) throws
            ErrMsgException {
        try {
            Check(request);
        } catch (ErrMsgException e) {
            throw e;
        }
        String sql = "";
        int length = 0;
        if (content != null)
            length = content.length();

        String strActionId = fileUpload.getFieldValue("actionId");
        if (!StrUtil.isNumeric(strActionId))
            throw new ErrMsgException("actionId必须为数字！");
        int actionId = Integer.parseInt(strActionId);

        String[] strAttachIds = fileUpload.getFieldValues("attachIds");

        TaskDb td = new TaskDb();
        td.setJobCode(jobCode);
        td.setInitiator(initiator);
        td.setType(type);
        td.setTitle(title);
        td.setContent(content);
        td.setPerson(person);
        td.setExpression(expression);
        td.setIp(ip);
        td.setActionId(actionId);
        td.setBeginDate(beginDate);
        td.setEndDate(endDate);
        td.setSecret(secret);

        if (ret != fileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fileUpload.getErrMessage());
        }

        boolean re = td.create();
        this.id = td.getId();
        this.rootid = td.getRootId();
        // 保存上传的文件
        if (re) {
            WorkflowActionDb wfa = new WorkflowActionDb();
            wfa = wfa.getWorkflowActionDb(actionId);
            wfa.setTaskId(td.getId());
            wfa.save(); // 保存动作中的任务ID

            // fileUpload.writeFile();
            if (fileUpload.getRet() == fileUpload.RET_SUCCESS) {
                Vector v = fileUpload.getFiles();
                FileInfo fi = null;
                Iterator ir = v.iterator();
                String vpath = "";
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                vpath = filepath + "/" +
                        year + "/" + month + "/";
                String fullpath = fileUpload.getRealPath() + vpath;
                while (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    String newfilename = fileUpload.getRandName();
                    fi.write(fullpath,
                             newfilename + "." + fi.getExt());

                    Attachment att = new Attachment();
                    att.setTaskId(td.getId());
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setFullPath(fullpath + newfilename + "." + fi.getExt() );
                    att.setVisualPath(vpath);
                    att.create();
                }
            }

            // 保存流程中动作的附件
            int len = 0;
            if (strAttachIds!=null)
                len = strAttachIds.length;

            com.redmoon.oa.flow.Attachment flowAtt;
            for (int i = 0; i < len; i++) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                String vpath = filepath + "/" +
                        year + "/" + month + "/";

                // System.out.println("attachId:" + strAttachIds[i]);
                flowAtt = new com.redmoon.oa.flow.Attachment(Integer.parseInt(strAttachIds[i]));
                Attachment att = new Attachment();
                att.setTaskId(td.getId());
                att.setName(flowAtt.getName());
                att.setDiskName(flowAtt.getDiskName());
                att.setFullPath(flowAtt.getFullPath());
                att.setVisualPath(flowAtt.getVisualPath());
                if (att.create()) {
                    // 将工作流中的文件拷贝至任务督办的文件夹中
                    String fullpath = fileUpload.getRealPath() + vpath + att.getDiskName();
                    FileUtil.CopyFile(flowAtt.getFullPath(), fullpath);
                }
            }
        }

        return re;
    }

    public boolean AddNew(HttpServletRequest request) throws
            ErrMsgException {
        try {
            Check(request);
        } catch (ErrMsgException e) {
            throw e;
        }

        TaskDb td = new TaskDb();
        td.setJobCode(jobCode);
        td.setInitiator(initiator);
        td.setType(type);
        td.setTitle(title);
        td.setContent(content);
        td.setPerson(person);
        td.setExpression(expression);
        td.setIp(ip);
        td.setBeginDate(beginDate);
        td.setEndDate(endDate);
        td.setRemark(remark);
        td.setSecret(secret);
        td.setProjectId(projectId);
        
        Privilege pvg = new Privilege();
        td.setUnitCode(pvg.getUserUnitCode(request));

        // LogUtil.getLog(getClass()).info(getClass() + " beginDate=" + DateUtil.format(beginDate, "yyyy-MM-dd"));
        /*
        if (type==TaskDb.TYPE_SUBTASK) {
            td.setStatus(TaskDb.STATUS_RECEIVED);
        }
        */

        if (ret != FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fileUpload.getErrMessage());
        }

        boolean re = td.create();
        this.id = td.getId();
        this.rootid = td.getRootId();
        // 保存上传的文件
        if (re) {
            // fileUpload.writeFile();
            if (fileUpload.getRet() == FileUpload.RET_SUCCESS) {
                Vector v = fileUpload.getFiles();
                FileInfo fi = null;
                Iterator ir = v.iterator();
                String vpath = "";
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                vpath = filepath + "/" +
                        year + "/" + month + "/";
                String fullpath = fileUpload.getRealPath() + vpath;
                while (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    String newfilename = FileUpload.getRandName();
                    fi.write(fullpath,
                             newfilename + "." + fi.getExt());

                    Attachment att = new Attachment();
                    att.setTaskId(td.getId());
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setFullPath(fullpath + newfilename + "." + fi.getExt() );
                    att.setVisualPath(vpath);
                    att.create();
                }
            }
        }

        return re;
    }

    public TaskDb getTaskDb(int id) {
        TaskDb td = new TaskDb();
        return td.getTaskDb(id);
    }

    public boolean AddReply(HttpServletRequest request) throws
            ErrMsgException {
        // 回复合法性检查
        CheckReply(request);

        int isfinish = 0;

        // 取出被回复的任务的有关信息
        int recount = 0, layer = 1, orders = 1, rootid = -1, parentorders = 1;
        int parentlayer = 1, parentreplyid = -1;
        boolean isroot = false;
        ResultSet rs = null;
        String sql;

        TaskDb parentTd = getTaskDb(parentid);
        if (parentTd != null && parentTd.isLoaded()) {
            recount = parentTd.getReCount();
            parentlayer = parentTd.getLayer();
            layer = parentlayer + 1;
            rootid = parentTd.getRootId();
            // System.out.println("rootid=" + rootid + " parent id=" + parentTd.getId());
            parentreplyid = parentTd.getParentId();
            if (parentreplyid == TaskDb.NOPARENT) { // 当被回复任务为根任务时
                isroot = true;
            } else
                recount = 0;
            parentorders = parentTd.getOrders();

            isfinish = parentTd.getStatus();
        } else
            throw new ErrMsgException("被回复的任务不存在！");

        TaskDb rootTd = getTaskDb(parentTd.getRootId());
        if (!isroot) { // 如果被回贴不是根任务则从根任务中取出回复数
            recount = rootTd.getReCount();
            isfinish = rootTd.getStatus();
        }
        if (isfinish == 1)
            throw new ErrMsgException("该任务已结束!");

        boolean updateorders = true;

        Conn conn = new Conn(connname);
        try {
            if (isroot && recount == 0)
                orders = parentorders + 1; // 如果是根贴且尚未有回贴，则orders=parentorders=1;
            else {
                if (parentreplyid == -1) { // 父结点为根贴
                    orders = recount + 2;
                    updateorders = false;
                } else {
                    // 取出被回任务的下一个兄弟结点或当无兄弟结点时取最靠近的layer较小的结点的orders
                    sql = "select min(orders) from task where rootid=" + rootid + " and orders>" +
                          parentorders +
                          " and layer<=" + parentlayer;
                    rs = conn.executeQuery(sql);
                    if (rs != null && rs.next())
                        orders = rs.getInt(1); // 如果orders=0，则表示未搜索到符合条件的贴子，回贴是位于最后的一个节点

                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                    if (orders == 0) {
                        updateorders = false;
                        orders = recount + 2;
                    }
                }
            }

            recount++;
            rootTd.setReCount(recount);
            rootTd.save();

            if (updateorders) {
                sql = "select id from task where rootid=" + rootid +
                      " and orders>=" + orders;
                rs = conn.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        TaskDb t = parentTd.getTaskDb(rs.getInt(1));
                        t.setOrders(t.getOrders() + 1);
                        t.save();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("AddReply:" + e.getMessage());
            throw new ErrMsgException("AddReply: 数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        TaskDb task = new TaskDb();
        task.setInitiator(privilege.getUser(request));
        task.setPerson(person);
        task.setType(type);
        task.setParentId(parentid);
        task.setTitle(title);
        task.setContent(content);
        task.setExpression(expression);
        task.setIp(ip);
        task.setOrders(orders);
        task.setLayer(layer);
        task.setRootId(rootid);
        task.setJobCode(jobCode);
        task.setBeginDate(beginDate);
        task.setEndDate(endDate);
        task.setSecret(secret);
        task.setProjectId(projectId);

        task.setUnitCode(privilege.getUserUnitCode(request));

        boolean re = task.create();
        this.id = task.getId();
        this.rootid = task.getRootId();
        if (re) {
            // 保存文件
            if (fileUpload.getRet() == FileUpload.RET_SUCCESS) {
                Vector v = fileUpload.getFiles();
                FileInfo fi = null;
                Iterator ir = v.iterator();
                String vpath = "";
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                vpath = filepath + "/" +
                        year + "/" + month + "/";
                String fullpath = fileUpload.getRealPath() + vpath;
                while (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    String newfilename = FileUpload.getRandName();
                    fi.write(fullpath,
                             newfilename + "." + fi.getExt());

                    Attachment att = new Attachment();
                    att.setTaskId(task.getId());
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setFullPath(fullpath + newfilename + "." + fi.getExt());
                    att.setVisualPath(vpath);
                    att.create();
                }
            }

            // 发送信息
            boolean isToMobile = StrUtil.getNullStr(fileUpload.getFieldValue(
                    "isToMobile")).equals("true");
            // System.out.println("TaskMgr.java: isToMobile=" + isToMobile + " isUseMsg=" + isUseMsg);
            IMessage imsg = null;
            String t = SkinUtil.LoadString(request,
                                           "res.module.task",
                                           "msg_create_title");
            String c = SkinUtil.LoadString(request,
                                           "res.module.task",
                                           "msg_create_content");
            if (isToMobile && isUseMsg) {
                ProxyFactory proxyFactory = new ProxyFactory(
                        "com.redmoon.oa.message.MessageDb");
                Advisor adv = new Advisor();
                MobileAfterAdvice mba = new MobileAfterAdvice();
                adv.setAdvice(mba);
                adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
                proxyFactory.addAdvisor(adv);
                imsg = (IMessage) proxyFactory.getProxy();
                if (type == TaskDb.TYPE_SUBTASK) {
                    t = t.replaceFirst("\\$title", title);
                    c = c.replaceFirst("\\$content",
                                       StrUtil.getAbstract(request, content, 50, "") + "...");
                    imsg.sendSysMsg(person, t, c);
                } else if (type == TaskDb.TYPE_HURRY) {
                    t = SkinUtil.LoadString(request,
                                            "res.module.task",
                                            "msg_hurry_title");
                    c = SkinUtil.LoadString(request,
                                            "res.module.task",
                                            "msg_hurry_content");
                    t = t.replaceFirst("\\$title", title);
                    c = c.replaceFirst("\\$content",
                                       StrUtil.getAbstract(request, content, 50, "") + "...");
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(initiator);
                    c = c.replaceFirst("\\$initiator", ud.getRealName());
                    imsg.sendSysMsg(person, t, c);
                }
                else if (type==TaskDb.TYPE_RESULT) {
                    t = SkinUtil.LoadString(request,
                                                   "res.module.task",
                                                   "msg_result_title");
                    c = SkinUtil.LoadString(request,
                                                   "res.module.task",
                                           "msg_result_content");
                    t = t.replaceFirst("\\$title", title);
                    c = c.replaceFirst("\\$content",
                                       StrUtil.getAbstract(request, content, 50, "") + "...");

                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(person);
                    c += "   (" + ud.getRealName() + ")";

                    imsg.sendSysMsg(parentTd.getInitiator(), t, c);

                    imsg.sendSysMsg(rootTd.getInitiator(), t, c);
                }
            } else if (isUseMsg) {
                MessageDb md = new MessageDb();
                // 发送信息
                if (type == TaskDb.TYPE_SUBTASK) {
                    t = t.replaceFirst("\\$title", title);
                    c = c.replaceFirst("\\$content",
                                       StrUtil.getAbstract(request, content, 50, "") + "...");
                    md.sendSysMsg(person, t, c);
                } else if (type == TaskDb.TYPE_HURRY) {
                    t = SkinUtil.LoadString(request,
                                            "res.module.task",
                                            "msg_hurry_title");
                    c = SkinUtil.LoadString(request,
                                            "res.module.task",
                                            "msg_hurry_content");
                    t = t.replaceFirst("\\$title", title);
                    c = c.replaceFirst("\\$content",
                                       StrUtil.getAbstract(request, content, 50, "") + "...");
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(initiator);
                    c = c.replaceFirst("\\$initiator", ud.getRealName());
                    md.sendSysMsg(person, t, c);
                }
                else if (type==TaskDb.TYPE_RESULT) {
                    t = SkinUtil.LoadString(request,
                                                   "res.module.task",
                                                   "msg_result_title");
                    c = SkinUtil.LoadString(request,
                                                   "res.module.task",
                                           "msg_result_content");
                    t = t.replaceFirst("\\$title", title);
                    c = c.replaceFirst("\\$content",
                                       StrUtil.getAbstract(request, content, 50, "") + "...");

                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(person);
                    c += "   (" + ud.getRealName() + ")";

                    md.sendSysMsg(parentTd.getInitiator(), t, c);

                    md.sendSysMsg(rootTd.getInitiator(), t, c);
                }
            }

        }
        return re;
    }

    public boolean del(HttpServletRequest request, int delid) throws
            ErrMsgException {
        TaskDb td = new TaskDb();
        return td.del(delid);
    }

    public boolean edit(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        ReceiveData(application, request);

        String editid = fileUpload.getFieldValue("editid");
        if (editid == null || !StrUtil.isNumeric(editid)) {
            throw new ErrMsgException("编辑标识非法！");
        }

        int id = Integer.parseInt(editid);

        if (!taskprivilege.canEdit(request, id))
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));

        TaskDb td = getTaskDb(id);

        Check(request);

        java.text.SimpleDateFormat formatter
                = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //将显示"1999-10-1 21:03:10"的格式.
        Calendar cal = Calendar.getInstance();
        java.util.Date currentTime = cal.getTime();
        String dateString = formatter.format(currentTime);

        UserDb ud = new UserDb();
        ud = ud.getUserDb(privilege.getUser(request));
        content += "<p align=right>该项已被" + ud.getRealName() + "编辑于" +
                dateString + "</p>";

        td.setTitle(title);
        td.setContent(content);
        td.setPerson(person);
        td.setExpression(expression);
        td.setIp(ip);
        td.setBeginDate(beginDate);
        td.setEndDate(endDate);
        td.setRemark(remark);
        td.setSecret(secret);

        boolean re = td.save();
        if (re) {
            if (fileUpload.getRet() == fileUpload.RET_SUCCESS) {
                Vector v = fileUpload.getFiles();
                FileInfo fi = null;
                Iterator ir = v.iterator();
                String vpath = "";
                // 置保存路径
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                vpath = filepath + "/" +
                        year + "/" + month + "/";
                String fullpath = fileUpload.getRealPath() + vpath;
                while (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    String newfilename = fileUpload.getRandName();
                    fi.write(fullpath,
                             newfilename + "." + fi.getExt());

                    Attachment att = new Attachment();
                    att.setTaskId(td.getId());
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setFullPath(fullpath + newfilename + "." + fi.getExt() );
                    att.setVisualPath(vpath);
                    att.create();
                }

                String t = SkinUtil.LoadString(request,
                                               "res.module.task",
                                               "msg_modify_title");
                String c = SkinUtil.LoadString(request,
                                               "res.module.task",
                                               "msg_modify_content");
                MessageDb md = new MessageDb();
                t = t.replaceFirst("\\$title", title);
                c = c.replaceFirst("\\$content",
                                   StrUtil.getAbstract(request, content, 50, "") + "...");
                boolean isToMobile = StrUtil.getNullStr(fileUpload.getFieldValue("isToMobile")).equals("true");

                // System.out.println("addReply isToMobile=" + isToMobile + " isUseMsg=" + isUseMsg + " person=" + person);

                if (isToMobile && isUseMsg &&  !td.getInitiator().equals(person)) {
                    IMessage imsg = null;
                    ProxyFactory proxyFactory = new ProxyFactory(
                            "com.redmoon.oa.message.MessageDb");
                    Advisor adv = new Advisor();
                    MobileAfterAdvice mba = new MobileAfterAdvice();
                    adv.setAdvice(mba);
                    adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
                    proxyFactory.addAdvisor(adv);
                    imsg = (IMessage) proxyFactory.getProxy();
                    imsg.sendSysMsg(person, t, c);
                }
                else if (isUseMsg && !td.getInitiator().equals(person)) {

                    md.sendSysMsg(person, t, c);
                }
            }
        }
        return re;
    }

    public boolean changeFinish(HttpServletRequest request, int taskid) throws
            ErrMsgException {
        TaskDb td = getTaskDb(taskid);
        if (!taskprivilege.canChangeStatus(request, td))
            throw new ErrMsgException("您无权改变任务状态！");
        String isfinish = request.getParameter("isfinish");
        if (!StrUtil.isNumeric(isfinish))
            throw new ErrMsgException("请输入正确的参数！");

        td.setStatus(Integer.parseInt(isfinish));
        
        if (isfinish.equals("" + TaskDb.STATUS_FINISHED))
        	td.setProgress(100);
        
        return td.save();
    }

    public Vector getUserJoinTask(String username) {
        TaskDb td = new TaskDb();
        return td.getUserJoinTask(username);
    }

    /**
     * 取得作为承办人未完成的任务
     * @param username String 承办人
     * @return String
     */
    public Vector getUserNotFinishedTask(String username) {
        TaskDb td = new TaskDb();
        return td.getUserNotFinishedTask(username);
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBeginDate(java.util.Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    private String jobCode;
    private int id;
    private boolean isUseMsg = true;
    private java.util.Date beginDate;
    private java.util.Date endDate;
    private boolean secret = false;
    private long projectId = -1;

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

}
