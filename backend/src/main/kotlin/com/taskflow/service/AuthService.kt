package com.taskflow.service

import com.taskflow.dto.AuthResponse
import com.taskflow.dto.LoginRequest
import com.taskflow.dto.RegisterRequest
import com.taskflow.dto.UserSummaryDto
import com.taskflow.exception.ConflictException
import com.taskflow.model.User
import com.taskflow.repository.UserRepository
import com.taskflow.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val normalizedEmail = request.email.lowercase().trim()

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw ConflictException("A user with email '${request.email}' already exists")
        }

        val newUser = userRepository.save(
            User(
                name = request.name.trim(),
                email = normalizedEmail,
                password = passwordEncoder.encode(request.password)
            )
        )

        val token = jwtTokenProvider.generateToken(
            userId = newUser.id!!,
            email = newUser.email,
            name = newUser.name
        )

        return AuthResponse(
            token = token,
            user = UserSummaryDto(
                id = newUser.id!!,
                name = newUser.name,
                email = newUser.email
            )
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val normalizedEmail = request.email.lowercase().trim()

        // Authenticate credentials via Spring Security AuthenticationManager
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(normalizedEmail, request.password)
        )

        val user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { IllegalStateException("Authenticated user not found in database") }

        val token = jwtTokenProvider.generateToken(
            userId = user.id!!,
            email = user.email,
            name = user.name
        )

        return AuthResponse(
            token = token,
            user = UserSummaryDto(
                id = user.id!!,
                name = user.name,
                email = user.email
            )
        )
    }
}

