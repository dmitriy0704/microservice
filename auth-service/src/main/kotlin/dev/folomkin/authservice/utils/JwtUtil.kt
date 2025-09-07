package dev.folomkin.authservice.utils

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil {

//    private val secret: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512) // Генерация ключа
    private val secret: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512) // Генерация ключа
    private val expirationTime: Long = 86400000 // 1 день в миллисекундах


    fun generateToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(secret)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

}