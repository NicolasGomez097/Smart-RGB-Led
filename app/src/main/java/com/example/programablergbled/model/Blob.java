package com.example.programablergbled.model;

public class Blob {
    private Vector2D<Integer> min;
    private Vector2D<Integer> max;
    public Integer pixelCount;
    public String description;

    public Blob(){
        pixelCount = 0;
    }

    public Blob(int x, int y, String desc){
        pixelCount = 0;
        addPoint(x,y);
        this.description = desc;
    }

    public void addPoint(int x, int y){
        if(min == null || max == null){
            min = new Vector2D<>(x,y);
            max = new Vector2D<>(x,y);
        }else{
            if( x < min.x)
                min.x = x;

            if( y < min.y)
                min.y = y;

            if( x > max.x)
                max.x = x;

            if( y > max.y)
                max.y = y;
        }
        pixelCount++;
    }

    public Vector2D<Integer> getCenter(){
        Vector2D<Integer> center = new Vector2D<>();
        center.x = (min.x + max.x)/2;
        center.y = (min.y + max.y)/2;
        return center;
    }

    public int getWidth(){
        return max.x-min.x;
    }

    public int getHeight(){
        return max.y-min.y;
    }

    public int getDistance(int x, int y){
        int dist1 = (int)(Math.sqrt(Math.pow(min.x-x,2.0)+Math.pow(min.y-y,2.0)));
        int dist2 = (int)(Math.sqrt(Math.pow(max.x-x,2.0)+Math.pow(min.y-y,2.0)));
        int dist3 = (int)(Math.sqrt(Math.pow(max.x-x,2.0)+Math.pow(max.y-y,2.0)));
        int dist4 = (int)(Math.sqrt(Math.pow(min.x-x,2.0)+Math.pow(max.y-y,2.0)));
        return Math.min(Math.min(dist1,dist2),Math.min(dist3,dist4));
    }
}
