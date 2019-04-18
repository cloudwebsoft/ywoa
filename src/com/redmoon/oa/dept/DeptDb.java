package com.redmoon.oa.dept;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.ui.LocalUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

public class DeptDb extends ObjectDb implements Serializable {
    private String code = "", name = "", description = "", parentCode = "-1",
            rootCode = "", addDate = "";
    private int orders = 1, layer = 1, childCount = 0;
    boolean isHome = false;
    private boolean loaded = false;
    
    private String shortName;
    
    public static final int TYPE_DEPT = 1;
    public static final int TYPE_UNIT = 0;
    
    private int type = TYPE_DEPT;
    
    /**
     * 是否为班组
     */
    private boolean group = false;
    
    /**
     * 是否隐藏
     */
    private boolean hide = false;
    
    public static final String ROOTCODE = "root";

    public DeptDb() {
        init();
    }

    public DeptDb(String code) {
        this.code = code;
        init();
        load();
    }

    public DeptDb(int id) {
        this.id = id;
        init();
        loadById();
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new DeptDb(pk.getStrValue());
    }

    public void setQueryCreate() {

    }

    public void setQuerySave() {
        this.QUERY_SAVE = "update department set name=?,description=?,parentCode=?,dept_type=?,orders=?,childCount=?,layer=?,is_show=?,short_name=?,is_group=?,is_hide=?,id=? where code=?";
    }

    public void setQueryDel() {

    }

