/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 *
 * @author samuel
 * Emmiter of rays to calculate reflections. Analogous for a speaker.
 */
public class RealSoundSource {
    World world;
    private Point2D xy;
    public RealSoundSource(Point2D xy, World world, BodyType bt){
        this.xy = xy;
        BodyDef bd = new BodyDef();
        this.world = world;
        
        bd.type = bt ;
        bd.position.set((float)xy.getX(),(float)xy.getY());
        
        Body body = world.createBody(bd);
       // body = new Body(bd,world);
        
        CircleShape cs = new CircleShape();
        cs.m_radius = 5f;
        //ps.setAsBox((float)(width/2)/scale,(float)(height/2)/scale);
        
        //ps.setAsBox((float)(width/2)/scale,(float)(height/2)/scale , coordToWorldVec( 0,0), (float)Math.toRadians(rotation));
        FixtureDef fd = new FixtureDef();
        fd.shape = cs;
        body.createFixture(fd);
    }
    
    public void reflectCast(double radiansAngle, RealSpace realSpace){
        Vec2 p1 = new Vec2(), p2 = new Vec2(), collision = new Vec2(), normal = new Vec2();    
        radiansAngle =Math.toRadians(180);
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportFixture(Fixture fxtr, Vec2 point, Vec2 norm, float f) {
                collision.set(point);
                normal.set(norm.add(point));
                normal.set(normal);                
                return 0;
            }
        };
        Point2D target = new Point2D(500,0);
        Point2D targetRotated = new Point2D(target.getX() * Math.cos(radiansAngle) - target.getY()*Math.sin(radiansAngle),target.getX() * Math.sin(radiansAngle) + target.getY()*Math.cos(radiansAngle));
        targetRotated = targetRotated.add(xy);
        world.raycast(callback, RealSpace.Point2DToVec2(xy), RealSpace.Point2DToVec2(targetRotated));
        realSpace.getRays().add(new Ray(xy,targetRotated,realSpace.Vec2toPoint2D(collision),realSpace.Vec2toPoint2D(normal)));
        ArrayList<PathNode> path = new ArrayList<>();
        
        reflectCast(2,realSpace,path);
    }
    
    public void reflectCast(double radiansAngle, RealSpace realSpace, ArrayList<PathNode> path){
        Vec2 p1 = new Vec2(), p2 = new Vec2(), collision = new Vec2(), normal = new Vec2();    
        radiansAngle =Math.toRadians(180);
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportFixture(Fixture fxtr, Vec2 point, Vec2 norm, float f) {
                collision.set(point);
                normal.set(norm.add(point));
                normal.set(normal);                
                return 0;
            }
        };
        Point2D target = new Point2D(500,0);
        Point2D targetRotated = new Point2D(target.getX() * Math.cos(radiansAngle) - target.getY()*Math.sin(radiansAngle),target.getX() * Math.sin(radiansAngle) + target.getY()*Math.cos(radiansAngle));
        targetRotated = targetRotated.add(xy);
        world.raycast(callback, RealSpace.Point2DToVec2(xy), RealSpace.Point2DToVec2(targetRotated));
        realSpace.getRays().add(new Ray(xy,targetRotated,realSpace.Vec2toPoint2D(collision),realSpace.Vec2toPoint2D(normal)));
        System.out.println(targetRotated);
    }
    
    public Point2D getXY(){
        return xy;
    }
}
