/*
 * hc11_Observer.java
 *
 * Created on July 7, 2006, 9:34 PM
 *
 *@author Paul Kratt
 */

package hc11emulator;

public interface hc11_Observer {
    /** The emulator will call this function when hardware that the user can 
     * see or interact with at runtime should update. This is called about
     * 30 times per second by default. */
    public void visualUpdate();
    
    /** This function returns the name of the observer device being added to the
     * board. This name should be unique to each device, as it is used to 
     * prevent the same device from being added to the board twice. */
    public String getName();
}
