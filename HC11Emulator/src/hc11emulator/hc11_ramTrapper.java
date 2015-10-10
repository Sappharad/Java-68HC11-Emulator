/*
 * hc11_ramTrapper.java
 *
 * Created on December 27, 2006, 6:43 PM
 *
 * @author Paul Kratt
 */

package hc11emulator;

public interface hc11_ramTrapper {
    /** Notify the class trapping this memory location that the trapped ram has been written to.
     *@param trapaddr The address that was trapped
     *@param instaddr The address of the instruction that fell into our trap.
     **/
    public void writeTrap(int trapaddr, int instraddr);
    
    /** Notify the class trapping this memory location that the trapped ram has been read from.
     *@param trapaddr The address that was trapped
     *@param instaddr The address of the instruction that fell into our trap.
     **/
    public void readTrap(int trapaddr, int instraddr);
}
