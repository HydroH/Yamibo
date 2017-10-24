package com.hydroh.yamibo.model;


public class Section {
    private int sectionType;
    private String title, url;
    private String description, newPosts;
    private String category, author, postDate;
    private int replies, views;

    public final static int TYPE_SECTOR = 0;
    public final static int TYPE_THREAD = 1;

    public Section(String title, String url, int sectionType) {
        this.title = title;
        this.url = url;
        this.sectionType = sectionType;
    }

    public Section(String title, String url, String description, String newPosts) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.newPosts = newPosts;
        this.sectionType = TYPE_SECTOR;
    }

    public Section(String title, String url, String category, String author, String postDate, int replies, int views) {
        this.title = title;
        this.url = url;
        this.category = category;
        this.author = author;
        this.postDate = postDate;
        this.replies = replies;
        this.views = views;
        this.sectionType = TYPE_THREAD;
    }

    public int getSectionType() {
        return sectionType;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getNewPosts() {
        return newPosts;
    }

    public String getCategory() {
        return category;
    }

    public String getAuthor() {
        return author;
    }

    public String getPostDate() {
        return postDate;
    }

    public int getReplies() {
        return replies;
    }

    public int getViews() {
        return views;
    }

}
