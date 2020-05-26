package com.example.baka2.Tools;

public class Global {

    public static ClientData clientDataGlobal = new ClientData();

    public static boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
    public static String urlReturn(String file) {
        String url = "https://computerizedselfcheckoutcart.tk/requests/app/" + file;
        return url;
    }


}
