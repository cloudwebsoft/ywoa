package com.redmoon.oa.base;

import java.util.Date;

public interface IAttachment {

    long getId();

    String getName();

    String getFileSizeMb();

    String getCreatorRealName();

    Date getCreateDate();

    String getPreviewUrl();

    boolean getCanDocInRed();

    boolean getCanSeal();

    String getFieldName();

    boolean isSealed();

    String getCreator();

    String getDiskName();

    void setCanDocInRed(boolean isCanDocInRed);

    void setCanSeal(boolean isCanSeal);

    String getVisualPath();

    boolean del();

    void setRed(boolean red);

    void setSealed(boolean sealed);

    boolean save();

    int getOrders();

    long getSize();

    int getDocId();

    long getVisualId();
}
