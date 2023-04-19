package edu.fandm.engagenow;

import java.util.HashMap;

public class Org {
    private static int counter = 0;

    public long id;
    public String name;
    public String descrip;
    public String url;

    public String userID;
    public HashMap<String, Object> m;
    public String event;

    public Org(String name, String descrip, String url, String userID, HashMap<String, Object> m, String event) {
        this.id = counter++;
        this.name = name;
        this.descrip = descrip;
        //For images
        this.url = url;
        this.userID = userID;
        this.m = m;
        this.event = event;
    }
}