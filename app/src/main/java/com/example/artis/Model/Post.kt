package com.example.artis.Model

class Post {

    private var postId: String = ""
    private var postimage: String = ""
    private var publisher: String = ""
    private var description: String = ""

    constructor()

    constructor(postId: String, postimage: String, publisher: String, description: String){
        this.postId = postId
        this.postimage = postimage
        this.publisher = publisher
        this.description = description
    }

    fun getPostid(): String {
        return postId
    }

    fun getPostimage(): String {
        return postimage
    }

    fun getPublisher(): String {
        return publisher
    }

    fun getDescription(): String {
        return description
    }

    fun setPostid(postid: String) {
        this.postId = postid
    }

    fun setPostimage(postimage: String) {
        this.postimage = postimage
    }

    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }

    fun setDescription(description: String) {
        this.description = description
    }
}