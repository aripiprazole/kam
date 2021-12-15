# Kam

An [arrow-kt](https://arrow-kt.io/) binding for [ktor server](https://ktor.io/).

Example:
```kt

data class User(val name: String)

fun findUser(id: Int): Either<String, User> =
  if (id == 1) User("Alice").right()
  else "User not found".left()

fun Application.module() {
  routingE {
    get("/user") {
      call.respond(findUser(1).bind().toString())
    }
  }
}

```
