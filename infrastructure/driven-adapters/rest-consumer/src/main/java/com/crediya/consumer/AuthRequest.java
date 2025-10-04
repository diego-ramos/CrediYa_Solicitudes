package com.crediya.consumer;

public record AuthRequest(
        String email,
        String password
){}
