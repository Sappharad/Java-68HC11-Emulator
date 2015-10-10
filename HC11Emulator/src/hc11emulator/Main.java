/*
 * Main.java
 *
 * Created on May 2, 2006, 1:13 PM
 */

package hc11emulator;

import javax.swing.UIManager;

/**
 *
 * @author Paul Kratt
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){}
        
        DebuggerWindow debwin = new DebuggerWindow();
        debwin.setVisible(true);
        
    }
    
}
