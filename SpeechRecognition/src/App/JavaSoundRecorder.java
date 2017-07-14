/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package App;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 *
 * @author HP
 */
public class JavaSoundRecorder {
    String filePath;
       
    public void setFilePath(String fp){
        filePath = fp;        
    }
   
    // record duration, in milliseconds
    static final long RECORD_TIME = 3000;  // 1 minute = 60000
 
    // path of the wav file
    //File wavFile = new File("E:\\Semester\\TESTING\\RecordAudio.wav");    
    
    
    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
 
    // the line from which audio data is captured
    TargetDataLine line;
 
    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }
 
    /**
     * Captures the sound and record into a WAV file
     */
    void start() {
        try {
            // path of the wav file
            //File wavFile = new File("E:\\Semester\\TESTING\\RecordAudio.wav"); 
            File wavFile = new File(filePath); 
            
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
 
            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing
            //buat objek frm
            Form frm = new Form();
            frm.setCapturing(true);
            
            System.out.println("Start capturing...");            

            AudioInputStream ais = new AudioInputStream(line);
            
            frm.setRecording(true);
            System.out.println("Start recording...");
 
            // start recording
            AudioSystem.write(ais, fileType, wavFile);
 
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
 
    /**
     * Closes the target data line to finish capturing and recording
     */
    void finish() {
        line.stop();
        line.close();
        Form frm = new Form();
        frm.setFinished(true);
        frm.setCapturing(false);
        frm.setRecording(false);
        System.out.println("Finished");
    }
}