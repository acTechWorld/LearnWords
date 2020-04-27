package com.perso.learnwords

class Word {
    var french_word : String = ""
    var english_word : String = ""
    var index : String?
    var checked : Boolean = false
    var view : Boolean = true

    constructor(french_word: String, english_word: String, index : String?, checked : Boolean) {
        this.french_word = french_word
        this.english_word = english_word
        this.index = index
        this.checked = checked
    }





}