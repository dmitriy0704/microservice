package dev.folomkin.shared.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class UserDto(
    @JsonProperty("id") val id: Long,
    @JsonProperty("name") val name: String,
    @JsonProperty("email") val email: String
) : Serializable