package rayspace;

import javafx.geometry.Point2D;

/**
 *
 * @author samuel
 * Class for storing some information relating to the state of the users mouse. Should be translated into inner helper class within RaySpace.
 */
public class Mouse {
    private Point2D pressedXY, releasedXY;
    private boolean dragging;
    public Mouse(){
        dragging = false;
        pressedXY = new Point2D(0,0);
        releasedXY = new Point2D(0,0);
    }
    
    public Point2D getPressedXY(){
        return pressedXY;
    }
    
    public Point2D getReleasedXY(){
        return releasedXY;
    }
    
    public void setPressedXY(Point2D pressedXY){
        this.pressedXY = pressedXY;
    }
    
    public void setReleasedXY(Point2D releasedXY){
        this.releasedXY = releasedXY;
    }
    
    public boolean isDragging(){
        return dragging;
    }
    
    public void setDragging(boolean dragging){
        this.dragging = dragging;
    }
}
