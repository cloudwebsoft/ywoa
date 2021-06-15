package com.redmoon.forum.plugin.sweet;

import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.UIListThread;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.Privilege;
import java.util.Vector;
import java.util.Iterator;
import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.BoardDb;

public class SweetViewListThread implements IPluginViewListThread {
    HttpServletRequest request;

    public SweetViewListThread(HttpServletRequest request, String boardCode) {
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
            str += getBoardRule() + getHelpLink() + getBoardNote();
            break;
        default:
            break;
        }
        return str;
    }

    public String getHelpLink() {
        return "<table><tr><td><a target=_blank href='plugin/sweet/help.htm'>帮助</a></td></tr></table>";
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = sb.getBoardDb(SweetUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道，提示贴子的id,加入的人员
        // 提示本人处于哪些贴子中，目前的状态
        // 您发起的专贴为[$id],该贴状态为[$state],已婚者[]:$spouse,追求者[$usercount人]:
        String quickGate = SweetSkin.LoadString(request, "quickGateOwner");
        String user = Privilege.getUser(request);
        SweetDb sd = new SweetDb();
        sd = sd.getSweetDb(user);
        // 如果本人已发起过情人贴
        if (sd.isLoaded()) {
            quickGate = quickGate.replaceFirst("\\$id", "<a href='" + Global.getRootPath() + "/forum/showtopic.jsp?rootid=" + sd.getMsgRootId() + "'>" + sd.getMsgRootId() + "</a>");
            quickGate = quickGate.replaceFirst("\\$state", sd.getStateDesc(request));
            if (sd.getState()==sd.STATE_MARRY){
                String spousestr = SweetSkin.LoadString(request, "spouse");
                spousestr.replaceFirst("\\$spouse", sd.getSpouse());
                quickGate += "&nbsp;" + spousestr;
            }
            // 取得所有的追求者
            SweetUserDb su = new SweetUserDb();
            Vector v = su.getAllPersuater(sd.getMsgRootId());
            Iterator ir = v.iterator();
            String puser = "";
            while (ir.hasNext()) {
                SweetUserDb sud = (SweetUserDb)ir.next();
                puser += "&nbsp;<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(sud.getName()) + "'>" + sud.getName() + "</a>";
            }
            puser = SweetSkin.LoadString(request, "persuaterCount").replaceFirst("\\$usercount", "" + v.size()) + puser;
            quickGate += "，" + puser + "<br>";
        }
        else {
            // 取得本人所参与的情人贴
            SweetUserDb su = new SweetUserDb();
            Vector v = su.getUserAttend(user);
            Iterator ir = v.iterator();
            MsgDb md = new MsgDb();
            quickGate = "";
            while (ir.hasNext()) {
                SweetUserDb sud = (SweetUserDb)ir.next();
                long msgRootId = sud.getMsgRootId();
                md = md.getMsgDb(msgRootId);
                quickGate += SweetSkin.LoadString(request, "quickGatePersuater") + "<a href='showtopic.jsp?rootid=" + msgRootId + "'>" + md.getTitle() + "</a><br>";
            }
        }
        return quickGate;
    }

    public boolean IsPluginBoard() {
        SweetUnit sut = new SweetUnit();
        return sut.isPluginBoard(boardCode);
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode, String op, String timelimit, int threadType) {
        return "";
    }

    private String boardCode;
}
