package com.redmoon.oa.licenceValidate.diskno;

import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.util.StrUtil;

public class DiskNo {
	private String diskNo;

	public static void main(String[] args) {
		try {
			DiskNo dio = new DiskNo();
			dio.GetDiskNo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DiskNo() {
		diskNo = chenmin.io.DiskID.DiskID();
	}

	public String GetDiskNo() {
		return diskNo;
	}
	
	public static JSONObject GetDiskNoOrMotherboardNo(JSONObject jobject) {
		String diskNo = "";

		try {
			// 因为是32位的dll，在64位机上运行会报错
			// diskNo = StrUtil.getNullString(chenmin.io.DiskID.DiskID());// 获取系统所在机器的硬盘号

			diskNo = CpuNo.getHardDiskSN("c");
			if (diskNo.equals("")) {
				String motherboard = StrUtil.getNullString(CpuNo.getMotherboardSN()); // 获取主板号
				if (motherboard.equals("")) {
					motherboard = "MotherboardCanNotGet";
				} else {
					motherboard = "Motherboard-" + motherboard;
				}

				jobject.put("diskNo", motherboard);
			} else {
				jobject.put("diskNo", diskNo);
			}
		} catch (JSONException e) {
			// 虚拟机某些电脑里无法获取diskId
			e.printStackTrace();
			LogUtil.getLog("com.redmoon.oa.licenceValidate.diskno.DiskNo").error(e);
		} catch (Throwable e) {
			String motherboard = StrUtil.getNullString(CpuNo.getMotherboardSN()); // 获取主板号
			if (motherboard.equals("")) {
				motherboard = "MotherboardCanNotGet";
			} else {
				motherboard = "Motherboard-" + motherboard;
			}

			try {
				jobject.put("diskNo", motherboard);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		return jobject;
	}
}