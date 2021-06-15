package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;

import javax.servlet.http.HttpServletRequest;

public interface ILicense {

    boolean isPlatform();

    boolean isPlatformSrc();

    boolean isPlatformSuper();

    void init();

    void toXML(String licenseXMLString);

    /**
     * 根据公钥对license.dat进行验证并重建XML Document
     *
     * @return boolean
     */
    boolean verify();

    void validate(HttpServletRequest request) throws ErrMsgException;

    void validate() throws ErrMsgException;

    boolean canUseModule(String moduleCode);

    boolean canUseSolution(String solutionCode);

    boolean isCloudDisk();

    boolean isTrial();

    boolean isBiz();

    boolean isOem();

    boolean isSrc();

    boolean isFree();

    boolean isVip();

    boolean checkSolution(String formCode) throws ErrMsgException;
}
