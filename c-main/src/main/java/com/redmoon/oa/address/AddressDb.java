package com.redmoon.oa.address;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

import javax.servlet.http.HttpServletRequest;

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
public class AddressDb extends ObjectDb {
    private int id;

    public static final int TYPE_PUBLIC = 1;
    public static final int TYPE_USER = 0;

    public AddressDb() {
        init();
    }

    public AddressDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public String getPerson() {
        return person;
    }

    public String getJob() {
        return job;
    }

    public String getTel() {
        return tel;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void initDB() {
        tableName = "address";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new AddressCache(this);
        isInitFromConfigDB = false;
        //firstname familyname middleName PERSON nickname email street city POSTALCODE
        //province country TEL fax mobile web companyStreet companyCity companyPostcode
        //companyProvice companyCountry operationweb operationPhone operationFax
        //BeepPager company JOB department ADADRESS INTRODUCTION
        QUERY_CREATE =
                "insert into " + tableName + " (id,person,firstname, familyname, middleName,job,tel,nickname,fax,mobile,web,companyStreet,companyCity,companyPostcode,companyProvice,companyCountry,operationweb,operationPhone,operationFax,BeepPager,company,email,city,street,address,country,postalcode,province,introduction,userName,department,type,addDate,typeId,qq,msn,unit_code,weixin) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set person=?,firstname=?,familyname=?, middleName=?,job=?,tel=?,nickname=?,fax=?,mobile=?,web=?,companyStreet=?,companyCity=?,companyPostcode=?,companyProvice=?,companyCountry=?,operationweb=?,operationPhone=?,operationFax=?,BeepPager=?,company=?,email=?,city=?,street=?,address=?,country=?,postalcode=?,province=?,introduction=?,userName=?,department=?,type=?,typeId=?,qq=?,msn=?,weixin=? where id=?";
        QUERY_LIST =
                "select id from " + tableName + " order by mydate desc";
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select person,firstname, familyname, middleName,job,tel,nickname,fax,mobile,web,companyStreet,companyCity,companyPostcode,companyProvice,companyCountry,operationweb,operationPhone,operationFax,BeepPager,company,email,city,street,address,country,postalcode,province,introduction,userName,department,type,addDate,typeId,qq,msn,unit_code,weixin from " +
                     tableName + " where id=?";
    }

    public AddressDb getAddressDb(int id) {
        return (AddressDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        id = (int) SequenceManager.nextID(SequenceManager.OA_ADDRESS);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            //"insert into " + tableName + " (id,person,firstname, familyname, middleName,job,tel,job,fax,mobile,web,companyStreet,
            //companyCity,companyPostcode,companyProvice,companyCountry,operationweb,operationPhone,operationFax,BeepPager,company,email,city,street,address,country,postalcode,province,introduction,userName,department,type,addDate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate)";
            ps.setInt(1, id);
            ps.setString(2, person);
            ps.setString(3, firstname);
            ps.setString(4, familyname);
            ps.setString(5, middleName);
            ps.setString(6, job);
            ps.setString(7, tel);
            ps.setString(8, nickname);
            ps.setString(9, fax);
            ps.setString(10, mobile);
            ps.setString(11, web);
            ps.setString(12, companyStreet);
            ps.setString(13, companyCity);
            ps.setString(14, companyPostcode);
            ps.setString(15, companyProvice);
            ps.setString(16, companyCountry);
            ps.setString(17, operationweb);
            ps.setString(18, operationPhone);
            ps.setString(19, operationFax);
            ps.setString(20, BeepPager);
            ps.setString(21, company);
            ps.setString(22, email);
            ps.setString(23, city);
            ps.setString(24, street);
            ps.setString(25, address);
            ps.setString(26, country);
            ps.setString(27, postalcode);
            ps.setString(28, province);
            ps.setString(29, introduction);
            ps.setString(30, userName);
            ps.setString(31, department);
            ps.setInt(32, type);
            ps.setTimestamp(33, new Timestamp(new java.util.Date().getTime()));
            ps.setString(34, typeId);
            ps.setString(35, QQ);
            ps.setString(36, MSN);
            ps.setString(37, unitCode);
            ps.setString(38, weixin);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                AddressCache rc = new AddressCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                AddressCache rc = new AddressCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new AddressDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            // QUERY_LOAD = "select person,firstname, familyname, middleName,job,tel,nickname,fax,mobile,web,companyStreet,companyCity,companyPostcode,companyProvice,companyCountry,operationweb,operationPhone,operationFax,BeepPager,company,email,city,street,address,country,postalcode,province,introduction,userName,department,type from " +
            //     tableName + " where id=?";
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                person = StrUtil.getNullStr(rs.getString(1));
                firstname = StrUtil.getNullStr(rs.getString(2));
                familyname = StrUtil.getNullStr(rs.getString(3));
                middleName = StrUtil.getNullStr(rs.getString(4));
                job = StrUtil.getNullStr(rs.getString(5));
                tel = StrUtil.getNullStr(rs.getString(6));
                nickname = StrUtil.getNullStr(rs.getString(7));
                fax = StrUtil.getNullStr(rs.getString(8));
                mobile = StrUtil.getNullStr(rs.getString(9));
                web = StrUtil.getNullStr(rs.getString(10));
                companyStreet = StrUtil.getNullStr(rs.getString(11));
                companyCity = StrUtil.getNullStr(rs.getString(12));
                companyPostcode = StrUtil.getNullStr(rs.getString(13));
                companyProvice = StrUtil.getNullStr(rs.getString(14));
                companyCountry = StrUtil.getNullStr(rs.getString(15));
                operationweb = StrUtil.getNullStr(rs.getString(16));
                operationPhone = StrUtil.getNullStr(rs.getString(17));
                operationFax = StrUtil.getNullStr(rs.getString(18));
                BeepPager = StrUtil.getNullStr(rs.getString(19));
                company = StrUtil.getNullStr(rs.getString(20));
                email = StrUtil.getNullStr(rs.getString(21));
                city = StrUtil.getNullStr(rs.getString(22));
                street = StrUtil.getNullStr(rs.getString(23));
                address = StrUtil.getNullStr(rs.getString(24));
                country = StrUtil.getNullStr(rs.getString(25));
                postalcode = StrUtil.getNullStr(rs.getString(26));
                province = StrUtil.getNullStr(rs.getString(27));
                introduction = StrUtil.getNullStr(rs.getString(28));
                userName = StrUtil.getNullStr(rs.getString(29));
                department = StrUtil.getNullStr(rs.getString(30));
                type = rs.getInt(31);
                addDate = rs.getTimestamp(32);
                typeId = rs.getString(33);
                QQ = StrUtil.getNullStr(rs.getString(34));
                MSN = StrUtil.getNullStr(rs.getString(35));
                unitCode = StrUtil.getNullStr(rs.getString(36));
                weixin = StrUtil.getNullStr(rs.getString(37));
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, person);
            ps.setString(2, firstname);
            ps.setString(3, familyname);
            ps.setString(4, middleName);
            ps.setString(5, job);
            ps.setString(6, tel);
            ps.setString(7, nickname);
            ps.setString(8, fax);
            ps.setString(9, mobile);
            ps.setString(10, web);
            ps.setString(11, companyStreet);
            ps.setString(12, companyCity);
            ps.setString(13, companyPostcode);
            ps.setString(14, companyProvice);
            ps.setString(15, companyCountry);
            ps.setString(16, operationweb);
            ps.setString(17, operationPhone);
            ps.setString(18, operationFax);
            ps.setString(19, BeepPager);
            ps.setString(20, company);
            ps.setString(21, email);
            ps.setString(22, city);
            ps.setString(23, street);
            ps.setString(24, address);
            ps.setString(25, country);
            ps.setString(26, postalcode);
            ps.setString(27, province);
            ps.setString(28, introduction);
            ps.setString(29, userName);
            ps.setString(30, department);
            ps.setInt(31, type);
            ps.setString(32, typeId);
            ps.setString(33, QQ);
            ps.setString(34, MSN);
            ps.setString(35, weixin);
            ps.setInt(36, id);
            re = conn.executePreUpdate() == 1 ? true : false;

            if (re) {
                AddressCache rc = new AddressCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public int getType() {
        return type;
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
                rs = null;
            }

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

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
                    AddressDb ug = getAddressDb(rs.getInt(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getFamilyname() {
        return familyname;
    }
    public String getNickname() {
        return nickname;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getCountry() {
        return country;
    }

    public String getFax() {
        return fax;
    }

    public String getCompanyStreet() {
        return companyStreet;
    }

    public String getCompanyCity() {
        return companyCity;
    }

    public String getCompanyPostcode() {
        return companyPostcode;
    }

    public String getCompanyProvice() {
        return companyProvice;
    }

    public String getCompanyCountry() {
        return companyCountry;
    }

    public String getOperationweb() {
        return operationweb;
    }

    public String getOperationPhone() {
        return operationPhone;
    }

    public String getOperationFax() {
        return operationFax;
    }

    public String getCompany() {
        return company;
    }

    public String getDepartment() {
        return department;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getWeb() {
        return web;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getMSN() {
        return MSN;
    }

    public String getQQ() {
        return QQ;
    }

    public String getBeepPager() {
        return BeepPager;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddDate(java.util.Date addDate) {
        this.addDate = addDate;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setFamilyname(String familyname) {
        this.familyname = familyname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setCompanyStreet(String companyStreet) {
        this.companyStreet = companyStreet;
    }

    public void setCompanyCity(String companyCity) {
        this.companyCity = companyCity;
    }

    public void setCompanyPostcode(String companyPostcode) {
        this.companyPostcode = companyPostcode;
    }

    public void setCompanyProvice(String companyProvice) {
        this.companyProvice = companyProvice;
    }

    public void setCompanyCountry(String companyCountry) {
        this.companyCountry = companyCountry;
    }

    public void setOperationweb(String operationweb) {
        this.operationweb = operationweb;
    }

    public void setOperationPhone(String operationPhone) {
        this.operationPhone = operationPhone;
    }

    public void setOperationFax(String operationFax) {
        this.operationFax = operationFax;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public void setMSN(String MSN) {
        this.MSN = MSN;
    }

    public void setQQ(String QQ) {
        this.QQ = QQ;
    }

    public void setBeepPager(String BeepPager) {
        this.BeepPager = BeepPager;
    }
    
    /**
     * 通过手机号取得联系人
     * 先从通讯录中找，找不到再从系统用户中找
     * @param mobile
     * @return
     */
    public String[] getAddressDbByMobile(String mobile) {
    	String[] ary = new String[6];
    	String sql = "select id from address where (mobile=" + StrUtil.sqlstr(mobile) + " or msn=" + StrUtil.sqlstr(mobile) + ") and type=" + TYPE_PUBLIC;
    	Iterator ir = list(sql).iterator();
    	if (ir.hasNext()) {
    		AddressDb ad = (AddressDb)ir.next();
    		ary[0] = ad.getPerson();
    		ary[1] = ad.getCompany();
    		// ary[2] = ad.getDepartment();
    		// ary[3] = ad.getCompanyProvice();
    		// ary[4] = ad.getCompanyCity();
    		return ary;
    	}
    	else {
    		UserDb user = new UserDb();
    		user = user.getUserDbByMobile(mobile);
    		if (user!=null && user.isLoaded()) {
    			ary[0] = user.getRealName();
    			DeptUserDb dud = new DeptUserDb();
    			ir = dud.getDeptsOfUser(user.getName()).iterator();
    			if (ir.hasNext()) {
    				DeptDb dd = (DeptDb)ir.next();
        			DeptMgr dm = new DeptMgr();
    				String deptName = "";
    				if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {					
    					deptName = dm.getDeptDb(dd.getParentCode()).getName() + " " + dd.getName();
    				}
    				else
    					deptName = dd.getName();
    				ary[1] = deptName;
    			}
    			return ary;
    		}
    	}
    	return null;
    }

    /**
     * 取得列表页的SQL语句
     * @param op
     * @param type
     * @param typeId
     * @param person
     * @param company
     * @param mobile
     * @param orderBy
     * @param sort
     * @return
     */
    public static String getSqlList(String op, String userName, int type, String typeId, String person, String company, String mobile, String orderBy, String sort) {
        String sql = "select id from address where type=" + type;
        if (type == AddressDb.TYPE_PUBLIC) {
            if (typeId.equals("public")) {
                op = "search";
                typeId = "";
            }
        } else {
            if (typeId.equals(userName)) {
                op = "search";
                typeId = "";
            }
        }

        if (op.equals("search")) {
            if (type == AddressDb.TYPE_USER)
                sql = "select id from address where userName=" + StrUtil.sqlstr(userName) + " and type=" + AddressDb.TYPE_USER;
            else {
                sql = "select id from address where type=" + type;
            }
            if (!person.equals("")) {
                sql += " and person like " + StrUtil.sqlstr("%" + person + "%");
            }

            if (!company.equals("")) {
                sql += " and company like " + StrUtil.sqlstr("%" + company + "%");
            }

            if (!typeId.equals("")) {
                sql += " and typeId = " + StrUtil.sqlstr(typeId);
            }
            if (!mobile.equals("")) {
                sql += " and mobile like " + StrUtil.sqlstr("%" + mobile + "%");
            }
        } else {
            if (!typeId.equals(""))
                sql += " and typeId = " + StrUtil.sqlstr(typeId);
            if (type != AddressDb.TYPE_PUBLIC)
                sql += " and userName=" + StrUtil.sqlstr(userName);
        }

        if (type == AddressDb.TYPE_PUBLIC) {
            sql += " and unit_code=" + StrUtil.sqlstr(userName);
        }

        sql += " order by " + orderBy;
        sql += " " + sort;
        return sql;
    }
    

    private String person;
    private String job;
    private String tel;
    private String mobile;
    private String email;
    private String address;
    private String postalcode;
    private String introduction;
    private String userName;
    private String firstname;
    private String familyname;
    private String nickname;
    private String street;
    private String city;
    private String province;
    private String country;
    private String fax;
    private String companyStreet;
    private String companyCity;
    private String companyPostcode;
    private String companyProvice;
    private String companyCountry;
    private String operationweb;
    private String operationPhone;
    private String operationFax;
    private String BeepPager;
    private String company;
    private String department;
    private int type;
    private String middleName;
    private String web;
    private java.util.Date addDate;
    private String typeId;
    private String QQ;
    /**
     * MSN已被改为短号
     */
    private String MSN;

    private String weixin;

    private String unitCode;

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}
	
	public String getShortMobile() {
		return MSN;
	}

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }
}

