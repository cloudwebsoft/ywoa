package com.redmoon.forum.plugin.remark;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.ThreadTypeDb;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.UIListThread;
import com.redmoon.forum.SQLBuilder;

public class RemarkViewListThread implements IPluginViewListThread {
    HttpServletRequest request;

    public RemarkViewListThread(HttpServletRequest request, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
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

    public String render(int position, MsgDb md) {
        String str = "";
        switch (position) {
        case UIListThread.POS_TOPIC_TITLE:
            break;
        default:
            break;
        }
        return str;
    }

    public String getHelpLink() {
        return RemarkSkin.LoadString(request, "helpLink");
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = sb.getBoardDb(RemarkUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道
        String note = "";
        return note;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(RemarkUnit.code, boardCode);
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode,
                                  String op, String timelimit, int threadType) {
        return "";
    }

    private String boardCode;
}
