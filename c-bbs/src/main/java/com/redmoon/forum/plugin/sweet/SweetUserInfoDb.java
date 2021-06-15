package com.redmoon.forum.plugin.sweet;

import java.sql.*;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.DateUtil;

public class SweetUserInfoDb extends ObjectDb {
    public static final int MEMBER_COMMON = 0;
    public static final int MEMBER_SILVER = 1;
    public static final int MEMBER_GOLD = 2;

    public SweetUserInfoDb() {
        init();
    }

    public SweetUserInfoDb(String name) {
        this.name = name;
        init();
        load();
    }

    private String address;

    private int tall;

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, name);
            ps.setString(2, gender);
            ps.setInt(3, age);
            ps.setString(4, DateUtil.toLongString(birthday));
            ps.setString(5, marriage);
            ps.setString(6, province);
            ps.setString(7, workAddress);
            ps.setInt(8, tall);
            ps.setString(9, xueli);
            ps.setString(10, job);
            ps.setString(11, salary);
            ps.setString(12, address);
            ps.setInt(13, postCode);
            ps.setString(14, tel);
            ps.setString(15, email);
            ps.setInt(16, OICQ);
            ps.setString(17, ICQ);
            ps.setString(18, MSN);
            ps.setString(19, description);
            ps.setString(20, sport);
            ps.setString(21, book);
            ps.setString(22, music);
            ps.setString(23, celebrity);
            ps.setString(24, photo);
            ps.setString(25, hobby);
            ps.setString(26, frendType);
            ps.setString(27, frendAge);
            ps.setString(28, frendTall);
            ps.setString(29, frendMarriage);
            ps.setString(30, frendProvince);
            ps.setString(31, frendXueli);
            ps.setString(32, frendSalary);
            ps.setString(33, frendRequire);
            ps.setString(34, manager);
            ps.setInt(35, checked?1:0);
            ps.setInt(36, member);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount > 0 ? true : false;
    }

    public boolean del() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setString(1, name);
            rowcount = conn.executePreUpdate();

            SweetUserInfoCache sc = new SweetUserInfoCache(this);
            sc.refreshDel(primaryKey);

        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount > 0 ? true : false;
    }

    public SweetUserInfoDb getSweetUserInfoDb(String name) {
        return (SweetUserInfoDb)getObjectDb(name);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
         return new SweetUserInfoDb(pk.getStrValue());
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, gender);
            ps.setInt(2, age);
            if (birthday!=null)
                ps.setString(3, DateUtil.toLongString(birthday));
            else
                ps.setString(3, null);
            ps.setString(4, marriage);
            ps.setString(5, province);
            ps.setString(6, workAddress);
            ps.setInt(7, tall);
            ps.setString(8, xueli);
            ps.setString(9, job);
            ps.setString(10, salary);
            ps.setString(11, address);
            ps.setInt(12, postCode);
            ps.setString(13, tel);
            ps.setString(14, email);
            ps.setInt(15, OICQ);
            ps.setString(16, ICQ);
            ps.setString(17, MSN);
            ps.setString(18, description);
            ps.setString(19, sport);
            ps.setString(20, book);
            ps.setString(21, music);
            ps.setString(22, celebrity);
            ps.setString(23, photo);
            ps.setString(24, hobby);
            ps.setString(25, frendType);
            ps.setString(26, frendAge);
            ps.setString(27, frendTall);
            ps.setString(28, frendMarriage);
            ps.setString(29, frendProvince);
            ps.setString(30, frendXueli);
            ps.setString(31, frendSalary);
            ps.setString(32, frendRequire);
            ps.setString(33, manager);
            ps.setInt(34, checked?1:0);
            ps.setInt(35, member);
            ps.setString(36, name);
            rowcount = conn.executePreUpdate();

            SweetUserInfoCache uc = new SweetUserInfoCache(this);
            primaryKey.setValue(name);
            uc.refreshSave(primaryKey);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, name);
            rs = conn.executePreQuery();
            if (rs.next()) {
               gender = rs.getString(1);
               age = rs.getInt(2);
               birthday = DateUtil.parse(rs.getString(3));
               marriage = rs.getString(4);
               province = rs.getString(5);
               workAddress = rs.getString(6);
               tall = rs.getInt(7);
               xueli = rs.getString(8);
               job = rs.getString(9);
               salary = rs.getString(10);
               address = rs.getString(11);
               postCode = rs.getInt(12);
               tel = rs.getString(13);
               email = rs.getString(14);
               OICQ = rs.getInt(15);
               ICQ = rs.getString(16);
               MSN = rs.getString(17);
               description = rs.getString(18);
               sport = rs.getString(19);
               book = rs.getString(20);
               music = rs.getString(21);
               celebrity = rs.getString(22);
               photo = rs.getString(23);
               hobby = rs.getString(24);
               frendType = rs.getString(25);
               frendAge = rs.getString(26);
               frendTall = rs.getString(27);
               frendMarriage = rs.getString(28);
               frendProvince = rs.getString(29);
               frendXueli = rs.getString(30);
               frendSalary = rs.getString(31);
               frendRequire = rs.getString(32);
               manager = rs.getString(33);
               checked = rs.getInt(34)==1?true:false;
               member = rs.getInt(35);

               primaryKey.setValue(name);
               loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void setPrimaryKey() {
        this.primaryKey = new PrimaryKey("name", primaryKey.TYPE_STRING);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setBirthday(java.util.Date birthday) {
        this.birthday = birthday;
    }

    public void setMarriage(String marriage) {
        this.marriage = marriage;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public void setTall(int tall) {
        this.tall = tall;
    }

    public void setXueli(String xueli) {
        this.xueli = xueli;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPostCode(int postCode) {
        this.postCode = postCode;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public void setCelebrity(String celebrity) {
        this.celebrity = celebrity;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public void setFrendType(String frendType) {
        this.frendType = frendType;
    }

    public void setFrendAge(String frendAge) {
        this.frendAge = frendAge;
    }

    public void setFrendTall(String frendTall) {
        this.frendTall = frendTall;
    }

    public void setFrendMarriage(String frendMarriage) {
        this.frendMarriage = frendMarriage;
    }

    public void setFrendProvince(String frendProvince) {
        this.frendProvince = frendProvince;
    }

    public void setFrendXueli(String frendXueli) {
        this.frendXueli = frendXueli;
    }

    public void setFrendSalary(String frendSalary) {
        this.frendSalary = frendSalary;
    }

    public void setFrendRequire(String frendRequire) {
        this.frendRequire = frendRequire;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setMember(int member) {
        this.member = member;
    }

    public void setMSN(String MSN) {
        this.MSN = MSN;
    }

    public void setICQ(String ICQ) {
        this.ICQ = ICQ;
    }

    public void setOICQ(int OICQ) {
        this.OICQ = OICQ;
    }


    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public java.util.Date getBirthday() {
        return birthday;
    }

    public String getMarriage() {
        return marriage;
    }

    public String getProvince() {
        return province;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public int getTall() {
        return tall;
    }

    public String getXueli() {
        return xueli;
    }

    public String getJob() {
        return job;
    }

    public String getSalary() {
        return salary;
    }

    public String getAddress() {
        return address;
    }

    public int getPostCode() {
        return postCode;
    }

    public String getTel() {
        return tel;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public String getSport() {
        return sport;
    }

    public String getBook() {
        return book;
    }

    public String getMusic() {
        return music;
    }

    public String getCelebrity() {
        return celebrity;
    }

    public String getPhoto() {
        return photo;
    }

    public String getHobby() {
        return hobby;
    }

    public String getFrendType() {
        return frendType;
    }

    public String getFrendAge() {
        return frendAge;
    }

    public String getFrendTall() {
        return frendTall;
    }

    public String getFrendMarriage() {
        return frendMarriage;
    }

    public String getFrendProvince() {
        return frendProvince;
    }

    public String getFrendXueli() {
        return frendXueli;
    }

    public String getFrendSalary() {
        return frendSalary;
    }

    public String getFrendRequire() {
        return frendRequire;
    }

    public String getManager() {
        return manager;
    }

    public boolean isChecked() {
        return checked;
    }

    public int getMember() {
        return member;
    }

    public String getMSN() {
        return MSN;
    }

    public String getICQ() {
        return ICQ;
    }

    public int getOICQ() {
        return OICQ;
    }

    public String getSkinCode() {
        return "default";
        // return skinCode;
    }

    private String name;
    private String gender;

    public String getMemberDesc() {
        if (member==this.MEMBER_COMMON)
            return "普通";
        else if (member==this.MEMBER_SILVER)
            return "银卡";
        else if (member==this.MEMBER_GOLD)
            return "金卡";
        else
            return "";
    }

    public void initDB() {
        this.tableName = "plugin_sweet_userinfo";
        primaryKey = new PrimaryKey("name", PrimaryKey.TYPE_STRING);
        objectCache = new SweetUserInfoCache(this);

        this.QUERY_CREATE = "insert into plugin_sweet_userinfo (name,gender,age,birthday,marriage,province,workAddress,tall,xueli,job,salary,address,postCode,tel,email,OICQ,ICQ,MSN,description,sport,book,music,celebrity,photo,hobby,frendType,frendAge,frendTall,frendMarriage,frendProvince,frendXueli,frendSalary, frendRequire, manager, isChecked,member) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        this.QUERY_SAVE = "update plugin_sweet_userinfo set gender=?,age=?,birthday=?,marriage=?,province=?,workAddress=?,tall=?,xueli=?,job=?,salary=?,address=?,postCode=?,tel=?,email=?,OICQ=?,ICQ=?,MSN=?,description=?,sport=?,book=?,music=?,celebrity=?,photo=?,hobby=?,frendType=?,frendAge=?,frendTall=?,frendMarriage=?,frendProvince=?,frendXueli=?,frendSalary=?, frendRequire=?, manager=?, isChecked=?, member=? where name=?";
        this.QUERY_DEL = "delete from plugin_sweet_userinfo where name=?";
        this.QUERY_LOAD =
                "select gender,age,birthday,marriage,province,workAddress,tall,xueli,job,salary,address,postCode,tel,email,OICQ,ICQ,MSN,description,sport,book,music,celebrity,photo,hobby,frendType,frendAge,frendTall,frendMarriage,frendProvince,frendXueli,frendSalary, frendRequire, manager, isChecked, member from " + tableName + " where name=?";
        this.QUERY_LIST = "select name from plugin_sweet_userinfo";
        isInitFromConfigDB = false;
    }

    private int age;
    private java.util.Date birthday;
    private String marriage;
    private String province;
    private String workAddress;
    private String xueli;
    private String job;
    private String salary;
    private int postCode;
    private String tel;
    private String email;
    private int OICQ;
    private String ICQ;
    private String MSN;
    private String description;
    private String sport;
    private String book;
    private String music;
    private String celebrity;
    private String photo;
    private String hobby;
    private String frendType;
    private String frendAge;
    private String frendTall;
    private String frendMarriage;
    private String frendProvince;
    private String frendXueli;
    private String frendSalary;
    private String frendRequire;
    private String manager;
    private boolean checked;
    private int member;

    //name,gender,age,birthday,marriage,province,workAddress,tall,xueli,job,salary,
    //address,postCode,tel,email,OICQ,ICQ,MSN,description,sport,book,music,celebrity,
    //photo,hobby,frendType,frendAge,frendTall,frendMarriage,frendProvince,frendXueli,
    //frendSalary,frendRequire,manager,isChecked
}
