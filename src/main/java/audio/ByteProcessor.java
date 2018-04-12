/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import java.util.ArrayList;

/**
 *
 * @author samuel
 */
public class ByteProcessor {
    public static ArrayList<Byte> intWavToByte(ArrayList<Integer> intWav){
        ArrayList<Byte> byteWav = new ArrayList<>();
        for(int sample : intWav){
            byteWav.add((byte)(sample & 0xFF));
            byteWav.add((byte) ((sample >> 8) & 0xFF ));
        }
        return byteWav;
    }
    public static ArrayList<Integer> byteWavToInt(ArrayList<Byte> byteWav){
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
}
