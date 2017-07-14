package MFCC;

import App.Form;

import java.util.ArrayList;
import javax.swing.JTextArea;

/**
 * Created by HP on 4/22/2017.
 */
public class FeatureExtraction {
    private int SAMPLE_RATE, SAMPLE_POINT, panjangSinyalAudio;
    private double frameRate;
    int jumlahFrame;
    int panjangFrame;
    
    int JUMLAH_FRAME_SETELAH_DIBAGI;
    ArrayList<Double> temp2 = new ArrayList<Double>();
    Form f = new Form();
    public static double[][] frame; 
    public double[][] windowingSignal;
    public double[][] magnitudeSemua;
    public double[][] melFreqEnergy;
    public double[][] dctCepstrum;
    public double[] outputMFCC;

    
    /**
     * @param SAMPLE_RATE = jumlah sample dalam sebuah frame yang akan diolah
     * @param SAMPLE_POINT = ukuran dari frame (frame size)
     * @param frameRate = jumlah frame yang akan dibuat
     */
    public FeatureExtraction(int SAMPLE_RATE, int SAMPLE_POINT, int frameRate) {
        this.frame = new double[frameRate][SAMPLE_POINT];
        this.windowingSignal = new double[frameRate][SAMPLE_POINT];
        this.magnitudeSemua = new double[frameRate][SAMPLE_POINT];
        this.SAMPLE_RATE = SAMPLE_RATE;
        this.SAMPLE_POINT = SAMPLE_POINT;
        this.frameRate = frameRate;
    }
    
    public double[][] getMagnitudeSemua() {
        return magnitudeSemua;
    }
    
    public int getJumlahFrame() {
        return jumlahFrame;
    }

    public int getPanjangFrame() {
        return panjangFrame;
    }
    
    public void frameBlocking(ArrayList<Double> preEmphasisSignal){
        /**
        * jumlah frame = ((I-N)/M)+1
        * I = sample rate  --> jumlah sample (keseluruhan) yang akan diolah (I)
        * N = sample point --> 16(bit per sample) * 25 (ms) = (N) 
        * M = N/2 (overlapping 50%)
        * 1 ms = 16 sample
        * */
        
        int M = SAMPLE_POINT/2;
        //frameRate = (SAMPLE_RATE - SAMPLE_POINT) / M -1; //jumlah frame dalam satu detik
        System.out.println("Jumlah Frame (setelah frame blocking) = "+String.valueOf(frameRate));
        
        //Frame Blocking                
        System.out.println("sample point : "+SAMPLE_POINT+", framerate : "+(frameRate)+", M = "+M);
       
        int start, end;
        
        for (int i = 0; i < frameRate; i++) {
            //buat(bagi) frame sebanyak framerate
            start = M*i;
            end = (i+1) * SAMPLE_POINT;
            //textareaFrameBlocking.append(String.valueOf(i)+"\t");            

            for (int j = start; j < start+SAMPLE_POINT; j++) {
                if (preEmphasisSignal!=null) {
                    frame[i][j-start] = preEmphasisSignal.get(j);
                } else {
                    frame[i][j-start] = 0.0;
                }
            }                       
        }   
    }

    /**
     * @param sample = jumlah sample dalam sebuah frame yang akan diolah
     * @param jumlahFrame = banyaknya frame (total frame)
     * @param panjangFrame = ukuran dari frame (frame size)
     * @return windowingSignal = array 2d hasil windowing 
     */
    //public void windowing(double sample[][], int jumlahFrame, int panjangFrame){
    public double[][] windowing(double sample[][], int jumlahFrame, int panjangFrame){
        this.panjangFrame = panjangFrame;
        this.jumlahFrame = jumlahFrame;
//        System.out.println(this.panjangFrame);
//        System.out.println(this.jumlahFrame);
//        System.out.println(panjangFrame);
//        System.out.println(jumlahFrame);
        double fungsiWindowingSignal[][] = new double[jumlahFrame][panjangFrame];
        /*
        * lakukan Hamming Window :
        *   w(n) = 0.54 - 0.46 cos( (2*phi*n) / (M-1) )
        * dimana :
        *   M = panjang frame
    *    *   n = 0, 1, ..., M-1
        */        
        for (int i = 0; i < jumlahFrame; i++) {
            for (int j = 0; j < panjangFrame; j++) {      
                double RUMUS_HAMMING_WINDOW = (2 * 3.14 * i) / ( panjangFrame - 1 );
                double FUNGSI_HAMMING_WINDOW = ((0.54 - 0.46) * Math.cos(RUMUS_HAMMING_WINDOW));
                fungsiWindowingSignal[i][j] = FUNGSI_HAMMING_WINDOW;
            }            
        }               

        /*
        * representasikan fungsi window terhadap sinyal
        *  x(n) = xi(n)* w(n)
        * dimana :
        *  n = 0, 1, ..., N-1
        *  x(n) nilai sampel signal hasil windowing
        *  xi(n) = nilai sampel dari frame signal ke-i (hasil pre emphasis)
        *  w(n) = fungsi window
        *  N = frame size
        */
        for (int i = 0; i < jumlahFrame; i++) {
            for (int j = 0; j < panjangFrame; j++) {      
                double HASIL_WINDOWING = sample[i][j] * fungsiWindowingSignal[i][j]; //sample = preEmphasisi
                windowingSignal[i][j] = HASIL_WINDOWING;
                
                //System.out.println("windowing ke"+i+","+j+" : "+windowingSignal[i][j]);
                //System.out.println("W-"+ i +","+j+" = "+ sample[i][j]+" * "+fungsiWindowingSignal[i][j]+" = "+windowingSignal[i][j]);
            }            
        }    
        //Done
        
        return windowingSignal;
        
    }

