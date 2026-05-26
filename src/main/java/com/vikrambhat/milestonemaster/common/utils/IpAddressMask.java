package com.vikrambhat.milestonemaster.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpAddressMask {

    public static final Pattern IPV4_PATTERN = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b"
    );
    private static final String IPV4_MASK = "XXX.XXX.XXX.XXX";
    private IpAddressMask() {
    }
    public static String maskEntireIp(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Matcher matcher = IPV4_PATTERN.matcher(message);
        return matcher.replaceAll(IPV4_MASK);
    }
    public static String maskPartialIp(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Pattern lastOctetPattern = Pattern.compile(
                "\\b((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3})(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b"
        );
        Matcher matcher = lastOctetPattern.matcher(message);
        return matcher.replaceAll("$1XXX");
    }
}
