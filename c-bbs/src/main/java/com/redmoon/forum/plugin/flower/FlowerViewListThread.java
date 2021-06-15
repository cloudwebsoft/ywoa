package com.redmoon.forum.plugin.flower;

import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.UIListThread;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.forum.MsgDb;

public class FlowerViewListThread implements IPluginViewListThread {
    HttpServletRequest request;

    public FlowerViewListThread(HttpServletRequest request, String boardCode) {
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
        return FlowerSkin.LoadString(request, "helpLink");
    }

    public String render(int position, MsgDb md) {
        return "";
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = (BoardDb)sb.getBoardDb(FlowerUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道
        String note = "";
        return note;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(FlowerUnit.code, boardCode);
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode, String op, String timelimit, int threadType) {
        return "";
    }

    private String boardCode;
}
