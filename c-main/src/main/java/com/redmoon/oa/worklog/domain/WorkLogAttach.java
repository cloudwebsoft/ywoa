package com.redmoon.oa.worklog.domain;
/**
 * 工作汇报附件表基础类
 * @author jfy
 * @date Jul 9, 2015
 */
public class WorkLogAttach {
	
	private int id;
	//汇报主表ID
	private int workLogId;
	//附件名称
	private String name;
	//附件随机名称
	private String diskName;
	//虚拟路径
	private String visualPath;
	//文件大小
	private long fileSize;
	//文件排序
	private int orders;

	public int getWorkLogId() {
		return workLogId;
	}

	public void setWorkLogId(int workLogId) {
		this.workLogId = workLogId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}

	public String getVisualPath() {
		return visualPath;
	}

	public void setVisualPath(String visualPath) {
		this.visualPath = visualPath;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}
