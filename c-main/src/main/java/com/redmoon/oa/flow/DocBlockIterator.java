package com.redmoon.oa.flow;

public class DocBlockIterator {
  //如果是在搜索时，则groupKey取all，如果是dir_code类别的文章列表，则取dir_code
  //当del create文章时，需要对groupKey组的缓存进行更新
  String groupKey;

  private long[] infoBlock;

  private int blockID;
  private int blockStart;

  private String query;

  private int startIndex;
  private int currentIndex;
  private int endIndex;

  private Object previousElement = null;
  private Object nextElement = null;

  /**
   * Constructs a new ForumThreadBlockIterator.
   *
   * @param threadBlock the starting threadBlock of elements to iterate
   *      through.
   * @param query the SQL query corresponding to this iteration.
   * @param startIndex the starting index of the iteration.
   * @param endIndex the ending index of the iteration.
   * @param forumID the forumID the threads are a part of.
   * @param factory a ForumFactory to load data from.
   */
  protected DocBlockIterator(long[] threadBlock, String query, String groupKey,
                              int startIndex, int endIndex) {
    this.infoBlock = threadBlock;
    this.blockID = startIndex / DocCacheMgr.DOC_BLOCK_SIZE;
    this.blockStart = blockID * DocCacheMgr.DOC_BLOCK_SIZE;
    this.query = query;
    this.currentIndex = startIndex - 1;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.groupKey = groupKey;
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
    }
    else {
      element = getNextElement();
      if (element == null) {
        throw new java.util.NoSuchElementException();
      }
    }
    return element;
  }

  public Object previous() {
    Object element = null;
    if (previousElement != null) {
      element = previousElement;
      previousElement = null;
    }
    else {
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
    if (endIndex!=Integer.MAX_VALUE) {//如果为max_value还用下句则有可能会导致CPU 100%
      while (currentIndex + 1 < endIndex && element == null) {
        currentIndex++;
        element = getElement(currentIndex);
      }
    }
    else {//如果next没有，则不再继续找
      if (currentIndex + 1 < endIndex) {
        currentIndex++;
        element = getElement(currentIndex);
      }
    }
    return element;
  }

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
    if (index < 0) {
      return null;
    }
    // See if element isn't in the current info block
    // 因为endIndex可能会大于本block的最末尾的序号
    if (index < blockStart || index >= blockStart + DocCacheMgr.DOC_BLOCK_SIZE) {
      // Then load up the appropriate block
      Document doc = new Document();
      this.infoBlock = doc.getDocBlock(query, groupKey, index);
      this.blockID = index / DocCacheMgr.DOC_BLOCK_SIZE;
      this.blockStart = blockID * DocCacheMgr.DOC_BLOCK_SIZE;
    }
    Object element = null;
    // Compute the relative index of the element, which is the index in the
    // current thread block.
    int relativeIndex = index % DocCacheMgr.DOC_BLOCK_SIZE;
    // Make sure index isn't too large
    if (relativeIndex < infoBlock.length) {
        DocumentMgr docmgr = new DocumentMgr();
      element = docmgr.getDocument((int)infoBlock[relativeIndex]);
    }
    return element;
  }

  public void setIndex(Document doc) {
      // Set nextElement and previousElement to null since we may be moving
      // the index.
      nextElement = null;
      previousElement = null;

      // Scan through all blocks looking for thread.
      long threadID = doc.getID();
      long[] currentBlock;
      for (int i = startIndex; i < endIndex; i++) {
          currentBlock = doc.getDocBlock(query, doc.getDirCode(), i);
          if (currentBlock.length == 0) {
              //throw new ErrMsgException("Thread with id " +
              //    threadID + " is not a valid index in the iteration.");
          }
          int blockID = i / DocCacheMgr.DOC_BLOCK_SIZE;
          int blockEnd = blockID * DocCacheMgr.DOC_BLOCK_SIZE +
                         DocCacheMgr.DOC_BLOCK_SIZE;
          // If in first block of threads
          if (startIndex < blockEnd) {
              // If we are in the first block, j should start at the
              // start index instead of the beginning of the block.
              for (int j = startIndex % DocCacheMgr.DOC_BLOCK_SIZE;
                           j < currentBlock.length; j++, i++) {
                  if (currentBlock[j] == threadID) {
                      this.currentIndex = i;
                      return;
                  }
              }
          }
          // Otherwise, not in first block so start looking at beginning
          else {
              for (int j = 0; j < currentBlock.length; j++, i++) {
                  if (currentBlock[j] == threadID) {
                      this.currentIndex = i;
                      return;
                  }
              }
          }
      }

    }
}
