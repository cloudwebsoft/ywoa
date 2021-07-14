package com.redmoon.forum.message;

import cn.js.fan.base.AbstractForm;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.*;

public class MessageForm extends AbstractForm {
    MessageDb md;
    HttpServletRequest request;

    public MessageForm() {
    }

    public MessageForm(HttpServletRequest request, MessageDb md) {
        this.request = request;
        this.md = md;
    }

    public String chkIp() {
        md.ip = request.getRemoteAddr();
        return md.ip;
    }

    public String chkTitle() throws ErrMsgException {
        String title = ParamUtil.get(request, "title");
        if (title.equals("")) {
            log(SkinUtil.LoadString(request, "res.forum.message.MessageForm",
                                    "err_need_title")); // "标题必须填写！");
        }
        Config cfg = Config.getInstance();
        int maxLen = cfg.getIntProperty("forum.shortMsgTitleLengthMax");
        int minLen = cfg.getIntProperty("forum.shortMsgTitleLengthMin");
        if (title.trim().length() > maxLen || title.trim().length() <minLen) {
            String str = SkinUtil.LoadString(request,
                                             "res.forum.message.MessageForm",
                                             "err_title");
            str = StrUtil.format(str, new Object[] {"" + minLen,"" + maxLen});
            throw new ErrMsgException(str);
        }

        if (!SQLFilter.isValidSqlParam(title)) {
            log(SkinUtil.LoadString(request, SkinUtil.ERR_SQL));
        }
        md.title = title;
        return title;
    }

    public String chkContent() throws ErrMsgException {
        String content = ParamUtil.get(request, "content");
        if (content.equals("")) {
            log(SkinUtil.LoadString(request, "res.forum.message.MessageForm",
                                    "err_need_content")); // "内容必须填写！");
        }
        Config cfg = Config.getInstance();
        int maxLen = cfg.getIntProperty("forum.shortMsgContentLengthMax");
        int minLen = cfg.getIntProperty("forum.shortMsgContentLengthMin");
        if (content.trim().length() > maxLen || content.trim().length() < minLen) {
            String str = SkinUtil.LoadString(request,
                                             "res.forum.message.MessageForm",
                                             "err_content");
            str = StrUtil.format(str, new Object[] {"" + minLen,"" + maxLen});
            throw new ErrMsgException(str);
        }
        if (!SQLFilter.isValidSqlParam(content)) {
            log(SkinUtil.LoadString(request, SkinUtil.ERR_SQL));
        }
        md.content = content;
        return content;
    }

    public String chkReceiver() {
        String[] receivers = ParamUtil.getParameters(request,"receivers");
        String receiver = ParamUtil.get(request, "receiver");
        if(receivers == null){
            if (receiver.equals("")) {
                log(SkinUtil.LoadString(request,
                                        "res.forum.message.MessageForm",
                                        "err_need_receiver"));
            }
        }
        /*
        if (!SQLFilter.isValidSqlParam(receiver)) {
            log(SkinUtil.LoadString(request, SkinUtil.ERR_SQL));
        }
        */
        md.receiver = receiver;
        return receiver;
    }

    public String checkIp() {
        String ip = request.getRemoteAddr();
        md.ip = ip;
        return ip;
    }

    public int chkType() {
        String type = request.getParameter("type");
        if (type == null || type.equals("")) {
            md.type = MessageDb.TYPE_USER;
        } else {
            md.type = Integer.parseInt(type);
        }
        return md.type;
    }

    public boolean checkCreate() throws ErrMsgException {
        init();
        chkTitle();
        chkContent();
        chkReceiver();
        chkIp();
        chkType();
        report();
        return true;
    }
}
