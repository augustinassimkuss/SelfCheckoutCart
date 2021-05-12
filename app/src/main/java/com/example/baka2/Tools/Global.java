package com.example.baka2.Tools;

import java.util.ArrayList;
import java.util.List;

public class Global {

    public static ClientData clientDataGlobal = new ClientData();
    public static List<String> createShoppingLists = new ArrayList<>();
    public static List<Integer> createShoppingListsCount = new ArrayList<>();
    public static List<Integer> createShoppingListsID = new ArrayList<>();
    public static String[][] items;
    public static boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
    public static String urlReturn(String file) {
        String url = "https://computerizedselfcheckoutcart.tk/requests/app/" + file;
        return url;
    }


}
