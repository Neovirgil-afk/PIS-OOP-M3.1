package com.example.ims001;

public class UserSession {
    private static int userId;
    private static String username;
    private static String fullName;

    public static void setUser(int id, String uname, String name) {
        userId = id;
        username = uname;
        fullName = name;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getFullName() {
        return fullName;
    }

    public static void clear() {
        userId = 0;
        username = null;
        fullName = null;
    }
}