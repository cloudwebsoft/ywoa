package cn.js.fan.module.cms;

import cn.js.fan.base.*;
import cn.js.fan.security.SecurityUtil;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ErrMsgException;
import org.apache.log4j.Logger;
import cn.js.fan.web.SkinUtil;

public class CommentCheck extends AbstractCheck {
    int id;
    String nick;
    String link;
    String content;
    String ip;
    int doc_id;

    Logger logger = Logger.getLogger(CommentCheck.class.getName());

    public CommentCheck() {
    }

    public String getNick() {
        return nick;
    }

    public String getLink() {
        return link;
    }

    public int getDocId() {
        return this.doc_id;
    }

    public String getContent() {
        return this.content;
    }

    public String getIp() {
        return this.ip;
    }

    public int getId() {
        return id;
    }

    public String chkNick(HttpServletRequest request) {
        nick = ParamUtil.get(request, "nick");
        if (nick.equals("")) {
            log(SkinUtil.LoadString(request, "res.cms.CommentCheck", "need_nick")); // "名称必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(nick))
            log(SkinUtil.LoadString(request, "res.cms.CommentCheck", "err_invalid_sql")); // "请勿使用' ; 等字符！");
        return nick;
    }

    public String chkLink(HttpServletRequest request) {
        link = ParamUtil.get(request, "link");
        if (link.equals("")) {
            // log(SkinUtil.LoadString(request, "res.cms.CommentCheck", "need_link")); // "链接必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(link))
            log(SkinUtil.LoadString(request, "res.cms.CommentCheck", "err_invalid_sql")); // "请勿使用' ; 等字符！");
        return link;
    }

    public String chkContent(HttpServletRequest request) {
        content = ParamUtil.get(request, "content");
        if (content.equals("")) {
            log(SkinUtil.LoadString(request, "res.cms.CommentCheck", "need_content")); // 内容必须填写！");
        }
        return content;
    }

    public int chkId(HttpServletRequest request) throws ErrMsgException {
        id = ParamUtil.getInt(request, "id");
        return id;
    }

    public String chkIp(HttpServletRequest request) {
        ip = request.getRemoteAddr();
        return ip;
    }

    public int chkDocId(HttpServletRequest request) throws ErrMsgException {
        try {
            doc_id = ParamUtil.getInt(request, "doc_id");
        }
        catch (ErrMsgException e) {
            logger.error(e.getMessage());
            throw e;
        }
        return doc_id;
    }

    public boolean checkInsert(HttpServletRequest request) throws ErrMsgException {
        init();
        chkDocId(request);
        chkNick(request);
        chkLink(request);
        chkContent(request);
        chkIp(request);
        report();
        return true;
    }

    public boolean checkId(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }
}
