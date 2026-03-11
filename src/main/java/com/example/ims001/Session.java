package com.example.ims001;

public class Session {
    private static String username;

    private Session() {}

    public static void setUsername(String user) {
        username = user;
    }

    public static String getUsername() {
        return username;
    }

    public static void clear() {
        username = null;
    }
}