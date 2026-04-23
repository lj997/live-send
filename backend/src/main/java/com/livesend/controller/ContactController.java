package com.livesend.controller;

import com.livesend.dto.ApiResponse;
import com.livesend.entity.Contact;
import com.livesend.service.ContactService;
import com.livesend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<List<Contact>> getAllContacts() {
        Long userId = userService.getDefaultUser().getId();
        List<Contact> contacts = contactService.getContactsByUser(userId);
        return ApiResponse.success(contacts);
    }

    @GetMapping("/{id}")
    public ApiResponse<Contact> getContact(@PathVariable Long id) {
        return contactService.getContactById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("联系人不存在"));
    }

    @PostMapping
    public ApiResponse<Contact> createContact(@RequestBody Contact contact) {
        Long userId = userService.getDefaultUser().getId();
        Contact createdContact = contactService.createContact(userId, contact);
        return ApiResponse.success("联系人创建成功", createdContact);
    }

    @PutMapping("/{id}")
    public ApiResponse<Contact> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        try {
            Contact updatedContact = contactService.updateContact(id, contact);
            return ApiResponse.success("联系人更新成功", updatedContact);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteContact(@PathVariable Long id) {
        try {
            contactService.deleteContact(id);
            return ApiResponse.success("联系人删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
