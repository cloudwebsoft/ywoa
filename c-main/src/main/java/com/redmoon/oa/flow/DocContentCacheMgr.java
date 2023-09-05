package com.redmoon.oa.flow;

import cn.js.fan.cache.jcs.RMCache;
import java.sql.ResultSet;
import cn.js.fan.web.Global;
import java.sql.SQLException;
import cn.js.fan.db.Conn;
import cn.js.fan.db.SQLFilter;
import java.util.Vector;
import cn.js.fan.cache.jcs.ICacheMgr;
import cn.js.fan.security.SecurityUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class DocContentCacheMgr implements ICacheMgr {

    public static final int DOCCONTENT_BLOCK_SIZE = 100;
    private static final long[] EMPTY_BLOCK = new long[0];

    String DOCCONTENTBLOCKCACHEPRIX = "FLOW_DOCCONTENTBLOCK";
    String cachePrix = "flow_doc_content_";
    String COUNT_GROUP_NAME = "flow_CONTENT_COUNT_";

    static boolean isRegisted = false;

    RMCache rmCache = RMCache.getInstance();

    String connname = "";

    public DocContentCacheMgr() {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("DocContentCacheMgr:默认数据库名为空！");
        }

        regist();
    }

    /**
     * 定时刷新缓存
     */
    @Override
    public void timer() {
    }

    /**
     * regist in RMCache
     */
    @Override
    public void regist() {
        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
        }
    }

    public void refreshCreate(int doc_id ) {
            //清DOCBLOCKCACHEPRIX缓存
            try {
                rmCache.invalidateGroup(DOCCONTENTBLOCKCACHEPRIX + doc_id);
                rmCache.invalidateGroup(COUNT_GROUP_NAME);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void refreshUpdate(int doc_id, int page_num) {
        removeFromCache(doc_id, page_num);
    }

    public void refreshDel(int doc_id, int page_num) {
        // 更新缓存
        removeFromCache(doc_id, page_num);
        // 清组DOCBLOCKCACHEPRIX缓存
        try {
            rmCache.invalidateGroup(DOCCONTENTBLOCKCACHEPRIX + doc_id);
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void removeFromCache(int doc_id, int page_num) {
        try {
            rmCache.remove(cachePrix+ doc_id + ":" + page_num);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    protected long[] getDocContentBlock(String query, String groupKey, int startIndex) {
        // First, discover what block number the results will be in.
        int blockID = startIndex / DOCCONTENT_BLOCK_SIZE;
        int blockStart = blockID * DOCCONTENT_BLOCK_SIZE;
        // Now, check cache to see if the block is already cached. The key is
        // simply the query plus the blockID.
        String key = query + blockID;

        long[] longArray = null;
        try {
            longArray = (long[]) rmCache.getFromGroup(key,
                    DOCCONTENTBLOCKCACHEPRIX + groupKey);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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
                conn.setMaxRows(DOCCONTENT_BLOCK_SIZE * (blockID + 1));
                rs = conn.executeQuery(query);
                //LogUtil.getLog(getClass()).info("query=" + query);
                // Grab THREAD_BLOCK_ROWS rows at a time.
                conn.setFetchSize(DOCCONTENT_BLOCK_SIZE);
                // Many JDBC drivers don't implement scrollable cursors the real
                // way, but instead load all results into memory. Looping through
                // the results ourselves is more efficient.
                for (int i = 0; i < blockStart; i++) {
                    rs.next();
                }
                // Keep reading results until the result set is exaughsted or
                // we come to the end of the block.
                int count = 0;
                while (rs.next() && count < DOCCONTENT_BLOCK_SIZE) {
                    DocBlock.addElement(new Long(rs.getLong(1)));
                    count++;
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {LogUtil.getLog(getClass()).error(e);}
                }
                conn.close();
            }
            int len = DocBlock.size();
            long[] docs = new long[len];
            for (int i = 0; i < len; i++) {
                docs[i] = (Long) DocBlock.elementAt(i);
            }
            // Add the thread block to cache
            try {
                rmCache.putInGroup(key, DOCCONTENTBLOCKCACHEPRIX + groupKey, docs);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
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
     *
     * @param sql String
     * @return int -1 表示sql语句不合法
     */
    public int getDocContentCount(String sql) {
        //根据sql语句得出计算总数的sql查询语句
        String query = SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query))
            return -1;
        Integer count = null;
        try {
            count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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
                if (rs.next()) {
                    docCount = rs.getInt(1);
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
                conn.close();
            }
            // Add the thread count to cache
            try {
                rmCache.putInGroup(query, COUNT_GROUP_NAME, docCount);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            return docCount;
        }
    }

    public DocContent getDocContent(int doc_id, int page_num) {
        DocContent dc = (DocContent) rmCache.get(cachePrix + doc_id);
        if (dc == null) {
            dc = new DocContent(doc_id, page_num);
            try {
                rmCache.put(cachePrix + doc_id + ":" + page_num, dc);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getDocContent:" + e.getMessage());
            }
            return dc;
        } else {
            dc.renew();
            return dc;
        }
    }

    /**
     *
     * @return int -1 表示sql语句不合法
     */
    public int getContentCount(int docId) {
        //根据sql语句得出计算总数的sql查询语句
        String query = "select count(*) from flow_doc_content where doc_id=" + docId;

        Integer count = null;
        try {
            count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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
                if (rs.next()) {
                    docCount = rs.getInt(1);
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
                conn.close();
            }
            // Add the thread count to cache
            try {
                rmCache.putInGroup(query, COUNT_GROUP_NAME, docCount);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            return docCount;
        }
    }
}
