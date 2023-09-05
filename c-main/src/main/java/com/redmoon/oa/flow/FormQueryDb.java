package com.redmoon.oa.flow;

import java.sql.*;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

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
public class FormQueryDb extends ObjectDb {
    public static final java.util.Date TIMEPOINT_CURRENT = null;
    private int flowStatus = 1000; // 不限
	private String scripts;
	
	private boolean script = false;

    public FormQueryDb() {
    }

    public String getScripts() {
		return scripts;
	}

	public void setScripts(String scripts) {
		this.scripts = scripts;
	}

	public FormQueryDb(int id) {
        this.id = id;
        init();
        load();
    }

    @Override
    public void initDB() {
        tableName = "form_query";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new FormQueryCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName +
                       " (id,query_name,show_field_code,table_code,order_field_code,dept_code,time_point,user_name,chart_pie,CHART_HISTOGRAM, chart_line,chart_tb,is_saved, query_related, flow_status, flow_begin_date1, flow_begin_date2, scripts, is_script, is_system) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName +
                     " set query_name=?,show_field_code=?,table_code=?,order_field_code=?,dept_code=?,time_point=?,chart_pie=?,CHART_HISTOGRAM=?, chart_line=?,chart_tb=?,is_saved=?,col_props=?,query_related=?,stat_desc=?,flow_status=?,flow_begin_date1=?,flow_begin_date2=?,scripts=?,is_script=? where id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select id,query_name,show_field_code,table_code,order_field_code,dept_code,time_point,user_name,chart_pie,CHART_HISTOGRAM, chart_line,chart_tb,is_saved,col_props,query_related,stat_desc,flow_status,flow_begin_date1,flow_begin_date2,is_system,scripts,is_script from " +
                     tableName + " where id=?";
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new FormQueryDb(pk.getIntValue());
    }

    public FormQueryDb getFormQueryDb(int id) {
        return (FormQueryDb) getObjectDb(new Integer(id));
    }

    @Override
    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            id = (int) SequenceManager.nextID(SequenceManager.OA_FORM_QUERY);
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setInt(1, id);
            pstmt.setString(2, queryName);
            pstmt.setString(3, showFieldCode);
            pstmt.setString(4, tableCode);
            pstmt.setString(5, orderFieldCode);
            pstmt.setString(6, deptCode);

            if(timePoint==null) {
                pstmt.setTimestamp(7, null);
            } else {
                pstmt.setTimestamp(7, new Timestamp(timePoint.getTime()));
            }
            pstmt.setString(8, userName);

            pstmt.setString(9, chartPie);
            pstmt.setString(10, chartHistogram);
            pstmt.setString(11, chartLine);
            pstmt.setString(12, chartTb);
            pstmt.setInt(13, saved?1:0);
            pstmt.setString(14, queryRelated);
            pstmt.setInt(15, flowStatus);
            if (flowBeginDate1==null) {
                pstmt.setTimestamp(16, null);
            } else {
                pstmt.setTimestamp(16, new Timestamp(flowBeginDate1.getTime()));
            }
            if (flowBeginDate2==null) {
                pstmt.setTimestamp(17, null);
            } else {
                pstmt.setTimestamp(17, new Timestamp(flowBeginDate2.getTime()));
            }
            
