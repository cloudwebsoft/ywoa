package com.redmoon.forum.plugin.sweet;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.plugin.base.IPluginPrivilege;
import com.redmoon.forum.Privilege;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import com.redmoon.kit.util.FileUpload;

public class SweetPrivilege implements IPluginPrivilege {
    static Logger logger = Logger.getLogger(SweetPrivilege.class.getName());

    public SweetPrivilege() {
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fu) throws ErrMsgException {
        // 检测本版是否含有此功能
        SweetUnit su = new SweetUnit();
        if(!su.isPluginBoard(boardCode))
            throw new ErrMsgException(SweetSkin.LoadString(request, "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();
        String user = privilege.getUser(request);

        SweetDb sd = new SweetDb();
        int id = sd.getMsgRootIdOfUser(user);
        // logger.info("user=" + user + " id=" + id);
        if (id==-1)
            return true;
        else {
            throw new ErrMsgException(SweetSkin.LoadString(request, "addNewError"));
        }
    }

    public boolean isOwner(HttpServletRequest request, long msgRootId) throws ErrMsgException {
        String user = Privilege.getUser(request);
        SweetDb sd = new SweetDb();
        sd = sd.getSweetDb(msgRootId);
        return sd.getName().equals(user);
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long msgRootId) throws ErrMsgException {
        // 检测本版是否含有此功能
        SweetUnit sut = new SweetUnit();
        if(!sut.isPluginBoard(boardCode))
            throw new ErrMsgException(SweetSkin.LoadString(request, "addNewErrorBoardInvalid"));

        // 检查是否为被屏蔽的用户
        String username = Privilege.getUser(request);
        SweetUserDb su = new SweetUserDb();
        su = su.getSweetUserDb(msgRootId, username);
        if (su.getState()==su.STATE_SHIELD) {
            throw new ErrMsgException(SweetSkin.LoadString(request, "ERROR_SHIELED"));
        }
        return true;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException {
        // 检测本版是否含有此功能
        SweetUnit sut = new SweetUnit();
        if(!sut.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(SweetSkin.LoadString(request, "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();
        String user = privilege.getUser(request);

        // 如果贴子作者是本人则可以编辑
        if (user.equals(md.getName()))
            return true;

        // 检查是否为被屏蔽的用户
        String username = Privilege.getUser(request);
        SweetUserDb su = new SweetUserDb();
        su = su.getSweetUserDb(md.getRootid(), username);
        if (su.getState()==su.STATE_SHIELD) {
            throw new ErrMsgException(SweetSkin.LoadString(request, "ERROR_SHIELED"));
        }
        return false;
    }

    /**
     *
     * @param request HttpServletRequest
     * @param md MsgDb replyid所对应的贴子，也即根贴
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canAddQuickReply(HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        // 检测本版是否含有此功能
        SweetUnit sut = new SweetUnit();
        if(!sut.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(SweetSkin.LoadString(request,
                    "addNewErrorBoardInvalid"));
        String username = Privilege.getUser(request);
        // 如果是楼主本人在浏览
        if (username.equals(md.getName()))
            return true;

        SweetUserDb su = new SweetUserDb();
        su = su.getSweetUserDb(md.getRootid(), username);
        if (su.isLoaded()) {
            // 如果是申请状态，则不能快速回复
            if (su.getType()==su.TYPE_APPLIER)
                return false;
        }
        else // 如果尚未加入，则不能快速回复
            return false;
        // 检查是否为被屏蔽的用户
        if (su.getState()==su.STATE_SHIELD) {
            throw new ErrMsgException(SweetSkin.LoadString(request, "ERROR_SHIELED"));
        }
        return true;
    }

    public boolean canManage(HttpServletRequest request, long msgId) throws ErrMsgException {
        Privilege privilege = new Privilege();
        // 如果是总版主，则允许删
        if (privilege.isMasterLogin(request))
            return true;
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);

        String boardCode = md.getboardcode();
        // 检测本版是否含有此功能
        SweetUnit sut = new SweetUnit();
        if(!sut.isPluginBoard(boardCode))
            throw new ErrMsgException(SweetSkin.LoadString(request, "addNewErrorBoardInvalid"));

        String user = privilege.getUser(request);

        // 如果是楼主可以管理
        long rootId = md.getRootid();
        md = md.getMsgDb(rootId);
        if (user.equals(md.getName()))
            return true;

        return false;
    }
}
