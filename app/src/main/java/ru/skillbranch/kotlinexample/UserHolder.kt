package ru.skillbranch.kotlinexample

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return map[email.toLowerCase().trim()]?.let {
            throw IllegalArgumentException("A user with this email already exists")
        } ?: User.makeUser(
            fullName,
            email = email,
            password = password
        ).also { user -> map[user.login] = user }
    }

    fun loginUser(login: String, password: String): String? {
        return map[login.trim()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }


    fun registerUserByPhone(fullName: String, rawPhone: String): User {
        if (!rawPhone.trimPhone().isValidPhone()) throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")

        return if (!map.keys.contains(rawPhone.trimPhone())) {
            User.makeUser(fullName, phone = rawPhone).also { user -> map[user.login] = user }
        } else throw IllegalArgumentException("A user with this phone already exists")
    }

    fun requestAccessCode(login: String) {
        var currentLogin = login.trim()
        if (login.isValidPhone()) {
            currentLogin = currentLogin.trimPhone()
        }
        map[currentLogin]?.run {
            this.accessCode = this.changePassword()
        }
    }

    fun importUsers(list: List<String>): List<User> =
        list.map {
            val (fullName, email, access, phone) = it.split(";")
            val cuser = User.makeImportUser(fullName, email, access, phone)
                .also { user -> map[user.login] = user }
            cuser
        }.filterNotNull()

}