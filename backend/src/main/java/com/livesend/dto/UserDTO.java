package com.livesend.dto;

import com.livesend.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String emailHost;
    private Integer emailPort;
    private String emailUsername;
    private boolean hasEmailPassword;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEmailHost(user.getEmailHost());
        dto.setEmailPort(user.getEmailPort());
        dto.setEmailUsername(user.getEmailUsername());
        dto.setHasEmailPassword(user.getEmailPassword() != null && !user.getEmailPassword().isEmpty());
        return dto;
    }
}
