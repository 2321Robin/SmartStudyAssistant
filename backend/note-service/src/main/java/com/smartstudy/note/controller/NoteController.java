package com.smartstudy.note.controller;

import com.smartstudy.common.api.ApiResponse;
import com.smartstudy.note.entity.Note;
import com.smartstudy.note.service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Note>> create(@Valid @RequestBody CreateNoteRequest request) {
        Note note = noteService.create(request.title(), request.content(), request.publicVisible());
        return ResponseEntity.ok(ApiResponse.ok(note));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Note>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(noteService.listAllNotes()));
    }

    @GetMapping("/square")
    public ResponseEntity<ApiResponse<List<Note>>> square() {
        return ResponseEntity.ok(ApiResponse.ok(noteService.listPublicNotes()));
    }

    public record CreateNoteRequest(
            @NotBlank String title,
            @NotBlank String content,
            boolean publicVisible
    ) {
    }
}
