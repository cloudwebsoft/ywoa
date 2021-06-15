package com.cloudweb.oa.bean;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2018/12/24 16:00
 */
@Component("Address")
public class Address implements Serializable {
	private int id;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalcode() {
		return postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getFamilyname() {
		return familyname;
	}

	public void setFamilyname(String familyname) {
		this.familyname = familyname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getCompanyStreet() {
		return companyStreet;
	}

	public void setCompanyStreet(String companyStreet) {
		this.companyStreet = companyStreet;
	}

	public String getCompanyCity() {
		return companyCity;
	}

	public void setCompanyCity(String companyCity) {
		this.companyCity = companyCity;
	}

	public String getCompanyPostcode() {
		return companyPostcode;
	}

	public void setCompanyPostcode(String companyPostcode) {
		this.companyPostcode = companyPostcode;
	}

	public String getCompanyProvice() {
		return companyProvice;
	}

	public void setCompanyProvice(String companyProvice) {
		this.companyProvice = companyProvice;
	}

	public String getCompanyCountry() {
		return companyCountry;
	}

	public void setCompanyCountry(String companyCountry) {
		this.companyCountry = companyCountry;
	}

	public String getOperationweb() {
		return operationweb;
	}

	public void setOperationweb(String operationweb) {
		this.operationweb = operationweb;
	}

	public String getOperationPhone() {
		return operationPhone;
	}

	public void setOperationPhone(String operationPhone) {
		this.operationPhone = operationPhone;
	}

	public String getOperationFax() {
		return operationFax;
	}

	public void setOperationFax(String operationFax) {
		this.operationFax = operationFax;
	}

	public String getBeepPager() {
		return BeepPager;
	}

	public void setBeepPager(String beepPager) {
		BeepPager = beepPager;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getQQ() {
		return QQ;
	}

	public void setQQ(String QQ) {
		this.QQ = QQ;
	}

	public String getMSN() {
		return MSN;
	}

	public void setMSN(String MSN) {
		this.MSN = MSN;
	}

	public String getWeixin() {
		return weixin;
	}

	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}
}
