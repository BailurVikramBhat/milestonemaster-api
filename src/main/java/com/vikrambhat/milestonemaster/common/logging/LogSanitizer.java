package com.vikrambhat.milestonemaster.common.logging;

public final class LogSanitizer {
    private LogSanitizer() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "invalid-email";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (localPart.length() == 1) {
            return "*" + domain;
        }

        return localPart.charAt(0) + "***" + domain;
    }
}
