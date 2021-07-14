package com.redmoon.forum.plugin.refer;

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

public class ReferMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public ReferMsgAction() {
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
    	int secretLevel = StrUtil.toInt(fu.getFieldValue("secretLevel"), ReferDb.SECRET_LEVEL_PUBLIC);

        ReferDb atd = new ReferDb();
        atd = atd.getReferDb(md.getId());
        atd.setSecretLevel(secretLevel);
        boolean re = false;
        try {
            re = atd.save();
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {

    	int secretLevel = StrUtil.toInt(fu.getFieldValue("secretLevel"), ReferDb.SECRET_LEVEL_PUBLIC);
        ReferDb atd = new ReferDb();
        atd.setMsgId(md.getId());
        atd.setSecretLevel(secretLevel);
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
    	int secretLevel = ParamUtil.getInt(request, "secretLevel", ReferDb.SECRET_LEVEL_PUBLIC);
        ReferDb rd = new ReferDb();
        rd.setMsgId(replyMsgId);
        rd.setSecretLevel(secretLevel);
        boolean re = false;
        try {
            re = rd.create();
            if (re) {      	
            	MsgDb md = new MsgDb();
            	md = md.getMsgDb(replyMsgId);
            	if (Privilege.isUserHasManagerIdentity(request, md.getboardcode())) {
	            	rd = rd.getReferDb(md.getRootid());
	            	rd.setReplied(true);
	            	rd.save();
            	}
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
        ReferDb rd = new ReferDb();
        rd = rd.getReferDb(delId);
        if (rd.isLoaded())
            return rd.del();
        else
            return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
    	int secretLevel = StrUtil.toInt(fu.getFieldValue("secretLevel"), ReferDb.SECRET_LEVEL_PUBLIC);
        ReferDb rd = new ReferDb();
        rd.setMsgId(md.getId());
        rd.setSecretLevel(secretLevel);
        boolean re = false;
        try {
            re = rd.create();
            if (re) {
            	if (Privilege.isUserHasManagerIdentity(request, md.getboardcode())) {
	            	rd = rd.getReferDb(md.getRootid());
	            	rd.setReplied(true);
	            	rd.save();
            	}
            }            
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }    	
        return re;
    }

}
