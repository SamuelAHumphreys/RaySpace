/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rayspace;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import realspace.RealHearer;
import realspace.RealSoundSource;
import realspace.RealSpace;
import realspace.RealWall;

/**
 *
 * @author samuel
 */
public class RaySpaceTest extends ApplicationTest{
    /*
    final double realScale = 0.2;
    RealSpace realSpace;
    public RaySpaceTest(){
        realSpace = new RealSpace(realScale);
    }
    
    @Override public void start(Stage primaryStage) {
           
        int windowWidth = 1080, windowHeight = 750;
        int spaceWidth = 800, spaceHeight = 750;
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
        
        spaceGC.setFill(Color.BLACK);
        spaceGC.fillRect(0, 0, windowWidth, windowHeight);
        spaceGC.setStroke(Color.WHITE);
        
        //PixelSpace pixelSpace = new PixelSpace();
        
        Mouse m = new Mouse();
        
        spaceCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(t.getButton().equals(MouseButton.PRIMARY) && !m.isDragging()){
                   // pixelSpace.setHearer(new Point2D(t.getX(),t.getY()));
                    realSpace.setHearer(realSpace.pixelXYToReal(new Point2D(t.getX(),t.getY())));
                }else if(t.getButton().equals(MouseButton.SECONDARY) && !m.isDragging()){
                    realSpace.addSoundSource(realSpace.pixelXYToReal(new Point2D(t.getX(),t.getY())));
                }
                m.setDragging(false);
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
                
                if(m.isDragging()){
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
                    p2 = realSpace.Point2DToVec2(m.getPressedXY());
                    p1 = realSpace.Point2DToVec2(m.getReleasedXY());
                    world.raycast(callback, p1, p2);
                    realSpace.getRays().add(new Ray(realSpace.Vec2toPoint2D(p1),realSpace.Vec2toPoint2D(p2),realSpace.Vec2toPoint2D(collision),realSpace.Vec2toPoint2D(normal)));

                }
            }
        });
        
        
        World world = realSpace.getWorld();
        
        
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
                if(!realSpace.getRays().isEmpty()){
                    spaceGC.setStroke(Color.GREEN);
                    for(Ray r : realSpace.getRays()){
                        spaceGC.setStroke(Color.GREEN);
                        spaceGC.strokeLine(realSpace.realXYToPixel(r.getStart()).getX(),realSpace.realXYToPixel(r.getStart()).getY(), realSpace.realXYToPixel(r.getEnd()).getX(), realSpace.realXYToPixel(r.getEnd()).getY());
                        Point2D pixelCollision = realSpace.realXYToPixel(r.getCollision());
                        Point2D pixelNormal = realSpace.realXYToPixel(r.getNormal());
                        spaceGC.setStroke(Color.WHITE);
                        System.out.println(pixelNormal);
                        spaceGC.strokeLine(pixelCollision.getX() ,pixelCollision.getY(), pixelNormal.getX(),  pixelNormal.getY());
                    }
                }
            }
        };
        at.start();
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Test public void should_contain_button() {
        // expect:
        verifyThat(".button", hasText("Hello World!"));
    }


    @Test public void should_create_real_hearer() {
        // when:
        clickOn(".spaceCanvas");
        // then:
        assertEquals(realSpace.getHearer().getXY(),new Point2D(400*realScale,375*realScale));
    }

    @Test public void should_create_real_sound_source() {
        // when:
        rightClickOn(".spaceCanvas");
        // then:
        assertEquals(realSpace.getSoundSources().get(0).getXY(), new Point2D(400*realScale, 375*realScale));
    }
    
    @Test public void should_create_real_wall() {
        // when:
        moveTo(".spaceCanvas");
        moveBy(50,50);
        drag();
        dropBy(50,50);
        // then:
        assertEquals(realSpace.getWalls().get(0).getCenter(),new Point2D(475*realScale,450*realScale));
    }
     
    @Test public void test_wall_rotation() {
        // when:
        moveTo(".spaceCanvas");
        //moveBy(50,50);
        drag();
        dropBy(0,100);
        // then:
        
        assertEquals((double)realSpace.getWalls().get(0).getRotation() , 90,0.001);
    }
    
    @Test public void test_ray_cast() {
        moveTo(".spaceCanvas");
        moveBy(50,50);
        drag();
        dropBy(50,50);
        moveBy(200,200);
        press(MouseButton.PRIMARY);
        release(MouseButton.PRIMARY);
        
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
        p2 = realSpace.Point2DToVec2(realSpace.getWalls().get(0).getCenter());
        p1 = realSpace.Point2DToVec2(realSpace.getHearer().getXY());
        world.raycast(callback, p1, p2);

        realSpace.getRays().add(new Ray(realSpace.Vec2toPoint2D(p1),realSpace.Vec2toPoint2D(p2),realSpace.Vec2toPoint2D(collision),realSpace.Vec2toPoint2D(normal)));

        
            
            
    }*/
}
