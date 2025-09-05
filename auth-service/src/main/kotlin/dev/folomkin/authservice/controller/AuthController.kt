package dev.folomkin.authservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/auth"])
class AuthController {

    @GetMapping
    fun authTest(): String = "test"
}