package com.redmoon.oa.project.forum;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.Privilege;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.forum.MsgMgr;
import cn.js.fan.util.DateUtil;

public class ProjectMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public ProjectMsgAction() {
    }

    /**
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb 所存储的是ReceiveData后得来的信息
     * @return boolean
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

    	long projectId = StrUtil.toLong(fu.getFieldValue("projectId"));
        ProjectDb atd = new ProjectDb();
        atd.setMsgId(md.getId());
        atd.setProjectId(projectId);
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
        ProjectDb rd = new ProjectDb();
        rd = rd.getProjectDb(delId);
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

}
