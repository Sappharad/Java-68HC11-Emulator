/*
 * Instuct_Container.java
 *
 * Created on May 5, 2006, 2:02 PM
 */

package hc11emulator;

/**
 *
 * @author Paul Kratt
 */
public class Instruct_Container {
    private String instruction; //The actual instruction
    private String description; //Description of instruction
    private int size; //Size of the instruction
    
    /** Creates a new instance of Instuct_Container */
    public Instruct_Container() {
        instruction = "unknown";
        description = "Unkown instruction";
        size=1;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
}
