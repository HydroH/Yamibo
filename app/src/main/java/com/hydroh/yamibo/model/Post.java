package com.hydroh.yamibo.model;


public class Post {
    private String author, avatar, contentHTML;
    private String postDate, editDate;

    public Post(String author, String avatar, String contentHTML, String postDate) {
        this.author = author;
        this.avatar = avatar;
        this.contentHTML = contentHTML;
        this.postDate = postDate;
        this.editDate = postDate;
    }

    public Post(String author, String avatar, String contentHTML, String postDate, String editDate) {
        this.author = author;
        this.avatar = avatar;
        this.contentHTML = contentHTML;
        this.postDate = postDate;
        this.editDate = editDate;
    }

    public String getAuthor() {
        return author;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getContentHTML() {
        return contentHTML;
    }

    public String getPostDate() {
        return postDate;
    }

    public String getEditDate() {
        return editDate;
    }
}
