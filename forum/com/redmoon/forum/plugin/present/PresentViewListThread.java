package com.redmoon.forum.plugin.present;

import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.UIListThread;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.forum.MsgDb;

public class PresentViewListThread implements IPluginViewListThread {
    HttpServletRequest request;

    public PresentViewListThread(HttpServletRequest request, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIListThread.POS_RULE:
            str += StrUtil.toHtml(StrUtil.ubb(request, getBoardRule(), true)) + getHelpLink() + getBoardNote();
            break;
        default:
            break;
        }
        return ""; // str;
    }

    public String getHelpLink() {
        return PresentSkin.LoadString(request, "helpLink");
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String render(int position, MsgDb md) {
        return "";
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = (BoardDb)sb.getBoardDb(PresentUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道
        String note = "";
        return note;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(PresentUnit.code, boardCode);
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode, String op, String timelimit, int threadType) {
        return "";
    }

    private String boardCode;
}
