package ru.skillbranch.kotlinexample

class User (
    private val firstName: String,
    private val lastName:String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map <String, Any>? = null
){
    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName,lastName)
            .joinToString(separator = " ")
            .capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName)
    private val phone: String?
    private val login: String
    private val passwordHasg: String
}