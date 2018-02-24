/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;


/**
 *
 * @author samuel
 * Physics space for containing all other .realspace objects. Contains helper methods for converting between pixelSpace and JBox2D "real" space.
 */
public class RealSpace {
    private double scale;
    private RealHearer hearer;
    private ArrayList<RealWall> walls;
    private World world;
    private ArrayList<RealSoundSource> soundSources;
    private ArrayList<ArrayList<PathNode>> paths;
    public RealSpace(double scale){
        hearer = null;
        walls = new ArrayList<>();
        soundSources = new ArrayList<>();
        paths = new ArrayList<>();
        this.scale = scale;

        Vec2 gravity = new Vec2(0, -9.8f);
        this.world = new World(gravity, true);
    }
    public void setHearer(Point2D realXY){
        if(hearer != null){
            world.destroyBody(hearer.getBody());
        }
        hearer = new RealHearer(realXY,world,BodyType.STATIC);
    }
    public RealHearer getHearer(){
        return hearer;
    }
    public void addWall(Point2D startXY, Point2D endXY){
        walls.add(new RealWall(startXY, endXY, world, BodyType.STATIC));
    }
    public Point2D pixelXYToReal(Point2D pixel){
        return new Point2D(pixel.getX()*scale,pixel.getY()*scale);
    }
    public Point2D realXYToPixel(Point2D real){
        return new Point2D(real.getX()/scale,real.getY()/scale);
    }
    public ArrayList<RealWall> getWalls(){
        return walls;
    }
    public void addSoundSource(Point2D realXY){
        soundSources.add(new RealSoundSource(realXY, world, BodyType.STATIC));
    }
    public ArrayList<RealSoundSource> getSoundSources(){
        return soundSources;
    }
    
    public World getWorld(){
        return world;
    }
 
    public static double angle(Point2D point1, Point2D point2){
        return Math.toDegrees(Math.atan2(point2.getY() - point1.getY(),point2.getX() - point1.getX()));
    }
    
    public static Vec2 Point2DToVec2(Point2D point){
        return new Vec2((float)point.getX(),(float)point.getY());
    }
    
    public static Point2D Vec2toPoint2D(Vec2 vec){
        return new Point2D(vec.x,vec.y);
    }
    
    public ArrayList<ArrayList<PathNode>> getPaths(){
        return paths;
    }
    
    public void reflectTest(){
        for(double i = 0; i < 360; i+=20){
            for(RealSoundSource ss : soundSources){
                paths.add(ss.reflectCast(Math.toRadians(i), this));
            }
        }
    }
}
