package com.redmoon.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for getDirectoryOptions complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="getDirectoryOptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDirectoryOptions", propOrder = { "arg0" })
public class GetDirectoryOptions {

	protected boolean arg0;

	/**
	 * Gets the value of the arg0 property.
	 * 
	 */
	public boolean isArg0() {
		return arg0;
	}

	/**
	 * Sets the value of the arg0 property.
	 * 
	 */
	public void setArg0(boolean value) {
		this.arg0 = value;
	}

}
