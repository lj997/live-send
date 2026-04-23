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

    @Autowired
    private EncryptionService encryptionService;

    private final Object lock = new Object();
    private volatile User cachedDefaultUser = null;

    public User getDefaultUser() {
        if (cachedDefaultUser != null) {
            return cachedDefaultUser;
        }

        synchronized (lock) {
            if (cachedDefaultUser != null) {
                return cachedDefaultUser;
            }

            Optional<User> existing = userRepository.findByUsername("default");
            if (existing.isPresent()) {
                cachedDefaultUser = existing.get();
                return cachedDefaultUser;
            }

            try {
                cachedDefaultUser = createDefaultUser();
                return cachedDefaultUser;
            } catch (Exception e) {
                existing = userRepository.findByUsername("default");
                if (existing.isPresent()) {
                    cachedDefaultUser = existing.get();
                    return cachedDefaultUser;
                }
                throw e;
            }
        }
    }

    private User createDefaultUser() {
        User user = new User();
        user.setUsername("default");
        user.setPassword(encryptionService.hashPassword("default123"));
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
        
        if (emailPassword != null && !emailPassword.isEmpty()) {
            if (!encryptionService.isEncrypted(emailPassword)) {
                emailPassword = encryptionService.encrypt(emailPassword);
            }
            user.setEmailPassword(emailPassword);
        }
        
        cachedDefaultUser = null;
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
        
        String newPassword = updatedUser.getEmailPassword();
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!encryptionService.isEncrypted(newPassword)) {
                newPassword = encryptionService.encrypt(newPassword);
            }
            user.setEmailPassword(newPassword);
        }
        
        cachedDefaultUser = null;
        return userRepository.save(user);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return encryptionService.verifyPassword(plainPassword, hashedPassword);
    }

    public String hashPassword(String plainPassword) {
        return encryptionService.hashPassword(plainPassword);
    }
}
