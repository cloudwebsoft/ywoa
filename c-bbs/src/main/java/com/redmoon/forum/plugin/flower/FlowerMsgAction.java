package com.redmoon.forum.plugin.flower;

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
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.Privilege;

public class FlowerMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public FlowerMsgAction() {
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
        FlowerDb rd = new FlowerDb();
        rd = rd.getFlowerDb(delId);
        if (rd.isLoaded())
            return rd.del();
        else
            return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }

    public boolean give(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        long msgId = ParamUtil.getLong(request, "msgId");
        MsgMgr mm = new MsgMgr();
        MsgDb md = mm.getMsgDb(msgId);
        if (!md.isLoaded()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.reward","err_isNotExsist"));//该贴已不存在！
        }

        // 检查是否为本人送鸡蛋或鲜花
        if (md.getName().equals(privilege.getUser(request))) {
            throw new ErrMsgException("您不能给自己送鲜花或者鸡蛋！");
        }

        FlowerDb fd = new FlowerDb();
        fd = fd.getFlowerDb(msgId);
        if (!fd.isLoaded()) {
            fd.setMsgId(msgId);
            try {
                fd.create();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }

        int type = ParamUtil.getInt(request, "type");

        boolean re;
        try {
            re = fd.doGive(md, privilege.getUser(request), type);
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }
}
