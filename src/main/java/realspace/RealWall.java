/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import javafx.geometry.Point2D;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 *
 * @author samuel
 */
public class RealWall {
    Point2D center, startXY, endXY;
    double rotation;
    double distance;
    public RealWall(Point2D startXY,Point2D endXY, World world, BodyType bt, double width){
        //float width = 1;
        this.startXY = startXY;
        this.endXY = endXY;
        BodyDef bd = new BodyDef();
        
        bd.type = bt ;
        center = startXY.midpoint(endXY);
        bd.position.set((float)center.getX(),(float)center.getY());
        rotation = RealSpace.angle(startXY, endXY);
        distance = startXY.distance(endXY);
        System.out.println(center);
        Body body = world.createBody(bd);
        PolygonShape ps = new PolygonShape();
        ps.setAsBox((float)(distance/2),(float)(width/2), new Vec2(0,0),(float)Math.toRadians(rotation));

        FixtureDef fd = new FixtureDef();
        fd.shape = ps;
        body.createFixture(fd);      
    }
    
    public Point2D getStartXY(){
        return startXY;
    }
    
    public Point2D getEndXY(){
        return endXY;
    }
            
    public Point2D getCenter(){
        return center;
    }
    
    public double getRotation(){
        return rotation;
    }
    
    public double getDistance(){
        return distance;
    }
}
