package com.redmoon.oa.flow;

import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.SpringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.*;
import com.redmoon.oa.person.PlanDb;

public class MyActionDb extends ObjectDb implements IDesktopUnit {
    /**
     * 未处理
     */
    public static final int CHECK_STATUS_NOT = 0;
    /**
     * 已处理
     */
    public static final int CHECK_STATUS_CHECKED = 1;

    /**
     * 挂起
     */
    public static final int CHECK_STATUS_SUSPEND = 2;

    /**
     * 转办
     */
    public static final int CHECK_STATUS_TRANSFER = 3;

    /**
     * 返回
     */
    public static final int CHECK_STATUS_RETURN = 4;

    /**
     * 移交
     */
    public static final int CHECK_STATUS_HANDOVER = 5;

    /**
     * 当节点处理策略为仅需某一人处理，节点已有某人处理后，其它待办记录将被忽略
     * 或者因放弃而被忽略，或者因回滚而忽略
     */
    public static final int CHECK_STATUS_PASS = 6;

    /**
     * 挂起结束
     */
    public static final int CHECK_STATUS_SUSPEND_OVER = 7;

    /**
     * 因为返回，而使得其它处理记录被忽略
     */
    public static final int CHECK_STATUS_PASS_BY_RETURN = 8;
    
    /**
     *  前一节点多人处理,分别交给不同的处理人时,已经被转交的人要等待前一节点结束,特设此状态
     */
    public static final int CHECK_STATUS_WAITING_TO_DO = 9;
    
    /**
     * 无子流程myActionId
     */
    public static final long SUB_MYACTION_ID_NONE = 0;
    
    /**
     * 流程中选择的兼职部门
     */
    private String partDept;

    /**
     * 是否为变更
     */
    private boolean alter;
    /**
     * 变更发生的时间
     */
    private java.util.Date alterTime;


    public MyActionDb() {
        init();
    }

    public MyActionDb(long id) {
        init();
        this.id = id;
        load();
    }

    @Override
    public void initDB() {
        tableName = "flow_my_action";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new MyActionCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName + " (action_id, receive_date, user_name, action_status,id, proxy, flow_id, expire_date,dept_codes, priv_myaction_id, sub_my_action_id, is_checked, is_readed) values (?,?,?,?,?,?,?,?,?,?,?,?,0)";
        QUERY_SAVE = "update " + tableName + " set check_date=?,is_checked=?,proxy=?,expire_date=?,performance=?,checker=?,action_status=?,result=?,result_value=?,sub_my_action_id=?,performance_reason=?,performance_modifier=?, is_readed=?, read_date=?, part_dept=?,ip=?,os=?,browser=?,is_alter=?,alter_time=?,cluster_no=?,priv_myaction_id=? where id=?";
        QUERY_LOAD =
                "select receive_date,check_date,user_name,is_checked,action_status,action_id,proxy,flow_id,expire_date,dept_codes,priv_myaction_id,performance,checker,result,result_value,sub_my_action_id,performance_reason,performance_modifier,is_readed,read_date,part_dept,ip,os,browser,is_alter,alter_time,cluster_no from " + tableName + " where id=?";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LIST = "select id from " + tableName + " order by receive_date asc";
    }

    public String getSqlDoingOrReturn(HttpServletRequest request) {
        Privilege privilege = new Privilege();
        String op = StrUtil.getNullString(request.getParameter("op"));
        String typeCode = ParamUtil.get(request, "typeCode");
        String title = ParamUtil.get(request, "title");
        String starter = ParamUtil.get(request, "starter");
        String by = ParamUtil.get(request, "by");

        String fromDate = ParamUtil.get(request, "fromDate");
        String toDate = ParamUtil.get(request, "toDate");

        String myname = ParamUtil.get(request, "userName");
        if ("".equals(myname)) {
            myname = privilege.getUser(request);
        }

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "receive_date";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }

        String sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and (user_name=" + StrUtil.sqlstr(myname) + " or proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE;
        if ("search".equals(op)) {
            sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE;
            if (!"".equals(starter)) {
                sql = "select m.id from flow_my_action m, flow f, users u where m.flow_id=f.id and f.userName=u.name and f.status<>" + WorkflowDb.STATUS_NONE + " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE;
            }
            if (!"".equals(typeCode)) {
                sql += " and f.type_code=" + StrUtil.sqlstr(typeCode);
            }

            if ("title".equals(by)) {
                if (!"".equals(title)) {
                    sql += " and f.title like " + StrUtil.sqlstr("%" + title + "%");
                }
            } else if ("flowId".equals(by)) {
                if (!StrUtil.isNumeric(title)) {
                    String str = LocalUtil.LoadString(request, "res.flow.Flow", "mustNumber");
                    LogUtil.getLog(getClass()).error("getSqlDoing: " + str);
                } else {
                    sql += " and f.id=" + title;
                }
            }
            if (!"".equals(fromDate)) {
                sql += " and f.mydate>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
            }
            if (!"".equals(toDate)) {
                java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
                d = DateUtil.addDate(d, 1);
                String toDate2 = DateUtil.format(d, "yyyy-MM-dd");
                sql += " and f.mydate<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd");
            }
            if (!"".equals(starter)) {
                sql += " and u.realname like " + StrUtil.sqlstr("%" + starter + "%");
            }
        }

