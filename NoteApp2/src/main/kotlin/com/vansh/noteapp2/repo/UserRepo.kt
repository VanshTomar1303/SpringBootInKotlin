package com.vansh.noteapp2.repo

import com.vansh.noteapp2.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepo: MongoRepository<User,ObjectId> {
    fun findUserByEmail(email: String): User?
}