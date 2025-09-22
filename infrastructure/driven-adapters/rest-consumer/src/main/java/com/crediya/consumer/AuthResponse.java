package com.crediya.consumer;

public record AuthResponse (
        String token,
        String username,
        String roleName
){}
