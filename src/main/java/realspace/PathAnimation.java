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
        int maxPathSize = 0;
        for (ArrayList<PathNode> path : space.getPaths()){
            if(path.size() > maxPathSize){
                maxPathSize = path.size();
            }
        }
        int pathCounter = 0;
        for(ArrayList<PathNode> path : space.getPaths()){
            pathCounter++;
            if(pathCounter % (double)(space.getPaths().size()/100) == 0){
                nodesToDraw.add(new ArrayList<PathNode>());
                int i = 0;
                for(PathNode node : path){
                    if(nodesToDraw.get(nodesToDraw.size()-1).size() > counter % maxPathSize){
                        break;
                    }
                    i++;
                    nodesToDraw.get(nodesToDraw.size()-1).add(node);
                }
            }
        }        
        return nodesToDraw;
    }
    
    public void step(){
        if(space.getPaths().size() == 0){
            counter = 0;
        }
        stepCounter++;
        if(stepCounter%7 == 0){
            counter++;
        }
    }
    
    public void reset(){
        counter = 0;
    }
}
