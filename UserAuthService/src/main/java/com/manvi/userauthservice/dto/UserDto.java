package com.manvi.userauthservice.dto;

import com.manvi.userauthservice.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private Long id;
    private String email;
    private Role role;
}