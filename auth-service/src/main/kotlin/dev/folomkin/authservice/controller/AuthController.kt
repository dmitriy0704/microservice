package dev.folomkin.authservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/auth"])
class AuthController {

    @GetMapping("/public/hello")
    fun publicAuthTest(): String = "Привет, это публичный эндпоинт"

    @GetMapping("/secure/hello")
    fun privateAuthTest(): String = "Привет, это приватный эндпоинт"

}