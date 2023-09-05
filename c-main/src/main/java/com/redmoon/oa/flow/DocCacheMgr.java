package com.redmoon.oa.flow;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.Conn;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.web.Global;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.security.SecurityUtil;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * 有三个缓存与Document关联，一个是用于Iterator模式列表的DOCBLOCKCACHEPRIX，另一个是Document本身的，还有一个是用于计算count值的COUNT_GROUP_NAME
 * 当creat时，需更新组DOCBLOCKCACHEPRIX COUNT_GROUP_NAME ALL
 * 当del时，需更新组DOCBLOCKCACHEPRIX COUNT_GROUP_NAME ALL
 * 当update时，需更新cachePrix+id
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DocCacheMgr implements ICacheMgr {
    public static final int DOC_BLOCK_SIZE = 100;
    private static final long[] EMPTY_BLOCK = new long[0];

    public final static String FULLTEXT = "FULLTEXT"; // 用于全文检索，使用SQLServer的全文检索
    public final static String ALL = "ALL"; // 用于搜索，使用like %...%
    String COUNT_GROUP_NAME = "FLOW_DOC_SQL_COUNT_";
    String DOCBLOCKCACHEPRIX = "FLOW_DOCBLOCK_";
    String cachePrix = "flow_doc";

    public final static String CHILDREN_OF_PARENT = "FLOW_CHILDREN_"; // 用于在首页列表显示一个目录下所有子目录中的文章，这种方法不能包含该目录下的文章，只能是子目录中的文章

    private final long FULLTEXTMAXLIFE = 3600;
    private long curFulltextLife = FULLTEXTMAXLIFE;// one hour
    static boolean isRegisted = false;

    RMCache rmCache = RMCache.getInstance();

    String connname = "";

    public DocCacheMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("DocCacheMgr:默认数据库名为空！");

        regist();
    }

    /**
     *
     */
    public void refreshFulltext() {
        try {
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + FULLTEXT);
        }
        catch (Exception e){
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    /**
     * 定时刷新缓存
     */
    public void timer() {
        // 刷新全文检索
        curFulltextLife--;
        if (curFulltextLife<=0) {
            refreshFulltext();
            curFulltextLife = FULLTEXTMAXLIFE;
        }
    }

    /**
     * regist in RMCache
     */
    public void regist() {
        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
        }
    }

    public void refreshCreate(String dir_code, String parent_code) {
        //清DOCBLOCKCACHEPRIX缓存
        try {
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + dir_code);
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
            rmCache.invalidateGroup(ALL);

            // 刷新其父目录的所有文章的列表缓存（不包含父目录本身）
            // LogUtil.getLog(getClass()).info("refreshCreate:" + DOCBLOCKCACHEPRIX + CHILDREN_OF_PARENT + parent_code);
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + CHILDREN_OF_PARENT + parent_code);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void refreshUpdate(int id) {
        removeFromCache(id);
    }

    /**
     * 当更新文章时，如果examne审核被置为非pass，则需用此函数更新缓存
     * @param id int
     * @param dir_code String
     * @param parent_code String
     */
    public void refreshUpdate(int id, String dir_code, String parent_code) {
        // 更新缓存
        removeFromCache(id);
        // 清组DOCBLOCKCACHEPRIX缓存
        try {
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + dir_code);
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
            rmCache.invalidateGroup(ALL);

            // 刷新其父目录的所有文章的列表缓存（不包含父目录本身）
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + CHILDREN_OF_PARENT +
                                    parent_code);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void refreshChangeDirCode(String dirCodeFrom, String dirCodeTo) {
        try {
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + dirCodeFrom);
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + dirCodeTo);
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void refreshDel(int id, String dir_code, String parent_code) {
        // 更新缓存
        removeFromCache(id);
        // 清组DOCBLOCKCACHEPRIX缓存
        try {
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + dir_code);
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
            rmCache.invalidateGroup(ALL);

            // 刷新其父目录的所有文章的列表缓存（不包含父目录本身）
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + CHILDREN_OF_PARENT +
                                    parent_code);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void removeFromCache(int id) {
        try {
            rmCache.remove(cachePrix + id);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    protected long[] getDocBlock(String query, String groupKey, int startIndex) {
        // First, discover what block number the results will be in.
        int blockID = startIndex / DOC_BLOCK_SIZE;
        int blockStart = blockID * DOC_BLOCK_SIZE;
        // Now, check cache to see if the block is already cached. The key is
        // simply the query plus the blockID.
        String key = query + blockID;

        long[] longArray = null;
        try {
            // LogUtil.getLog(getClass()).info("getDocBlock:" +DOCBLOCKCACHEPRIX + groupKey);
            longArray = (long[]) rmCache.getFromGroup(key,
                    DOCBLOCKCACHEPRIX + groupKey);
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
                conn.setMaxRows(DOC_BLOCK_SIZE * (blockID + 1));
                rs = conn.executeQuery(query);
                //LogUtil.getLog(getClass()).info("query=" + query);
                // Grab THREAD_BLOCK_ROWS rows at a time.
                conn.setFetchSize(DOC_BLOCK_SIZE);
                // Many JDBC drivers don't implement scrollable cursors the real
                // way, but instead load all results into memory. Looping through
                // the results ourselves is more efficient.
                for (int i = 0; i < blockStart; i++) {
                    rs.next();
                }
                // Keep reading results until the result set is exaughsted or
                // we come to the end of the block.
                int count = 0;
                while (rs.next() && count < DOC_BLOCK_SIZE) {
                    DocBlock.addElement(new Long(rs.getLong(1)));
                    count++;
                }
            } catch (SQLException sqle) {
                LogUtil.getLog(getClass()).error("getDocBlock: " + sqle.getMessage());
                // sqlLogUtil.getLog(getClass()).error(e);
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
                rmCache.putInGroup(key, DOCBLOCKCACHEPRIX + groupKey, docs);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getDocBlock1:" + e.getMessage());
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
    public int getDocCount(String sql) {
        //根据sql语句得出计算总数的sql查询语句
        String query = SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query)) {
            return -1;
        }
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
                rmCache.putInGroup(query, COUNT_GROUP_NAME,
                        docCount);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            return docCount;
        }
    }

    public Document getDocument(int id) {
        Document doc = (Document) rmCache.get(cachePrix + id);
        if (doc == null) {
            doc = new Document(id);
            if (doc.isLoaded()) {
                try {
                    rmCache.put(cachePrix + id, doc);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getDocument:" + e.getMessage());
                }
                return doc;
            }
            else {
                return null;
            }
        } else {
            doc.renew();
            return doc;
        }
    }
}
