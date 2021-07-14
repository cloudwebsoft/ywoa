package cn.js.fan.module.cms;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import org.apache.log4j.*;

public class CommentMgr {
    Logger logger = Logger.getLogger(CommentMgr.class.getName());

    public CommentMgr() {
    }

    public CommentDb getCommentDb(int id) {
        CommentDb cmt = new CommentDb();
        return cmt.getCommentDb(id);
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        // 检查验证码
        Config cfg = new Config();
        if (cfg.getBooleanProperty("cms.commentValidateCode")) {
           ValidateCodeCreator.validate(request);
        }
        CommentCheck cc = new CommentCheck();
        cc.checkInsert(request);
        CommentDb cmt = new CommentDb();
        cmt.setDocId(cc.getDocId());
        cmt.setNick(cc.getNick());
        cmt.setLink(cc.getLink());
        cmt.setContent(cc.getContent());
        cmt.setIp(cc.getIp());
        return cmt.create(new JdbcTemplate());
    }

    public boolean del(HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CommentCheck cc = new CommentCheck();
        cc.checkDel(request);
        CommentDb cmt = getCommentDb(cc.getId());
        return cmt.del(new JdbcTemplate());
    }

    /**
     * 删除对应于doc_id文章的所有评论
     * @param request HttpServletRequest
     * @param privilege IPrivilege
     * @throws ErrMsgException
     */
    public void delAll(HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CommentCheck cc = new CommentCheck();
        cc.init();
        cc.chkDocId(request);
        cc.report();

        CommentDb cd = new CommentDb();

        String sql = "SELECT id from cms_comment WHERE doc_id=" + cc.getDocId();
        Iterator ir = cd.list(sql).iterator();
        CommentDb cmt = null;
        // 删除并更新缓存
        try {
            while (ir.hasNext()) {
                cmt = (CommentDb) ir.next();
                cmt.del(new JdbcTemplate());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
