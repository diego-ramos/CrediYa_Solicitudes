package com.crediya.consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String RECEIVED_USER = "✅ Received User: {}";
    public static final String REQUESTING_USER_WITH_ID_NUMBER = "➡️ Requesting user with idNumber={}";
    public static final String USER_NOT_FOUND_WITH_ID_NUMBER = "⚠️ No user found with idNumber={}";
    public static final String FINAL_USER_READY = "📦 Final user ready: {}";
    public static final String CIRCUIT_BREAKER_OPENED_FOR_ID = "Circuit breaker opened for id {}: {}";
    public static final String REQUESTING_ADMIN_EMAILS = "Requesting Admin Emails";
    public static final String RECEIVED_EMAILS = "✅ Received Emails: {}";
}
