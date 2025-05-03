package com.vansh.simpleblogapi.controller

import com.vansh.simpleblogapi.model.Blog
import com.vansh.simpleblogapi.model.User
import com.vansh.simpleblogapi.repo.BlogRepo
import com.vansh.simpleblogapi.repo.UserRepo
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// User controller
// GET - http://localhost:8080/user/getAllLikedBlogs
// GET - http://localhost:8080/user/getAllSavedBlogs
// POST - http://localhost:8080/user/likeBlog/{blogId}
// POST - http://localhost:8080/user/savedBlog/{blogId}

@RestController
@RequestMapping("/user")
class UserController(
    private val blogRepo: BlogRepo,
    private val userRepo: UserRepo
) {

    data class BlogResponse(
        val blog: List<Blog>
    )

    @GetMapping("/getAllLikedBlogs")
    fun getAllLikedBlogs(): BlogResponse{
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val likedBlog = userRepo.findById(ObjectId(ownerId)).orElseThrow{
            IllegalArgumentException("User not found.")
        }.likedBlog
        val likedResponse = BlogResponse(
            blog = likedBlog
        )
        return likedResponse
    }

    @GetMapping("/getAllSavedBlogs")
    fun getAllSavedBlogs(): BlogResponse{
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val savedBlog = userRepo.findById(ObjectId(ownerId)).orElseThrow{
            IllegalArgumentException("User not found.")
        }.savedBlog
        val savedResponse = BlogResponse(
            blog = savedBlog
        )
        return savedResponse
    }


    @PostMapping("/likeBlog/{id}")
    fun likeABlog(
        @PathVariable id: String
    ): ResponseEntity<String> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val blog = blogRepo.findById(ObjectId(id)).get()
        val user = userRepo.findById(ObjectId(ownerId)).get()
        if (user.id.toHexString() != ownerId) throw IllegalArgumentException("User not found.")

        userRepo.save(
            User(
                likedBlog = listOf(blog),
                id = user.id,
                email = user.email,
                hashedPassword = user.hashedPassword,
                savedBlog = user.savedBlog
            )
        )
        return ResponseEntity.status(200)
            .body("You liked this blog")
    }

    @PostMapping("/savedBlog/{id}")
    fun savedABlog(
        @PathVariable id: String
    ): ResponseEntity<String> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val blog = blogRepo.findById(ObjectId(id)).get()
        val user = userRepo.findById(ObjectId(ownerId)).get()
        if (user.id.toHexString() != ownerId) throw IllegalArgumentException("User not found.")

        userRepo.save(
            User(
                likedBlog = user.likedBlog,
                id = user.id,
                email = user.email,
                hashedPassword = user.hashedPassword,
                savedBlog = listOf(blog)
            )
        )
        return ResponseEntity.status(200)
            .body("You Saved this blog")
    }
}