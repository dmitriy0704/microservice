package dev.folomkin.userservice.controller

import dev.folomkin.shared.dto.UserDto
import dev.folomkin.userservice.service.UserProducer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userProducer: UserProducer
) {
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserDto {
        //-> Мок-данные для примера; замените на реальную логику (например, получение из базы данных)
        return UserDto(id = id, name = "User-$id", email = "user-email-$id")
    }

    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return listOf(
            UserDto(id = 1L, name = "Alice", email = "alice@gmail.com"),
            UserDto(id = 2L, name = "Bob", email = "bob@gmail.com")
        )
    }

    @PostMapping("/send")
    fun sendMessage(@RequestBody user: UserDto): ResponseEntity<String> {
        userProducer.sendUser(user)
        return ResponseEntity
            .accepted()
            .body("Сообщение принято в очередь на отправку")    }
}