package com.redmoon.oa.fileark;

import java.util.Vector;
import org.apache.log4j.Logger;
import java.util.Iterator;
import cn.js.fan.web.Global;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultRecord;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import java.sql.ResultSet;
import cn.js.fan.db.Conn;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.base.IPrivilege;

public class CommentMgr {
    String connname = "";
    String cachePrix = "cmt";
    String cachePrixList = "cmt_list";
    RMCache rmCache;
    Logger logger = Logger.getLogger(CommentMgr.class.getName());

    public CommentMgr() {
        rmCache = RMCache.getInstance();
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("CommentMgr:默认数据库名为空！");
    }

    /**
     * 取得doc_id的评论
     * @param doc_id int
     * @return Iterator
     */
    public Iterator getList(int doc_id) {
        Vector list  = (Vector) rmCache.get(this.
                cachePrixList + doc_id);

        Iterator listids = null;
        if (list!=null)
            listids = list.iterator();
        if (listids == null) {
            String sql = "select id from cms_comment where doc_id=" + doc_id +
                         " order by add_date desc";
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                rs = conn.executeQuery(sql);
                if (rs!=null) {
                    Vector v = new Vector();
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        v.addElement("" + id);
                    }
                    rmCache.put(cachePrixList + doc_id, v);
                    listids = v.iterator();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            finally {
                if (conn!=null) {
                    conn.close(); conn = null;
                }
            }
        }

        Vector v = new Vector();
        if (listids!=null) {
	        while (listids.hasNext()) {
	            String id = (String) listids.next();
	            int intid = Integer.parseInt(id);
	            v.addElement(getComment(intid));
	        }
        }
        return v.iterator();
    }

    public Comment getComment(int id) {
        Comment cmt = (Comment) rmCache.get(cachePrix + id);
        if (cmt == null) {
            cmt = new Comment(id);
            try {
                rmCache.put(cachePrix + id, cmt);
            } catch (Exception e) {
                logger.error("getComment:" + e.getMessage());
            }
            return cmt;
        } else {
            cmt.renew();
            return cmt;
        }
    }

    public boolean insert(HttpServletRequest request) throws ErrMsgException {
        CommentCheck cc = new CommentCheck();
        cc.checkInsert(request);
        Comment cmt = new Comment();
        return create(cc.getDocId(), cc.getNick(), 
                cc.getContent(), cc.getLink(), cc.getIp());
    }
    
    public boolean create(int docId, String userName, String content, String link, String ip) {
        Comment cmt = new Comment();
        boolean re = cmt.insert(docId, userName, link,
                               content, ip);
        if (re) {
            // 更新缓存
            try {
                rmCache.remove(this.cachePrixList + docId);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return re;
    }

    public boolean del(HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CommentCheck cc = new CommentCheck();
        cc.checkDel(request);

        return del(request, privilege, cc.getId());
    }

    public boolean del(HttpServletRequest request, IPrivilege privilege, int id) throws
            ErrMsgException {
        Comment cmt = getComment(id);
        boolean re = cmt.del(id);
        if (re) {
            // 更新缓存
            try {
                rmCache.remove(cachePrixList + cmt.getDocId());
                rmCache.remove(cachePrix + id);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return re;
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

        Iterator ir = getList(cc.getDocId());
        Comment cmt = null;
        // 删除并更新缓存
        try {
            while (ir.hasNext()) {
                cmt = (Comment) ir.next();
                cmt.del(cmt.getId());
                rmCache.remove(this.cachePrix + cc.getId());
            }
            rmCache.remove(this.cachePrixList + cc.getDocId());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
