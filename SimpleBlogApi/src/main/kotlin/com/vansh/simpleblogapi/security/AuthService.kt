package com.vansh.simpleblogapi.security

import com.vansh.simpleblogapi.model.RefreshToken
import com.vansh.simpleblogapi.model.User
import com.vansh.simpleblogapi.repo.RefreshTokenRepo
import com.vansh.simpleblogapi.repo.UserRepo
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val jwtService: JwtService,
    private val repo: UserRepo,
    private val hashedEncoder: HashedEncoder,
    private val refreshTokenRepo: RefreshTokenRepo
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email: String,password: String): User {
        val user = repo.findUserByEmail(email.trim())
        if(user != null) throw ResponseStatusException(HttpStatus.CONFLICT,"User already exist.")
        return repo.save(
            User(
                email = email,
                hashedPassword = hashedEncoder.encode(password),
                likedBlog = emptyList(),
                savedBlog = emptyList()
            )
        )
    }

    fun login(email: String,password: String): TokenPair{
        val user = repo.findUserByEmail(email)
            ?: throw BadCredentialsException("Invalid Credentials.")

        if(!hashedEncoder.matches(password,user.hashedPassword)){
            throw BadCredentialsException("Invalid Credentials.")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())

        storeRefreshToken(user.id,newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair{
        if(!jwtService.validateRefreshToken(refreshToken)){
            throw ResponseStatusException(HttpStatusCode.valueOf(401),"Invalid refresh Token.")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = repo.findById(ObjectId(userId)).orElseThrow {
            ResponseStatusException(HttpStatusCode.valueOf(401),"Invalid refresh Token.")
        }

        val hashed = hashToken((refreshToken))
        refreshTokenRepo.findByUserIDAndHashedToken(user.id,hashed)
            ?: throw ResponseStatusException(HttpStatusCode.valueOf(401),"Refresh Token not recognized.")

        refreshTokenRepo.deleteByUserIDAndHashedToken(user.id,hashed)

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(user.id,newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

    }

    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String){
        val hashed = hashToken(rawRefreshToken)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepo.save(
            RefreshToken(
                userID = userId,
                hashedToken = hashed,
                expiresAt = expiresAt,
            )
        )

    }
    private fun hashToken(token: String): String{
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())

        return Base64.getEncoder().encodeToString(hashBytes)
    }
}