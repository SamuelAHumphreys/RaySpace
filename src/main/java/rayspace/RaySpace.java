/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rayspace;

import audio.WavProcessor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
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
    private RealSpace realSpace;
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
        Button importWavButton = new Button("Import WAV");
        importWavButton.getStyleClass().add("button");
        mainUI.getChildren().add(importWavButton);
        FileChooser fileChooser = new FileChooser();
        WavProcessor wp = new WavProcessor();
        importWavButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent e) {
            fileChooser.setTitle("Select Wav File");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("WAV", "*.wav"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if(file != null){
                try {
                    wp.setFile(file);
                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        });
        
        
        Label angleSliderLabel = new Label("Ray Angle Range");
        Label centerSliderLabel = new Label("Ray center");
        mainUI.getChildren().add(angleSliderLabel);

        
        
        Slider angleSlider = new Slider();
        angleSlider.setMin(1);
        angleSlider.setMax(360);
        angleSlider.setValue(180);
        angleSlider.setShowTickLabels(true);
        angleSlider.setShowTickMarks(true);
        angleSlider.setMajorTickUnit(50);
        angleSlider.setMinorTickCount(5);
        angleSlider.setBlockIncrement(10);
        mainUI.getChildren().add(angleSlider);
        
        mainUI.getChildren().add(centerSliderLabel);
        
        Slider centerSlider = new Slider();
        centerSlider.setMin(0);
        centerSlider.setMax(360);
        centerSlider.setValue(180);
        centerSlider.setShowTickLabels(true);
        centerSlider.setShowTickMarks(true);
        centerSlider.setMajorTickUnit(50);
        centerSlider.setMinorTickCount(5);
        centerSlider.setBlockIncrement(10);
        mainUI.getChildren().add(centerSlider);
        
        
        
        mainUI.setMaxWidth(1000);

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
                    realSpace.reflect(20,angleSlider.getValue(),centerSlider.getValue());
                    wp.applyReverb(realSpace.getPaths());
                    try {
                        wp.save();
                    } catch (IOException ex) {
                        Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
                
                if(m.isDragging()){
                    realSpace.addWall(realSpace.pixelXYToReal(m.getPressedXY()), realSpace.pixelXYToReal(m.getReleasedXY()));
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
