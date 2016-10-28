package com.luanvotrong.Utiliies;

import java.util.LinkedList;

public class FrameQueue<T> {
    protected LinkedList<T> list;

    public FrameQueue() {
        list = new LinkedList<T>();
    }

    public void add(T el) {
        list.add(el);
    }

    public T poll() {
        try {
            return list.pollFirst();
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public T peek() {
        try {
            return list.peekFirst();
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public int size() {
        return list.size();
    }
}
