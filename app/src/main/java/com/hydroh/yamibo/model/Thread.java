package com.hydroh.yamibo.model;

public class Thread {
    private String title;
    private String author;
    private int replyNum;
    private boolean containsImage;

    public Thread(String title, String author, int replyNum, boolean containsImage) {
        this.title = title;
        this.author = author;
        this.replyNum = replyNum;
        this.containsImage = containsImage;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getReplyNum() { return replyNum; }
    public boolean isContainsImage() { return containsImage; }
}
