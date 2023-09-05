package com.redmoon.oa.flow;

import java.io.File;
import java.sql.*;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.base.IObjectDb;
import com.cloudwebsoft.framework.base.ObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
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
        return (DocTemplateDb)getObjectDb(id);
    }

    @Override
    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new DocTemplateDb(pk.getIntValue());
    }

    public String getFileUrl(HttpServletRequest request) {
        return request.getContextPath() + "/flow/getTemplateFile.do?id=" + id;
    }

    @Override
    public void initDB() {
        this.tableName = "flow_doc_template";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new DocTemplateCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT INTO " + tableName + " (title,file_name,sort, depts, unit_code, file_name_tail) VALUES (?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT title,file_name,sort,depts,unit_code,file_name_tail FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET title=?,file_name=?,sort=?,depts=?,file_name_tail=? WHERE id=?";
        this.QUERY_LIST = "select id from " + tableName + " order by sort";
        isInitFromConfigDB = false;
    }

    @Override
    public boolean save(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_SAVE, new Object[] {title, fileName, sort, depts, fileNameTail, id})==1;
            if (re) {
                DocTemplateCache mc = new DocTemplateCache(this);
                primaryKey.setValue(id);
                mc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        return re;
    }

    public boolean save(FileUpload fu) throws ErrMsgException {
        writeFile(fu);
        return save(new JdbcTemplate());
    }

    @Override
    public void load(JdbcTemplate jt) {
        try {
            ResultIterator ri = jt.executeQuery(this.QUERY_LOAD, new Object[] {id});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                this.title = rr.getString(1);
                this.fileName = StrUtil.getNullString(rr.getString(2));
                this.sort = rr.getInt(3);
                depts = StrUtil.getNullStr(rr.getString(4));
                unitCode = StrUtil.getNullStr(rr.getString(5));
                fileNameTail = StrUtil.getNullStr(rr.getString(6));
                primaryKey.setValue(id);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
    }

    @Override
    public boolean del(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_DEL, new Object[] {id})==1;
            if (re) {
                delFile();
                DocTemplateCache mc = new DocTemplateCache(this);
                mc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return re;
    }

    public void delFile() {
        IFileService fileService = SpringUtil.getBean(IFileService.class);
        if (fileName != null && !"".equals(fileName)) {
            fileService.del("upfile/" + linkBasePath, fileName);
        }
        if (fileNameTail != null && !"".equals(fileNameTail)) {
            fileService.del("upfile/" + linkBasePath, fileNameTail);
        }
    }

    public String writeFile(FileUpload fu) {
        if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector<FileInfo> v = fu.getFiles();
            if (v!=null && v.size()>0) {
                for (FileInfo fi : v) {
                    IFileService fileService = SpringUtil.getBean(IFileService.class);
                    if ("filename".equals(fi.getFieldName())) {
                        // 使用原来的名称写入磁盘
                        if ("".equals(fileName)) {
                            // 中文路径可能weboffice控件不支持，所以不使用文件本来的名称
                            fileService.write(fi, "upfile/" + linkBasePath);
                        } else {
                            fi.setName(fileName);
                            fileService.write(fi, "upfile/" + linkBasePath, false);
                        }
                        fileName = fi.getDiskName();
                    } else {
                        // 使用原来的名称写入磁盘
                        if ("".equals(fileNameTail)) {
                            // 中文路径可能weboffice控件不支持，所以不使用文件本来的名称
                            fileService.write(fi, "upfile/" + linkBasePath);
                        } else {
                            fi.setName(fileNameTail);
                            fileService.write(fi, "upfile/" + linkBasePath, false);
                        }
                        fileNameTail = fi.getDiskName();
                    }
                }
                return fileName;
            }
        }
        return "";
    }

    @Override
    public boolean create(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_CREATE, new Object[] {title,
                                  fileName, sort, depts, unitCode, fileNameTail,}) == 1;
            if (re) {
                DocTemplateCache mc = new DocTemplateCache(this);
                mc.refreshCreate();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        return re;
    }

    public boolean create(FileUpload fu) throws ErrMsgException {
        fileName = writeFile(fu);
        if ("".equals(fileName)) {
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
    private String fileNameTail = "";
    private int sort;
    private String unitCode;

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

    public String getFileNameTail() {
        return fileNameTail;
    }

    public void setFileNameTail(String fileNameTail) {
        this.fileNameTail = fileNameTail;
    }
}
