package com.perso.learnwords

class Update {
    var date : String = ""
    var nbWords : Int = 0
    var listWords : ArrayList<Word> = ArrayList<Word>()

    constructor(date: String, nbWords : Int, listWords: ArrayList<Word>) {
        this.date = date
        this.nbWords = nbWords
        this.listWords = listWords
    }
}