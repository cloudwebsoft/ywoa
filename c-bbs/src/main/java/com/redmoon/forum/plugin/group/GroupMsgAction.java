package com.redmoon.forum.plugin.group;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.forum.plugin.activity.ActivityUnit;

public class GroupMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public GroupMsgAction() {
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
        return true;
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        String[] ids = fu.getFieldValues("group_id");
        GroupThreadDb gt = new GroupThreadDb();
        GroupUserDb gu = new GroupUserDb();
        String userName = Privilege.getUser(request);

        if (md.getboardcode().equals(GroupUnit.code)) {
            if (ids==null)
                throw new ErrMsgException("请选择圈子!");
        }

        if (ids!=null) {
            GroupDb gd = new GroupDb();
            int len = ids.length;
            for (int i=0; i<len; i++) {
                try {
                    // System.out.println(getClass() + " ids[i]=" + ids[i] + "end");
                    long id = SequenceMgr.nextID(SequenceMgr.PLUGIN_GROUP_THREAD);
                    gt.create(new JdbcTemplate(), new Object[] {new Long(id), new Long(md.getId()), new Long(ids[i]), new java.util.Date(), new java.util.Date(), userName});

                    gu = gu.getGroupUserDb(StrUtil.toLong(ids[i]), userName);
                    if (gu==null) {
                        gu = new GroupUserDb();
                    }
                    else {
                        gu.set("msg_count",
                               new Integer(gu.getInt("msg_count") + 1));
                        gu.set("total_count",
                               new Integer(gu.getInt("total_count") + 1));
                        gu.save();
                    }

                    gd = (GroupDb)gd.getQObjectDb(new Long(ids[i]));
                    gd.set("msg_count", new Integer(gd.getInt("msg_count") + 1));
                    gd.set("total_count", new Integer(gd.getInt("total_count") + 1));
                    gd.save();

                    if (md.getPluginCode().equals(ActivityUnit.code)) {
                        long activityId = SequenceMgr.nextID(SequenceMgr.PLUGIN_GROUP_ACTIVITY);

                        GroupActivityDb ga = new GroupActivityDb();
                        ga.create(new JdbcTemplate(), new Object[] {new Long(activityId), new Long(md.getId()), new Long(ids[i]), userName});
                    }
                }
                catch (ResKeyException e) {
                    throw new ErrMsgException(e.getMessage(request));
                }
            }
        }
        return true;
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        return true;
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
                            HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
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
        // 删除活动
        MsgMgr mm = new MsgMgr();
        MsgDb md = mm.getMsgDb(delId);
        if (md.getPluginCode().equals(ActivityUnit.code)) {
            GroupActivityDb ga = new GroupActivityDb();
            ga.onDelMsg(delId);
        }

        GroupThreadDb gtd = new GroupThreadDb();
        boolean re = gtd.onDelMsg(delId);

        return re;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }
}
