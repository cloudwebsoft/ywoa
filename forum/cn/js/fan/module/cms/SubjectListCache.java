package cn.js.fan.module.cms;

import com.cloudwebsoft.framework.base.*;
import cn.js.fan.db.Conn;
import cn.js.fan.db.SQLFilter;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.security.SecurityUtil;
import com.cloudwebsoft.framework.db.Connection;

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
public class SubjectListCache extends ObjectCache {
    public static final int DOC_BLOCK_SIZE = 100;
    private static final long[] EMPTY_BLOCK = new long[0];
    String DOCBLOCKCACHEPRIX = "SUBJECT_BLOCK_";

    public SubjectListCache(SubjectListDb sld) {
        super(sld);
    }

    /**
     * 当更新文章时，如果examne被改变时，则需用此函数更新缓存
     * @param id int
     * @param dir_code String
     * @param parent_code String
     */
    public void refreshList(String subjectCode) {
        // 清组DOCBLOCKCACHEPRIX缓存
        try {
            rmCache.invalidateGroup(DOCBLOCKCACHEPRIX + subjectCode);
            rmCache.invalidateGroup(COUNT_GROUP_NAME);
        } catch (Exception e) {
            logger.error(e.getMessage());
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
            // logger.info("getDocBlock:" +DOCBLOCKCACHEPRIX + groupKey);
            longArray = (long[]) rmCache.getFromGroup(key,
                    DOCBLOCKCACHEPRIX + groupKey);
        } catch (Exception e) {
            logger.error(e.getMessage());
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
            Connection conn = new Connection(connname);
            ResultSet rs = null;
            try {
                // Set the maxium number of rows to end at the end of this block.
                conn.setMaxRows(DOC_BLOCK_SIZE * (blockID + 1));
                rs = conn.executeQuery(query);
                //logger.info("query=" + query);
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
                logger.error("getDocBlock: " + sqle.getMessage());
                // sqle.printStackTrace();
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
                rmCache.putInGroup(key, DOCBLOCKCACHEPRIX + groupKey, docs);
            } catch (Exception e) {
                logger.error("getDocBlock1:" + e.getMessage());
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
        if (!SecurityUtil.isValidSql(query))
            return -1;
        Integer count = null;
        try {
            count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME);
        } catch (Exception e) {
            logger.error(e.getMessage());
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
                rmCache.putInGroup(query, COUNT_GROUP_NAME,
                                   new Integer(docCount));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return docCount;
        }
    }
}
