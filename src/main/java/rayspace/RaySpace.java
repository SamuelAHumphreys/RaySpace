/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rayspace;

import audio.ProgressListener;
import audio.WavProcessor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jbox2d.dynamics.World;
import realspace.PathAnimation;
import realspace.PathNode;
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
    final double realScale = 0.2;//Size of simulation in meters relative to pixels
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
        final int wallWidth = 20;//pixels
        PathAnimation pa = new PathAnimation(realSpace);
        WavProcessor wp = new WavProcessor();
        BorderPane layout = new BorderPane();
        Scene scene = new Scene(layout,windowWidth,windowHeight);
        primaryStage.setResizable(false);
        Canvas spaceCanvas = new Canvas(spaceWidth,spaceHeight);
        spaceCanvas.getStyleClass().add("spaceCanvas");
        GraphicsContext spaceGC = spaceCanvas.getGraphicsContext2D();
        layout.setLeft(spaceCanvas);
        VBox mainUI = new VBox();
        mainUI.getStyleClass().add("VBox");
        //UI labels and sliders---------------------------------------------------------------------------------------------------------------
        Label angleSliderLabel = new Label("Ray Angle Range");
        Label centerSliderLabel = new Label("Ray center");
        Label roomSizeLabel = new Label("Room Size");
        Label rayDensityLabel = new Label("Ray Density");
        Label mixSliderLabel = new Label("Mix");
        Label delaySliderLabel = new Label("Delay");
        Label surfaceRoughnessLabel = new Label("Surface Roughness");
        Label surfaceHighFreqAbsorbityLabel = new Label("Surface High Frequency Absorptivity");
        Label surfaceLowFreqAbsorbityLabel = new Label("Surface Low Frequency Absorptivity");
        
        mainUI.getChildren().add(angleSliderLabel);
        
        Slider angleSlider = new Slider();
        angleSlider.setMin(1);
        angleSlider.setMax(360);
        angleSlider.setValue(180);
        angleSlider.setShowTickLabels(true);
        angleSlider.setShowTickMarks(true);
        angleSlider.setMajorTickUnit(72);
        angleSlider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                wp.setMustUpdate(true);
            }   
        });
        mainUI.getChildren().add(angleSlider);
        
        mainUI.getChildren().add(centerSliderLabel);
        
        Slider centerSlider = new Slider();
        centerSlider.setMin(0);
        centerSlider.setMax(360);
        centerSlider.setValue(180);
        centerSlider.setShowTickLabels(true);
        centerSlider.setShowTickMarks(true);
        centerSlider.setMajorTickUnit(72);
        centerSlider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                wp.setMustUpdate(true);
            }   
        });
        mainUI.getChildren().add(centerSlider);
        
        mainUI.getChildren().add(roomSizeLabel);
        
        Slider sizeSlider = new Slider();
        sizeSlider.setMin(0.01);
        sizeSlider.setMax(1);
        sizeSlider.setValue(0.1);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setMajorTickUnit(1);
        sizeSlider.setMinorTickCount(10);
        sizeSlider.setBlockIncrement(0.1);
        sizeSlider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                wp.setMustUpdate(true);
            }   
        });
        mainUI.getChildren().add(sizeSlider);
        
        mainUI.getChildren().add(rayDensityLabel);
        
        final ProgressBar pb = new ProgressBar(0);
        wp.setProgressListener(new ProgressListener() {
            @Override
            public void update(double percentage) {
                pb.setProgress(percentage);
                if(percentage == 100){
                    pb.lookup(".bar").setStyle("-fx-background-color: green;");
                }else{
                    pb.lookup(".bar").setStyle("-fx-background-color: blue;");
                }
            }
        });
        
        Slider rayDensity = new Slider();
        rayDensity.setLabelFormatter(new StringConverter<Double> () {
            @Override
            public String toString(Double object) {
                return object.toString() + "%";
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string.substring(0, string.length()-1));
            }



        });
        rayDensity.setMin(0);
        rayDensity.setMax(100);
        rayDensity.setValue(15);
        rayDensity.setShowTickLabels(true);
        rayDensity.setShowTickMarks(true);
        rayDensity.setMajorTickUnit(10);
        rayDensity.setMinorTickCount(5);
        rayDensity.setBlockIncrement(10);
        rayDensity.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                wp.setMustUpdate(true);
            }   
        });
        mainUI.getChildren().add(rayDensity);
        
        mainUI.getChildren().add(mixSliderLabel);
        
        Slider mixSlider = new Slider();
        mixSlider.setMin(0);
        mixSlider.setMax(1);
        mixSlider.setValue(0.4);
        mixSlider.setShowTickLabels(true);
        mixSlider.setShowTickMarks(true);
        mixSlider.setMajorTickUnit(1);
        mixSlider.setMinorTickCount(10);
        mixSlider.setBlockIncrement(0.1);
        mainUI.getChildren().add(mixSlider);
        
        mainUI.getChildren().add(delaySliderLabel);
        
        Slider delaySlider = new Slider();
        delaySlider.setMin(-10000);
        delaySlider.setMax(10000);
        delaySlider.setValue(0);
        delaySlider.setShowTickLabels(true);
        delaySlider.setShowTickMarks(true);
        delaySlider.setMajorTickUnit(100);
        delaySlider.setMinorTickCount(50);
        mainUI.getChildren().add(delaySlider);
        
        mainUI.getChildren().add(surfaceRoughnessLabel);
        
        Slider roughnessSlider = new Slider();
        roughnessSlider.setMin(0);
        roughnessSlider.setMax(1);
        roughnessSlider.setValue(0);
        roughnessSlider.setShowTickLabels(true);
        roughnessSlider.setShowTickMarks(true);
        roughnessSlider.setMajorTickUnit(0.1);
        roughnessSlider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                realSpace.setSurfaceRoughness(roughnessSlider.getValue());
                wp.setMustUpdate(true);
            }   
        });
        
        mainUI.getChildren().add(roughnessSlider);
        
        mainUI.getChildren().add(surfaceHighFreqAbsorbityLabel);

        Slider highFreqAbsobSlider = new Slider();
        highFreqAbsobSlider.setMin(0);
        highFreqAbsobSlider.setMax(1);
        highFreqAbsobSlider.setValue(0);
        highFreqAbsobSlider.setShowTickLabels(true);
        highFreqAbsobSlider.setShowTickMarks(true);
        highFreqAbsobSlider.setMajorTickUnit(0.1);
        highFreqAbsobSlider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                wp.setHighFreqAbsorptivity(highFreqAbsobSlider.getValue());
                wp.setMustUpdate(true);
            }   
        });
        mainUI.getChildren().add(highFreqAbsobSlider);
        
        mainUI.getChildren().add(surfaceLowFreqAbsorbityLabel);

        Slider lowFreqAbsobSlider = new Slider();
        lowFreqAbsobSlider.setMin(0);
        lowFreqAbsobSlider.setMax(1);
        lowFreqAbsobSlider.setValue(0);
        lowFreqAbsobSlider.setShowTickLabels(true);
        lowFreqAbsobSlider.setShowTickMarks(true);
        lowFreqAbsobSlider.setMajorTickUnit(0.1);
        lowFreqAbsobSlider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                wp.setLowFreqAbsorptivity(lowFreqAbsobSlider.getValue());
                wp.setMustUpdate(true);
            }   
        });
        mainUI.getChildren().add(lowFreqAbsobSlider);
        
        mainUI.setMaxWidth(1000);
        
        //UI buttons-------------------------------------------------------------------------------------------------
        
        Button simulateRoomButton = new Button("Simulate Room");
        simulateRoomButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent e) {
            if(pb.getProgress() == 0 || pb.getProgress() == 100){
                realSpace.getPaths().clear();
                pa.reset();
                realSpace.reflect(20/((rayDensity.getValue()+0.02)*200),angleSlider.getValue(),centerSlider.getValue());
            }
        }
        });
        mainUI.getChildren().add(simulateRoomButton);
        
        Button importWavButton = new Button("Import WAV");
        importWavButton.getStyleClass().add("button");
        mainUI.getChildren().add(importWavButton);
        FileChooser fileChooser = new FileChooser();
        importWavButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent e) {
            if(pb.getProgress() == 0 || pb.getProgress() == 100){
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
                if(wp.getWavFile() != null){
                    importWavButton.setStyle("-fx-background-color: #008000; ");
                }
            }
        }
        });
        
        Button playSampleButton = new Button("Play Sample");
        mainUI.getChildren().add(playSampleButton);
        playSampleButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent e) {
            if(pb.getProgress() == 0 || pb.getProgress() == 100){
                if(wp.mustUpdate() || wp.wasConvolved()){
                    realSpace.getPaths().clear();
                    realSpace.reflect(20/((rayDensity.getValue()+0.02)*80),angleSlider.getValue(),centerSlider.getValue());
                    
                    if(wp.getWavFile() != null){

                        wp.setRoomSize(sizeSlider.getValue());
                        pb.setProgress(0.0001);
                        wp.setWasConvolved(false);
                        Thread t = new Thread(){
                            public void run() {
                                try {
                                    wp.applyReverb(realSpace.getPaths(),mixSlider.getValue(),(int)delaySlider.getValue(),1);
                                    if(realSpace.getStereoHearer() != null){
                                        wp.applyReverb(realSpace.getPaths(),mixSlider.getValue(),(int)delaySlider.getValue(),2);
                                    }
                                } catch (LineUnavailableException ex) {
                                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                wp.updateMix(mixSlider.getValue(),(int)delaySlider.getValue());
                                try {
                                    wp.playMix();
                                } catch (LineUnavailableException ex) {
                                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        };
                        t.setDaemon(true);
                        t.start();
                        wp.setMustUpdate(false);
                    }else{
                        importWavButton.setStyle("-fx-background-color: #d23939; ");
                    }
                    
                }else{
                    wp.updateMix(mixSlider.getValue(),(int)delaySlider.getValue());
                    try {
                        wp.playMix();
                    } catch (LineUnavailableException ex) {
                        Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            }
        }
        });
        
        Button playConvolvedSampleButton = new Button("Play Convolved Sample");
        mainUI.getChildren().add(playConvolvedSampleButton);
        playConvolvedSampleButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent e) {
            if(pb.getProgress() == 0 || pb.getProgress() == 100){
                if(wp.mustUpdate() || !wp.wasConvolved()){
                    realSpace.getPaths().clear();
                    realSpace.reflect(20/((rayDensity.getValue()+0.02)*80),angleSlider.getValue(),centerSlider.getValue());
                    
                    if(wp.getWavFile() != null){

                        wp.setRoomSize(sizeSlider.getValue());
                        pb.setProgress(0.0001);
                        wp.setWasConvolved(true);
                        Thread t = new Thread(){
                            public void run() {
                                try {
                                    wp.convolve(realSpace.getPaths(),mixSlider.getValue(),(int)delaySlider.getValue(),1);
                                    if(realSpace.getStereoHearer() != null){
                                        wp.convolve(realSpace.getPaths(),mixSlider.getValue(),(int)delaySlider.getValue(),2);
                                    }
                                   
                                } catch (UnsupportedAudioFileException ex) {
                                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (LineUnavailableException ex) {
                                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                wp.updateMix(mixSlider.getValue(),(int)delaySlider.getValue());
                                try {
                                    wp.playMix();
                                } catch (LineUnavailableException ex) {
                                    Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        };
                        t.setDaemon(true);
                        t.start();
                        wp.setMustUpdate(false);
                    }else{
                        importWavButton.setStyle("-fx-background-color: #d23939; ");
                    }
                    
                }else{
                    wp.updateMix(mixSlider.getValue(),(int)delaySlider.getValue());
                    try {
                        wp.playMix();
                    } catch (LineUnavailableException ex) {
                        Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            }
        }
        });
        

        Button exportWavButton = new Button("Export Wav");
        exportWavButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(pb.getProgress() == 0 || pb.getProgress() == 100){
                    realSpace.getPaths().clear();
                    realSpace.reflect(20/((rayDensity.getValue()+0.02)*80),angleSlider.getValue(),centerSlider.getValue());
                    
                    if(wp.getWavFile() != null){
                        fileChooser.setTitle("Save Reverb");
                        fileChooser.setInitialFileName(".wav");
                        final File file = fileChooser.showSaveDialog(primaryStage);
                        if(file != null){
                            wp.setRoomSize(sizeSlider.getValue());
                            pb.setProgress(0.0001);
                            Thread t = new Thread(){
                                public void run() {
                                    try {
                                        if(wp.mustUpdate()){
                                            wp.applyReverb(realSpace.getPaths(),mixSlider.getValue(),(int)delaySlider.getValue(),1);
                                            wp.applyReverb(realSpace.getPaths(),mixSlider.getValue(),(int)delaySlider.getValue(),2);
                                            wp.setMustUpdate(false);
                                        }
                                    } catch (LineUnavailableException ex) {
                                        Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    try {
                                        if(realSpace.getStereoHearer() != null){
                                            wp.stereoSave(file);
                                        }else{
                                            wp.save(file);

                                        }
                                    } catch (IOException ex) {
                                        Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (LineUnavailableException ex) {
                                        Logger.getLogger(RaySpace.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            };
                            t.setDaemon(true);
                            t.start();
                        }
                    }else{
                        importWavButton.setStyle("-fx-background-color: #d23939; ");
                    }
                }
            }
        });
        mainUI.getChildren().add(exportWavButton);
        mainUI.getChildren().add(pb);
        

        layout.setRight(mainUI);
        mainUI.setFillWidth(true);
        primaryStage.setWidth(windowWidth);
        primaryStage.setHeight(windowHeight);
        primaryStage.setTitle("RaySpace");
        
        spaceGC.setFill(Color.BLACK);//background colour of space editor.
        spaceGC.fillRect(0, 0, windowWidth, windowHeight);
        
        //PixelSpace pixelSpace = new PixelSpace();
        //Creation of physics objects via canvas------------------------------------------------------------------------
        Mouse m = new Mouse();
        
        spaceCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(t.getButton().equals(MouseButton.PRIMARY) && !m.isDragging()){
                    if(!t.isControlDown()){
                        realSpace.setHearer(realSpace.pixelXYToReal(new Point2D(t.getX(),t.getY())));
                    }else{
                        realSpace.setStereoHearer(realSpace.pixelXYToReal(new Point2D(t.getX(),t.getY())));
                    }
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
                    realSpace.addWall(realSpace.pixelXYToReal(m.getPressedXY()), realSpace.pixelXYToReal(m.getReleasedXY()),wallWidth * realScale );
                }
            }
        });
        
        //Draw physics objects onto canvas-------------------------------------------------------------------------
        World world = realSpace.getWorld();
        AnimationTimer at = new AnimationTimer() {
            @Override
            public void handle(long now) {
                spaceGC.setFill(Color.BLACK);
                spaceGC.fillRect(0, 0, spaceWidth, spaceHeight);
                double lineWidth = 5;
                world.step(0.02f, 8, 6);
                if(realSpace.getHearer() != null){
                    spaceGC.setLineWidth(lineWidth);
                    spaceGC.setFill(Color.GREY);
                    RealHearer hearer = realSpace.getHearer();
                    Point2D hearerPixelXY = realSpace.realXYToPixel(hearer.getXY());
                    spaceGC.setStroke(Color.WHITE);
                    spaceGC.fillOval(hearerPixelXY.getX()-25, hearerPixelXY.getY()-25, 50, 50);
                    spaceGC.strokeOval(hearerPixelXY.getX()-25, hearerPixelXY.getY()-25, 50, 50);
                }
                if(realSpace.getStereoHearer()!= null){
                    spaceGC.setLineWidth(lineWidth);
                    spaceGC.setFill(Color.GREY);
                    RealHearer hearer = realSpace.getStereoHearer();
                    Point2D hearerPixelXY = realSpace.realXYToPixel(hearer.getXY());
                    spaceGC.setStroke(Color.WHITE);
                    spaceGC.fillOval(hearerPixelXY.getX()-25, hearerPixelXY.getY()-25, 50, 50);
                    spaceGC.strokeOval(hearerPixelXY.getX()-25, hearerPixelXY.getY()-25, 50, 50);
                }

                if(!realSpace.getWalls().isEmpty()){
                    spaceGC.setStroke(Color.DARKBLUE);
                    spaceGC.setFill(Color.BLUE);
                    for(RealWall wall : realSpace.getWalls()){
                        spaceGC.setLineWidth(lineWidth);

                        spaceGC.save();
                        Point2D wallPixelXY = realSpace.realXYToPixel(wall.getCenter());
                        spaceGC.translate(wallPixelXY.getX(), wallPixelXY.getY());
                        spaceGC.rotate(wall.getRotation());
                        spaceGC.fillRect(-(wall.getDistance()/realScale)/2,-wallWidth/2,wall.getDistance()/realScale, wallWidth);
                        spaceGC.strokeRect((-(wall.getDistance()/realScale)/2)+ (lineWidth/2),(-wallWidth/2)+ (lineWidth/2),(wall.getDistance()/realScale)- (lineWidth), wallWidth- (lineWidth));
                        spaceGC.restore(); 
                    }
                }

                pa.step();
                lineWidth = 1;
                spaceGC.setLineWidth(lineWidth);

                for(ArrayList<PathNode> nodes : pa.nodesToDraw()){
                    Point2D start,end;
                    for(int i = 0; i < nodes.size()-1; i++){
                        start = realSpace.realXYToPixel(RealSpace.Vec2toPoint2D(nodes.get(i).getXy()));
                        end = realSpace.realXYToPixel(RealSpace.Vec2toPoint2D(nodes.get(i+1).getXy()));
                        spaceGC.setStroke(Color.GREEN);
                        spaceGC.strokeLine(start.getX() ,start.getY(), end.getX(), end.getY());
                    }
                }
               
                if(!realSpace.getSoundSources().isEmpty()){
                    for(RealSoundSource source : realSpace.getSoundSources()){
                        lineWidth = 5;
                        spaceGC.setLineWidth(lineWidth);
                        spaceGC.setFill(Color.LIGHTGREEN);
                        spaceGC.setStroke(Color.DARKGREEN);
                        Point2D sourcePixelXY = realSpace.realXYToPixel(source.getXY());
                        spaceGC.fillOval(sourcePixelXY.getX()-25,sourcePixelXY.getY()-25,50,50);
                        spaceGC.strokeOval(sourcePixelXY.getX()-25,sourcePixelXY.getY()-25,50,50);
                    }
                }
            }
        };
        at.start();
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}
