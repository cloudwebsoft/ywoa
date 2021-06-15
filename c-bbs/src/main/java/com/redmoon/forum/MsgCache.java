package com.redmoon.forum;

import cn.js.fan.cache.jcs.RMCache;
import org.apache.log4j.Logger;
import cn.js.fan.db.Conn;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.web.Global;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.security.SecurityUtil;

/**
 * <p>Title: 有关MsgDb中用到的缓存都在此类中管理</p>
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
public class MsgCache {
    String connname = "forum";
    public static final int MSG_BLOCK_SIZE = 100;
    public static final int THREAD_BLOCK_SIZE = 200;
    private static final long[] EMPTY_BLOCK = new long[0];

    RMCache rmCache = RMCache.getInstance();
    String COUNT_GROUP_NAME = "SQL_COUNT_";
    String MSGBLOCKCACHEPRIX = "MSGBLOCK_";
    String THREADBLOCKCACHEPRIX = "THREADBLOCK_";
    String cachePrix = "sq_msg_";

    public MsgCache() {
        init();
    }

    public void init() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            Logger.getLogger(getClass()).info("MsgCache:conname is empty.");
    }

    public MsgDb getMsgDb(long id) {
        MsgDb msg = (MsgDb) rmCache.get(cachePrix + id);
        // logger.info("getMsgDb msg=" + msg + " id=" + id);
        if (msg == null) {
            msg = new MsgDb(id);
            // logger.info("getMsgDb: loaded=" + msg.isLoaded());
            try {
                if (msg.isLoaded())
                    rmCache.put(cachePrix + id, msg);
            } catch (Exception e) {
                Logger.getLogger(getClass()).error("getMsgDb:" + e.getMessage());
            }
        }
        else {
            // renew;

        }
        return msg;
    }

    /**
     * 取得thread的block(预读取的块)
     * @param query String
     * @param groupname String
     * @param startIndex long
     * @return long[]
     */
    protected long[] getThreadsBlock(String query, String groupname, long startIndex) {
            // First, discover what block number the results will be in.
            long blockID = startIndex / THREAD_BLOCK_SIZE;
            long blockStart = blockID * THREAD_BLOCK_SIZE;
            // Now, check cache to see if the block is already cached. The key is
            // simply the query plus the blockID.
            String key = query + blockID;

            long[] longArray = null;
            try {
                longArray = (long[]) rmCache.getFromGroup(key,
                        THREADBLOCKCACHEPRIX + groupname);
            } catch (Exception e) {
                Logger.getLogger(getClass()).error(e.getMessage());
            }
            //If already in cache, return the block.
            if (longArray != null) {
                /**
                 * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
                 * the case, it means two things:
                 *  1) We're at the end boundary of all the results.
                 *  2) If the start index is greater than the length of the current
                 *     block, than there aren't really any results to return.
                 */
                long[] docs = longArray;
                //当startIndex过大时
                if (startIndex >= blockStart + docs.length) {
                    // Return an empty array
                    return EMPTY_BLOCK;
                } else {
                    return docs;
                }
            }
            // Otherwise, we have to load up the block from the database.
            else {
                Vector DocBlock = new Vector();
                Conn conn = new Conn(connname);
                ResultSet rs = null;
                try {
                    // Set the maxium number of rows to end at the end of this block.
                    conn.setMaxRows((int)(THREAD_BLOCK_SIZE * (blockID + 1)));
                    rs = conn.executeQuery(query);
                    // Grab THREAD_BLOCK_ROWS rows at a time.
                    conn.setFetchSize(THREAD_BLOCK_SIZE);
                    // Many JDBC drivers don't implement scrollable cursors the real
                    // way, but instead load all results into memory. Looping through
                    // the results ourselves is more efficient.
                    for (int i = 0; i < blockStart; i++) {
                        rs.next();
                    }
                    // Keep reading results until the result set is exaughsted or
                    // we come to the end of the block.
                    int count = 0;
                    while (rs.next() && count < THREAD_BLOCK_SIZE) {
                        DocBlock.addElement(new Long(rs.getLong(1)));
                        count++;
                    }
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (Exception e) {}
                        rs = null;
                    }
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }
                }
                int len = DocBlock.size();
                long[] docs = new long[len];
                for (int i = 0; i < len; i++) {
                    docs[i] = ((Long) DocBlock.elementAt(i)).longValue();
                }
                // Add the thread block to cache
                try {
                    rmCache.putInGroup(key, THREADBLOCKCACHEPRIX + groupname, docs);
                } catch (Exception e) {
                    Logger.getLogger(getClass()).error(e.getMessage());
                }
                /**
                 * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
                 * the case, it means two things:
                 *  1) We're at the end boundary of all the results.
                 *  2) If the start index is greater than the length of the current
                 *     block, than there aren't really any results to return.
                 */
                if (startIndex >= blockStart + docs.length) {
                    // Return an empty array
                    return EMPTY_BLOCK;
                } else {
                    return docs;
                }
        }
    }

    /**
     * 取得主题贴及其回复的block(预读取块)
     * @param query String
     * @param groupname String
     * @param startIndex long
     * @return long[]
     */
    protected long[] getMsgBlock(String query, String groupname, long startIndex) {
        // First, discover what block number the results will be in.
        long blockID = startIndex / MSG_BLOCK_SIZE;
        long blockStart = blockID * MSG_BLOCK_SIZE;
        // Now, check cache to see if the block is already cached. The key is
        // simply the query plus the blockID.
        String key = query + blockID;

        long[] longArray = null;
        try {
            longArray = (long[]) rmCache.getFromGroup(key,
                    MSGBLOCKCACHEPRIX + groupname);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error("getMsgBlock:" + e.getMessage());
        }
        //If already in cache, return the block.
        if (longArray != null) {
            /**
             * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
             * the case, it means two things:
             *  1) We're at the end boundary of all the results.
             *  2) If the start index is greater than the length of the current
             *     block, than there aren't really any results to return.
             */
            long[] docs = longArray;
            //当startIndex过大时
            if (startIndex >= blockStart + docs.length) {
                // Return an empty array
                return EMPTY_BLOCK;
            } else {
                return docs;
            }
        }
        // Otherwise, we have to load up the block from the database.
        else {
            Vector DocBlock = new Vector();
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                // Set the maxium number of rows to end at the end of this block.
                conn.setMaxRows((int)(MSG_BLOCK_SIZE * (blockID + 1)));
                rs = conn.executeQuery(query);
                // Grab THREAD_BLOCK_ROWS rows at a time.
                conn.setFetchSize(MSG_BLOCK_SIZE);
                // Many JDBC drivers don't implement scrollable cursors the real
                // way, but instead load all results into memory. Looping through
                // the results ourselves is more efficient.
                for (int i = 0; i < blockStart; i++) {
                    rs.next();
                }
                // Keep reading results until the result set is exaughsted or
                // we come to the end of the block.
                int count = 0;
                while (rs.next() && count < MSG_BLOCK_SIZE) {
                    DocBlock.addElement(new Long(rs.getLong(1)));
                    count++;
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {}
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            int len = DocBlock.size();
            long[] docs = new long[len];
            for (int i = 0; i < len; i++) {
                docs[i] = ((Long) DocBlock.elementAt(i)).longValue();
            }
            // Add the thread block to cache
            try {
                rmCache.putInGroup(key, MSGBLOCKCACHEPRIX + groupname, docs);
            } catch (Exception e) {
                Logger.getLogger(getClass()).error(e.getMessage());
            }
            /**
             * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
             * the case, it means two things:
             *  1) We're at the end boundary of all the results.
             *  2) If the start index is greater than the length of the current
             *     block, than there aren't really any results to return.
             */
            if (startIndex >= blockStart + docs.length) {
                // Return an empty array
                return EMPTY_BLOCK;
            } else {
                return docs;
            }
        }
    }

    /**
     * 取得版块中的thread数量
     * @param sql String
     * @param boardcode String
     * @return int
     */
    public int getThreadsCount(String sql, String boardcode) {
            // 根据sql语句得出计算总数的sql查询语句
            String query = SQLFilter.getCountSql(sql);
            if (!SecurityUtil.isValidSql(query))
                return -1;
            Integer count = null;
            try {
                count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME + boardcode);
            } catch (Exception e) {
                Logger.getLogger(getClass()).error(e.getMessage());
            }

            // If already in cache, return the count.
            if (count != null) {
                return count.intValue();
            }
            // Otherwise, we have to load the count from the db.
            else {
                int docCount = 0;
                Conn conn = new Conn(connname);
                ResultSet rs = null;
                try {
                    rs = conn.executeQuery(query);
                    if (rs.next())
                        docCount = rs.getInt(1);
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        rs = null;
                    }
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }
                }
                // Add the thread count to cache
                try {
                    rmCache.putInGroup(query, COUNT_GROUP_NAME + boardcode,
                                       new Integer(docCount));
                } catch (Exception e) {
                    Logger.getLogger(getClass()).error(e.getMessage());
                }
                return docCount;
        }
    }

    /**
     * 取得主题贴及其回复的数量
     * @param sql String
     * @return int -1 表示sql语句不合法
     */
    public long getMsgCount(String sql, String boardcode, long rootid) {
        //根据sql语句得出计算总数的sql查询语句
        String query = SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query))
            return -1;
        Long count = null;
        try {
            count = (Long) rmCache.getFromGroup(query, COUNT_GROUP_NAME + boardcode + rootid);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }

        // If already in cache, return the count.
        if (count != null) {
            return count.longValue();
        }
        // Otherwise, we have to load the count from the db.
        else {
            long docCount = 0;
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                rs = conn.executeQuery(query);
                if (rs.next())
                    docCount = rs.getLong(1);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            try {
                rmCache.putInGroup(query, COUNT_GROUP_NAME + boardcode + rootid,
                                   new Long(docCount));
            } catch (Exception e) {
                Logger.getLogger(getClass()).getLogger(getClass()).error(e.getMessage());
            }
            return docCount;
        }
    }

    /**
     * 有三种cache
     * 1、自身 2、sql结果集的总数 3、thread 4、版面列表 后三种为组
     * @param isInvalidateGroup boolean
     */
    public void removeFromCache(long id) {
        try {
            rmCache.remove(cachePrix + id);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }
    }

    /**
     * 更新boardcode版块的threads列表
     * @param boardcode String
     */
    public void removeFromCacheThreadsBlock(String boardcode) {
        try {
            rmCache.invalidateGroup(THREADBLOCKCACHEPRIX + boardcode);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }
    }

    /**
     * 更新thread (rootid) 的贴子列表
     * @param boardcode String
     * @param rootid int
     */
    public void removeFromCacheMsgBlock(String boardcode, long rootid) {
        try {
            rmCache.invalidateGroup(MSGBLOCKCACHEPRIX + boardcode + rootid);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }
    }

    public void removeFromCacheThreadsCount(String boardcode) {
        try {
            rmCache.invalidateGroup(COUNT_GROUP_NAME + boardcode);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }
    }

    public void removeFromCacheMsgCount(String boardcode, long rootid) {
        try {
            rmCache.invalidateGroup(COUNT_GROUP_NAME + boardcode + rootid);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }
    }

    /**
     * 当发贴后刷新相关缓存
     * @param boardcode String
     * @param blogId long
     * @param isBlog boolean
     * @param blogUserDir String
     */
    public void refreshAdd(String boardcode, long blogId, boolean isBlog, String blogUserDir) {
        removeFromCacheThreadsBlock(boardcode);
        removeFromCacheThreadsCount(boardcode);
        // 刷新leaf
        // Leaf lf = new Leaf();
        // lf.removeFromCache(boardcode);
        if (isBlog) {
            removeFromCacheThreadsBlock(MsgDb.getVirtualBoardcodeOfBlog(
                    blogId, blogUserDir));
            removeFromCacheThreadsCount(MsgDb.getVirtualBoardcodeOfBlog(
                    blogId, blogUserDir));

            // 刷新全部文章的列表
            removeFromCacheThreadsBlock(MsgDb.getVirtualBoardcodeOfBlog(
                    blogId, ""));
            removeFromCacheThreadsCount(MsgDb.getVirtualBoardcodeOfBlog(
                    blogId, ""));
        }
    }

    /**
     * 当转移贴子至其它版块时，刷新缓存
     * @param id long
     * @param boardcode String
     * @param newboardcode String
     */
    public void refreshChangeBoard(long id, String boardcode, String newboardcode) {
        removeFromCacheThreadsBlock(boardcode);
        removeFromCacheThreadsCount(boardcode);
        removeFromCacheThreadsBlock(newboardcode);
        removeFromCacheThreadsCount(newboardcode);
        removeFromCache(id);
    }

    /**
     * 回复时刷新相关缓存
     * @param boardcode String
     * @param rootid long
     */
    public void refreshReply(String boardcode, long rootid) {
        // logger.info("refreshReply boardcode=" + boardcode);
        // 2007.04.25 在AddQuickReply AddReply AddReplyWE中都有对主题贴的save()，而在save()方法中包含了下句
        // removeFromCache(rootid); // 更新rootid的recount rename

        removeFromCacheThreadsBlock(boardcode); // 刷新贴子列表，以使被回贴可被up
        removeFromCacheMsgBlock(boardcode, rootid);
        removeFromCacheMsgCount(boardcode, rootid);
        // logger.info("refreshReply rootid=" + boardcode);
    }

    /**
     * 修改贴子时刷新缓存
     * @param id long
     */
    public void refreshUpdate(long id) {
        removeFromCache(id);
    }

    /**
     * 当更改文章所属用户博客的目录时，更新相应缓存
     * @param id int
     * @param userName String
     * @param isBlog boolean
     * @param blogUserDir String
     * @param oldBlogUserDir String
     */
    public void refreshUpdate(long id, long blogId, boolean isBlog, String blogUserDir, String oldBlogUserDir) {
        removeFromCacheThreadsBlock(MsgDb.getVirtualBoardcodeOfBlog(
                blogId, blogUserDir));
        removeFromCacheThreadsCount(MsgDb.getVirtualBoardcodeOfBlog(
                blogId, blogUserDir));
        removeFromCacheThreadsBlock(MsgDb.getVirtualBoardcodeOfBlog(
                blogId, oldBlogUserDir));
        removeFromCacheThreadsCount(MsgDb.getVirtualBoardcodeOfBlog(
                blogId, oldBlogUserDir));

        // 当贴子由非博客贴在论坛中编辑为博客贴时，刷新博客中全部文章的列表
        removeFromCacheThreadsBlock(MsgDb.getVirtualBoardcodeOfBlog(
                blogId, ""));
        removeFromCacheThreadsCount(MsgDb.getVirtualBoardcodeOfBlog(
                blogId, ""));

        refreshUpdate(id);
    }

    public void refreshThreadList(String boardcode) {
        removeFromCacheThreadsBlock(boardcode);
        removeFromCacheThreadsCount(boardcode);
    }

    public void refreshMsgAndList(String boardcode, long id) {
        removeFromCache(id);
        removeFromCacheThreadsBlock(boardcode);
        removeFromCacheThreadsCount(boardcode);
    }

    /**
     * 删除主题贴时刷新相关缓存
     * @param boardcode String
     * @param id long
     * @param blogId long
     * @param isBlog boolean
     * @param blogUserDir String
     */
    public void refreshDelRoot(String boardcode, long id, long blogId, boolean isBlog, String blogUserDir) {
         removeFromCache(id);
         removeFromCacheThreadsBlock(boardcode);
         removeFromCacheThreadsCount(boardcode);

         removeFromCacheMsgBlock(boardcode, id);
         removeFromCacheMsgCount(boardcode, id);

         if (isBlog) {
             removeFromCacheThreadsBlock(MsgDb.getVirtualBoardcodeOfBlog(blogId, blogUserDir));
             removeFromCacheThreadsCount(MsgDb.getVirtualBoardcodeOfBlog(blogId, blogUserDir));

             // 刷新博客的列表
             removeFromCacheThreadsBlock(MsgDb.getVirtualBoardcodeOfBlog(
                     blogId, ""));
             removeFromCacheThreadsCount(MsgDb.getVirtualBoardcodeOfBlog(
                    blogId, ""));
         }
    }

    public void refreshDelReply(String boardcode, long rootid, long id) {
         removeFromCache(id);
         removeFromCacheMsgBlock(boardcode, rootid);
         removeFromCacheMsgCount(boardcode, rootid);
         //logger.info("refreshDelReply: boardcode=" + boardcode + " rootid=" + rootid + " id=" + id);
    }
}
