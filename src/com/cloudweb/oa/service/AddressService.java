package com.cloudweb.oa.service;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.bean.Address;
import com.cloudweb.oa.dao.AddressDao;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.upgrade.service.SpringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Vector;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2018/12/28 14:34
 */
@Service
public class AddressService {

	public static final int TYPE_PUBLIC = 1;
	public static final int TYPE_USER = 0;

	@Autowired
	private AddressDao addressDao;
	@Autowired
	private HttpServletRequest request;

	public AddressService() {
	}

	public AddressDao getAddressDao() {
		if (addressDao == null){
			addressDao = SpringHelper.getBean(AddressDao.class);
		}
		return addressDao;
	}

	public Address getAddress(int id){
		return getAddressDao().getAddress(id);
	}
	public Address getAddressByMobile(String mobile){
		return getAddressDao().getAddressByMobile(mobile);
	}
	public boolean create(Address address) throws ErrMsgException{
		String errmsg = "";
		Privilege privilege = new Privilege();
		if (address.getTypeId().equals("")) {
			errmsg += "请选择类别！";
		}
		if (address.getPerson() == null || address.getPerson().equals("")) {
			errmsg += "姓名不能为空！";
		}
		if (address.getPostalcode().length() > 10) {
			errmsg += "邮政编码长度不能超过10位！\\n";
		}
		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}

		if (address.getType() == TYPE_PUBLIC) {
			if (!privilege.isUserPrivValid(request, "admin.address.public")) {
				throw new ErrMsgException(Privilege.MSG_INVALID);
			}
		}
		// 生成id
		int id = (int) SequenceManager.nextID(SequenceManager.OA_ADDRESS);
		address.setId(id);
		address.setUserName(privilege.getUser(request));
		address.setUnitCode(privilege.getUserUnitCode(request));
		return getAddressDao().create(address);
	}
	public boolean del(int id){
		return getAddressDao().del(id);
	}
	public boolean save(Address address) throws ErrMsgException{
		String errmsg = "";
		Privilege privilege = new Privilege();

		if (address.getTypeId().equals("")) {
			errmsg += "请选择类别！";
		}
		String person = address.getPerson();
		if (person == null || person.equals("")) {
			errmsg += "姓名不能为空！";
		}
		if (address.getPostalcode().length() > 10) {
			errmsg += "邮政编码长度不能超过10位！\\n";
		}
		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}
		if (address.getType() == TYPE_PUBLIC) {
			if (!privilege.isUserPrivValid(request, "admin.address.public")) {
				throw new ErrMsgException(com.redmoon.oa.pvg.Privilege.MSG_INVALID);
			}
		} else {
			if (!privilege.getUser(request).equals(address.getUserName())) {
				throw new ErrMsgException("非法操作！");
			}
		}
		return getAddressDao().save(address);
	}

	/*public ListResult listResult(String userName, int type, String typeId, String person, String company, String mobile, String orderBy, String sort, int pageSize, int currentPage){
		if (type == 1) {
			if (typeId.equals("public")) {
				typeId = "";
			}
		} else {
			if (typeId.equals(userName)) {
				typeId = "";
			}
		}
		List<Address> addressList =  getAddressDao().listResult(userName, type, typeId, "%" + person + "%", "%" + company + "%", "%" + mobile + "%", orderBy, sort,pageSize,(currentPage - 1)*pageSize);
		ListResult listResult = new ListResult();
		int count = getAddressDao().count();
		Vector vector = new Vector();
		vector.addAll(addressList);
		listResult.setTotal(count);
		listResult.setResult(vector);
		return listResult;
	}*/
	public ListResult listSql(String sql){
		List<Integer> addresseIds = getAddressDao().listSql(sql);
		Vector vector = new Vector();
		for (Integer id : addresseIds){
			Address address = getAddress(id);
			vector.add(address);
		}
		ListResult listResult = new ListResult();
		int count = getAddressDao().count("select count(*) from (" + sql + ") as myTable");
		listResult.setTotal(count);
		listResult.setResult(vector);
		return listResult;
	}

	public void delBatch(String ids) throws ErrMsgException{
		String[] idTemp = ids.split(",");
		Privilege privilege = new Privilege();
		String userUnitCode = privilege.getUserUnitCode(request);
		// 首先判断此数据是否存在
		for(String id : idTemp){
			Address address = getAddress(StrUtil.toInt(id));
			if (address == null){
				throw new ErrMsgException("该项已不存在！");
			}
			if (address.getType() == TYPE_PUBLIC) {
				if (!privilege.isUserPrivValid(request, "admin.address.public") || !address.getUnitCode().equals(userUnitCode)) {
					throw new ErrMsgException(Privilege.MSG_INVALID);
				}
			} else {
				if (!privilege.getUser(request).equals(address.getUserName())) {
					throw new ErrMsgException("非法操作！");
				}
			}
			del(StrUtil.toInt(id));
		}
	}

	/**
	 * 用于获取sql语句
	 * @param op
	 * @param userName
	 * @param type
	 * @param typeId
	 * @param person
	 * @param company
	 * @param mobile
	 * @param orderBy
	 * @param sort
	 * @return
	 */
	public String getSql(String op, String userName, int type, String typeId, String person, String company, String mobile, String orderBy, String sort,String unit_code){
		String sql = "select id from address where type=" + type;
		if (type == TYPE_PUBLIC) {
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
			if (type == TYPE_USER)
				sql = "select id from address where userName=" + StrUtil.sqlstr(userName) + " and type=" + TYPE_USER;
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
			if (type != TYPE_PUBLIC)
				sql += " and userName=" + StrUtil.sqlstr(userName);
		}

		if (type == TYPE_PUBLIC) {
			sql += " and unit_code=" + StrUtil.sqlstr(unit_code);
		}

		sql += " order by " + orderBy;
		sql += " " + sort;
		return sql;
	}
}
