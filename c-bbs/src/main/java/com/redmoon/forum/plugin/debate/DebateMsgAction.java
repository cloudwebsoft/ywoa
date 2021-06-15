package com.redmoon.forum.plugin.debate;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.forum.MsgMgr;
import cn.js.fan.util.DateUtil;

public class DebateMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public DebateMsgAction() {
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
        if (md.isRootMsg()) {
            String strBeginDate = fu.getFieldValue("debateBeginDate");
            java.util.Date beginDate = DateUtil.parse(strBeginDate,
                    "yyyy-MM-dd");
            String strEndDate = fu.getFieldValue("debateEndDate");
            java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
            String viewpoint1 = fu.getFieldValue("viewpoint1");
            String viewpoint2 = fu.getFieldValue("viewpoint2");

            DebateDb atd = new DebateDb();
            atd = atd.getDebateDb(md.getId());
            atd.setBeginDate(beginDate);
            atd.setEndDate(endDate);
            atd.setViewpoint1(viewpoint1);
            atd.setViewpoint2(viewpoint2);
            boolean re = false;
            try {
                re = atd.save();
            } catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
            return re;
        }
        else {
            String strType = StrUtil.getNullString(fu.getFieldValue(
                    "viewpoint_type"));
            int type = DebateViewpointDb.TYPE_SUPPORT;
            if (StrUtil.isNumeric(strType)) {
                type = Integer.parseInt(strType);
            }
            DebateViewpointDb dvd = new DebateViewpointDb();
            dvd = dvd.getDebateViewpointDb(md.getId());
            int oldType = dvd.getType();
            // 已在plugin/debate/edittopic.jsp中将类别置为不可改，以避免在此需要刷新列表的缓存，下面的代码中未作刷新
            if (oldType!=type) {
                dvd.setType(type);
                boolean re = false;
                try {
                    re = dvd.save();
                    if (re) {
                        DebateDb dd = new DebateDb();
                        dd = dd.getDebateDb(md.getRootid());
                        if (type == DebateViewpointDb.TYPE_SUPPORT) {
                            if (oldType==DebateViewpointDb.TYPE_OPPOSE) {
                                dd.setUserCount2(dd.getUserCount2() - 1);
                            }
                            else
                                dd.setUserCount3(dd.getUserCount3() - 1);
                            dd.setUserCount1(dd.getUserCount1() + 1);
                        } else if (type == DebateViewpointDb.TYPE_OPPOSE) {
                            if (oldType==DebateViewpointDb.TYPE_SUPPORT) {
                                dd.setUserCount1(dd.getUserCount1() - 1);
                            }
                            else
                                dd.setUserCount3(dd.getUserCount3() - 1);
                            dd.setUserCount2(dd.getUserCount2() + 1);
                        } else {
                            if (oldType==DebateViewpointDb.TYPE_SUPPORT) {
                                dd.setUserCount1(dd.getUserCount1() - 1);
                            }
                            else
                                dd.setUserCount2(dd.getUserCount2() + 1);
                            dd.setUserCount3(dd.getUserCount3() + 1);
                        }
                        dd.save();
                    }
                } catch (ResKeyException e) {
                    throw new ErrMsgException(e.getMessage(request));
                }
                return re;
            }
        }
        return true;
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        String strBeginDate = fu.getFieldValue("debateBeginDate");
        java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
        String strEndDate = fu.getFieldValue("debateEndDate");
        java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
        String viewpoint1 = fu.getFieldValue("viewpoint1");
        String viewpoint2 = fu.getFieldValue("viewpoint2");
        DebateDb atd = new DebateDb();
        atd.setMsgId(md.getId());
        atd.setBeginDate(beginDate);
        atd.setEndDate(endDate);
        atd.setViewpoint1(viewpoint1);
        atd.setViewpoint2(viewpoint2);
        boolean re = false;
        try {
            re = atd.create();
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        String strType = ParamUtil.get(request, "viewpoint_type");
        // System.out.println(getClass() + " type=" + strType);

        int type = DebateViewpointDb.TYPE_SUPPORT;
        if (StrUtil.isNumeric(strType)) {
            type = Integer.parseInt(strType);
        }
        MsgDb md = new MsgDb();
        md = md.getMsgDb(replyMsgId);
        // System.out.println(getClass() + " type=" + type + " title=" + md.getTitle());
        DebateViewpointDb dvd = new DebateViewpointDb();
        dvd.setType(type);
        dvd.setMsgId(md.getId());
        boolean re = false;
        try {
            re = dvd.create();
            if (re) {
                DebateDb dd = new DebateDb();
                dd = dd.getDebateDb(md.getRootid());
                if (type==DebateViewpointDb.TYPE_SUPPORT) {
                    dd.setUserCount1(dd.getUserCount1() + 1);
                }
                else if (type==DebateViewpointDb.TYPE_OPPOSE) {
                    dd.setUserCount2(dd.getUserCount2() + 1);
                }
                else
                    dd.setUserCount3(dd.getUserCount3() + 1);
                dd.save();
            }
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
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
        boolean re = false;
        if (md.isRootMsg()) {
            DebateDb rd = new DebateDb();
            rd = rd.getDebateDb(md.getId());
            re = rd.del();
        }
        else {
            DebateViewpointDb dvd = new DebateViewpointDb();
            dvd = dvd.getDebateViewpointDb(md.getId());
            re = dvd.del();
        }
        return re;
    }

    /**
     * 此函数置于MsgDb delSingleMsg中真正删除贴子之前，以便于递归删除贴子
     * @param delId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delSingleMsg(long delId) throws
            ResKeyException {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(delId);
        if (md.isRootMsg()) {
            DebateDb rd = new DebateDb();
            rd = rd.getDebateDb(delId);
            if (rd.isLoaded())
                return rd.del();
            else
                return false;
        }
        else {
            DebateViewpointDb dvd = new DebateViewpointDb();
            dvd = dvd.getDebateViewpointDb(delId);
            return dvd.del();
        }
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        String strType = StrUtil.getNullString(fu.getFieldValue("viewpoint_type"));
        int type = DebateViewpointDb.TYPE_SUPPORT;
        if (StrUtil.isNumeric(strType)) {
            type = Integer.parseInt(strType);
        }
        DebateViewpointDb dvd = new DebateViewpointDb();
        dvd.setType(type);
        dvd.setMsgId(md.getId());
        boolean re = false;
        try {
            re = dvd.create();
            if (re) {
                DebateDb dd = new DebateDb();
                dd = dd.getDebateDb(md.getRootid());
                if (type==DebateViewpointDb.TYPE_SUPPORT) {
                    dd.setUserCount1(dd.getUserCount1() + 1);
                }
                else if (type==DebateViewpointDb.TYPE_OPPOSE) {
                    dd.setUserCount2(dd.getUserCount2() + 1);
                }
                else
                    dd.setUserCount3(dd.getUserCount3() + 1);
                dd.save();
            }
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

}
