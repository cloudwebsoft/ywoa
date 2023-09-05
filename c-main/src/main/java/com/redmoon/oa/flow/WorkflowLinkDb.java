package com.redmoon.oa.flow;

import java.io.Serializable;
import java.sql.*;
import java.util.Calendar;
import java.util.Vector;

import cn.js.fan.db.Conn;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IWorkflowHelper;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.db.SequenceManager;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.oacalendar.OACalendarDb;

/**
 * workflow_link:2,1,,172,174,100,118,150,118,125,118,125,118,0,2005,05,02,新用户13,蓝风;
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
public class WorkflowLinkDb implements Serializable{
    String connname = "";
    static final String INSERT = "insert into flow_link (id, flow_id, action_from, action_to,speedup_date,isSpeedup,title,type,cond_desc,item1,expire_hour,expire_action) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    final String LOAD = "select flow_id,action_from,action_to,isSpeedup,speedup_date,title,type,cond_desc,item1,expire_hour,expire_action from flow_link where id=?";
    final String SAVE = "update flow_link set action_from=?,action_to=?,isSpeedup=?,speedup_date=?,title=?,type=?,cond_desc=?,item1=?,expire_hour=?,expire_action=? where id=?";
    final String DELETE = "delete from flow_link where id=?";

    public static final int TYPE_TOWARD = 0; // 流向
    public static final int TYPE_RETURN = 1; // 打回
    public static final int TYPE_BOTH = 2;   // 流向及打回

    private String expireAction;
    public static final String COND_TYPE_FORM = "";
    public static final String COND_TYPE_ROLE = "role";
    public static final String COND_TYPE_DEPT = "dept";
    public static final String COND_TYPE_COMB_COND = "comb_cond";
    public static final String COND_TYPE_NONE = "-1";

    public static final String COND_TYPE_SCRIPT = "script";

    public static final String COND_TYPE_MUST = "1";
    public static final String COND_TYPE_DEPT_BELONG = "dept_belong";

    public WorkflowLinkDb() {
        init();
    }

    public WorkflowLinkDb(int id) {
        init();
        this.id = id;
        loadFromDb();
    }

    private String title;

    public void init() {
        flowId = -1;
        speedupDate = Calendar.getInstance();
        connname = Global.getDefaultDB();
    }

    private Calendar speedupDate;

    public void setId(int id) {
        this.id = id;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public void setSpeedupDate(Calendar speedupDate) {
        this.speedupDate = speedupDate;
    }

    public WorkflowActionDb getToAction() {
        WorkflowActionDb wa = new WorkflowActionDb();
        return wa.getWorkflowActionDbByInternalName(to, flowId);
    }

    /**
     * 找出从from至to节点之间的连接线（这根线有可能为连接线，也可能为打回线，也可能为both），或者to至from，但类型为both的连接线
     * 用于当需要notifyUser时，获取连接线上的expireHour
     * @param from WorkflowActionDb
     * @param to WorkflowActionDb
     * @return WorkflowLinkDb
     */
    public WorkflowLinkDb getWorkflowLinkDbForward(WorkflowActionDb from, WorkflowActionDb to) {
        String sql = "select id from flow_link where flow_id=? and action_from=? and action_to=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, from.getFlowId());
            pstmt.setString(2, from.getInternalName());
            pstmt.setString(3, to.getInternalName());
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    int linkId = rs.getInt(1);
                    return getWorkflowLinkDb(linkId);
                }
            }

            pstmt.close();

            // 如果没有找到，则继续寻找为both型的从to至from的连接线
            sql = "select id from flow_link where flow_id=? and action_from=? and action_to=? and type=" + TYPE_BOTH;
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, from.getFlowId());
            pstmt.setString(2, to.getInternalName());
            pstmt.setString(3, from.getInternalName());
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    int linkId = rs.getInt(1);
                    return getWorkflowLinkDb(linkId);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getToLinks:" + e.getMessage());
        } finally {
            conn.close();
        }

        return null;
    }

    /**
     * 取得流程中的所有连线
     * @param flowId
     * @return
     */
    public Vector<WorkflowLinkDb> getLinksOfFlow(int flowId) {
        Vector<WorkflowLinkDb> ret = new Vector<>();
        String sql = "select id from flow_link where flow_id=? order by id asc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int linkId = rs.getInt(1);
                    ret.addElement(getWorkflowLinkDb(linkId));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getLinksOfFlow:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return ret;
    }

    /**
     * 获取从当前节点连出的连接线，除去打回线
     * @param curAction WorkflowActionDb
     * @return int
     */
    public Vector<WorkflowLinkDb> getToWorkflowLinks(WorkflowActionDb curAction) {
        Vector<WorkflowLinkDb> ret = new Vector<>();
        String sql =
                "select id from flow_link where action_from=? and flow_id=? and type<>" +
                WorkflowLinkDb.TYPE_RETURN;
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, curAction.getInternalName());
            pstmt.setInt(2, curAction.getFlowId());
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int linkId = rs.getInt(1);
                    ret.addElement(getWorkflowLinkDb(linkId));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getToWorkflowLinks:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return ret;
    }

    public WorkflowLinkDb getWorkflowLinkDb(int flowId, String from, String to) {
        String sql = "select id from flow_link where flow_id=? and action_from=? and action_to=?";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            pstmt.setString(2, from);
            pstmt.setString(3, to);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                return null;
            } else {
                int id = rs.getInt(1);
                return getWorkflowLinkDb(id);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getWorkflowLinkDb:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }
    
    /**
     * 获取从当前节点连出的连接线的数量即入度(除去返回线)，用于得到开始节点（多起点）fgf 20160924
     * @Description: 
     * @param curAction
     * @return
     */
    public int getFromLinkCount(WorkflowActionDb curAction) {
    	int ret = 0;
        String sql =
                "select count(id) from flow_link where action_to=? and flow_id=? and type<>" +
                WorkflowLinkDb.TYPE_RETURN;
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, curAction.getInternalName());
            pstmt.setInt(2, curAction.getFlowId());
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    ret = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getFromWorkflowLinksCount:" + e.getMessage());
        } finally {
            conn.close();
        }
        return ret;
    }    

    public void setIsSpeedup(int isSpeedup) {
        this.isSpeedup = isSpeedup;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCondDesc(String condDesc) {
        this.condDesc = condDesc;
    }

    public void setCondType(String condType) {
        this.condType = condType;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setExpireHour(double expireHour) {
        this.expireHour = expireHour;
    }

    public void setExpireAction(String expireAction) {
        this.expireAction = expireAction;
    }

    public int getId() {
        return id;
    }

    public int getFlowId() {
        return flowId;
    }

    public Calendar getSpeedupDate() {
        return speedupDate;
    }

    public int getIsSpeedup() {
        return isSpeedup;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return type;
    }

    public String getCondDesc() {
        return condDesc;
    }

    public String getCondType() {
        return condType;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public double getExpireHour() {
        return expireHour;
    }

    public String getExpireAction() {
        return expireAction;
    }

    /**
     * @param str String
     */
    public boolean fromString(String str) throws ErrMsgException {
        IWorkflowHelper workflowHelper = SpringUtil.getBean(IWorkflowHelper.class);
        return workflowHelper.fromString(this, str);
    }

    public void createAddBatch(PreparedStatement pstmt) {
        this.id = (int) SequenceManager.nextID(SequenceManager.OA_WORKFLOW_LINK);
        try {
            pstmt.setInt(1, id);
            pstmt.setInt(2, flowId);
            pstmt.setString(3, from);
            pstmt.setString(4, to);
            pstmt.setDate(5, new java.sql.Date(speedupDate.getTimeInMillis()));
            pstmt.setInt(6, isSpeedup);
            pstmt.setString(7, title);
            pstmt.setInt(8, type);
            pstmt.setString(9, condDesc);
            pstmt.setString(10, condType);
            pstmt.setDouble(11, expireHour);
            pstmt.setString(12, expireAction);
            pstmt.addBatch();
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
    }

    public boolean create() {
        this.id = (int) SequenceManager.nextID(SequenceManager.OA_WORKFLOW_LINK);
        Conn conn = new Conn(connname);
        try {
            // 更新文件内容
            // (id, flow_id, action_form, action_to,speedup_date,isSpeedup) values (?,?,?,?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setInt(1, id);
            pstmt.setInt(2, flowId);
            pstmt.setString(3, from);
            pstmt.setString(4, to);
            pstmt.setDate(5, new java.sql.Date(speedupDate.getTimeInMillis()));
            pstmt.setInt(6, isSpeedup);
            pstmt.setString(7, title);
            pstmt.setInt(8, type);
            pstmt.setString(9, condDesc);
            pstmt.setString(10, condType);
            pstmt.setDouble(11, expireHour);
            pstmt.setString(12, expireAction);
            int r = conn.executePreUpdate();
            if (r==1) {
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setString(1, from);
            pstmt.setString(2, to);
            pstmt.setInt(3, isSpeedup);
            if (speedupDate==null) {
                pstmt.setTimestamp(4, null);
            } else {
                pstmt.setTimestamp(4, new Timestamp(speedupDate.getTimeInMillis()));
            }
            pstmt.setString(5, title);
            pstmt.setInt(6, type);
            pstmt.setString(7, condDesc);
            pstmt.setString(8, condType);
            pstmt.setDouble(9, expireHour);
            pstmt.setString(10, expireAction);
            pstmt.setInt(11, id);
            int r = conn.executePreUpdate();
            if (r==1)
                return true;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

    public static String tran(String str) {
        str = str.replaceAll("\\\\quot", "\"");
        str = str.replaceAll("\\\\colon", ":");
        str = str.replaceAll("\\\\semicolon", ";");
        str = str.replaceAll("\\\\comma", ",");
        str = str.replaceAll("\\\\newline","\r\n");
        return str;
    }

    public WorkflowLinkDb getWorkflowLinkDb(int id) {
        WorkflowLinkCacheMgr wacm = new WorkflowLinkCacheMgr();
        return wacm.getWorkflowLinkDb(id);
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(DELETE);
            pstmt.setInt(1, id);
            int r = pstmt.executeUpdate();
            if (r==1) {
                // 更新缓存
                WorkflowLinkCacheMgr wlc = new WorkflowLinkCacheMgr();
                wlc.refreshDel(id);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            return false;
        } finally {
            conn.close();
        }
        return false;
    }

    private int id;
    private int flowId;
    private int isSpeedup;
    private String to;
    private String from;

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                LogUtil.getLog(getClass()).error("流程连接id= " + id +
                             " 在数据库中未找到.");
            } else {
                this.flowId = rs.getInt(1);
                this.from = rs.getString(2);
                this.to = rs.getString(3);
                this.isSpeedup = rs.getInt(4);
                this.speedupDate.setTime(rs.getDate(5));
                this.title = StrUtil.getNullString(rs.getString(6));
                this.type = rs.getInt(7);
                this.condDesc = StrUtil.getNullString(rs.getString(8));
                condType = StrUtil.getNullString(rs.getString(9));
                expireHour = rs.getDouble(10);
                expireAction = StrUtil.getNullStr(rs.getString(11));
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDb:" + e.getMessage());
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
    }

    /**
     * 取得到期时间
     * @return Date
     */
    public java.util.Date calulateExpireDate() {
        // 加上对于休息日的处理，如果在指定处理时间（小时）范围内，有工作日，则顺延
        // 如果关联
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        LogUtil.getLog(getClass()).info("flowExpireRelateOACalendar id=" + cfg.get("flowExpireRelateOACalendar"));

        if ("true".equals(cfg.get("flowExpireRelateOACalendar"))) {
            String flowExpireUnit = cfg.get("flowExpireUnit");
            LogUtil.getLog(getClass()).info("flowExpireUnit id=" + flowExpireUnit);

            if ("day".equals(flowExpireUnit)) {
                // 当天不计入超时时间
                // 遍历指定的当天其后的expire天，如果是休息日，则不计入，往后顺延
                int expireDay = (int)getExpireHour();

                return OACalendarDb.addWorkDay(expireDay);
            }
            else {
                return OACalendarDb.addWorkHour(expireHour);
            }
        }
        else {
            String flowExpireUnit = cfg.get("flowExpireUnit");
            if ("day".equals(flowExpireUnit)) {
                return DateUtil.addDate(new java.
                        util.
                        Date(),
                        (int)getExpireHour());
            }
            else {
                return OACalendarDb.addHour(new java.
                        util.
                        Date(),
                        getExpireHour());
            }
        }
    }

    public static String getINSERT() {
        return INSERT;
    }

    private int type = TYPE_TOWARD;
    private String condDesc;
    private String condType;
    private boolean loaded = false;
    private double expireHour = 0;

}
