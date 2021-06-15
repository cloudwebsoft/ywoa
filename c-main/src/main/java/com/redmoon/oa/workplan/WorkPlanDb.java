package com.redmoon.oa.workplan;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.kernel.*;
import com.redmoon.oa.db.SequenceManager;

import javax.servlet.http.HttpServletRequest;

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
public class WorkPlanDb extends ObjectDb {
    private int id;
    private String unitCode;
	private String gantt;

    public static final int KIND_MEMBER = 0;
    public static final int KIND_PRINCIPAL = 1;
    
    /**
     * 未审核
     */
    public static final int CHECK_STATUS_NOT = 0;
    /**
     * 已通过
     */
    public static final int CHECK_STATUS_PASSED = 1;

    public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public WorkPlanDb() {
        init();
    }

    public WorkPlanDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public java.util.Date getBeginDate() {
        return beginDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getRemark() {
        return remark;
    }

    public String getPrincipal() {
        return principal;
    }

    public void initDB() {
        tableName = "work_plan";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new WorkPlanCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (title,content,beginDate,endDate,typeId,remark,principal,author,progress,add_date,id, project_id, unit_code,flow_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set title=?,content=?,beginDate=?,endDate=?,typeId=?,remark=?,principal=?,progress=?,project_id=?,flow_id=?,gantt=?,check_status=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select title,content,beginDate,endDate,typeId,remark,principal,author,progress,project_id,unit_code,flow_id,gantt,check_status from " + tableName + " where id=?";
    }

    public String getQuery(String action, String op, String kind, String userName, String title, String content, String what, int progressFlag, String typeId, String beginDate, String endDate, String orderBy, String sort) {
        String sql = "select distinct p.id, " + orderBy + " from work_plan p, work_plan_user u where u.workPlanId=p.id and u.userName=" + StrUtil.sqlstr(userName);
        if (op.equals("mine"))
            sql = "select id from work_plan where author=" + StrUtil.sqlstr(userName);
        else if (op.equals("favorite")) {
            sql = "select workplan_id from work_plan_favorite f, work_plan p where p.id=f.workplan_id and f.user_name=" + StrUtil.sqlstr(userName);
        }
        if (op.equals("search"))
        {
            if (title != null &&  !title.equals(""))
                sql += " and p.title like " + StrUtil.sqlstr("%" + title + "%");
            if(content != null &&  !content.equals(""))
                sql += " and p.content like " + StrUtil.sqlstr("%" + content + "%");
            if (!beginDate.equals(""))
                sql += " and p.beginDate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
            if (!endDate.equals(""))
                sql += " and p.endDate<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
            if (!typeId.equals("all") && !typeId.equals(""))
                sql += " and p.typeId=" + typeId;
        }
        if (action.equals("search")) {
            if (!op.equals("mine")) {
                if (kind.equals("title"))
                    sql += " and p.title like " + StrUtil.sqlstr("%" + what + "%");
                else
                    sql += " and p.content like " + StrUtil.sqlstr("%" + what + "%");
                if (!beginDate.equals(""))
                    sql += " and p.beginDate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
                if (!endDate.equals(""))
                    sql += " and p.endDate<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
                if (!typeId.equals("all") && !typeId.equals(""))
                    sql += " and p.typeId=" + typeId;
                if (progressFlag!=-1) {
                    if (progressFlag==0)
                        sql += " and p.progress<100";
                    else
                        sql += " and p.progress=100";
                }
            }
            else {
                if (kind.equals("title"))
                    sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
                else
                    sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
                if (!beginDate.equals(""))
                    sql += " and beginDate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
                if (!endDate.equals(""))
                    sql += " and endDate<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
                if (!typeId.equals("all") && !typeId.equals(""))
                    sql += " and typeId=" + typeId;
                if (progressFlag!=-1) {
                    if (progressFlag==0)
                        sql += " and progress<100";
                    else
                        sql += " and progress=100";
                }
            }
        }
        sql += " order by " + orderBy + " " + sort;
        return sql;
    }

    public WorkPlanDb getWorkPlanDb(int id) {
        return (WorkPlanDb)getObjectDb(new Integer(id));
    }

    public boolean create(FileUpload fu) throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = (int)SequenceManager.nextID(SequenceManager.OA_WORKPLAN);
            conn.beginTrans();
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setDate(3, new java.sql.Date(beginDate.getTime()));
            ps.setDate(4, new java.sql.Date(endDate.getTime()));
            ps.setInt(5, typeId);
            ps.setString(6, remark);
            ps.setString(7, principal);
            ps.setString(8, author);
            ps.setInt(9, progress);
            ps.setTimestamp(10, new java.sql.Timestamp(new java.util.Date().getTime()));
            ps.setInt(11, id);
            ps.setLong(12, projectId);
            ps.setString(13, unitCode);
            ps.setInt(14, flowId);
            re = conn.executePreUpdate()==1?true:false;
            // logger.info("create:re=" + re + " users=" + users);
            if (ps != null) {
                ps.close();
                ps = null;
            }

            if (re) {
                if (users!=null) {
                    int len = users.length;
                    String sql = "insert into work_plan_user (workPlanId, userName, kind) values (?,?,?)";
                    ps = conn.prepareStatement(sql);
                    for (int i=0; i<len; i++) {
                        ps.clearParameters();
                        ps.setInt(1, id);
                        ps.setString(2, users[i]);
                        ps.setInt(3, KIND_MEMBER);
                        conn.executePreUpdate();
                    }
                }
                if (principals!=null) {
                    int len = principals.length;
                    String sql = "insert into work_plan_user (workPlanId, userName, kind) values (?,?,?)";
                    ps = conn.prepareStatement(sql);
                    for (int i=0; i<len; i++) {
                        ps.clearParameters();
                        ps.setInt(1, id);
                        ps.setString(2, principals[i]);
                        ps.setInt(3, KIND_PRINCIPAL);
                        conn.executePreUpdate();
                    }
                }

                if (ps!=null) {
                    ps.close();
                    ps = null;
                }
                if (depts!=null) {
                    int len = depts.length;
                    String sql = "insert into work_plan_dept (workPlanId, deptCode) values (?,?)";
                    ps = conn.prepareStatement(sql);
                    for (int i=0; i<len; i++) {
                        ps.clearParameters();
                        ps.setInt(1, id);
                        ps.setString(2, depts[i]);
                        conn.executePreUpdate();
                    }
                 }

                 // 处理附件
                 if (fu.getRet() == FileUpload.RET_SUCCESS) {
                     // 置保存路径
                     Calendar cal = Calendar.getInstance();
                     String year = "" + (cal.get(Calendar.YEAR));
                     String month = "" + (cal.get(Calendar.MONTH) + 1);

                     com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                     String vpath = cfg.get("file_workplan") + "/" + year + "/" + month + "/";

                     String filepath = Global.getRealPath() + vpath;
                     fu.setSavePath(filepath);
                     // 使用随机名称写入磁盘
                     fu.writeFile(true);
                     Vector v = fu.getFiles();
                     FileInfo fi = null;
                     Iterator ir = v.iterator();
                     while (ir.hasNext()) {
                         fi = (FileInfo) ir.next();
                         Attachment att = new Attachment();
                         att.setFullPath(filepath + fi.getDiskName());
                         att.setWorkPlanId(id);
                         att.setName(fi.getName());
                         att.setDiskName(fi.getDiskName());
                         att.setVisualPath(vpath);
                         re = att.create();
                     }
                 }
                 WorkPlanCache rc = new WorkPlanCache(this);
                 rc.refreshCreate();
            }
            conn.commit();
        }
        catch (SQLException e) {
            conn.rollback();
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public String[] getPrincipals() {
		return principals;
	}


	/**
     * del
     *
     * @return boolean
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                WorkPlanCache rc = new WorkPlanCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);

                // 删除附件
                if (attachments!=null) {
                    Iterator ir = attachments.iterator();
                    while (ir.hasNext()) {
                        Attachment att = (Attachment)ir.next();
                        att.del();
                    }
                }

                // 删除task
                WorkPlanTaskDb wptd = new WorkPlanTaskDb();
                try {
                    wptd.delOfWorkPlan(id);
                } catch (ResKeyException ex) {
                    ex.printStackTrace();
                }

                // 删除调度项
                JobUnitDb ju = new JobUnitDb();
                ju.delJobOfWorkplan(id);

                // 删除进度信息
                WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
        		String sql = wad.getTable().getSql("listWorkPlanAnnex");
        		Iterator ir = wad.list(sql, new Object[]{new Long(id)}).iterator();
        		while (ir.hasNext()) {
        			wad = (WorkPlanAnnexDb)ir.next();
        			try {
						wad.del();
					} catch (ResKeyException e) {
						e.printStackTrace();
					}
        		}
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WorkPlanDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
        // QUERY_LOAD = "select title,content,beginDate,endDate,typeId,remark from " + tableName + " where id=?";
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                title = rs.getString(1);
                content = rs.getString(2);
                beginDate = rs.getTimestamp(3);
                endDate = rs.getTimestamp(4);
                typeId = rs.getInt(5);
                remark = rs.getString(6);
                principal = StrUtil.getNullStr(rs.getString(7));
                author = rs.getString(8);
                progress = rs.getInt(9);
                projectId = rs.getLong(10);
                unitCode = rs.getString(11);
                flowId = rs.getInt(12);
                gantt = StrUtil.getNullStr(rs.getString(13));
                checkStatus = rs.getInt(14);
                loaded = true;
                primaryKey.setValue(new Integer(id));

                // 获取附件
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                String sql =
                        "select id from work_plan_attach where workPlanId=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                rs = conn.executePreQuery();
                attachments = new Vector();
                while (rs.next()) {
                    Attachment att = new Attachment(rs.getInt(1));
                    attachments.addElement(att);
                }

                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                sql = "select deptCode from work_plan_dept where workPlanId=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                rs = conn.executePreQuery();
                if (rs != null) {
                    depts = new String[conn.getRows()];
                    int i = 0;
                    while (rs.next()) {
                        depts[i] = rs.getString(1);
                        i++;
                    }
                }
                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                // 取出参与者
                sql = "select userName from work_plan_user where workPlanId=? and kind=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.setInt(2, KIND_MEMBER);
                rs = conn.executePreQuery();
                if (rs != null) {
                    users = new String[conn.getRows()];
                    int i = 0;
                    while (rs.next()) {
                        users[i] = rs.getString(1);
                        i++;
                    }
                }
                // 取出负责人
                ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.setInt(2, KIND_PRINCIPAL);
                rs = conn.executePreQuery();
                if (rs != null) {
                    principals = new String[conn.getRows()];
                    int i = 0;
                    while (rs.next()) {
                    	principals[i] = rs.getString(1);
                        i++;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    public Attachment getAttachment(int attId) {
        Iterator ir = attachments.iterator();
        while (ir.hasNext()) {
            Attachment at = (Attachment)ir.next();
            if (at.getId()==attId)
                return at;
        }
        return null;
    }

    public boolean delAttachment(int attachId) {
        Attachment att = new Attachment(attachId);
        if (att==null)
            return false;
        boolean re = att.del();
        WorkPlanCache rc = new WorkPlanCache(this);
        primaryKey.setValue(new Integer(id));
        rc.refreshSave(primaryKey);
        return re;
    }

    /**
     * 保存，用于当进度发生变化时
     * @return boolean
     */
    public boolean save() {
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, title);
             ps.setString(2, content);
             ps.setDate(3, new java.sql.Date(beginDate.getTime()));
             ps.setDate(4, new java.sql.Date(endDate.getTime()));
             ps.setInt(5, typeId);
             ps.setString(6, remark);
             ps.setString(7, principal);
             ps.setInt(8, progress);
             ps.setLong(9, projectId);
             ps.setInt(10, flowId);
             ps.setString(11, gantt);
             ps.setInt(12, checkStatus);
             ps.setInt(13, id);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 WorkPlanCache rc = new WorkPlanCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             logger.error("save: " + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
        return re;
    }

    /**
     * save
     *
     * @return boolean
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save(FileUpload fu) throws ErrMsgException {
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             // QUERY_SAVE = "update " + tableName + " set title=?,content=?,beginDate=?,endDate=?,typeId=?,remark=? where id=?";
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, title);
             ps.setString(2, content);
             ps.setDate(3, new java.sql.Date(beginDate.getTime()));
             ps.setDate(4, new java.sql.Date(endDate.getTime()));
             ps.setInt(5, typeId);
             ps.setString(6, remark);
             ps.setString(7, principal);
             ps.setInt(8, progress);
             ps.setLong(9, projectId);
             ps.setInt(10, flowId);
             ps.setString(11, gantt);
             ps.setInt(12, checkStatus);
             ps.setInt(13, id);
             re = conn.executePreUpdate()==1?true:false;
             if (re) {
                 WorkPlanCache rc = new WorkPlanCache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);

                 // 更新计划中的参与人员与部门
                 if (ps!=null) {
                     ps.close();
                     ps = null;
                 }
                 String sql = "delete from work_plan_user where workPlanId=?";
                 ps = conn.prepareStatement(sql);
                 ps.setInt(1, id);
                 conn.executePreUpdate();
                 sql = "delete from work_plan_dept where workPlanId=?";
                 ps = conn.prepareStatement(sql);
                 ps.setInt(1, id);
                 conn.executePreUpdate();

                 if (ps!=null) {
                     ps.close();
                     ps = null;
                 }
                 if (users!=null) {
                     int len = users.length;
                     sql = "insert into work_plan_user (workPlanId, userName, kind) values (?,?,?)";
                     ps = conn.prepareStatement(sql);
                     for (int i=0; i<len; i++) {
                         ps.clearParameters();
                         ps.setInt(1, id);
                         ps.setString(2, users[i]);
                         ps.setInt(3, KIND_MEMBER);
                         conn.executePreUpdate();
                     }
                 }
                 if (principals!=null) {
                     int len = principals.length;
                     sql = "insert into work_plan_user (workPlanId, userName, kind) values (?,?,?)";
                     ps = conn.prepareStatement(sql);
                     for (int i=0; i<len; i++) {
                         ps.clearParameters();
                         ps.setInt(1, id);
                         ps.setString(2, principals[i]);
                         ps.setInt(3, KIND_PRINCIPAL);
                         conn.executePreUpdate();
                     }
                 }

                 if (ps!=null) {
                     ps.close();
                     ps = null;
                 }
                 if (depts!=null) {
                     int len = depts.length;
                     sql = "insert into work_plan_dept (workPlanId, deptCode) values (?,?)";
                     ps = conn.prepareStatement(sql);
                     for (int i=0; i<len; i++) {
                         ps.clearParameters();
                         ps.setInt(1, id);
                         ps.setString(2, depts[i]);
                         conn.executePreUpdate();
                     }
                 }

                 // 保存附件
                 if (fu.getRet() == FileUpload.RET_SUCCESS) {
                     // 置保存路径
                     Calendar cal = Calendar.getInstance();
                     String year = "" + (cal.get(Calendar.YEAR));
                     String month = "" + (cal.get(Calendar.MONTH) + 1);

                     com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                     String vpath = cfg.get("file_workplan") + "/" + year + "/" + month + "/";

                     String filepath = Global.getRealPath() + vpath;
                     fu.setSavePath(filepath);
                     // 使用随机名称写入磁盘
                     fu.writeFile(true);
                     Vector v = fu.getFiles();
                     FileInfo fi = null;
                     Iterator ir = v.iterator();
                     while (ir.hasNext()) {
                         fi = (FileInfo) ir.next();
                         Attachment att = new Attachment();
                         att.setFullPath(filepath + fi.getDiskName());
                         att.setWorkPlanId(id);
                         att.setName(fi.getName());
                         att.setDiskName(fi.getDiskName());
                         att.setVisualPath(vpath);
                         re = att.create();
                     }
                 }
             }
         } catch (SQLException e) {
             logger.error("save: " + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
        return re;
    }

    public String getGantt() {
		return gantt;
	}

	public void setGantt(String gantt) {
		this.gantt = gantt;
	}

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
            // logger.info("countsql=" + countsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

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
                    WorkPlanDb wpd = getWorkPlanDb(rs.getInt(1));
                    result.addElement(wpd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error("listResult:" + e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     * 取出全部信息置于result中
     */
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
                    result.addElement(getWorkPlanDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }
    public boolean insertIntoWorkplanUser(int workPlanId, String user, int kindStyle)
    {
    	Conn conn = new Conn(connname);
    	String sql = "insert into work_plan_user (workPlanId, userName, kind) values (?,?,?)";
    	PreparedStatement ps;
		try {
			ps = conn.prepareStatement(QUERY_CREATE);
			ps = conn.prepareStatement(sql);
	        
		    ps.clearParameters();
		    ps.setInt(1, workPlanId);
		    ps.setString(2, user);
		    ps.setInt(3, kindStyle);
		    conn.executePreUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	
	    return true;
        
    }
    /**
     * 根据计划ID和用户名查询是计划参与人是否有此人
     * @param workPlanId
     * @param user
     * @return
     */
    public boolean queryByWorkPlanIdAndName(int workPlanId, String user)
    {
    	Conn conn = new Conn(connname);
    	String sql = "select count(*) count from work_plan_user where workPlanId=? and userName=?";
    	ResultSet rs = null;
    	PreparedStatement ps;
		try {
			ps = conn.prepareStatement(QUERY_LIST);
			ps = conn.prepareStatement(sql);
	        
		    ps.clearParameters();
		    ps.setInt(1, workPlanId);
		    ps.setString(2, user);
		    rs = conn.executePreQuery();
		    int rowCount = 0; 
		    if (rs.next())
		    {
		    	rowCount = rs.getInt("count");   
		    }
		    if(rowCount == 0)
		    {
		    	return false;
		    }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	
	    return true;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setBeginDate(java.util.Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String[] getUsers() {
        return users;
    }

    public String[] getDepts() {
        return depts;
    }

    public Vector getAttachments() {
        return attachments;
    }

    public String getAuthor() {
        return author;
    }

    public int getProgress() {
        return progress;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    public void setDepts(String[] depts) {
        this.depts = depts;
    }

    public void setAttachments(Vector attachments) {
        this.attachments = attachments;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setAddDate(java.util.Date addDate) {
        this.addDate = addDate;
    }

    private String title;
    private String content;
    private java.util.Date beginDate;
    private java.util.Date endDate;
    private int typeId;
    private String remark;

    /**
     * @deprecated
     */
    private String principal;

    private String[] users;
    private String[] depts;
    private String[] principals;
    private Vector attachments;
    private String author;
    private int progress = 0;
    private java.util.Date addDate;
    private int flowId = 0;
    public int getFlowId() {
		return flowId;
	}

	public void setFlowId(int flowId) {
		this.flowId = flowId;
	}

	private long projectId = -1;
	private int checkStatus = CHECK_STATUS_NOT;

	public int getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(int checkStatus) {
		this.checkStatus = checkStatus;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public void setPrincipals(String[] principals) {
		this.principals = principals;
	}

}
