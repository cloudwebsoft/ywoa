package com.redmoon.forum.plugin.refer;

import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.UIListThread;
import com.redmoon.forum.plugin.dig.DigUnit;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.forum.MsgDb;

public class ReferViewListThread implements IPluginViewListThread {
    HttpServletRequest request;

    public ReferViewListThread(HttpServletRequest request, String boardCode) {
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
        return ReferSkin.LoadString(request, "helpLink");
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String render(int position, MsgDb md) {
        String str = "";
        switch (position) {
        case UIListThread.POS_TOPIC_TITLE:
        	ReferUnit ru = new ReferUnit();
            ReferDb rd = new ReferDb();
            rd = rd.getReferDb(md.getId());
            if (ru.isPluginBoard(md.getboardcode())) {
            	if (rd.isReplied()) {
            		str = "[已回复]";
            	}
            	else {
            		str = "<font color='red'>[未回复]</font>";
            	}
            }
            break;
        default:
            break;
        }
        return str;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = sb.getBoardDb(ReferUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道
        String note = "";
        return note;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(ReferUnit.code, boardCode);
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode, String op, String timelimit, int threadType) {
        return "";
    }

    private String boardCode;
}
