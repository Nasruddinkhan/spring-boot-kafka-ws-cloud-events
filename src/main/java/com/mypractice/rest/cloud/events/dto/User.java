package com.mypractice.rest.cloud.events.dto;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String message;
    private Integer age;
}
