package com.studyroom.common;

import java.security.MessageDigest;

public class Md5Util {
    private static final String SALT = "study_room_2024";

    public static String md5(String input) {
        try {
            String salted = input + SALT;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(salted.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
