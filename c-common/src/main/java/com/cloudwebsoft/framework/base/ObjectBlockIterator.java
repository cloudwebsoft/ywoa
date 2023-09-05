package com.cloudwebsoft.framework.base;

import java.util.Iterator;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.KeyUnit;
import java.util.HashMap;

public class ObjectBlockIterator implements Iterator {
    public ObjectBlockIterator() {
    }

    ObjectDb objectDb;

    private Object[] infoBlock;
    private int blockID;
    private int blockStart;

    private String query;

    private int startIndex;
    private int currentIndex;
    private int endIndex;

    private Object previousElement = null;
    private Object nextElement = null;

    String groupKey;

    /**
     * Constructs a new ForumThreadBlockIterator.
     *
     *      through.
     * @param query the SQL query corresponding to this iteration.
     * @param startIndex the starting index of the iteration.
     * @param endIndex the ending index of the iteration.
     */
    public ObjectBlockIterator(ObjectDb objectDb, Object[] block, String query,
                               String groupKey,
                               int startIndex, int endIndex) {
        this.objectDb = objectDb;
        this.infoBlock = block;
        this.blockID = startIndex / objectDb.getBlockSize();
        this.blockStart = blockID * objectDb.getBlockSize();
        this.query = query;
        this.currentIndex = startIndex - 1;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.groupKey = groupKey;
    }

    /**
     * 将指针放在第一个元素前，恢复到构造函数运行后的初始状态
     */
    public void beforeFirst() {
        currentIndex = startIndex - 1;
        previousElement = null;
        nextElement = null;
    }

    public void remove() {

    }

