package com.example.ims001;

public class Session {
    private static String username;

    public static void setUsername(String u) {
        username = u;
    }

    public static String getUsername() {
        return username;
    }

    public static void clear() {
        username = null;
    }
}
