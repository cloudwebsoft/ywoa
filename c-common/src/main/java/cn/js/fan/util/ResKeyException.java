package cn.js.fan.util;

import java.sql.SQLException;

import cn.js.fan.base.ISkin;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;

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
public class ResKeyException extends Exception{
    public ResKeyException(ISkin iSkin, String key) {
        this.iSkin = iSkin;
        this.key = key;
    }

    public ResKeyException(String res, String key) {
        this.res = res;
        this.key = key;
    }

    public ResKeyException(String res, String key, Object[] args) {
        this.res = res;
        this.key = key;
        this.args = args;
    }

    public ResKeyException(String key) {
        this.key = key;
    }
    
    public ResKeyException(String key, SQLException e) {
        this.key = key;
        this.sqlException = e;
    }    

    @Override
    public String getMessage() {
        return "Error key= " + key;
    }

    public String getMessage(HttpServletRequest request) {
        if (res!=null) {
            if (args==null) {
                String str = SkinUtil.LoadString(request, res, key);
                if ("".equals(str)) {
                    return key;
                }
                else {
                    return str;
                }
            }
            else {
                String str = SkinUtil.LoadString(request, res, key);
                return StrUtil.format(str, args);
            }
        }
        else if (iSkin!=null) {
            return iSkin.LoadStr(request, key);
        }
        else {
            String str = SkinUtil.LoadString(request, key);
            if ("".equals(str)) {
                return key;
            }
            else {
                return str;
            }
        }
    }

    public void setISkin(ISkin iSkin) {
        this.iSkin = iSkin;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public ISkin getISkin() {
        return iSkin;
    }

    public String getKey() {
        return key;
    }

    public String getRes() {
        return res;
    }

    public void setSqlException(SQLException sqlException) {
		this.sqlException = sqlException;
	}

	public SQLException getSqlException() {
		return sqlException;
	}

	private ISkin iSkin;
    private String key;
    private String res;
    private Object[] args;
    
    private SQLException sqlException;
}
