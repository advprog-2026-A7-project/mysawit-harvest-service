package com.mysawit.harvest.dto;

import lombok.Data;

@Data
public class IdentityUserResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String mandorId;
    private String mandorName;
    private String certificationNumber;
    private String kebunId;
}
