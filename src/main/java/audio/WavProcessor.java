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
        AudioInputStream stream = new AudioInputStream(b_in, format,
                output.length);
        File file = new File("file.wav");
        AudioSystem.write(stream,Type.WAVE, file);

    }
    /*
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
    */
    public void applyReverb(ArrayList<ArrayList<PathNode>> rayData){
        ArrayList<RayData> rayDatas = new ArrayList<>();
        for(ArrayList<PathNode> path : rayData){
            rayDatas.add(new RayData(path));
        }
        ArrayList<Integer> wavInInt = new ArrayList<>();
        byte b1,b2;
        b1 = 0;
        b2 = 0;
        System.out.println(format);
        for(int i = 0; i < wavInBytes.size(); i++){
            if(i % 2 == 0){
                b1 = wavInBytes.get(i);
            }else{
                b2 = wavInBytes.get(i);
                wavInInt.add((int)(((b2 & 0xFF) << 8) + (b1 & 0xFF))<< 16 >> 16);
            }
        }
        ArrayList<Integer> reverbInInt = new ArrayList<>();
        for(RayData data : rayDatas){
            int byteDelay = ((int)(format.getFrameRate()*data.delay )* (format.getFrameSize()));
            int intDelay = byteDelay/(format.getFrameSize());
            while(reverbInInt.size() < intDelay){
                reverbInInt.add(0);
            }
            

            double gain = data.gain;
            if(data.invert){
                gain = -data.gain;
            }
            for(int i = 0; i < wavInInt.size(); i++){
                if(i + intDelay < reverbInInt.size()){
                    reverbInInt.set(i + intDelay, reverbInInt.get(i + intDelay) + (int)(wavInInt.get(i) * gain));
                }else{
                    reverbInInt.add((int)(wavInInt.get(i) * gain));
                }
                
            }
        }
        int largest = 0;
        for(int sample : reverbInInt){
            if(Math.abs(sample) > largest){
                largest = Math.abs(sample);
            }
        }
        double multiplier = ((double)Short.MAX_VALUE/(double)largest);
        for(int i = 0; i < reverbInInt.size(); i++){
            reverbInInt.set(i, (int)(reverbInInt.get(i)*multiplier));
        }
        
        ArrayList<Byte> reverbInBytes = new ArrayList<>();
        for(int sample : reverbInInt){
            reverbInBytes.add((byte)(sample & 0xFF));
            reverbInBytes.add((byte) ((sample >> 8) & 0xFF ));
            
            
        }
        wavInBytes = reverbInBytes;
        System.out.println("FIN");
    }
    
    private class RayData{
        boolean invert;
        double gain;
        double delay;
        public RayData(ArrayList<PathNode> path){
            gain = 1;
            double gainMultiplier = 0.9;
            double size = 0.2;
            invert = false;
            int i = 1;
            double totalLength = 0;
            for(PathNode node : path){
                invert = !invert;
                gain *= gainMultiplier;
                
                if(i < path.size()){
                    totalLength += RealSpace.Vec2toPoint2D(node.getXy()).distance(RealSpace.Vec2toPoint2D(path.get(i).getXy()));
                }
                
                i++;
            }
            totalLength *= size;
            delay = totalLength/343;
        }
    }
    
}
