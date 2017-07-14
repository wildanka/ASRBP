/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package App;


import MFCC.FeatureExtraction;
import MFCC.SpeechProcessing;

import WavFileProcessor.ReadWav;
import java.io.File;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFileChooser;

/**
 *
 * @author HP
 */
public class Form extends javax.swing.JFrame {
    int DURASI_REKAMAN, KALI_DETIK;
    String LOKASI_SIMPAN_REKAMAN;
    //private int samplePoint, frameRate;
            
    private boolean capturing = false;
    private boolean recording = false;
    private boolean finished = false;
//    double[][] magnitudeFFT = new double[frameRate][samplePoint];
    
    //private ArrayList<Double> tempArray = new ArrayList<>();
//    private static Complex[] magnitudeFFT = new Complex[512/2];
//
//    public static Complex[] getMagnitudeFFT() {
//        return magnitudeFFT;
//    }
//
//    public static void setMagnitudeFFT(Complex[] magnitudeFFT) {
//        Form.magnitudeFFT = magnitudeFFT;
//    }
    
   /* 
    public int getSamplePoint() {
        return samplePoint;
    }

    //ArrayList<C> MagnitudeSatuSisi[i] = new ArrayList<>;
    public int getFrameRate() {
        return frameRate;
    }
*/
    
