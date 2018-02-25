/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import realspace.PathNode;
import realspace.RealSpace;

/**
 *
 * @author samuel
 */
public class WavProcessor {
    private File wavFile;
    private AudioFormat format;
    ArrayList<Byte> wavInBytes;
    
    public WavProcessor(){
        wavInBytes = new ArrayList<>();
    }
    
    public void setFile(File wavFile) throws UnsupportedAudioFileException, IOException{
        this.wavFile = wavFile;
        AudioInputStream is = AudioSystem.getAudioInputStream(wavFile);
        format = is.getFormat();
        if(!format.isBigEndian() && format.getChannels() == 1 && format.getSampleSizeInBits() == 16){
            wavInBytes = new ArrayList<>();
            byte[] buffer = new byte[format.getFrameSize()];
            for(int i = 0; i < format.getFrameSize(); i++){
                wavInBytes.add(buffer[i]);
            }
            
            int b = is.read(buffer);
            while(b != -1){
                b = is.read(buffer);
                for(int i = 0; i < format.getFrameSize(); i++){
                    wavInBytes.add(buffer[i]);
                }
            }
        }else{
            System.out.println("format not yet supported");
        }
    }
    
    public WavProcessor(File wavFile) throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        this.wavFile = wavFile;
        AudioInputStream is = AudioSystem.getAudioInputStream(wavFile);
        format = is.getFormat();
        if(!format.isBigEndian() && format.getChannels() == 1 && format.getSampleSizeInBits() == 16){
            wavInBytes = new ArrayList<>();
            byte[] buffer = new byte[format.getFrameSize()];
            for(int i = 0; i < format.getFrameSize(); i++){
                wavInBytes.add(buffer[i]);
            }
            
            int b = is.read(buffer);
            while(b != -1){
                b = is.read(buffer);
                for(int i = 0; i < format.getFrameSize(); i++){
                    wavInBytes.add(buffer[i]);
                }
            }
        }else{
            System.out.println("format not yet supported");
        }
        
    }
    
    public void save() throws FileNotFoundException, IOException{
        byte[] output = new byte[wavInBytes.size()];
        for(int i = 0; i < wavInBytes.size(); i++){
            output[i] = wavInBytes.get(i);
        }
        InputStream b_in = new ByteArrayInputStream(output);
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(
                "C:\\filename.bin"));
        dos.write(output);
        AudioInputStream stream = new AudioInputStream(b_in, format,
                output.length);
        File file = new File("file.wav");
        AudioSystem.write(stream, Type.WAVE, file);

    }
    
    public void delay(){
        int size = wavInBytes.size();
        int numberOfFrames = size/format.getFrameSize();
        
        
        for(int i = (numberOfFrames/2)*format.getFrameSize(); i < size + (numberOfFrames/2)*format.getFrameSize(); i++){
            if( i < size){
                wavInBytes.set(i, (byte)(wavInBytes.get(i) + wavInBytes.get(i-(numberOfFrames/2)*format.getFrameSize())));
            }else{
                wavInBytes.add(wavInBytes.get(i-(numberOfFrames/2)*format.getFrameSize()));
            }
        }
    }
    
    public void applyReverb(ArrayList<ArrayList<PathNode>> rayData){
        ArrayList<RayData> rayDatas = new ArrayList<>();
        for(ArrayList<PathNode> path : rayData){
            rayDatas.add(new RayData(path));
        }
        
        
    }
    
    private class RayData{
        boolean invert;
        double gain;
        double delay;
        public RayData(ArrayList<PathNode> path){
            gain = 0.75;
            invert = false;
            int i = 1;
            double totalLength = 0;
            for(PathNode node : path){
                invert = !invert;
                gain *= gain;
                
                if(i < path.size()){
                    totalLength += RealSpace.Vec2toPoint2D(node.getXy()).distance(RealSpace.Vec2toPoint2D(path.get(i).getXy()));
                }
                
                i++;
            }
            delay = totalLength/343;
        }
    }
    
}
