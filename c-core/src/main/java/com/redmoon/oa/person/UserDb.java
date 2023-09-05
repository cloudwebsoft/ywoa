package com.redmoon.oa.person;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.Group;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.UserVO;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.UserGroupDb;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class UserDb extends ObjectDb {
    public static final String ADMIN = ConstUtil.USER_ADMIN;

    public static final String SYSTEM = ConstUtil.USER_SYSTEM;

    public static final int VALID_WORKING = 1; // 工作中
    public static final int VALID_FIRED = 0; // 已解雇

    public static final int CHECK_NOT = 0;		// 未审批
    public static final int CHECK_PASSED_YES = 1;	// 通过
    public static final int CHECK_PASSED_NO = 2;	// 未通过

    private String name;
    private String MSN;
    private String pwdMD5;
    private String pwdRaw;
    private String realName;
    /**
     * 0 男性 1 女性
     */
    private int gender = 0;
    private int marriaged = 0;
    private String mobile;
    private String phone;
    private String state;
    private String city;
    private String address;
    private String postCode;
    /**
     * 身份证
     */
    private String IDCard;
    private String picture;
    private String hobbies;
    private String email;
    private String QQ;
    private Date regDate;
    private String proxy; // 代理者的职位编码
    private Date birthday;
    private Date entryDate;
    
    /**
     * 人员编号，用于同步考勤机
     */
    private String personNo;
    
    /**
     * 为了便于扩展，比如企业型、专家型
     */
    private String type;
    
    private String photo;
    
    /**
     * 微信
     */
    private String weixin;
    
    /**
     * 钉钉
     */
    private String dingding;

    /**
     * 排序号，按从大到小排列
     */
    private int orders = 0;

    public UserDb() {
        // init(); // ObjectDb的构造函数中已调用
    }

    public UserDb(String name) {
        this.name = name;
        load();
        init();
    }

    @Override
    public void initDB() {
        tableName = "users";
        isInitFromConfigDB = false;
    }

    public boolean Auth(String name, String pwd) {
		UserDb user = getUserDb(name);
		if (user == null || !user.isLoaded()) {
            return false;
        }
        String pwdMD5 = user.getPwdMD5();
        String p = "";
        try {
            p = SecurityUtil.MD5(pwd);
        } catch (Exception e) {}
        return pwdMD5.equals(p);
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return null;
    }

    @Override
    public Vector<UserDb> list() {
        Vector<UserDb> v = new Vector<>();
        IUserService userService = SpringUtil.getBean(IUserService.class);
        List<User> list = userService.listAll();
        for (User user : list) {
            v.addElement(getFromUser(user, new UserDb()));
        }
        return v;
    }

    @Override
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
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages) {
                curPage = totalpages;
            }
            if (curPage <= 0) {
                curPage = 1;
            }

            conn.prepareStatement(listsql);

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            // rs = conn.executeQuery(listsql); // MySQL中效率很低，70万行的数据，原本30毫秒的数据，需要2秒多才能查出
            rs = conn.executePreQuery();

            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    result.addElement(getUserDb(rs.getString(1)));
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listResult:" + e.getMessage());
            throw new ErrMsgException("Db error.");
        } finally {
            conn.close();
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     * 列出全部用户，含已离职用户
     * @return
     */
    public Vector<UserDb> listAll() {
        String sql = "select name from users order by regDate desc";
        return list(sql);
    }

    @Override
    public Vector<UserDb> list(String sql) {
        Vector<UserDb> v = new Vector<>();
        IUserService userService = SpringUtil.getBean(IUserService.class);
        List<String> list = userService.listNameBySql(sql);
        for (String userName : list) {
            User user = userService.getUser(userName);
            v.addElement(getFromUser(user, new UserDb()));
        }
        return v;
    }

    public UserDb getFromUser(User user, UserDb ud) {
        if (user==null) {
            return ud;
        }
        ud.setId(user.getId());
        ud.setName(user.getName());
        ud.setMSN(user.getMsn());
        ud.setPwdMD5(user.getPwd());
        ud.setRealName(user.getRealName());
        ud.setGender(user.getGender()?1:0);
        ud.setMarriaged(user.getIsMarriaged()?1:0);
        ud.setMobile(user.getMobile());
        ud.setPhone(user.getPhone());
        ud.setState(user.getState());
        ud.setCity(user.getCity());
        ud.setAddress(user.getAddress());
        ud.setPostCode(user.getPostCode());
        ud.setIDCard(user.getIDCard());
        ud.setPicture(user.getPicture());
        ud.setHobbies(user.getHobbies());
        ud.setEmail(StrUtil.getNullStr(user.getEmail()));
        ud.setQQ(user.getQq());
        ud.setRegDate(DateUtil.asDate(user.getRegDate()));
        ud.setProxy(user.getProxy());
        ud.setBirthday(DateUtil.asDate(user.getBirthday()));
        ud.setValid(user.getIsValid());
        ud.setProxyBeginDate(DateUtil.asDate(user.getProxyBeginDate()));
        ud.setProxyEndDate(DateUtil.asDate(user.getProxyEndDate()));
        ud.setPwdRaw(user.getPwdRaw());
        ud.setDiskSpaceAllowed(user.getDiskSpaceAllowed());
        ud.setDiskSpaceUsed(user.getDiskSpaceUsed());
        ud.setId(user.getId());
        ud.setRankCode(user.getRankCode());
        ud.setOnlineTime(user.getOnlineTime());
        ud.setUin(user.getUin());
        ud.setUnitCode(user.getUnitCode());
        ud.setPersonNo(user.getPersonNo());
        ud.setType(user.getUserType());
        ud.setDuty(user.getDuty());
        ud.setParty(user.getParty());
        ud.setResume(user.getResume());
        ud.setPhoto(user.getPhoto());
        ud.setIsPass(user.getIsPass());
        ud.setEntryDate(DateUtil.asDate(user.getEntryDate()));
        ud.setWeixin(user.getWeixin());
        ud.setDingding(user.getDingding());
        ud.setOrders(user.getOrders());
        ud.setLastLogin(DateUtil.asDate(user.getLastLogin()));
        ud.setOpenId(user.getOpenId());
        ud.setLoaded(true);
        return ud;
    }

    /**
     * 取得某单位的全部有效用户
     * @param unitCode String
     * @return Vector
     */
    public Vector<UserDb> listUserOfUnit(String unitCode) {
        IUserService userService = SpringUtil.getBean(IUserService.class);
        List<User> list = userService.listByUnitCode(unitCode);

        Vector<UserDb> v = new Vector<>();
        for (User user : list) {
            v.addElement(getFromUser(user, new UserDb()));
        }
        return v;
    }

    /**
     * 列出部门中的用户
     * @param deptCode
     * @return
     */
    public Vector<UserDb> listByDeptCode(String deptCode) {
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        List<DeptUser> list = deptUserService.listByDeptCode(deptCode);
        Vector<UserDb> v = new Vector<>();
        for (DeptUser user : list) {
            v.addElement(getUserDb(user.getUserName()));
        }
        return v;
    }

    public String getProxy() {
        return proxy;
    }
    
    public UserDb getUserDbByEmail(String email) {
        IUserService userService = SpringUtil.getBean(IUserService.class);
        return getFromUser(userService.getUserByEmail(email), new UserDb());
    }

    /**
     * 通过手机号码或短號取得用户
     * @param mobile String
     * @return UserDb
     */
    public UserDb getUserDbByMobile(String mobile) {
        IUserService userService = SpringUtil.getBean(IUserService.class);
        return getFromUser(userService.getUserByMobile(mobile), new UserDb());
    }

    /**
     * 根据用户姓名取得用户名，用于导入数据
     * @param realName String
     * @return UserDb
     */
    public UserDb getUserDbByRealName(String realName) {
        IUserService userService = SpringUtil.getBean(IUserService.class);
        return getFromUser(userService.getUserByRealName(realName), new UserDb());
    }

    public boolean isExist(String username) {
        UserDb user = getUserDb(username);
        return user != null && user.isLoaded();
    }

    /**
     * 用于导入用户
     * @param name String
     * @param realName String
     * @param pwd String
     * @param mobile String
     * @return boolean
     */
    public final boolean create(String name, String realName, String pwd, String mobile, String unitCode, int isPass) {
        boolean re = false;
        IUserService userService = SpringUtil.getBean(IUserService.class);
        UserVO userVO = new UserVO();
        try {
            userVO.setName(name);
            userVO.setRealName(realName);
            userVO.setPwdRaw(pwd);

            String pwdMD5 = "";
            try {
                pwdMD5 = SecurityUtil.MD5(pwd);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            userVO.setPwd(pwdMD5);
            userVO.setMobile(mobile);
            userVO.setUnitCode(unitCode);
            userVO.setIsPass(isPass);
            userVO.setLoginName(name);

            this.name = name;
            this.realName = realName;
            this.pwdRaw = pwd;
            this.pwdMD5 = pwdMD5;
            this.mobile = mobile;
            this.unitCode = unitCode;
            this.setIsPass(isPass);
            this.setValid(1);

            re = userService.create(userVO);
        } catch (IOException | ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
    }

    /**
     * 用于导入或同步用户
     * @param name String
     * @param realName String
     * @param pwd String
     * @param mobile String
     * @return boolean
     */
    public final boolean create(String name, String realName, String pwd, String mobile, String unitCode) {
        return create(name, realName, pwd, mobile, unitCode, CHECK_PASSED_YES);
    }

    @Override
    public boolean save() {
        IUserService userService = SpringUtil.getBean(IUserService.class);
        User user = new User();
        user.setName(name);
        user.setPwd(pwdMD5);
        user.setRealName(realName);
        user.setGender(gender==1);
        user.setIsMarriaged(marriaged==1);
        user.setMobile(mobile);
        user.setPhone(phone);
        user.setState(state);
        user.setCity(city);
        user.setAddress(address);
        user.setPostCode(postCode);
        user.setIDCard(IDCard);
        user.setPicture(picture);
        user.setHobbies(hobbies);
        user.setEmail(email);
        user.setQq(QQ);
        user.setRegDate(DateUtil.toLocalDateTime(regDate));
        user.setProxy(proxy);
        user.setMsn(MSN);
        user.setBirthday(DateUtil.toLocalDate(birthday));
        user.setIsValid(valid);
        user.setProxyBeginDate(DateUtil.toLocalDateTime(proxyBeginDate));
        user.setProxyEndDate(DateUtil.toLocalDateTime(proxyEndDate));
        user.setPwdRaw(pwdRaw);
        user.setDiskSpaceAllowed(diskSpaceAllowed);
        user.setDiskSpaceUsed(diskSpaceUsed);
        user.setRankCode(rankCode);
        user.setOnlineTime(onlineTime);
        user.setUin(uin);
        user.setUnitCode(unitCode);
        user.setPersonNo(personNo);
        user.setUserType(type);
        user.setDuty(duty);
        user.setParty(party);
        user.setResume(resume);
        user.setPhoto(photo);
        user.setIsPass(pass);
        user.setEntryDate(DateUtil.toLocalDate(entryDate));
        user.setWeixin(weixin);
        user.setDingding(dingding);
        user.setOrders(orders);
        user.setLastLogin(DateUtil.toLocalDateTime(lastLogin));
        return userService.updateByUserName(user);
    }

    @Override
    public void load() {
        com.cloudweb.oa.cache.UserCache userCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserCache.class);
        User user = userCache.getUser(name);
        if (user!=null) {
            getFromUser(user, this);
        }
    }

    @Override
    public boolean del() {
        IUserService userService = SpringUtil.getBean(IUserService.class);
        boolean re = false;
        try {
//            com.cloudweb.oa.cache.UserCache userCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserCache.class);
//            User user = userCache.getUser(name);
            re = userService.delUsers(new String[]{String.valueOf(id)});
        } catch (ResKeyException | ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPwdMD5(String pwdMD5) {
        this.pwdMD5 = pwdMD5;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setMarriaged(int marriaged) {
        this.marriaged = marriaged;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setBirthday(Date birthDay) {
        this.birthday = birthDay;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public void setProxyBeginDate(Date proxyBeginDate) {
        this.proxyBeginDate = proxyBeginDate;
    }

    public void setProxyEndDate(Date proxyEndDate) {
        this.proxyEndDate = proxyEndDate;
    }

    public void setPwdRaw(String pwdRaw) {
        this.pwdRaw = pwdRaw;
    }

    public void setDiskSpaceAllowed(long diskSpaceAllowed) {
        this.diskSpaceAllowed = diskSpaceAllowed;
    }

    public void setDiskSpaceUsed(long diskSpaceUsed) {
        this.diskSpaceUsed = diskSpaceUsed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRankCode(String rankCode) {
        this.rankCode = rankCode;
    }

    public void setOnlineTime(float onlineTime) {
        this.onlineTime = onlineTime;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public void setMSN(String MSN) {
        this.MSN = MSN;
    }

    public void setQQ(String QQ) {
        this.QQ = QQ;
    }

    public void setIDCard(String IDCard) {
        this.IDCard = IDCard;
    }

    public String getName() {
        return name;
    }

    public String getPwdMD5() {
        return pwdMD5;
    }
    
    public String getRealNameRaw() {
    	return realName;
    }

    public String getRealName() {
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    	StringBuffer sb = new StringBuffer();
		int realNameShowWithDeptLayer = cfg.getInt("realNameShowWithDeptLayer");
		if (realNameShowWithDeptLayer>=2) {
	    	DeptUserDb du = new DeptUserDb();
	    	DeptMgr dm = new DeptMgr();
			Iterator ir = du.getDeptsOfUser(name).iterator();		
			int k = 0;
			while (ir.hasNext()) {
				DeptDb dd = (DeptDb)ir.next();
				String deptName = dd.getName();
				
				DeptDb pdd = null;
				if (!dd.getCode().equals(DeptDb.ROOTCODE)) {
					pdd = dm.getDeptDb(dd.getParentCode());
					int parentLayer = pdd.getLayer();
					while (parentLayer >= realNameShowWithDeptLayer) {					
						deptName = pdd.getName();
						pdd = dm.getDeptDb(pdd.getParentCode());
						parentLayer = pdd.getLayer();
					}					

					if (k==0) {
						sb.append(deptName);
					}
					else {
						sb.append("，" + deptName);
					}
					k++;					
				}
			}
			
			if (sb.length()>0) {
				sb.append("：" + realName);
			}
			else {
				sb.append(realName);
			}

			return sb.toString();
		}
		else {
			return realName;
		}
    }

    public int getGender() {
        return gender;
    }

    public int getMarriaged() {
        return marriaged;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPhone() {
        return phone;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getPicture() {
        return picture;
    }

    public String getHobbies() {
        return hobbies;
    }

    public String getEmail() {
        return email;
    }

    public Date getRegDate() {
        return regDate;
    }

    public Date getBirthday() {
        return birthday;
    }

    public int getValid() {
        return valid;
    }

    public boolean isValid() {
        return valid == 1;
    }

    public Date getProxyBeginDate() {
        return proxyBeginDate;
    }

    public Date getProxyEndDate() {
        return proxyEndDate;
    }

    public String getPwdRaw() {
        return pwdRaw;
    }

    public long getDiskSpaceAllowed() {
        return diskSpaceAllowed;
    }

    /**
     * 获取磁盘配额，以角色所赋予的及用户自己的，其中最大的配置为准
     * 此方法已无效，磁盘份额以getDiskSpaceAllowed()为准
     * @return long
     */
    public long getDiskQuota() {
    	return getDiskSpaceAllowed();
    }

    public long getDiskSpaceUsed() {
        return diskSpaceUsed;
    }

    public int getId() {
        return id;
    }

    public String getRankCode() {
        return rankCode;
    }

    public float getOnlineTime() {
        return onlineTime;
    }

    public String getUin() {
        return uin;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public String getMSN() {
        return MSN;
    }

    public String getQQ() {
        return QQ;
    }

    public String getIDCard() {
        return IDCard;
    }

    public UserDb getUserDb(String name) {
        // this.name = name;
        com.cloudweb.oa.cache.UserCache userCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserCache.class);
        User user = userCache.getUser(name);
        return getFromUser(user, new UserDb());
    }

    public UserDb getUserDb(int userId) {
        IUserService userService = SpringUtil.getBean(IUserService.class);
        User user = userService.getUserById(userId);
        return getFromUser(user, this);
    }

    @Override
    public String toString() {
        return "UserDb name=" + name;
    }

    /**
     * 取得用户所属的用户组，包含everyone用户组
     * @return UserGroupDb[]
     */
    public UserGroupDb[] getGroups() {
        com.cloudweb.oa.cache.UserCache userCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserCache.class);
        List<Group> list = userCache.getGroups(name);

        Group groupEveryone = new Group();
        groupEveryone.setCode(ConstUtil.GROUP_EVERYONE);
        groupEveryone.setIsSystem(true);
        groupEveryone.setIsDept(false);
        groupEveryone.setIsIncludeSubDept(0);
        groupEveryone.setUnitCode(ConstUtil.DEPT_ROOT);

        UserGroupDb[] userGroupDbs = new UserGroupDb[list.size() + 1];
        UserGroupDb userGroupDb = new UserGroupDb();
        int i = 0;
        for (Group group : list) {
            userGroupDbs[i] = userGroupDb.getFromGroup(group, new UserGroupDb());
            i++;
        }

        userGroupDbs[i] = userGroupDb.getFromGroup(groupEveryone, new UserGroupDb());
        return userGroupDbs;
    }

    /**
     * 判断用户是否属于角色
     * @param roleCode String 角色编码
     * @return boolean
     */
    public boolean isUserOfRole(String roleCode) {
        if (roleCode.equals(RoleDb.CODE_MEMBER)) {
            return true;
        }
        RoleDb[] rds = getRoles();
        int len = rds.length;
        for (int i = 0; i < len; i++) {
            RoleDb rd = rds[i];
            if (rd.getCode().equals(roleCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取得用户所属的角色，包含其所属用户组拥有的角色，包含MEMBER角色
     * @return RoleDb[]
     */
    public RoleDb[] getRoles() {
        com.cloudweb.oa.cache.UserCache userCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserCache.class);
        List<Role> list = userCache.getRoles(name);

        Role roleMember = new Role();
        roleMember.setCode(ConstUtil.ROLE_MEMBER);
        roleMember.setIsSystem(true);
        roleMember.setUnitCode(ConstUtil.DEPT_ROOT);
        roleMember.setOrders(0);
        roleMember.setRoleType(RoleDb.TYPE_NORMAL);
        roleMember.setMsgSpaceQuota(ConstUtil.QUOTA_NOT_SET);
        roleMember.setDiskQuota(ConstUtil.QUOTA_NOT_SET);
        roleMember.setDescription("全部用户");
        roleMember.setStatus(true);

        RoleDb rd = new RoleDb();
        RoleDb[] roles = new RoleDb[list.size() + 1];
        int i = 0;
        for (Role role : list) {
            roles[i] = rd.getFromRole(role, new RoleDb());
            i++;
        }

        roles[i] = rd.getFromRole(roleMember, new RoleDb());
        return roles;
    }

    /**
     * 取得用户管理的部门
     * @return String[]
     */
    public String[] getAdminDepts() {
        com.cloudweb.oa.cache.UserCache userCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserCache.class);
        return userCache.getAdminDepts(name);
    } 

    /**
     * 取得用户的权限
     * @return String[]
     */
    public String[] getPrivs() {
        com.cloudweb.oa.cache.UserCache userCache = SpringUtil.getBean(com.cloudweb.oa.cache.UserCache.class);
        return userCache.getPrivs(name);
    }

    public void setPersonNo(String personNo) {
		this.personNo = personNo;
	}

	public String getPersonNo() {
		return personNo;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public String getResume() {
		return resume;
	}

	public void setDuty(String duty) {
		this.duty = duty;
	}

	public String getDuty() {
		return duty;
	}

	public void setParty(String party) {
		this.party = party;
	}

	public String getParty() {
		return party;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPhoto() {
		return photo;
	}
	
	public int getIsPass() {
		return pass;
	}

	public void setIsPass(int pass) {
		this.pass = pass;
	}
	
	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}
	
	public void refreshOrders() {
        // 始终刷新orders字段，因为有些场景下可能仍需按orders排序
        RoleDb[] roles = getRoles();
        int order = 0;
        for (RoleDb role : roles) {
            if (role.getOrders() > order) {
                order = role.getOrders();
            }
        }
        setOrders(order);
        save();
	}

	public String getWeixin() {
		return weixin;
	}

	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}

	public String getDingding() {
		return dingding;
	}

	public void setDingding(String dingding) {
		this.dingding = dingding;
	}

	/**
     * 是否有效
     */
    private int valid = 1;
    /**
     * 代理开始时间
     */
    private Date proxyBeginDate;
    /**
     * 代理结束时间
     */
    private Date proxyEndDate;
    /**
     * 虚拟磁盘分配的空间
     */
    private long diskSpaceAllowed = 102400000; // 100M
    /**
     * 虚拟磁盘已用空间
     */
    private long diskSpaceUsed;
    private int id;
    /**
     * 职级
     */
    private String rankCode;
    /**
     * 在线时长
     */
    private float onlineTime = 0;
    /**
     * RTX号码
     */
    private String uin;

    /**
     * 用于用户管理里，快速得到用户列表
     */
    private String unitCode;

    /**
     * 简历
     */
    private String resume;
    
    /***
     * 职务，已不能称为职务，现用于排序，对应于角色的大小，取最大的角色的排序号
     */
    private String duty;
    
    /**
     * 党派
     */
    private String party;

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    private Date lastLogin;
    
    
    /**
     * 手机端是否审批通过
     */
    private int pass = CHECK_NOT;

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    private String openId;

}
