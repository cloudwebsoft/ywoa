package com.redmoon.forum.person;

import cn.js.fan.base.ObjectBlockIterator;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.redmoon.blog.UserConfigDb;
import com.redmoon.forum.*;
import com.redmoon.forum.setup.UserLevelDb;
import com.redmoon.forum.treasure.TreasureUserDb;
import com.redmoon.forum.util.ForumFileUpload;
import com.redmoon.forum.util.ForumFileUtil;
import com.redmoon.kit.util.FileInfo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

public class UserDb extends ObjectDb {
    String name, pwdMd5, realName, email;
    int experience, credit, addCount, delCount;
    String realPic;
    private String gender;
    private Date regDate;
    private boolean mobileValid;
    
    private int provinceId, cityId, countryId;

    public int getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public static final int CHECK_STATUS_NOT = 0;
    public static final int CHECK_STATUS_PASS = 1;

    public static final String myfaceBasePath = "myface";

    public static final String ADMIN = "Administrator";

    public UserDb(String name) {
        this.name = name;
        init();
        load();
    }

    public UserDb() {
        init();
    }

    public String getRealPic() {
        return this.realPic;
    }

    public void setRealPic(String r) {
        this.realPic = r;
    }

    public ObjectBlockIterator getUsers(String query,
                                        int startIndex,
                                        int endIndex) {
        // if (!SecurityUtil.isValidSql(query))
        //     return null;
        //可能取得的infoBlock中的元素的顺序号小于endIndex
        Object[] docBlock = getObjectBlock(query, startIndex);

        return new ObjectBlockIterator(this, docBlock, query,
                                       startIndex, endIndex);
    }

    /**
     * 用于创建用户，如与其它系统集成时，可用来同步帐号
     * @param name String
     * @param pwd String
     * @param nick String
     * @param gender String
     * @param check_status int
     * @return boolean
     */
    public boolean create(String name, String pwd, String nick, String gender,
                          int check_status) {
        String sql = "insert into sq_user (name,nick,pwd,rawPwd,timeZone,Gender,RegDate,check_status,diskSpaceAllowed,RealPic,province_id,city_id,country_id) values (?,?,?,?,?,?,?,?,?,'face.gif',?,?,?)";
        boolean re = false;
        String pwdMD5 = "";
        try {
            pwdMD5 = SecurityUtil.MD5(pwd);
        } catch (Exception e) {
            logger.error("regist:" + e.getMessage());
        }
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, nick);
            ps.setString(3, pwdMD5);
            ps.setString(4, pwd);
            ps.setString(5, "GMT+08:00");
            ps.setString(6, gender);
            ps.setString(7, "" + new java.util.Date().getTime());
            ps.setInt(8, check_status);
            ps.setInt(9, provinceId);
            ps.setInt(10, cityId);
            ps.setInt(11, countryId);

            Config cfg = Config.getInstance();
            diskSpaceAllowed = StrUtil.toLong(cfg.getProperty("forum.defaultDiskSpaceAllowed"));

