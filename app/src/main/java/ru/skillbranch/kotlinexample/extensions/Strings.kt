package ru.skillbranch.kotlinexample.extensions
//подчишенные баги
fun String.isValidPhone() = this.trimPhone().matches(Regex("^\\+\\d{11}"))

fun String.trimPhone() = this.replace("[^+\\d]".toRegex(), replacement = "")