    public boolean hasNext() {
        // If we are at the end of the list there are no more elements.
        if (currentIndex == endIndex) {
            return false;
        }
        // Otherwise, see if nextElement is null. If so, try to load the next
        // element to make sure it exists. If nextElement isn't null, that
        // means we've already checked to make sure it exists.
        if (nextElement == null) {
            nextElement = getNextElement();
            // If getting the next element failed, return false.
            if (nextElement == null) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPrevious() {
        // If we are at the start of the list there are no previous elements.
        if (currentIndex == startIndex) {
            return false;
        }
        // Otherwise, see if previousElement is null. If so, try to load the
        // previous element to make sure it exists.
        if (previousElement == null) {
            previousElement = getPreviousElement();
            // If getting the previous element failed, return false.
            if (previousElement == null) {
                return false;
            }
        }
        return true;
    }

    public Object next() throws java.util.NoSuchElementException {
        Object element = null;
        if (nextElement != null) {
            element = nextElement;
            this.nextElement = null;
        } else {
            element = getNextElement();
            if (element == null) {
                throw new java.util.NoSuchElementException();
            }
        }
        // logger.info("next:" + element);
        return element;
    }

    public Object previous() {
        Object element = null;
        if (previousElement != null) {
            element = previousElement;
            previousElement = null;
        } else {
            element = getPreviousElement();
            if (element == null) {
                throw new java.util.NoSuchElementException();
            }
        }
        return element;
    }

    /**
     * Returns the next element, or null if there are no more
     * elements to return.
     *
     * @return the next element.
     */
    private Object getNextElement() {
        previousElement = null;
        Object element = null;
        if (endIndex != Integer.MAX_VALUE) { // 如果为max_value还用下句则有可能会导致CPU 100%
            while (currentIndex + 1 < endIndex && element == null) {
                currentIndex++;
                element = getElement(currentIndex);
                if (element == null)
                    break;
                // logger.info("getNextElement:currentIndex=" + currentIndex + " element=" + element);
            }
        } else { // 如果next没有，则不再继续找
            if (currentIndex + 1 < endIndex) {
                currentIndex++;
                element = getElement(currentIndex);
            }
        }
        // logger.info("getNextElement:" + element + " currentIndex=" + currentIndex);
        return element;
    }

    /*// 当endIndex受攻击很大时，就会使得循环计算很长时间，因此改为当element为null时退出
    private Object getNextElement() {
      previousElement = null;
      Object element = null;
      if (endIndex!=Integer.MAX_VALUE) { // 如果为max_value还用下句则有可能会导致CPU 100%
        while (currentIndex + 1 < endIndex && element == null) {
          currentIndex++;
          element = getElement(currentIndex);
          // logger.info("getNextElement:currentIndex=" + currentIndex);
        }
      }
      else { // 如果next没有，则不再继续找
        if (currentIndex + 1 < endIndex) {
          currentIndex++;
          element = getElement(currentIndex);
        }
      }
      // logger.info("getNextElement:" + element + " currentIndex=" + currentIndex);
      return element;
    }
    */

   /**
    * Returns the previous element, or null if there are no more elements
    * to return.
    *
    * @return the previous element.
    */
   private Object getPreviousElement() {
       nextElement = null;

       Object element = null;
       while (currentIndex >= startIndex && element == null) {
           currentIndex--;
           element = getElement(currentIndex);
       }
       return element;
   }

    /**
     *
     * @param index int 对应第多少条记录
     * @return Object
     */
    private Object getElement(int index) {
        // logger.info("getElement index=" + index);
        if (index < 0) {
            return null;
        }
        // See if element isn't in the current info block
        // 因为endIndex可能会大于本block的最末尾的序号
        if (index < blockStart || index >= blockStart + objectDb.getBlockSize()) {
            // Then load up the appropriate block
            this.infoBlock = objectDb.getObjectBlock(query, groupKey, index);
            this.blockID = index / objectDb.getBlockSize();
            this.blockStart = blockID * objectDb.getBlockSize();
        }
        Object element = null;
        // Compute the relative index of the element, which is the index in the
        // current thread block.
        int relativeIndex = index % objectDb.getBlockSize();
        // Make sure index isn't too large
        if (relativeIndex < infoBlock.length) {
            element = objectDb.getObjectDb(infoBlock[relativeIndex]);
            // logger.info("getElement:infoBlock=" + infoBlock[relativeIndex] + " emement=" + element);
        }
        return element;
    }

    /**
     * 置obj的索引
     * @param obj ObjectDb
     */
    public void setIndex(ObjectDb obj) {
        // Set nextElement and previousElement to null since we may be moving
        // the index.
        nextElement = null;
        previousElement = null;

        // Scan through all blocks looking for thread.
        Object[] currentBlock;
        for (int i = startIndex; i < endIndex; i++) {
            currentBlock = obj.getObjectBlock(query, groupKey, i);
            if (currentBlock.length == 0) {
                //throw new ErrMsgException("Thread with id " +
                //    threadID + " is not a valid index in the iteration.");
            }
            int blockID = i / obj.getBlockSize();
            int blockEnd = blockID * obj.getBlockSize() +
                           obj.getBlockSize();
            // If in first block of threads
            if (startIndex < blockEnd) {
                // If we are in the first block, j should start at the
                // start index instead of the beginning of the block.
                for (int j = startIndex % obj.getBlockSize();
                             j < currentBlock.length; j++, i++) {
                    if (comparePrimarKey(obj.getPrimaryKey(), currentBlock[j])) {
                        this.currentIndex = i;
                        return;
                    }
                }
            }
            // Otherwise, not in first block so start looking at beginning
            else {
                for (int j = 0; j < currentBlock.length; j++, i++) {
                    if (comparePrimarKey(obj.getPrimaryKey(), currentBlock[j])) {
                        this.currentIndex = i;
                        return;
                    }
                }
            }
        }
    }

    /**
     * 对比主键是否一致
     * @param primaryKey PrimaryKey
     * @param objKey Object 主键中的值
     * @return boolean
     */
    public boolean comparePrimarKey(PrimaryKey primaryKey, Object objKey) {
        // 如果不是复合主键
        if (primaryKey.getKeyCount() == 1) {
            if (primaryKey.getType() == primaryKey.TYPE_LONG)
                return primaryKey.getLongValue() == ((Long) objKey).longValue();
            else if (primaryKey.getType() == primaryKey.TYPE_INT)
                return primaryKey.getIntValue() == ((Integer) objKey).intValue();
            else if (primaryKey.getType() == primaryKey.TYPE_STRING)
                return primaryKey.getStrValue().equals(((String) objKey));
            else if (primaryKey.getType() == primaryKey.TYPE_DATE)
              return primaryKey.getDateValue().equals((java.util.Date)objKey);
        } else if (primaryKey.getType() == primaryKey.TYPE_COMPOUND) { // 如果是复合主键
            HashMap keys = primaryKey.getKeys();
            Iterator ir = keys.keySet().iterator();
            HashMap ks = (HashMap) objKey;
            while (ir.hasNext()) {
                String keyName = (String) ir.next();
                KeyUnit ku = (KeyUnit) keys.get(keyName);
                KeyUnit ku2 = (KeyUnit) ks.get(keyName);
                if (ku2 == null)
                    throw new IllegalArgumentException("The key " + keyName +
                            " is not exists in Primary");
                return ku.getValue().equals(ku2.getValue());
            }
        }
        return false;
    }
}
