package com.senior491.mobileapp;

public class Destination {
    private String name;
    private String[] corners;

    public Destination(String name, String[] corners) {
        this.name = name;
        this.corners = corners;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getCorners() {
        return corners;
    }

    public void setCorners(String[] corners) {
        this.corners = corners;
    }
}
