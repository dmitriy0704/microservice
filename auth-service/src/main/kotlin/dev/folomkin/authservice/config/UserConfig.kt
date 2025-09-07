package dev.folomkin.authservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
class UserConfig {


    @Bean
    fun userDetailsService(): UserDetailsService {

        val user = User.withUsername("user")
            .password("{noop}password") // {noop} = без шифрования
            .roles("USER")
            .build()

        return InMemoryUserDetailsManager(user)
    }
}