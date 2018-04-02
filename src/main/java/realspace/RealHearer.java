/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import javafx.geometry.Point2D;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 *
 * @author samuel
 * Intended end point for rays cast by RealSoundSource, analogous to an ear. Contains Jbox2D variables. 
 */
public class RealHearer {
    private Body body;
    private Point2D xy;
    private Fixture fixture;
    public RealHearer(Point2D xy, World world, BodyType bt){
        this.xy = xy;
        BodyDef bd = new BodyDef();
        bd.type = bt ;
        bd.position.set((float)xy.getX(),(float)xy.getY());
        body = world.createBody(bd);
        CircleShape cs = new CircleShape();
        cs.m_radius = 5f;
        FixtureDef fd = new FixtureDef();
        fd.shape = cs;
        fixture = body.createFixture(fd);
        body.getFixtureList().setSensor(true);
    }
    
    public Body getBody(){
        return body;
    }
    
    public Point2D getXY(){
        return xy;
    }
    
    public Fixture getFixture(){
        return fixture;
    }
}
