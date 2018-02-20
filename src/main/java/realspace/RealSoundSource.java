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
 * Emitter of rays to calculate reflections. Analogous for a speaker.
 */
public class RealSoundSource {
    private World world;
    private Point2D xy;
    private Fixture fixture;
    public RealSoundSource(Point2D xy, World world, BodyType bt){
        this.xy = xy;
        BodyDef bd = new BodyDef();
        this.world = world;
        bd.type = bt ;
        bd.position.set((float)xy.getX(),(float)xy.getY());
        Body body = world.createBody(bd);        
        CircleShape cs = new CircleShape();
        cs.m_radius = 5f;        
        FixtureDef fd = new FixtureDef();
        fd.shape = cs;
        fixture = body.createFixture(fd);
    }
    
    public PathNode reflectCast(double radiansAngle, RealSpace realSpace){
        PathNode origin = new PathNode(RealSpace.Point2DToVec2(this.getXY()), fixture);
        PathNode rayHit = new PathNode();
        origin.setNextNode(rayHit);
        rayHit.setPreviousNode(origin);
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportFixture(Fixture fxtr, Vec2 point, Vec2 norm, float f) {
                rayHit.setXy(new Vec2(point));
                rayHit.setNorm(new Vec2(norm));
                rayHit.setFixture(fxtr);
                return 0;
            }
        };
        Point2D target = new Point2D(500,0);
        Point2D targetRotated = new Point2D(target.getX() * Math.cos(radiansAngle) - target.getY()*Math.sin(radiansAngle),target.getX() * Math.sin(radiansAngle) + target.getY()*Math.cos(radiansAngle));
        targetRotated = targetRotated.add(xy);
        world.raycast(callback, RealSpace.Point2DToVec2(xy), RealSpace.Point2DToVec2(targetRotated));
        if(rayHit.getFixture() != null){
            System.out.println("YARRRRR HIT CAPTAIN");
            Vec2 v = new Vec2((float)Math.cos(radiansAngle), (float)Math.sin(radiansAngle));
            Point2D d = RealSpace.Vec2toPoint2D(v);
            double top = d.dotProduct(RealSpace.Vec2toPoint2D(rayHit.getNorm()));
            top *=2;
            Vec2 r = v.sub(rayHit.getNorm().mul((float)top));
            reflectCast( Math.atan2(r.y, r.x),realSpace,rayHit);
        }else{
            origin.setNextNode(null);
        }
        return origin;
    }
    
    public void reflectCast(double radiansAngle, RealSpace realSpace, PathNode currentNode){
        PathNode rayHit = new PathNode();
        currentNode.setNextNode(rayHit);
        rayHit.setPreviousNode(currentNode);
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportFixture(Fixture fxtr, Vec2 point, Vec2 norm, float f) {
                rayHit.setXy(new Vec2(point));
                rayHit.setNorm(new Vec2(norm));
                rayHit.setFixture(fxtr);
                return 0;
            }
        };
        Point2D target = new Point2D(500,0);
        Point2D targetRotated = new Point2D(target.getX() * Math.cos(radiansAngle) - target.getY()*Math.sin(radiansAngle),target.getX() * Math.sin(radiansAngle) + target.getY()*Math.cos(radiansAngle));
        targetRotated = targetRotated.add(RealSpace.Vec2toPoint2D(currentNode.getXy()));
        world.raycast(callback, currentNode.getXy(), RealSpace.Point2DToVec2(targetRotated));
        if(rayHit.getFixture() != null){
            System.out.println("YARRRRR HIT CAPTAIN");
            Vec2 v = new Vec2((float)Math.cos(radiansAngle), (float)Math.sin(radiansAngle));
            Point2D d = RealSpace.Vec2toPoint2D(v);
            double top = d.dotProduct(RealSpace.Vec2toPoint2D(rayHit.getNorm()));
            top *=2;
            Vec2 r = v.sub(rayHit.getNorm().mul((float)top));
            reflectCast( Math.atan2(r.y, r.x),realSpace,rayHit);
        }else{
            currentNode.setNextNode(null);
        }
    }
    
    public Point2D getXY(){
        return xy;
    }
}
