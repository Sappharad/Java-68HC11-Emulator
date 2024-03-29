/*
 * DumpConversionWindow.java
 *
 * Created on July 6, 2006, 8:53 PM
 */

package hc11emulator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;
import javax.swing.*;

/**
 *
 * @author  Paul Kratt
 */
public class DumpConversionWindow extends javax.swing.JFrame {
    
    /** Creates new form DumpConversionWindow */
    public DumpConversionWindow() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtInFile = new javax.swing.JTextField();
        btnOpen = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtOutFile = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        btnConvert = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Hex to binary conversion tool");
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("This tool will convert a hex dump to a binary file.");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Note: The memory locations present in the input file will be ignored.");

        jLabel3.setText("Input file:");

        txtInFile.setEditable(false);
        txtInFile.setText("No file selected!");

        btnOpen.setText("Browse");
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });

        jLabel4.setText("Save as:");

        txtOutFile.setEditable(false);
        txtOutFile.setText("No file selected!");

        btnSave.setText("Browse");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnConvert.setText("Convert");
        btnConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtOutFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .add(txtInFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
                        .add(9, 9, 9)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(btnSave, 0, 0, Short.MAX_VALUE)
                            .add(btnOpen, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, btnConvert, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnOpen)
                    .add(txtInFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtOutFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnSave))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnConvert)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertActionPerformed
        JOptionPane msgbox = new JOptionPane();
        
        if(txtInFile.getText().equalsIgnoreCase("No file selected!") ||
                txtOutFile.getText().equalsIgnoreCase("No file selected!")){
            msgbox.showMessageDialog(this,"Please select both input and output files.");
        } else{
            try {
                Scanner inRead = new Scanner(new File(txtInFile.getText()));
                FileOutputStream fout = new FileOutputStream(new File(txtOutFile.getText()));
                
                while(inRead.hasNextLine()){
                    String data = inRead.nextLine();
                    if(data.startsWith("/") && (data.length()==43)){
                        data = data.substring(9,41);
                        while(data.length()>0){
                            fout.write(Integer.parseInt(data.substring(0,2),16));
                            data=data.substring(2);
                        }
                    }
                }
                inRead.close();
                fout.close();
                msgbox.showMessageDialog(this,"The conversion is done!");
                this.dispose();
            } catch (Exception e) {
                msgbox.showMessageDialog(this,"Bad input file!");
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnConvertActionPerformed
    
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        JFileChooser fsel = new JFileChooser();
        
        if(fsel.showSaveDialog(this)==fsel.APPROVE_OPTION){
            txtOutFile.setText(fsel.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_btnSaveActionPerformed
    
    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        JFileChooser fsel = new JFileChooser();
        
        if(fsel.showOpenDialog(this)==fsel.APPROVE_OPTION){
            txtInFile.setText(fsel.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_btnOpenActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DumpConversionWindow().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConvert;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField txtInFile;
    private javax.swing.JTextField txtOutFile;
    // End of variables declaration//GEN-END:variables
    
}
