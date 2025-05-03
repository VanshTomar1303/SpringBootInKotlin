package com.vansh.simpleblogapi.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("comment")
data class Comment(
    val createdAt: Instant,
    val ownerId: ObjectId,
    val blogId: ObjectId,
    val comment: String,
    val auther: String
)
