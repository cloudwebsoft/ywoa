package com.cloudwebsoft.framework.util;

/**
 *
 * <p>Title: 队列</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Queue extends java.util.Vector {
    public Queue() {
        super();
    }

    public synchronized void enq(Object x) {
        super.addElement(x);
    }

    public synchronized Object deq() {
        /* 队列若为空，引发EmptyQueueException异常 */
        if (this.empty())
            throw new RuntimeException("Queue is Empty.");
        Object x = super.elementAt(0);
        super.removeElementAt(0);
        return x;
    }

    public synchronized Object front() {
        if (this.empty())
            throw new RuntimeException("Queue is Empty.");
        return super.elementAt(0);
    }

    public boolean empty() {
        return super.isEmpty();
    }

    public synchronized void clear() {
        super.removeAllElements();
    }

    public int search(Object x) {
        return super.indexOf(x);
    }
}

