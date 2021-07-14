package com.redmoon.forum.plugin.sweet;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import java.util.Calendar;
import com.redmoon.kit.util.FileUpload;
import java.io.File;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.web.Global;

public class SweetMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public static final String ACTION_APPLY = "apply";
    public static final String ACTION_ACCEPT_APPLY = "acceptApply";
    public static final String ACTION_DECLINE_APPLY = "declineApply";

    public static final String ACTION_APPLY_MARRY = "applymarry";
    public static final String ACTION_ACCEPT_MARRY = "acceptApplyMarry";
    public static final String ACTION_DECLINE_MARRY = "acceptDeclineMarry";

    public SweetMsgAction() {
    }

    /**
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb 所存储的是ReceiveData后得来的信息
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean editTopic(ServletContext application,
                                          HttpServletRequest request,
                                          MsgDb md, FileUpload fu) throws
            ErrMsgException {
        // 修改密级
        long editid = Long.parseLong(fu.getFieldValue("editid"));
        String strsecretLevel = StrUtil.getNullStr(fu.getFieldValue("secretLevel"));
        // 未随表单发送secretLevel则返回，比如当编辑根贴的时候，因为根贴公共可见
        if (strsecretLevel.equals(""))
            return true;
        // 修改情人贴子所对应的secretLevel
        int secretLevel = SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC;
        if (StrUtil.isNumeric(strsecretLevel)) {
            secretLevel = Integer.parseInt(strsecretLevel);
        }
        SweetMsgDb sm = new SweetMsgDb();
        sm = sm.getSweetMsgDb(editid);
        if (!sm.isLoaded()) {
            sm.setMsgId(editid);
            sm.setScretLevel(secretLevel);
            sm.setUserAction(sm.USER_ACTION_GENERAL);
            return sm.create();
        }
        else {
            sm.setScretLevel(secretLevel);
            return sm.save();
        }
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        // logger.info("AddNew:msgRootId=" + md.getId());
        SweetDb sd = new SweetDb();
        sd.setmsgRootId(md.getId());
        sd.setState(sd.STATE_PURSUE);
        sd.setName(md.getName());
        return sd.create();
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        // 登记密级
        int secretLevel = SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC;
        try {
            secretLevel = ParamUtil.getInt(request, "secretLevel");
        }
        catch (Exception e) {
            logger.error("AddQuickReply:" + e.getMessage());
            return true;
        }
        SweetMsgDb sm = new SweetMsgDb();
        sm.setMsgId(replyMsgId);
        sm.setScretLevel(secretLevel);
        sm.setUserAction(sm.USER_ACTION_GENERAL);
        return sm.create();
    }

    /**
     * 本方法置于MsgMgr中delTopic真正删除贴子之前，使在删除插件相应内容后，再删除贴子本身
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delTopic(ServletContext application,
                            HttpServletRequest request, MsgDb md) throws ErrMsgException {
        // 如果是删除根贴
        if (md.isRootMsg()) {
            // 删除plugin_sweet中对应的内容，所有plugin_sweet_sq_message中对应于msgRootId的内容
            // 及所有plugin_sweet_user中对应的msgRootId中的内容
            SweetDb sd = new SweetDb();
            sd = sd.getSweetDb(md.getId());
            sd.del();

            // 删除用户照片
            SweetUserInfoDb suid = new SweetUserInfoDb();
            suid = suid.getSweetUserInfoDb(sd.getName());
            String sPhoto = StrUtil.getNullString(suid.getPhoto());
            if (!sPhoto.equals("")) {
                String rootpath = Global.getRealPath();
                File file = new File(rootpath + sPhoto);
                file.delete();
            }
            // 删除用户信息
            suid.del();
        }
        return true;
    }

    /**
     * 此函数置于MsgDb delSingleMsg中真正删除贴子之前，以便于递归删除贴子
     * @param delId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delSingleMsg(long delId) throws
            ResKeyException {
        SweetMsgDb sm = new SweetMsgDb();
        sm = sm.getSweetMsgDb(delId);
        return sm.del();
    }

    public boolean AddReply(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        String sweetAction;
        sweetAction = StrUtil.getNullString(fu.getFieldValue("sweetAction"));
        // logger.info("sweetAction=" + sweetAction);
        if (sweetAction.equals(ACTION_APPLY)) {
            String strsecretLevel = fu.getFieldValue("secretLevel");
            int secretLevel = SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC;
            if (StrUtil.isNumeric(strsecretLevel)) {
                secretLevel = Integer.parseInt(strsecretLevel);
            }
            // 登记该贴为申请贴
           SweetMsgDb sm = new SweetMsgDb();
           sm.setMsgId(md.getId());
           sm.setScretLevel(secretLevel); // sm.SECRET_LEVEL_MSG_OWNER);
           sm.setUserAction(sm.USER_ACTION_APPLY);
           sm.create();

           // 登记申请人
           SweetUserDb su = new SweetUserDb();
           su.setName(md.getName());
           su.setState(su.STATE_NORMAL);
           su.setType(su.TYPE_APPLIER);
           su.setMsgRootId(md.getRootid());
           su.create();
       }
       else if (sweetAction.equals(this.ACTION_ACCEPT_APPLY)) { // 同意申请成为追求者
           SweetUserDb su = new SweetUserDb();
           // 取得被回复的贴子的作者
           MsgDb msgDb = md.getMsgDb(md.getReplyid());
           String name = msgDb.getName();

           // 取得被回复者对应的记录，将其置为追求者
           su = su.getSweetUserDb(md.getRootid(), name);
           su.setType(su.TYPE_PERSUATER);
           su.save();

           // 发送短消息至被回复者，它已被置为追求者
       }
        //else if (sweetAction.equals(this.ACTION_DECLINE_APPLY)) {

        //}
        else if (sweetAction.equals(ACTION_APPLY_MARRY)) { // 登记申请结婚
                // 登记该贴为申请结婚贴
               String strsecretLevel = fu.getFieldValue("secretLevel");
               int secretLevel = SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC;
               if (StrUtil.isNumeric(strsecretLevel)) {
                   secretLevel = Integer.parseInt(strsecretLevel);
               }
               SweetMsgDb sm = new SweetMsgDb();
               sm.setMsgId(md.getId());
               sm.setScretLevel(secretLevel);
               sm.setUserAction(sm.USER_ACTION_APPLY_MARRY);
               sm.create();

               // 如果该人尚未成为追求者（尚未被加入本贴），则登记申请结婚的人
               SweetUserDb su = new SweetUserDb();
               su = su.getSweetUserDb(md.getRootid(), md.getName());
               if (!su.isLoaded()) {
                   su.setName(md.getName());
                   su.setState(su.STATE_NORMAL);
                   su.setType(su.TYPE_APPLIER);
                   su.setMsgRootId(md.getRootid());
                   su.create();
               }
        }
        else if (sweetAction.equals(this.ACTION_ACCEPT_MARRY)) { // 接受求婚
            SweetUserDb su = new SweetUserDb();
            // 取得被回复的贴子的作者
            MsgDb msgDb = md.getMsgDb(md.getReplyid());
            String name = msgDb.getName();

            // 取得被回复者对应的记录，将其置为配偶
            su = su.getSweetUserDb(md.getRootid(), name);
            su.setType(su.TYPE_SPOUSE);
            su.save();

            // 在plugin_sweet_life表中添加其结婚记录
            SweetLifeDb sl = new SweetLifeDb();
            sl.setMsgRootId(msgDb.getRootid());
            sl.setMarryDate(Calendar.getInstance().getTime());
            MsgDb rootmsg = md.getMsgDb(msgDb.getRootid());
            sl.setOwnerName(rootmsg.getName());
            sl.setSpouseName(name);
            sl.create();

            // 发送短消息至被回复者，它已被置为追求者
        }
        //else if (sweetAction.equals(this.ACTION_DECLINE_MARRY)) {

        //}
        else {
            // 登记密级
            String strsecretLevel = fu.getFieldValue("secretLevel");
            int secretLevel = SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC;
            if (StrUtil.isNumeric(strsecretLevel)) {
                secretLevel = Integer.parseInt(strsecretLevel);
            }
            SweetMsgDb sm = new SweetMsgDb();
            sm.setMsgId(md.getId());
            sm.setScretLevel(secretLevel);
            sm.setUserAction(sm.USER_ACTION_GENERAL);
            sm.create();
        }
        return true;
    }
}
