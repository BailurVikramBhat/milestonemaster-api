package com.vikrambhat.milestonemaster.common.utils;

import java.util.Locale;

public final class EmailFormatter {
    private EmailFormatter() {
    }
    public static String normalize(final String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
