package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
//подчишенные баги
/*
* class User должен иметь приватный primary constructor со следующими аргументами:
val firstName: String,
val lastName: String?,
email: String? = null,
rawPhone: String? = null,
meta: Map<String, Any>? = null
class User должен минимально иметь следующие свойства:
val firstName: String,
val lastName: String?,
val userInfo: String,
val fullName: String,
val initials: String,
var phone: String?
var login: String,
val salt: String,
var passwordHash: String,
var accessCode: String? //@VisibleForTesting(otherwise = VisibleForTesting.NONE)
Cвойство userInfo должно быть представлено следующим содержимым:
userInfo = """
      firstName: $firstName
      lastName: $lastName
      login: $login
      fullName: $fullName
      initials: $initials
      email: $email
      phone: $phone
      meta: $meta
    """.trimIndent()

* */
class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString ( " " )
            .capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map{it.first().toUpperCase() }
            .joinToString ( " " )
    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(),"")
        }
    private var _login: String? = null
    internal var login: String
        set(value){
            _login = value?.toLowerCase()
        }
        get() = _login!!
    private var _salt: String? = null
    private val salt: String by lazy {
        _salt?: ByteArray(16).also{SecureRandom().nextBytes(it)}.toString()
    }
    private lateinit var passwordHash: String
    @VisibleForTesting(otherwise = VisibleForTesting.NONE) // Поле объявлено только для теста
    var accessCode: String? = null

    init{
        println("First init block, primary constructor was called")

        check(!firstName.isBlank()) {"First name must not be blank"}
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()) {"Email or phone must not be blank"}

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    //for email
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ): this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("secondary mail constructor")
        passwordHash = encrypt(password)
    }

    // for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ): this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("secondary phone constructor")
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)

    }

    //for csv
    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        pwdHash: String,
        pwdSalt: String,
        phone: String?
    ): this(firstName, lastName, email = email, meta = mapOf("src" to "csv"), rawPhone = phone) {
        println("csv constructor")
        passwordHash = pwdHash
        _salt = pwdSalt
    }

    internal fun checkPassword(pass: String) = encrypt(pass) == passwordHash
    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalArgumentException("The entered password does not match the current password")
    }

    private fun encrypt(password: String): String  = salt.plus(password).md5()
    private fun String.md5() : String {
        val md= MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())// 16 byte
        val hexString = BigInteger(1, digest).toString(16) // возвращает 32 символа (16 байт в hex)
        return hexString.padStart(32, '0')
    }

    fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
        return StringBuilder().apply{
            repeat(6){
                (possible.indices).random().also{index ->
                    append(possible[index])
                }
            }
        }.toString()
    }
    private fun sendAccessCodeToUser(phone: String?, code: String) {
        println("..... sending access code: $code on $phone")
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            salt: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()

            return when {
                !salt.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firstName,
                    lastName,
                    email,
                    password,
                    salt,
                    phone
                )
                !phone.isNullOrBlank() -> User(firstName, lastName, rawPhone = phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firstName,
                    lastName,
                    email,
                    password
                )
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")

            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter {it.isNotBlank()}
                .run {
                    when (size) {
                        1 -> first() to null// only one word
                        2 -> first() to last() // two words
                        else -> throw IllegalArgumentException("Full name must contain only first name and last name, current split result ${this@fullNameToPair}")
                    }
                }
        }

        /*fun makeImportUser(
            fullName: String,
            email: String,
            access: String,
            phone: String,
            passwordInfo: String?
        ): User = User("stub", "stub", "stub")*/
    }


}
