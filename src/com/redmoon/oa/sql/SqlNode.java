package com.redmoon.oa.sql;

import java.util.ArrayList;
import java.util.List;

public class SqlNode
{
  private SqlNode _this = this;
  private String left;
  private String right;
  private String betweenStart;
  private String betweenEnd;
  private int conditionInt;
  private String condition;
  private boolean atom;
  private SqlNode leftchild = null;

  private SqlNode rightchild = null;
  private boolean function;
  private List<byte[]> inList = new ArrayList(10);

  public void setInList(String[] arr) {
    this.inList.clear();
    if (arr == null) {
      return;
    }
    for (String str : arr) {
      str = str.trim();
      this.inList.add(str.getBytes());
    }
  }

  public void setSelfNull() {
    this._this = null;
  }

  public String getBetweenStart() {
    return this.betweenStart;
  }

  public void setBetweenStart(String betweenStart) {
    this.betweenStart = betweenStart;
  }

  public String getBetweenEnd() {
    return this.betweenEnd;
  }

  public void setBetweenEnd(String betweenEnd) {
    this.betweenEnd = betweenEnd;
  }

  public int getConditionInt() {
    return this.conditionInt;
  }

  public void setConditionInt(int conditionInt) {
    this.conditionInt = conditionInt;
  }

  public boolean isAtom() {
    return this.atom;
  }

  public void setAtom(boolean atom) {
    this.atom = atom;
  }

  public String getLeft() {
    return this.left;
  }

  public void setLeft(String left) {
    this.left = left;
  }

  public String getRight() {
    return this.right;
  }

  public void setRight(String right) {
    this.right = right;
  }

  public String getCondition() {
    return this.condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public SqlNode getLeftchild() {
    return this.leftchild;
  }

  public void setLeftchild(SqlNode leftchild) {
    this.leftchild = leftchild;
  }

  public SqlNode getRightchild() {
    return this.rightchild;
  }

  public void setRightchild(SqlNode rightchild) {
    this.rightchild = rightchild;
  }

  public boolean isFunction() {
    return this.function;
  }

  public void setFunction(boolean function) {
    this.function = function;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("condition:" + this.condition + "  left child:" + (
      getLeftchild() == null) + "  right child:" + (
      getRightchild() == null));
    if ((this.left != null) && (this.conditionInt != 2) &&
      (this.conditionInt != 3)) {
      sb.append("\r\nleft is:" + this.left + "  and right is:" +
        new String(getRight()));
    } else if (this.conditionInt == 2) {
      sb.append("\r\nleft is:" + this.left + "  and between is:" +
        new String(getBetweenStart()) + "  and " +
        new String(getBetweenEnd()));
    } else if (this.conditionInt == 3) {
      sb.append("\r\nleft is:" + this.left + "  and in is:");
      for (byte[] bytes : this.inList)
      {
        sb.append(new String(bytes)).append(",");
      }
    }

    return sb.toString();
  }
}