    public void setQueryLoad() {
        this.QUERY_LOAD = "select code,name,description,parentCode,rootCode,orders,layer,childCount,addDate,dept_type,id,is_show,short_name,is_group,is_hide from department where code=?";
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                this.code = rs.getString(1);
                name = rs.getString(2);
                description = StrUtil.getNullStr(rs.getString(3));
                parentCode = rs.getString(4);
                rootCode = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                childCount = rs.getInt(8);
                setAddDate(DateUtil.format(rs.getDate(9), "yyyy-MM-dd"));
                type = rs.getInt(10);
                id = rs.getInt(11);
                show = rs.getInt(12)==1;
                shortName = StrUtil.getNullStr(rs.getString(13));
                group = rs.getInt(14)==1;
                hide = rs.getInt(15)==1;
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void loadById() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement("select code,name,description,parentCode,rootCode,orders,layer,childCount,addDate,dept_type,id,is_show,short_name,is_group,is_hide from department where id=?");
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                this.code = rs.getString(1);
                name = rs.getString(2);
                description = StrUtil.getNullStr(rs.getString(3));
                parentCode = rs.getString(4);
                rootCode = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                childCount = rs.getInt(8);
                setAddDate(DateUtil.format(rs.getDate(9), "yyyy-MM-dd"));
                type = rs.getInt(10);
                id = rs.getInt(11);
                show = rs.getInt(12)==1;
                shortName = StrUtil.getNullStr(rs.getString(13));
                group = rs.getInt(14)==1;
                hide = rs.getInt(15)==1;
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setRootCode(String c) {
        this.rootCode = c;
    }

    public void setType(int t) {
        this.type = t;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public int getOrders() {
        return orders;
    }

    public boolean getIsHome() {
        return isHome;
    }

    public void setParentCode(String p) {
        this.parentCode = p;
    }

    public String getParentCode() {
        return this.parentCode;
    }

    public void setIsHome(boolean b) {
        this.isHome = b;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getRootCode() {
        return rootCode;
    }

    public int getLayer() {
        return layer;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public int getChildCount() {
        return childCount;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getId() {
        return id;
    }

    public Vector getChildren() {
        Vector v = new Vector();
        String sql = "select code from department where parentCode=? order by orders";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    String c = rs.getString(1);
                    //logger.info("child=" + c);
                    v.addElement(getDeptDb(c));
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取出code结点的所有孩子结点
     * @param code String
     * @return ResultIterator
     * @throws ErrMsgException
     */
    public Vector getAllChild(Vector vt, DeptDb dd) throws ErrMsgException {
        Vector children = dd.getChildren();
        if (children.isEmpty())
            return children;
        vt.addAll(children);
        Iterator ir = children.iterator();
        while (ir.hasNext()) {
            DeptDb deptDb = (DeptDb) ir.next();
            getAllChild(vt, deptDb);
        }
        return vt;
    }

    public String toString() {
        return "Dept is " + code;
    }

    public synchronized boolean save() {
        Conn conn = new Conn(connname);
        int r = 0;
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, parentCode);
            ps.setInt(4, type);
            ps.setInt(5, orders);
            ps.setInt(6, childCount);
            ps.setInt(7, layer);
            ps.setInt(8, show?1:0);
            ps.setString(9, shortName);
            ps.setInt(10, group?1:0);
            ps.setInt(11, hide?1:0);
            ps.setInt(12,id);
            ps.setString(13, code);

            r = conn.executePreUpdate();
            try {
                if (r == 1) {
                    re = true;
                    DeptCache dcm = new DeptCache();
                    dcm.refreshSave(code, parentCode);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 更改了分类
     * @param newDirCode String
     * @return boolean
     */
    public synchronized boolean save(String newParentCode) {
        if (newParentCode.equals(parentCode))
            return false;
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        DeptDb lfparent = getDeptDb(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update department set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",dept_type=" + type +
                     ",parentCode=" + StrUtil.sqlstr(newParentCode) + ",orders=" + neworders +
                     ",layer=" + (parentLayer+1) + ",is_show=" + (show?1:0) +
                     ",short_name=" + StrUtil.sqlstr(shortName) +
                     ",is_group=" + (group?1:0) + ",is_hide=" + (hide?1:0) +
                     " where code=" + StrUtil.sqlstr(code);
 
        String oldParentCode = parentCode;
        parentCode = newParentCode;
        DeptCache dcm = new DeptCache();

        int r = 0;

        RMConn conn = new RMConn(connname);
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    dcm.refreshSave(code, parentCode);

                    dcm.removeFromCache(newParentCode);
                    dcm.removeFromCache(oldParentCode);
                    // DirListCacheMgr更新
                    DeptChildrenCache.remove(oldParentCode);
                    DeptChildrenCache.remove(newParentCode);

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from department where parentCode=" + StrUtil.sqlstr(oldParentCode) +
                          " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord)ri.next();
                        DeptDb dd = getDeptDb(rr.getString(1));
                        dd.setOrders(dd.getOrders() - 1);
                        dd.save();
                    }

                    // 更新其所有子结点的layer
                    Vector vt = new Vector();
                    getAllChild(vt, this);
                    // int childcount = vt.size();
                    Iterator ir = vt.iterator();
                    while (ir.hasNext()) {
                        DeptDb childlf = (DeptDb)ir.next();
                        int layer = parentLayer + 1 + 1;
                        String pcode = childlf.getParentCode();
                        while (!pcode.equals(code)) {
                            layer ++;
                            DeptDb lfp = getDeptDb(pcode);
                            pcode = lfp.getParentCode();
                        }

                        childlf.setLayer(layer);
                        childlf.save();
                    }


                    // 将其原来的父结点的孩子数-1
                    DeptDb oldParentLeaf = getDeptDb(oldParentCode);
                    oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
                    oldParentLeaf.save();

                    // 将其新父结点的孩子数 + 1
                    DeptDb newParentLeaf = getDeptDb(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
                    newParentLeaf.save();

                    logger.info("save: newParentLeaf.childcount=" + newParentLeaf.getChildCount() + " oldParentLeaf.childcount=" + oldParentLeaf.getChildCount() + " oldparentCode=" + oldParentCode + " newParentCode=" + newParentCode + " neworders=" + neworders);

                }
            } catch (Exception e) {
                logger.error("save1: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("save2: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            dcm.removeFromCache(code);
        }
        return re;
    }
    
    public boolean AddChild(DeptDb childleaf) throws
            ErrMsgException {
        //计算得出插入结点的orders
        int childorders = childCount + 1;
        
        childleaf.setOrders(childorders);

        String updatesql = "";

        String insertsql = "insert into department (code,name,parentCode,description,orders,rootCode,childCount,layer,dept_type,addDate,id,is_show,short_name,is_group,is_hide) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException("请勿输入非法字符如;号等！");
        Conn conn = new Conn(connname);
        try {
            //更改根结点的信息
            updatesql = "update department set childCount=childCount+1" +
                        " where code=" + StrUtil.sqlstr(code);
            conn.beginTrans();
            PreparedStatement ps = conn.prepareStatement(insertsql);
            ps.setString(1, childleaf.getCode());
            ps.setString(2, childleaf.getName());
            ps.setString(3, code);
            ps.setString(4, childleaf.getDescription());
            ps.setInt(5, childorders);
            ps.setString(6, rootCode);
            ps.setInt(7, 0);
            ps.setInt(8, layer + 1);
            ps.setInt(9, childleaf.getType());
            ps.setDate(10, new java.sql.Date(new java.util.Date().getTime()));
            if(childleaf.getId() == -1){
                childleaf.setId((int)SequenceManager.nextID(SequenceManager.OA_DEPT));
            }
            ps.setInt(11, childleaf.getId());
            ps.setInt(12, childleaf.isShow()?1:0);
            ps.setString(13, childleaf.getShortName());
            ps.setInt(14, childleaf.isGroup()?1:0);
            ps.setInt(15, childleaf.isHide()?1:0);
            
            conn.executePreUpdate();
            if (ps!=null) {
                ps.close();
                ps = null;
            }

            conn.executeUpdate(updatesql);
            conn.commit();
            DeptCache dcm = new DeptCache();
            dcm.refreshAddChild(code);
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage());
            throw new ErrMsgException("请检查 " + childleaf.getName() + " 编码:" + childleaf.getCode() + " 是否有重复！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
    }

    public DeptDb getDeptDb(String code) {
        DeptCache dcm = new DeptCache();
        return dcm.getDeptDb(code);
    }

    public boolean delsingle(DeptDb leaf) {
        String sql = "delete from department where code=" + StrUtil.sqlstr(leaf.getCode());
        RMConn rmconn = new RMConn(connname);
        boolean r = false;
        try {
            r = rmconn.executeUpdate(sql) == 1 ? true : false;
            sql = "update department set orders=orders-1 where parentCode=" + StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" + leaf.getOrders();
            rmconn.executeUpdate(sql);
            sql = "update department set childCount=childCount-1 where code=" + StrUtil.sqlstr(leaf.getParentCode());
            rmconn.executeUpdate(sql);
            DeptCache dcm = new DeptCache();
            dcm.refreshDel(leaf.getCode(), leaf.getParentCode());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
        finally {
            DeptCache dcm = new DeptCache();
            dcm.refreshDel(leaf.getCode(), leaf.getParentCode());
        }
        return r;
    }

    public synchronized void del(DeptDb leaf) throws ErrMsgException {
        if(leaf.getChildCount()>0){
            throw new ErrMsgException("请删除部门下的子部门后，再删除本部门！");
        }
        DeptUserDb du = new DeptUserDb();
        int count = du.list(leaf.getCode()).size();
        if (count>0)
            throw new ErrMsgException("部门：" + leaf.getName() + " 下有" + count + "名人员，请先将人员安排至其它部门再删除！");
        Iterator children = leaf.getChildren().iterator();
        while (children.hasNext()) {
            DeptDb lf = (DeptDb) children.next();
            del(lf);
        }
        delsingle(leaf);
    }

    public boolean del() throws ErrMsgException {
        del(this);
        return true;
    }

    public DeptDb getBrother(String direction) {
        String sql;
        RMConn rmconn = new RMConn(connname);
        DeptDb bleaf = null;
        try {
            if (direction.equals("down")) {
                sql = "select code from department where parentCode=" +
                      StrUtil.sqlstr(parentCode) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from department where parentCode=" +
                      StrUtil.sqlstr(parentCode) +
                      " and orders=" + (orders - 1);
            }

            ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                bleaf = getDeptDb(rr.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return bleaf;
    }

    public boolean move(String direction) {
        String sql = "";

        // 取出该结点的移动方向上的下一个兄弟结点的orders
        boolean isexist = false;

        DeptDb bleaf = getBrother(direction);
        if (bleaf != null) {
            isexist = true;
        }
        
        com.redmoon.oa.tigase.Config cfg = new com.redmoon.oa.tigase.Config();
        if (cfg.getBooleanProperty("isUse")) {
        	bcode = bleaf.getCode();
        }
        
        //如果移动方向上的兄弟结点存在则移动，否则不移动
        if (isexist) {
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();
                if (direction.equals("down")) {
                    sql = "update department set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update department set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                else if (direction.equals("up")) {
                    sql = "update department set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update department set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                conn.commit();
                DeptCache dcm = new DeptCache();
                dcm.refreshMove(code, bleaf.getCode());
            } catch (Exception e) {
                conn.rollback();
                logger.error(e.getMessage());
                return false;
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        }

        return true;
    }
/*    
    public int getInitialChildCount(String code){
    	int count = 0;
    	String sql = "";
    	if("root".equals(code)){
    		sql = "select count(code) from department where code <> 'root' and code like '____'" ;
    	}else{
    		sql = "select count(code) from department where code like '"+code+"____'" ;
    	}
    	RMConn rmconn = new RMConn(connname);
    	try {
    		ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                count = rr.getInt(1);
            }
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
    	return count;
    }*/

    public void setQueryList() {
        this.QUERY_LIST = "select code from department";
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("code", PrimaryKey.TYPE_STRING);
    }

    public ObjectDb getObjectDb(Object objKey) {
        return getDeptDb(objKey.toString());
    }

    /**
     * 只是单纯为了实现纯虚函数
     * @param sql String
     * @return int
     */
    public int getObjectCount(String sql) {
        return 0;
    }
    
    /**
     * 取得部门所在的单位
     * @param dd
     * @return
     */
    public DeptDb getUnitOfDept(DeptDb dd) {
    	// System.out.println(getClass() + " " + dd.getName());
    	if (dd.getType()==TYPE_UNIT) {
    		return dd;
    	}

    	String parentCode = dd.getParentCode();
    	
    	while (!parentCode.equals(ROOTCODE) && !parentCode.equals("-1")) {
        	DeptDb deptDb = getDeptDb(parentCode);
        	if (deptDb==null || !deptDb.isLoaded())
        		return getDeptDb(ROOTCODE);
        	if (deptDb.getType()==DeptDb.TYPE_UNIT)
        		return deptDb;
        	
        	// System.out.println(getClass() + " parentCode=" + parentCode);
        	parentCode = deptDb.getParentCode();
    	}
    	
    	return getDeptDb(ROOTCODE);
    	
    	
    }

    /**
     * 只为实现纯虚函数
     * @param query String
     * @param startIndex int
     * @return Object[]
     */
    public Object[] getObjectBlock(String query, int startIndex) {
        return null;
    }
    /**
     * 创建节点
     */
    public synchronized boolean create(){
    	
    	 String insertsql = "insert into department (code,name,parentCode,description,orders,rootCode,childCount,layer,dept_type,addDate,id,is_show,short_name,is_group,is_hide) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    	 if (!SecurityUtil.isValidSql(insertsql)){
    		 LogUtil.getLog(this.getClass()).error("请勿输入非法字符如;号等！");
    		 return false;
    	 }
    	   Conn conn = new Conn(connname);
    	   PreparedStatement ps = null;
    	   try
    	   {
	    	   ps = conn.prepareStatement(insertsql);
	           ps.setString(1, this.code);
	           ps.setString(2, this.name);
	           ps.setString(3, this.parentCode);
	           ps.setString(4, this.description);
	           ps.setInt(5, this.orders);
	           ps.setString(6, this.rootCode);
	           ps.setInt(7, this.childCount);
	           ps.setInt(8, layer);
	           ps.setInt(9, type);
	           ps.setDate(10, new java.sql.Date(new java.util.Date().getTime()));
	           this.setId((int)SequenceManager.nextID(SequenceManager.OA_DEPT));
	           ps.setInt(11, this.getId());
	           ps.setInt(12, this.isShow()?1:0);
	           ps.setString(13, shortName);
	           ps.setInt(14, group?1:0);
	           ps.setInt(15, hide?1:0);
	           conn.executePreUpdate();
	           
    	   }catch(Exception e){
    		  LogUtil.getLog(this.getClass()).error("create dept error :" + e.getMessage());
    		  return false;
    	   }finally{
    		   if (ps!=null) {
	               try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					LogUtil.getLog(this.getClass()).error("create dept error :" + e.getMessage());
				}
	           }
    		   if (conn != null) {
                   conn.close();
               }
    	   }
        return true;
    }
    /**
     * 获取子节点数目
     * @param code
     * @return
     */
    public int getChildCountByCode(String code){
    	String sql = "select count(*) from department where parentCode=?";
    	 Conn conn = new Conn(connname);
    	 PreparedStatement ps = null;
    	 int count = 0;
    	 try{
    		 ps = conn.prepareStatement(sql);
	         ps.setString(1, code);
	         ResultSet rs =  conn.executePreQuery();
	         count = rs.getInt(1);
    	 }catch(Exception e){
    		 
    	 }finally{
    		 if (ps!=null) {
	               try {
						ps.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						LogUtil.getLog(this.getClass()).error("create dept error :" + e.getMessage());
					}
	           }
  		   if (conn != null) {
                 conn.close();
             }
    	 }
    	 return count;
    }
    /**
     * 初始化所有childcount
     * @return
     */
    public boolean initChildCount(){
    	boolean flag = false;
    		// 遍历部门，给childcount赋值
 			DeptDb allDeptDb = new DeptDb();
 			Vector vr = allDeptDb.list();
 			Iterator it = vr.iterator();
 			while (it.hasNext()) {
 				DeptDb tempDeptDb = (DeptDb) it.next();
 				tempDeptDb.setChildCount(getChildCountByCode(tempDeptDb.getCode()));
 				tempDeptDb.save();
 			}
 			flag = true;
 		
    	return flag;
	}
    /**
     * 根据部门名称获取部门编码
     * @param name
     * @return
     */
    public String getCodeByName(String name){
     String sql = "select code from department where name = ?";
   	 Conn conn = new Conn(connname);
   	 PreparedStatement ps = null;
   	 String code = "";
   	 try{
   		 ps = conn.prepareStatement(sql);
         ps.setString(1, name);
         ResultSet rs =  conn.executePreQuery();
         if (rs != null) {
            if (rs.next()) {
            	 code = rs.getString(1);
             }
         }
   	 }catch(Exception e){
   		 LogUtil.getLog(this.getClass()).error("getCodeByName error:" + e);
   	 }finally{
   		 if (ps!=null) {
	               try {
						ps.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						LogUtil.getLog(this.getClass()).error("create dept error :" + e.getMessage());
					}
	           }
 		   if (conn != null) {
                conn.close();
            }
   	 }
   	 return code;
    }
    /**
     * 中英文切换
     * @param request
     * @return
     */
    public String getName(HttpServletRequest request){
    	if(name.startsWith("#")){
    		return LocalUtil.LoadString(request, "res.ui.department",code);
    	}else{
    		return name;
    	}
    }
    
    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setShow(boolean show) {
		this.show = show;
	}

	public boolean isShow() {
		return show;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setAddDate(String addDate) {
		this.addDate = addDate;
	}

	public String getAddDate() {
		return addDate;
	}

	private boolean show = true;
	private String bcode;
	
	/**
	 * @return the bcode
	 */
	public String getBcode() {
		return bcode;
	}

	/**
	 * @param bcode the bcode to set
	 */
	public void setBcode(String bcode) {
		this.bcode = bcode;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public boolean isGroup() {
		return group;
	}

	/**
	 * @param hide the hide to set
	 */
	public void setHide(boolean hide) {
		this.hide = hide;
	}

	/**
	 * @return the hide
	 */
	public boolean isHide() {
		return hide;
	}

	private int id = -1;
}
