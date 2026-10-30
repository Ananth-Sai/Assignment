package com.taskflow.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Name cannot be blank")
    @field:Size(max = 100, message = "Name cannot exceed 100 characters")
    val name: String,

    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email format is invalid")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 6, max = 100, message = "Password must be at least 6 characters long")
    val password: String
)

data class LoginRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email format is invalid")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    val password: String
)

data class UserSummaryDto(
    val id: Long,
    val name: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val user: UserSummaryDto
)

