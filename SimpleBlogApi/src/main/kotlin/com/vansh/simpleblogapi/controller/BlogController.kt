package com.vansh.simpleblogapi.controller

import com.vansh.simpleblogapi.model.Blog
import com.vansh.simpleblogapi.model.Comment
import com.vansh.simpleblogapi.repo.BlogRepo
import com.vansh.simpleblogapi.repo.CommentRepo
import com.vansh.simpleblogapi.repo.UserRepo
import jakarta.validation.Valid
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

// Blog controller
// GET - http://localhost:8080/blog/getAllBlogs
// GET - http://localhost:8080/blog/{id}
// POST - http://localhost:8080/blog/saveBlog
// DELETE - http://localhost:8080/blog/{id}

@RestController
@RequestMapping("/blog")
class BlogController(
    private val blogRepo: BlogRepo,
    private val commentRepo: CommentRepo,
    private val userRepo: UserRepo
) {

    data class BlogResponse(
        val id: String,
        val title: String,
        val auther: String,
        val content: String,
        val createdAt: Instant,
        val comments: List<Comment>
    )

    data class BlogRequest(
        @field:NotNull("Title can't be null.")
        val title: String,
        @field:NotNull("Content can't be null.")
        val content: String,
        val id: String?
    )

    // get all blogs
    @GetMapping("/getAllBlogs")
    fun getAllBlogs(): List<BlogResponse>{
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val blogs = blogRepo.findAll()
        if(blogs[0].ownerId.toHexString() != ownerId) throw IllegalArgumentException("Bad Credentials.")
        if(blogs.isEmpty()) throw IllegalArgumentException("There are no blogs.")
        return blogs.map {
            it.toResponse()
        }
    }

    // get blogs by user id
    @GetMapping(path = ["/{id}"])
    fun getAllBlogsByOwnerId(
        @PathVariable id: String
    ): List<BlogResponse>{
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if(ownerId != id) throw IllegalArgumentException("Wrong Credential.")
        val blogs = blogRepo.findBlogByOwnerId(ObjectId(id)).map {
            it.toResponse()
        }
        if(blogs.isEmpty()) throw IllegalArgumentException("There are no blogs.")
        return blogs
    }

    // upsert the blog
    @PostMapping("/saveBlog")
    fun saveNote(
        @Valid @RequestBody blogRequest: BlogRequest
    ): BlogResponse{
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val auther = userRepo.findById(ObjectId(ownerId)).orElseThrow {
            IllegalArgumentException("Author not found.")
        }.email
        val savedBlog = blogRepo.save(
            Blog(
                id = blogRequest.id?.let{ ObjectId(it) } ?: ObjectId.get(),
                title = blogRequest.title,
                content = blogRequest.content,
                auther = auther,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId),
                comments = emptyList()
            )
        )

        return savedBlog.toResponse()
    }

    // delete a blog
    @DeleteMapping(path = ["/{id}"])
    fun deleteBlogByOwnerId(
        @PathVariable id: String
    ){
        val blog = blogRepo.findById(ObjectId(id)).orElseThrow {
            IllegalArgumentException("Blog not found.")
        }
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if(blog.ownerId.toHexString() == ownerId){
            blogRepo.deleteById(blog.id)
            commentRepo.deleteCommentByBlogId(blog.id)
        }
    }

    private fun Blog.toResponse(): BlogResponse {
        return BlogResponse(
            title = title,
            id = id.toHexString(),
            content = content,
            createdAt = createdAt,
            comments = comments,
            auther = auther
        )
    }
}