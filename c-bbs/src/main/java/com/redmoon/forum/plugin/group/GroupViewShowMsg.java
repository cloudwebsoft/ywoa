package com.redmoon.forum.plugin.group;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import com.cloudwebsoft.framework.base.*;
import org.apache.log4j.Logger;

public class GroupViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;
    Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public GroupViewShowMsg(HttpServletRequest request, String boardCode,
                             MsgDb msgDb) {
        this.request = request;
        this.msgDb = msgDb;
        this.boardCode = boardCode;
        if (msgDb.getReplyid() == -1)
            isRoot = true;
    }

    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb,
                                  String userId) {
        return "";
    }

    public String getQuickReplyFormElement() {
        return "";
    }

    public String getQucikReplyNote() {
        String str = "";
        return str;
    }

    public String getNote() {
        UserMgr um = new UserMgr();
        String str = GroupSkin.LoadString(request, "LABEL_MSG_OWNER") +
                     "<a href='../userinfo.jsp?username=" +
                     StrUtil.UrlEncode(msgDb.getName()) + "'>" +
                     um.getUser(msgDb.getName()).getNick() + "</a>";
        return str;
    }

    public String render(int position) {
        String str = "";

        switch (position) {
        case UIShowMsg.POS_NOTE:
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            GroupThreadDb gtd = new GroupThreadDb();
            String sql = gtd.getTable().getSql("groupthreadofmsg");
            sql = sql.replaceAll("\\?", "" + msgDb.getId());
            long count = gtd.getQObjectCount(sql);
            if (count>0) {
                str += GroupSkin.LoadString(request, "belong_to_group");
                // 这里缓存只有当周期到了以后才会刷新，但是因为只有当贴子被删除时才需刷新
                // 而删除的几率较小，所以此处为保证效率忽略了此问题
                QObjectBlockIterator obi = gtd.getQObjects(sql, 0, (int) count);
                GroupDb gd2 = new GroupDb();
                while (obi.hasNext()) {
                    gtd = (GroupThreadDb) obi.next();
                    GroupDb gd = (GroupDb) gd2.getQObjectDb(new Long(gtd.
                            getLong("group_id")));
                    str += "<a target=_blank href='plugin/group/group.jsp?id=" +
                            gd.getLong("id") + "'>" + gd.getString("name") +
                            "</a>&nbsp;&nbsp;";
                }
            }
            break;
        case UIShowMsg.POS_QUICK_REPLY_NOTE:
            break;
        case UIShowMsg.POS_QUICK_REPLY_ELEMENT:
            break;
        default:
        }
        return str;
    }


    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setMsgDb(MsgDb msgDb) {
        this.msgDb = msgDb;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public MsgDb getMsgDb() {
        return msgDb;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(GroupUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return GroupSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
