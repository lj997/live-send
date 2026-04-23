package com.livesend.service;

import com.livesend.entity.Note;
import com.livesend.entity.User;
import com.livesend.repository.NoteRepository;
import com.livesend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Note> getNotesByUser(Long userId) {
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    @Transactional
    public Note createNote(Long userId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUser(user);
        
        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(Long id, String title, String content) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        
        note.setTitle(title);
        note.setContent(content);
        
        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
}
