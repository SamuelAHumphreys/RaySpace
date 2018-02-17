/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import javafx.geometry.Point2D;

/**
 *
 * @author samuel
 * Stores information given from raycasts within JBox2D. May become obsolete after PathNode implemented.
 */
public class Ray {
    Point2D start,end,collision,normal;
    
    public Ray(Point2D start, Point2D end, Point2D collision, Point2D normal){
        this.start = start;
        this.end = end;
        this.collision = collision;
        this.normal = normal;
    }
    
    public Point2D getStart(){
        return start;
    }
    
    public Point2D getEnd(){
        return end;
    }
    
    public Point2D getCollision(){
        return collision;
    }
    
    public Point2D getNormal(){
        return normal;
    }
    
}
