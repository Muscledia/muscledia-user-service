package com.muscledia.user_service.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataDTO {
    private Long userId;
    private Double height;
    private Double weight;
    private String goalType;
    private String gender;
    private Integer age;
}