            pstmt.setString(18, scripts);
            pstmt.setInt(19, script?1:0);
            pstmt.setInt(20, system?1:0);
            
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                FormQueryCache aqc = new FormQueryCache(this);
                aqc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException("插入FormQuery时出错！");
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, queryName);
            pstmt.setString(2, showFieldCode);
            pstmt.setString(3, tableCode);
            pstmt.setString(4, orderFieldCode);
            pstmt.setString(5,deptCode);
            if(timePoint==null) {
                pstmt.setTimestamp(6, null);
            } else {
                pstmt.setTimestamp(6, new Timestamp(timePoint.getTime()));
            }

            pstmt.setString(7, chartPie);
            pstmt.setString(8, chartHistogram);
            pstmt.setString(9, chartLine);
            pstmt.setString(10, chartTb);
            pstmt.setInt(11, saved?1:0);
            pstmt.setString(12, colProps);
            pstmt.setString(13, queryRelated);
            pstmt.setString(14, statDesc);
            pstmt.setInt(15, flowStatus);

            if (flowBeginDate1==null) {
                pstmt.setTimestamp(16, null);
            } else {
                pstmt.setTimestamp(16, new Timestamp(flowBeginDate1.getTime()));
            }
            if (flowBeginDate2==null) {
                pstmt.setTimestamp(17, null);
            } else {
                pstmt.setTimestamp(17, new Timestamp(flowBeginDate2.getTime()));
            }
            pstmt.setString(18, scripts);
            pstmt.setInt(19, script?1:0);
            pstmt.setInt(20, id);
            re = conn.executePreUpdate() > 0;
            if (re) {
                FormQueryCache aqc = new FormQueryCache(this);
                primaryKey.setValue(id);
                aqc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
            throw new ErrMsgException("更新FormQuery时出错！");
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            LogUtil.getLog(getClass()).info("load: id=" + id + " QUERY_LOAD=" + QUERY_LOAD);
            if (rs.next()) {
                this.id = rs.getInt(1);
                this.queryName = StrUtil.getNullStr(rs.getString(2));

                LogUtil.getLog(getClass()).info("load: id=" + id + " queryName=" + queryName);

                this.showFieldCode = StrUtil.getNullStr(rs.getString(3));
                this.tableCode = StrUtil.getNullStr(rs.getString(4));
                this.orderFieldCode = StrUtil.getNullStr(rs.getString(5));
                this.deptCode = StrUtil.getNullStr(rs.getString(6));
                this.timePoint = rs.getTimestamp(7);
                userName = rs.getString(8);

                chartPie = StrUtil.getNullStr(rs.getString(9));
                chartHistogram = StrUtil.getNullStr(rs.getString(10));
                chartLine = StrUtil.getNullStr(rs.getString(11));
                chartTb = StrUtil.getNullStr(rs.getString(12));
                saved = rs.getInt(13)==1;
                colProps = StrUtil.getNullStr(rs.getString(14));

                queryRelated = StrUtil.getNullStr(rs.getString(15));
                statDesc = StrUtil.getNullStr(rs.getString(16));

                flowStatus = rs.getInt(17);
                flowBeginDate1 = rs.getTimestamp(18);
                flowBeginDate2 = rs.getTimestamp(19);
                
                system = rs.getInt(20)==1;
                scripts = StrUtil.getNullStr(rs.getString(21));
                script = rs.getInt(22)==1;

                loaded = true;
                primaryKey.setValue(id);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            conn.close();
        }
    }

    @Override
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate() > 0;
            if (re) {
                FormQueryCache aqc = new FormQueryCache(this);
                primaryKey.setValue(id);
                aqc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException("删除FormQuery时出错！");
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    FormQueryDb aqd = getFormQueryDb(rs.getInt(1));
                    result.addElement(aqd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException("数据库出错！");
        } finally {
            conn.close();
        }
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public ResultIterator getResultIterator(String sqlOfQuery) throws
            ErrMsgException {
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        try {
            ri = rmconn.executeQuery(sqlOfQuery);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getResultIterator" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        return ri;
    }

    public ResultIterator getResultIterator(String sqlOfQuery, int curPage, int pageSize) throws
            ErrMsgException {
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri;
        try {
            ri = rmconn.executeQuery(sqlOfQuery, curPage, pageSize);
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        }
        return ri;
    }

    @Override
    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(getFormQueryDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
        } finally {
            conn.close();
        }
        return result;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public void setShowFieldCode(String showFieldCode) {
        this.showFieldCode = showFieldCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }

    public void setOrderFieldCode(String orderFieldCode) {
        this.orderFieldCode = orderFieldCode;
    }

    public void setConditionFieldCode(String conditionFieldCode) {
        this.conditionFieldCode = conditionFieldCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public void setTimePoint(java.util.Date timePoint) {
        this.timePoint = timePoint;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setChartPie(String chartPie) {
        this.chartPie = chartPie;
    }

    public void setChartLine(String chartLine) {
        this.chartLine = chartLine;
    }

    public void setChartHistogram(String chartHistogram) {
        this.chartHistogram = chartHistogram;
    }

    public void setChartTb(String chartTb) {
        this.chartTb = chartTb;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public void setColProps(String colProps) {
        this.colProps = colProps;
    }

    public void setQueryRelated(String queryRelated) {
        this.queryRelated = queryRelated;
    }

    public void setStatDesc(String statDesc) {
        this.statDesc = statDesc;
    }

    public void setFlowStatus(int flowStatus) {
        this.flowStatus = flowStatus;
    }

    public void setFlowBeginDate1(java.util.Date flowBeginDate1) {
        this.flowBeginDate1 = flowBeginDate1;
    }

    public void setFlowBeginDate2(java.util.Date flowBeginDate2) {
        this.flowBeginDate2 = flowBeginDate2;
    }

    public int getId() {
        return id;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getShowFieldCode() {
        return showFieldCode;
    }

    public String getTableCode() {
        return tableCode;
    }

    public String getOrderFieldCode() {
        return orderFieldCode;
    }

    public String getConditionFieldCode() {
        return conditionFieldCode;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public java.util.Date getTimePoint() {
        return timePoint;
    }

    public String getUserName() {
        return userName;
    }

    public String getChartPie() {
        return chartPie;
    }

    public String getChartLine() {
        return chartLine;
    }

    public String getChartHistogram() {
        return chartHistogram;
    }

    public String getChartTb() {
        return chartTb;
    }

    public boolean getSaved() {
        return saved;
    }

    public String getColProps() {
        return colProps;
    }

    public String getQueryRelated() {
        return queryRelated;
    }

    public String getStatDesc() {
        return statDesc;
    }

    public int getFlowStatus() {
        return flowStatus;
    }

    public java.util.Date getFlowBeginDate1() {
        return flowBeginDate1;
    }

    public java.util.Date getFlowBeginDate2() {
        return flowBeginDate2;
    }

    /**
     * 取出主表中所关联的嵌套表查询
     * @return Vector
     */
    public Vector getSubRelatedQuery() {
        String sql = "select id from " + tableName + " where query_related=" + id + " order by time_point desc";
        return list(sql);
    }

    public void setSystem(boolean system) {
		this.system = system;
	}

	public boolean isSystem() {
		return system;
	}

	public void setScript(boolean script) {
		this.script = script;
	}

	public boolean isScript() {
		return script;
	}

	private String queryName;
    private String orderFieldCode;
    private int id;
    private String tableCode;
    private String showFieldCode;
    private String conditionFieldCode;
    /**
     * 暂无用
     */
    private String deptCode;
    private java.util.Date timePoint;
    private java.util.Date flowBeginDate2;
    private String userName;
    private String chartPie;
    private String chartLine;
    private String chartHistogram;
    private String statDesc;
    private java.util.Date flowBeginDate1;

    /**
     * 同比
     */
    private String chartTb;
    private boolean saved = true;
    private String colProps;
    /**
     * @task:原本考虑可关联多条主表的查询，但是后来发现应该只允许关联一条查询，这样可以方便从主表查出关联的副表查询
     */
    private String queryRelated;
    
    private boolean system = false;
}
