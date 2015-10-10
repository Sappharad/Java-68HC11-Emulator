/*
 * hc11_thread.java
 *
 * Created on July 7, 2006, 8:49 PM
 * @author Paul Kratt
 */

package hc11emulator;

public class hc11_thread extends Thread{
    private hc11_Device board; //The hc11 board we're running.
    private final long clkspeed = 2000000; //Clock speed. Default is 2mhz
    private final int fps=30; //How many times per second the observers should be notified.
    private boolean runmode,noquit;
    private debug_breakPtHandler bphandler; //Breakpoint handler, if attached.
    
    /** Creates a new instance of hc11_thread */
    public hc11_thread(hc11_Device board) {
        this.board=board;
        runmode=false;
        noquit=true;
    }
    
    /** Continously execute instructions on the HC11.
     * Sleep 30 times a second, so that the speed of the emulator matches real hardware.
     */
    public void run() {
        long segment_cycles=clkspeed/fps;
        long lastTime=0;
        final long sleeptime = (long)Math.ceil(1000.0/fps);
        
        try {
            while(noquit){
                //This should remain an infinite loop, until the program wants to quit.
                if(runmode){
                    lastTime+=System.currentTimeMillis();
                    long stoptime = board.getCycles()+segment_cycles;
                    while(board.getCycles()<stoptime){
                        board.execute();
                        if(bphandler!=null && bphandler.checkPC()){
                            //Breakpoints are on, and we need to stop
                            runmode = false;
                            stoptime=board.getCycles();
                            break;
                        }
                    }
                    board.notifyObservers();
                    lastTime=sleeptime-(System.currentTimeMillis()-lastTime); //How many ms have passed?
                    if(lastTime>0){
                        this.sleep(lastTime);
                        lastTime=0;
                        //If lastTime is negative, we'll just sleep less the next cycle to catch up.
                    }
                } else{
                    this.sleep(100); //Sleep for a 10th of a second
                    lastTime=0;
                }
                
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    /** Tell the thread to be running, executing instructions in pseudo-realtime. */
    public void setRunmode(boolean runmode) {
        this.runmode = runmode;
    }
    
    /** Set the noquit variable. If true, don't quit. If false, end the thread as soon as you can. **/
    public void setNoquit(boolean noquit) {
        this.noquit = noquit;
    }
    
    /** Attach a breakpoint handler to this thread. We sort've need one for the GUI to handle breakpoints! **/
    public void attachBPhandler(debug_breakPtHandler bph){
        bphandler = bph;
    }
    
}
