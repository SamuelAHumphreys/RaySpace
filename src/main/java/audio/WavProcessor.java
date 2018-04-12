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
import javax.sound.sampled.DataLine;
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
    private double roomSize, highFreqAbsorptivity,lowFreqAbsorptivity;
    private ArrayList<Byte> wavInBytes, reverbInBytes, mixInBytes,mixChannel2InBytes;
    private ArrayList<Integer>wavInInt, reverbInInt, mixInInt,mixChannel2InInt;
    private ProgressListener listener;
    private boolean mustUpdate,wasConvolved;
    private Clip clip;
    
    public WavProcessor() throws LineUnavailableException{
        wasConvolved = false;
        highFreqAbsorptivity = 0;
        lowFreqAbsorptivity = 0;
        wavInBytes = new ArrayList<>();
        reverbInBytes = new ArrayList<>();
        mixInBytes = new ArrayList<>();
        wavInInt = new ArrayList<>();
        reverbInInt = new ArrayList<>();
        mixInInt = new ArrayList<>();
        mixChannel2InBytes = new ArrayList<>();
        mixChannel2InInt = new ArrayList<>();
        mustUpdate = true;
        roomSize = 0.1;
        listener = null;
        clip = AudioSystem.getClip();
    }
    
    public boolean wasConvolved(){
        return wasConvolved;
    }
    
    public void setWasConvolved(boolean wasConvolved){
        this.wasConvolved = wasConvolved;
    }
    
    public void setHighFreqAbsorptivity(double highFreqAbsorptivity){
        this.highFreqAbsorptivity = highFreqAbsorptivity;
    }
    /**
     * 
     * @param byteWav Wav file in the form of a Byte ArrayList.
     * @return Wav file in the form of an Integer ArrayList.
     * Currently only works for mono, 16bit little endian.
     */
    /**
     * 
     * @param intWav Wav file in the form of a Integer ArrayList.
     * @return Wav file in the form of an Byte ArrayList.
     * only works for mono, 16bit little endian.
     */
    public void setMustUpdate(boolean mustUpdate){
        this.mustUpdate = mustUpdate;
    }
    
    public boolean mustUpdate(){
        return mustUpdate;
    }
    
    public void setRoomSize(double roomSize){
        this.roomSize = roomSize;
    }
    /**
     * Load Wav from file into a byte array. Currently only mono, 16bit, little endian files are supported.
     * @param wavFile
     * @throws UnsupportedAudioFileException
     * @throws IOException 
     */
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
    /**
     * Translates the produced reverb along with the original wav into a single mix.
     * @param mix The amplitude ratio of original file : reverb (dry/wet)
     * @param delay The length in samples of how soon or late to play the reverb relative to the dry sound.
     */
    public void updateMix(double mix, int delay){
        if(!reverbInInt.isEmpty()){
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
            mixInBytes = ByteProcessor.intWavToByte(mixInInt);
        }
    }
    
    public void playMix() throws LineUnavailableException{
        int max = Integer.MIN_VALUE;
        for(int i : mixInInt){
            if(Math.abs(i) > max){
                max = (Math.abs(i));
            }
        }
        if(max != 0 && max!=Short.MAX_VALUE){
            for(int i = 0; i < mixInInt.size(); i++){
                mixInInt.set(i,(int)(mixInInt.get(i)*(double)((double)Short.MAX_VALUE/(double)max)));
            }
        }
        System.out.println(mixInInt.size());
        mixInBytes = ByteProcessor.intWavToByte(mixInInt);
        if(mixInBytes.size() != 0){
            byte[] mixArray = new byte[mixInBytes.size()];
            for(int i = 0; i < mixInBytes.size(); i++){
                mixArray[i] = mixInBytes.get(i); 
            }
            System.out.println(mixInBytes.size());
            if(clip.isOpen()){
                clip.close();
            }
            AudioFormat f = new AudioFormat(format.getEncoding(),format.getSampleRate(), format.getSampleSizeInBits(),format.getChannels(),format.getFrameSize(),format.getFrameRate(),format.isBigEndian());

            if(this.mixChannel2InBytes.size() > 0){
                ArrayList<Integer> stereoMix = new ArrayList<>();
                int maxSize = mixChannel2InInt.size();
                if(maxSize > mixInInt.size()){
                    maxSize = mixInInt.size();
                }
                for(int i = 0; i < maxSize; i++){
                    if(i < mixChannel2InInt.size()){
                        stereoMix.add(mixChannel2InInt.get(i));
                    }else{
                        stereoMix.add(0);
                    }
                    if(i < mixInInt.size()){
                        stereoMix.add(mixInInt.get(i));
                    }else{
                        stereoMix.add(0);
                    }
                }
                ArrayList<Byte> stereoMixInBytes = ByteProcessor.intWavToByte(stereoMix);
                mixArray = new byte[stereoMixInBytes.size()];
                for(int i = 0; i < stereoMixInBytes.size(); i++){
                    mixArray[i] = stereoMixInBytes.get(i);
                }
                f = new AudioFormat(format.getEncoding(),format.getSampleRate(), format.getSampleSizeInBits(),2,format.getFrameSize()*2,format.getFrameRate(),format.isBigEndian());
            }
            DataLine.Info info = new DataLine.Info(Clip.class, f);
            clip = (Clip)AudioSystem.getLine(info);
            clip.open(f, mixArray,0,mixArray.length-1);
            System.out.println(clip.getFormat());
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
        this.listener.update(100);
    }
    
    public void stereoSave(File saveFile) throws FileNotFoundException, IOException, LineUnavailableException{
        if(saveFile != null){
            int largest = mixInBytes.size();
            if(largest < mixChannel2InBytes.size()){
                largest = mixChannel2InBytes.size();
            }
            ArrayList<Integer> stereoMix = new ArrayList<>();
            int maxSize = mixChannel2InInt.size();
            if(maxSize > mixInInt.size()){
                maxSize = mixInInt.size();
            }
            for(int i = 0; i < maxSize; i++){
                if(i < mixChannel2InInt.size()){
                    stereoMix.add(mixChannel2InInt.get(i));
                }else{
                    stereoMix.add(0);
                }
                if(i < mixInInt.size()){
                    stereoMix.add(mixInInt.get(i));
                }else{
                    stereoMix.add(0);
                }
            }
            ArrayList<Byte> stereoMixInBytes = ByteProcessor.intWavToByte(stereoMix);
            byte[] output = new byte[stereoMixInBytes.size()];
            for(int i = 0; i < stereoMixInBytes.size(); i++){
                output[i] = stereoMixInBytes.get(i);
            }
            InputStream b_in = new ByteArrayInputStream(output);
            b_in = new ByteArrayInputStream(output);
            AudioFormat f = new AudioFormat(format.getEncoding(),format.getSampleRate(), format.getSampleSizeInBits(),2,format.getFrameSize()*2,format.getFrameRate(),format.isBigEndian());
            AudioInputStream stream = new AudioInputStream(b_in, f,
                    output.length);
            stream = new AudioInputStream(b_in, f,
                    output.length);
            AudioSystem.write(stream,Type.WAVE, saveFile);
        }
        this.listener.update(100);

    }
    
    public void setProgressListener(ProgressListener listener){
        this.listener = listener;
    }

    public void applyReverb(ArrayList<ArrayList<PathNode>> rayData, double mix, int delay,int channel) throws LineUnavailableException{
        
        this.wavInInt = ByteProcessor.byteWavToInt(wavInBytes);
        ArrayList<RayData> rayDatas = new ArrayList<>();
        for(ArrayList<PathNode> path : rayData){
            if(path.get(path.size()-1).getFixture()!= null && path.get(path.size()-1).getFixture().isSensor() && path.size() > 1 && path.get(path.size()-1).getStereoChannel() == channel){
                rayDatas.add(new RayData(path));
            }
        }
        if(rayDatas.size() > 0){
            //may translate into function------------------------------------------------------------------
            ArrayList<Integer> reverbInInt = new ArrayList<>();
            int progress = 1;
            double percentage;
            ArrayList<Integer> pathReverb;
            for(RayData data : rayDatas){
                pathReverb = new ArrayList<>();
                int byteDelay = ((int)(format.getFrameRate()*data.delay )* (format.getFrameSize()));
                int intDelay = byteDelay/(format.getFrameSize());
                double gain = data.gain;
                if(data.invert){
                    gain = -data.gain;
                }
                float freq = (float)(20000 - ((highFreqAbsorptivity*12000) * ((float)data.numberOfReflections)));
                if(freq < 0){
                    freq = 0;
                }
                if(freq > 20000){
                    freq = 20000;
                }
                ArrayList<Integer> lowpass =(ArrayList<Integer>)wavInInt.clone();

                if(freq != 20000){
                   lowpass =maximise(Filters.lowPass(wavInInt, freq,format));
                }
                freq = (float)Math.abs(20000-(20000 - ((lowFreqAbsorptivity*50) * ((float)data.numberOfReflections))));
                if(freq > 20000){
                    freq = 20000;
                }
                if(freq != 0){
                   lowpass = maximise(Filters.highPass(lowpass,freq,format));
                }
                for(int i = 0; i < lowpass.size(); i++){
                    pathReverb.add((int)((double)lowpass.get(i) * gain));
                }
                for(int i = 0; i < pathReverb.size(); i++){
                    if(i + intDelay < reverbInInt.size()){
                        reverbInInt.set(i + intDelay, reverbInInt.get(i + intDelay) + pathReverb.get(i));
                    }else{
                        reverbInInt.add((int)(pathReverb.get(i)));
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
            if(channel == 1){
                mixInInt = (ArrayList<Integer>)reverbInInt.clone();
                for(int i = 0; i < wavInInt.size(); i++){
                    mixInInt.set(i,reverbInInt.get(i)+(int)(wavInInt.get(i)*(1-mix))); 
                }
                mixInBytes = ByteProcessor.intWavToByte(mixInInt);
            }else{
                mixChannel2InInt = (ArrayList<Integer>)reverbInInt.clone();
                for(int i = 0; i < wavInInt.size(); i++){
                    mixChannel2InInt.set(i,reverbInInt.get(i)+(int)(wavInInt.get(i)*(1-mix))); 
                }
                mixChannel2InBytes = ByteProcessor.intWavToByte(mixChannel2InInt);
            }
        }else{
            mixInInt = new ArrayList<>();
            for(int i = 0; i < wavInInt.size(); i++){
                mixInInt.add((int)(wavInInt.get(i)*(1-mix))); 
            }
            mixInBytes = ByteProcessor.intWavToByte(mixInInt);
        }
        System.out.println("FIN");
    }

    public void setLowFreqAbsorptivity(double lowFreqAbsorptivity) {
        this.lowFreqAbsorptivity = lowFreqAbsorptivity;
    }
    
    private ArrayList<Integer> maximise(ArrayList<Integer> array){
        int max = 0;
        ArrayList<Integer> clone = new ArrayList<>();
        for(int i : array){
            clone.add(i);
            if(Math.abs(i) > max){
                max = Math.abs(i);
            }
        }
        double mul = 0;
        if(max!=0){
            mul = (double)(Short.MAX_VALUE-10)/max;
        }
        for(int i = 0;i < clone.size();i++){
            clone.set(i, (int)((double)clone.get(i)*mul));
        }
        return clone;
    }
    
   
    public File getWavFile(){
        return wavFile;
    }
    
    public void convolve(ArrayList<ArrayList<PathNode>> rayData,double mix, int delay,int channel) throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        this.wavInInt = ByteProcessor.byteWavToInt(wavInBytes);
        ArrayList<Integer> tmpWavInInt = new ArrayList<>();
        for(int i : wavInInt){
            tmpWavInInt.add(i);
        }
        reverbInInt = new ArrayList<>();
        ArrayList<Integer> impulseResponse = new ArrayList<>();
        ArrayList<Integer> whiteNoise = new ArrayList<>();
        ArrayList<Byte> byteWhiteNoise = new ArrayList<>();
        File file = new File("src/main/java/resources/shortSineSweep.wav");
        AudioInputStream is = AudioSystem.getAudioInputStream(file);
        byte[] buffer = new byte[format.getFrameSize()];
        for(int i = 0; i < format.getFrameSize(); i++){
            byteWhiteNoise.add(buffer[i]);
        }
        int b = is.read(buffer);
        while(b != -1){
            b = is.read(buffer);
            for(int i = 0; i < format.getFrameSize(); i++){
                byteWhiteNoise.add(buffer[i]);
            }
        }
        whiteNoise = ByteProcessor.byteWavToInt(byteWhiteNoise);
        wavInBytes = byteWhiteNoise;
        wavInInt = whiteNoise;
        this.applyReverb(rayData, 1, 0,channel);
        impulseResponse = (ArrayList<Integer>)reverbInInt.clone();
        wavInInt = tmpWavInInt;
        for(int i = 0; i < wavInInt.size(); i++){
            while(reverbInInt.size() < wavInInt.size() + impulseResponse.size()){
                reverbInInt.add(0);
            }
        }
        int largest = wavInInt.size();
        if(largest < impulseResponse.size()){
            largest = impulseResponse.size();
        }
        largest =  wavInInt.size()+impulseResponse.size();
        double m=1;
        m /= impulseResponse.size();
        double[] x = new double[largest];
        double[] y = new double[largest];
        double[] z = new double[largest];
        double[] a = new double[largest];
        double[] c = new double[largest];
        for(int i = 0; i < largest; i++){
            if(i < whiteNoise.size()){
                a[i] = whiteNoise.get(whiteNoise.size() -1 - i)*m;
            }else{
                a[i] = 0;
            }
        }
        for(int i = 0; i < largest; i++){
            c[i] = 0;
        }
        for(int i = 0; i < largest; i++){
            if(i < wavInInt.size()){
                x[i] = wavInInt.get(i)*m;
            }else{
                x[i] = 0;
            }
        }
        for(int i = 0; i < largest; i++){
            if(i < impulseResponse.size()){
                y[i] = impulseResponse.get(i)*m;
            }else{
                y[i] = 0;
            }  
        }
        for(int i = 0; i < largest; i++){
            z[i] = 0;
        }
        Fft.convolve(a, y, z);
        mixInInt = new ArrayList<>();
        for(int i = 0 ; i < largest; i++){
            mixInInt.add((int)z[i]);
        }
        mixInBytes = ByteProcessor.intWavToByte(mixInInt);
        Fft.convolve(x, z, c);
        for(int i = 0; i < largest; i++){
            if(i < reverbInInt.size()){
                reverbInInt.set(i, (int)(c[i]/m));
            }else{
                reverbInInt.add((int)(c[i]/m));
            }
        }
        largest = 0;
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
        wavInInt = tmpWavInInt;
        wavInBytes = ByteProcessor.intWavToByte(wavInInt);
        if(channel == 0){
            mixInInt = (ArrayList<Integer>)reverbInInt.clone();
            for(int i = 0; i < wavInInt.size(); i++){
                mixInInt.set(i,reverbInInt.get(i)+(int)(wavInInt.get(i)*(1-mix))); 
            }
        }else{
            this.mixChannel2InInt = (ArrayList<Integer>)reverbInInt.clone();
            for(int i = 0; i < wavInInt.size(); i++){
                this.mixChannel2InInt.set(i,reverbInInt.get(i)+(int)(wavInInt.get(i)*(1-mix))); 
            }
        }
        System.out.println("FIN2");
    }
    
    private class RayData{
        boolean invert;
        double gain;
        double delay;
        int numberOfReflections;
        public RayData(ArrayList<PathNode> path){
            numberOfReflections = path.size()-1;
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
    
}
