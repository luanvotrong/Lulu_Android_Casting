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
        return list.pollFirst();
    }

    public T peek() {
        return list.peekFirst();
    }

    public int size() {
        return list.size();
    }
}
