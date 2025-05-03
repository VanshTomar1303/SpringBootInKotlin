package com.vansh.noteapp2.controller

import com.vansh.noteapp2.model.Note
import com.vansh.noteapp2.repo.NoteRepo
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
    private val repo: NoteRepo
) {

    data class NoteRequest(
        val id: String?,
        @field:NotBlank(message = "Title can't be blank.")
        val title: String,
        val description: String,
        val color: Long
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val description: String,
        val color: Long,
        val createdAt: Instant
    )

    @PostMapping("/saveNote")
    // this function can update and insert data
    fun saveNote(
        @Valid @RequestBody note: NoteRequest
    ): NoteResponse {
        val ownerId =  SecurityContextHolder.getContext().authentication.principal as String
        val savedNote = repo.save(
            Note(
                id = note.id?.let{ ObjectId(it) } ?: ObjectId.get(),
                title = note.title,
                content = note.description,
                color = note.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
            )
        )
        return savedNote.toResponse()
    }

    @GetMapping("/getNotes")
    fun getAllNotes(): List<NoteResponse> {
        val ownerId =  SecurityContextHolder.getContext().authentication.principal as String
        return repo.findNoteByOwnerId(ownerId = ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(
        @PathVariable id: String
    ){
        val note = repo.findById(ObjectId(id)).orElseThrow{
            IllegalArgumentException("Note not found.")
        }
        val ownerId =  SecurityContextHolder.getContext().authentication.principal as String
        if(note.ownerId.toHexString() == ownerId) repo.deleteById(ObjectId(id))
    }

    private fun Note.toResponse(): NoteController.NoteResponse {
        return NoteResponse(
            id = id.toHexString(),
            title = title,
            description = content,
            color = color,
            createdAt = createdAt
        )
    }
}