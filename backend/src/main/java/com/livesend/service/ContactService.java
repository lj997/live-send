package com.livesend.service;

import com.livesend.entity.Contact;
import com.livesend.entity.User;
import com.livesend.repository.ContactRepository;
import com.livesend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Contact> getContactsByUser(Long userId) {
        return contactRepository.findByUserId(userId);
    }

    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }

    @Transactional
    public Contact createContact(Long userId, Contact contact) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        contact.setUser(user);
        return contactRepository.save(contact);
    }

    @Transactional
    public Contact updateContact(Long id, Contact updatedContact) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("联系人不存在"));
        
        contact.setName(updatedContact.getName());
        contact.setEmail(updatedContact.getEmail());
        contact.setRelationship(updatedContact.getRelationship());
        contact.setNote(updatedContact.getNote());
        
        return contactRepository.save(contact);
    }

    @Transactional
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}