        sql += " and f.status<>" + WorkflowDb.STATUS_DELETED + " and f.status<>" + WorkflowDb.STATUS_DISCARDED;
        sql += " order by " + orderBy + " " + sort;
        return sql;
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new MyActionDb(pk.getLongValue());
    }

    /**
     * 取得流程的发起记录
     * @param flowId int
     * @return MyActionDb
     */
    public MyActionDb getFirstMyActionDbOfFlow(int flowId) {
        String sql = "select id from " + tableName + " where flow_id=? and priv_myaction_id=-1";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getFirstMyActionDbOfFlow:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }
    
    /**
     * 取得流程最后一个办理记录
     * @param flowId int
     * @return MyActionDb
     */
    public MyActionDb getLastMyActionDbOfFlow(int flowId) {
        String sql = "select id from " + tableName + " where flow_id=? order by receive_date desc";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getActionDoingByUser:" + e.getMessage());
        } finally {
            conn.close();
        }
        return null;
    }

    /**
     * 取得流程最后一个办理记录
     * @param flowId int
     * @return MyActionDb
     */
    public MyActionDb getLastMyActionDbDoneOfFlow(int flowId) {
        // 只需其中一人办理checker is not null and checker<>''，而这种情况下的被忽略的人员，流程步骤中显示的还是“已处理”
        String sql = "select id from flow_my_action where flow_id=? and is_checked<>" + MyActionDb.CHECK_STATUS_NOT + " and is_checked<>" + CHECK_STATUS_PASS + " and checker is not null and checker<>'' order by receive_date desc";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, flowId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getLastMyActionDbDoneOfFlow:" + e.getMessage());
        } finally {
            conn.close();
        }
        return null;
    }

    /**
     * 取得流程正在办理的记录
     * @param flowId int
     * @return MyActionDb
     */
    public Vector<MyActionDb> getMyActionDbDoingOfFlow(int flowId) {
        String sql = "select id from flow_my_action where flow_id=" + flowId + " and is_checked=" + MyActionDb.CHECK_STATUS_NOT + " order by receive_date desc";
        return list(sql);
    }

    /**
     * 当放弃流程时
     * @param flowId long
     */
    public void onDiscard(long flowId) {
        // 置当前正在办理的节点的状态为忽略状态
        String sql = "select id from " + tableName + " where flow_id=? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int aId = rs.getInt(1);
                    MyActionDb mad = getMyActionDb(aId);
                    mad.setCheckStatus(MyActionDb.CHECK_STATUS_PASS);
                    mad.save();
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("onDiscard:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * 返回，同时置其它当前正在办理的节点的状态为因返回而忽略
     */
    public void returnMyAction() {
        setCheckStatus(MyActionDb.CHECK_STATUS_RETURN);
        setResultValue(WorkflowActionDb.RESULT_VALUE_RETURN);
        setCheckDate(new java.util.Date());
        setChecker(SpringUtil.getUserName());
        save();

        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb((int)actionId);
        // 如果节点未设置为异步退回
        if (!wa.isXorReturn()) {
            // 置其它当前正在办理的节点的状态为因返回而忽略，除去节点action状态为被返回的节点
            // String sql = "select id from " + tableName + " where flow_id=? and id<>? and is_checked=0 and action_status<>" + WorkflowActionDb.STATE_RETURN;
            // 20200419改，以免当前节点为被退回状态时，该节点上有两个人处理，节点状态为已退回，其中一个人操作退回了，而另一个人没有被自动忽略
            // 如部门提供信息中，当街道退回给区，区再退回给部门时（区节点也是被退回状态），因没有对执行返回操作的节点作自动忽略，而使得区帐户1退回后，区帐户2仍处于待办状态
            // String sql = "select id from " + tableName + " where flow_id=? and id<>? and ((is_checked=0 and action_status<>" + WorkflowActionDb.STATE_RETURN + ") or action_id=" + actionId + ")";
            // 上句中如果当前节点actionId多次退回，需过滤掉之前已办理的待办记录，否则之前的这些记录也会被置为自动忽略
            String sql = "select id from " + tableName + " where flow_id=? and id<>? and ((is_checked=0 and action_status<>" + WorkflowActionDb.STATE_RETURN + ") or (is_checked=0 and action_id=" + actionId + "))";

            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, flowId);
                pstmt.setLong(2, id);
                rs = conn.executePreQuery();
                while (rs.next()) {
                    long aId = rs.getLong(1);
                    MyActionDb mad = getMyActionDb(aId);
                    mad.setCheckStatus(MyActionDb.CHECK_STATUS_PASS_BY_RETURN);
                    mad.save();
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error("returnMyAction:" + e.getMessage());
                LogUtil.getLog(getClass()).error(e);
            } finally {
                conn.close();
            }
        }
    }
    
    public Vector<MyActionDb> getFlowDoingWithoutAction(long flowId) {
        Vector<MyActionDb> v = new Vector<>();
        String sql = "select id from " + tableName + " where flow_id=? and is_checked=0 order by id desc";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int aId = rs.getInt(1);
                    v.addElement( getMyActionDb(aId) );
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getFlowDoingWithoutAction:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;    	
    }    
    
    public Vector<MyActionDb> getActionDoing(long actionId) {
        Vector<MyActionDb> v = new Vector<>();
        String sql = "select id from " + tableName + " where action_id=? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, actionId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int aId = rs.getInt(1);
                    v.addElement( getMyActionDb(aId) );
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getActionDoing:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;    	
    }
    
    /**
     * 取得所连接的节点中，正在待办的记录（变更时不允许存在这些记录）
     * @Description: 
     * @return
     */
    public Vector<MyActionDb> getMyActionDbOfToActionDoing() {
    	Vector<MyActionDb> v = new Vector<>();
    	WorkflowLinkDb wld = new WorkflowLinkDb();
    	WorkflowActionDb wa = new WorkflowActionDb();
    	wa = wa.getWorkflowActionDb((int)getActionId());
    	
    	JdbcTemplate jt = new JdbcTemplate();
    	jt.setAutoClose(false);
		String sql = "select id from " + tableName + " where action_id=? and is_checked=0";
    	try {
        	Vector<WorkflowLinkDb> links = wld.getToWorkflowLinks(wa);
            for (WorkflowLinkDb link : links) {
                wld = link;
                WorkflowActionDb toAction = wld.getToAction();
                ResultIterator ri = jt.executeQuery(sql, new Object[]{toAction.getId()});
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    int aId = rr.getInt(1);
                    v.addElement(getMyActionDb(aId));
                }
            }
    	}
    	catch (SQLException e) {
    		LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error("getMyActionDbOfToActionDoing:" + e.getMessage());    		
    	}
    	finally {
    		jt.close();
    	}
    	return v;
    }

    /**
     * 取得某节点上除myActionId外，正在处理中的MyAction，用于OnlyOne的处理
     * @return MyActionDb
     */
    public Vector<MyActionDb> getOthersOfActionDoing() {
        Vector<MyActionDb> v = new Vector<>();
        String sql = "select id from " + tableName + " where action_id=? and id<>? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, actionId);
            pstmt.setLong(2, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int aId = rs.getInt(1);
                    v.addElement( getMyActionDb(aId) );
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getMyActionDbOfActionDoing:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;
    }
    
	/**
	 * @Description: 取得某节点审核通过的人员数,用户XOfRole
	 * @return
	 */
	public int getActionAgreedCount() {
		String sql = "select count(id) from " + tableName
				+ " where action_id=? and is_checked=" + CHECK_STATUS_CHECKED;
		Conn conn = new Conn(connname);
		ResultSet rs;
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, actionId);
			rs = conn.executePreQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("getMyActionDbOfActionDoing:" + e.getMessage());
			LogUtil.getLog(getClass()).error(e);
		} finally {
		    conn.close();
		}
		return 0;
	}
	
	/**
	 * @Description: 取得某节点未审核的人员数,用户XOfRole
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Vector<MyActionDb> getActionUnchecked() {
		String sql = "select id from " + tableName
				+ " where action_id=? and is_checked=" + CHECK_STATUS_NOT;
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		Vector<MyActionDb> v = new Vector<>();
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, actionId);
			rs = conn.executePreQuery();
			while (rs.next()) {
				v.add(new MyActionDb(rs.getInt(1)));
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("getMyActionDbOfActionDoing:" + e.getMessage());
			LogUtil.getLog(getClass()).error(e);
		} finally {
		    conn.close();
		}
		return v;
	}
    
    /**
     * 能否撤回
     * @param myUserName
     * @return
     */
    public boolean canRecall(String myUserName) {
        return canRecall(myUserName, false);
    }

    /**
     * 判断能否撤回，如果后续节点已处理，则不能撤回
     * @param myUserName String
     * @param isLight boolean 是否为@流程
     * @return boolean
     */
    public boolean canRecall(String myUserName, boolean isLight) {
        if ((getUserName().equals(myUserName) || getProxyUserName().equals(myUserName)) && isChecked() && getSubMyActionId()==MyActionDb.SUB_MYACTION_ID_NONE && getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND_OVER && getCheckStatus()!=MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
            ;
        } else {
            return false;
        }

        // 检查当前节点的状态，如果是退回，则不能撤回
        if (getCheckStatus() == CHECK_STATUS_RETURN) {
            return false;
        }
        
        // 检查是否存在后续节点
        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb((int)getActionId());
        if (wa.getLinkToActions().size()==0) {
            return isLight;
        }
        
        // 判断后续节点是否已处理
        String sql = "select is_checked from " + tableName + " where priv_myaction_id=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {id});
            if (ri.size()>0) {
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    if (rr.getInt(1) != MyActionDb.CHECK_STATUS_NOT) {
                        return false;
                    }
                }
            }
            else {
                return false;
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }
        return true;
    }

    /**
     * 取得用户下一个需处理的某一节点（包含由其代理的情况），用于自由流程处理时，如：当前节点状态为已处理时，此时再次激活了本节点的情况
     * 子流程结束时，取得父流程对应节点的MyActionDb
     * @param wad WorkflowActionDb 正在处理的节点
     * @param userName String 处理者的用户名
     * @return MyActionDb
     */
    public MyActionDb getMyActionDbOfActionDoingByUser(WorkflowActionDb wad, String userName) {
        String sql = "select id from " + tableName + " where action_id=? and is_checked=0 and (user_name=? or proxy=?)";
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, wad.getId());
            pstmt.setString(2, userName);
            pstmt.setString(3, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getActionDoingByUser:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }

    /**
     * 取得某节点上已处理的MyActionDb，用于调度延迟job中获取上一节点MyActionDb，以便于notify，只取一个
     * @param wad WorkflowActionDb
     * @return MyActionDb
     */
    public MyActionDb getMyActionDbOfActionChecked(WorkflowActionDb wad) {
        String sql = "select id from " + tableName + " where action_id=? and is_checked=?";
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, wad.getId());
            pstmt.setInt(2, CHECK_STATUS_CHECKED);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getMyActionDbOfActionChecked:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }

    /**
     * 取出节点下的所有办理记录
     * @param actionId
     * @return
     */
    public List<MyActionDb> listByAction(int actionId) {
        List<MyActionDb> list = new ArrayList<>();
        String sql = "select id from " + tableName + " where action_id=?";
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, actionId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                list.add(getMyActionDb(rs.getInt(1)));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listMyActionDbOfAction:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return list;
    }

    /**
     * 取出节点下的所有状态为已处理的待办记录，用于退回处理
     * @param actionId
     * @return
     */
    public List<MyActionDb> listByActionRealyChecked(int actionId) {
        List<MyActionDb> list = new ArrayList<>();
        String sql;
        if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            sql = "select id,user_name,proxy from " + tableName + " where action_id=? and is_checked=? and checker is not null";
        } else {
            sql = "select id,user_name,proxy from " + tableName + " where action_id=? and is_checked=? and checker<>'' and checker is not null";
        }
        // action节点被退回后，再次提交后，再被退回，在其上的一个人会有多个待办记录，所以需过滤掉重复的记录
        Map<String, String> map = new HashMap<>();
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, actionId);
            pstmt.setInt(2, CHECK_STATUS_CHECKED);
            rs = conn.executePreQuery();
            while (rs.next()) {
                String uName = rs.getString(2);
                String proxy = rs.getString(3);
                String thatName = "";
                if (!StrUtil.isEmpty(proxy)) {
                    if (!map.containsKey(proxy)) {
                        thatName = proxy;
                    }
                }
                else {
                    if (!map.containsKey(uName)) {
                        thatName = uName;
                    }
                }
                if (!"".equals(thatName)) {
                    list.add(getMyActionDb(rs.getInt(1)));
                    map.put(thatName, uName);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listByActionChecked:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return list;
    }
	
    /**
     * 取得用户第一个已处理的记录，用于从模块列表上变更流程记录
     * @param flowId long
     * @param userName String
     * @return MyActionDb
     */
    public MyActionDb getMyActionDbFirstChecked(long flowId, String userName) {
        String sql = "select id from " + tableName + " where flow_id=? and is_checked=" + CHECK_STATUS_CHECKED + " and (user_name=? or proxy=?) order by id asc";
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            pstmt.setString(2, userName);
            pstmt.setString(3, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }
    
    /**
     * 取得本流程中下一个由userName处理的节点，用于连续处理时
     * @param flowId long
     * @param userName String
     * @return MyActionDb
     */
    public MyActionDb getMyActionDbOfFlowDoingByUser(long flowId, String userName) {
        String sql = "select id from " + tableName + " where flow_id=? and is_checked=0 and (user_name=? or proxy=?)";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            pstmt.setString(2, userName);
            pstmt.setString(3, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }

    /**
     * 取得某用户最后一次处理某流程时的myaction，用于rend report时，根据最后一次的WorkflowActionDb，隐藏相应字段
     * @param flowId long
     * @param userName String
     * @return MyActionDb
     */
    public MyActionDb getMyActionDbOfFlow(long flowId, String userName) {
        String sql = "select id from " + tableName + " where flow_id=? and (user_name=? or proxy=?) order by receive_date desc";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            pstmt.setString(2, userName);
            pstmt.setString(3, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                int aId = rs.getInt(1);
                return getMyActionDb(aId);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return null;
    }

    /**
     * 获取priv_myaciton_id为myActionId的所有待办记录，如果该记录尚未办理，则撤回，如果已办理，则不能撤回
     * @param myActionId long
     * @return int 被撤回的节点数
     */
    public int recallMyActionsByPrivMyAction(long myActionId) {
        WorkflowActionDb action = new WorkflowActionDb();
        int k = 0;
        String sql = "select id from " + tableName + " where priv_myaction_id=? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, myActionId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                // 删除action、连接线
                MyActionDb mad = getMyActionDb(rs.getLong(1));
                long nextActionId = mad.getActionId();
                WorkflowActionDb nextAction = action.getWorkflowActionDb((int)nextActionId);
                // WorkflowLinkDb wld = new WorkflowLinkDb();
                // wld = wld.getWorkflowLinkDbForward(privAction, nextAction);
                // wld.del();
                nextAction.setStatus(WorkflowActionDb.STATE_NOTDO);
                nextAction.save();

                PlanDb pd = new PlanDb();
                pd = pd.getPlanDb(mad.getUserName(), PlanDb.ACTION_TYPE_FLOW, String.valueOf(mad.getId()));
                if (pd!=null) {
                    pd.del();
                }

                mad.del();

                k++;
            }
        } catch (SQLException | ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return k;
    }

    /**
     * 撤回自由流程
     * @param myActionId long
     * @return int
     */
    public int recallMyActionsByPrivMyActionFree(long myActionId) {
        MyActionDb privMyActoin = getMyActionDb(myActionId);
        WorkflowActionDb action = new WorkflowActionDb();
        WorkflowActionDb privAction = action.getWorkflowActionDb((int)privMyActoin.getActionId());

        int k = 0;
        String sql = "select id from " + tableName + " where priv_myaction_id=? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, myActionId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    // 删除action、连接线
                    MyActionDb mad = getMyActionDb(rs.getLong(1));
                    long nextActionId = mad.getActionId();
                    WorkflowActionDb nextAction = action.getWorkflowActionDb((int)nextActionId);
                    WorkflowLinkDb wld = new WorkflowLinkDb();
                    wld = wld.getWorkflowLinkDbForward(privAction, nextAction);
                    wld.del();
                    nextAction.del();
                    
                    PlanDb pd = new PlanDb();
                    pd = pd.getPlanDb(mad.getUserName(), PlanDb.ACTION_TYPE_FLOW, String.valueOf(mad.getId()));
                    if (pd!=null) {
                    	pd.del();
                    }
                    
                    mad.del();

                    k++;
                }
            }
        } catch (SQLException | ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return k;
    }

    public long getActionAccessByUserCount(String userName) {
        long count = 0;
        String sql = "select count(*) from " + tableName + " where user_name=? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                // "select receive_date,check_date from " + tableName + " where action_id=?";
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return count;
    }

    /**
     * 检查是否所有的某个action节点上的用户是否都已经处理过了
     * @return boolean
     */
    public boolean isAllUserOfActionChecked() {
        String sql = "select user_name from " + tableName + " where action_id=? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, actionId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                LogUtil.getLog(getClass()).info("isAllUserOfActionChecked:" + rs.getString(1) + " action_id=" + actionId);
                return false;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return true;
    }

    /**
     * 用户是否加入了流程
     * @param flowId long
     * @param userName String
     * @return boolean
     */
    public boolean isUserAttendFlow(long flowId, String userName) {
        String sql = "select flow_id from " + tableName + " where flow_id=? and (user_name=? or proxy=?)";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            pstmt.setString(2, userName);
            pstmt.setString(3, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                // "select receive_date,check_date from " + tableName + " where action_id=?";
                LogUtil.getLog(getClass()).info("isUserAttendedFlow:" + rs.getString(1) + " action_id=" + actionId);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            return false;
        } finally {
            conn.close();
        }
        return false;
    }

    @Override
    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
         String url = du.getPageList();
         return url;
     }

     @Override
     public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
         Privilege privilege = new Privilege();
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
         String url = du.getPageShow();
         Config cfg = Config.getInstance();
         boolean isFlowProxy = cfg.getBooleanProperty("isFlowProxy");
         
         WorkflowPredefineDb wpd = new WorkflowPredefineDb();
         String myname = privilege.getUser(request);
         String sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<>" + WorkflowDb.STATUS_DELETED + " and f.status<>" + WorkflowDb.STATUS_DISCARDED + " and (user_name=" + StrUtil.sqlstr(myname);
         if (isFlowProxy) {
             sql += " or proxy=" + StrUtil.sqlstr(myname);
         }
         sql += ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE + " order by receive_date desc";

         String flowTypeCode = uds.getMetaData();
         if (!"".equals(flowTypeCode)) {
             sql = "select m.id from flow_my_action m, flow f where f.type_code=" + StrUtil.sqlstr(flowTypeCode) + " and m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<>" + WorkflowDb.STATUS_DELETED + " and f.status<>" + WorkflowDb.STATUS_DISCARDED + " and (user_name=" + StrUtil.sqlstr(myname);
             if (isFlowProxy) {
                 sql += " or proxy=" + StrUtil.sqlstr(myname);
             }
             sql += ") and (is_checked=0 or is_checked=2) and sub_my_action_id=" + MyActionDb.SUB_MYACTION_ID_NONE + " order by receive_date desc";
         }
         String str = "";
         try {
             ListResult wflr = listResult(sql, 1, uds.getCount());
             Iterator wfir = wflr.getResult().iterator();
             WorkflowDb wfd = null;
             WorkflowMgr wfm = new WorkflowMgr();
             Directory dir = new Directory();
             if(wfir.hasNext()){
            	 str += "<table class='article_table'>";
                 while (wfir.hasNext()) {
                	 MyActionDb mad = (MyActionDb) wfir.next();
                     // WorkflowActionDb wad = new WorkflowActionDb();
                	 // wad = wad.getWorkflowActionDb((int) mad.getActionId());
                	 wfd = wfm.getWorkflowDb((int) mad.getFlowId());
                     Leaf lf = dir.getLeaf(wfd.getTypeCode());
                     if (lf==null) {
                         continue;
                     }
                     if (lf.getType()==Leaf.TYPE_FREE) {
                         wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
                         if (wpd.isLight()) {
                        	 url = "flow_dispose_light.jsp?myActionId=";                    	 
                         }
                         else {
                        	 url = "flowDisposeFree.do?myActionId=";
                         }
                     }
                     else {
                    	 url = "flowDispose.do?myActionId=";
                     }

					String t = wpd.isLight() ? MyActionMgr.renderTitle(request,
							wfd) : StrUtil.toHtml(StrUtil.getLeft(wfd
							.getTitle(), uds.getWordCount()));

    	           	  String cls = "class=\"readed\"";
    	        	  if (!mad.isReaded()) {
    	        	  	cls = "class=\"unreaded\"";
    	        	  }
            	  
                     str += "<tr><td class='article_content'><a " + cls + " title='" + wfd.getTitle() + "' href='" + url + mad.getId() + "'>" +
                             t + "</a></td><td class='article_time'>[" +
                             DateUtil.format(wfd.getMydate(), "yyyy-MM-dd") +
                             "]</td></tr>";
                 }
                 str += "</table>";
             }else{
            	 str = "<div class='no_content'><img title='暂无待办流程' src='images/desktop/no_content.jpg'></div>";
             }
         } catch (ErrMsgException e) {
             LogUtil.getLog(getClass()).error(e);
         }
         return str;
    }

    public MyActionDb getMyActionDb(long id) {
        return (MyActionDb)getObjectDb(id);
    }

    public void setActionId(long actionId) {
        this.actionId = actionId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setReceiveDate(java.util.Date receiveDate) {
        this.receiveDate = receiveDate;
    }

    public void setCheckDate(java.util.Date checkDate) {
        this.checkDate = checkDate;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checked) {
            this.checkStatus = CHECK_STATUS_CHECKED;
        } else {
            this.checkStatus = CHECK_STATUS_NOT;
        }
    }

    public void setActionStatus(int actionStatus) {
        this.actionStatus = actionStatus;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    public void setFlowId(long flowId) {
        this.flowId = flowId;
    }

    public void setExpireDate(java.util.Date expireDate) {
        this.expireDate = expireDate;
    }

    public void setDeptCodes(String deptCodes) {
        this.deptCodes = deptCodes;
    }

    public void setPrivMyActionId(long privMyActionId) {
        this.privMyActionId = privMyActionId;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public void setChecker(String checker) {
        this.checker = checker;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public void setResultValue(int resultValue) {
        this.resultValue = resultValue;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setSubMyActionId(long subMyActionId) {
        this.subMyActionId = subMyActionId;
    }

    public void setPerformanceReason(String performanceReason) {
        this.performanceReason = performanceReason;
    }

    public void setPerformanceModify(String performanceModifier) {
        this.performanceModifier = performanceModifier;
    }

    public long getFlowId() {
        return flowId;
    }

    public long getActionId() {
        return actionId;
    }

    public java.util.Date getReceiveDate() {
        return receiveDate;
    }

    public java.util.Date getCheckDate() {
        return checkDate;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isChecked() {
        return checked;
    }

    public int getActionStatus() {
        return actionStatus;
    }

    public long getId() {
        return id;
    }

    public String getProxyUserName() {
        return proxyUserName;
    }

    public java.util.Date getExpireDate() {
        return expireDate;
    }

    public String getDeptCodes() {
        return deptCodes;
    }

    public long getPrivMyActionId() {
        return privMyActionId;
    }

    public double getPerformance() {
        return performance;
    }

    public String getChecker() {
        return checker;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public int getResultValue() {
        return resultValue;
    }

    public String getResult() {
        return result;
    }

    public long getSubMyActionId() {
        return subMyActionId;
    }

    public String getPerformanceReason() {
        return performanceReason;
    }

    public String getPerformanceModifier() {
        return performanceModifier;
    }

    /**
     * 取得超期且需要自动转交的待办记录
     * @return Vector
     */
    public Vector listExpiredAndNeedAutoDeliver() {
        // 取得三天内的过期待办记录
        java.util.Date d = DateUtil.addDate(new java.util.Date(), -3);
        String sql = "select id from " + tableName + " where is_checked=0 and expire_date>?";
        Vector v = new Vector();
        WorkflowActionDb wad = new WorkflowActionDb();
        WorkflowLinkDb wld = new WorkflowLinkDb();
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {d});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                MyActionDb mad = getMyActionDb(rr.getLong(1));
                // 检查mad连接线上超期处理设置
                MyActionDb privMad = getMyActionDb(mad.getPrivMyActionId());
                WorkflowActionDb wa1 = wad.getWorkflowActionDb((int)privMad.getActionId());
                WorkflowActionDb wa2 = wad.getWorkflowActionDb((int)mad.getActionId());
                wld = wld.getWorkflowLinkDbForward(wa1, wa2);
                if (!wld.getExpireAction().equals("")) {
                    v.addElement(getMyActionDb(rr.getLong(1)));
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return v;
    }

    /**
     * 取得将要超期的待办记录
     * @return Vector
     */
    public Vector<MyActionDb> listWillExpire() {
        Config cfg = new Config();
        // 在多少天内的未处理的流程动作快到期时发出提醒
        int flowActionExpireDay = StrUtil.toInt(cfg.get("flowActionExpireDay"), 30); // 30天
        // 在流程动作到期前多少分钟开始提醒
        int flowActionExpireRemindBeforeMinute = StrUtil.toInt(cfg.get("flowActionExpireRemindBeforeMinute"), 60); // 60分钟
        // String sql = "select id from " + tableName + " is_checked=0 and (DATE_ADD(NOW(),INTERVAL " + flowActionExpireRemindBeforeMinute + " MINUTE)>expire_date) and (TO_DAYS(NOW())-TO_DAYS(receive_date)<=" + flowActionExpireDay + ")";
        // return list(sql);

        String sql = "select id from " + tableName + " where is_checked=0 and expire_date<=? and expire_date>? and receive_date>=?";
        java.util.Date d1 = DateUtil.addMinuteDate(new java.util.Date(), flowActionExpireRemindBeforeMinute);
        java.util.Date d2 = DateUtil.addDate(new java.util.Date(), -flowActionExpireDay);

        Vector<MyActionDb> v = new Vector<>();
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {d1, new java.util.Date(), d2});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                v.addElement(getMyActionDb(rr.getLong(1)));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return v;
    }

    /**
     * 从挂起中恢复
     * @return boolean
     */
    public long resume(String checker) {
        // 置原来的状态为挂起结束
        setCheckStatus(MyActionDb.CHECK_STATUS_SUSPEND_OVER);
        setChecker(checker);
        boolean re = save();
        long newId = -1;
        if (re) {
            // 重新计算到期时间
            WorkflowLinkDb wld = new WorkflowLinkDb();

            MyActionDb privmad = new MyActionDb();
            privmad = privmad.getMyActionDb(privMyActionId);
            WorkflowActionDb privAction = new WorkflowActionDb();
            privAction = privAction.getWorkflowActionDb((int)privmad.getActionId());

            WorkflowActionDb actionToDo = new WorkflowActionDb();
            actionToDo = actionToDo.getWorkflowActionDb((int)actionId);

            wld = wld.getWorkflowLinkDbForward(privAction, actionToDo);
            // 如果wld为空，则说明中间有跳过的情况
            // 取得其之前有跳过的情况
            if (wld==null) {
                // @task:取actionToDo之前的一条link来计算，这样会带来一个问题，如果actionToDb为聚合节点，之前有数条分支怎么处理
                // 不过这里应该影响不大，因为出现跳至聚合节点的情况本来就少，而且如果真有数条分支了，分条分支线的到期时间应该是差不多的
                Iterator ir = actionToDo.getLinkFromActions().iterator();
                if (ir.hasNext()) {
                    WorkflowActionDb pAction = (WorkflowActionDb)ir.next();
                    wld = new WorkflowLinkDb();
                    wld = wld.getWorkflowLinkDbForward(pAction, actionToDo);
                }
            }

            // 计算新的到期时间
            java.util.Date expireDate = null;
            if (wld != null) {
            	expireDate = wld.calulateExpireDate();
            }

            Conn conn = new Conn(connname);
            try {
                newId = SequenceManager.nextID(SequenceManager.OA_WORKFLOW_MYACTION);
                PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
                pstmt.setLong(1, actionId);
                pstmt.setTimestamp(2, new Timestamp((new java.util.Date()).getTime()));
                pstmt.setString(3, userName);
                // 置到达时状态为挂起结束
                pstmt.setInt(4, WorkflowActionDb.STATE_SUSPEND_OVER);
                pstmt.setLong(5, newId);
                pstmt.setString(6, proxyUserName);
                pstmt.setLong(7, flowId);
                // 置新的到期时间
                if (expireDate != null) {
                    pstmt.setTimestamp(8, new Timestamp(expireDate.getTime()));
                } else {
                    pstmt.setTimestamp(8, null);
                }
                pstmt.setString(9, deptCodes);
                pstmt.setLong(10, privMyActionId);
                pstmt.setLong(11, subMyActionId);
                pstmt.setLong(12, checkStatus);

                re = conn.executePreUpdate() == 1 ? true : false;
                if (re) {
                    MyActionCache rc = new MyActionCache(this);
                    rc.refreshCreate();
                }

            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
            } finally {
                conn.close();
            }
        }
        return newId;
    }

    @Override
    public boolean create() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = SequenceManager.nextID(SequenceManager.OA_WORKFLOW_MYACTION);
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setLong(1, actionId);
            pstmt.setTimestamp(2, new Timestamp((new java.util.Date()).getTime()));
            pstmt.setString(3, userName);
            pstmt.setInt(4, actionStatus);
            pstmt.setLong(5, id);
            pstmt.setString(6, proxyUserName);
            pstmt.setLong(7, flowId);
            if (expireDate!=null) {
                pstmt.setTimestamp(8, new Timestamp(expireDate.getTime()));
            } else {
                pstmt.setTimestamp(8, null);
            }
            pstmt.setString(9, deptCodes);
            pstmt.setLong(10, privMyActionId);
            pstmt.setLong(11, subMyActionId);
            pstmt.setInt(12, checkStatus);
            re = conn.executePreUpdate() == 1;
            if (re) {
                MyActionCache rc = new MyActionCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            if (checkDate == null) {
                pstmt.setDate(1, null);
            }
            else {
                pstmt.setTimestamp(1, new Timestamp(checkDate.getTime()));
            }
            pstmt.setInt(2, checkStatus);
            LogUtil.getLog(getClass()).info("save mad id=" + id + " checkStatus=" + checkStatus + " resultValue=" + resultValue);
            pstmt.setString(3, proxyUserName);
            if (expireDate==null) {
                pstmt.setTimestamp(4, null);
            } else {
                pstmt.setTimestamp(4, new Timestamp(expireDate.getTime()));
            }
            pstmt.setDouble(5, performance);
            pstmt.setString(6, checker);
            pstmt.setInt(7, actionStatus);
            pstmt.setString(8, result);
            pstmt.setInt(9, resultValue);
            pstmt.setLong(10, subMyActionId);
            pstmt.setString(11, performanceReason);
            pstmt.setString(12, performanceModifier);
            pstmt.setInt(13, readed?1:0);
            if (readDate==null) {
                pstmt.setTimestamp(14, null);
            } else {
                pstmt.setTimestamp(14, new Timestamp(readDate.getTime()));
            }
            pstmt.setString(15, partDept);
            pstmt.setString(16, ip);
            pstmt.setString(17, os);
            pstmt.setString(18, browser);
            pstmt.setInt(19, alter?1:0);
            if (alterTime==null) {
                pstmt.setTimestamp(20, null);
            }
            else {
                pstmt.setTimestamp(20, new Timestamp(alterTime.getTime()));
            }
            pstmt.setString(21, Global.getInstance().getClusterNo());
            pstmt.setLong(22, privMyActionId);
            pstmt.setLong(23, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                MyActionCache rc = new MyActionCache(this);
                rc.refreshList();
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    // "select receive_date,check_date from " + tableName + " where action_id=?";
                    receiveDate = rs.getTimestamp(1);
                    checkDate = rs.getTimestamp(2);
                    userName = rs.getString(3);
                    checkStatus = rs.getInt(4);
                    checked = checkStatus==CHECK_STATUS_CHECKED || checkStatus==CHECK_STATUS_TRANSFER || checkStatus==CHECK_STATUS_RETURN || checkStatus==CHECK_STATUS_HANDOVER || checkStatus==CHECK_STATUS_SUSPEND_OVER || checkStatus==CHECK_STATUS_PASS_BY_RETURN;
                    actionStatus = rs.getInt(5);
                    actionId = rs.getLong(6);
                    proxyUserName = StrUtil.getNullString(rs.getString(7));
                    flowId = rs.getLong(8);
                    expireDate = rs.getTimestamp(9);
                    deptCodes = StrUtil.getNullStr(rs.getString(10));
                    privMyActionId = rs.getLong(11);
                    performance = rs.getDouble(12);
                    checker = StrUtil.getNullStr(rs.getString(13));
                    result = StrUtil.getNullStr(rs.getString(14));
                    resultValue = rs.getInt(15);
                    subMyActionId = rs.getLong(16);
                    performanceReason = StrUtil.getNullStr(rs.getString(17));
                    performanceModifier = StrUtil.getNullStr(rs.getString(18));
                    readed = rs.getInt(19)==1;
                    readDate = rs.getTimestamp(20);
                    partDept = StrUtil.getNullStr(rs.getString(21));
                    ip = StrUtil.getNullStr(rs.getString(22));
                    os = StrUtil.getNullStr(rs.getString(23));
                    browser = StrUtil.getNullStr(rs.getString(24));
                    alter= rs.getInt(25)==1;
                    alterTime = rs.getTimestamp(26);
                    clusterNo = StrUtil.getNullStr(rs.getString(27));
                    loaded = true;
                    primaryKey.setValue(actionId);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * 当流程被手工结束时的处理，拒绝
     * @param flowId long
     */
    public void onWorkflowManualFinished(long flowId, String checkUserName, boolean isFinishAgree) throws ErrMsgException {
        Config cfg = new Config();
        if ("true".equals(cfg.get("flowExpireRelateOACalendar"))) {
            String flowExpireUnit = cfg.get("flowExpireUnit");
            OACalendarDb oad = new OACalendarDb();
            if ("day".equals(flowExpireUnit)) {
                double d = oad.getWorkDayCountFromDb(getReceiveDate(), new java.util.Date());
                double d2 = oad.getWorkDayCount(getReceiveDate(), getExpireDate());
                double performance = 0;

                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!"".equals(formula)) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else {
                        performance = d / d2;
                    }
                }
                setPerformance(performance);
            }
            else {
                // 完成所用时间
                double d = oad.getWorkHourCount(getReceiveDate(), new java.util.Date());
                // 流程到期时间
                double d2 = oad.getWorkHourCount(getReceiveDate(), getExpireDate());

                LogUtil.getLog(getClass()).info("onFlowManualFinished:" + "d=" + d + "d2=" + d2);

                double performance = 0;
                if (d2!=0) {
                    String formula = StrUtil.getNullStr(cfg.get("flowPerformanceFormula"));
                    if (!"".equals(formula)) {
                        formula = formula.replaceAll("a", NumberUtil.round(d, 2));
                        formula = formula.replaceAll("b", NumberUtil.round(d2, 2));
                        FormulaCalculator fc = new FormulaCalculator(formula);
                        performance = fc.getResult();
                    }
                    else {
                        performance = d / d2;
                    }
                }
                setPerformance(performance);
            }
        }
        setChecker(checkUserName);
        setChecked(true);
        if (!isFinishAgree) {
			setResultValue(WorkflowActionDb.RESULT_VALUE_DISAGGREE);
        }
        else {
			setResultValue(WorkflowActionDb.RESULT_VALUE_AGGREE);
        }
        save();

        onChecked();

        String sql = "select id from " + tableName + " where flow_id=? and (is_checked=0 or is_checked=" + CHECK_STATUS_SUSPEND + ")";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    MyActionDb mad = getMyActionDb(rs.getLong(1));
                    // 置待处理动作通知中的处理时间及将其设置为已处理
                    mad.setCheckDate(new java.util.Date());
                    mad.setChecked(true);
                    mad.setChecker(UserDb.SYSTEM);
                    // 因拒绝而自动处理
                    if (!isFinishAgree) {
                        mad.setResult(LocalUtil.LoadString(null, "res.flow.Flow", "systemAutoAccessedWhenDisposeDecline"));
                    }
                    mad.save();

                    mad.onChecked();
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * 删除流程中的所有myaction
     * @param flowId long
     */
    public void delMyActionOfFlow(long flowId) {
        String sql = "select id from " + tableName + " where flow_id=?";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                MyActionDb mad = getMyActionDb(rs.getLong(1));
                mad.del();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * 删除加签
     * @param actionId
     */
    public void delByPlus(int actionId) {
        String sql = "select id from " + tableName + " where action_id=? and action_status=" + WorkflowActionDb.STATE_PLUS;
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, flowId);
            rs = conn.executePreQuery();
            while (rs.next()) {
                MyActionDb mad = getMyActionDb(rs.getLong(1));
                mad.del();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delByPlus:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    @Override
    public boolean del() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setLong(1, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                MyActionCache rc = new MyActionCache(this);
                rc.refreshDel(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    /**
     * 清除用户的某个代理
     * @param userName String
     * @param proxy String
     */
    public void clearProxyOfUser(String userName, String proxy) {
        String sql = "select id from " + tableName + " where user_name=? and is_checked=0 and proxy=?";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            pstmt.setString(2, proxy);
            rs = conn.executePreQuery();
            while (rs.next()) {
                MyActionDb mad = getMyActionDb(rs.getLong(1));
                mad.setProxyUserName("");
                mad.save();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * 置默认代理
     * @param userName String
     * @param proxyUserName String
     * @deprecated
     */
    public void putProxyOfUser(String userName, String proxyUserName) {
        String sql = "select id from " + tableName + " where user_name=? and is_checked=0";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    MyActionDb mad = getMyActionDb(rs.getLong(1));
                    mad.setProxyUserName(proxyUserName);
                    mad.save();
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * 判断是否存在相应的MyActionDb记录，通过synchronized防止多线程时，并发处理产生多个流程Notify的情况
     * @param userName String
     * @param privMyActionId long
     * @return boolean
     */
    public synchronized static boolean isNotifyExist(long flowId, String userName, long privMyActionId) {
        String sql = "select id from flow_my_action where flow_id=? and user_name=? and priv_myaction_id=? and is_checked=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{flowId, userName, privMyActionId, CHECK_STATUS_NOT});
            if (ri.hasNext()) {
                return true;
            }
        } catch (SQLException ex) {
            LogUtil.getLog(MyActionDb.class).error(ex);
        }
        return false;
    }

    /**
     * 取得节点上用户的待办记录
     * @param flowId
     * @param userName
     * @param actionId
     * @return
     */
    public synchronized static MyActionDb getMyActionDbByAction(long flowId, String userName, long actionId) {
        String sql = "select id from flow_my_action where flow_id=? and user_name=? and action_id=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{flowId, userName, actionId});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                long myActionId = rr.getLong(1);
                MyActionDb mad = new MyActionDb();
                return mad.getMyActionDb(myActionId);
            }
        } catch (SQLException ex) {
            LogUtil.getLog(MyActionDb.class).error(ex);
        }
        return null;
    }

    public synchronized static boolean isNotifyNotCheckedExistOnAction(long flowId, String userName, long actionId) {
        String sql = "select id from flow_my_action where flow_id=? and user_name=? and action_id=? and is_checked=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{flowId, userName, actionId, CHECK_STATUS_NOT});
            if (ri.hasNext()) {
                return true;
            }
        } catch (SQLException ex) {
            LogUtil.getLog(MyActionDb.class).error(ex);
        }
        return false;
    }
    
    /*
     * 判断是否存在正在等待上一节点处理完毕的待办记录
     */
    public synchronized static int isActionWaitExist(long actionId, String userName) {
    	String sql = "select id from flow_my_action where action_id=? and user_name=? and is_checked=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{actionId, userName, CHECK_STATUS_WAITING_TO_DO});
            if (ri.hasNext()) {
            	ResultRecord rr = (ResultRecord) ri.next();
                return rr.getInt(1);
            }
        } catch (SQLException ex) {
            LogUtil.getLog(MyActionDb.class).error(ex);
        }
        return 0;
    }

    /**
     * 取得加签待办通知
     * @param actionId int
     * @return Vector
     */
    public Vector getPlusMyActionDbs(long actionId) {
        String sql =
                "select id from flow_my_action where action_id=" + actionId + " and action_status=" +
                WorkflowActionDb.STATE_PLUS + " order by id asc";
        return list(sql);
    }

    public String getCheckStatusName() {
        return getCheckStatusName(checkStatus);
    }

    public static String getCheckStatusName(int checkStatus) {
        String r;
        WorkflowConfig wfcfg = WorkflowConfig.getInstance();
        switch(checkStatus) {
          case CHECK_STATUS_NOT: r = wfcfg.getProperty("CHECK_STATUS_NOT"); break;
          case CHECK_STATUS_CHECKED: r = wfcfg.getProperty("CHECK_STATUS_CHECKED"); break;
          case CHECK_STATUS_SUSPEND: r = wfcfg.getProperty("CHECK_STATUS_SUSPEND"); break;
          case CHECK_STATUS_TRANSFER: r = wfcfg.getProperty("CHECK_STATUS_TRANSFER"); break;
          case CHECK_STATUS_RETURN: r = wfcfg.getProperty("CHECK_STATUS_RETURN"); break;
          case CHECK_STATUS_HANDOVER: r = wfcfg.getProperty("CHECK_STATUS_HANDOVER"); break;
          case CHECK_STATUS_PASS: r = wfcfg.getProperty("CHECK_STATUS_PASS"); break;
          case CHECK_STATUS_SUSPEND_OVER: r = wfcfg.getProperty("CHECK_STATUS_SUSPEND_OVER"); break;
          case CHECK_STATUS_PASS_BY_RETURN: r = wfcfg.getProperty("CHECK_STATUS_PASS_BY_RETURN"); break;
          case CHECK_STATUS_WAITING_TO_DO: r = wfcfg.getProperty("CHECK_STATUS_WAITING_TO_DO"); break;
          default: r = wfcfg.getProperty("CHECK_STATUS_UNKNOWN");
        }
        return r;
    }

    /**
     * 取得CSS显示样式
     * @param checkStatus int
     * @return String
     */
    public static String getCheckStatusClass(int checkStatus) {
        String r;
        switch(checkStatus) {
          case CHECK_STATUS_NOT: r = "CHECK_STATUS_NOT"; break;
          case CHECK_STATUS_CHECKED: r = "CHECK_STATUS_CHECKED"; break;
          case CHECK_STATUS_SUSPEND: r = "CHECK_STATUS_SUSPEND"; break;
          case CHECK_STATUS_TRANSFER: r = "CHECK_STATUS_TRANSFER"; break;
          case CHECK_STATUS_RETURN: r = "CHECK_STATUS_RETURN"; break;
          case CHECK_STATUS_HANDOVER: r = "CHECK_STATUS_HANDOVER"; break;
          case CHECK_STATUS_PASS: r = "CHECK_STATUS_PASS"; break;
          case CHECK_STATUS_PASS_BY_RETURN: r = "CHECK_STATUS_PASS_BY_RETURN"; break;
          default: r = "CHECK_STATUS_NOT";
        }
        return r;
    }

    /**
     * 判断用户在处理actionId对应的节点前有没有处理过流程中的其它节点，如果返回了则不算在内
     * @param userName String
     * @param flowId long
     * @return boolean
     */
    public static boolean isUserAccessedBefore(String userName, long actionId, long flowId) {
    	// fgf 20170315通过action_id排除掉可能是返回后再处理同一节点的情况
        String sql = "select id from flow_my_action where flow_id=" + flowId + " and action_id<>" + actionId + " and user_name=" + StrUtil.sqlstr(userName) + " and is_checked<>" + CHECK_STATUS_RETURN + " order by receive_date desc";
        MyActionDb mad = new MyActionDb();
        Vector v = mad.list(sql);
        return v.size() > 0;
    }

    public void onChecked() {
        // 结束日程
        try {
            PlanDb pd = new PlanDb();
            pd = pd.getPlanDb(userName, PlanDb.ACTION_TYPE_FLOW, String.valueOf(id));
            // 判断是为了兼容旧版的代码
            if (pd!=null) {
                pd.setClosed(true);
                pd.save();
            }
        } catch (ErrMsgException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }

    }

    /**
     * 取得某段时间内某节点上的平均绩效
     * @param internalName String action动作上的内部名称
     * @param start Date
     * @param end Date
     * @return double
     */
    public double getActionAvgPerformance(String internalName, java.util.Date start, java.util.Date end) {
        double p = 0.0;

        String sql = "select sum(ma.performance) from " + this.getTableName() + " ma, flow_action a where a.status=" + WorkflowActionDb.STATE_FINISHED + " and ma.action_id=a.id and a.internal_name=? and ma.receive_date>=? and ma.receive_date<?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {internalName, start, end});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                p = rr.getDouble(1);
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }

        int c = 0;
        sql = "select count(ma.performance) from " + this.getTableName() + " ma, flow_action a where a.status=" + WorkflowActionDb.STATE_FINISHED + " and ma.action_id=a.id and a.internal_name=? and ma.receive_date>=? and ma.receive_date<?";
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {internalName, start, end});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                c = rr.getInt(1);
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }

        if (c!=0) {
            p = p/c;
        }

        return p;
    }

    public void setReaded(boolean readed) {
		this.readed = readed;
	}

	public boolean isReaded() {
		return readed;
	}
	
	/**
	 * 取得父流程中对应的MyActionDb
	 * @return
	 */
	public MyActionDb getParentMyActionDb() {
		String sql = "select id from flow_my_action where sub_my_action_id=" + id;
		Iterator ir = list(sql).iterator();
		if (ir.hasNext()) {
			return (MyActionDb)ir.next();
		}
		return null;
	}

	/**
	 * 取得安排任务的json树形结构
	 * @param request
	 * @param flowId
	 * @param len
	 * @return
	 */
    public static JSONArray getTaskTree(HttpServletRequest request, int flowId, int len) {
        MyActionDb mad = new MyActionDb();
    	JSONArray jsonAry = new JSONArray();
        UserDb user = new UserDb();
    	String sql = "select id from " + mad.getTableName() + " where flow_id=" + flowId + " order by id asc";
    	Vector v = mad.list(sql);
    	Iterator ir = v.iterator();
    	while (ir.hasNext()) {
    		mad = (MyActionDb)ir.next();
    		
    		user = user.getUserDb(mad.getUserName());
    		
    		JSONObject json = new JSONObject();
            try {
                json.put("id", String.valueOf(mad.getId()));
                String parent = mad.getPrivMyActionId()==-1?"#":String.valueOf(mad.getPrivMyActionId());
                json.put("parent", parent);
                String result = MyActionMgr.renderResult(request, mad);
                // 不能带有表情，因为显示时会有折行，页面看起来混乱
                boolean isImg = false;
                json.put("text", user.getRealName() + "：" + StrUtil.getAbstract(request, result, len, " ", isImg) + "&nbsp;" + DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm"));
                jsonAry.put(json);
            } catch (JSONException ex2) {
                ex2.printStackTrace();
            }    		
    	}
    	
    	return jsonAry;
    }	
    
    public static JSONArray getTaskTreeSorted(int flowId) throws JSONException {
    	MyActionDb mad = new MyActionDb();
    	JSONArray jsonAry = new JSONArray();
        UserDb user = new UserDb();
    	String sql = "select id from " + mad.getTableName() + " where flow_id=" + flowId + " order by id asc";
    	Vector v = mad.list(sql);
        for (Object o : v) {
            mad = (MyActionDb) o;

            user = user.getUserDb(mad.getUserName());

            JSONObject json = new JSONObject();
            try {
                json.put("id", String.valueOf(mad.getId()));
                String parent = mad.getPrivMyActionId() == -1 ? "#" : String.valueOf(mad.getPrivMyActionId());
                json.put("parent", parent);
                /*
                String result = MyActionMgr.renderResult(request, mad);
                // 不能带有表情，因为显示时会有折行，页面看起来混乱
                boolean isImg = false;
                json.put("text", user.getRealName() + "：" + StrUtil.getAbstract(request, result, len, " ", isImg) + "&nbsp;" + DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm"));
                */
                jsonAry.put(json);
            } catch (JSONException ex2) {
                ex2.printStackTrace();
            }
        }
    	
    	int len = jsonAry.length();
    	if (len==0) {
            return jsonAry;
        }
    	
    	JSONArray jary = new JSONArray();
    	// 取出根节点
    	JSONObject parent = jsonAry.getJSONObject(0);
    	parent.put("orders", 0);
    	parent.put("layer", 0);
    	jary.put(parent);
    	
    	for (int k=0; k<jary.length() && k<len; k++) {
    		parent = jary.getJSONObject(k);
    		int orders = parent.getInt("orders");
    		int layer = parent.getInt("layer");
    		
	    	for (int i=1; i<len; i++) {
	    		JSONObject json = jsonAry.getJSONObject(i);
	    		if (json.get("parent").equals(parent.get("id"))) {
	    			orders++;
	    			json.put("orders", orders);
	    			json.put("layer", layer + 1);
	    			jary.put(json);
	    		}
	    	}
    	}
    	
    	return jary;
    }

    public Vector<MyActionDb> getMyActionDbOfFlow(int flowId) {
        String sql = "select id from flow_my_action where flow_id=" + flowId + " order by receive_date asc";
        MyActionDb mad = new MyActionDb();
        return mad.list(sql);
    }

	public void setReadDate(java.util.Date readDate) {
		this.readDate = readDate;
	}

	public java.util.Date getReadDate() {
		return readDate;
	}

	/**
	 * @param partDept the partDept to set
	 */
	public void setPartDept(String partDept) {
		this.partDept = partDept;
	}

	/**
	 * @return the partDept
	 */
	public String getPartDept() {
		return partDept;
	}

	private long actionId;
    /**
     * 办理记录创建时间
     */
    private java.util.Date receiveDate;
    /**
     * 处理时间
     */
    private java.util.Date checkDate;
    private String userName;
    private boolean checked = false;
    /**
     * 未处理状态
     */
    private int checkStatus = CHECK_STATUS_NOT;

    /**
     * 当该action被接收时的status，有正处理、打回两种状态
     * 如果超时则由系统置为被忽略状态
     */
    private int actionStatus = WorkflowActionDb.STATE_DOING;

    private long id;
    private String proxyUserName;
    private int resultValue = WorkflowActionDb.RESULT_VALUE_NOT_ACCESSED;
    private long flowId;
    private java.util.Date expireDate;
    /**
     * 记录用户处理流程时所属的部门
     */
    private String deptCodes;
    private long privMyActionId = -1; // -1为起始待办记录

    private double performance = 0;
    private String performanceReason;
    private String checker;
    private String result;
    private String performanceModifier;
    
    private boolean readed = false;

    /**
     * 子流程中对应的myActionId
     */
    private long subMyActionId = SUB_MYACTION_ID_NONE;
    
    private java.util.Date readDate;

    public String getClusterNo() {
        return clusterNo;
    }

    public void setClusterNo(String clusterNo) {
        this.clusterNo = clusterNo;
    }

    /**
     * 集群编号
     */
    private String clusterNo;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    private String ip;
    private String os;
    private String browser;


    public boolean isAlter() {
        return alter;
    }

    public void setAlter(boolean alter) {
        this.alter = alter;
    }

    public java.util.Date getAlterTime() {
        return alterTime;
    }

    public void setAlterTime(java.util.Date alterTime) {
        this.alterTime = alterTime;
    }
}
