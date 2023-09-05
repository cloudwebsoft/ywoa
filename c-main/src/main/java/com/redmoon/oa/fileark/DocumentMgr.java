package com.redmoon.oa.fileark;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IMsgProducer;
import com.cloudweb.oa.api.IWorkflowProService;
import com.cloudweb.oa.api.IWorkflowUtil;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.DesktopUnit;
import com.redmoon.oa.ui.PortalDb;
import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.base.IPrivilege;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.kit.util.FileUploadExt;
import com.redmoon.oa.fileark.plugin.PluginMgr;
import com.redmoon.oa.fileark.plugin.PluginUnit;
import com.redmoon.oa.fileark.plugin.base.IPluginDocumentAction;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.util.Pdf2Html;

public class DocumentMgr {
    public static final int EXAMINE_FLOW_ID_NONE = -1;
    private int examineFlowId = EXAMINE_FLOW_ID_NONE;

    public DocumentMgr() {
    }

    public Document getDocument(int id) {
        Document doc = new Document();
        return doc.getDocument(id);
    }

    public Document getDocument(HttpServletRequest request, int id, IPrivilege privilege) throws
            ErrMsgException {
        boolean isValid = false;
        Document doc = getDocument(id);
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (lp.canUserSee(privilege.getUser(request))) {
            return doc;
        }

        if (!isValid) {
            throw new ErrMsgException(Privilege.MSG_INVALID);
        }
        return getDocument(id);
    }

    /**
     * 当directory的结点code的类型为文章时，取其文章，如果文章不存在，则创建文章
     * @param request HttpServletRequest
     * @param code String
     * @param privilege IPrivilege
     * @return Document
     * @throws ErrMsgException
     */
    public Document getDocumentByCode(HttpServletRequest request, String code,
                                      IPrivilege privilege) throws
            ErrMsgException {
        boolean isValid = false;

        LeafPriv lp = new LeafPriv(code);
        if (lp.canUserSee(privilege.getUser(request))) {
            isValid = true;
        }

        if (!isValid) {
            throw new ErrMsgException(Privilege.MSG_INVALID);
        }
        Document doc = new Document();
        int id = doc.getIDOrCreateByCode(code, privilege.getUser(request));
        return getDocument(id);
    }

    public CMSMultiFileUploadBean doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = new CMSMultiFileUploadBean();
        mfu.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        // String[] ext = {"htm", "gif", "bmp", "jpg", "png", "rar", "doc", "hs", "ppt", "rar", "zip", "jar"};
        // mfu.setValidExtname(ext);
        
		// 20170814 fgf
		com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
		String exts = cfg.get("filearkFileExt").replaceAll("，", ",");
		if (exts.equals("*")) {
			exts = "";
		}
		String[] ext = StrUtil.split(exts, ",");
		if (ext != null) {
            mfu.setValidExtname(ext);
        }
		
        int ret = 0;
        // LogUtil.getLog(getClass()).info("ret=" + ret);
        try {
        	// mfu.setDebug(true);
            ret = mfu.doUpload(application, request);

            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(mfu.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        return mfu;
    }

    public void passExamineBatch(HttpServletRequest request) throws
            ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids == null) {
            return;
        }
        String isSendMsg = ParamUtil.get(request, "sendMessage");
        
        int len = ids.length;
        Document doc = null;

