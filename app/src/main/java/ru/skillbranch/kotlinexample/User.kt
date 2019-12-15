package ru.skillbranch.kotlinexample

import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.lang.StringBuilder
import java.lang.IllegalArgumentException

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
            .joinToString (" ")
            .capitalize()

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first ().toUpperCase() }
            .joinToString(" ")

    private val phone: String?=null
        set (value) {
            field = value?.replace("[^+\\d]".toRegex(),"")
        }

    private val _login: String? = null
    private var login : String
        set(value) {
            _login = value?.toLowerCase()
        }
        get() = _login!!

    private val salt: String by lazy {
        ByteArray(16).also {SecureRandom().nextBytes(it)}
    }
    private lateinit var passwordHasg: String
//for email
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("Secondary email constructor")
    }

//for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor")
        val code: String = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    init {
        println("!First init block, primary constructor was called")
        
        check(!firstName.isBlank()){ "FirstName must be not blank" }
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank() ){ "Email or phone must be not blank" }
        
        phone=rawPhone
        login=email ?; phone!!
                
        userInfo = """
            firstName: $firstName
            lastName: $lastName
            Login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    private lateinit var salt: String

    fun defineSalt() =  ByteArray(16).also {
        SecureRandom().nextBytes(it)
    }.toString()
    

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash


    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalArgumentException("The entered password does not much the current password")
    }
    private fun encrypt(password: String) = salt.plus(password).md5()
    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }
    private fun sendAccessCodeToUser(phone: String, code: String) {
        println("..... sending access code: $code on $phone")
    }
    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray()) //16 byte
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    } 

   

//fun checkPassword(pass: String) = encrypt(pass) == passwordHash

//fun checkPhone(phone: String): Boolean = this.phone == phone


    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName:String, lastName:String?) = fullName.fullNameToPair()
        return when{
            !phone.isNullOrBlank() -> User(firstName, lastName)
            !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName)
        }
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {this: List<String>
                    when (size) {
                        1 -> first() to null ^run
                        2 -> first() to last() ^run
                        else -> throw IllegalArgumentException("Fullname must contain only first name" +
                        "and last name^ current split result ${this@fullNameToPair}")
                }
            }
    }
}
