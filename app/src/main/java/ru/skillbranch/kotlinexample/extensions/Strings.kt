package ru.skillbranch.kotlinexample.extensions

fun String.isValidPhone() = this.trimPhone().matches(Regex("^\\+\\d{11}"))

fun String.trimPhone() = this.replace("[^+\\d]".toRegex(), replacement = "")