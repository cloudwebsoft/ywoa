package com.redmoon.oa.task;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.db.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.ui.*;

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
public class TaskDb extends ObjectDb implements IDesktopUnit  {
    private int id;
    
    private long projectId;

    public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public static final int NOPARENT = -1;

    public static final int TYPE_TASK = 0;
    public static final int TYPE_SUBTASK = 1;
    public static final int TYPE_RESULT = 2;
    public static final int TYPE_HURRY = 3;


    public static final int STATUS_NOTFINISHED = 0; // 未安排
    public static final int STATUS_FINISHED = 1; // 已完成
    public static final int STATUS_DISCARD = 2; // 已放弃
    public static final int STATUS_URGENT = 3; // 紧急
    public static final int STATUS_ARRANGED = 4; // 已安排

    // 子任务的状态
    public static final int STATUS_RECEIVED = 5; // 已接收
    public static final int STATUS_DOING = 6; // 进行中
    public static final int STATUS_WATI = 7; // 等待他人
    public static final int STATUS_FINISHED_NORMAL = 8; // 计划内完成
    public static final int STATUS_FINISHED_EXPIRE = 9; // 超期完成


    public TaskDb() {
        init();
    }

    public TaskDb(int id) {
        this.id = id;
        init();
        load();
    }

    public static String getTaskStatusDesc(int status) {
        switch (status) {
        case STATUS_NOTFINISHED:  return "未安排";
        case STATUS_FINISHED:  return "已完成";
        case STATUS_DISCARD:  return "已放弃";
        case STATUS_URGENT:  return "紧急";
        case STATUS_ARRANGED:  return "已安排";
        case STATUS_RECEIVED:  return "已接收";
        case STATUS_DOING:  return "进行中";
        case STATUS_WATI:  return "等待他人";
        case STATUS_FINISHED_NORMAL:  return "计划内完成";
        case STATUS_FINISHED_EXPIRE:  return "超期完成";
        }
        return "";
    }

