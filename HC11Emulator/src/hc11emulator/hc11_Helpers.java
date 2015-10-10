/*
 * hc11_Helpers.java
 *
 * Created on July 4, 2006, 10:11 PM
 *
 * Functions that help the HC11 emulator be used. This class can not be instantiated.
 * This includes stuff like the .ELF file loader.
 */

package hc11emulator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

/**
 *
 * @author Paul Kratt
 */
public class hc11_Helpers {
    
    /** Cannot create a new instance of hc11_Helpers.*/
    private hc11_Helpers() {
    }
    
    /** Load an ELF file into the device provided, and set PC to the entry point.
     *@param elfFile The ELF file that you would like to load.
     *@param board The HC11 device to load the executable code onto.
     *@return True if loaded successfully, False if the input file is not ELF, or some other problem occurs.
     */
    public static boolean loadElf(File elfFile, hc11_Device board){
        FileInputStream codereader;
        boolean retval = true;
        
        try{
            int entrypoint,sections;
            byte[] elfdata = new byte[(int)(elfFile.length())];
            //I can load the entire ELF file into RAM, it's not very big, and its faster to read files in chunks.
            codereader = new FileInputStream(elfFile);
            codereader.read(elfdata);
            codereader.close();
            
            //Check for valid ELF, then read the chunks.
            if(elfdata[0]==0x7F && elfdata[1]==0x45 && elfdata[2]==0x4C && elfdata[3]==0x46){
                entrypoint = get32bit(elfdata,0x18);
                sections = elfdata[0x2D]; //If there are more than 127 sections in an ELF, something must be wrong.
                //I'll assume if the header is valid, the file should work.
                
                //Loop to load all sections into HC11 memory.
                for(int i=0;i<sections;i++){
                    int fileloc,memloc,size;
                    
                    fileloc = get32bit(elfdata,0x38+(i*32));
                    memloc = get32bit(elfdata,0x38+(i*32)+4);
                    size = get32bit(elfdata,0x38+(i*32)+12);
                    
                    byte[] chunk = new byte[size];
                    //Simple memcpy now!
                    for(int y=0;y<size;y++){
                        chunk[y] = elfdata[fileloc+y];
                    }
                    board.writeMem(memloc,chunk);
                }
                
                board.setPC(entrypoint); //At this point, the entire ELF should be loaded successfully.
                //I set the entry point last, in case something goes wrong.
            }
            else{
                System.out.println("Not ELF!");
                retval=false;
            }
            
        }catch (Exception e){
            System.out.println("Error opening file, or something.");
            e.printStackTrace();
            retval = false;
        }
        
        return retval;
    }
    
    /* Get a 32-bit Big endian value from the array, at the location specified. Put it into an integer.
     *@param arr An array of bytes
     *@param loc Where you want to get the 32-bit value from
     *@return The 32-bit value at that location in the array
     */
    public static int get32bit(byte[] arr, int loc){
        int b3,b2,b1,b0;
        b3=arr[loc];
        b2=arr[loc+1];
        b1=arr[loc+2];
        b0=arr[loc+3];
        
        if(b3 < 0)
            b3+=256;
        if(b2 < 0)
            b2+=256;
        if(b1 < 0)
            b1+=256;
        if(b0 < 0)
            b0+=256;
        
        b3 <<= 24;
        b2 <<= 16;
        b1 <<= 8;
        
        return (b3+b2+b1+b0);
    }
    
    /** Load a binary file to the memory address specified.
     *@param binFile The binary file that you would like to load.
     *@param board The HC11 device to load the executable code onto.
     *@param addr The address to load the binary file to
     *@return True if loaded successfully, False if unable to load for some reason.
     */
    public static boolean loadBinary(File binFile, hc11_Device board, int addr){
        FileInputStream codereader;
        boolean retval = true;
        
        try{
            byte[] data = new byte[(int)(binFile.length())];
            codereader = new FileInputStream(binFile);
            codereader.read(data);
            codereader.close();
            
            board.writeMem(addr,data);
        }catch (Exception e){
            System.out.println("Error opening file, or something.");
            e.printStackTrace();
            retval = false;
        }
        
        return retval;
    }
    
    /** Load an S19 file into the device provided, and set PC to the entry point.
     *@param s19File The S19 file that you would like to load.
     *@param board The HC11 device to load the executable code onto.
     *@return True if loaded successfully, False if the input file is not S19, or some other problem occurs.
     */
    public static boolean loadS19(File s19File, hc11_Device board){
        Scanner codereader;
        boolean retval = true;
        
        try{
            int entrypoint;
            codereader = new Scanner(s19File);
            
            /** S19 files are basically just text files, which is a complete waste of space.
             *So we read them with a Scanner, just like text files. */
            while(codereader.hasNextLine()){
                String line = codereader.nextLine();
                if(line.length()>0){
                    if(line.charAt(0)=='S'){
                        if(line.charAt(1)=='1'){
                            //This line contains data
                            int size,loc,curval,curpos=0;
                            size = Integer.parseInt(line.substring(2,4),16);
                            loc = Integer.parseInt(line.substring(4,8),16);
                            size-=2;
                            while(size>0){
                                curval = Integer.parseInt(line.substring(8+(curpos*2),10+(curpos*2)),16);
                                board.writeMem(loc+curpos,curval);
                                curpos++;
                                size--;
                            }
                        }
                        else if(line.charAt(1)=='9'){
                            //This line contains the entry point, and should be treated as the end of the file, even when it isn't.
                            //This line contains data
                            int entrypt = Integer.parseInt(line.substring(4,8),16);
                            
                            board.setPC(entrypt);
                            
                            break; //End of file
                        }
                        //Otherwise we ignore the line
                    }
                    else{
                        System.out.println("Not a valid .s19 file");
                        retval=false;
                        break;
                    }
                }
            }
            
            codereader.close();
        }catch (Exception e){
            System.out.println("Error opening file, or something.");
            e.printStackTrace();
            retval = false;
        }
        
        return retval;
    }
    
}
