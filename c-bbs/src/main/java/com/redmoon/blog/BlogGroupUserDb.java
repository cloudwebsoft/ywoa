package com.redmoon.blog;

import com.cloudwebsoft.framework.base.*;
import cn.js.fan.db.PrimaryKey;
import java.util.Vector;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: �ŶӲ��ͳ�Ա����</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BlogGroupUserDb extends QObjectDb {
    public static int CHECK_STATUS_NOT = 0;
    public static int CHECK_STATUS_PASSED = 1;

    public static String PRIV_TOPIC = "priv_topic";

    public BlogGroupUserDb() {
    }

    public BlogGroupUserDb getBlogGroupUserDb(long blogId, String userName) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("blog_id", new Long(blogId));
        pk.setKeyValue("user_name", userName);
        return (BlogGroupUserDb)getQObjectDb(pk.getKeys());
    }

    public void delUserOfBlog(long blogId) throws ResKeyException {
        String sql = "select blog_id, user_name from " + table.getName() + " where blog_id=" + blogId;
        Iterator ir = list(sql).iterator();
        while (ir.hasNext()) {
            BlogGroupUserDb bgu = (BlogGroupUserDb)ir.next();
            bgu.del();
        }
    }

    /**
     * �ж��û��Ƿ�ӵ���ŶӲ����е����Ȩ��
     * @param request HttpServletRequest
     * @param blogId long
     * @param priv String
     * @return boolean
     * @throws ErrMsgException
     */
    public static boolean canUserDo(HttpServletRequest request, long blogId, String priv) throws ErrMsgException {
        String userName = Privilege.getUser(request);
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        // �ŶӲ��ʹ�����
        if (ucd.getUserName().equals(userName))
            return true;
        BlogGroupUserDb bgu = new BlogGroupUserDb();
        bgu = bgu.getBlogGroupUserDb(blogId, userName);
        if (bgu==null)
            return false;
        // �����ŶӲ��͹���ԱȨ��
        if (bgu.getString(Privilege.PRIV_ALL).equals("1")) {
            return true;
        }
        if (priv.equals(Privilege.PRIV_ENTER)) {
            if (bgu==null || !bgu.isLoaded()) {
                return false;
            }
            else {
                if (bgu.getString("check_status").equals("1"))
                    return true;
                else
                    throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.BlogGroupUserDb", "err_not_checked"));
            }
        }
        return bgu.getString(priv).equals("1");
    }

    public Iterator getBlogGroupUserAttend(String userName) {
        String sql = "select blog_id, user_name from " + table.getName() + " where user_name=" + StrUtil.sqlstr(userName);
        long count = getQObjectCount(sql);
        return getQObjects(sql, 0, (int)count);
    }

}
