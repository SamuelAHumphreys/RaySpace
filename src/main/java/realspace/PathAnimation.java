/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realspace;

import java.util.ArrayList;

/**
 *
 * @author samuel
 */
public class PathAnimation {
    private RealSpace space;
    private int counter,stepCounter;
    public PathAnimation(RealSpace space){
        this.space = space;
        counter = 0;
        stepCounter = 0;
    }
    
    public ArrayList<ArrayList<PathNode>> nodesToDraw(){
        ArrayList<ArrayList<PathNode>> nodesToDraw = new ArrayList<>();
        for(PathNode origin : space.getOriginNodes()){
            nodesToDraw.add(new ArrayList<PathNode>());
            PathNode nextNode = origin.getNextNode();
            int i = 0;
            while(origin.getNextNode() != null && i < counter){
                i++;
                nodesToDraw.get(nodesToDraw.size()-1).add(origin);
                origin = origin.getNextNode();
                nextNode = origin.getNextNode();
                if(origin.getNextNode() == null){
                    counter = -1;
                }
            }
        }        
        return nodesToDraw;
    }
    
    public void step(){
        stepCounter++;
        if(stepCounter%15 == 0){
            counter++;
        }
    }
}