    public void fastFourierTransform(double sample[][], int jumlahFrame, int panjangFrame){
        double[][] spectrumFFT = new double[jumlahFrame][panjangFrame];
        double hitungFFT,hcos,hsin;
        double magnitudeSample[][] = new double[panjangFrame][panjangFrame];
        double nilaiAbsolut;
        double re, imaj;
        
        for (int i = 0; i < jumlahFrame; i++) {
            for (int n = 0; n < panjangFrame; n++) {
                double totCos = 0;
                double totSin = 0;
                for (int k = 0; k < panjangFrame; k++) {
                    hcos = sample[i][k]*(Math.cos((2*3.14*n*k)/panjangFrame));
                    hsin = sample[i][k]*(Math.sin((2*3.14*n*k)/panjangFrame));
                    totCos = totCos + hcos;
                    totSin = totSin + hsin;                    
                }
                re = Math.pow(totCos, 2);
                imaj = Math.pow(totSin, 2) *(-1); // 3i^2 = 3 * -1 = -3  
                nilaiAbsolut = Math.abs(re +imaj);
                magnitudeSample[i][n] = Math.abs(re +imaj);
                magnitudeSemua[i][n] = nilaiAbsolut;
            }            
        }
        
        
        /*
        for (int i = 0; i < panjangFrame; i++) {
            double totCos = 0;
            double totSin = 0;
            for (int j = 0; j < panjangFrame; j++) {
                    hcos = sample[1][j]*(Math.cos((2*3.14*i*j)/panjangFrame));
                    hsin = sample[1][j]*(Math.sin((2*3.14*i*j)/panjangFrame));
                    totCos = totCos + hcos;
                    totSin = totSin + hsin;

                    //System.out.println((2*3.13*i*j)/panjangFrame); 
            } 
                re = Math.sqrt(Math.pow(totCos, 2));
                imaj = Math.pow(totSin, 2) *(-1); // 3i^2 = 3 * -1 = -3
                //Magnitude[i] = re + imaj ;
                System.out.println(i+" = "+re+" + "+imaj);
        }*/        
    }
            
                              
     /*   
        for (int i = 0; i < jumlahFrame; i++) {
            
//                hitungFFT = ()/spectrumFFT.length;
//                spectrumFFT[i][j] = 
              for (int j = 0; j < panjangFrame; j++) {
                hcos = sample[i][j]*(Math.cos((2*3.14*i*j)/panjangFrame));
                hsin = sample[i][j]*(Math.sin((2*3.14*i*j)/panjangFrame));
                totCos = totCos + hcos;
                totSin = totSin + hsin;
               
                if (i==1) {
                /*
                    System.out.println("");
                    System.out.println(i+","+j+" = "+totCos+", j"+totSin );
                
                    System.out.println((2*3.13*i*j)/panjangFrame); 
                } 
            }      
        }
        */

    
    /**
     *
     * @param sample sample hasil FFT
     * @param freq frekuensi sample (sampling rate)
     * @param jumlahFrame jumlah frame yang ada (baris)
     * @param panjangFrame panjang dari masing-masing frame (kolom)
     */
    public void filterbank(double[][] sample, int freq, int jumlahFrame, int panjangFrame){
    //public void filterbank(){                
        //dapatkan nilai maksimal dari setiap frame
        double[] nilaiMax = new double[jumlahFrame];
        for (int i = 0; i < jumlahFrame; i++) {
            nilaiMax[i] = 0;
            for (int j = 0; j < panjangFrame; j++) {
                if (sample[i][j]>nilaiMax[i]) {
                    nilaiMax[i] = sample[i][j];
                }
            }            
        }
        
        System.out.println("filterbank--------;-------");
        double[][] melFilterFreq = new double[jumlahFrame][panjangFrame];
        double[][] koefisienFilterbank = new double[jumlahFrame][panjangFrame];
        for (int i = 0; i < jumlahFrame; i++) {
            for (int j = 0; j < panjangFrame; j++) {
                koefisienFilterbank[i][j] = (2595 * Math.log10(1 + nilaiMax[i]/700))/(sample[i][j]/2);
                //koefisienFilterbank[i][j] = (2595 * Math.log10(1 + sample[i][j]/700))/(sample[i][j]/2);
                //melFilterFreq[i][j] = sample[i][j] * koefisienFilterbank[i][j];;                
                melFilterFreq[i][j] = koefisienFilterbank[i][j];;                
                System.out.println(melFilterFreq[i][j]);
            }            
        }        
        melFreqEnergy = melFilterFreq;
    }
    
