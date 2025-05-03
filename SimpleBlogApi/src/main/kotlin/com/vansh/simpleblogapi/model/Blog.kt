package com.vansh.simpleblogapi.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("blog")
data class Blog(
    @Id
    val id: ObjectId,
    val auther: String,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val ownerId: ObjectId,
    val comments: List<Comment>
)
