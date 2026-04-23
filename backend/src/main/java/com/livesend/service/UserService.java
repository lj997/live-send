package com.livesend.service;

import com.livesend.entity.User;
import com.livesend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getDefaultUser() {
        return userRepository.findById(1L)
                .orElseGet(() -> createDefaultUser());
    }

    private User createDefaultUser() {
        User user = new User();
        user.setUsername("default");
        user.setPassword("default123");
        user.setEmail("user@example.com");
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateEmailConfig(Long userId, String email, String host, Integer port, 
                                   String emailUsername, String emailPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEmail(email);
        user.setEmailHost(host);
        user.setEmailPort(port);
        user.setEmailUsername(emailUsername);
        user.setEmailPassword(emailPassword);
        return userRepository.save(user);
    }

    @Transactional
    public User updateEmailConfig(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEmail(updatedUser.getEmail());
        user.setEmailHost(updatedUser.getEmailHost());
        user.setEmailPort(updatedUser.getEmailPort());
        user.setEmailUsername(updatedUser.getEmailUsername());
        user.setEmailPassword(updatedUser.getEmailPassword());
        return userRepository.save(user);
    }
}
