package com.redmoon.forum.person;

import java.sql.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.IPUtil;

public class UserGroupDb extends ObjectDb {
    public static final String ALL = "all"; // 全部用户，仅用于CMS中

    public static final String EVERYONE = "everyone";
    public static final String GUEST = "guest";

    private String code;
    private String desc;

    public String QUERY_GET_GUEST_USERGROUP_BY_IP;

    public UserGroupDb() {
        init();
    } 

    public UserGroupDb(String code) {
        this.code = code;
        load(new JdbcTemplate(new DataSource()));
        init();
    }

    public UserGroupDb(String code, String desc) {
        init();

        this.code = code;
        this.desc = desc;
    }

    public void initDB() {
        this.tableName = "sq_user_group";
        primaryKey = new PrimaryKey("code", PrimaryKey.TYPE_STRING);
        objectCache = new UserGroupCache(this);

        QUERY_CREATE = "insert into sq_user_group (code, description, display_order,ip_begin,ip_end,is_guest) values (?,?,?,?,?,?)";
        QUERY_SAVE = "update sq_user_group set description=?,display_order=?,ip_begin=?,ip_end=?,is_guest=? where code=?";
        QUERY_LIST = "select code from sq_user_group order by display_order";
        QUERY_DEL = "delete from sq_user_group where code=?";
        QUERY_LOAD = "select code, description, isSystem, display_order,ip_begin,ip_end,is_guest from sq_user_group where code=?";

        QUERY_GET_GUEST_USERGROUP_BY_IP = "select code from " + tableName + " where is_guest=1 and ip_begin<>0 and ip_end<>0";

        isInitFromConfigDB = false;
    }

    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserGroupDb(pk.getStrValue());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String c) {
        code = c;
    }

    public String getDesc() {
        if (code.equals(ALL)) {
            return "全部用户";
        }
        else
            return desc;
    }

    public boolean isSystem() {
        return system;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public long getIpBegin() {
        return ipBegin;
    }

    public long getIpEnd() {
        return ipEnd;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setIpBegin(long ipBegin) {
        this.ipBegin = ipBegin;
    }

    public void setIpEnd(long ipEnd) {
        this.ipEnd = ipEnd;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    private boolean system = false;

    public boolean create(JdbcTemplate jt) throws ResKeyException {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_CREATE, new Object[] {code, desc, new Integer(displayOrder), new Long(ipBegin), new Long(ipEnd), new Integer(guest?1:0)}) == 1;
        }
        catch (SQLException e) {
            logger.info("create:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        }
        UserGroupCache ugc = new UserGroupCache(this);
        ugc.refreshCreate();
        return re;
    }

    public void load(JdbcTemplate jt) {
        try {
            ResultIterator ri = jt.executeQuery(QUERY_LOAD, new Object[] {code});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                code = rr.getString(1);
                desc = StrUtil.getNullStr(rr.getString(2));
                system = rr.getInt(3)==1;
                displayOrder = rr.getInt(4);
                ipBegin = rr.getLong(5);
                ipEnd = rr.getLong(6);
                guest = rr.getInt(7)==1;
                loaded = true;
                primaryKey.setValue(code);
            }
        }
        catch (SQLException e) {
            logger.info("load:" + e.getMessage());
        }
    }

    public boolean save(JdbcTemplate jt) throws ResKeyException {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_SAVE, new Object[] {desc,  new Integer(displayOrder), new Long(ipBegin), new Long(ipEnd), new Integer(guest?1:0), code}) == 1;
        } catch (SQLException e) {
            logger.info("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        }
        UserGroupCache uc = new UserGroupCache(this);
        primaryKey.setValue(code);
        uc.refreshSave(primaryKey);
        if (guest)
            uc.refreshList();
        return re;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        if (isSystem())
            return false;
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_DEL, new Object[] {code}) == 1;
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        }
        UserGroupCache uc = new UserGroupCache(this);
        primaryKey.setValue(code);
        uc.refreshDel(primaryKey);

        // @task:删除CMS中的权限

        return re;
    }

    public IObjectDb getObjectDb(Object primaryKeyValue) {
        UserGroupCache uc = new UserGroupCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (UserGroupDb)uc.getObjectDb(pk);
    }

    public UserGroupDb getUserGroupDb(String code) {
        return (UserGroupDb)getObjectDb(code);
    }

    /**
     * 根据IP取得用户所属的组,@task需优化
     * @param ip String
     * @return String
     */
    public String getGuestGroupCodeByIP(String ip) {
        int count = getObjectCount(QUERY_GET_GUEST_USERGROUP_BY_IP);
        if (count==0)
            return GUEST;
        /*
        String[] ary = StrUtil.split(ip, "\\.");
        String u = "";
        int len = ary.length;
        for (int i=0; i<len; i++) {
            if (!ary[i].equals("*")) {
                if (ary[i].length()<3)
                    ary[i] = StrUtil.PadString(ary[i], '0', 3, true);
            } else {
                ary[i] = "000";
            }
            u += ary[i];
        }
        */

        long ul = IPUtil.ip2long(ip);
        if (ul==0) // 如果IP地址为IPV6
            return GUEST;

        ObjectBlockIterator ir = getObjects(QUERY_GET_GUEST_USERGROUP_BY_IP, 0, count);
        while (ir.hasNext()) {
            UserGroupDb ug = (UserGroupDb)ir.next();
            long begin = ug.getIpBegin();
            long end = ug.getIpEnd();
            /*
            String[] ary1 = StrUtil.split(begin, "\\.");
            String b = "";
            len = ary1.length;
            for (int i=0; i<len; i++) {
                if (!ary1[i].equals("*")) {
                    if (ary1[i].length()<3)
                        ary1[i] = StrUtil.PadString(ary1[i], '0', 3, true);
                } else {
                    ary1[i] = "000";
                }
                b += ary1[i];
            }

            String[] ary2 = StrUtil.split(end, "\\.");
            len = ary1.length;
            String e = "";
            for (int i=0; i<len; i++) {
                if (!ary2[i].equals("*")) {
                    if (ary2[i].length()<3)
                        ary2[i] = StrUtil.PadString(ary2[i], '0', 3, true);
                } else {
                    ary2[i] = "000";
                }
                e += ary2[i];
            }
            */

            // System.out.println(getClass() + " u=" + u + " b=" + b + " e=" + e);
            if (ul >= begin && ul <= end) {
                return ug.getCode();
            }
        }
        return GUEST;
    }

    private int displayOrder = 0;
    private long ipBegin;
    private long ipEnd;
    private boolean guest = false;
}
