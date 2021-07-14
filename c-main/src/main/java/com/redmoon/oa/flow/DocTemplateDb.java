package com.redmoon.oa.flow;

import java.io.File;
import java.sql.*;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.base.IObjectDb;
import com.cloudwebsoft.framework.base.ObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;

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
public class DocTemplateDb extends ObjectDb {
    private int id;

    public static final String KIND_DEFAULT = "default";

    public static final String linkBasePath = "file_flow_doc_template";
    
    private String depts;

    public DocTemplateDb() {
        super();
    }

    public DocTemplateDb(int id) {
        this.id = id;
        init();
        load(new JdbcTemplate());
    }

    private String kind;

    public DocTemplateDb getDocTemplateDb(int id) {
        return (DocTemplateDb)getObjectDb(new Integer(id));
    }

    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new DocTemplateDb(pk.getIntValue());
    }

    public String getFileUrl(HttpServletRequest request) {
        // String rootPath = Global.getFullRootPath(request);
        String rootPath = request.getContextPath();
        String attachmentBasePath = rootPath + "/upfile/" +
                                    linkBasePath + "/";
        return attachmentBasePath + StrUtil.UrlEncode(fileName);
    }

    public void initDB() {
        this.tableName = "flow_doc_template";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new DocTemplateCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT INTO " + tableName + " (title,file_name,sort, depts, unit_code) VALUES (?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT title,file_name,sort,depts,unit_code FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET title=?,file_name=?,sort=?,depts=? WHERE id=?";
        this.QUERY_LIST = "select id from " + tableName + " order by sort";
        isInitFromConfigDB = false;
    }

    public boolean save(JdbcTemplate jt) {
        // Based on the id in the object, get the message data from the database.
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_SAVE, new Object[] {title, fileName, new Integer(sort), depts, new Integer(id)})==1;
            if (re) {
                DocTemplateCache mc = new DocTemplateCache(this);
                primaryKey.setValue(new Integer(id));
                mc.refreshSave(primaryKey);
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        return re;
    }

    public boolean save(FileUpload fu) throws ErrMsgException {
        String newFileName = writeFile(fu);
        /*
        if (!newFileName.equals("")) {
            // 如果上传了文件，则删除原来的文件
            delFile();
            fileName = newFileName;
        }
        */
        return save(new JdbcTemplate());
    }

    public void load(JdbcTemplate jt) {
        // Based on the id in the object, get the message data from the database.
        try {
            ResultIterator ri = jt.executeQuery(this.QUERY_LOAD, new Object[] {new Integer(id)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                this.title = rr.getString(1);
                this.fileName = StrUtil.getNullString(rr.getString(2));
                this.sort = rr.getInt(3);
                depts = StrUtil.getNullStr(rr.getString(4));
                unitCode = StrUtil.getNullStr(rr.getString(5));
                primaryKey.setValue(new Integer(id));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
    }

    public boolean del(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_DEL, new Object[] {new Integer(id)})==1;
            if (re) {
                delFile();
                DocTemplateCache mc = new DocTemplateCache(this);
                mc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return re;
    }

    public void delFile() {
        if (fileName != null && !fileName.equals("")) {
            try {
                File file = new File(Global.realPath + "upfile/" + linkBasePath +
                                     "/" + fileName);
                file.delete();
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
    }

    public String writeFile(FileUpload fu) throws ErrMsgException {
        if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0)
                fi = (FileInfo) v.get(0);
            if (fi != null) {
                // 置保存路径
                String filepath = Global.getRealPath() + "upfile/" + linkBasePath + "/";
                // 置本地路径
                fu.setSavePath(filepath);
                // 使用原来的名称写入磁盘
                if (fileName.equals(""))
                    fi.write(filepath, true); // 中文路径可能weboffice控件不支持，所以不使用文件本来的名称
                else
                    fi.write(filepath, fileName);
                return fi.getDiskName();
            }
        }
        return "";
    }

    public boolean create(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_CREATE, new Object[] {title,
                                  fileName, new Integer(sort), depts, unitCode}) == 1;
            if (re) {
                DocTemplateCache mc = new DocTemplateCache(this);
                mc.refreshCreate();
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        return re;
    }

    public boolean create(FileUpload fu) throws ErrMsgException {
        fileName = writeFile(fu);
        if (fileName.equals("")) {
            throw new ErrMsgException("请选择文件！");
        }
        return create(new JdbcTemplate());
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getSort() {
        return sort;
    }

    public String getKind() {
        return kind;
    }
    
    public String getFileName() {
    	return fileName;
    }

    public void setDepts(String depts) {
		this.depts = depts;
	}

	public String getDepts() {
		return depts;
	}

	private String title;
    private String fileName = "";
    private int sort;
    private String unitCode;

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}
}
