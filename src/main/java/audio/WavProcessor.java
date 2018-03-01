/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
 * Class for translating wav files into byte arrays, int arrays and applying ray information to wavs to create the reverb effect.
 */
public class WavProcessor {
    private File wavFile;
    private AudioFormat format;
    private double roomSize;
    ArrayList<Byte> wavInBytes, reverbInBytes, mixInBytes;
    ArrayList<Integer>wavInInt, reverbInInt, mixInInt;
    ProgressListener listener;
    private boolean mustUpdate;
    
    public WavProcessor(){
        wavInBytes = new ArrayList<>();
        reverbInBytes = new ArrayList<>();
        mixInBytes = new ArrayList<>();
        wavInInt = new ArrayList<>();
        reverbInInt = new ArrayList<>();
        mixInInt = new ArrayList<>();
        mustUpdate = true;
        roomSize = 0.1;
        listener = null;
    }
    /**
     * 
     * @param byteWav Wav file in the form of a Byte ArrayList.
     * @return Wav file in the form of an Integer ArrayList.
     * Currently only works for mono, 16bit little endian.
     */
    public ArrayList<Integer> byteWavToInt(ArrayList<Byte> byteWav){
        ArrayList<Integer> intWav = new ArrayList<>();
        byte b1,b2;
        b1 = 0;
        b2 = 0;
        for(int i = 0; i < byteWav.size(); i++){
            if(i % 2 == 0){
                b1 = byteWav.get(i);
            }else{
                b2 = byteWav.get(i);
                intWav.add((int)(((b2 & 0xFF) << 8) + (b1 & 0xFF))<< 16 >> 16);
            }
        }
        return intWav;
    }
    /**
     * 
     * @param intWav Wav file in the form of a Integer ArrayList.
     * @return Wav file in the form of an Byte ArrayList.
     * only works for mono, 16bit little endian.
     */
    public ArrayList<Byte> intWavToByte(ArrayList<Integer> intWav){
        ArrayList<Byte> byteWav = new ArrayList<>();
        for(int sample : intWav){
            byteWav.add((byte)(sample & 0xFF));
            byteWav.add((byte) ((sample >> 8) & 0xFF ));
        }
        return byteWav;
    }
    
    public void setMustUpdate(boolean mustUpdate){
        this.mustUpdate = mustUpdate;
    }
    
    public boolean mustUpdate(){
        return mustUpdate;
    }
    
