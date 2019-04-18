package com.redmoon.oa.licenceValidate.diskno;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Scanner;

public class CpuNo {

    /**
     * 获取主板序列号
     *
     * @return
     */
    public static String getMotherboardSN() {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_BaseBoard\") \n"
                    + "For Each objItem in colItems \n"
                    //+ "    Wscript.Echo objItem.SerialNumber \n"
                    + "    Wscript.Echo objItem.Product \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();

            if(result.contains("输入错误")){
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return result.trim();
    }

    /**
     * 获取硬盘序列号
     *
     * @param drive
     *            盘符
     * @return
     */
    public static String getHardDiskSN(String drive) {
        String result = "";
        try {
            // File file = File.createTempFile("realhowto", ".vbs",new File("C:\\"));
            File file = File.createTempFile("DiskSN", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\""
                    + drive
                    + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber"; // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
    }


    /**
     * 纯java获取CPU序列号
     *
     * @param
     *
     * @return
     * @throws IOException
     */
    public static String getCpuNo() throws IOException {
        Process process = Runtime.getRuntime().exec(
                new String[] { "wmic", "cpu", "get", "ProcessorId" });
        process.getOutputStream().close();
        Scanner sc = new Scanner(process.getInputStream());
        String serial = sc.next();

        return serial;
    }

    /**
     * 获取CPU序列号
     *
     * @return
     */
    public static String getCPUSerial() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_Processor\") \n"
                    + "For Each objItem in colItems \n"
                    + "    Wscript.Echo objItem.ProcessorId \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";



            // + "    exit for  \r\n" + "Next";
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if (result.trim().length() < 1 || result == null) {
            result = "无CPU_ID被读取";
        }
        return result.trim();
    }

    /**
     * 获取MAC地址
     */
    public static String getMac() {
        String result = "";
        try {

            Process process = Runtime.getRuntime().exec("ipconfig /all");

            InputStreamReader ir = new InputStreamReader(
                    process.getInputStream(), "GBK");

            LineNumberReader input = new LineNumberReader(ir);

            String line;

            while ((line = input.readLine()) != null)
                // System.out.println(line);
                if (line.indexOf("Physical Address") > 0 || line.indexOf("物理地址") > 0) {
                    String MACAddr = line.substring(line.indexOf("-") - 2);
                    result = MACAddr;
                }

        } catch (java.io.IOException e) {

            System.err.println("IOException " + e.getMessage());

        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("CPU SN:" + CpuNo.getCPUSerial());
        System.out.println("主板 SN:" + CpuNo.getMotherboardSN());
        System.out.println("C盘 SN:" + CpuNo.getHardDiskSN("c"));
        System.out.println("MAC SN:" + CpuNo.getMac());
        System.out.println("------------------");
        System.out.println("cpu:" + ": " + getCpuNo());
    }

}