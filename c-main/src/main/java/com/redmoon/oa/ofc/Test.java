package com.redmoon.oa.ofc;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.js.fan.util.StrUtil;
import cn.sendsms.helper.CommPortIdentifier;

import com.redmoon.webservice.DocService;
import com.redmoon.webservice.DocServiceService;

import bsh.Interpreter;

import jofc2.model.Chart;
import jofc2.model.axis.XAxis;
import jofc2.model.axis.YAxis;
import jofc2.model.elements.BarChart;
import jofc2.model.elements.PieChart;
import jofc2.model.elements.LineChart;
import jofc2.model.elements.BarChart.Bar;

public class Test {
    public static void main(String[] args) throws Exception {
    	System.out.println(Pattern.compile("'").matcher("wsss'f").find());
    }	
}
