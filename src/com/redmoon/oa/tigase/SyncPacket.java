package com.redmoon.oa.tigase;

import java.util.*;

import org.jivesoftware.smack.packet.IQ;

/**
 * @Description:
 * @author:
 * @Date: 2016-6-6上午09:23:43
 */
public class SyncPacket extends IQ {

	public final static String ELEMENTNAME = "query";
	public final static String NAMESPACE = "jabber:iq:sync";

	private Map attributes;

	public SyncPacket() {
		attributes = null;
	}

	/**
	 * @return the attributes
	 */
	public Map getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}

	/**
	 * @Description:
	 * @return
	 */
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(ELEMENTNAME).append(" xmlns=\"").append(
				NAMESPACE).append("\">");
		if (attributes != null && attributes.size() > 0) {
			Iterator it = attributes.keySet().iterator();
			boolean isGet = false;
			if (attributes.containsKey(SyncProvider.SET_MAP_KEY)
					&& attributes.get(SyncProvider.SET_MAP_KEY) == null) {
				isGet = true;
			}
			while (it.hasNext()) {
				String name = (String) it.next();
				if (isGet) {
					if (name.equals(SyncProvider.SET_MAP_KEY)) {
						continue;
					}
					Map values = (Map) attributes.get(name);
					if (values != null) {
						buf.append("<item>");
						Iterator subit = values.keySet().iterator();
						while (subit.hasNext()) {
							String sn = (String) subit.next();
							String value = (String) values.get(sn);
							buf.append("<").append(sn).append(">");
							buf.append(value).append("</").append(sn).append(">");
						}
						buf.append("</item>");
					}
				} else {
					String value = (String) attributes.get(name);
					buf.append("<").append(name).append(">");
					buf.append(value).append("</").append(name).append(">");
				}
			}
		}
		buf.append(getExtensionsXML());
		buf.append("</").append(ELEMENTNAME).append(">");
		return buf.toString();
	}

}
