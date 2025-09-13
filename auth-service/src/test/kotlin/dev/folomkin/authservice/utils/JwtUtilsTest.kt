package dev.folomkin.authservice.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class JwtUtilsTest {

    private val secret =
        "5JzoMbk6E5qIqHSuBTgeQCARtUsxAkBiHwdjXOSW8kWdXzYmP3X51C0"
    private val expirationTime = 86400000L
//    private val jwtUtil = JwtUtils(secret, expirationTime)
    private val jwtUtil = JwtUtils()


    @Test
    fun testCreateJwt() {
        val token = jwtUtil.createJwt("Authentication")
        assertNotNull(token)
        val claims: Jws<Claims?>? = jwtUtil.parseJwt(token)
        assertNotNull(claims)

        assertAll(
            { assertEquals("HS256", claims!!.header.algorithm) },
        )

    }



//    import org.junit.jupiter.api.Assertions.*
//    import org.junit.jupiter.api.BeforeEach
//    import org.junit.jupiter.api.Test
//
//    class JwtUtilsTest {
//
//        private val secret =
//            "5JzoMbk6E5qIqHSuBTgeQCARtUsxAkBiHwdjXOSW8kWdXzYmP3X51C0"
//        private val expirationTime = 1000L * 60 * 60 // 1 час
//        private lateinit var jwtUtil: JwtUtils
//
//        @BeforeEach
//        fun setup() {
//            jwtUtil = JwtUtils(secret, expirationTime)
//        }
//
//        @Test
//        fun `should create valid JWT token`() {
//            val token = jwtUtil.createJwt("testUser")
//            assertNotNull(token)
//            assertTrue(token.isNotBlank())
//        }
//
//        @Test
//        fun `should extract username from token`() {
//            val token = jwtUtil.createJwt("testUser")
//            val username = jwtUtil.extractUsername(token)
//            assertEquals("testUser", username)
//        }
//
//        @Test
//        fun `should validate a valid token`() {
//            val token = jwtUtil.createJwt("testUser")
//            assertTrue(jwtUtil.validateJwt(token))
//        }
//
//        @Test
//        fun `should reject an invalid token`() {
//            val invalidToken = "abc.def.ghi"
//            assertFalse(jwtUtil.validateJwt(invalidToken))
//        }
//
//        @Test
//        fun `should reject an expired token`() {
//            val shortLivedJwtUtil = JwtUtils(secret, 1) // 1 ms
//            val token = shortLivedJwtUtil.createJwt("testUser")
//
//            Thread.sleep(5) // ждём, пока истечёт срок жизни
//
//            assertFalse(shortLivedJwtUtil.validateJwt(token))
//        }
//    }


    /////////////////////////////////


//    import org.junit.jupiter.api.Assertions.*
//    import org.junit.jupiter.api.BeforeEach
//    import org.junit.jupiter.api.Test
//    import org.junit.jupiter.api.assertAll
//
//    class JwtUtilsTest {
//
//        private val secret =
//            "5JzoMbk6E5qIqHSuBTgeQCARtUsxAkBiHwdjXOSW8kWdXzYmP3X51C0"
//        private val expirationTime = 1000L * 60 * 60 // 1 час
//        private lateinit var jwtUtil: JwtUtils
//
//        @BeforeEach
//        fun setup() {
//            jwtUtil = JwtUtils(secret, expirationTime)
//        }
//
//        @Test
//        fun `should create valid JWT token and extract username`() {
//            val token = jwtUtil.createJwt("testUser")
//
//            assertAll(
//                "check token properties",
//                { assertNotNull(token, "Token must not be null") },
//                { assertTrue(token.isNotBlank(), "Token must not be blank") },
//                { assertEquals("testUser", jwtUtil.extractUsername(token)) },
//                { assertTrue(jwtUtil.validateJwt(token), "Token must be valid") }
//            )
//        }
//
//        @Test
//        fun `should reject invalid token`() {
//            val invalidToken = "abc.def.ghi"
//
//            assertAll(
//                "invalid token checks",
//                { assertFalse(jwtUtil.validateJwt(invalidToken)) },
//                { assertThrows<Exception> { jwtUtil.extractUsername(invalidToken) } }
//            )
//        }
//
//        @Test
//        fun `should reject expired token`() {
//            val shortLivedJwtUtil = JwtUtils(secret, 1) // 1 ms
//            val token = shortLivedJwtUtil.createJwt("testUser")
//            Thread.sleep(5) // ждём истечения срока
//
//            assertAll(
//                "expired token checks",
//                { assertFalse(shortLivedJwtUtil.validateJwt(token)) },
//                { assertThrows<Exception> { shortLivedJwtUtil.extractUsername(token) } }
//            )
//        }
//    }

}