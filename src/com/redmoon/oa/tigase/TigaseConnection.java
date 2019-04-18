package com.redmoon.oa.tigase;

import java.util.*;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smack.Roster;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.post.PostDb;
import com.redmoon.oa.post.PostUserDb;
import com.redmoon.oa.post.PostUserMgr;
import com.redmoon.oa.pvg.RoleDb;

/**
 * @Description:
 * @author:
 * @Date: 2016-5-30上午10:12:39
 */
public class TigaseConnection {
	private XMPPConnection connection;
	private ConnectionConfiguration config;
	private String host;
	private int port;
	private boolean isUse;
	private String exceptions;
	private String defaultPwd;
	private String deptCode;

	public TigaseConnection() {
		Config cfg = new Config();
		host = cfg.get("tigaseHost");
		port = cfg.getInt("tigasePort");
		isUse = cfg.getBooleanProperty("isUse");
		config = new ConnectionConfiguration(host, port);
		config.setSASLAuthenticationEnabled(false);
		config.setDebuggerEnabled(false);
		config.setSecurityMode(SecurityMode.disabled);
		connection = new XMPPConnection(config);
		exceptions = cfg.get("exceptions");
		defaultPwd = cfg.get("defaultPwd");
	}

	public void addUser(String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb user = new UserDb(userName);
			// 同步system,可能在usres里没有这个账号
			if ((((user == null || !user.isLoaded()) && userName
					.equals(UserDb.SYSTEM)) || user.getName().equals(
					UserDb.SYSTEM))
					&& (user.getPwdMD5() == null || user.getPwdMD5().equals(""))) {
				user.setName(userName);
				user.setRealName(userName);
				user.setPwdMD5(SecurityUtil.MD5(defaultPwd));
			}
			connection.getAccountManager().createAccount(
					user.getName().toLowerCase(), user.getPwdMD5());
			System.out.println("create user " + user.getRealName()
					+ " successfully!");
			connection.login(user.getName(), user.getPwdMD5());
			setRoster(user);
			System.out.println("setRoster " + user.getRealName()
					+ " successfully!");
		} catch (XMPPException e) {
			System.out.println(userName + "登录失败!");
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	public void syncUser(String userName, String oldPwd) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb user = new UserDb(userName);
			if (user.isValid()) {
				if (oldPwd == null || oldPwd.equals("")) {
					connection.login(userName, user.getPwdMD5());
				} else {
					connection.login(userName, oldPwd);
					if (!oldPwd.equals(user.getPwdMD5())) {
						connection.getAccountManager().changePassword(
								user.getPwdMD5());
					}
				}
				setRoster(user);
			} else {
				connection.login(userName, user.getPwdMD5());
				// 先删除个人信息,再删除账号
				delRoster();
				connection.getAccountManager().deleteAccount();
			}
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	public void delUser(String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb user = new UserDb(userName);
			connection.login(userName, user.getPwdMD5());
			// 先删除个人信息,再删除账号
			delRoster();
			connection.getAccountManager().deleteAccount();
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	public void syncDept(String code, String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb ud = new UserDb(userName);
			connection.login(userName, ud.getPwdMD5());
			DeptDb dd = new DeptDb(code);
			Map<String, String> map = new HashMap<String, String>();
			map.put("_id", code);
			map.put("name", dd.getName());
			map.put("parent_id", dd.getParentCode());
			map.put("layer", String.valueOf(dd.getLayer()));
			map.put("order", String.valueOf(dd.getOrders()));
			SyncManager sync = new SyncManager(connection);
			sync.syncDept(map);
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	public void delDept(String code, String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb ud = new UserDb(userName);
			connection.login(userName, ud.getPwdMD5());
			SyncManager sync = new SyncManager(connection);
			sync.removeDept(code);
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	public void deleteAll(String userName) {
		delAllDepts(userName);
		delAllUsers(userName);
	}

	public String getAllDepts() {
		if (!isUse) {
			return "";
		}
		String result = "";
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			connection.login("admin", "e2faa530a22c018a2ec2815384f171a5");
			SyncManager sync = new SyncManager(connection);
			result = sync.getAllDepts();
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
		return result;
	}

	public void setOAInfo(String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb ud = new UserDb(userName);
			connection.login(userName, ud.getPwdMD5());
			SyncManager sync = new SyncManager(connection);
			sync.setOAInfo();
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	public void sendMsg(String fromUser, String toUser, String text) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb user = new UserDb(fromUser);
			// 同步system,可能在usres里没有这个账号
			if ((((user == null || !user.isLoaded()) && fromUser
					.equals(UserDb.SYSTEM)) || user.getName().equals(
					UserDb.SYSTEM))
					&& (user.getPwdMD5() == null || user.getPwdMD5().equals(""))) {
				user.setName(fromUser);
				user.setRealName(MessageDb.SENDER_SYSTEM);
				user.setPwdMD5(SecurityUtil.MD5(defaultPwd));
			}
			connection.login(fromUser, user.getPwdMD5());
			connection.getChatManager().createChat(
					toUser.toLowerCase() + "@" + host, new MessageListener() {
						@Override
						public void processMessage(Chat chat, Message message) {

						}
					}).sendMessage(text);
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	private void delAllDepts(String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb ud = new UserDb(userName);
			connection.login(userName, ud.getPwdMD5());
			SyncManager sync = new SyncManager(connection);
			sync.removeAllDepts();
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	@SuppressWarnings("unused")
	private void createUser(UserDb user) throws XMPPException {
		if (!isUse) {
			return;
		}
		// AccountManager am = new AccountManager(connection);
		connection.getAccountManager().createAccount(user.getName(),
				user.getPwdMD5());

		// 本打算将dept_code,realname直接存放在tig_users表,后发现存在在tig_nodes表更易于扩展
		/*
		 * Map<String, String> attributes = new HashMap<String, String>();
		 * AccountManager am = new AccountManager(connection); for (String
		 * attributeName : am.getAccountAttributes()) {
		 * attributes.put(attributeName, ""); } Registration reg = new
		 * Registration(); reg.setType(IQ.Type.SET);
		 * reg.setTo(connection.getServiceName()); attributes.put("username",
		 * user.getName()); attributes.put("password", user.getPwdMD5());
		 * DeptUserDb dud = new DeptUserDb(user.getName());
		 * attributes.put("dept_code", dud.getDeptCode());
		 * reg.setAttributes(attributes); PacketFilter filter = new
		 * AndFilter(new PacketIDFilter(reg .getPacketID()), new
		 * PacketTypeFilter(IQ.class)); PacketCollector collector =
		 * connection.createPacketCollector(filter); connection.sendPacket(reg);
		 * IQ result = (IQ) collector.nextResult(SmackConfiguration
		 * .getPacketReplyTimeout()); collector.cancel(); if (result == null) {
		 * throw new XMPPException("No response from server."); } else if
		 * (result.getType() == IQ.Type.ERROR) { throw new
		 * XMPPException(result.getError()); }
		 */
	}

	private void setRoster(UserDb user) throws XMPPException {
		if (deptCode != null && !deptCode.equals("")) {
			DeptDb dd = new DeptDb(deptCode);
			DeptUserDb dud = new DeptUserDb();
			setRoster(user, deptCode, dd.getName(), dud.getMaxOrders(deptCode) + 1);
		} else {
			DeptUserDb dud = new DeptUserDb(user.getName());
			setRoster(user, dud.getDeptCode(), dud.getDeptName(), dud.getOrders());
		}
	}

	private void setRoster(UserDb user, String deptCode, String deptName,
			int orders) throws XMPPException {
		if (!isUse) {
			return;
		}
		VCard vc = new VCard();
		vc.setNickName(user.getRealName());
		vc.setOrganizationUnit(deptCode);
		vc.setOrganization(deptName);
		vc.setField("ORDER", String.valueOf(orders));
		vc.setEmailWork(user.getEmail());
		vc
				.setField("GENDER", user.getGender() == UserDb.GENDER_MAN ? "男"
						: "女");
		vc.setField("MOBILE", user.getMobile());
		vc.setField("QQ", user.getQQ());
		vc.setField("SHORT", user.getMSN());
		if (user.getPhoto() != null && !user.getPhoto().equals("")) {
			// try {
			// File file = new File(Global.getAppPath() + "/" +
			// user.getPhoto());
			// byte[] bytes = TigaseUtil.getFileBytes(file);
			// vc.setAvatar(bytes, new
			// MimetypesFileTypeMap().getContentType(file));
			// } catch (Exception e) {
			// e.printStackTrace();
			// LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			// }
			vc.setField("PHOTOURL", user.getPhoto());
		}

		// 我的角色
		com.redmoon.oa.pvg.RoleDb[] rld = user.getRoles();
		// int num = 0;
		int rolelen = 0;
		if (rld != null)
			rolelen = rld.length;
		String roleNames = "";
		for (int k = 0; k < rolelen; k++) {
			if (rld[k].getCode().equals(RoleDb.CODE_MEMBER)) {
				continue;
				// if (++num == 2) {
				// continue;
				// }
			}
			if (roleNames.equals(""))
				roleNames = rld[k].getDesc() != null ? rld[k].getDesc() : "";// 防止rld[k].getDesc()为null报错问题
			else
				roleNames += "，"
						+ (rld[k].getDesc() != null ? rld[k].getDesc() : "");
		}
		if (roleNames.equals("")) {
			roleNames = new RoleDb(RoleDb.CODE_MEMBER).getDesc();
		}
		vc.setField("ROLE", roleNames);

		// 我的岗位
		PostUserMgr puMgr = new PostUserMgr();
		puMgr.setUserName(user.getName());
		PostUserDb pudb = puMgr.postByUserName();
		if (pudb != null && pudb.isLoaded()) {
			PostDb pdb = new PostDb();
			pdb = pdb.getPostDb(pudb.getInt("post_id"));
			if (pdb != null && pdb.isLoaded()) {
				vc.setField("POST", pdb.getString("name"));
			}
		}
		vc.save(connection);
	}

	private void delRoster() throws XMPPException {
		VCard vcard = new VCard();
		vcard.setField("REMOVE", "");
		vcard.save(connection);
	}

	private void delAllRoster() throws XMPPException {
		VCard vcard = new VCard();
		vcard.setField("REMOVEALL", "");
		vcard.setField("EXCEPTIONS", exceptions);
		vcard.save(connection);
	}

	private void delAllUsers(String userName) {
		if (!isUse) {
			return;
		}
		UserDb ud = new UserDb(userName);
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			connection.login(userName, ud.getPwdMD5());
			// 先清除个人节点数据
			delAllRoster();

			Registration reg = new Registration();
			reg.setType(IQ.Type.SET);
			reg.setTo(connection.getServiceName());
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("removeAll", "");
			attributes.put("exceptions", getExceptionsXML(userName));
			reg.setAttributes(attributes);
			PacketFilter filter = new AndFilter(new PacketIDFilter(reg
					.getPacketID()), new PacketTypeFilter(IQ.class));
			PacketCollector collector = connection
					.createPacketCollector(filter);
			connection.sendPacket(reg);
			IQ result = (IQ) collector.nextResult(SmackConfiguration
					.getPacketReplyTimeout());
			collector.cancel();
			if (result == null) {
				LogUtil.getLog(getClass()).error("No response from server.");
			} else if (result.getType() == IQ.Type.ERROR) {
				LogUtil.getLog(getClass()).error(result.getError().toString());
			}
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	private String getExceptionsXML(String userName) {
		String[] ex = exceptions.split(",");
		StringBuilder sb = new StringBuilder();
		for (String str : ex) {
			sb.append("<name>").append(str).append("</name>");
		}
		sb.append("<name>").append(userName).append("</name>");
		return sb.toString();
	}
	
	public void addFriends2(String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb ud = new UserDb(userName);
			connection.login(userName, ud.getPwdMD5());
			SyncManager sync = new SyncManager(connection);
			sync.makeFriends();
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}
	
	public void addMyFriends2(String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb ud = new UserDb(userName);
			connection.login(userName, ud.getPwdMD5());
			SyncManager sync = new SyncManager(connection);
			sync.makeFriend(ud.getRealName());
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			connection.disconnect();
		}
	}

	public void addFriends() {
		if (!isUse) {
			return;
		}
		Map<String, XMPPConnection> conns = new HashMap<String, XMPPConnection>();
		try {
			UserDb ud = new UserDb();
			Vector v = ud.list();
			for (int i = 0; i < v.size(); i++) {
				UserDb user = (UserDb) v.get(i);
				if (user == null || !user.isLoaded()) {
					continue;
				}
				if (user.getName().equals(UserDb.SYSTEM)) {
					continue;
				}
				XMPPConnection conn = null;
				if (conns.containsKey(user.getName())) {
					conn = conns.get(user.getName());
				} else {
					conn = new XMPPConnection(config);
					conn.connect();
					conn.login(user.getName(), user.getPwdMD5());
					conns.put(user.getName(), conn);
				}
				Roster roster = conn.getRoster();
				roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
				for (int j = i + 1; j < v.size(); j++) {
					UserDb user2 = (UserDb) v.get(j);
					XMPPConnection conn2 = null;
					if (conns.containsKey(user2.getName())) {
						conn2 = conns.get(user2.getName());
					} else {
						try {
							conn2 = new XMPPConnection(config);
							conn2.connect();
							conn2.login(user2.getName(), user2.getPwdMD5());
							conns.put(user2.getName(), conn2);
						} catch (XMPPException e) {
							System.out.println(user2.getRealName() + "登录失败!");
							e.printStackTrace();
							LogUtil.getLog(getClass()).error(StrUtil.trace(e));
							continue;
						}
					}
					try {
						roster.createEntry(user2.getName() + "@" + host, user2
								.getRealName(), new String[] { "Friends" });
						System.out.println(conn.getUser()
								+ " has made friends with "
								+ user2.getRealName());

						Roster roster2 = conn2.getRoster();
						roster2
								.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
						roster2.createEntry(user.getName() + "@" + host, user
								.getRealName(), new String[] { "Friends" });
						System.out.println(conn2.getUser()
								+ " has made friends with "
								+ user.getRealName());
					} catch (XMPPException e) {
						e.printStackTrace();
						LogUtil.getLog(getClass()).error(StrUtil.trace(e));
					}
				}
			}
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			for (Map.Entry<String, XMPPConnection> e : conns.entrySet()) {
				XMPPConnection conn = e.getValue();
				if (conn.isConnected()) {
					conn.disconnect();
				}
			}
		}
	}

	public void addMyFriends(String userName) {
		if (!isUse) {
			return;
		}
		try {
			if (connection == null) {
				connection = new XMPPConnection(config);
			}
			if (!connection.isConnected()) {
				connection.connect();
			}
			UserDb ud = new UserDb(userName);
			connection.login(ud.getName(), ud.getPwdMD5());
			Roster roster = connection.getRoster();
			roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
			Vector v = ud.list();
			Iterator it = v.iterator();
			while (it.hasNext()) {
				UserDb user = (UserDb) it.next();
				if (user == null || !user.isLoaded()) {
					continue;
				}
				if (user.getName().equals(UserDb.SYSTEM)) {
					continue;
				}
				if (connection.getUser().startsWith(
						user.getName().toLowerCase() + "@" + host)) {
					continue;
				}
				XMPPConnection conn = null;
				try {
					roster.createEntry(user.getName() + "@" + host, user
							.getRealName(), new String[] { "Friends" });
					System.out.println(connection.getUser()
							+ " has made friends with " + user.getRealName());

					conn = new XMPPConnection(config);
					conn.connect();
					conn.login(user.getName(), user.getPwdMD5());
					Roster roster2 = conn.getRoster();
					roster2
							.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
					roster2.createEntry(userName + "@" + host,
							ud.getRealName(), new String[] { "Friends" });
					System.out.println(conn.getUser()
							+ " has made friends with " + ud.getRealName());
				} catch (XMPPException e) {
					e.printStackTrace();
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				} finally {
					if (conn != null && conn.isConnected()) {
						conn.disconnect();
					}
				}
			}
		} catch (XMPPException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (connection.isConnected()) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * @return the deptCode
	 */
	public String getDeptCode() {
		return deptCode;
	}

	/**
	 * @param deptCode the deptCode to set
	 */
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public static void main(String[] args) {
		TigaseConnection tc = new TigaseConnection();
		tc.getAllDepts();
	}

}
