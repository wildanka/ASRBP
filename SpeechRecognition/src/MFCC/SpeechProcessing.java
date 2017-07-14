/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MFCC;

import java.util.ArrayList;

/**
 *
 * @author DAN
 */
public class SpeechProcessing {
    ArrayList<Double> temp1 = new ArrayList<>();
    private ArrayList<Double> preEmphasisSignal = new ArrayList<Double>();
     
    public SpeechProcessing(){
        
    }
    
    public ArrayList<Double> getPreEmphasisSignal() {
        return preEmphasisSignal;
    }    
    
    public ArrayList<Double> dcRemoval(ArrayList<Double> sample){
        //System.out.println("DC Removal---------------");
        double NILAI_RATA;
        double jumlahSampel = 0;
        
        //menghitung rata-rata sinyal sample suara
        for (int i = 0; i < sample.size(); i++) {
            jumlahSampel = jumlahSampel+sample.get(i);
        }
        NILAI_RATA = jumlahSampel/sample.size();

        //menghitung DC Removal
        for (int i = 0; i < sample.size(); i++) {
            double hitungDCRemovali = sample.get(i) - NILAI_RATA;
            temp1.add(hitungDCRemovali); //temp1 adalah sinyal dcRemoval
        }

        return temp1;
    }
        
    public ArrayList<Double> preEmphasis(ArrayList<Double> sample){
        System.out.println("Pre emphasis-------------");
        double YMin1 = 0.0;
        ArrayList<Double> temp2 = new ArrayList<>();
        Double YBaru[] = new Double[sample.size()];
        Double NBaru[] = new Double[sample.size()];

        // Yn = Sn - (a. S[n-1] )
        for (int i=0; i<sample.size(); i++) {
            //System.out.println(String.valueOf(sinyalDCRemoval[i]));
            double hitungPreEmphasisi = temp1.get(i) - (0.97 * YMin1);
            temp2.add(hitungPreEmphasisi);
            //System.out.println("Y"+i+" = "+temp1.get(i)+" - ("+YMin1+" * 0.97) = "+String.valueOf(preEmphasisi.get(i)));
            YMin1 = temp1.get(i);
        }

        for (int i = 0; i <sample.size() ; i++) {
            preEmphasisSignal.add(temp1.get(i)+temp2.get(i)); //sinyal baru = preEmphasisSignal
        }
        return preEmphasisSignal;
    }
    
}
