package com.redmoon.blog;

import com.redmoon.forum.MsgDb;
import java.util.Vector;

/**
 * <p>Title: </p>
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
public class UserBlog {
    public UserBlog(long blogId) {
        this.blogId = blogId;
    }

    /**
     *
     * @param userName
     * @param year int
     * @param month int
     * @return int[] 月份的第一天的日志数为int[1]
     */
    public int[] getBlogDayCount(int year, int month) {
        MsgDb md = new MsgDb();
        return md.getBlogMsgDayCount(blogId, year, month);
    }

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public long getBlogId() {
        return blogId;
    }

    public Vector getBlogDayList(int year, int month, int day) {
        MsgDb md = new MsgDb();
        return md.getBlogDayList(blogId, year, month, day);
    }

    private long blogId;
}
