package com.redmoon.forum.plugin.sweet;

import com.redmoon.forum.plugin.base.IPluginViewAddMsg;
import com.redmoon.forum.plugin.base.UIAddMsg;
import javax.servlet.http.HttpServletRequest;

public class SweetViewAddMsg implements IPluginViewAddMsg {
    String boardCode;
    HttpServletRequest request;

    public SweetViewAddMsg(HttpServletRequest request, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddMsg.POS_TITLE:
            str += SweetSkin.LoadString(request, "addMsgTitle");
            break;
        default:
            break;
        }
        return str;
    }

    public boolean IsPluginBoard() {
        SweetUnit sut = new SweetUnit();
        return sut.isPluginBoard(boardCode);
    }
}
