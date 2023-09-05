package com.redmoon.oa.flow;

import cn.js.fan.cache.jcs.AbstractRMCacheMgr;
import cn.js.fan.db.Conn;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.security.SecurityUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class WorkflowCacheMgr extends AbstractRMCacheMgr {
    public static final int FLOW_BLOCK_SIZE = 100;
    private static final long[] EMPTY_BLOCK = new long[0];

    // final String FLOWBLOCKCACHEPRIX = "FLOWBLOCK";
    final String FLOW_COUNT_KEY = "FLOW_COUNT";
    public static final String FLOW_GROUP_KEY = "FLOW_GROUP";

    public WorkflowCacheMgr() {
    }

    public void initLogger() {

    }

    public void initCachePrix() {
        cachePrix = "Workflow_";
    }

    public WorkflowDb getWorkflow(int id) {
        WorkflowDb wf = (WorkflowDb) rmCache.get(cachePrix + id);
        if (wf == null) {
            //LogUtil.getLog(getClass()).info( "getWorkflow: id=" + id);
            wf = new WorkflowDb(id);
            try {
                rmCache.put(cachePrix + id, wf);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getWorkflow:" + e.getMessage());
            }
            return wf;
        } else {
            return wf;
        }
    }

    public void removeFromCache(int id) {
        try {
            rmCache.remove(cachePrix + id);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void refreshSave(int id) {
        removeFromCache(id);
    }

    public void refreshDel(int id) {
        removeFromCache(id);
        refreshList();
    }

    public void refreshPutOrClearProxy() {
        refreshList();
    }

    public void refreshList() {
        // 清除缓存
        try {
            rmCache.invalidateGroup(FLOW_GROUP_KEY);
            rmCache.invalidateGroup(FLOW_COUNT_KEY);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public void refreshCreate() {
        // 清除缓存
        refreshList();
    }

    protected long[] getWorkflowBlock(String query, String groupKey, int startIndex) {
        // !!!!!-----此处限定groupKey为FLOW_GROUP_NAME，以后可考虑用用户名作为组来优化
        groupKey = FLOW_GROUP_KEY;
        // First, discover what block number the results will be in.
        int blockID = startIndex / FLOW_BLOCK_SIZE;
        int blockStart = blockID * FLOW_BLOCK_SIZE;
        // Now, check cache to see if the block is already cached. The key is
        // simply the query plus the blockID.
        String key = query + blockID;

        long[] longArray = null;
        try {
            longArray = (long[]) rmCache.getFromGroup(key,
                    groupKey);
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
            Vector FlowBlock = new Vector();
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                // Set the maxium number of rows to end at the end of this block.
                conn.setMaxRows(FLOW_BLOCK_SIZE * (blockID + 1));
                rs = conn.executeQuery(query);
                //LogUtil.getLog(getClass()).info("query=" + query);
                // Grab THREAD_BLOCK_ROWS rows at a time.
                conn.setFetchSize(FLOW_BLOCK_SIZE);
                // Many JDBC drivers don't implement scrollable cursors the real
                // way, but instead load all results into memory. Looping through
                // the results ourselves is more efficient.
                for (int i = 0; i < blockStart; i++) {
                    rs.next();
                }
                // Keep reading results until the result set is exaughsted or
                // we come to the end of the block.
                int count = 0;
                while (rs.next() && count < FLOW_BLOCK_SIZE) {
                    FlowBlock.addElement(new Long(rs.getLong(1)));
                    count++;
                }
            } catch (SQLException sqle) {
                LogUtil.getLog(getClass()).error(sqle.getMessage());
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
            int len = FlowBlock.size();
            long[] docs = new long[len];
            for (int i = 0; i < len; i++) {
                docs[i] = ((Long) FlowBlock.elementAt(i)).longValue();
            }
            // Add the thread block to cache
            try {
                rmCache.putInGroup(key, groupKey, docs);
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
    public int getWorkflowCount(String sql) {
        //根据sql语句得出计算总数的sql查询语句
        String query = SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query)) {
            return -1;
        }
        Integer count = null;
        try {
            count = (Integer) rmCache.getFromGroup(query, FLOW_COUNT_KEY);
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
                rmCache.putInGroup(query, FLOW_COUNT_KEY, docCount);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            return docCount;
        }
    }

}
