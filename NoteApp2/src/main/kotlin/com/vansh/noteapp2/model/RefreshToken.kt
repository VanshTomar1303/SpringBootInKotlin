package com.vansh.noteapp2.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("refresh_token")
data class RefreshToken(
    val userID: ObjectId,
    @Indexed(expireAfter = "0s") val expiresAt: Instant,
    val hashedToken: String,
    val createAt: Instant = Instant.now()
)
