package com.redmoon.forum.plugin.witkey;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

public class WitkeyPrivilege implements IPluginPrivilege {
    static Logger logger = Logger.getLogger(WitkeyPrivilege.class.getName());

    public WitkeyPrivilege() {
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fileUpload) throws ErrMsgException {
        // 检测本版是否含有此功能
        WitkeyUnit cu = new WitkeyUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(WitkeySkin.LoadString(request,
                    "addNewErrorBoardInvalid"));
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
        WitkeyUnit cu = new WitkeyUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException {
        // 检测本版是否含有此功能
        WitkeyUnit cu = new WitkeyUnit();
        if (!cu.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addNewErrorBoardInvalid"));

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
        WitkeyUnit au = new WitkeyUnit();
        if (!au.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();

        boolean re = false;

        // 如果是版主也可以管理
        if (privilege.canManage(request, msgId)) {
            re = true;
        }
        return re;
    }
}