        MessageDb md = new MessageDb();
        Privilege privilege = new Privilege();
        for (int i = 0; i < len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            if (doc.getExamine() != Document.EXAMINE_PASS) {
                LeafPriv lp = new LeafPriv(doc.getDirCode());
                if (lp.canUserExamine(privilege.getUser(request))) {
                    if (doc.UpdateExamine(Document.EXAMINE_PASS)) {
                        if (isSendMsg.equals("1")) {
                            md.sendSysMsg(doc.getNick(), "文件：" + doc.getTitle() + " 审核通过", "审核通过");
                        }

                        // 如果文件在门户中的目录上，则提醒有权限查看的人员
                        remind(request, doc);
                    }
                } else {
                    throw new ErrMsgException(Privilege.MSG_INVALID);
                }
            }
        }
    }

    public void unpassExamineBatch(HttpServletRequest request) throws
            ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids == null) {
            return;
        }
        String isSendMsg = ParamUtil.get(request, "sendMessage");

        int len = ids.length;
        Document doc = null;

        MessageDb md = new MessageDb();
        Privilege privilege = new Privilege();
        for (int i = 0; i < len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            if (doc.getExamine() != Document.EXAMINE_NOTPASS) {
                LeafPriv lp = new LeafPriv(doc.getDirCode());
                if (lp.canUserExamine(privilege.getUser(request))) {
                    if (doc.UpdateExamine(Document.EXAMINE_NOTPASS)) {
                        if (isSendMsg.equals("1")) {
                            md.sendSysMsg(doc.getNick(), "文件：" + doc.getTitle() + " 审核不通过", "审核不通过");
                        }
                    }
                } else {
                    throw new ErrMsgException(Privilege.MSG_INVALID);
                }
            }
        }
    }

    public boolean resumeBatch(HttpServletRequest request) throws ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return false;

        int len = ids.length;
        boolean re = false;
        Document doc = null;
        for (int i=0; i<len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            re = doc.UpdateExamine(Document.EXAMINE_PASS);
        }
        return re;
    }

    public boolean resume(HttpServletRequest request, int id) throws ErrMsgException {
        Document doc = getDocument(id);
        return doc.UpdateExamine(Document.EXAMINE_PASS);
    }

    public void clearDustbin(HttpServletRequest request) throws ErrMsgException {
        Document doc = new Document();
        doc.clearDustbin();
    }
    
    public boolean delBatch(HttpServletRequest request, boolean isDustbin) throws ErrMsgException {
        Privilege privilege = new Privilege();
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return false;
        boolean re = false;
        int len = ids.length;
        for (int i=0; i<len; i++) {
            // doc = getDocument(Integer.parseInt(ids[i]));
            // doc.del();
            re = del(request, Integer.parseInt(ids[i]), privilege, isDustbin);
        }
        return re;
    }

    public boolean publish(HttpServletRequest request, String userName, String[] ids) throws ErrMsgException {
        int len = ids.length;
        Leaf leaf = new Leaf();
        boolean re = false;
        for (int i=0; i<len; i++) {
            Document doc = getDocument(Integer.parseInt(ids[i]));
            leaf = leaf.getLeaf(doc.getDirCode());
            if (leaf == null) {
                throw new ErrMsgException(doc.getTitle() + " 的目录不存在");
            }
            LeafPriv lp = new LeafPriv(leaf.getCode());
            // 当由草稿状态转发布时的状态
            int examineWhenPublish = Document.EXAMINE_NOT;
            if (!leaf.isExamine() || lp.canUserExamine(userName)) {
                examineWhenPublish = Document.EXAMINE_PASS;
            }
            doc.setExamine(examineWhenPublish);
            re = doc.save();
            if (re) {
                if (doc.getExamine()==Document.EXAMINE_NOT) {
                    // 如果需流程审批，则自动发起流程，并提交至下一步
                    if (leaf.isExamine()) {
                        // 判断节点是否需通过流程审核
                        if (!Leaf.FLOW_TYPE_CODE_NONE.equals(leaf.getFlowTypeCode())) {
                            // 自动发起流程
                            Privilege pvg = new Privilege();
                            initExamineFlow(request, pvg.getUser(request), leaf, doc);
                        }
                    }
                }
            }
        }
        return re;
    }

    public boolean Operate(ServletContext application,
                           HttpServletRequest request, IPrivilege privilege) throws ErrMsgException {
        CMSMultiFileUploadBean mfu = doUpload(application, request);
        
        fileUpload = mfu;
        
        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));
        
        // 编辑时如果当前文档所在目录为不在前台显示,则dir_code为空
        if (dir_code == null || dir_code.equals("")) {
        	dir_code = ParamUtil.get(request, "dir_code");
        	mfu.setFieldValue("dir_code", dir_code);
        }

        dirCode = dir_code;
        Document doc = new Document();

        // LogUtil.getLog(getClass()).info("op=" + op);
        boolean isValid = false;
        if (op.equals("contribute")) { // || privilege.isValid(request)) { //isAdmin(user, pwdmd5)) {
            isValid = true;
        }
        else {
            LeafPriv lp = new LeafPriv();
            lp.setDirCode(dir_code);
            if (op.equals("edit")) {
                String idstr = StrUtil.getNullString(mfu.getFieldValue("id"));
                if (!StrUtil.isNumeric(idstr)) {
                    throw new ErrMsgException("标识id=" + idstr + "非法，必须为数字！");
                }
                id = Integer.parseInt(idstr);
                doc = doc.getDocument(id);

                if (lp.canUserModify(privilege.getUser(request))) {
                    isValid = true;
                }
                else {
                    if (doc.getAuthor().equals(privilege.getUser(request))) {
                        if (doc.getExamine() == Document.EXAMINE_NOT) {
                            throw new ErrMsgException("文章正在审核中，不能编辑");
                        }
                        Config cfg = Config.getInstance();
                        double filearkUserDelIntervalH = StrUtil.toDouble(cfg.get("filearkUserEditDelInterval"), 0);
                        double intervalMinute = filearkUserDelIntervalH * 60;
                        if (DateUtil.datediffMinute(new Date(), doc.getCreateDate()) < intervalMinute) {
                            isValid = true;
                        }
                        else {
                            throw new ErrMsgException("已超时，发布后" + filearkUserDelIntervalH + "小时内可修改");
                        }
                    }
                }
            }
            else {
                if (lp.canUserAppend(privilege.getUser(request))) {
                    isValid = true;
                }
            }
        }
        if (!isValid) {
            throw new ErrMsgException(Privilege.MSG_INVALID);
        }

        if (op.equals("edit")) {
            document = doc;
            int examine = doc.getExamine();

            // LeafPriv lp = new LeafPriv(doc.getDirCode());
            /*if (!lp.canUserModify(privilege.getUser(request))) {
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
            }*/
            boolean re = doc.Update(application, mfu);
            if (re) {
                // 设置权限
                String deptRoleGroup = mfu.getFieldValue("deptRoleGroup");
                // 清空原来的浏览权限，同时具有其它权限的不删除，这些有可能是管理员设的
                DocPriv docPriv = new DocPriv();
                Vector<DocPriv> v = docPriv.listByDocId(doc.getId());
                Iterator<DocPriv> ir = v.iterator();
                while (ir.hasNext()) {
                    docPriv = ir.next();
                    if (docPriv.getSee()==1) {
                        docPriv.del();
                    }
                }

                if (deptRoleGroup!=null && !"".equals(deptRoleGroup)) {
                    JSONArray arr = JSONArray.parseArray(deptRoleGroup);
                    v = docPriv.listByDocId(doc.getId());
                    for (int i=0; i<arr.size(); i++) {
                        JSONObject json = arr.getJSONObject(i);
                        String code = json.getString("code");
                        String kind = json.getString("kind");
                        int type = StrUtil.toInt(kind, DocPriv.TYPE_DEPT);
                        // 判断原来是否已存在该权限
                        boolean isFound = false;
                        ir = v.iterator();
                        while (ir.hasNext()) {
                            docPriv = ir.next();
                            if (docPriv.getName().equals(code) && docPriv.getType()==type) {
                                isFound = true;
                                break;
                            }
                        }

                        if (isFound) {
                            // 如果原来不能浏览，则置为可以浏览
                            if (docPriv.getSee()!=1) {
                                docPriv.setSee(1);
                                docPriv.setDownload(1);
                                docPriv.setOfficePrint(1);
                                docPriv.setOfficeSee(1);
                                docPriv.save();
                            }
                        }
                        else {
                            docPriv.add(code, type, doc.getID());
                        }
                    }
                }

                // 如果原审核状态为草稿，现审核状态为not
                if (examine==Document.EXAMINE_DRAFT) {
                    if (doc.getExamine()==Document.EXAMINE_NOT) {
                        // 如果需流程审批，则自动发起流程，并提交至下一步
                        Leaf lf = new Leaf();
                        lf = lf.getLeaf(dir_code);
                        // 如果目录设置需审核，且文章未审核（如果有审核权限的人发布文章则直接通过）
                        if (lf.isExamine()) {
                            // 判断节点否通过流程审核
                            if (!Leaf.FLOW_TYPE_CODE_NONE.equals(lf.getFlowTypeCode())) {
                                // 自动发起流程
                                Privilege pvg = new Privilege();
                                initExamineFlow(request, pvg.getUser(request), lf, doc);
                            }
                        }
                    }
                }

                if (examine!=Document.EXAMINE_PASS && doc.getExamine()==Document.EXAMINE_PASS) {
                    remind(request, doc);
                }

                PluginMgr pm = new PluginMgr();
                PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                if (pu != null) {
                    IPluginDocumentAction ipda = pu.getDocumentAction();
                    re = ipda.update(application, request, mfu, doc);
                }
            }
            return re;
        } else {
            LeafPriv lp = new LeafPriv(dir_code);
            if (!lp.canUserAppend(privilege.getUser(request))) {
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
            }
            String userName = privilege.getUser(request);
            boolean re = doc.create(application, mfu, userName);
            document = doc;
            id = doc.getID();
            if (re) {
                // 设置权限
                String deptRoleGroup = mfu.getFieldValue("deptRoleGroup");
                if (deptRoleGroup!=null && !"".equals(deptRoleGroup)) {
                    JSONArray arr = JSONArray.parseArray(deptRoleGroup);
                    DocPriv docPriv = new DocPriv();
                    Vector<DocPriv> v = docPriv.listByDocId(doc.getId());
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject json = arr.getJSONObject(i);
                        String code = json.getString("code");
                        String kind = json.getString("kind");
                        int type = StrUtil.toInt(kind, DocPriv.TYPE_DEPT);

                        docPriv.add(code, type, doc.getID());
                    }
                }

                // 如果显示于门户，则发送消息提醒
                if (doc.getExamine()==Document.EXAMINE_PASS) {
                    remind(request, doc);
                }

                PluginMgr pm = new PluginMgr();
                PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                if (pu!=null) {
                    IPluginDocumentAction ipda= pu.getDocumentAction();
                    doc = doc.getDocument(doc.getID());
                    re = ipda.create(application, request, mfu, doc);
                }
                
				Vector documents = new Vector<Document>();
				documents.addElement(doc);  
				launchScriptOnAdd(request, privilege.getUser(request), documents);

				// 如果需流程审批，则自动发起流程，并提交至下一步
                Leaf lf = new Leaf();
                lf = lf.getLeaf(dir_code);
                // 如果目录设置需审核，且文章未审核（如果有审核权限的人发布文章则直接通过）
                if (lf.isExamine() && doc.getExamine()==Document.EXAMINE_NOT) {
                    // 判断节点是否通过流程审核
                    if (!Leaf.FLOW_TYPE_CODE_NONE.equals(lf.getFlowTypeCode())) {
                        // 自动发起流程
                        initExamineFlow(request, userName, lf, doc);
                    }
                }
            }
            return re;
        }
    }

    /**
     * 发起审核流程
     * @param request
     * @param userName
     * @param lf
     * @param doc
     * @throws ErrMsgException
     */
    public void initExamineFlow(HttpServletRequest request, String userName, Leaf lf, Document doc) throws ErrMsgException {
        WorkflowMgr wm = new WorkflowMgr();
        com.redmoon.oa.flow.Leaf lfFlow = new com.redmoon.oa.flow.Leaf();
        lfFlow = lfFlow.getLeaf(lf.getFlowTypeCode());

        long myActionId = wm.initWorkflow(userName, lf.getFlowTypeCode(), "", -1, WorkflowDb.LEVEL_NORMAL);
        MyActionDb mad = new MyActionDb();
        mad = mad.getMyActionDb(myActionId);
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb((int)mad.getFlowId());
        wf.setStatus(WorkflowDb.STATUS_STARTED);
        wf.setBeginDate(new Date());
        wf.save();

        // 置表单的值为相应的文章属性
        FormDb fd = new FormDb(lfFlow.getFormCode());
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(wf.getId(), fd);
        fdao.setFieldValue("title", doc.getTitle());
        fdao.setFieldValue("content", doc.getContent(1));
        fdao.setFieldValue("author", doc.getAuthor());
        fdao.setFieldValue("doc_id", String.valueOf(doc.getID()));
        fdao.setFieldValue("is_comment", doc.isCanComment()?"1":"0");
        fdao.setFieldValue("color", doc.getColor());
        fdao.setFieldValue("is_bold", doc.isBold()?"1":"0");
        fdao.setFieldValue("expire_date", DateUtil.format(doc.getExpireDate(), "yyyy-MM-dd"));
        fdao.setFieldValue("summary", doc.getSummary());
        fdao.save();

        String flowTitle = WorkflowMgr.makeTitle(request, userName, lfFlow, false);
        flowTitle = WorkflowMgr.makeTitleWithField(request, wf, fdao, flowTitle);
        wf.setTitle(flowTitle);
        wf.save();

        // 将附件直接置为表单的附件，并不复制文件
        com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment();
        Vector vAtt = doc.getAttachments(1);
        Iterator irAtt = vAtt.iterator();
        while (irAtt.hasNext()) {
            Attachment attDoc = (Attachment)irAtt.next();
            att.setName(attDoc.getName());
            att.setOrders(attDoc.getOrders());
            att.setSize(attDoc.getSize());
            att.setPageNum(1);
            att.setDiskName(attDoc.getDiskName());
            att.setVisualPath(attDoc.getVisualPath());
            att.setDocId(wf.getDocument().getID());
            att.setCreator(userName);
            att.setCreateDate(new Date());
            att.setFlowId(wf.getId());
            att.create();
        }

        // 自动完成第一个节点
        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb((int)mad.getActionId());
        Vector vto = wa.getLinkToActions();
        // 流程中不能有分支
        Iterator irto = vto.iterator();
        if (irto.hasNext()) {
            WorkflowActionDb nextwa = (WorkflowActionDb)irto.next();
            try {
                WorkflowRouter workflowRouter = new WorkflowRouter();
                Vector vt = workflowRouter.matchActionUser(request, nextwa, wa, false, null);
                StringBuffer sbName = new StringBuffer();
                StringBuffer sbRealName = new StringBuffer();
                Iterator irUser = vt.iterator();
                while (irUser.hasNext()) {
                    UserDb userNext = (UserDb)irUser.next();
                    StrUtil.concat(sbName, ",", userNext.getName());
                    StrUtil.concat(sbRealName, ",", userNext.getRealName());
                    // 置节点上的用户名
                    nextwa.setUserName(sbName.toString());
                    nextwa.setUserRealName(sbRealName.toString());
                    nextwa.save();
                }
            } catch (MatchUserException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        StringBuffer sb = new StringBuffer();
        IWorkflowProService workflowProService = SpringUtil.getBean(IWorkflowProService.class);
        workflowProService.finishActionSingle(request, mad, userName, sb);
        if (sb.length()>0) {
            throw new ErrMsgException(sb.toString());
        }
        examineFlowId = wf.getId();
    }

    /**
     * 发布文章时提醒用户，注意仅当该文章所在的目录显示于门户时，才发送消息提醒
     * @param request
     * @param doc
     * @throws ErrMsgException
     */
    public void remind(HttpServletRequest request, Document doc) throws ErrMsgException {
        Config cfg = Config.getInstance();
        if (!cfg.getBooleanProperty("filearkOnDesktopRemindWhenAdd")) {
            return;
        }
        PortalDb pd = new PortalDb();
        UserDesktopSetupDb udsd = new UserDesktopSetupDb();
        DesktopMgr dm = new DesktopMgr();
        Vector<PortalDb> v = pd.getPortals();
        boolean isToMobile = SMSFactory.isUseSMS();
        String title = cfg.get("filearkOnDesktopRemindTitle");
        String content = cfg.get("filearkOnDesktopRemindContent");
        int wordNum = cfg.getInt("filearkOnDesktopRemindWordNum");

        // 防止重复
        List<String> userList = new ArrayList<>();

        Iterator<PortalDb> ir = v.iterator();
        while(ir.hasNext()) {
            pd = ir.next();
            // 遍历门户中的元素
            String sql = udsd.getSqlByPortalId(pd.getLong("id"));
            Vector vt = udsd.list(sql);
            Iterator itr = vt.iterator();
            while(itr.hasNext()) {
                udsd = (UserDesktopSetupDb)itr.next();
                DesktopUnit du = dm.getDesktopUnit(udsd.getModuleCode());
                if (du == null) {
                    continue;
                }

                // 如果是文件柜目录型
                if (UserDesktopSetupDb.MODULE_FILEARK.equals(udsd.getModuleCode())) {
                    String[] ary = StrUtil.split(udsd.getModuleItem(), ",");
                    if (ary!=null) {
                        for (String item : ary) {
                            if (doc.getDirCode().equals(item)) {
                                // 发送给有浏览权限的人员
                                Vector vDocUser = DocPriv.getUsersCanSee(doc.getId());
                                Iterator irDocUser = vDocUser.iterator();
                                while (irDocUser.hasNext()) {
                                    UserDb user = (UserDb)irDocUser.next();
                                    if (user.getName().equals(UserDb.SYSTEM)) {
                                        continue;
                                    }

                                    if (!userList.contains(user.getName())) {
                                        userList.add(user.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (userList.size() > 0) {
            try {
                boolean mqIsOpen = cfg.getBooleanProperty("mqIsOpen");
                UserDb user = new UserDb();
                String actionType = MessageDb.ACTION_FILEARK_NEW;
                String actionSubType = "";
                String action = String.valueOf(doc.getId());
                String summary = doc.getSummary();
                if ("".equals(summary)) {
                    summary = doc.getAbstract(request, wordNum, true);
                }
                else {
                    summary = StrUtil.getAbstract(request, summary, wordNum, "span");
                }
                UserDb userAuthor = user.getUserDb(doc.getAuthor());
                String t = StrUtil.format(title, new Object[]{doc.getTitle()});
                Leaf leafDoc = new Leaf();
                leafDoc = leafDoc.getLeaf(doc.getDirCode());
                String c = content.replaceFirst("\\$author", userAuthor.isLoaded()?userAuthor.getRealName():doc.getAuthor())
                        .replaceFirst("\\$summary", summary)
                        .replaceFirst("\\$dir", leafDoc.getName());

                if (mqIsOpen) {
                    String[] arr = new String[userList.size()];
                    userList.toArray(arr);
                    IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                    msgProducer.sendSysMsg(arr, t, c, actionType, actionSubType, action);
                } else {
                    MessageDb md = new MessageDb();
                    String[] arr = new String[userList.size()];
                    userList.toArray(arr);
                    md.sendSysMsg(arr, t, c, actionType, actionSubType, action);
                }

                if (isToMobile) {
                    if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                        if (mqIsOpen) {
                            IMsgProducer msgProducer = SpringUtil.getBean(IMsgProducer.class);
                            msgProducer.sendSmsBatch((String[]) userList.toArray(), t, ConstUtil.USER_SYSTEM);
                        } else {
                            IMsgUtil imu = SMSFactory.getMsgUtil();
                            imu.sendBatch((String[]) userList.toArray(), t, ConstUtil.USER_SYSTEM);
                        }
                    }
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }
    
    public void launchScriptOnAdd(HttpServletRequest request, String userName, Vector<Document> documents) {
    	Leaf lfRoot = new Leaf();
    	lfRoot = lfRoot.getLeaf(Leaf.ROOTCODE);
		String script = lfRoot.getScript(Leaf.SCRIPTS_ADD);
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();
				// 赋值用户
				sb.append("userName=\"" + userName + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("request", request);
				bsh.set("documents", documents);

				bsh.eval(script);
			} catch (EvalError e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
    }
    
    public void launchScriptBeforeDel(HttpServletRequest request, String userName, Vector<Document> documents) {
    	Leaf lfRoot = new Leaf();
    	lfRoot = lfRoot.getLeaf(Leaf.ROOTCODE);
		String script = lfRoot.getScript(Leaf.SCRIPTS_DEL);
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();
				// 赋值用户
				sb.append("userName=\"" + userName + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("request", request);
				bsh.set("documents", documents);

				bsh.eval(script);
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error(e);
			}
		}
    }    

    public boolean del(HttpServletRequest request, int id, IPrivilege privilege, boolean isDustbin) throws
            ErrMsgException {
        Document doc = new Document();
        doc = getDocument(id);
        if (doc==null || !doc.isLoaded()) {
            throw new ErrMsgException("文件 " + id + " 不存在！");
        }
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        boolean canDel = lp.canUserDel(privilege.getUser(request));
        if (!canDel) {
            // 判断是否本人删除自己的文章
            if (doc.getAuthor().equals(privilege.getUser(request))) {
                if (doc.getExamine()==Document.EXAMINE_DRAFT) {
                    canDel = true;
                }
                else {
                    Config cfg = Config.getInstance();
                    double filearkUserDelIntervalH = StrUtil.toDouble(cfg.get("filearkUserEditDelInterval"), 0);
                    double intervalMinute = filearkUserDelIntervalH * 60;
                    if (DateUtil.datediffMinute(new Date(), doc.getCreateDate()) < intervalMinute) {
                        canDel = true;
                    } else {
                        throw new ErrMsgException("已超时，发布后" + filearkUserDelIntervalH + "小时内可删除");
                    }
                }
            }
        }
        if (canDel) {
        	boolean re = false;
            if (isDustbin) {
                re = doc.UpdateExamine(Document.EXAMINE_DUSTBIN);
            }
            else {        	
            	Vector<Document> documents = new Vector<Document>();
            	documents.addElement(doc);
            	launchScriptBeforeDel(request,privilege.getUser(request),documents);    
            	
            	re = doc.del();
            }
            if (re) {
	            PluginMgr pm = new PluginMgr();
	            PluginUnit pu = pm.getPluginUnitOfDir(doc.getDirCode());
	            // LogUtil.getLog(getClass()).info("del:" + pu.getCode());
	            if (pu != null) {
	                IPluginDocumentAction ipda = pu.getDocumentAction();
	                re = ipda.del(request, doc, isDustbin);
	            }
	            
	            // 删除日志
	            DocLogDb dld = new DocLogDb();
	            dld.delOfDoc(doc.getId());
	            DocAttachmentLogDb dad = new DocAttachmentLogDb();
	            dad.delOfDoc(doc.getId());
            }
            return re;
        }
        else {
            throw new ErrMsgException(Privilege.MSG_INVALID);
        }
    }

    public boolean increaseHit(HttpServletRequest request, int id,
                               IPrivilege privilege) throws
            ErrMsgException {
        Document doc = getDocument(id);
        boolean re = doc.increaseHit();
        return re;
    }

    public boolean UpdateIsHome(HttpServletRequest request, int id,
                                IPrivilege privilege) throws
            ErrMsgException {

        Document doc = new Document();
        String v = ParamUtil.get(request, "value");
        doc.setID(id);
        boolean re = doc.UpdateIsHome(v.equals("y") ? true : false);
        return re;

    }

    public boolean vote(HttpServletRequest request, int id) throws
            ErrMsgException {
               Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));
        String[] opts = ParamUtil.getParameters(request, "votesel");
        if (opts==null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "err_vote_none"));

        String name = privilege.getUser(request);


        DocPollDb mpd = new DocPollDb();
        mpd = (DocPollDb)mpd.getQObjectDb(new Integer(id));

        Date d = mpd.getDate("expire_date");

        // 检查是否已过期
        if (d!=null) {
            if (DateUtil.compare(d, new java.util.Date()) != 1)
                throw new ErrMsgException(StrUtil.format(SkinUtil.LoadString(request,"res.forum.MsgDb",
                        "err_vote_expire"),
                                          new Object[] {DateUtil.format(d, "yyyy-MM-dd")}));
        }

        int len = opts.length;
        int max_choice = mpd.getInt("max_choice");
        if (len > max_choice) {
            throw new ErrMsgException(StrUtil.format(SkinUtil.LoadString(request,"res.forum.MsgDb",
                    "err_vote_max_count"),
                                          new Object[] {"" + max_choice}));
        }

        // 检查用户是否已投过票
        DocPollOptionDb mpod = new DocPollOptionDb();
        Vector v = mpd.getOptions(id);
        int optLen = v.size();
        for (int i=0; i<optLen; i++) {
            DocPollOptionDb mo = mpod.getDocPollOptionDb(id, i);
            String vote_user = StrUtil.getNullString(mo.getString("vote_user"));
            String[] ary = StrUtil.split(vote_user, ",");
            if (ary!=null) {
                int len2 = ary.length;
                for (int k=0; k<len2; k++) {
                    if (ary[k].equals(name))
                        throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "err_vote_repeat"));
                }
            }
        }

        boolean re = true;
        for (int i=0; i<len; i++) {
            DocPollOptionDb mo = mpod.getDocPollOptionDb(id, StrUtil.toInt(opts[i]));
            mo.set("vote_count", new Integer(mo.getInt("vote_count") + 1));
            String vote_user = StrUtil.getNullString(mo.getString("vote_user"));
            if (vote_user.equals(""))
                vote_user = name;
            else
                vote_user += "," + name;
            mo.set("vote_user", vote_user);
            try {
                re = mo.save();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }

        return re;
    }

    public boolean OperatePage(ServletContext application,
                           HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = doUpload(application, request);
        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

        boolean isValid = false;

        LeafPriv lp = new LeafPriv();
        lp.setDirCode(dir_code);
        if (op.equals("add")) {
            if (lp.canUserAppend(privilege.getUser(request)))
                isValid = true;
        }
        if (op.equals("edit")) {
            if (lp.canUserModify(privilege.getUser(request)))
                isValid = true;
        }

        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);

        String strdoc_id = StrUtil.getNullStr(mfu.getFieldValue("id"));
        int doc_id = Integer.parseInt(strdoc_id);
        Document doc = new Document();
        doc = doc.getDocument(doc_id);

        // LogUtil.getLog(getClass()).info("filepath=" + mfu.getFieldValue("filepath"));

        if (op.equals("add")) {
            String content = StrUtil.getNullStr(mfu.getFieldValue(
                    "htmlcode"));
            return doc.AddContentPage(application, mfu, content);
        }

        if (op.equals("edit")) {
            // return doc.EditContentPage(content, pageNum);
            return doc.EditContentPage(application, mfu);
        }

        return false;
    }

    /**
     * 往文章中插入图片
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return String[] 图片的ID数组
     * @throws ErrMsgException
     */
    public String[] uploadImg(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        // if (!privilege.isUserLogin(request))
        //    throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));

        FileUploadExt fu = new FileUploadExt();
        
        fu.setMaxFileSize(Global.FileSize);

        String[] ext = new String[] {"flv", "jpg", "jpeg", "gif", "png", "bmp", "swf", "mpg", "asf", "wma", "wmv", "avi", "mov", "mp3", "rm", "ra", "rmvb", "mid", "ram"};
        if (ext!=null) {
            fu.setValidExtname(ext);
        }

        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret!=FileUploadExt.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }
            if (fu.getFiles().size()==0) {
                throw new ErrMsgException("请上传文件！");
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }

        Calendar cal = Calendar.getInstance();
		String year = "" + (cal.get(Calendar.YEAR));
		String month = "" + (cal.get(Calendar.MONTH) + 1);
		String virtualpath = year + "/" + month;

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String attPath = cfg.get("file_folder");

        String[] re = null;
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();
        int orders = 0;

        String attachmentBasePath = "/";

        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            // 保存至磁盘相应路径
            IFileService fileService = SpringUtil.getBean(IFileService.class);
            fileService.write(fi, attPath + "/" + virtualpath);

            // 记录于数据库
            com.redmoon.oa.fileark.Attachment att = new Attachment();
            att.setDiskName(fi.getDiskName());
            att.setDocId(Attachment.TEMP_DOC_ID);
            att.setName(fi.getName());
            att.setDiskName(fi.getDiskName());
            att.setOrders(orders);
            att.setVisualPath(attPath + "/" + virtualpath);
            att.setUploadDate(new java.util.Date());
            att.setSize(fi.getSize());
            att.setExt(StrUtil.getFileExt(fi.getName()));
            att.setEmbedded(true);
            String module = ParamUtil.get(request, "module");
            if ("notice".equals(module)) {
            	att.setPageNum(1);
            }
            if (att.create()) {
                re = new String[3];
                re[0] = "" + att.getId();

                re[1] = attachmentBasePath + att.getVisualPath() + "/" + att.getDiskName();
                re[2] = fi.uploadSerialNo;
            }
        }

        return re;
    }
    
    /**
     * 创建Office文件
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean uploadDocument(ServletContext application,
                                  HttpServletRequest request) throws
            ErrMsgException {
        String[] extnames = {"doc", "docx", "xls", "xlsx"};
        FileUpload TheBean = new FileUpload();
        TheBean.setValidExtname(extnames); // 设置可上传的文件类型
        TheBean.setMaxFileSize(Global.FileSize); // 最大35000K
        int ret = 0;
        try {
            ret = TheBean.doUpload(application, request);
            if (ret!=FileUploadExt.RET_SUCCESS) {
                throw new ErrMsgException(TheBean.getErrMessage(request));
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("uploadDocument:" + e.getMessage());
        }
        if (ret == 1) {
            Document doc = new Document();
            return doc.uploadDocument(TheBean);
        } else
            return false;
    }   
    
    /**
     * webedit快速上传，可上传目录
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean uploadByWebedit(ServletContext application, HttpServletRequest request)
	throws ErrMsgException {
    	DocumentFileUploadBean mfu = new DocumentFileUploadBean();
		mfu.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K

		// 20170814 fgf 原来用的是网盘的扩展名配置
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("filearkFileExt").replaceAll("，", ",");
		if (exts.equals("*")) {
			exts = "";
		}
		String[] ext = StrUtil.split(exts, ",");
		if (ext != null) {
            mfu.setValidExtname(ext);
        }

		int ret = 0;
		try {
			ret = mfu.doUpload(application, request);
			if (ret != FileUpload.RET_SUCCESS) {
				if(ret == -4){
					throw new ErrMsgException("操作失败，扩展名非法");
				}else if(ret == -3){
					throw new ErrMsgException("操作失败，单个文件大小超过了预设的最大值");
				}else if(ret == -2){
					throw new ErrMsgException("操作失败，文件总的大小超过了预设的最大值");
				}else{
					throw new ErrMsgException(mfu.getErrMessage());
				}
			}
		} catch (Exception e) {
			throw new ErrMsgException(e.getMessage());
		}

		Vector v = new Vector<Document>();

		if (ret == FileUpload.RET_SUCCESS) {
			String dirCode = ParamUtil.get(request, "dirCode");
			//String dirCode = mfu.getFieldValue("dirCode");
			Privilege privilege = new Privilege();
			
			
			String userName = privilege.getUser(request);

			Leaf lf = new Leaf();
			lf = lf.getLeaf(dirCode);
			
			if (lf==null) {
                throw new ErrMsgException("目录不存在!");
            }

			String visualPath = StrUtil.getNullString(mfu.getFieldValue("filepath"));

			String FilePath = cfg.get("file_folder");
			if (!visualPath.equals("")) {
                FilePath += "/" + visualPath;
            }

			String attSavePath = Global.getRealPath() + FilePath + "/";

			mfu.setSavePath(attSavePath); // 取得目录

			LogUtil.getLog(getClass()).info("attSavePath=" + attSavePath);

			File f = new File(attSavePath);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			// 检查上传的文件大小有没有超出磁盘空间
			Vector attachs = mfu.getFiles();
			if (attachs.size()==0)
				attachs = mfu.getAttachments();

			// 检查是不是按目录上传
			String upDirCode = "";
			String uploadDirName = StrUtil.getNullStr(mfu.getFieldValue("uploadDirName"));
			if (!uploadDirName.equals("")) {
				// 取得uploadDirName中的目录名称，检测是否已存在，如不存在，则创建该目录
				int index = uploadDirName.lastIndexOf("\\");
				String upDirName = uploadDirName.substring(index + 1);
				// 检查是否存在同名目录
				boolean isExist = false;
				Iterator irlf = lf.getChildren().iterator();
				while (irlf.hasNext()) {
					Leaf child = (Leaf) irlf.next();
					if (child.getName().equals(upDirName)) {
						upDirCode = child.getCode();
						isExist = true;
						break;
					}
				}
				// 如果不存在同名目录，则创建
				if (!isExist) {
					// 创建uploadDirName节点及物理目录
					Leaf lfCh = new Leaf();
					lfCh.setName(upDirName);
					upDirCode = FileUpload.getRandName();
					lfCh.setCode(upDirCode);
					lfCh.setParentCode(lf.getCode());
					lfCh.setDescription(lf.getCode());
					lfCh.setType(Leaf.TYPE_LIST);
					//lfCh.setSystem(true);
					boolean flag = lf.AddChild(lfCh);
					if(!flag){
						throw new ErrMsgException("操作失败,文件名太长!");
						//return flag;
					}
					String savePath = Global.getRealPath()
							+ cfg.get("file_folder") + "/"
							+ lfCh.getFilePath() + "/";
					// 检查物理目录是否存在，如果不存在，则创建
					// " cfg.get(\"file_netdisk\")=" +
					// cfg.get("file_netdisk"));
					f = new File(savePath);
					if (!f.isDirectory()) {
						f.mkdirs();
					}
				}
			}

			LogUtil.getLog(getClass()).info(
					"uploadDirName=" + uploadDirName + " att size="
							+ attachs.size() + " file size=" + mfu.getFiles().size());

			Iterator ir = attachs.iterator();
			while (ir.hasNext()) {
				FileInfo fi = (FileInfo) ir.next();

				String myVisualPath = visualPath;
				String savePath = mfu.getSavePath();
				String curDirCode = dirCode;

				LogUtil.getLog(getClass()).info(
						"fi.clientPath=" + fi.clientPath + " uploadDirName="
								+ uploadDirName + " savePath=" + savePath);

				if (!uploadDirName.equals("")) {
					// 为该文件在数据库及磁盘创建相应目录					
					// 检查目录是否包含于客户端路径中
					int p = fi.clientPath.indexOf(uploadDirName);
					if (p != -1) {
						// 在循环中，lf的child_count会变化，缓存会被刷新，因此在这里要重新获取
						lf = lf.getLeaf(upDirCode);
						
						curDirCode = lf.getCode();

						//myVisualPath = lf.getFilePath();
						Calendar cal = Calendar.getInstance();
						String year = "" + (cal.get(Calendar.YEAR));
						String month = "" + (cal.get(Calendar.MONTH) + 1);
						myVisualPath = year + "/" + month;
						
						savePath = Global.getRealPath()
								+ cfg.get("file_folder") + "/"
								+ myVisualPath + "/";

						// 取得upoadDirName后的路径
						String path = fi.clientPath.substring(p
								+ uploadDirName.length() + 1);
						
						// 检查path在树形结构上是否已存在，如果不存在，则创建目录节点，并创建物理目录
						String[] ary = path.split("\\\\");				

						Leaf plf = lf;
						Leaf lfCh = null;
						
						LogUtil.getLog(getClass()).info(
								"path=" + path + " ary.len=" + ary.length + " plf.getName=" + plf.getName());		
						
						// 数组中最后一位是文件名，因此不用处理
						for (int i = 0; i < ary.length - 1; i++) {
							// 检查在孩子节点中是否存在
							boolean isFound = false;
							Iterator irLf = plf.getChildren().iterator();
							while (irLf.hasNext()) {
								Leaf lf2 = (Leaf) irLf.next();
								LogUtil.getLog(getClass()).info(
										"lf2.getName()=" + lf2.getName()
												+ " ary[i]=" + ary[i]);
								if (lf2.getName().equals(ary[i])) {
									isFound = true;
									lfCh = lf2;
									plf = lf2;
									break;
								}
							}
							LogUtil.getLog(getClass()).info(
									"isFound=" + isFound);
							if (!isFound) {
								// 创建节点及物理目录
								lfCh = new Leaf();
								lfCh.setName(ary[i]);
								lfCh.setCode(FileUpload.getRandName());
								lfCh.setParentCode(plf.getCode());
								lfCh.setDescription(ary[i]);
								lfCh.setType(Leaf.TYPE_LIST);
								//lfCh.setSystem(true);
								boolean flag = plf.AddChild(lfCh);
								if(!flag){
									throw new ErrMsgException("操作失败,文件名太长!");
									//return flag;
								}
								lfCh = lfCh.getLeaf(lfCh.getCode());
								plf = lfCh;
							}
							curDirCode = lfCh.getCode();
						}

						if (lfCh != null) {
							//myVisualPath = lfCh.getFilePath();
							cal = Calendar.getInstance();
							year = "" + (cal.get(Calendar.YEAR));
							month = "" + (cal.get(Calendar.MONTH) + 1);
							myVisualPath = year + "/" + month;

							savePath = Global.getRealPath()
									+ cfg.get("file_folder") + "/"
									+ myVisualPath + "/";
							
							LogUtil.getLog(getClass()).info("savePath=" + savePath + " curDirCode=" + curDirCode);							
							// 检查物理目录是否存在，如果不存在，则创建
							f = new File(savePath);
							if (!f.isDirectory()) {
								f.mkdirs();
							}
						}
					}
				}

				if(uploadDirName.equals("")){
					//savePath += "/" + lf.getName()+"/";
					Calendar cal = Calendar.getInstance();
					String year = "" + (cal.get(Calendar.YEAR));
					String month = "" + (cal.get(Calendar.MONTH) + 1);
					String visual_Path = year + "/" + month;
					
					savePath = Global.getRealPath() + cfg.get("file_folder")
							+ "/" + visual_Path + "/";
				}

                IFileService fileService = SpringUtil.getBean(IFileService.class);
                fileService.write(fi, cfg.get("file_folder") + "/"+myVisualPath);

                if(!"".equals(uploadDirName)){
					//保存文档
					Document document = new Document();
					int docId = document.create(fi.getName(),curDirCode,curDirCode,userName);
					v.addElement(document);
					
					Attachment att = new Attachment();
					
					String name = fi.getName();
					String[] nameArr = name.split("\\.");
					
					String toghterName1 = "";
					for(int i=0;i<=nameArr.length-2;i++){
						if(i == (nameArr.length-2)){
							toghterName1 += nameArr[i];
						}else{
							toghterName1 += nameArr[i]+".";
						}
					}
					
					int num = att.findAttachNum(toghterName1, savePath,1,nameArr[nameArr.length-1]);
					//if( num != 0){
					//	name = nameArr[0]+"("+num+")"+"."+nameArr[1];
					//}
					if( num != 0){
						String toghterName = "";
						for(int i=0;i<=nameArr.length-2;i++){
							if(i == (nameArr.length-2)){
								toghterName += nameArr[i];
							}else{
								toghterName += nameArr[i]+".";
							}
						}
						name = toghterName+"("+num+")"+"."+nameArr[nameArr.length-1];
					}
					
					att.setName(name);
					att.setDiskName(fi.getDiskName());
					att.setVisualPath(cfg.get("file_folder") + "/"+myVisualPath);
					att.setSize(fi.getSize());
					att.setExt(fi.getExt());
					att.setDocId(docId);
					// att.setFullPath(savePath+fi.getDiskName());
					att.setPageNum(1);
					att.setOrders(1);
					att.setDownloadCount(0);
					att.setUploadDate(new Date());
					att.setEmbedded(false);
					att.create();
				}
				else {
					//Iterator ir1 = attachs.iterator();
					//while(ir1.hasNext()){
					//	FileInfo fi1 = (FileInfo) ir1.next();
					//保存文档
					Document document = new Document();
					int docId = document.create(fi.getName(),dirCode,dirCode,userName);
					v.addElement(document);

					Attachment att = new Attachment();
					
					String name = fi.getName();
					String[] nameArr = name.split("\\.");
					String toghterName1 = "";
					for(int i=0;i<=nameArr.length-2;i++){
						if(i == (nameArr.length-2)){
							toghterName1 += nameArr[i];
						}else{
							toghterName1 += nameArr[i]+".";
						}
					}
					int num = att.findAttachNum(toghterName1, savePath,1,nameArr[nameArr.length-1]);
					if( num != 0){
						String toghterName = "";
						for(int i=0;i<=nameArr.length-2;i++){
							if(i == (nameArr.length-2)){
								toghterName += nameArr[i];
							}else{
								toghterName += nameArr[i]+".";
							}
						}
						name = toghterName+"("+num+")"+"."+nameArr[nameArr.length-1];
					}
					//if( num != 0){
					//	name = nameArr[0]+"("+num+")"+"."+nameArr[1];
					//}
					lf = new Leaf(dirCode);
					Calendar cal = Calendar.getInstance();
					String year = "" + (cal.get(Calendar.YEAR));
					String month = "" + (cal.get(Calendar.MONTH) + 1);
					myVisualPath = year + "/" + month;
					String visualPath1 = cfg.get("file_folder") + "/"+ myVisualPath ;
					att.setName(name);
					att.setDiskName(fi.getDiskName());
					att.setVisualPath(visualPath1);
					att.setSize(fi.getSize());
					att.setExt(fi.getExt());
					att.setDocId(docId);
					att.setFullPath(savePath+fi.getDiskName());
					att.setPageNum(1);
					att.setOrders(1);
					att.setDownloadCount(0);
					att.setUploadDate(new Date());
					att.setEmbedded(false);
					att.create();
					//}
				}
			}
			launchScriptOnAdd(request, privilege.getUser(request), v);

			return true;
		} else {
            return false;
        }
    }
    
    /**
     * swfupload上传文件
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean uploadBatch(ServletContext application,
    		HttpServletRequest request) throws
    		ErrMsgException {
    	boolean flag = false;
		String contentType = request.getContentType();
		if (contentType.indexOf("multipart/form-data") == -1) {
			throw new IllegalStateException("The content type of request is not multipart/form-data");
		}
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("filearkFileExt").replaceAll("，", ",");
		if (exts.equals("*")) {
			exts = "";
		}
		FileUpload fu = new FileUpload();
		String[] extAry = StrUtil.split(exts, ",");
		if (extAry != null) {
			fu.setValidExtname(extAry);
		}
		fu.setMaxFileSize(Global.FileSize); 
		int ret = -1;
		try {
			ret = fu.doUpload(application, request);
		} catch (IOException e) {
			throw new ErrMsgException(e.getMessage());
		}

		flag = writeFile(request, fu);
		return flag;
    }    
    
	public boolean writeFile(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
		String userName = ParamUtil.get(request, "userName");
		Privilege privilege = new Privilege();
		if (userName == null || "".equals(userName)) {
			userName = privilege.getUser(request);
		}
		boolean flag = true;
		String dirCode = ParamUtil.get(request, "dirCode");

		boolean canExamine = false;
        LeafPriv lp = new LeafPriv(dirCode);
        if (privilege.isUserPrivValid(request, ConstUtil.PRIV_ADMIN) || lp.canUserModify(privilege.getUser(request))) {
            canExamine = true;
        }

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		Calendar cal = Calendar.getInstance();
		String year = "" + (cal.get(Calendar.YEAR));
		String month = "" + (cal.get(Calendar.MONTH) + 1);
		String myVisualPath = year + "/" + month;
		String savePath = Global.getRealPath() + cfg.get("file_folder") + "/" + myVisualPath + "/";

		if (fu.getRet() == FileUpload.RET_SUCCESS) {
			Vector<Document> documents = new Vector<Document>();
			Vector v = fu.getFiles();
			Iterator ir = v.iterator();
            IFileService fileService = SpringUtil.getBean(IFileService.class);

            // swfupload选择多个文件时是逐个上传的，即每次只上传一个文件，故此处可以用 if (ir.hasNext())
			while (ir.hasNext()) {
				FileInfo fi = (FileInfo) ir.next();
				
				//保存文档
				Document document = new Document();
				if (canExamine) {
				    document.setExamine(Document.EXAMINE_PASS);
                }
				int docId = document.create(fi.getName(),dirCode,dirCode, userName);
				documents.addElement(document);
				
				// 使用随机名称写入磁盘
                fileService.write(fi, cfg.get("file_folder")+ "/" + myVisualPath);

				Attachment att = new Attachment();
				
				String name = fi.getName();
				String[] nameArr = name.split("\\.");
				String toghterName1 = "";
				for(int i=0;i<=nameArr.length-2;i++){
					if(i == (nameArr.length-2)){
						toghterName1 += nameArr[i];
					}else{
						toghterName1 += nameArr[i]+".";
					}
				}
				int num = att.findAttachNum(toghterName1, cfg.get("file_folder")+ "/" +myVisualPath,1,nameArr[nameArr.length-1]);
				if( num != 0){
					String toghterName = "";
					for(int i=0;i<=nameArr.length-2;i++){
						if(i == (nameArr.length-2)){
							toghterName += nameArr[i];
						}else{
							toghterName += nameArr[i]+".";
						}
					}
					name = toghterName+"("+num+")"+"."+nameArr[nameArr.length-1];
				}
				
				att.setName(name);
				att.setDiskName(fi.getDiskName());
				att.setVisualPath(cfg.get("file_folder")+ "/" + myVisualPath);
				att.setSize(fi.getSize());
				att.setExt(fi.getExt());
				att.setDocId(docId);
				// att.setFullPath(fu.getSavePath() + fi.getDiskName());
				att.setPageNum(1);
				att.setOrders(1);
				att.setDownloadCount(0);
				att.setUploadDate(new Date());
				att.setEmbedded(false);
				flag = att.create();

				// 生成html预览
                boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
                boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
                String ext = StrUtil.getFileExt(att.getDiskName());
                String previewfile = savePath + att.getDiskName();
                if (canOfficeFilePreview) {
                    if (ext.equals("doc") || ext.equals("docx") || ext.equals("xls") || ext.equals("xlsx")) {
                        Document.createOfficeFilePreviewHTML(previewfile);
                    }
                }
                if (canPdfFilePreview) {
                    if ("pdf".equals(ext)) {
                        Pdf2Html.createPreviewHTML(previewfile);
                    }
                }
			}

			launchScriptOnAdd(request, privilege.getUser(request), documents);
		} else {
			flag = false;
		}

		return flag;
	}
    
    public int getId() {
        return id;
    }
    
    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public String getDirCode() {
        return dirCode;
    }
    
    public Document getDocument() {
    	return document;
    }

    private FileUpload fileUpload;

    private String dirCode;

    private int id;
    
    /**
     * 用以记录创建的文档
     */
    private Document document;

    public int getExamineFlowId() {
        return examineFlowId;
    }
}
