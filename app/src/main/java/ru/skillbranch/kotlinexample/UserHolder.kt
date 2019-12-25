@file:Suppress("Annotator")

package ru.skillbranch.kotlinexample

import ru.skillbranch.kotlinexample.extensions.isValidPhone
import ru.skillbranch.kotlinexample.extensions.trimPhone
//подчишенные баги

//Необходимо реализовать метод объекта (object UserHolder) для регистрации пользователя
object UserHolder {
    private val map = mutableMapOf<String, User >()
/*
* 1)Реализуй метод registerUser(fullName: String, email: String, password: String)
* возвращающий объект User,
* если пользователь с таким же логином уже есть в системе
* необходимо бросить исключение IllegalArgumentException("A user with this email already exists")*/
    fun registerUser(fullName: String, email: String, password: String): User {
        val user = User.makeUser(fullName, email = email, password = password)
        return if (!map.keys.contains(user.login)) {
            user.also { user -> map[user.login] = user }
        } else throw IllegalArgumentException("A user with this email already exists")
    }

    //Необходимо реализовать метод объекта (object UserHolder) для регистрации пользователя через телефон
    /*
    * 2)Реализуй метод registerUserByPhone(fullName: String, rawPhone: String)
    * возвращающий объект User (объект User должен содержать поле accessCode с 6 значным значением состоящим из случайных строчных и прописных букв латинского алфавита и цифр от 0 до 9),
    *  если пользователь с таким же телефоном уже есть в системе
    * необходимо бросить ошибку IllegalArgumentException("A user with this phone already exists")
    * валидным является любой номер телефона содержащий первым символом + и 11 цифр и не содержащий буквы,
    * иначе необходимо бросить исключение IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")*/
    fun registerUserByPhone(fullName: String, rawPhone: String): User {
        if (!rawPhone.trimPhone().isValidPhone()) throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")

        return if (!map.keys.contains(rawPhone.trimPhone())) {
            User.makeUser(fullName, phone = rawPhone).also { user -> map[user.login] = user }
        } else throw IllegalArgumentException("A user with this phone already exists")
    }

/*
* 3)Реализуй метод loginUser(login: String, password: String) : String
* возвращающий поле userInfo пользователя с соответствующим логином и паролем
* (логин для пользователя phone или email,
* пароль соответственно accessCode или password
* указанный при регистрации методом registerUser) или возвращающий null
* если пользователь с указанным логином и паролем не найден (или неверный пароль)*/
    fun loginUser(login:String, password: String): String? {
        return map[login.trim()]?.run{
            if (checkPassword(password)) this.userInfo
            else null
        }
    }


 /* Необходимо реализовать метод объекта (object UserHolder) для запроса нового кода
 * авторизации пользователя по номеру телефона +1
* Реализуй метод requestAccessCode(login: String) : Unit, после выполнения данного метода у пользователя с соответствующим логином должен быть сгенерирован новый код авторизации и помещен в свойство accessCode, соответственно должен измениться и хеш пароля пользователя (вызов метода loginUser должен отрабатывать корректно)
*/
    fun requestAccessCode(login: String) {
        if(map.contains(login.trimPhone())) {
            map[login.trimPhone()]?.apply {
                val code = generateAccessCode()
                passwordHash = encrypt (code)
                accessCode = code
            }!!.also { map[login]=it }

        }

    }
/* Необходимо реализовать метод объекта (object UserHolder) для импорта пользователей из списка строк +3
* Реализуй метод importUsers(list: List): List,
* в качестве аргумента принимает список строк
* где разделителем полей является ";" данные перечислены в следующем порядке -
*  Полное имя пользователя;
* email;
* соль:хеш пароля;
* телефон
* (Пример: " John Doe ;JohnDoe@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;")
* метод должен вернуть коллекцию список User
* (Пример возвращаемого userInfo:
* firstName: John
* lastName: Doe
* login: johndoe@unknow.com
* fullName: John Doe
* initials: J D
* email: JohnDoe@unknow.com
* phone: null
* meta: {src=csv}
* ), при этом meta должно содержать "src" : "csv", если сзначение в csv строке пустое то соответствующее свойство в объекте User должно быть null, обратите внимание что salt и hash пароля в csv разделены ":" , после импорта пользователей вызов метода loginUser должен отрабатывать корректно (достаточно по логину паролю)
*/
    fun importUsers(list: List<String>): List<User> {
        val users = mutableListOf<User>()
        list.forEach { string ->
            val userFields = string.split(";")
            val user = User.makeImportUser(fullName = userFields[0].trim(), email = userFields[1].ifEmpty { null }, passwordInfo = userFields[2].ifEmpty { null }, phone = userFields[3].ifEmpty { null })
            map[user.login] = user
            users.add(user)
        }
        return users
    }

}
