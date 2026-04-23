package com.livesend.controller;

import com.livesend.dto.ApiResponse;
import com.livesend.entity.Note;
import com.livesend.service.NoteService;
import com.livesend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<List<Note>> getAllNotes() {
        Long userId = userService.getDefaultUser().getId();
        List<Note> notes = noteService.getNotesByUser(userId);
        return ApiResponse.success(notes);
    }

    @GetMapping("/{id}")
    public ApiResponse<Note> getNote(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("笔记不存在"));
    }

    @PostMapping
    public ApiResponse<Note> createNote(@RequestBody Note note) {
        Long userId = userService.getDefaultUser().getId();
        Note createdNote = noteService.createNote(userId, note.getTitle(), note.getContent());
        return ApiResponse.success("笔记创建成功", createdNote);
    }

    @PutMapping("/{id}")
    public ApiResponse<Note> updateNote(@PathVariable Long id, @RequestBody Note note) {
        try {
            Note updatedNote = noteService.updateNote(id, note.getTitle(), note.getContent());
            return ApiResponse.success("笔记更新成功", updatedNote);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNote(@PathVariable Long id) {
        try {
            noteService.deleteNote(id);
            return ApiResponse.success("笔记删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
