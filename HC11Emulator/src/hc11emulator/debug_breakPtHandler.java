/*
 * debug_breakPtHandler.java
 *
 * Created on January 3, 2007, 9:12 PM
 * Handles breakpoints, and informing the Debug Window about them.
 *
 * @author Paul Kratt
 */

package hc11emulator;

import java.util.ArrayList;

public class debug_breakPtHandler implements hc11_ramTrapper{
    private DebuggerWindow window;
    private hc11_Device device;
    private ArrayList<Integer> bpxlist; //Breakpoint on execute list
    private ArrayList<Integer> bprlist; //Breakpoint on read list
    private ArrayList<Integer> bpwlist; //Breakpoint on write list
    private boolean haltnext; //Halt on the next instruction, because we hit a read/write during the previous.
    private int haltaddr=0; //When halting for bpr and bpw, this ensures we halt at the instruction doing the access, not the one after.
    
    /** Creates a new instance of debug_breakPtHandler */
    public debug_breakPtHandler(DebuggerWindow dbgWin, hc11_Device board) {
        window = dbgWin;
        device = board;
        bpxlist = new ArrayList(); //Setup our bpx list.
        bprlist = new ArrayList(); //Setup our bpr list.
        bpwlist = new ArrayList(); //Setup our bpw list.
        haltnext = false;
    }
    
    /** Check PC to see if we need to break right now.
     *@return True if the thread needs to halt.
     **/
    public boolean checkPC(){
        boolean retval = false;
        int mypc = device.getPC();
        
        if(haltnext){
            retval=true;
            haltnext=false;
            window.haltForBreakpoint(haltaddr);
        }
        //Loop through our PC's and see if any of them are the location we are at now.
        for(int i=0; i<bpxlist.size();i++){
            if(mypc==bpxlist.get(i).intValue()){
                retval=true;
                window.haltForBreakpoint(device.getPC());
                break;
            }
        }
        
        return retval;
    }
    
    /** Add a bpx somewhere **/
    public void addbpx(int location){
        bpxlist.add(new Integer(location));
    }
    
    /** Remove a bpx from somewhere **/
    public void removebpx(int location){
        for(int i=0;i<bpxlist.size();i++)
            if(bpxlist.get(i).intValue()==location){
                bpxlist.remove(i);
                break;
            }
    }
    
    /** Add a bpr somewhere **/
    public void addbpr(int location){
        bprlist.add(new Integer(location));
        device.addTrap(location,this);
    }
    
    /** Remove a bpr from somewhere. **/
    public void removebpr(int location){
        for(int i=0;i<bprlist.size();i++)
            if(bprlist.get(i).intValue()==location){
                bprlist.remove(i);
                device.removeTrap(location,this);
                break;
            }
    }
    
    /** Add a bpw somewhere **/
    public void addbpw(int location){
        bpwlist.add(new Integer(location));
        device.addTrap(location,this);
    }
    
    /** Remove a bpr from somewhere. **/
    public void removebpw(int location){
        for(int i=0;i<bpwlist.size();i++)
            if(bpwlist.get(i).intValue()==location){
                bpwlist.remove(i);
                device.removeTrap(location,this);
                break;
            }
    }

    public void readTrap(int trapaddr, int instraddr) {
        boolean exists = false;
        for(int i=0; i<bprlist.size(); i++){
            if(bprlist.get(i).intValue()==trapaddr)
                exists=true; //Confirm that we have a read trap on this, not a write trap.
        }
        if(exists){
            haltaddr=instraddr;
            haltnext=true;
        }
    }

    public void writeTrap(int trapaddr, int instraddr) {
        boolean exists = false;
        for(int i=0; i<bpwlist.size(); i++){
            if(bpwlist.get(i).intValue()==trapaddr)
                exists=true; //Confirm that we have a read trap on this, not a write trap.
        }
        if(exists){
            haltaddr=instraddr;
            haltnext=true;
        }
    }
    
}