    public boolean create() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = (int) SequenceManager.nextID(SequenceManager.OA_TASK);
            if (rootId==-1) // 如果是回复时，rootId已设不会为-1
                rootId = id;
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, fileName);
            ps.setString(5, ext);
            ps.setInt(6, type);
            ps.setInt(7, expression);
            ps.setString(8, initiator);
            ps.setInt(9, rootId);
            if (parentId==NOPARENT)
                person = initiator; // 这样处理是为了便于搜索
            ps.setString(10, person);
            ps.setInt(11, layer);
            ps.setInt(12, orders);
            ps.setInt(13, parentId);
            ps.setString(14, ip);
            ps.setInt(15, reCount);
            ps.setInt(16, status);
            ps.setString(17, jobCode);
            ps.setInt(18, actionId);
            ps.setTimestamp(19, new Timestamp(new java.util.Date().getTime()));
            if (beginDate==null)
                ps.setTimestamp(20, null);
            else
                ps.setTimestamp(20, new Timestamp(beginDate.getTime()));
            if (endDate==null)
                ps.setTimestamp(21, null);
            else
                ps.setTimestamp(21, new Timestamp(endDate.getTime()));
            ps.setString(22, remark);
            ps.setInt(23, secret?1:0);
            ps.setLong(24, projectId);
            ps.setString(25, unitCode);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                TaskCache rc = new TaskCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
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

    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url + StrUtil.UrlEncode(uds.getModuleItem());
    }

    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        Privilege privilege = new Privilege();

        int count = uds.getCount();
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageShow();

        String str = "";

        Vector tasks = getUserNotFinishedTask(privilege.getUser(request));
        Iterator taskir = tasks.iterator();
        String mydate = "";
        int k = 1;
        if(taskir.hasNext()) {
        	str = "<table class='article_table'>";
        	while (taskir.hasNext()) {
                TaskDb td = (TaskDb) taskir.next();
                mydate = DateUtil.format(td.getMyDate(), "yyyy-MM-dd");
                str += "<tr><td class='article_content'><a href='" + url + "?showid=" + td.getId() + "&rootid=" +
                        td.getId() +
                        "'>" + StrUtil.toHtml(td.getTitle()) + "</a></td><td class='article_time'>[" + mydate +
                        "]</td></tr>";
                k++;
                if (k>count)
                    break;
            }
        	str += "</table>";
        }else{
        	str = "<div class='no_content'><img title='暂无任务督办' src='images/desktop/no_content.jpg'></div>";
        }

        return str;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
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
                // 删除上传的文件
                Iterator ir = attachments.iterator();
                while (ir.hasNext()) {
                    Attachment att = (Attachment)ir.next();
                    att.del();
                }

                TaskCache rc = new TaskCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
                return true;
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

    public void initDB() {
        tableName = "task";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new TaskCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE = "insert into " + tableName + " (id,title,content,filename,ext,type,expression,initiator,rootid,person,layer,orders,parentid,ip,recount,status,jobCode,actionId,mydate,begin_date,end_date,remark,is_secret,project_id,unit_code) value (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set title=?,content=?,filename=?,ext=?,type=?,expression=?,initiator=?,rootid=?,person=?,layer=?,orders=?,parentid=?,ip=?,recount=?,status=?,jobCode=?,actionId=?,begin_date=?,end_date=?,remark=?,is_secret=?,project_id=?,progress=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by mydate desc";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select title,content,filename,ext,type,expression,initiator,rootid,person,layer,orders,parentid,ip,recount,status,mydate,jobCode,actionId,begin_date,end_date,remark,is_secret,project_id,progress,unit_code from task where id=?";

    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new TaskDb(pk.getIntValue());
    }

    public TaskDb getTaskDb(int id) {
        return (TaskDb)getObjectDb(new Integer(id));
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                title = rs.getString(1);
                content = rs.getString(2);
                fileName = StrUtil.getNullStr(rs.getString(3));
                ext = StrUtil.getNullStr(rs.getString(4));
                type = rs.getInt(5);
                expression = rs.getInt(6);
                initiator = StrUtil.getNullStr(rs.getString(7));
                rootId = rs.getInt(8);
                person = rs.getString(9);
                layer = rs.getInt(10);
                orders = rs.getInt(11);
                parentId = rs.getInt(12);
                ip = rs.getString(13);
                reCount = rs.getInt(14);
                status = rs.getInt(15);
                myDate = rs.getTimestamp(16);
                jobCode = rs.getString(17);
                actionId = rs.getInt(18);
                beginDate = rs.getTimestamp(19);
                endDate = rs.getTimestamp(20);
                remark = StrUtil.getNullStr(rs.getString(21));
                secret = rs.getInt(22)==1;
                projectId = rs.getLong(23);
                progress = rs.getInt(24);
                unitCode = rs.getString(25);
                
                loaded = true;
                primaryKey.setValue(new Integer(id));

                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                // 取得附件
                attachments = new Vector();
                String sql = "select id from task_attach where taskId=?";
                ps = conn.prepareStatement(sql);
                //logger.info(LOAD_DOCUMENT_ATTACHMENTS);
                ps.setInt(1, id);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        int aid = rs.getInt(1);
                        Attachment am = new Attachment(aid);
                        attachments.addElement(am);
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

    public boolean delAttachment(int attachId) {
        Attachment att = getAttachment(attachId);
        if (att==null)
            return false;
        boolean re = att.del();
        TaskCache rc = new TaskCache(this);
        primaryKey.setValue(new Integer(id));
        rc.refreshSave(primaryKey);
        return re;
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, fileName);
            ps.setString(4, ext);
            ps.setInt(5, type);
            ps.setInt(6, expression);
            ps.setString(7, initiator);
            ps.setInt(8, rootId);
            ps.setString(9, person);
            ps.setInt(10, layer);
            ps.setInt(11, orders);
            ps.setInt(12, parentId);
            ps.setString(13, ip);
            ps.setInt(14, reCount);
            ps.setInt(15, status);
            ps.setString(16, jobCode);
            ps.setInt(17, actionId);
            if (beginDate==null)
                ps.setTimestamp(18, null);
            else
                ps.setTimestamp(18, new Timestamp(beginDate.getTime()));
            if (endDate==null)
                ps.setTimestamp(19, null);
            else
                ps.setTimestamp(19, new Timestamp(endDate.getTime()));
            ps.setString(20, remark);
            ps.setInt(21, secret?1:0);
            ps.setLong(22, projectId);
            ps.setInt(23, progress);
            ps.setInt(24, id);
            re = conn.executePreUpdate()==1?true:false;

            if (re) {
                TaskCache rc = new TaskCache(this);
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

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setExpression(int expression) {
        this.expression = expression;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public void setRootId(int rootId) {
        this.rootId = rootId;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setReCount(int reCount) {
        this.reCount = reCount;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMyDate(Date myDate) {
        this.myDate = myDate;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public void setAttachments(Vector attachments) {
        this.attachments = attachments;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public void setBeginDate(java.util.Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
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

    public String getFileName() {
        return fileName;
    }

    public String getExt() {
        return ext;
    }

    public int getType() {
        return type;
    }

    public int getExpression() {
        return expression;
    }

    public String getInitiator() {
        return initiator;
    }

    public int getRootId() {
        return rootId;
    }

    public String getPerson() {
        return person;
    }

    public int getLayer() {
        return layer;
    }

    public int getOrders() {
        return orders;
    }

    public int getParentId() {
        return parentId;
    }

    public String getIp() {
        return ip;
    }

    public int getReCount() {
        return reCount;
    }

    public int getStatus() {
        return status;
    }

    public java.util.Date getMyDate() {
        return myDate;
    }

    public String getJobCode() {
        return jobCode;
    }

    public Vector getAttachments() {
        return attachments;
    }

    public int getActionId() {
        return actionId;
    }

    public java.util.Date getBeginDate() {
        return beginDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public String getTask() {
        return task;
    }

    public String getRemark() {
        return remark;
    }

    public boolean isSecret() {
        return secret;
    }

    public synchronized boolean del(int delid) throws
            ErrMsgException {

        TaskDb td = getTaskDb(delid);
        if (td==null)
        	return false;
        
        TaskDb rootTd = td.getTaskDb(td.getRootId());

        if (td == null || !td.isLoaded())
            throw new ErrMsgException("该任务已不存在！");
        ResultSet rs = null;
        int layer = 1, orders = 1, rootid = -1;
        String sql;

        layer = td.getLayer();
        rootid = td.getRootId();
        orders = td.getOrders();
        initiator = td.getInitiator();

        boolean updateorders = true;
        int orders1 = orders;

        // 删除结点本身
        boolean re = td.del();
        if (!re)
            return false;

        Conn conn = new Conn(connname);
        try {
            if (rootid == delid) { // 如果是根节点
                sql = "select id from task where rootid=" + delid + " and parentid<>-1";
                rs = conn.executeQuery(sql);
                while (rs.next()) {
                    TaskDb t = td.getTaskDb(rs.getInt(1));
                    t.del();
                }
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                updateorders = false;
            } else {
                sql = "select min(orders) from task where rootid=" + rootid +
                      " and orders>" + orders + " and layer<=" + layer;

                rs = conn.executeQuery(sql);
                if (rs != null && rs.next()) {
                    orders1 = rs.getInt(1); // 取出位于其后的第一个兄弟结点（如不存在则为第一个其父结点的兄弟结点）的orders
                }
                if (rs!=null) {
                    rs.close();
                    rs = null;
                }

                if (orders1 == 0) { // 为0则表示不存在兄弟结点，在其后位置上也不存在其父结点的兄弟结点
                    sql = "select id from task where rootid=" + rootid +
                          " and orders>" +
                          orders;
                    rs = conn.executeQuery(sql);
                    while (rs.next()) {
                        TaskDb t = td.getTaskDb(rs.getInt(1));
                        t.del();
                    }
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }

                    updateorders = false;

                    rootTd.setReCount(orders - 2);
                    rootTd.save();
                } else {
                    sql = "select id from task where rootid=" + rootid +
                          " and orders>" +
                          orders + " and orders<" + orders1;
                    logger.info("delTipic:sql=" + sql);
                    rs = conn.executeQuery(sql);
                    while (rs.next()) {
                        TaskDb t = td.getTaskDb(rs.getInt(1));
                        t.del();
                    }
                    if (rs!=null) {
                        rs.close();
                        rs = null;
                    }
                    rootTd.setReCount(rootTd.getReCount() - (orders1 - orders));
                    logger.info("recount=" + td.getReCount() + " " + (orders1 - orders));
                    rootTd.save();
                }
            }

            if (re && updateorders) {
                int dlt = orders1 - orders;
                sql = "select id from task where rootid=" +
                      rootid + " and orders>=" + orders1;
                rs = conn.executeQuery(sql);
                while (rs.next()) {
                    TaskDb t = td.getTaskDb(rs.getInt(1));
                    t.setOrders(t.getOrders() - dlt);
                    t.save();
                }
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }

        return re;
    }

    public Vector getUserJoinTask(String username) {
        String sql = "select distinct rootid from task where person=? order by mydate desc";
        ResultSet rs = null;
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    v.addElement(getTaskDb(rs.getInt(1)));
                }
            }
        } catch (Exception e) {
            logger.error("getUserJoinTask:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取得作为承办人未完成的任务
     * @param username String 承办人
     * @return String
     */
    public Vector getUserNotFinishedTask(String username) {
        String sql =
                "select distinct rootid from task where status=" + STATUS_NOTFINISHED + " and person=? order by mydate desc";
        ResultSet rs = null;
        Vector v = new Vector();

        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    v.addElement(getTaskDb(rs.getInt(1)));
                }
            }
        } catch (Exception e) {
            logger.error("getTaskNotFinishedOfPerson:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public ListResult listResult(String sql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
                // System.out.println("total=" + total);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executeQuery(sql);
            if (rs == null) {
                return lr;
            } else {
                TaskDb td;
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    td = getTaskDb(rs.getInt(1));
                    result.addElement(td);
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

    public ListResult listResult(String sql) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
                // System.out.println("total=" + total);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(total); // 尽量减少内存的使用

            rs = conn.executeQuery(sql);
            if (rs == null) {
                return lr;
            } else {
                TaskDb td;
                rs.setFetchSize(total);
                while (rs.next()) {
                    td = getTaskDb(rs.getInt(1));
                    result.addElement(td);
                }
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
        return lr;
    }

    private String title;
    private String content;
    private String fileName;
    private String ext;
    private int type;
    private int expression;
    private String initiator;
    private int rootId = -1;
    private String person;
    private int layer = 1;
    private int orders = 1;
    private int parentId = -1;
    private String ip;
    private int reCount = 0;
    private int status = 0;
    private java.util.Date myDate;
    private String jobCode;
    private Vector attachments;
    private int actionId;
    private java.util.Date beginDate;
    private java.util.Date endDate;
    private String task;
    private String remark;
    private boolean secret = false;
    
    private int progress = 0;
    
    private String unitCode;

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
}
