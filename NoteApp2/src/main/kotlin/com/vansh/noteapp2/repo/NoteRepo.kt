package com.vansh.noteapp2.repo

import com.vansh.noteapp2.model.Note
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepo: MongoRepository<Note, ObjectId> {
    fun findNoteByOwnerId(ownerId: ObjectId): List<Note>
}