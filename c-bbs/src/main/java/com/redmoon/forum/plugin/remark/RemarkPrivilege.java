package com.redmoon.forum.plugin.remark;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

public class RemarkPrivilege implements IPluginPrivilege {
    static Logger logger = Logger.getLogger(RemarkPrivilege.class.getName());

    public RemarkPrivilege() {
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode, FileUpload fileUpload) throws ErrMsgException {
        // 检测本版是否含有此功能
        RemarkUnit cu = new RemarkUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(RemarkSkin.LoadString(request,
                    "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode, long msgRootId) throws ErrMsgException {
        // 检测本版是否含有此功能
        RemarkUnit cu = new RemarkUnit();
        if (!cu.isPluginBoard(boardCode))
            throw new ErrMsgException(RemarkSkin.LoadString(request, "addNewErrorBoardInvalid"));
        return true;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws ErrMsgException {
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
        RemarkUnit au = new RemarkUnit();
        if (!au.isPluginBoard(md.getboardcode()))
            throw new ErrMsgException(RemarkSkin.LoadString(request, "addNewErrorBoardInvalid"));

        Privilege privilege = new Privilege();
        if (privilege.canManage(request, msgId))
           return true;

       // 如果是楼主可以管理
       if (md.isBlog()) {
           long rootId = md.getRootid();
           md = md.getMsgDb(rootId);
           if (Privilege.getUser(request).equalsIgnoreCase(md.getName()))
               return true;
       }
       return false;
    }

    public static boolean canUserDo(HttpServletRequest request, long RemarkId, String action) {
        return true;
    }
}
