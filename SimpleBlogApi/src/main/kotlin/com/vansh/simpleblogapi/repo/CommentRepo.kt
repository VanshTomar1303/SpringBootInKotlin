package com.vansh.simpleblogapi.repo

import com.vansh.simpleblogapi.model.Comment
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface CommentRepo: MongoRepository<Comment,ObjectId> {
    fun findCommentByBlogId(blogId: ObjectId): List<Comment>
    fun deleteCommentByBlogId(blogId: ObjectId)
    fun deleteCommentByOwnerId(blogId: ObjectId)
    fun findCommentByOwnerId(ownerId: ObjectId): List<Comment>
}