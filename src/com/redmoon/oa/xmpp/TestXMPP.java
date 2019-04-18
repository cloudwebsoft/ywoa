package com.redmoon.oa.xmpp;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.Chat;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class TestXMPP {

	public static void chatuser(final Connection conn) {

		ChatManager manager = conn.getChatManager();
		final MessageListener messagelistener = new MessageListener() {
			public void processMessage(Chat chat, Message message) {
				System.out.println(conn.getUser() + " Received message:"
						+ message.getBody());
			}

		};
		manager.addChatListener(new ChatManagerListener() {
			public void chatCreated(Chat chat, boolean createdLocally) {
				if (!createdLocally)
					chat.addMessageListener(messagelistener);

			}
		});
	}

	public static short IsUserOnLine(String host, int port, String jid) {
		short shOnLineState = 0; // -不存在-

		try {
			String strUrl = "http://" + host + ":" + port + "/plugins/presence/status?jid=" + jid + "@" + host + "&type=xml";
			URL oUrl = new URL(strUrl);
			URLConnection oConn = oUrl.openConnection();
			if (oConn != null) {
				BufferedReader oIn = new BufferedReader(new InputStreamReader(
						oConn.getInputStream()));
				if (null != oIn) {
					String strFlag = oIn.readLine();
					
					System.out.println("strFlag:" + strFlag);
					
					oIn.close();

					if (strFlag.indexOf("type=\"unavailable\"") >= 0) {
						shOnLineState = 2;
					}
					if (strFlag.indexOf("type=\"error\"") >= 0) {
						shOnLineState = 0;
					} else if (strFlag.indexOf("priority") >= 0
							|| strFlag.indexOf("id=\"") >= 0) {
						shOnLineState = 1;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("online state:" + shOnLineState);
		return shOnLineState;
	}

	public static void sendPacket() {
		IsUserOnLine("localhost", 9090, "jack");
		
		ConnectionConfiguration config = new ConnectionConfiguration(
				"127.0.0.1", 5222); // 根据msg服务器ip和端口，获取配置信息
		XMPPConnection conn = null;
		conn = new XMPPConnection(config);

		try {
			conn.connect();
			conn.login("jack", "jack"); // 登录msg服务器
		} catch (XMPPException e) {
			conn.disconnect();
			e.printStackTrace();
		}

		String domain = "";
		String sendUserJID = "james@localhost/xmsg";
		String recvUserJID = "jack@localhost";
		long threadID = System.currentTimeMillis();
		String thread = String.valueOf(threadID);
		double flag = Math.random() * 1000;
		String packetID = "xmsg_" + String.valueOf(flag);

		Message msgX = new Message();
		msgX.setFrom(sendUserJID);
		msgX.setTo(recvUserJID);
		msgX.setSubject("消息标题");
		msgX.setType(Message.Type.normal);
		msgX.setThread(thread);
		msgX.setPacketID(packetID);

		String S = "开会通知: http://localhost:8080/oa/ <a href=\"http://OA_X/meeting/process/boardroomUseApplyDetail/mainActivity.w?applyID=123\">会议详细内容</a>";
		DefaultPacketExtension ext = new DefaultPacketExtension("html",
				"http://jabber.org/protocol/xhtml-im");
		ext.setValue("body", S);
		msgX.setBody(S); // 发送内容
		msgX.addExtension(ext);

		conn.sendPacket(msgX);

		conn.disconnect();

	}

	// 主函数，与服务器连接、用户登录、发送消息都是从这里实现的
	public static void main(String[] args) {
		sendPacket();
		
		if (true)
			return;
		
		ConnectionConfiguration config = new ConnectionConfiguration(
				"127.0.0.1", 5222); // 第一个参数是你的openfire服务器地址，5222是openfire通信的端口
		config.setCompressionEnabled(true);
		config.setSASLAuthenticationEnabled(true);

		Connection connection = new XMPPConnection(config);
		// Connection conn = new XMPPConnection(config);

		try {
			connection.connect();
			// conn.connect();
			connection.login("james", "james");
			// conn.login("以陌", "111111");

			System.out.println("user=" + connection.getUser());
			
			ChatManager chatManager = connection.getChatManager();
			Chat chat = chatManager.createChat("jack@192.168.1.101/Smack",
					new MessageListener() { // 这里第一个参数是接收人的地址，不能只写用户名，具体是什么可以在上面先用System.out.println(conn.getUser());把用户的具体表示方法打印出来看看
						public void processMessage(Chat chat, Message message) {
							System.out.println("Received message:" + message.getBody());
						}
					});
			while (true) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				String message = "";
				System.out.println(connection.getUser()
						+ " Please input the message:");
				try {
					message = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// chatuser(conn);
				chat.sendMessage(message);
			}
		} catch (XMPPException e) {
			e.printStackTrace();
			System.out.println("Error Delivering block");
		}

		connection.disconnect();
	}
}
