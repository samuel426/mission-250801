package org.example;

// 첫 글자 대문자. - 파스칼 표기법
// 의미가 달라지는 부분 시작 글자 대문자. - 카멜 표기법

public class WiseSaying {

    private int id;
    private String content;
    private String author;

    public WiseSaying(int id, String saying, String author) {
        this.id = id;
        this.content = content;
        this.author = author;
    }

    public int getId() {
        return id;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String saying) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
