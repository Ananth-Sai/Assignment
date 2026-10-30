package com.taskflow.security

import com.taskflow.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email.lowercase().trim())
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }

        return UserPrincipal(
            id = user.id!!,
            name = user.name,
            email = user.email,
            passwordHash = user.password
        )
    }
}

