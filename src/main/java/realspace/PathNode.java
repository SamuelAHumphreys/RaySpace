/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

/**
 *
 * @author samuel
 * Place in physics space where a ray either starts or ends. Linked together to create a path.
 */
public class PathNode {
    private Vec2 xy,norm;
    private Fixture fixture;
    public PathNode(Vec2 xy, Fixture fixture){
        this.norm = null;
        this.xy = xy;
        this.fixture = fixture;
    }
    public PathNode(){
        this.norm = null;
        this.xy = null;
        this.fixture = null;
    }

    
    public Vec2 getXy() {
        return xy;
    }

    public void setXy(Vec2 xy) {
        this.xy = xy;
    }

    public Vec2 getNorm() {
        return norm;
    }

    public void setNorm(Vec2 norm) {
        this.norm = norm;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }
    
}