    public void setCapturing(boolean capturing) {
        this.capturing = capturing;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    
    public String getLOKASI_SIMPAN_REKAMAN() {
        return LOKASI_SIMPAN_REKAMAN;
    }
    
    
    /**
     * Creates new form Form
     */
    public Form() {
        initComponents();
    }

    public int getDurasiRekaman(){
        return DURASI_REKAMAN;
    }
    

    public void rekamSuara(String lokasi, int durasi){
        //tentukan durasi rekaman
        //DURASI_REKAMAN = Integer.valueOf(textfieldDurasiRekam.getText());
        KALI_DETIK = 1000;
        DURASI_REKAMAN = durasi * KALI_DETIK;
        LOKASI_SIMPAN_REKAMAN = lokasi;
        System.out.println(LOKASI_SIMPAN_REKAMAN);
        System.out.println(DURASI_REKAMAN);
        
        final JavaSoundRecorder recorder = new JavaSoundRecorder();
        recorder.setFilePath(LOKASI_SIMPAN_REKAMAN);
        // creates a new thread that waits for a specified
        // of time before stopping
        Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(DURASI_REKAMAN);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                recorder.finish();
            }
        });
 
        stopper.start();
 
        // start recording
        recorder.start();
        labelStatusRekam1.setText("Berhasil merekam suara");        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                labelStatusRekam1.setText("-");                
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }, 1500);        
    }    

    
    private void hitungMFCC(String lokasi){       
        DecimalFormat formatEmpat = new DecimalFormat("#0.0000"); //format angka
        DecimalFormat formatEnam = new DecimalFormat("#0.000000"); //format angka
        DecimalFormat formatSembilan = new DecimalFormat("#0.000000000"); //format angka
     
        //lakukan pre-processing (sampling)
        ReadWav pre = new ReadWav();
        pre.bacaFile(lokasi); 
        pre.getNilaiSample();        
        
        //output nilai sample
        for (int i = 0; i < pre.getNilaiSample().size(); i++) {            
            textareaNilaiSampel.append("("+i+")"+String.valueOf(formatSembilan.format(pre.getNilaiSample().get(i)))+"\n");
        }                 

        /*
        //silencer
        ArrayList<Double> nonSilence = new ArrayList<>();
        nonSilence = pre.silenceRemoval(pre.getNilaiSample(), 16000);        
        // output hasil silence removal
        for (int i = 0; i < nonSilence.size(); i++) {            
            textareaSilenceRemoved.append("("+i+")"+String.valueOf(formatSembilan.format(nonSilence.get(i)))+"\n");
        }         
        */
        
        //lakukan Speech processing
        SpeechProcessing sp = new SpeechProcessing();
        
        // DC REMOVAL
        sp.dcRemoval(pre.getNilaiSample());
        
        // PRE EMPHASIS
        ArrayList<Double> preEmphasisSignal = sp.preEmphasis(sp.dcRemoval(pre.getNilaiSample()));
        for (int i = 0; i < pre.getNilaiSample().size(); i++) {
            textareaPreEmphasis.append("("+i+")"+String.valueOf(formatSembilan.format(preEmphasisSignal.get(i))+"\n"));
        }                              
        
        // FRAME BLOCKING        
        //tentukan jumlah frame blocking (frameRate)        
        // jika frame sebesar 25 ms = 16 * 25 = 400        
        int samplePoint = 16 * 25; 
        int M = samplePoint/2;  //M
        int frameRate = (pre.getNilaiSample().size() - samplePoint) / M +1; //jumlah frame dalam satu detik
        FeatureExtraction mfcc = new FeatureExtraction(pre.getNilaiSample().size(),samplePoint,frameRate);        
        mfcc.frameBlocking(preEmphasisSignal);
        
        //output hasil frame blocking
        int start;
        
        for (int i = 0; i < frameRate; i++) {
            //buat(bagi) frame sebanyak framerate
            start = (samplePoint/2)*i;
            textareaFrameBlocking.append(String.valueOf(i)+"\t");
            for (int j = start; j < start+samplePoint; j++) {
                if (preEmphasisSignal!=null) {
                    mfcc.frame[i][j-start] = preEmphasisSignal.get(j);
                } else {
                    mfcc.frame[i][j-start] = 0.0;
                }               
               textareaFrameBlocking.append(String.valueOf(formatEmpat.format(mfcc.frame[i][j-start]))+"\t");               
            }           
            textareaFrameBlocking.append("\n");
        }
        
        // WINDOWING
        mfcc.windowing(FeatureExtraction.frame, frameRate, samplePoint);                
        for (int i = 0; i < frameRate; i++) {
            textareaWindowing.append(String.valueOf(i)+"\t");
            for (int j = 0; j < samplePoint; j++) {                
                textareaWindowing.append(String.valueOf(formatEnam.format(mfcc.windowingSignal[i][j]))+"\t");                
            }
            textareaWindowing.append("\n");
        }     
    
        //lakukan Ekstraksi Ciri
        //FFT               
        mfcc.fastFourierTransform(FeatureExtraction.frame, frameRate, samplePoint);
        mfcc.getMagnitudeSemua();
        //double magnitude[][] = new double[frameRate][samplePoint];
        //magnitude = mfcc.getMagnitudeSemua();
        for (int i = 0; i < frameRate; i++) {
            textareaFFT.append(String.valueOf(i)+"\t");
            for (int j = 0; j < samplePoint; j++) {               
               textareaFFT.append(String.valueOf(formatEmpat.format(mfcc.magnitudeSemua[i][j]))+"\t");
               //System.out.println(i+" - "+j);
               //textareaFFT.append(i+"-"+j+"\t");
            }
            textareaFFT.append("\n");
        }
        
        //mel-filterbank
        //mfcc.filterbank(mfcc.magnitudeSemua, 22);
        // framerate = jumlah frame yang ada
        // samplePoint = jumlah point(sample dalam 1 frame)
        mfcc.filterbank(mfcc.getMagnitudeSemua(),1000,frameRate,samplePoint); 
        for (int i = 0; i < frameRate; i++) {
            textareaFilterbank.append(String.valueOf(i)+"\t");
            for (int j = 0; j < samplePoint; j++) {               
               textareaFilterbank.append(String.valueOf(formatEmpat.format(mfcc.melFreqEnergy[i][j]))+"\t");               
            }
            textareaFilterbank.append("\n");
        }
        
        //DCT

        mfcc.discreteCosineTransform(mfcc.melFreqEnergy, frameRate, samplePoint);
        System.out.println(mfcc.dctCepstrum.length+" "+mfcc.dctCepstrum[0].length);
        for (int i = 0; i < mfcc.dctCepstrum.length; i++) {
            textareaDCT.append(String.valueOf(i)+"\t");
            for (int j = 0; j < mfcc.dctCepstrum[0].length; j++) {               
               textareaDCT.append(String.valueOf(formatEmpat.format(mfcc.dctCepstrum[i][j]))+"\t");               
            }
            textareaDCT.append("\n");
        }
        
        
        //cepstral Liftering  
        mfcc.normalisasi();
        double[] outputRata = new double[frameRate];
        //outputRata = mfcc.normalisasi();
        //System.out.println(mfcc.dctCepstrum.length+" "+mfcc.dctCepstrum[0].length);
        /*
        for (int j = 0; j < mfcc.dctCepstrum[0].length; j++) {               
           textareaCepstralLiftering.append(String.valueOf(formatEmpat.format(outputRata[j]))+"\n");               
        }
        */
        for (int j = 0; j < mfcc.outputMFCC.length; j++) {               
           //textareaInputERNN.append(String.valueOf(formatEmpat.format(mfcc.outputMFCC[j]))+"\n");               
           textareaInputERNN.append(String.valueOf(mfcc.outputMFCC[j])+"\n");               
        }

    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        Perhitungan = new javax.swing.JTabbedPane();
        panelRekamParent = new javax.swing.JPanel();
        panelRekamChild = new javax.swing.JPanel();
        btnRekam = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        textfieldDurasiRekam = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        textfieldLokasiRekaman = new javax.swing.JTextField();
        btnBrowseLokasiSave = new javax.swing.JButton();
        labelStatusRekam1 = new javax.swing.JLabel();
        panelTraining = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtLokasiFileTraining = new javax.swing.JTextField();
        btnBrowseFileTraining = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        comboTargetTraining = new javax.swing.JComboBox<>();
        btnMulaiTraining = new javax.swing.JButton();
        panelTesting = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        textfieldLokasiFile = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        btnAnalisisAudio = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        textareaHasil = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        textareaNilaiSampel = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        textareaSilenceRemoved = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        textareaPreEmphasis = new javax.swing.JTextArea();
        panelPerhitunganMFCC = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textareaFrameBlocking = new javax.swing.JTextArea();
        panelPerhitungan = new javax.swing.JScrollPane();
        textareaWindowing = new javax.swing.JTextArea();
        jScrollPane7 = new javax.swing.JScrollPane();
        textareaFilterbank = new javax.swing.JTextArea();
        jScrollPane8 = new javax.swing.JScrollPane();
        textareaDCT = new javax.swing.JTextArea();
        jScrollPane9 = new javax.swing.JScrollPane();
        textareaCepstralLiftering = new javax.swing.JTextArea();
        jScrollPane11 = new javax.swing.JScrollPane();
        textareaFFT = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        textareaInputERNN = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelRekamChild.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnRekam.setText("Rekam");
        btnRekam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRekamActionPerformed(evt);
            }
        });

        jLabel1.setText("Tekan Tombol \"Rekam\" untuk mulai merekam");

        jLabel2.setText("Masukkan Durasi Proses rekam ");

        jLabel9.setText("Tentukan Lokasi File Hasil Rekaman ");

        btnBrowseLokasiSave.setText("...");
        btnBrowseLokasiSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseLokasiSaveActionPerformed(evt);
            }
        });

        labelStatusRekam1.setText("-");

        javax.swing.GroupLayout panelRekamChildLayout = new javax.swing.GroupLayout(panelRekamChild);
        panelRekamChild.setLayout(panelRekamChildLayout);
        panelRekamChildLayout.setHorizontalGroup(
            panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRekamChildLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textfieldDurasiRekam, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelRekamChildLayout.createSequentialGroup()
                            .addComponent(btnRekam)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelStatusRekam1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelRekamChildLayout.createSequentialGroup()
                            .addComponent(textfieldLokasiRekaman, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnBrowseLokasiSave, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(134, 134, 134))
        );
        panelRekamChildLayout.setVerticalGroup(
            panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRekamChildLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textfieldDurasiRekam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(textfieldLokasiRekaman, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseLokasiSave))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelRekamChildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnRekam)
                    .addComponent(labelStatusRekam1))
                .addContainerGap(301, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelRekamParentLayout = new javax.swing.GroupLayout(panelRekamParent);
        panelRekamParent.setLayout(panelRekamParentLayout);
        panelRekamParentLayout.setHorizontalGroup(
            panelRekamParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRekamParentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelRekamChild, javax.swing.GroupLayout.PREFERRED_SIZE, 667, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelRekamParentLayout.setVerticalGroup(
            panelRekamParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRekamParentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelRekamChild, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        Perhitungan.addTab("Rekam", panelRekamParent);

        jLabel13.setText("Klik tombol \"Mulai\" untuk melakukan training");

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setText("Masukkan File Suara Training :");

        btnBrowseFileTraining.setText("...");
        btnBrowseFileTraining.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseFileTrainingActionPerformed(evt);
            }
        });

        jLabel8.setText("Teks yang diharapkan :");

        comboTargetTraining.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "a", "i", "bu", "ka", "ki", "ku", "ke", "me", "sa", "sar", "sin", "ya", " ", " " }));

        btnMulaiTraining.setText("Mulai");
        btnMulaiTraining.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMulaiTrainingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnMulaiTraining, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtLokasiFileTraining, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBrowseFileTraining))
                    .addComponent(comboTargetTraining, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtLokasiFileTraining, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseFileTraining))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(comboTargetTraining, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnMulaiTraining, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelTrainingLayout = new javax.swing.GroupLayout(panelTraining);
        panelTraining.setLayout(panelTrainingLayout);
        panelTrainingLayout.setHorizontalGroup(
            panelTrainingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTrainingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTrainingLayout.createSequentialGroup()
                .addContainerGap(356, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(315, 315, 315))
        );
        panelTrainingLayout.setVerticalGroup(
            panelTrainingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTrainingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 204, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(23, 23, 23))
        );

        Perhitungan.addTab("Training", panelTraining);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder()));

        jLabel11.setText("Masukkan Data Uji");

        btnBrowse.setText("...");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        btnAnalisisAudio.setText("Analyze Audio");
        btnAnalisisAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnalisisAudioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addGap(138, 138, 138)
                .addComponent(textfieldLokasiFile, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnalisisAudio)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textfieldLokasiFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse)
                    .addComponent(btnAnalisisAudio)
                    .addComponent(jLabel11))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Hasil"));

        textareaHasil.setColumns(20);
        textareaHasil.setRows(5);
        jScrollPane10.setViewportView(textareaHasil);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Nilai Sampel"));

        textareaNilaiSampel.setColumns(20);
        textareaNilaiSampel.setRows(5);
        textareaNilaiSampel.setMaximumSize(new java.awt.Dimension(164, 94));
        textareaNilaiSampel.setName(""); // NOI18N
        jScrollPane2.setViewportView(textareaNilaiSampel);

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Silence Removed"));

        textareaSilenceRemoved.setColumns(20);
        textareaSilenceRemoved.setRows(5);
        jScrollPane4.setViewportView(textareaSilenceRemoved);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Pre Emphasis"));

        textareaPreEmphasis.setColumns(20);
        textareaPreEmphasis.setRows(5);
        textareaPreEmphasis.setMaximumSize(new java.awt.Dimension(164, 94));
        jScrollPane3.setViewportView(textareaPreEmphasis);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 8, Short.MAX_VALUE)))))
                .addGap(102, 102, 102))
        );

        javax.swing.GroupLayout panelTestingLayout = new javax.swing.GroupLayout(panelTesting);
        panelTesting.setLayout(panelTestingLayout);
        panelTestingLayout.setHorizontalGroup(
            panelTestingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTestingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTestingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelTestingLayout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 175, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelTestingLayout.setVerticalGroup(
            panelTestingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTestingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        Perhitungan.addTab("Testing", panelTesting);

        jScrollPane5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel2.setAutoscrolls(true);

        textareaFrameBlocking.setColumns(20);
        textareaFrameBlocking.setRows(5);
        textareaFrameBlocking.setBorder(javax.swing.BorderFactory.createTitledBorder("Frame Blocking"));
        jScrollPane1.setViewportView(textareaFrameBlocking);

        textareaWindowing.setColumns(20);
        textareaWindowing.setRows(5);
        textareaWindowing.setBorder(javax.swing.BorderFactory.createTitledBorder("Windowing"));
        panelPerhitungan.setViewportView(textareaWindowing);

        textareaFilterbank.setColumns(20);
        textareaFilterbank.setRows(5);
        textareaFilterbank.setBorder(javax.swing.BorderFactory.createTitledBorder("Filterbank"));
        jScrollPane7.setViewportView(textareaFilterbank);

        textareaDCT.setColumns(20);
        textareaDCT.setRows(5);
        textareaDCT.setBorder(javax.swing.BorderFactory.createTitledBorder("DCT"));
        jScrollPane8.setViewportView(textareaDCT);

        textareaCepstralLiftering.setColumns(20);
        textareaCepstralLiftering.setRows(5);
        textareaCepstralLiftering.setBorder(javax.swing.BorderFactory.createTitledBorder("Cepstral Liftering"));
        jScrollPane9.setViewportView(textareaCepstralLiftering);

        textareaFFT.setColumns(20);
        textareaFFT.setRows(5);
        textareaFFT.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "FFT"));
        jScrollPane11.setViewportView(textareaFFT);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelPerhitungan, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .addComponent(jScrollPane8))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .addComponent(jScrollPane9))
                .addGap(136, 136, 136))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelPerhitungan, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        jScrollPane5.setViewportView(jPanel2);

        javax.swing.GroupLayout panelPerhitunganMFCCLayout = new javax.swing.GroupLayout(panelPerhitunganMFCC);
        panelPerhitunganMFCC.setLayout(panelPerhitunganMFCCLayout);
        panelPerhitunganMFCCLayout.setHorizontalGroup(
            panelPerhitunganMFCCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 926, Short.MAX_VALUE)
        );
        panelPerhitunganMFCCLayout.setVerticalGroup(
            panelPerhitunganMFCCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
        );

        Perhitungan.addTab("Perhitungan Ekstraksi Ciri", panelPerhitunganMFCC);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Normalisasi MFCC (input)"));

        textareaInputERNN.setColumns(20);
        textareaInputERNN.setRows(5);
        jScrollPane6.setViewportView(textareaInputERNN);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(657, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(200, Short.MAX_VALUE))
        );

        Perhitungan.addTab("Perhitungan ERNN", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Perhitungan, javax.swing.GroupLayout.PREFERRED_SIZE, 883, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Perhitungan)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAnalisisAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalisisAudioActionPerformed
        textareaFrameBlocking.setText("");
        textareaNilaiSampel.setText("");
        textareaSilenceRemoved.setText("");
        textareaPreEmphasis.setText("");
        textareaWindowing.setText("");
        textareaFFT.setText("");
        textareaFilterbank.setText("");
        textareaDCT.setText("");
        textareaCepstralLiftering.setText("");
        
        textareaInputERNN.setText("");
        String lokasi = textfieldLokasiFile.getText();
        System.out.println(lokasi);
        //int bufferBy = Integer.parseInt(jTextField1.getText());
        
        try {
            hitungMFCC(lokasi);
        } catch (Exception e) {
            System.out.println(e);
        }
    }//GEN-LAST:event_btnAnalisisAudioActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        //JFileChooser chooser = new JFileChooser();
        jFileChooser1.showOpenDialog(null);
        File f = jFileChooser1.getSelectedFile();
        String filename = f.getAbsolutePath();

        textfieldLokasiFile.setText(filename);
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void btnMulaiTrainingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMulaiTrainingActionPerformed
        // TODO add your handling code here:
        String lokasi = txtLokasiFileTraining.getText();
        hitungMFCC(lokasi);
    }//GEN-LAST:event_btnMulaiTrainingActionPerformed

    private void btnBrowseFileTrainingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseFileTrainingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBrowseFileTrainingActionPerformed

    private void btnBrowseLokasiSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseLokasiSaveActionPerformed
        jFileChooser1.showOpenDialog(null);
        File f = jFileChooser1.getSelectedFile();
        String filename = f.getAbsolutePath();

        textfieldLokasiRekaman.setText(filename);
    }//GEN-LAST:event_btnBrowseLokasiSaveActionPerformed

    private void btnRekamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRekamActionPerformed
        String lokasi = textfieldLokasiRekaman.getText();
        int durasi = Integer.valueOf(textfieldDurasiRekam.getText());
        rekamSuara(lokasi, durasi);
    }//GEN-LAST:event_btnRekamActionPerformed
        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Form().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane Perhitungan;
    private javax.swing.JButton btnAnalisisAudio;
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnBrowseFileTraining;
    private javax.swing.JButton btnBrowseLokasiSave;
    private javax.swing.JButton btnMulaiTraining;
    private javax.swing.JButton btnRekam;
    private javax.swing.JComboBox<String> comboTargetTraining;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JLabel labelStatusRekam1;
    private javax.swing.JScrollPane panelPerhitungan;
    private javax.swing.JPanel panelPerhitunganMFCC;
    private javax.swing.JPanel panelRekamChild;
    private javax.swing.JPanel panelRekamParent;
    private javax.swing.JPanel panelTesting;
    private javax.swing.JPanel panelTraining;
    private javax.swing.JTextArea textareaCepstralLiftering;
    private javax.swing.JTextArea textareaDCT;
    private javax.swing.JTextArea textareaFFT;
    private javax.swing.JTextArea textareaFilterbank;
    private javax.swing.JTextArea textareaFrameBlocking;
    private javax.swing.JTextArea textareaHasil;
    private javax.swing.JTextArea textareaInputERNN;
    private javax.swing.JTextArea textareaNilaiSampel;
    private javax.swing.JTextArea textareaPreEmphasis;
    private javax.swing.JTextArea textareaSilenceRemoved;
    private javax.swing.JTextArea textareaWindowing;
    private javax.swing.JTextField textfieldDurasiRekam;
    private javax.swing.JTextField textfieldLokasiFile;
    private javax.swing.JTextField textfieldLokasiRekaman;
    private javax.swing.JTextField txtLokasiFileTraining;
    // End of variables declaration//GEN-END:variables
}
