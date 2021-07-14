package com.redmoon.oa.netdisk;

import java.io.*;
import java.sql.*;
import java.util.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.log4j.*;

public class PublicAttachment implements java.io.Serializable {
    int id;
    String name;
    String diskName;
    String visualPath;
    String connname;

    String LOAD = "SELECT att_id, name, diskname, visualpath, orders, file_size, ext, create_date, public_dir, user_name FROM netdisk_public_attach WHERE id=?";
    String SAVE = "update netdisk_public_attach  set name=?, orders=?, public_dir= ?,diskname=?,ext=?,visualpath=?  WHERE id=?";
    transient Logger logger = Logger.getLogger(PublicAttachment.class.getName());

    public PublicAttachment() {
        connname = Global.getDefaultDB();
    }

    public PublicAttachment(int id) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Attachment:默认数据库名为空！");
        this.id = id;
        loadFromDb();
    }

    public boolean isExist(String name, String dirCode) {
    	String sql = "select id from netdisk_public_attach where public_dir=? and name=?";
    	try {
	    	JdbcTemplate jt = new JdbcTemplate();
	    	ResultIterator ri = jt.executeQuery(sql, new Object[]{dirCode, name});
	    	if (ri.hasNext())
	    		return true;
    	}
    	catch (SQLException e) {
    		LogUtil.getLog(getClass()).error(StrUtil.trace(e));
    	}
    	return false;
    }

    public PublicAttachment getPublicAttachment(int id) {
        return new PublicAttachment(id);
    }

    public boolean create() {
        String sql =
            "insert into netdisk_public_attach (att_id,name,diskname,visualpath,orders,file_size,ext,public_dir,create_date,user_name) values (?,?,?,?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attId);
            pstmt.setString(2, name);
            pstmt.setString(3, diskName);
            pstmt.setString(4, visualPath);
            pstmt.setInt(5, orders);
            // 取得文件的大小
            // File f = new File(fullPath);
            File f = new File(Global.getRealPath() + visualPath + "/" + diskName);
            if (f.exists())
                size = f.length();
            pstmt.setLong(6, size);
            pstmt.setString(7, ext);
            pstmt.setString(8, publicDir);
            pstmt.setTimestamp(9, new Timestamp(new java.util.Date().getTime()));
            pstmt.setString(10, userName);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }
    
    /**
     * 文件的链接是否已被共享
     * @param publicDir
     * @param attId
     * @return
     */
    public boolean isAttLinkShareExist(String publicDir, int attId) {
    	String sql = "select id from netdisk_public_attach where public_dir=? and att_id=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{publicDir, new Integer(attId)});
	    	if (ri.hasNext()) {
	    		return true;
	    	}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
    }
    
    /**
     * 删除目录下面的所有文件
     * @param publicDirCode
     */
    public void delOfDir(String publicDirCode) {
    	String sql = "select id from netdisk_public_attach where public_dir=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{publicDirCode});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				getPublicAttachment(rr.getInt(1)).del();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public boolean del() {
        String sql = "delete from netdisk_public_attach where id=?";
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // logger.info("del:id=" + id);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;
            pstmt.close();
        }
        catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        if (attId==0) {
	        // 非链接型,则删除文件
	        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	        String file_netdisk = cfg.get("file_netdisk_public");
	        String path = "";
	        if(visualPath!=null && !visualPath.equals("")){
	        	path = Global.getRealPath()+ file_netdisk + "/" + visualPath + "/" + diskName;
	        }else{
	        	path = Global.getRealPath()+ file_netdisk + "/" + diskName;
	        }
	        File fl = new File(path);
	        fl.delete();
        }
        return re;
    }

    public boolean save() {

        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(SAVE);
            pstmt.setString(1, name);
            pstmt.setInt(2, orders);
            pstmt.setString(3, publicDir);
            //lzm添加 文件重命名修改后缀以及物理文件名
            pstmt.setString(4,diskName);
            pstmt.setString(5, ext);
            pstmt.setString(6,visualPath);
            pstmt.setInt(7, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiskName() {
        return this.diskName;
    }

    public void setDiskName(String dn) {
        this.diskName = dn;
    }

    public String getVisualPath() {
        return this.visualPath;
    }

    public int getOrders() {
        return orders;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public long getSize() {
        return size;
    }

    public String getExt() {
        return ext;
    }

    public void setVisualPath(String vp) {
        this.visualPath = vp;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

   public String getIcon() {
       return Attachment.getIcon(ext);
   }

    public void loadFromDb() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                attId = rs.getInt(1);
                name = rs.getString(2);
                diskName = rs.getString(3);
                visualPath = StrUtil.getNullStr(rs.getString(4));
                orders = rs.getInt(5);
                size = rs.getInt(6);
                ext = rs.getString(7);
                createDate = rs.getTimestamp(8);
                publicDir = rs.getString(9);
                userName = StrUtil.getNullStr(rs.getString(10));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("loadFromDb:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String getUserName() {
        return userName;
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
                    result.addElement(getPublicAttachment(rs.getInt(1)));
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
                    PublicAttachment patt = getPublicAttachment(rs.getInt(1));
                    result.addElement(patt);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }
    public java.util.Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}

	public String getPublicDir() {
		return publicDir;
	}

	public void setPublicDir(String publicDir) {
		this.publicDir = publicDir;
	}

    private int orders = 0;
    private boolean loaded = false;
    private long size = 0;
    private String ext;
    private java.util.Date createDate;
	private String publicDir;
    private String userName;
    private int attId = 0;

	public int getAttId() {
		return attId;
	}

	public void setAttId(int attId) {
		this.attId = attId;
	}
}
