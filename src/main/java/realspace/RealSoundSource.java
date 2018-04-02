/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import java.util.ArrayList;
import java.util.Random;
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
    private static int maxPathSize = 150;
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
    
    public ArrayList<PathNode> reflectCast(double radiansAngle, RealSpace realSpace, double surfaceRoughness){
        PathNode origin = new PathNode(RealSpace.Point2DToVec2(this.getXY()), fixture);
        PathNode rayHit = new PathNode();
        ArrayList<PathNode> path = new ArrayList<>();
        path.add(origin);
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportFixture(Fixture fxtr, Vec2 point, Vec2 norm, float f) {
                if(fxtr.equals(fixture)){
                    return -1;
                }else if(realSpace.getHearer() != null && realSpace.getHearer().getFixture().equals(fxtr)){
                    rayHit.setStereoChannel(1);
                }else if(realSpace.getStereoHearer() != null && realSpace.getStereoHearer().getFixture().equals(fxtr)){
                    rayHit.setStereoChannel(2);
                }
                rayHit.setXy(new Vec2(point));
                rayHit.setNorm(new Vec2(norm));
                rayHit.setFixture(fxtr);
                return f;
            }
        };
        Point2D target = new Point2D(500,0);
        Point2D targetRotated = new Point2D(target.getX() * Math.cos(radiansAngle) - target.getY()*Math.sin(radiansAngle),target.getX() * Math.sin(radiansAngle) + target.getY()*Math.cos(radiansAngle));
        targetRotated = targetRotated.add(xy);
        world.raycast(callback, RealSpace.Point2DToVec2(xy), RealSpace.Point2DToVec2(targetRotated));
        if(rayHit.getFixture() != null){
            Vec2 v = new Vec2((float)Math.cos(radiansAngle), (float)Math.sin(radiansAngle));
            Point2D d = RealSpace.Vec2toPoint2D(v);
            double top = d.dotProduct(RealSpace.Vec2toPoint2D(rayHit.getNorm()));
            top *=2;
            Vec2 r = v.sub(rayHit.getNorm().mul((float)top));
            path.add(rayHit);
            
            Random ran = new Random();
            double random1 = ran.nextGaussian()*surfaceRoughness;
            double random2 = ran.nextGaussian()*surfaceRoughness;
            
            Vec2 r2 = r.clone();
            r2.set(r.x+(float)random1, r.y+(float)random2);
            int i = 0;
            while(1.5708 < Math.abs(Math.atan2(rayHit.getNorm().y,rayHit.getNorm().x) - Math.atan2(r2.y,r2.x)) && i < 100){
                i++;
                random1 = ran.nextGaussian()*(surfaceRoughness*1.5708);
                random2 = ran.nextGaussian()*(surfaceRoughness*1.5708);
                r2.set(r.x+(float)random1, r.y+(float)random2);
            }
            if(i == 100){
                random1 = 0;
                random2 = 0;
            }

            if(path.size() < maxPathSize && !rayHit.getFixture().isSensor()){
                reflectCast( Math.atan2(r2.y, r2.x),realSpace,path);
            }
        }else{
            rayHit.setXy(RealSpace.Point2DToVec2(targetRotated));
            path.add(rayHit);

        }
        return path;
    }
    
    public void reflectCast(double radiansAngle, RealSpace realSpace, ArrayList<PathNode> currentPath){
        PathNode rayHit = new PathNode();
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportFixture(Fixture fxtr, Vec2 point, Vec2 norm, float f) {

                if(fxtr.equals(fixture)){
                    return -1;
                }else if(realSpace.getHearer() != null && realSpace.getHearer().getFixture().equals(fxtr)){
                    rayHit.setStereoChannel(1);
                }else if(realSpace.getStereoHearer() != null && realSpace.getStereoHearer().getFixture().equals(fxtr)){
                    rayHit.setStereoChannel(2);
                }
                rayHit.setXy(new Vec2(point));
                rayHit.setNorm(new Vec2(norm));
                rayHit.setFixture(fxtr);
                return f;
            }
        };
        Point2D target = new Point2D(500,0);
        Point2D targetRotated = new Point2D(target.getX() * Math.cos(radiansAngle) - target.getY()*Math.sin(radiansAngle),target.getX() * Math.sin(radiansAngle) + target.getY()*Math.cos(radiansAngle));
        targetRotated = targetRotated.add(RealSpace.Vec2toPoint2D(currentPath.get(currentPath.size()-1).getXy()));
        world.raycast(callback, currentPath.get(currentPath.size()-1).getXy(), RealSpace.Point2DToVec2(targetRotated));
        if(rayHit.getFixture() != null){
            Vec2 v = new Vec2((float)Math.cos(radiansAngle), (float)Math.sin(radiansAngle));
            Point2D d = RealSpace.Vec2toPoint2D(v);
            double top = d.dotProduct(RealSpace.Vec2toPoint2D(rayHit.getNorm()));
            top *=2;
            Vec2 r = v.sub(rayHit.getNorm().mul((float)top));
            currentPath.add(rayHit);
            if(currentPath.size() < maxPathSize && !rayHit.getFixture().isSensor()){
                reflectCast( Math.atan2(r.y, r.x),realSpace,currentPath);
            }
        }else{
            rayHit.setXy(RealSpace.Point2DToVec2(targetRotated));
            currentPath.add(rayHit);
        }
    }
    
    public Point2D getXY(){
        return xy;
    }
}
