package dev.folomkin.authservice.controller

import dev.folomkin.authservice.utils.JwtUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/auth")
class AuthController {

    @GetMapping
    fun getToken(): String {
        return JwtUtils().createJwt("Authentication")
    }
}