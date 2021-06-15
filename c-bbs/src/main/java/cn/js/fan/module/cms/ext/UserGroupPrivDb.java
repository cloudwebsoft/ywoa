package cn.js.fan.module.cms.ext;

import com.cloudwebsoft.framework.base.QObjectDb;
import cn.js.fan.db.PrimaryKey;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.module.cms.Config;
import java.util.Iterator;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UserGroupPrivDb extends QObjectDb {
    public static final String ALLDIR = "alldir";

    public UserGroupPrivDb() {
    }

    public boolean init(String groupCode, String boardCode) {
        Config cfg = new Config();
        // 发表贴子 回复贴子
        String attach_download = cfg.getProperty("cms.canUserDownloadAttach").
                                 equals("true") ? "1" : "0";
        String view_doc = cfg.getProperty("cms.canUserViewDoc").equals("true")?"1":"0";
        boolean re = false;
        try {
            re = create(new JdbcTemplate(), new Object[] {groupCode,boardCode,"1",attach_download,"1",view_doc,"",new Integer(0)});
        }
        catch (ResKeyException e) {
            // LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new IllegalAccessError(e.getMessage());
        }
        return re;
    }

    /**
     * 删除dirCode目录上所有论坛用户组的权限
     * @param dirCode String
     */
    public void delUserGroupPrivOfDir(String dirCode) {
        String sql = "select group_code, dir_code from " + getTable().getName() + " where dir_code=?";
        Iterator ir = list(sql, new Object[]{dirCode}).iterator();
        while (ir.hasNext()) {
            UserGroupPrivDb ugpd = (UserGroupPrivDb)ir.next();
            try {
                ugpd.del();
            }
            catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error("delUserGroupPrivOfDir:" + StrUtil.trace(e));
            }
        }
    }

    /**
     * 检查用户组的权限是否已被指定，暂时无用
     * @param groupCode String
     * @param dirCode String
     * @return boolean
     */
    public boolean isUserGropuPrivDbCreated(String groupCode, String dirCode) {
        PrimaryKey pk = (PrimaryKey) primaryKey.clone();
        pk.setKeyValue("group_code", groupCode);
        pk.setKeyValue("dir_code", dirCode);
        UserGroupPrivDb ugp = (UserGroupPrivDb) getQObjectDb(pk.getKeys());
        if (ugp == null) {
            return false;
        } else
            return true;
    }

    /**
     * 取得用户组在dirCode节点上的权限，如果没有初始化过，则进行节点上权限的初始化
     * @param groupCode String
     * @param dirCode String
     * @return UserGroupPrivDb
     */
    public UserGroupPrivDb getUserGroupPrivDb(String groupCode, String dirCode) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("group_code", groupCode);
        pk.setKeyValue("dir_code", dirCode);
        UserGroupPrivDb ugp = (UserGroupPrivDb)getQObjectDb(pk.getKeys());
        // LogUtil.getLog(getClass()).info("getUserGroupPrivDb:" + primaryKey);
        if (ugp==null) {
            init(groupCode, dirCode);
            return (UserGroupPrivDb)getQObjectDb(pk.getKeys());
        }
        else
            return ugp;
    }

}
