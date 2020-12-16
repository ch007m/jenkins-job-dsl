import utilities.Hello

String userName = ${user} ?: "Charles"
println "UserName property: ${userName}"
Hello.message(userName);