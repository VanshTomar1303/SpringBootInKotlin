package com.vansh.simpleblogapi.repo

import com.vansh.simpleblogapi.model.RefreshToken
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface RefreshTokenRepo: MongoRepository<RefreshToken, ObjectId> {
    fun findByUserIDAndHashedToken(userId: ObjectId, hashedToken: String): RefreshToken?
    fun deleteByUserIDAndHashedToken(userId: ObjectId, hashedToken: String)
}