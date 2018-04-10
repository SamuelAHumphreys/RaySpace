/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import uk.me.berndporr.iirj.Butterworth;

/**
 *
 * @author samuel
 */
public class Filters {
    public static ArrayList<Integer> highPass(ArrayList<Integer> pathReverb, float freq, AudioFormat format){
        Butterworth b = new Butterworth();
        ArrayList<Integer> clone = new ArrayList<>();

        b.highPass(4, format.getSampleRate(), freq);

        double mul = Short.MAX_VALUE/Float.MAX_VALUE;
        for (int i = 0; i < pathReverb.size(); i++){
            clone.add(pathReverb.get(i));
            pathReverb.set(i, (int)((double)b.filter((double)pathReverb.get(i)*mul)/mul));
        }
        return pathReverb;

    }
    
    public static ArrayList<Integer> lowPass(ArrayList<Integer> pathReverb, float freq, AudioFormat format){
        Butterworth b = new Butterworth();
        ArrayList<Integer> clone = new ArrayList<>();

        b.lowPass(4, format.getSampleRate(), freq);

        double mul = Short.MAX_VALUE/Float.MAX_VALUE;
        for (int i = 0; i < pathReverb.size(); i++){
            clone.add(pathReverb.get(i));
            clone.set(i, (int)((double)b.filter((double)clone.get(i)*mul)/mul));
        }
        return clone;
    }
}