            ps.setLong(9, diskSpaceAllowed);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            UserCache uc = new UserCache(this);
            uc.refreshCreate();
            // 更新论坛总人数
            ForumDb fd = new ForumDb();
            fd = fd.getForumDb();
            fd.setUserCount(fd.getUserCount() + 1);
            fd.setUserNew(name);
            fd.save();
        }
        return re;
    }

    public boolean create() throws ErrMsgException {
        UserDb user = getUserDbByNick(name);
        if (user != null && user.isLoaded()) {
            throw new ErrMsgException("该用户名已被注册,请选择新的用户名!");
        }

        String tzID = timeZone.getID();

        boolean isvalid = false;
        int srt = secret ? 1 : 0;
        // name已事先指定，如在OA中，则不使用ID
        if (name == null || name.equals("")) {
            name = "" + SequenceMgr.nextID(SequenceMgr.SQ_USER);
        }
        PreparedStatement ps = null;
        String sql =
                "insert into sq_user (name,pwd,Question,Answer,RealName,Career,Gender,Job, Birthday," +
                "Marriage, Phone, Mobile, State, City, Address, PostCode, IDCard," +
                "RealPic, Hobbies, Email,OICQ,RegDate,sign,experience,credit,gold,diskSpaceAllowed,diskSpaceUsed,isSecret,IP,rawPwd,timeZone,home,msn,lastTime,curTime,locale,nick,check_status,fetion,can_rename) values (" +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, pwdMd5);
            ps.setString(3, question);
            ps.setString(4, answer);
            ps.setString(5, realName);
            ps.setString(6, career);
            ps.setString(7, gender);
            ps.setString(8, job);
            if (birthday==null)
                ps.setString(9, "");
            else
                ps.setString(9, "" + birthday.getTime());
            ps.setInt(10, marriage);
            ps.setString(11, phone);
            ps.setString(12, mobile);
            ps.setString(13, state);
            ps.setString(14, city);
            ps.setString(15, address);
            ps.setString(16, postCode);
            ps.setString(17, IDCard);
            ps.setString(18, realPic);
            ps.setString(19, hobbies);
            ps.setString(20, email);
            ps.setString(21, oicq);
            ps.setString(22, "" + System.currentTimeMillis());
            ps.setString(23, sign);
            ps.setInt(24, experience);
            ps.setInt(25, credit);
            ps.setInt(26, gold);

            Config cfg = Config.getInstance();
            diskSpaceAllowed = StrUtil.toLong(cfg.getProperty("forum.defaultDiskSpaceAllowed"));

            ps.setLong(27, diskSpaceAllowed);
            ps.setLong(28, diskSpaceUsed);
            ps.setInt(29, srt);
            ps.setString(30, ip);
            ps.setString(31, rawPwd);
            ps.setString(32, tzID);
            ps.setString(33, home);
            ps.setString(34, msn);
            ps.setString(35, "" + System.currentTimeMillis());
            ps.setString(36, "" + System.currentTimeMillis());
            ps.setString(37, locale);
            ps.setString(38, nick);
            ps.setInt(39, checkStatus);
            ps.setString(40, fetion);
            ps.setInt(41, canRename?1:0);
            isvalid = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            isvalid = false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (isvalid) {
            UserCache uc = new UserCache(this);
            uc.refreshCreate();
            // 更新论坛总人数
            ForumDb fd = new ForumDb();
            fd = fd.getForumDb();
            fd.setUserCount(fd.getUserCount() + 1);
            fd.setUserNew(name);
            fd.save();
        }
        return isvalid;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        UserCache uc = new UserCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (UserDb) uc.getObjectDb(pk);
    }

    public boolean del() throws ResKeyException, ErrMsgException {
        boolean re = false;
        // 删除博客
        UserConfigDb ucd = new UserConfigDb();
        long blogId = ucd.getBlogIdByUserName(name);
        if (blogId != UserConfigDb.NO_BLOG) {
            ucd = ucd.getUserConfigDb(blogId);
            ucd.del();
        }
        // 删除用户的贴子
        MsgDb md = new MsgDb();
        md.delMessagesOfUser(name);

        // 删除好友列表
        UserFriendDb ufd = new UserFriendDb();
        ufd.delFriendsOfUser(name);
        // 删除版主
        BoardManagerDb bmd = new BoardManagerDb();
        bmd.delManager(name);
        // 删除财富
        TreasureUserDb tu = new TreasureUserDb();
        tu.delTreasureOfUser(name);

        // 更新会员总数
        ForumDb fd = ForumDb.getInstance();
        fd.setUserCount(fd.getUserCount() - 1);
        fd.save();

        // 删除权限
        UserPrivDb upd = new UserPrivDb();
        upd = upd.getUserPrivDb(name);
        upd.del();

        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setString(1, name);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("del:" + e.getMessage());
        } finally {
            UserCache uc = new UserCache(this);
            primaryKey.setValue(name);
            uc.refreshDel(primaryKey);
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public int getObjectCount(String sql) {
        UserCache uc = new UserCache(this);
        return uc.getObjectCount(sql);
    }

    public int getUserCount(String sql) {
        return getObjectCount(sql);
    }

    public Object[] getObjectBlock(String query, int startIndex) {
        UserCache dcm = new UserCache(this);
        return dcm.getObjectBlock(query, startIndex);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserDb(pk.getStrValue());
    }

    public void setQueryCreate() {
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
                "update sq_user set pwd=?,Question=?,Answer=?,realname=?," +
                "Career=?,Gender=?,Job=?," +
                "Birthday=?,Marriage=?,Phone=?," +
                "Mobile=?,State=?,City=?,Address=?," +
                "PostCode=?,IDCard=?,RealPic=?,Hobbies=?," +
                "Email=?,OICQ=?,sign=?,myface=?," +
                "experience=?,credit=?,addcount=?," +
                "delcount=?,arrestday=?,arrestreason=?,arresttime=?,arrestpolice=?,ispolice=?,lastTime=?,curTime=?,eliteCount=?,gold=?,favoriate=?,isValid=?,rawPwd=?,diskSpaceAllowed=?,diskSpaceUsed=?,isSecret=?,ip=?,releasetime=?,timeZone=?,home=?,msn=?,group_code=?,locale=?,nick=?,check_status=?,online_time=?,fetion=?,can_rename=?,is_mobile_valid=?,province_id=?,city_id=?,country_id=? where name=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from sq_user where name=?";
    }

    public void setQueryLoad() {
        QUERY_LOAD = "select pwd,Question,Answer,realname," +
                     "Career,Gender,Job," +
                     "Birthday,Marriage,Phone," +
                     "Mobile,State,City,Address," +
                     "PostCode,IDCard,RealPic,Hobbies," +
                     "Email,OICQ,sign,myface," +
                     "experience,credit,addcount," +
                     "delcount,arrestday,arrestreason,arresttime,arrestpolice,ispolice,RegDate,lastTime,curTime,eliteCount,gold,favoriate,isValid,rawPwd,diskSpaceAllowed,diskSpaceUsed,isSecret,ip,releasetime,timeZone,home,msn,group_code,locale,nick,check_status,online_time,fetion,can_rename,is_mobile_valid,province_id,city_id,country_id from sq_user where name=?";
    }

    public void setQueryList() {
        QUERY_LIST = "select name from sq_user order by RegDate desc";
    }

    /**
     * 获取用户排行
     * @param rankType String 排行类型 online_time experience
     * @param count int
     * @return Vector
     */
    public ObjectBlockIterator listUserRank(String rankType, int count) {
        String sql = "select name from sq_user order by " + rankType + " desc";
        // System.out.println(getClass() + sql);
        return getObjects(sql, 0, count);
    }

    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, pwdMd5);
            ps.setString(2, question);
            ps.setString(3, answer);
            ps.setString(4, realName);
            ps.setString(5, career);
            ps.setString(6, gender);
            ps.setString(7, job);
            ps.setString(8, "" + DateUtil.toLong(birthday));
            ps.setInt(9, marriage);
            ps.setString(10, phone);
            ps.setString(11, mobile);
            ps.setString(12, state);
            ps.setString(13, city);
            ps.setString(14, address);
            ps.setString(15, postCode);
            ps.setString(16, IDCard);
            ps.setString(17, realPic);
            ps.setString(18, hobbies);
            ps.setString(19, email);
            ps.setString(20, oicq);
            ps.setString(21, sign);
            ps.setString(22, myface);
            ps.setInt(23, experience);
            ps.setInt(24, credit);
            ps.setInt(25, addCount);
            ps.setInt(26, delCount);
            ps.setInt(27, arrestDay);
            ps.setString(28, arrestReason);
            if (arrestTime == null)
                ps.setString(29, null);
            else
                ps.setString(29, DateUtil.toLongString(arrestTime));
            ps.setString(30, arrestPolice);
            ps.setInt(31, isPolice);
            ps.setString(32, DateUtil.toLongString(lastTime));
            ps.setString(33, DateUtil.toLongString(curTime));
            ps.setInt(34, eliteCount);
            ps.setInt(35, gold);
            ps.setString(36, favoriate);
            ps.setInt(37, valid ? 1 : 0);
            ps.setString(38, rawPwd);
            ps.setInt(39, (int) diskSpaceAllowed);
            ps.setInt(40, (int) diskSpaceUsed);
            ps.setInt(41, secret ? 1 : 0);
            ps.setString(42, ip);
            releaseTime = DateUtil.addDate(arrestTime, arrestDay);
            ps.setString(43, DateUtil.toLongString(releaseTime));
            String tzID = timeZone.getID();
            ps.setString(44, tzID);
            ps.setString(45, home);
            ps.setString(46, msn);
            ps.setString(47, groupCode);
            ps.setString(48, locale);
            ps.setString(49, nick);
            ps.setInt(50, checkStatus);
            ps.setFloat(51, onlineTime);
            ps.setString(52, fetion);
            ps.setInt(53, canRename?1:0);
            ps.setInt(54, mobileValid?1:0);
            
            ps.setInt(55, provinceId);
            ps.setInt(56, cityId);
            ps.setInt(57, countryId);
            
            ps.setString(58, name);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("save:" + e.getMessage());
            e.printStackTrace();
        } finally {
            UserCache uc = new UserCache(this);
            primaryKey.setValue(name);
            uc.refreshSave(primaryKey);
            uc.refreshNick(nick);

            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("name", PrimaryKey.TYPE_STRING);
    }

    public UserDb getUserDbByIP(String ip) {
        String sql =
                "select name from sq_user where ip=? order by RegDate desc";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, ip);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                return getUser(rs.getString(1));
            }
        } catch (Exception e) {
            logger.error("getUserDbByIP: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }

    public UserDb getUserDbByNick(String nick) {
    	new java.util.Date();
        UserCache uc = new UserCache(this);
        return uc.getUserDbByNick(nick);
    }

    public boolean validEmailOnly(String email) {
        String sql = "select name from sq_user where Email=?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                return false;
            }
        } catch (Exception e) {
            logger.error("getUserDbByNick: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 搜索贴子时使用
     * @param nick String
     * @return String
     */
    public String getNicksLike(String nick) {
        String sql = "select name from sq_user where nick like " +
                     StrUtil.sqlstr("%" + nick + "%");
        ResultSet rs = null;
        String nicks = "";
        Conn conn = new Conn(connname);
        try {
            rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    if (nicks.equals(""))
                        nicks = StrUtil.sqlstr(rs.getString(1));
                    else
                        nicks += "," + StrUtil.sqlstr(rs.getString(1));
                }
            }
        } catch (Exception e) {
            logger.error("getNicksLike: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return nicks;
    }

    public synchronized void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, name);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                pwdMd5 = rs.getString(1);
                question = StrUtil.getNullStr(rs.getString(2));
                answer = StrUtil.getNullStr(rs.getString(3));
                realName = StrUtil.getNullStr(rs.getString(4));
                career = StrUtil.getNullStr(rs.getString(5));
                gender = StrUtil.getNullStr(rs.getString(6));
                job = StrUtil.getNullStr(rs.getString(7));
                try {
                    birthday = DateUtil.parse(rs.getString(8));
                } catch (Exception e) {}
                marriage = rs.getInt(9);
                phone = StrUtil.getNullStr(rs.getString(10));
                mobile = StrUtil.getNullStr(rs.getString(11));
                state = StrUtil.getNullStr(rs.getString(12));
                city = StrUtil.getNullStr(rs.getString(13));
                address = StrUtil.getNullStr(rs.getString(14));
                postCode = StrUtil.getNullStr(rs.getString(15));
                IDCard = StrUtil.getNullStr(rs.getString(16));
                realPic = StrUtil.getNullStr(rs.getString(17));
                hobbies = StrUtil.getNullStr(rs.getString(18));
                email = StrUtil.getNullStr(rs.getString(19));
                oicq = StrUtil.getNullStr(rs.getString(20));
                sign = StrUtil.getNullStr(rs.getString(21));
                myface = StrUtil.getNullString(rs.getString(22));
                experience = rs.getInt(23);
                credit = rs.getInt(24);
                addCount = rs.getInt(25);
                delCount = rs.getInt(26);
                arrestDay = rs.getInt(27);
                arrestReason = StrUtil.getNullStr(rs.getString(28));
                arrestTime = DateUtil.parse(rs.getString(29));
                arrestPolice = StrUtil.getNullStr(rs.getString(30));
                isPolice = rs.getInt(31);
                try {
                    regDate = DateUtil.parse(rs.getString(32));
                    lastTime = DateUtil.parse(rs.getString(33));
                    curTime = DateUtil.parse(rs.getString(34));
                } catch (Exception e) {}
                eliteCount = rs.getInt(35);
                gold = rs.getInt(36);
                favoriate = StrUtil.getNullString(rs.getString(37));
                valid = rs.getInt(38) == 1 ? true : false;
                rawPwd = rs.getString(39);
                diskSpaceAllowed = rs.getInt(40);
                diskSpaceUsed = rs.getInt(41);
                secret = rs.getInt(42) == 1 ? true : false;
                id = name;
                ip = StrUtil.getNullStr(rs.getString(43));
                releaseTime = DateUtil.parse(rs.getString(44));
                String tzID = StrUtil.getNullStr(rs.getString(45));
                timeZone = TimeZone.getTimeZone(tzID);
                home = StrUtil.getNullStr(rs.getString(46));
                msn = StrUtil.getNullStr(rs.getString(47));
                groupCode = StrUtil.getNullStr(rs.getString(48));
                locale = StrUtil.getNullStr(rs.getString(49));
                nick = rs.getString(50);
                checkStatus = rs.getInt(51);
                onlineTime = rs.getFloat(52);
                fetion = StrUtil.getNullStr(rs.getString(53));
                canRename = rs.getInt(54)==1;
                mobileValid = rs.getInt(55)==1;
                provinceId = rs.getInt(56);
                cityId = rs.getInt(57);
                countryId = rs.getInt(58);
                
                loaded = true;
                primaryKey.setValue(name);
            }
        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int e) {
        this.experience = e;
    }

    public int getCredit() {
        return this.credit;
    }

    public void setCredit(int c) {
        this.credit = c;
    }

    public int getAddCount() {
        return addCount;
    }

    public void setAddCount(int a) {
        this.addCount = a;
    }

    public int getDelCount() {
        return delCount;
    }

    public void setDelCount(int d) {
        this.delCount = d;
    }

    public String getPwdMd5() {
        return pwdMd5;
    }

    public void setPwdMd5(String p) {
        this.pwdMd5 = p;
    }

    public String getRealName() {
        return realName;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public Date getRegDate() {
        return regDate;
    }

    public String getSign() {
        return sign;
    }

    public String getMyface() {
        return myface;
    }

    public void setRealName(String r) {
        realName = r;
    }

    public void setEmail(String e) {
        email = e;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getJob() {
        return job;
    }

    public String getPhone() {
        return phone;
    }

    public Date getBirthday() {
        return birthday;
    }

    public String getMobile() {
        return mobile;
    }

    public String getState() {
        return state;
    }

    public String getAddress() {
        return address;
    }

    public String getOicq() {
        return oicq;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getHobbies() {
        return hobbies;
    }

    public String getCity() {
        return city;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getIDCard() {
        return IDCard;
    }

    /**
     * 取得用户所属的组，如果用户被指定组，则返回指定组，如果未指定，即groupCode为空，则返回根据级别自动划分的组，如果都没有，则返回everyone组
     * 用以进行权限的判别，及页面的显示
     * @return UserGroupDb
     */
    public UserGroupDb getUserGroupDb() {
        String userGroupCode = "";
        UserGroupDb ugd = new UserGroupDb();
        if (groupCode.equals("")) { // 未指定用户组
            UserLevelDb uld = getUserLevelDb();
            // 取得等级自动划分的组
            if (uld.getGroupCode().equals("")) {
                return ugd.getUserGroupDb(ugd.EVERYONE);
            } else
                userGroupCode = uld.getGroupCode();
        } else {
            // 检查指定的用户组是否存在
            ugd = ugd.getUserGroupDb(groupCode);
            if (!ugd.isLoaded()) { // 不存在
                UserLevelDb uld = getUserLevelDb();
                // 取得等级自动划分的组
                if (uld.getGroupCode().equals("")) {
                    return ugd.getUserGroupDb(ugd.EVERYONE);
                } else
                    userGroupCode = uld.getGroupCode();
            } else
                userGroupCode = groupCode;
        }
        return ugd.getUserGroupDb(userGroupCode);
    }

    public UserLevelDb getUserLevelDb() {
        UserLevelDb uld = new UserLevelDb();
        Config cfg = Config.getInstance();
        String level = cfg.getProperty("forum.userLevel");
        // Vector v = uld.getAllLevel();
        int levelCompare = 0;

        if (level.equals("levelCredit")) {
            if (credit < 0) {
                credit = 0;
            }
            levelCompare = credit;
        } else if (level.equals("levelExperience")) {
            if (experience < 0) {
                experience = 0;
            }
            levelCompare = experience;
        } else if (level.equals("levelGold")) {
            if (gold < 0) {
                gold = 0;
            }
            levelCompare = gold;
        } else if (level.equals("levelTopticCount")) {
            if (addCount < 0) {
                addCount = 0;
            }
            levelCompare = addCount;
        }

        return uld.getUserLevelDbByLevel(levelCompare);
    }

    /**
     * 取得等级的图像路径
     * @return String
     */
    public String getLevelPic() {
        return getUserLevelDb().getLevelPicPath();
    }

    /**
     * 取得等级的描述
     * @return String
     */
    public String getLevelDesc() {
        return getUserLevelDb().getDesc();
    }

    public void setMyface(String myface) {
        this.myface = myface;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setOicq(String oicq) {
        this.oicq = oicq;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setIDCard(String IDCard) {
        this.IDCard = IDCard;
    }

    public UserDb getUser(String name) {
        // logger.info("name=" + name);
        return (UserDb) getObjectDb(name);
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getCareer() {
        return this.career;
    }

    public void setCareer(String c) {
        this.career = c;
    }

    public int getMarriage() {
        return this.marriage;
    }

    public int getArrestDay() {
        return arrestDay;
    }

    public String getArrestReason() {
        return arrestReason;
    }

    public Date getArrestTime() {
        return arrestTime;
    }

    public String getArrestPolice() {
        return arrestPolice;
    }

    public int getIsPolice() {
        return isPolice;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public Date getCurTime() {
        return curTime;
    }

    public int getEliteCount() {
        return eliteCount;
    }

    public int getGold() {
        return gold;
    }

    public String getFavoriate() {
        return favoriate;
    }

    public boolean isValid() {
        return valid;
    }

    public String getRawPwd() {
        return rawPwd;
    }

    public long getDiskSpaceAllowed() {
        return diskSpaceAllowed;
    }

    public long getDiskSpaceUsed() {
        return diskSpaceUsed;
    }

    public boolean isSecret() {
        return secret;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public String getHome() {
        return home;
    }

    public String getMsn() {
        return msn;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getLocale() {
        return locale;
    }

    public String getNick() {
        return nick;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public float getOnlineTime() {
        return onlineTime;
    }

    public String getFetion() {
        return fetion;
    }

    public boolean isCanRename() {
        return canRename;
    }

    public void setMarriage(int m) {
        this.marriage = m;
    }

    public void setArrestDay(int arrestDay) {
        this.arrestDay = arrestDay;
    }

    public void setArrestReason(String arrestReason) {
        this.arrestReason = arrestReason;
    }

    public void setArrestTime(Date arrestTime) {
        this.arrestTime = arrestTime;
    }

    public void setArrestPolice(String arrestPolice) {
        this.arrestPolice = arrestPolice;
    }

    public void setIsPolice(int isPolice) {
        this.isPolice = isPolice;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public void setCurTime(Date curTime) {
        this.curTime = curTime;
    }

    public void setEliteCount(int eliteCount) {
        this.eliteCount = eliteCount;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setFavoriate(String favoriate) {
        this.favoriate = favoriate;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setRawPwd(String rawPwd) {
        this.rawPwd = rawPwd;
    }

    public void setDiskSpaceAllowed(long diskSpaceAllowed) {
        this.diskSpaceAllowed = diskSpaceAllowed;
    }

    public void setDiskSpaceUsed(long diskSpaceUsed) {
        this.diskSpaceUsed = diskSpaceUsed;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void setMsn(String msn) {
        this.msn = msn;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public void setOnlineTime(float onlineTime) {
        this.onlineTime = onlineTime;
    }

    public void setFetion(String fetion) {
        this.fetion = fetion;
    }

    public void setCanRename(boolean canRename) {
        this.canRename = canRename;
    }

    public void setCurTime() {
        curTime = new Date();
    }

    public String getMyfaceUrl(HttpServletRequest request) {
		if (myface.equals(""))
			return request.getContextPath() + "/forum/images/face/" + getRealPic();
    	
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String attachmentBasePath =  "upfile/" +  myfaceBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            attachmentBasePath = cfg.getRemoteBaseUrl();
            attachmentBasePath += myfaceBasePath + "/";
        }
        String path = attachmentBasePath + myface;
        
        return request.getContextPath() + "/img_show.jsp?path=" + StrUtil.UrlEncode(path);
    }

    public boolean DIYMyface(ServletContext application, ForumFileUpload fu) throws
            ResKeyException {
        String oldface = myface;

        Vector v = fu.getFiles();
        int size = v.size();
        FileInfo fi = null;

        if (size > 0)
            fi = (FileInfo) v.firstElement();

        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);
        String photopath = Global.getRealPath() + "upfile/" + myfaceBasePath +
                           "/" + year + "/" + month + "/";

        // 保存图片
        if (fi != null) {
            // 置本地路径
            fu.setSavePath(photopath);
            // 置远程路径
            fu.setRemoteBasePath(myfaceBasePath + "/" + year + "/" + month +
                                 "/");
            // 使用随机名称写入磁盘
            fu.writeFile(true);
        }

        if (fi != null) {
            myface = year + "/" + month + "/" + fi.getDiskName();
        } else
            myface = "";

        if (!save())
            throw new ResKeyException(SkinUtil.ERR_DB);

        if (oldface != null && !oldface.equals("")) {
            com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                ForumFileUtil ffu = new ForumFileUtil();
                ffu.delFtpFile(myfaceBasePath + "/" + oldface);
            } else {
                // 删除原来的gif或jpg相片
                String fname = Global.realPath + "upfile/" + myfaceBasePath +
                               "/" + oldface;
                File virtualFile = new File(fname);
                virtualFile.delete();
            }
        }

        primaryKey.setValue(name);
        UserCache uc = new UserCache(this);
        uc.refreshSave(primaryKey);

        return true;
    }

    private String myface;
    private String sign;
    private String question;
    private String answer;
    private String career;
    private String job;
    private int marriage;
    private String phone;
    private Date birthday;
    private String mobile;
    private String state;
    private String address;
    private String oicq;
    private String postCode;
    private String hobbies;
    private String city;
    private String IDCard;
    private boolean loaded = false;
    private int arrestDay = 0;
    private String arrestReason;
    private Date arrestTime;
    private String arrestPolice;
    private int isPolice = 0;
    private Date lastTime;
    private Date curTime;
    private int eliteCount; // 被设为精华贴的数量

    private int gold = 0;
    private String favoriate;
    private boolean valid = true;
    private String rawPwd;
    private long diskSpaceAllowed = 10240000; // 10M
    private long diskSpaceUsed = 0;
    private boolean secret = true;
    private String id;
    private String ip;
    private Date releaseTime; // 用以辅助查询用户是否已被释放
    private TimeZone timeZone;
    private String home;
    private String msn;
    private String groupCode;
    private String locale;
    private String nick;
    private int checkStatus = CHECK_STATUS_PASS;
    private float onlineTime;
    private String fetion;
    private boolean canRename = false;

	public boolean isMobileValid() {
		return mobileValid;
	}

	public void setMobileValid(boolean mobileValid) {
		this.mobileValid = mobileValid;
	}
}
