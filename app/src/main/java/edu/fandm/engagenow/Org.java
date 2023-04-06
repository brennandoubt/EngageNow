package edu.fandm.engagenow;

public class Org {
    private static int counter = 0;

    public long id;
    public String name;
    public String descrip;
    public String url;

    public String userID;

    public Org(String name, String descrip, String url, String userID) {
        this.id = counter++;
        this.name = name;
        this.descrip = descrip;
        //For images
        this.url = url;
        this.userID = userID;
    }
}