package com.redmoon.oa.netdisk;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import cn.js.fan.base.ITagSupport;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.Conn;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

public class PublicLeaf implements Serializable,ITagSupport {
    transient RMCache rmCache = RMCache.getInstance();
    String connname = "";
    transient Logger logger = Logger.getLogger(PublicLeaf.class.getName());

    int docId;

    public static final int TYPE_LIST = 2;  // 目录节点

    public static int TEMPLATE_NONE = -1;
    public static String ROOTCODE = "root";

    private String code = "", name = "", description = "", parent_code = "-1",
            root_code = "", add_date = "";
    private int orders = 1, layer = 1, child_count = 0, islocked = 0;
    final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,doc_id,template_id,pluginCode,mappingAddress from netdisk_public_directory where code=?";
    boolean isHome = false;
    final String dirCache = "NETDISK_PUBLIC_DIR";

    public String get(String field) {
        if (field.equals("code"))
            return getCode();
        else if (field.equals("name"))
            return getName();
        else if (field.equals("desc"))
            return getDescription();
        else if (field.equals("parent_code"))
            return getParentCode();
        else if (field.equals("root_code"))
            return getRootCode();
        else if (field.equals("layer"))
            return "" + getLayer();
        else
            return "";
    }

    public PublicLeaf() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:默认数据库名不能为空");
    }

    public PublicLeaf(String code) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:默认数据库名不能为空");
        this.code = code;
        loadFromDb();
    }

    public void renew() {
        if (logger==null) {
            logger = Logger.getLogger(PublicLeaf.class.getName());
        }
        if (rmCache==null) {
            rmCache = RMCache.getInstance();
        }
    }

    public String getFilePath() {
		// 取得文件虚拟路径
		String parentcode = getParentCode();
		PublicLeaf plf = new PublicLeaf();
		String filePath = getName();
		if (!parentcode.equals("-1")) {
			// 非根目录取节点名称
			filePath = getName();
			while (!parentcode.equals(ROOTCODE)) {
				plf = plf.getLeaf(parentcode);
				if (plf==null)
					break;
				parentcode = plf.getParentCode();
				filePath = plf.getName() + "/" + filePath;
			}
			// filePath = getRootCode() + "/" + filePath;
		}
		else
			filePath = "";

		return filePath;
    }
    
    public String getFullPath() {
		// 取得文件虚拟路径
		String parentcode = getParentCode();
		PublicLeaf plf = new PublicLeaf();
		String filePath = getName();
		if (!parentcode.equals("-1")) {
			if(parentcode.equals("root")){
				filePath = "";
			}else{
				// 非根目录取节点名称
				filePath = getName();
				while (!parentcode.equals(ROOTCODE)) {
					plf = plf.getLeaf(parentcode);
					if (plf==null)
						break;
					parentcode = plf.getParentCode();
					filePath = plf.getName() + "/" + filePath;
				}
				// filePath = getRootCode() + "/" + filePath;
			}
		}
		else
			filePath = "";

		return filePath;
    }
        
        
    
    public void loadFromDb() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(LOAD);
            ps.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                this.code = rs.getString(1);
                name = rs.getString(2);
                description = rs.getString(3);
                parent_code = rs.getString(4);
                root_code = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                child_count = rs.getInt(8);
                add_date = StrUtil.getNullStr(rs.getString(9));
                if (add_date.length()>=19)
                    add_date = add_date.substring(0, 19);
                islocked = rs.getInt(10);
                type = rs.getInt(11);
                isHome = rs.getInt(12) > 0 ? true : false;
                docId = rs.getInt(13);
                templateId = rs.getInt(14);
                pluginCode = rs.getString(15);
                mappingAddress = StrUtil.getNullStr(rs.getString(16));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("loadFromDb: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public int getDocID() {
        return docId;
    }

    public void setDocID(int d) {
        this.docId = d;
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
        this.root_code = c;
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
        this.parent_code = p;
    }

    public String getParentCode() {
        return this.parent_code;
    }

    public void setIsHome(boolean b) {
        this.isHome = b;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public String getRootCode() {
        return root_code;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public int getTemplateId() {
        return templateId;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getPluginCode() {
        return pluginCode;
    }

    public String getMappingAddress() {
        return mappingAddress;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector getChildren() {
        Vector v = new Vector();
        String sql = "select code from netdisk_public_directory where parent_code=? order by orders asc";
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
                    v.addElement(getLeaf(c));
                }
            }
        } catch (SQLException e) {
            logger.error("getChildren: " + e.getMessage());
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
    public Vector getAllChild(Vector vt, PublicLeaf leaf) throws ErrMsgException {
        Vector children = leaf.getChildren();
        if (children.isEmpty())
            return children;
        vt.addAll(children);
        Iterator ir = children.iterator();
        while (ir.hasNext()) {
            PublicLeaf lf = (PublicLeaf) ir.next();
            getAllChild(vt, lf);
        }
        // return children;
        return vt;
    }

    @Override
	public String toString() {
        return "PublicLeaf is " + name;
    }

    private int type;

    public synchronized boolean update() {
        String sql = "update netdisk_public_directory set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                     ",orders=" + orders + ",layer=" + layer + ",child_count=" + child_count + ",pluginCode=" + StrUtil.sqlstr(pluginCode) + ",mappingAddress=" + StrUtil.sqlstr(mappingAddress) +
                     " where code=" + StrUtil.sqlstr(code);
        // logger.info(sql);
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeFromCache(code);
                    //logger.info("cache is removed " + code);
                    //DirListCacheMgr更新
                    PublicLeafChildrenCacheMgr.remove(parent_code);
                }
            } catch (Exception e) {
                logger.error("update: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("update: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    /**
     * 更改了分类
     * @param newDirCode String
     * @return boolean
     */
    public synchronized boolean update(String newParentCode) {
        if (newParentCode.equals(parent_code))
            return false;
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        PublicLeaf lfparent = getLeaf(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update netdisk_public_directory set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                     ",parent_code=" + StrUtil.sqlstr(newParentCode) + ",orders=" + neworders +
                     ",layer=" + (parentLayer+1) +
                     " where code=" + StrUtil.sqlstr(code);

        String oldParentCode = parent_code;
        parent_code = newParentCode;
        RMConn conn = new RMConn(connname);

        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeFromCache(code);
                    removeFromCache(newParentCode);
                    removeFromCache(oldParentCode);
                    //DirListCacheMgr更新
                    PublicLeafChildrenCacheMgr.remove(oldParentCode);
                    PublicLeafChildrenCacheMgr.remove(newParentCode);

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from netdisk_public_directory where parent_code=" + StrUtil.sqlstr(oldParentCode) +
                          " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord)ri.next();
                        PublicLeaf clf = getLeaf(rr.getString(1));
                        clf.setOrders(clf.getOrders() - 1);
                        clf.update();
                    }

                    // 更新其所有子结点的layer
                    Vector vt = new Vector();
                    getAllChild(vt, this);
                    int childcount = vt.size();
                    Iterator ir = vt.iterator();
                    while (ir.hasNext()) {
                        PublicLeaf childlf = (PublicLeaf)ir.next();
                        int layer = parentLayer + 1 + 1;
                        String pcode = childlf.getParentCode();
                        while (!pcode.equals(code)) {
                            layer ++;
                            PublicLeaf lfp = getLeaf(pcode);
                            pcode = lfp.getParentCode();
                        }

                        childlf.setLayer(layer);
                        childlf.update();
                    }

                    // 将其原来的父结点的孩子数-1
                    PublicLeaf oldParentLeaf = getLeaf(oldParentCode);
                    oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
                    oldParentLeaf.update();

                    // 将其新父结点的孩子数 + 1
                    PublicLeaf newParentLeaf = getLeaf(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
                    newParentLeaf.update();
                }
            } catch (Exception e) {
                logger.error("update: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("update: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    public boolean AddChild(PublicLeaf childleaf) throws
            ErrMsgException {
        //计算得出插入结点的orders
        int childorders = child_count + 1;

        String updatesql = "";
        String insertsql = "insert into netdisk_public_directory (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date,mappingAddress) values (?,?,?,?,?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        try {
            // 更改根结点的信息
            updatesql = "Update netdisk_public_directory set child_count=child_count+1" +
                        " where code=" + StrUtil.sqlstr(code);
            conn.beginTrans();

            PreparedStatement ps = conn.prepareStatement(insertsql);
            ps.setString(1, childleaf.getCode());
            ps.setString(2, childleaf.getName());
            ps.setString(3, code);
            ps.setString(4, childleaf.getDescription());
            ps.setInt(5, childorders);
            ps.setString(6, root_code);
            ps.setInt(7, 0);
            ps.setInt(8, layer+1);
            ps.setInt(9, childleaf.getType());
            ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
            ps.setString(11, childleaf.getMappingAddress());
            conn.executePreUpdate();
            ps.close();

            conn.executeUpdate(updatesql);
            removeFromCache(code);
            conn.commit();
            
            // 加入默认权限 everyone
            PublicLeafPriv lp = new PublicLeafPriv();
            lp.add(childleaf.getCode());
            
            //lzm 
			PublicLeaf pl = getLeaf(childleaf.getCode());
			String filePath = pl.getFilePath();
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			filePath = Global.getRealPath() + cfg.get("file_netdisk_public") + "/"
					+ filePath;

			File f = new File(filePath);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
        } catch (SQLException e) {
            conn.rollback();
            logger.error("AddChild: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
    }
	/**
	 * 判断是否存在同名文件夹
	 * @param name
	 * @param parentCode
	 * @return
	 */
	
    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     * @param code String
     */
    public void removeFromCache(String code) {
        try {
            rmCache.remove(code, dirCache);
            PublicLeafChildrenCacheMgr.remove(code);
        } catch (Exception e) {
            logger.error("removeFromCache: " + e.getMessage());
        }
    }


    public void removeAllFromCache() {
        try {
            rmCache.invalidateGroup(dirCache);
            PublicLeafChildrenCacheMgr.removeAll();
        } catch (Exception e) {
            logger.error("removeAllFromCache: " + e.getMessage());
        }
    }

    public PublicLeaf getLeaf(String code) {
        PublicLeaf leaf = null;
        try {
            leaf = (PublicLeaf) rmCache.getFromGroup(code, dirCache);
            if (leaf == null) {
                leaf = new PublicLeaf(code);
                if (leaf!=null) {
                    if (!leaf.isLoaded())
                        leaf = null;
                    else
                        rmCache.putInGroup(code, dirCache, leaf);
                }
            }
            else {
                leaf.renew();
            }
        } catch (Exception e) {
            logger.error("getLeaf: " + e.getMessage());
        }
        return leaf;
    }

    public boolean delsingle(PublicLeaf leaf) {
        RMConn rmconn = new RMConn(connname);
        try {
            String sql = "delete from netdisk_public_directory where code=" + StrUtil.sqlstr(leaf.getCode());
            boolean r = rmconn.executeUpdate(sql) == 1 ? true : false;
            sql = "update netdisk_public_directory set orders=orders-1 where parent_code=" + StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" + leaf.getOrders();
            rmconn.executeUpdate(sql);
            sql = "update netdisk_public_directory set child_count=child_count-1 where code=" + StrUtil.sqlstr(leaf.getParentCode());
            rmconn.executeUpdate(sql);

            // removeFromCache(leaf.getCode());
            // removeFromCache(leaf.getParentCode());
            removeAllFromCache();

            // 删除该目录下的所有文章            
            PublicAttachment patt = new PublicAttachment();
            patt.delOfDir(leaf.getCode());
            
            // 删除目录对应的权限
            PublicLeafPriv plp = new PublicLeafPriv(leaf.getCode());
            plp.delPrivsOfDir();
            
            // 删除磁盘目录
			String filePath = leaf.getFilePath();
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			filePath = Global.getRealPath() + cfg.get("file_netdisk_public") + "/"
					+ filePath;
			File f = new File(filePath);
			if (f.isDirectory()) {
				f.delete();
			}
        } catch (SQLException e) {
            logger.error("delsingle: " + e.getMessage());
            return false;
        }
        return true;
    }
    /**
     * 递归删除 文件夹下所有附件
     * @param leaf
     */
    public void del(PublicLeaf leaf) {
        Iterator children = leaf.getChildren().iterator();
        while (children.hasNext()) {
            PublicLeaf lf = (PublicLeaf) children.next();
            del(lf);
        }
        delsingle(leaf);
    }

    public PublicLeaf getBrother(String direction) {
        String sql;
        RMConn rmconn = new RMConn(connname);
        PublicLeaf bleaf = null;
        try {
            if (direction.equals("down")) {
                sql = "select code from netdisk_public_directory where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from netdisk_public_directory where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders - 1);
            }

            ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                bleaf = getLeaf(rr.getString(1));
            }
        } catch (SQLException e) {
            logger.error("getBrother: " + e.getMessage());
        }
        return bleaf;
    }

    public boolean move(String direction) {
        String sql = "";

        // 取出该结点的移动方向上的下一个兄弟结点的orders
        boolean isexist = false;

        PublicLeaf bleaf = getBrother(direction);
        if (bleaf != null) {
            isexist = true;
        }

        //如果移动方向上的兄弟结点存在则移动，否则不移动
        if (isexist) {
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();
                if (direction.equals("down")) {
                    sql = "update netdisk_public_directory set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update netdisk_public_directory set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }

                if (direction.equals("up")) {
                    sql = "update netdisk_public_directory set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update netdisk_public_directory set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                conn.commit();
                removeFromCache(code);
                removeFromCache(bleaf.getCode());
            } catch (Exception e) {
                conn.rollback();
                logger.error("move: " + e.getMessage());
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
    /**
     * 文件夹打包
     * @param basePath
     * @param zipPath
     * @return
     * @throws ErrMsgException 
     */
    public HashMap<String,String> listFilesForZip(String publicDirCode){
    	HashMap<String,String> paths = new HashMap<String, String>();
    	PublicLeaf pl = new PublicLeaf(publicDirCode);
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    	String filePublicPath = Global.getRealPath() + cfg.get("file_netdisk_public") + "/";
    	String filePath = Global.getRealPath() + cfg.get("file_netdisk") + "/";
    	Vector vt = new Vector();
    	try {
			vt = pl.getAllChild(vt, pl);
			if(vt!=null){
				vt.add(pl);
				Iterator ir = vt.iterator();
				while(ir.hasNext()){
					PublicLeaf plChild = (PublicLeaf)ir.next();
					String sql = "select id from netdisk_public_attach where public_dir="+StrUtil.sqlstr(plChild.getCode())+" order by create_date desc";
					PublicAttachment publicAtt = new PublicAttachment();
			    	Vector attVec = publicAtt.list(sql);
			    	if(attVec!=null && attVec.size()>0){
			    		Iterator attIr = attVec.iterator();
			    		while(attIr.hasNext()){
			    			PublicAttachment pa = (PublicAttachment)attIr.next();
			    			if(pa.getAttId()==0){
								paths.put(filePublicPath+pa.getVisualPath()+"/"+pa.getDiskName(), pa.getVisualPath()+"/"+pa.getDiskName());
							}else{
								Attachment attach = new Attachment(pa.getAttId());
								paths.put(filePath+attach.getVisualPath()+"/"+attach.getDiskName(), attach.getVisualPath()+"/"+attach.getDiskName());
							}
			    		}
			    	}
				}
				
			}
			
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			logger.error("ErrMsgException"+e.getMessage());
			//throw new ErrMsgException(e.getMessage());
		}
    	
    	return paths;
    }
    
    public HashMap<String,String>  listFileForZipByMapping(String rootPath,File file,HashMap<String,String> hashmap){
    		if(file.exists()){
    			File[] files = file.listFiles();
        		if (files == null) {
        			return null;
        		}
    			for(File fileChild:files){
    				if(fileChild.isDirectory()){
    					listFileForZipByMapping(rootPath,fileChild,hashmap);
    				}else{
    					String absolutePath = fileChild.getAbsolutePath();
    					int pathIndex = rootPath.length();
    					int index = absolutePath.length();
    					hashmap.put(absolutePath,absolutePath.substring(pathIndex,index));
    				}
    				
    			}
    			
    		}
    		return hashmap;
    }
    /**
     * lzm添加
     * @return
     */
    public static String getAutoCode(){
    	//自动生成20位的id
    	return RandomSecquenceCreator.getId(20);
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public void setMappingAddress(String mappingAddress) {
        this.mappingAddress = mappingAddress;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    private int templateId = -1;
    private boolean loaded = false;
    private String pluginCode;
    private String mappingAddress;
    private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	

    

}
