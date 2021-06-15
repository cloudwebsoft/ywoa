package com.redmoon.forum.plugin.dig;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

public class DigPrivilege implements IPluginPrivilege {
    static Logger logger = Logger.getLogger(DigPrivilege.class.getName());

    public DigPrivilege() {
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fileUpload) throws ErrMsgException {
        // 检测本版是否含有此功能
        DigUnit cu = new DigUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(DigSkin.LoadString(request,
                    "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long msgRootId) throws ErrMsgException {
        // 检测本版是否含有此功能
        DigUnit cu = new DigUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(DigSkin.LoadString(request, "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException {
        // 检测本版是否含有此功能
        DigUnit cu = new DigUnit();
        if (!cu.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(DigSkin.LoadString(request, "addNewErrorBoardInvalid"));

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
        DigUnit au = new DigUnit();
        if (!au.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(DigSkin.LoadString(request, "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();

        boolean re = false;

        // 如果是版主也可以管理
        if (privilege.canManage(request, msgId)) {
            re = true;
        }
        return re;
    }

    public static boolean canUserDo(HttpServletRequest request, long DigId, String action) {
        return true;
    }
}
