package com.redmoon.forum.security.flood;

public class ActionHistory {
	public long time;
	public String uri;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
