/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rayspace;

import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import realspace.PathAnimation;
import realspace.PathNode;
import realspace.Ray;
import realspace.RealHearer;
import realspace.RealSoundSource;
import realspace.RealSpace;
import realspace.RealWall;

/**
 *
 * @author samuel
 * Contains main class, UI and animation timer. 
 */
public class RaySpace extends Application {
        final double realScale = 0.2;
    RealSpace realSpace;
    public RaySpace(){
        realSpace = new RealSpace(realScale);
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        int windowWidth = 1080, windowHeight = 750;//Default window size, not yet able to resize.
        int spaceWidth = 800, spaceHeight = 750;//pixel size of area for editing the room to be simulated.
        BorderPane layout = new BorderPane();
        Scene scene = new Scene(layout,windowWidth,windowHeight);
        Canvas spaceCanvas = new Canvas(spaceWidth,spaceHeight);
        spaceCanvas.getStyleClass().add("spaceCanvas");
        GraphicsContext spaceGC = spaceCanvas.getGraphicsContext2D();
        layout.setLeft(spaceCanvas);
        VBox mainUI = new VBox();
        mainUI.getStyleClass().add("VBox");
        Button helloWorld = new Button("Hello World!");
        helloWorld.getStyleClass().add("button");
        mainUI.getChildren().add(helloWorld);
        mainUI.getStylesheets().add("rayspace/mainUI.css");
        layout.setRight(mainUI);
        mainUI.setFillWidth(true);
        primaryStage.setWidth(windowWidth);
        primaryStage.setHeight(windowHeight);
        primaryStage.setTitle("RaySpace");
        
        spaceGC.setFill(Color.BLACK);//background colour of space editor.
        spaceGC.fillRect(0, 0, windowWidth, windowHeight);
        spaceGC.setStroke(Color.WHITE);
        
        //PixelSpace pixelSpace = new PixelSpace();
        
        Mouse m = new Mouse();
        
        spaceCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(t.getButton().equals(MouseButton.PRIMARY) && !m.isDragging()){
                    realSpace.setHearer(realSpace.pixelXYToReal(new Point2D(t.getX(),t.getY())));
                }else if(t.getButton().equals(MouseButton.SECONDARY) && !m.isDragging()){
                    realSpace.addSoundSource(realSpace.pixelXYToReal(new Point2D(t.getX(),t.getY())));
                }
                m.setDragging(false);
            }
        });
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() { //For testing purposes, should be deleted later.
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode().equals(KeyCode.R)){
                    realSpace.reflectTest();
                }
            }
        });
        spaceCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                 m.setDragging(true);
            }
        });
        spaceCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                m.setPressedXY(new Point2D(t.getX(),t.getY()));
            }
        });
        spaceCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                m.setReleasedXY(new Point2D(t.getX(),t.getY()));
                
                if(m.isDragging() && !t.isControlDown()){
                    realSpace.addWall(realSpace.pixelXYToReal(m.getPressedXY()), realSpace.pixelXYToReal(m.getReleasedXY()));
                }
                if(m.isDragging() && t.isControlDown()){
                    World world = realSpace.getWorld();
        
                    Vec2 p1 = new Vec2(), p2 = new Vec2(), collision = new Vec2(), normal = new Vec2();    
                    RayCastCallback callback = new RayCastCallback() {
                        @Override
                        public float reportFixture(Fixture fxtr, Vec2 point, Vec2 norm, float f) {
                            collision.set(point);
                            normal.set(norm.add(point));
                            normal.set(normal);                
                            return 0;
                        }
                    };
                    p2 = realSpace.Point2DToVec2(realSpace.pixelXYToReal(m.getPressedXY()));
                    p1 = realSpace.Point2DToVec2(realSpace.pixelXYToReal(m.getReleasedXY()));
                    world.raycast(callback, p2, p1);
                    //realSpace.getRays().add(new Ray(realSpace.Vec2toPoint2D(p1),realSpace.Vec2toPoint2D(p2),realSpace.Vec2toPoint2D(collision),realSpace.Vec2toPoint2D(normal)));

                }
            }
        });
        
        
        World world = realSpace.getWorld();
        PathAnimation pa = new PathAnimation(realSpace);
        AnimationTimer at = new AnimationTimer() {
            @Override
            public void handle(long now) {
                spaceGC.fillRect(0, 0, spaceWidth, spaceHeight);
                world.step(0.02f, 8, 6);
                if(realSpace.getHearer() != null){
                    RealHearer hearer = realSpace.getHearer();
                    Point2D hearerPixelXY = realSpace.realXYToPixel(hearer.getXY());
                    spaceGC.setStroke(Color.WHITE);
                    spaceGC.strokeOval(hearerPixelXY.getX()-25, hearerPixelXY.getY()-25, 50, 50);
                }
                if(!realSpace.getSoundSources().isEmpty()){
                    for(RealSoundSource source : realSpace.getSoundSources()){
                        Point2D sourcePixelXY = realSpace.realXYToPixel(source.getXY());
                        spaceGC.setStroke(Color.BLUE);
                        spaceGC.strokeOval(sourcePixelXY.getX()-25,sourcePixelXY.getY()-25,50,50);
                    }
                }
                if(!realSpace.getWalls().isEmpty()){
                    spaceGC.setStroke(Color.RED);
                    for(RealWall wall : realSpace.getWalls()){
                        
                        spaceGC.save();
                        Point2D wallPixelXY = realSpace.realXYToPixel(wall.getCenter());
                        spaceGC.translate(wallPixelXY.getX(), wallPixelXY.getY());
                        spaceGC.rotate(wall.getRotation());
                        spaceGC.strokeRect(-(wall.getDistance()/realScale)/2,-50/2,wall.getDistance()/realScale, 50);
                        

                        spaceGC.restore(); 

                        
                    }
                }

                pa.step();
                
                for(ArrayList<PathNode> nodes : pa.nodesToDraw()){
                    Point2D start,end;
                    for(int i = 0; i < nodes.size()-1; i++){
                        start = realSpace.realXYToPixel(RealSpace.Vec2toPoint2D(nodes.get(i).getXy()));
                        end = realSpace.realXYToPixel(RealSpace.Vec2toPoint2D(nodes.get(i+1).getXy()));
                        spaceGC.setStroke(Color.GREEN);
                        spaceGC.strokeLine(start.getX() ,start.getY(), end.getX(), end.getY());
                        
                    }
                }
               
            }
        };
        at.start();
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}
