package com.example.artis.Model

class User {
    private var username: String = ""
    private var fullname: String = ""
    private var work: String = ""
    private var image: String = ""
    private var uid: String = ""

    constructor()

    constructor(username: String, fullname: String, bio: String, image: String, uid: String) {
        this.username = username
        this.fullname = fullname
        this.work = work
        this.image = image
        this.uid = uid
    }

    fun getUsername(): String {
        return username
    }
    fun setUsername(username: String){
        this.username = username
    }

    fun getFullName(): String {
        return fullname
    }
    fun setFullName(fullname: String){
        this.fullname = fullname
    }

    fun getWork(): String {
        return work
    }
    fun setWork(work: String){
        this.work = work
    }

    fun getImage(): String {
        return image
    }
    fun setImage(image: String){
        this.image = image
    }

    fun getUID(): String {
        return uid
    }
    fun setUID(uid: String){
        this.uid = uid
    }

}