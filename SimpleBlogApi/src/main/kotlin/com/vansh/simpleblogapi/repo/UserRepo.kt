package com.vansh.simpleblogapi.repo

import com.vansh.simpleblogapi.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepo: MongoRepository<User,ObjectId> {
    fun findUserByEmail(email: String): User?
}