    /**
     *
     * @param melFreqEnergy hasil fari mel filterbank (mel frequency)
     * @param jumlahFrame jumlah frame yang ada (baris) = frameRate
     * @param panjangFrame panjang dari masing-masing frame (kolom) = samplePoint
     */
    public void discreteCosineTransform(double[][] melFreqEnergy, int jumlahFrame, int panjangFrame){
        
        int koefisien = 13;
        double[][] dct = new double[jumlahFrame][koefisien];

        for (int k = 0; k < jumlahFrame; k++) {
            for (int i = 0; i < koefisien; i++) {
                double temp = 0;    
                for (int j = 0; j < panjangFrame; j++) {                
                    //rumus
                    temp = temp + (Math.log(melFreqEnergy[k][j])* Math.cos(i*(j-0.5)*3.14/(koefisien)));
                }       
                dct[k][i] = temp;
                //System.out.println("frame ke-"+k+", dct ke"+i+" = "+dct[k][i]);
            }
        }
        
        dctCepstrum = dct;
    }

    
    public double[] rata(){
        System.out.println("rata2----------------------");
        System.out.println(dctCepstrum.length+" "+dctCepstrum[0].length);
        
        //menghitung total
        double[] sum = new double[dctCepstrum[0].length];
        for (int i = 0; i < dctCepstrum.length; i++) {     //16
            for (int j = 0; j < dctCepstrum[0].length; j++) { //13
                sum[j] += dctCepstrum[i][j];
            }                        
        }
        
        for (int i = 0; i < sum.length; i++) {
            sum[i] = sum[i]/ (double) dctCepstrum.length;
            System.out.println(i+" "+sum[i]);
        }
        
        return sum;
    }
    
    public void cepstralLiftering(ArrayList<Double> cepstralCoefficient){
  /*
        System.out.println("\n Cepstral Liftering---------");

        for (int i = 0; i <cepstralCoefficient.size() ; i++) {

            double lifteredCepstral = cepstralCoefficient.get(i) * (cepstralCoefficient.size()/2) * Math.sin(Math.PI/7) ;
            this.lifteredCepstral.add(lifteredCepstral);
            //System.out.println(this.lifteredCepstral.size());
            System.out.println(this.lifteredCepstral.get(i));
        }
  */
    }
    
    public double[] normalisasi(){
        double[] nilaiAkhir = new double[dctCepstrum[0].length];
        nilaiAkhir = rata();
        
        double[] normal = new double[dctCepstrum[0].length-1];
        double[] normalisasiEkstraksiCiri = new double[dctCepstrum[0].length-1];
        double min=999999;
        double max=0;
        //cari max
        for (int i = 1; i < dctCepstrum[0].length; i++) {
            if (nilaiAkhir[i] > max) {
                max = nilaiAkhir[i];
            }            
        //cari min
            if (nilaiAkhir[i] < min) {
                min = nilaiAkhir[i];
            }
            System.out.println(i+" "+nilaiAkhir[i]);
        }        
        
        System.out.println(min+" "+max);
        for (int i = 1; i < dctCepstrum[0].length; i++) {
            normal[i-1] = (0.8 * (nilaiAkhir[i]-min)/(max-min) )+0.1;            
            System.out.println(i+" |"+nilaiAkhir[i]+" = "+normal[i-1]+", setelah normalisasi = "+thresholding(normal[i-1]));   
            //outputMFCC[1] = thresholding(normal[i-1]);
            normalisasiEkstraksiCiri[i-1] = thresholding(normal[i-1]);
        }
        
        outputMFCC = normalisasiEkstraksiCiri;
        return normal;
    }
    
    private int thresholding(double input){
        if(input>=0.5){
            return 1;
        }
        return 0;
    }
}
