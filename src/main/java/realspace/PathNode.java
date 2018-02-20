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
 */
public class PathNode {
    private Vec2 xy,norm;
    private PathNode nextNode, previousNode;
    private Fixture fixture;
    public PathNode(Vec2 xy, Fixture fixture){
        this.nextNode = null;
        this.previousNode = null;
        this.norm = null;
        this.xy = xy;
        this.fixture = fixture;
    }

    public PathNode getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(PathNode previousNode) {
        this.previousNode = previousNode;
    }
    public PathNode(){
        this.nextNode = null;
        this.norm = null;
        this.xy = null;
        this.fixture = null;
    }
    public void setNextNode(PathNode nextNode){
        this.nextNode = nextNode;
    }
    public PathNode getNextNode(){
        return nextNode;
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
