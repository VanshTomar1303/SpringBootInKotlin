package com.vansh.simpleblogapi.controller

import com.vansh.simpleblogapi.model.Blog
import com.vansh.simpleblogapi.model.Comment
import com.vansh.simpleblogapi.repo.BlogRepo
import com.vansh.simpleblogapi.repo.CommentRepo
import com.vansh.simpleblogapi.repo.UserRepo
import org.bson.types.ObjectId
import org.jetbrains.annotations.NotNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

// Comment controller
// GET - http://localhost:8080/comments/{blogId}
// POST - http://localhost:8080/comments/postComments/{blogID}
// GET - http://localhost:8080/comments/getByOwnerId/{ownerId}
// DELETE - http://localhost:8080/comments/{ownerId}

@RestController
@RequestMapping("/comments")
class CommentController(
    private val commentRepo: CommentRepo,
    private val userRepo: UserRepo,
    private val blogRepo: BlogRepo
) {
    data class CommentResponse(
        val auther: String,
        val blogId: ObjectId,
        val comment: String,
        val createdAt: Instant,
    )
    data class CommentRequest(
        @field:NotNull("Comment can't be empty.")
        val comment: String
    )

    @GetMapping(path = ["/{id}"])
    fun getCommentByPostId(
       @PathVariable id: String
    ): List<CommentResponse>{
        if(id.isBlank()) throw IllegalArgumentException("Id can't be blank")
        val comments = commentRepo.findCommentByBlogId(ObjectId(id)).map{
            it.toResponse()
        }
        return comments
    }

    @PostMapping("/postComment/{blogId}")
    fun saveComment(
        @RequestBody body: CommentRequest,
        @PathVariable blogId: String
    ): CommentResponse{
        if(body.comment.isBlank()) throw IllegalArgumentException("Comment can't be blank")
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val ownerUserName = userRepo.findById(ObjectId(ownerId)).orElseThrow {
            IllegalArgumentException("Author not found.")
        }.email
        val savedComment = commentRepo.save(
            Comment(
                ownerId = ObjectId(ownerId),
                blogId = ObjectId(blogId),
                auther = ownerUserName,
                comment = body.comment,
                createdAt = Instant.now()
            )
        )
        val blog = blogRepo.findById(ObjectId(blogId)).get()
        blogRepo.save(
            Blog(
                title = blog.title,
                comments = commentRepo.findCommentByBlogId(ObjectId(blogId)),
                id = blog.id,
                auther = blog.auther,
                content = blog.content,
                createdAt = blog.createdAt,
                ownerId = blog.ownerId
            )
        )
        return savedComment.toResponse()
    }

    @GetMapping(path = ["getByOwnerId/{id}"])
    fun getCommentByOwnerId(
        @PathVariable id: String
    ): List<CommentResponse>{
        val comments = commentRepo.findCommentByOwnerId(ObjectId(id)).map {
            it.toResponse()
        }
        return comments
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteCommentByOwnerId(
        @PathVariable id: String
    ){
        val owner = userRepo.findById(ObjectId(id)).orElseThrow {
            IllegalArgumentException("User not found.")
        }
        commentRepo.deleteCommentByOwnerId(owner.id)
    }

    private fun Comment.toResponse(): CommentResponse{
        return CommentResponse(
            auther = auther,
            blogId = blogId,
            createdAt = createdAt,
            comment = comment
        )
    }
}