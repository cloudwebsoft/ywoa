package com.redmoon.forum.plugin.present;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.plugin.base.IPluginPrivilege;
import com.redmoon.forum.Privilege;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.kit.util.FileUpload;

public class PresentPrivilege implements IPluginPrivilege {
    static Logger logger = Logger.getLogger(PresentPrivilege.class.getName());

    public PresentPrivilege() {
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fu) throws ErrMsgException {
        // 检测本版是否含有此功能
        BoardDb sb = new BoardDb();
        if (!sb.isPluginBoard(PresentUnit.code, boardCode))
            throw new ErrMsgException(PresentSkin.LoadString(request, "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean isOwner(HttpServletRequest request, long msgRootId) {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgRootId);
        Privilege privilege = new Privilege();
        String user = privilege.getUser(request);
        if (user.equals(md.getName()))
            return true;
        else
            return false;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long msgRootId) throws ErrMsgException {
        // 检测本版是否含有此功能
        BoardDb sb = new BoardDb();
        if (!sb.isPluginBoard(PresentUnit.code, boardCode))
            throw new ErrMsgException(PresentSkin.LoadString(request, "addNewErrorBoardInvalid"));

        return true;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException {
        // 检测本版是否含有此功能
        BoardDb sb = new BoardDb();
        if (!sb.isPluginBoard(PresentUnit.code, md.getboardcode()))
            throw new ErrMsgException(PresentSkin.LoadString(request, "addNewErrorBoardInvalid"));

        return true;
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
        return true;
    }

    public boolean canManage(HttpServletRequest request, long msgId) throws ErrMsgException {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);

        // 检测本版是否含有此功能
        BoardDb sb = new BoardDb();
        // System.out.println(getClass() + " boardCode=" + md.getboardcode() + sb.isPluginBoard(PresentUnit.code, md.getboardcode()));

        if (!sb.isPluginBoard(PresentUnit.code, md.getboardcode()))
            throw new ErrMsgException(PresentSkin.LoadString(request, "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();

        boolean re = false;

        // 如果是版主则可以管理
        if (privilege.canManage(request, msgId)) {
            re = true;
        }

        return re;
    }
}
