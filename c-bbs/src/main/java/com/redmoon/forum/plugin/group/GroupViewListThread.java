package com.redmoon.forum.plugin.group;

import java.util.*;
import javax.servlet.http.*;
import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import java.sql.Timestamp;

public class GroupViewListThread implements IPluginViewListThread {
    HttpServletRequest request;

    public GroupViewListThread(HttpServletRequest request, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
    }

    public String render(int position, MsgDb md) {
        return "";
    }

    public String render(int position) {
        String str = "";

        switch (position) {
        case UIListThread.POS_RULE:
            break;
        default:
            break;
        }
        return str; // str;
    }

    public String getHelpLink() {
        return GroupSkin.LoadString(request, "helpLink");
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = sb.getBoardDb(GroupUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道
        String note = "";
        return note;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(GroupUnit.code, boardCode);
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode,
                                  String op, String timelimit, int threadType) {
        return "";
    }

    private String boardCode;
}
