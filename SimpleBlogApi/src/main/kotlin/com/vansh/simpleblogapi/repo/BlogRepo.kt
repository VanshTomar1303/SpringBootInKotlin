package com.vansh.simpleblogapi.repo

import com.vansh.simpleblogapi.model.Blog
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface BlogRepo: MongoRepository<Blog,ObjectId> {
    fun findBlogByOwnerId(ownerId: ObjectId): List<Blog>
    fun findBlogByAuther(auther: String): List<Blog>
}