    public void setRoomSize(double roomSize){
        this.roomSize = roomSize;
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
    
    public void updateMix(double mix, int delay){
        if(reverbInInt.size() != 0){
            ArrayList<Integer> reverbInInt, wavInInt;
            reverbInInt = (ArrayList<Integer>)this.reverbInInt.clone();
            wavInInt = (ArrayList<Integer>)this.wavInInt.clone();
            ArrayList<Byte> wavInBytes;
            wavInBytes = (ArrayList<Byte>)this.wavInBytes.clone();

            int largest = 0;
            for(int sample : reverbInInt){
                if(Math.abs(sample) > largest){
                    largest = Math.abs(sample);
                }
            }
            double multiplier = ((double)Short.MAX_VALUE/(double)largest);
            int firstNonZeroIndex = 0;
            for(int i = 0; i < reverbInInt.size(); i++){
                reverbInInt.set(i, (int)((reverbInInt.get(i)*multiplier)*mix));
                if(reverbInInt.get(i) != 0 && firstNonZeroIndex == 0){
                    firstNonZeroIndex = i;
                }
            }
            ArrayList<Integer> reverbInIntClone = (ArrayList<Integer>)reverbInInt.clone();
            delay = 0;
            for(int i = -delay; i < reverbInInt.size(); i++){
                if(i < 0){
                    reverbInInt.set(i+delay,0);
                }else
                if(i + firstNonZeroIndex < reverbInIntClone.size() && i+delay<reverbInInt.size()){
                    reverbInInt.set(i+delay,reverbInIntClone.get(i + firstNonZeroIndex));
                }else if(i+delay>reverbInInt.size() && i + firstNonZeroIndex < reverbInIntClone.size()){
                    reverbInInt.add(reverbInIntClone.get(i + firstNonZeroIndex));
                }else
                {
                    reverbInInt.set(i,0);
                }
            }
            
            mixInInt = (ArrayList<Integer>)reverbInInt.clone();
            for(int i = 0; i < wavInInt.size(); i++){
                mixInInt.set(i,reverbInInt.get(i)+(int)(wavInInt.get(i)*(1-mix))); 
            }
            mixInBytes = intWavToByte(mixInInt);

        }
    }
    
    public void playMix() throws LineUnavailableException{
        if(mixInBytes.size() != 0){
            byte[] mixArray = new byte[mixInBytes.size()];
            for(int i = 0; i < mixInBytes.size(); i++){
                mixArray[i] = mixInBytes.get(i);
            }
            Clip clip = AudioSystem.getClip();
            clip.open(format, mixArray,0,mixArray.length);
            clip.start();
            
        }
        listener.update(100);
    }
    
    public void save(File saveFile) throws FileNotFoundException, IOException, LineUnavailableException{
        if(saveFile != null){
            byte[] output = new byte[mixInBytes.size()];
            for(int i = 0; i < mixInBytes.size(); i++){
                output[i] = mixInBytes.get(i);
            }
            InputStream b_in = new ByteArrayInputStream(output);
            AudioInputStream stream = new AudioInputStream(b_in, format,
                    output.length);
            AudioSystem.write(stream,Type.WAVE, saveFile);
        }
        playMix(); 
    }
    
    public void setProgressListener(ProgressListener listener){
        this.listener = listener;
    }

    public void applyReverb(ArrayList<ArrayList<PathNode>> rayData, double mix, int delay) throws LineUnavailableException{
        this.wavInInt = byteWavToInt(wavInBytes);
        ArrayList<RayData> rayDatas = new ArrayList<>();
        for(ArrayList<PathNode> path : rayData){
            if(path.get(path.size()-1).getFixture()!= null && path.get(path.size()-1).getFixture().isSensor() && path.size() > 1){
                rayDatas.add(new RayData(path));
            }
        }
        if(rayDatas.size() > 0){
            //may translate into function------------------------------------------------------------------
            ArrayList<Integer> reverbInInt = new ArrayList<>();
            int progress = 1;
            double percentage;
            for(RayData data : rayDatas){
                int byteDelay = ((int)(format.getFrameRate()*data.delay )* (format.getFrameSize()));
                int intDelay = byteDelay/(format.getFrameSize());
                /*
                while(reverbInInt.size() < intDelay){
                    reverbInInt.add(0);
                }
    */
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
                percentage = (double)progress/(double)rayDatas.size();
                if(percentage >= 100){
                    percentage = 99.9;
                }
                listener.update(percentage);
                progress++;
            }


            int largest = 0;
            for(int sample : reverbInInt){
                if(Math.abs(sample) > largest){
                    largest = Math.abs(sample);
                }
            }
            double multiplier = ((double)Short.MAX_VALUE/(double)largest);
            int firstNonZeroIndex = 0;
            for(int i = 0; i < reverbInInt.size(); i++){
                reverbInInt.set(i, (int)((reverbInInt.get(i)*multiplier)*mix));
                if(reverbInInt.get(i) != 0 && firstNonZeroIndex == 0){
                    firstNonZeroIndex = i;
                }
            }
            ArrayList<Integer> reverbInIntClone = (ArrayList<Integer>)reverbInInt.clone();
            delay = 0;
            for(int i = -delay; i < reverbInInt.size(); i++){
                if(i < 0){
                    reverbInInt.set(i+delay,0);
                }else
                if(i + firstNonZeroIndex < reverbInIntClone.size() && i+delay<reverbInInt.size()){
                    reverbInInt.set(i+delay,reverbInIntClone.get(i + firstNonZeroIndex));
                }else if(i+delay>reverbInInt.size() && i + firstNonZeroIndex < reverbInIntClone.size()){
                    reverbInInt.add(reverbInIntClone.get(i + firstNonZeroIndex));
                }else
                {
                    reverbInInt.set(i,0);
                }
            }
            this.reverbInInt = reverbInInt;
            mixInInt = (ArrayList<Integer>)reverbInInt.clone();
            for(int i = 0; i < wavInInt.size(); i++){
                mixInInt.set(i,reverbInInt.get(i)+(int)(wavInInt.get(i)*(1-mix))); 
            }
            mixInBytes = intWavToByte(mixInInt);
        }else{
            mixInInt = new ArrayList<>();
            for(int i = 0; i < wavInInt.size(); i++){
                mixInInt.add((int)(wavInInt.get(i)*(1-mix))); 
            }
            mixInBytes = intWavToByte(mixInInt);
        }
        System.out.println("FIN");
    }
    
    private class RayData{
        boolean invert;
        double gain;
        double delay;
        public RayData(ArrayList<PathNode> path){
            gain = 1;
            double gainMultiplier = 0.9;
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
            totalLength *= roomSize;
            delay = totalLength/343;
        }
    }
    
    public File getWavFile(){
        return wavFile;
    }
    
}
