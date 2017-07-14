/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WavFileProcessor;

import WavFileProcessor.WavFile;
import java.io.File;
import App.Form;
import java.util.ArrayList;
import sun.nio.cs.StreamDecoder;



/**
 *
 * @author HP
 */
public class ReadWav {    
    double min;
    double max;
    int nilaiN = 0;
    private ArrayList<Double> nilaiSample = new ArrayList<Double>();
    public void bacaFile(String lokasiFile){
        try
        {
            //String lokasiFile = "E:\\Semester\\S8\\AUDIO\\1ac22khz.wav";
            //File fileLocation = new File(lokasiFile);
            
            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(new File(lokasiFile));

            // Display information about the wav file
            wavFile.display();

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer of 100 frames
            double[] buffer = new double[300 * numChannels];

            int framesRead;
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
            int count = 0;

            do
            {
                // Read frames into buffer
                //framesRead = wavFile.readFrames(buffer, 100);
                Form f = new Form();
                
                framesRead = wavFile.readFrames(buffer, 300);

                // Loop through frames and look for minimum and maximum value
                for (int j = 0; j < buffer.length; j++) {
                    double d = buffer[j];
                    
                }
                for (int s=0 ; s<framesRead * numChannels ; s++)
                {            
                    if (buffer[s] > max) max = buffer[s];
                    if (buffer[s] < min) min = buffer[s];
                    nilaiSample.add(buffer[s]);
//                    if (frameKe.get(1).size()==320) {
//                        System.out.println("Sudah");
//                    }
                    //count++;
                    
                    //System.out.println("S ke-"+s+" = "+buffer[s]);
                }
            }
            while (framesRead != 0);

            // Close the wavFile
            wavFile.close();

            // Output the minimum and maximum value
            System.out.printf("Min: %f, Max: %f\n", min, max);            
        }
        catch (Exception e)
        {
            System.err.println(e);
        }       
    }

    public ArrayList<Double> getNilaiSample() {
        return nilaiSample;
    }

    public ArrayList<Double> silenceRemoval(ArrayList<Double> withSilence,int frekuensiSampling){
    //public void silenceRemoval(ArrayList<Double> withSilence,int frekuensiSampling){
        //tampung array baru
        ArrayList<Double> nonSilence = new ArrayList<>();
        int count = 0;
        
        double frame_duration = 0.025;
        double samplePoint = frame_duration * frekuensiSampling;
        int N = withSilence.size();
        int num_frames = (int) (N/samplePoint);
        //ArrayList<Double> frame = new ArrayList();
        //int[][] frame = new int[num_frames][(int) samplePoint];
        double[][] frame = new double[num_frames][(int) samplePoint];
        double[] maxvalFrame = new double[num_frames];
        double max_val=0;
        
        int end = 0;
        for (int i = 0; i < num_frames; i++) {
//            System.out.println(i+" ======================="+num_frames);            
//            System.out.println(i*samplePoint+" "+(samplePoint*(i+1)-1));
                        
            //pecah kedalam beberapa frame
            for (int j = 0; j < samplePoint; j++) {
                frame[i][j] = withSilence.get((i*(int)samplePoint)+j);
                max_val = Math.max(max_val,withSilence.get((i*(int)samplePoint)+j));
                
               // System.out.println(i+" "+j+", maxval = "+max_val+"now = "+((i*samplePoint)+j));                
//              System.out.println(withSilence.get(end+j));
            }
            maxvalFrame[i] = max_val;
            max_val = 0; //kembalikan ke awal
            

            end = i * (int) samplePoint;
        }
        
        System.out.println("===========");
        for (int i = 0; i < num_frames; i++) {            
            if (maxvalFrame[i] > 0.03) {
                //nonSilence.get(end);
                for (int j = 0; j < samplePoint; j++) {
                    nonSilence.add(frame[i][j]);
                    //System.out.println(frame[i][j]);
                    //nonSilence.add((i*(int)samplePoint)+i, frame[i][]);
                }
            }            
        }
        return nonSilence;
    }
    
}
