/*
 * hc11instructions.java
 *
 * Created on May 4, 2006, 12:44 PM
 */

package hc11emulator;

/**
 * All of the HC11 instructions are executed by this class. Each instruction has its own function.
 * @author Paul Kratt
 */
public class hc11_Instructions {
    private final hc11_Device device; //This instruction executor's host hardware
    private static final int INH=0,IMM=1,DIR=2,EXT=3,INDX=4,INDY=5,IMMA=6,IMMB=7;
    
    /** Creates a new instance of hc11instructions 
     @param device The HC11 device I'm executing instructions for
    */
    public hc11_Instructions(hc11_Device device) {
        this.device = device;
    }
    
    /** No Operation **/
    public void nop(){
        device.addCycles(2);
    }
    
    /** Clear overflow flag **/
    public void clv(){
        device.clearCCRbits((byte)(2));
        device.addCycles(2);
    }
    
    /** Set overflow flag **/
    public void sev(){
        device.setCCRbits((byte)(2));
        device.addCycles(2);
    }
    
    /** Clear carry flag **/
    public void clc(){
        device.clearCCRbits((byte)(1));
        device.addCycles(2);
    }
    
    /** Set carry flag **/
    public void sec(){
        device.setCCRbits((byte)(1));
        device.addCycles(2);
    }
    
    /** Clear the interrupt flag, enabling interupts. **/
    public void cli(){
        device.clearCCRbits((byte)(0x10));
        device.addCycles(2);
    }
    
    /** Set the interrupt flag, disables interrupts **/
    public void sei(){
        device.setCCRbits((byte)(0x10));
        device.addCycles(2);
    }
    
    /** Subtract B from A, putting the result into A. Also used for CBA. **/
    public void sba(boolean cba){
        int a = device.getA();
        int b = device.getB();
        int result = a-b;
        
        if(result < 0){
            result+=256;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow8(a, b, result);
        
        updateCCR8(result);
        if(!cba)
            device.setA(result);
        device.addCycles(2);
    }
    
    /** Branch if bits set **/
    public void brset(int mode){
        int offset = device.nextInstruct();
        int mask = device.nextInstruct();
        int jumprel = device.nextInstruct();
        
        if(mode==DIR){
            offset = device.getMem(offset);
            device.addCycles(6);
        }
        else if(mode==INDX){
            offset = device.getMem(device.getX()+offset);
            device.addCycles(7);
        }
        else if(mode==INDY){
            offset = device.getMem(device.getY()+offset);
            device.addCycles(8);
        }
        
        if(jumprel >= 128){
            jumprel -= 256;
        }
        
        offset &= mask;
        if(offset == mask){
            device.setPC(device.getPC()+jumprel);
        }
    }
    
    /** Branch if bits cleared  **/
    public void brclr(int mode){
        int offset = device.nextInstruct();
        int mask = device.nextInstruct();
        int jumprel = device.nextInstruct();
        
        if(mode==DIR){
            offset = device.getMem(offset);
            device.addCycles(6);
        }
        else if(mode==INDX){
            offset = device.getMem(device.getX()+offset);
            device.addCycles(7);
        }
        else if(mode==INDY){
            offset = device.getMem(device.getY()+offset);
            device.addCycles(8);
        }
        
        if(jumprel >= 128){
            jumprel -= 256;
        }
        
        offset &= mask;
        if(offset == 0){
            device.setPC(device.getPC()+jumprel);
        }
    }
    
    /** Bit set **/
    public void bset(int mode){
        int offset = device.nextInstruct();
        int value;
        int mask = device.nextInstruct();
        
        if(mode==DIR){
            value = device.getMem(offset);
            device.addCycles(6);
        }
        else if(mode==INDX){
            value = device.getMem(device.getX()+offset);
            device.addCycles(7);
        }
        else{ //INDY
            value = device.getMem(device.getY()+offset);
            device.addCycles(8);
        }
        
        value |= mask;
        
        if(mode==DIR){
            device.writeMem(offset,value);
        }
        else if(mode==INDX){
            device.writeMem(device.getX()+offset,value);
        }
        else if(mode==INDY){
            device.writeMem(device.getY()+offset,value);
        }
        
        updateCCR8(value); //Update N and Z
        device.clearCCRbits((byte)(2)); //Clear overflow bit
    }
    
    /** Bit clear **/
    public void bclr(int mode){
        int offset = device.nextInstruct();
        int value;
        int mask = device.nextInstruct();
        
        if(mode==DIR){
            value = device.getMem(offset);
            device.addCycles(6);
        }
        else if(mode==INDX){
            value = device.getMem(device.getX()+offset);
            device.addCycles(7);
        }
        else{ //INDY
            value = device.getMem(device.getY()+offset);
            device.addCycles(8);
        }
        
        value &= (~mask);
        
        if(mode==DIR){
            device.writeMem(offset,value);
        }
        else if(mode==INDX){
            device.writeMem(device.getX()+offset,value);
        }
        else if(mode==INDY){
            device.writeMem(device.getY()+offset,value);
        }
        
        updateCCR8(value); //Update N and Z
        device.clearCCRbits((byte)(2)); //Clear overflow bit
    }
    
    /** Decimal Adjust A for Binary Coded Decimal - TODO: Fix this! **/
    public void daa(){
        int value = device.getA();
        
        if(((device.getCCR()&1) == 1) || (value >= 0x9A)){
            value += 0x66; //Carry bit was set, this is the result of a big addition.
            value&=0xFF; //Clamp it.
            device.setCCRbits((byte)(3));
            if((value & 0xF) > 9) //This value needs adjusting!
                value-=6;
        }
        else{
            device.clearCCRbits((byte)(3));
        }
        if((value & 0xF) > 9){ //This value needs adjusting!
            value+=6;
        }
                
        updateCCR8(value); //Update negative and zero bits
        device.setA(value);
        device.addCycles(2);
    }
    
    /** Add B to A, putting the result into A **/
    public void aba(){
        int result = device.getA() + device.getB();
        
        if(((device.getA()&0xF) + (device.getB()&0xF)) > 0xF)
            device.setCCRbits((byte)(32)); //Set the Half-Carry bit
        else
            device.clearCCRbits((byte)(32)); //Clear the Half-Carry bit
        
        if(result >= 256){
            device.setCCRbits((byte)(3)); //Set Carry and overflow
            result-=256;
        }
        else
            device.clearCCRbits((byte)(3)); //Clear carry and overflow
        
        updateCCR8(result);
        device.setA(result);
        device.addCycles(2);
    }
    
    /** Add B to X, putting the result into X **/
    public void abx(){
        int result = device.getB() + device.getX();
        
        //No CCR's get updated this time, but we still need to properly handle overflow
        if(result > 0xFFFF)
            result -= 0x10000;
        
        device.setX(result);
        device.addCycles(3);
    }
    
    /** Add B to Y, putting the result into X **/
    public void aby(){
        int result = device.getB() + device.getY();
        
        //No CCR's get updated this time, but we still need to properly handle overflow
        if(result > 0xFFFF)
            result -= 0x10000;
        
        device.setY(result);
        device.addCycles(4);
    }
    
    /** Logical shift right accumulator D. **/
    public void lsrd(){
        int value = device.getD();
        
        if((value&1)==1)
            device.setCCRbits((byte)(1)); //Carry bit
        else
            device.clearCCRbits((byte)(1));
        //I don't quite understand how this can "overflow." I'll assume it means going from A into B.
        if((value&0x100)==0x100)
            device.setCCRbits((byte)(2));
        else
            device.clearCCRbits((byte)(2));
        
        value >>=1; //Right shift once
        updateCCR16(value);
        
        device.setD(value);
        device.addCycles(3);
    }
    
    /** Logical/Arithmetically shift left accumulator D. **/
    public void lsld(){
        int value = device.getD();
        
        if((value&0x8000)==0x8000)
            device.setCCRbits((byte)(1)); //Carry bit
        else
            device.clearCCRbits((byte)(1));
        //I'll assume overflow means going from A into B again.
        if((value&0x80)==0x80)
            device.setCCRbits((byte)(2));
        else
            device.clearCCRbits((byte)(2));
        
        value <<=1; //Left shift once
        value &= 0xFFFF; //Clean up the value
        updateCCR16(value);
        
        device.setD(value);
        device.addCycles(3);
    }
    
    /** Integer division! How fun! **/
    public void idiv(){
        int resx, resd;
        
        if(device.getX() > 0){
            resx = device.getD() / device.getX();
            resd = device.getD() % device.getX();
        }
        else{
            resd = device.getX();
            resx = 0; //Divide by zero means 0 remainder X?
        }
        
        if(resx == 0)
            device.setCCRbits((byte)(4)); //Set the zero bit
        else
            device.clearCCRbits((byte)(4)); //Clear the zero bit
        if(resd != 0)
            device.setCCRbits((byte)(1)); //Set the carry bit
        else
            device.clearCCRbits((byte)(1)); //Clear the carry bit
        device.clearCCRbits((byte)(2)); //Clear the Overflow bit!
        device.setD(resd);
        device.setX(resx);
        device.addCycles(41);
    }
    
    /** Fractional division! Even more fun... oh wait. **/
    public void fdiv(){
        int resx, resd;
        
        if(device.getX() > 0){
            resx = (device.getD() * 0x10000) / device.getX();
            if(resx > 0xFFFF){
                resx &= 0xFFFF;
                device.setCCRbits((byte)(2)); //Overflow!!!
            }
            else{
                device.clearCCRbits((byte)(2)); //Clear the Overflow bit!
            }
            resd = device.getD() % device.getX();
        }
        else{
            resd = device.getX();
            resx = 0; //Divide by zero means 0 remainder X?
        }
        
        if(resx == 0)
            device.setCCRbits((byte)(4)); //Set the zero bit
        else
            device.clearCCRbits((byte)(4)); //Clear the zero bit
        if(resd != 0)
            device.setCCRbits((byte)(1)); //Set the carry bit
        else
            device.clearCCRbits((byte)(1)); //Clear the carry bit
        
        device.setD(resd);
        device.setX(resx);
        device.addCycles(41);
    }
    
    /** Branch if carry bit clear. Also is Branch if higher or same (bhs) */
    public void bcc(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x1)==0){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if carry bit set... Also is blo, branch if lower */
    public void bcs(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x1)==1){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if plus. N=0 */ 
    public void bpl(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x8)==0){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch Always */ 
    public void bra(){
        int offset = device.nextInstruct();
        
        if(offset>=128)
            offset-=256;
        device.setPC(device.getPC()+offset);
        device.addCycles(3);
    }
    
    /** Branch to subroutine */ 
    public void bsr(){
        int offset = device.nextInstruct();
        int retaddr = device.getPC();
        
        if(offset>=128)
            offset-=256;
        
        device.writeMem(device.getSP(),retaddr&0xFF);
        device.writeMem(device.getSP()-1,retaddr>>8);
        device.setSP(device.getSP()-2);
        device.setPC(device.getPC()+offset);
        device.addCycles(6);
    }
    
    /** Jump to subroutine */ 
    public void jsr(int mode){
        int jumpaddr=0;
        int retaddr=0;
        
        if(mode==DIR){
            jumpaddr = device.nextInstruct();
            device.addCycles(5);
        }
        else if (mode==EXT){
            jumpaddr = device.nextInstruct();
            jumpaddr<<=8;
            jumpaddr+=device.nextInstruct();
            device.addCycles(6);
        }
        else if (mode==INDX){
            int place = device.getX();
            place+=device.nextInstruct();
            jumpaddr = device.getMem(place);
            jumpaddr<<=8;
            jumpaddr+=device.getMem(place+1);
            device.addCycles(6);
        }
        else if (mode==INDY){
            int place = device.getY();
            place+=device.nextInstruct();
            jumpaddr = device.getMem(place);
            jumpaddr<<=8;
            jumpaddr+=device.getMem(place+1);
            device.addCycles(7);
        }
        
        retaddr=device.getPC();
        device.writeMem(device.getSP(),retaddr&0xFF);
        device.writeMem(device.getSP()-1,retaddr>>8);
        device.setSP(device.getSP()-2);
        device.setPC(jumpaddr);
    }
    
    /** Return from subroutine */ 
    public void rts(){
        int retaddr = device.getMem(device.getSP()+1);
        retaddr = retaddr << 8;
        retaddr += device.getMem(device.getSP()+2);
        device.setSP(device.getSP()+2);
        
        device.setPC(retaddr);
        device.addCycles(5);
    }
    
    /** Return from interrupt */ 
    public void rti(){
        int ccr = device.getMem(device.getSP()+1);
        int accb = device.getMem(device.getSP()+2);
        int acca = device.getMem(device.getSP()+3);
        int accx = device.getMem(device.getSP()+4);
        accx <<= 8;
        accx += device.getMem(device.getSP()+5);
        int accy = device.getMem(device.getSP()+6);
        accy <<= 8;
        accy += device.getMem(device.getSP()+7);
        int retaddr = device.getMem(device.getSP()+8);
        retaddr = retaddr << 8;
        retaddr += device.getMem(device.getSP()+9);
        device.setSP(device.getSP()+9);
        
        if(ccr > 127)
            ccr-=256;
        
        device.setCCR((byte)ccr);
        device.setA(acca);
        device.setB(accb);
        device.setX(accx);
        device.setY(accy);
        device.setPC(retaddr);
        device.addCycles(12);
    }
    
    /** Multiply A x B, put the result into D **/
    public void mul(){
        int result = device.getA() * device.getB();
        if(result > 0x10000){ //This is IMPOSSIBLE!
            result &= 0xFFFF;
            device.setCCRbits((byte)(1));
        }
        else
            device.clearCCRbits((byte)(1));
        
        device.setD(result);
        device.addCycles(10);
    }
    
    /** Branch Never */ 
    public void brn(){
        int offset = device.nextInstruct();
        
        device.addCycles(3);
    }
    
    /** Branch if minus... N=1 */ 
    public void bmi(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x8)==1){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if equal, the zero bit set */
    public void beq(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x4)==4){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if not equal to zero, Z=0 */
    public void bne(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x4)==0){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if overflow clear, V=0 */
    public void bvc(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x2)==0){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if overflow set, V=1 */
    public void bvs(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x2)==2){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if greater or equal (N ^ V == 0)*/
    public void bge(){
        int offset = device.nextInstruct();
        
        //XOR = 0 if both bits are the same
        if((device.getCCR()&0xA)==0 || (device.getCCR()&0xA)==0xA){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if less than (N ^ V == 1)*/
    public void blt(){
        int offset = device.nextInstruct();
        
        //XOR = 1 if both bits are the different
        if((device.getCCR()&0xA)>0 && (device.getCCR()&0xA)<0xA){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if greater than (Z + (N ^ V) == 0)*/
    public void bgt(){
        int offset = device.nextInstruct();
        
        //XOR = 0 if both bits are the same
        if(((device.getCCR()&0xA)==0 || (device.getCCR()&0xA)==0xA) || (device.getCCR()&0x4)==0){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if greater than (Z + (N ^ V) == 1)*/
    public void ble(){
        int offset = device.nextInstruct();
        
        //XOR = 0 if both bits are the same
        if(((device.getCCR()&0xA)==0xA || (device.getCCR()&0xA)==0xA) || (device.getCCR()&0x4)==4){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if higher (C | Z==0))*/
    public void bhi(){
        int offset = device.nextInstruct();
        
        //XOR = 0 if both bits are the same
        if((device.getCCR()&0x05)==0){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Branch if lower or same (C | Z==1))*/
    public void bls(){
        int offset = device.nextInstruct();
        
        if((device.getCCR()&0x05)>0){
            if(offset>=128)
                offset-=256;
            device.setPC(device.getPC()+offset);
        }
        device.addCycles(3);
    }
    
    /** Push accumulator A onto the stack */
    public void psha(){
        int value=device.getA();
        
        device.writeMem(device.getSP(),value);
        device.setSP(device.getSP()-1);
        device.addCycles(3);
    }
    
    /** Pull accumulator A off of the stack */
    public void pula(){
        device.setSP(device.getSP()+1);
        device.setA(device.getMem(device.getSP()));
         
        device.addCycles(4);
    }
    
    /** Push accumulator B onto the stack */
    public void pshb(){
        int value=device.getB();
        
        device.writeMem(device.getSP(),value);
        device.setSP(device.getSP()-1);
        device.addCycles(3);
    }
    
    /** Pull accumulator B off of the stack */
    public void pulb(){
        device.setSP(device.getSP()+1);
        device.setB(device.getMem(device.getSP()));
         
        device.addCycles(4);
    }
    
    /** Push register X onto the stack */
    public void pshx(){
        int value=device.getX();
        
        device.writeMem(device.getSP(),value&0xFF);
        device.writeMem(device.getSP()-1,value>>8);
        device.setSP(device.getSP()-2);
        device.addCycles(4);
    }
    
    /** Pull register X off of the stack **/
    public void pulx(){
        int value=device.getMem(device.getSP()+1);
        
        value = value << 8;
        value += device.getMem(device.getSP()+2);
        device.setX(value);
        device.setSP(device.getSP()+2);
        device.addCycles(5);
    }
    
    /** Push register Y onto the stack */
    public void pshy(){
        int value=device.getY();
        
        device.writeMem(device.getSP(),value&0xFF);
        device.writeMem(device.getSP()-1,value>>8);
        device.setSP(device.getSP()-2);
        device.addCycles(5);
    }
    
    /** Pull register Y off of the stack **/
    public void puly(){
        int value=device.getMem(device.getSP()+1);
        
        value = value << 8;
        value += device.getMem(device.getSP()+2);
        device.setY(value);
        device.setSP(device.getSP()+2);
        device.addCycles(6);
    }
    
    /** Negate an 8-bit value by 2's complement **/
    public void neg(int mode){
        int value=0;
        int addr=0;
        
        if(mode==IMMA){
            value = device.getA();
            device.addCycles(2);
        }
        else if(mode==IMMB){
            value = device.getB();
            device.addCycles(2);
        }
        else if(mode==EXT){
            addr = device.nextInstruct();
            addr <<= 8;
            addr += device.nextInstruct();
            value = device.getMem(addr);
            device.addCycles(6);
        }
        else if(mode==INDX){
            addr = device.getX()+device.nextInstruct();
            value = device.getMem(addr);
            device.addCycles(6);
        }
        else{ //(mode==INDY)
            addr = device.getY()+device.nextInstruct();
            value = device.getMem(addr);
            device.addCycles(7);
        }
        
        //Now we have our value, let us complement it on its good manners.
        value = (~value) & 0xFF;
        value++; //Add one
        
        //Carry bit
        if(value >= 0x100){
            device.setCCRbits((byte)(1));
            value = value & 0xFF;
        }
        else
            device.clearCCRbits((byte)(1));
        //Overflow if -128
        if(value == 0x80) //-128
            device.setCCRbits((byte)(2));
        else
            device.clearCCRbits((byte)(2));
        
        updateCCR8(value); //Update N and Z bits.
        
        //Return the negated value to its origin
        if(mode==IMMA){
            device.setA(value);
        }
        else if(mode==IMMB){
            device.setB(value);
        }
        else{
            device.writeMem(addr,value);
        }
    }
    
    /** Perform 1's complement on an 8-bit value **/
    public void com(int mode){
        int value=0;
        int addr=0;
        
        if(mode==IMMA){
            value = device.getA();
            device.addCycles(2);
        }
        else if(mode==IMMB){
            value = device.getB();
            device.addCycles(2);
        }
        else if(mode==EXT){
            addr = device.nextInstruct();
            addr <<= 8;
            addr += device.nextInstruct();
            value = device.getMem(addr);
            device.addCycles(6);
        }
        else if(mode==INDX){
            addr = device.getX()+device.nextInstruct();
            value = device.getMem(addr);
            device.addCycles(6);
        }
        else{ //(mode==INDY)
            addr = device.getY()+device.nextInstruct();
            value = device.getMem(addr);
            device.addCycles(7);
        }
        
        //Now we have our value, let us complement it on its good manners.
        value = (~value) & 0xFF;
        
        //Update the CCR
        device.setCCRbits((byte)(1));
        device.clearCCRbits((byte)(2));
        updateCCR8(value); //Update N and Z bits.
        
        //Return the complemented value to its origin
        if(mode==IMMA){
            device.setA(value);
        }
        else if(mode==IMMB){
            device.setB(value);
        }
        else{
            device.writeMem(addr,value);
        }
    }
    
    /** Load Stack Pointer... Set SP to somewhere in memory **/
    public void lds(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==IMM){
            value = get16bitImmediate();
            device.addCycles(3);
        }
        else if(mode==DIR){
            int tempad = get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(4);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(5);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(5);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(6);
        }
        //No else statement, because I want something bad to happen if you don't call this correctly.
        device.setSP(value);
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /** Bitwise Rotate Left **/
    public void rol(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            value = roll_left(value);
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            value = roll_left(value);
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            value = roll_left(value);
            device.writeMem(tempad,value);
            device.addCycles(7);
        }
        else if(mode==IMMA){
            value = device.getA();
            value = roll_left(value);
            device.setA(value);
            device.addCycles(2);
        }
        else if(mode==IMMB){
            value = device.getB();
            value = roll_left(value);
            device.setB(value);
            device.addCycles(2);
        }
    }
    
    /** Logical Shift Right **/
    public void lsr(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            value = roll_right(value);
            value &= 0x7F;
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            value = roll_right(value);
            value &= 0x7F;
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            value = roll_right(value);
            value &= 0x7F;
            device.writeMem(tempad,value);
            device.addCycles(7);
        }
        else if(mode==IMMA){
            value = device.getA();
            value = roll_right(value);
            value &= 0x7F;
            device.setA(value);
            device.addCycles(2);
        }
        else if(mode==IMMB){
            value = device.getB();
            value = roll_right(value);
            value &= 0x7F;
            device.setB(value);
            device.addCycles(2);
        }
        updateCCR8(value);
    }
    
    /** Arithmetically Shift Right **/
    public void asr(int mode){
        int value=0;
        int tempad=0;
        boolean negative = false;
        
        //Grab the operand
        if(mode==EXT){
            tempad = get16bitImmediate();
            value = device.getMem(tempad);
        }
        else if(mode==INDX){
            tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
        }
        else if(mode==INDY){
            tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
        }
        else if(mode==IMMA){
            value = device.getA();
        }
        else if(mode==IMMB){
            value = device.getB();
        }
        
        if((value&0x80)==0x80){
            negative = true;
            value &= 0x7F;
        }
        if((value&1)==1)
            device.setCCRbits((byte)(1));
        else
            device.clearCCRbits((byte)(1));
        value >>= 1;
        if(negative)
            value |= 0x80;
        
        //Grab the operand
        if(mode==EXT){
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDX){
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDY){
            device.writeMem(tempad,value);
            device.addCycles(7);
        }
        else if(mode==IMMA){
            device.setA(value);
            device.addCycles(2);
        }
        else if(mode==IMMB){
            device.setB(value);
            device.addCycles(2);
        }
        
        updateCCR8(value);
    }
    
    /** Arithmetically Shift Left **/
    public void asl(int mode){
        int value=0;
        int tempad=0;
        
        //Grab the operand
        if(mode==EXT){
            tempad = get16bitImmediate();
            value = device.getMem(tempad);
        }
        else if(mode==INDX){
            tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
        }
        else if(mode==INDY){
            tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
        }
        else if(mode==IMMA){
            value = device.getA();
        }
        else if(mode==IMMB){
            value = device.getB();
        }
        
        if((value&0x40)==0x40)
            device.setCCRbits((byte)(2));
        else
            device.clearCCRbits((byte)(2));
        if((value&0x80)==0x80)
            device.setCCRbits((byte)(1));
        else
            device.clearCCRbits((byte)(1));
        value <<= 1;
        value &= 0xFF;
        
        //Write the result back
        if(mode==EXT){
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDX){
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDY){
            device.writeMem(tempad,value);
            device.addCycles(7);
        }
        else if(mode==IMMA){
            device.setA(value);
            device.addCycles(2);
        }
        else if(mode==IMMB){
            device.setB(value);
            device.addCycles(2);
        }
        
        updateCCR8(value);
    }
    
    /** Bitwise Rotate Right **/
    public void ror(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            value = roll_right(value);
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            value = roll_right(value);
            device.writeMem(tempad,value);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            value = roll_right(value);
            device.writeMem(tempad,value);
            device.addCycles(7);
        }
        else if(mode==IMMA){
            value = device.getA();
            value = roll_right(value);
            device.setA(value);
            device.addCycles(2);
        }
        else if(mode==IMMB){
            value = device.getB();
            value = roll_right(value);
            device.setB(value);
            device.addCycles(2);
        }
    }
    
    /** Roll the current value to the left and return the rolled value.
     * Updates the carry bit accordingly.
     */
    private int roll_left(int value){
        int tempval = value;
        boolean carrybit = ((device.getCCR()&1)==1);
        //Now we have the value sitting in value.
        if((tempval & 0x80) == 0x80){ //Left most bit is set!!
            device.setCCRbits((byte)(1)); //Turn on the carry bit
        }
        else{
            device.clearCCRbits((byte)(1)); //We need to clear the carry bit!!!
        }
        tempval = (tempval << 1) & 0xFF;
        if(carrybit) //Carry bit was set
            tempval += 1;
        return tempval;
    }
    
    /** Roll the current value to the right and return the rolled value.
     * Updates the carry bit accordingly.
     */
    private int roll_right(int value){
        int tempval = value;
        boolean carrybit = ((device.getCCR()&1)==1);
        //Now we have the value sitting in value.
        if((tempval & 0x01) == 1){ //Right most bit is set!!
            device.setCCRbits((byte)(1)); //Turn on the carry bit
        }
        else
            device.clearCCRbits((byte)(1)); //Clear the carry bit.
        tempval = (tempval >> 1);
        if(carrybit) //Carry bit was set
            tempval |= 0x80;
        return tempval;
    }
    
    /** Jump to somewhere else **/
    public void jmp(int mode){
        int jumploc=0;
        
        if(mode==EXT){
            jumploc=device.nextInstruct();
            jumploc<<=8;
            jumploc+=device.nextInstruct();
            device.addCycles(3);
        }
        else if(mode==INDX){
            jumploc = device.nextInstruct();
            jumploc+=device.getX();
            device.addCycles(3);
        }
        else if(mode==INDY){
            jumploc = device.nextInstruct();
            jumploc+=device.getY();
            device.addCycles(4);
        }
        device.setPC(jumploc);
    }
    
    /** Subtract from Accumulator A **/
    public void suba(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        int a = device.getA();
        result = a - subval;
        if(result < 0){
            result+=256;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow8(a, subval, result);
        updateCCR8(result);
        device.setA(result);
    }
    
    /** Compare Accumulator A **/
    public void cmpa(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        int a = device.getA();
        result = a - subval;
        if(result < 0){
            result+=256;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow8(a, subval, result);
        updateCCR8(result);
    }
    
    /** Subtract with carry from Accumulator A **/
    public void sbca(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
       
        int a = device.getA();
        result = a - subval;
        if((device.getCCR()&1)==1)
            result--; //Carrry bit set, subtract one more.
        if(result < 0){
            result+=256;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow8(a, subval, result);
        updateCCR8(result);
        device.setA(result);
    }
    
    /** Subtract from Accumulator B **/
    public void subb(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        int b = device.getB();
        result = b - subval;
        if(result < 0){
            result+=256;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow8(b, subval, result);
        updateCCR8(result);
        device.setB(result);
    }

    /** Compare Accumulator B **/
    public void cmpb(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        int b = device.getB();
        result = b - subval;
        
        if(result < 0){
            result+=256;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow8(b, subval, result);
        updateCCR8(result);
    }
    
    /** Subtract with carry from Accumulator B **/
    public void sbcb(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        int b = device.getB();
        result = b - subval;
        if((device.getCCR()&1)==1)
            result--; //Subtract carry bit if it's set.
        if(result < 0){
            result+=256;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow8(b, subval, result);
        updateCCR8(result);
        device.setB(result);
    }
    
    /** Bitwise AND accumulator A **/
    public void anda(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval = device.getA() & subval;
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
        device.setA(subval);
    }
    
    /** Bitwise AND accumulator B **/
    public void andb(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval = device.getB() & subval;
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
        device.setB(subval);
    }
    
    /** Bitwise test accumulator A **/
    public void bita(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval = device.getA() & subval;
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
    }
    
    /** Bitwise test accumulator B **/
    public void bitb(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval = device.getB() & subval;
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
    }
    
    /** XOR accumulator A **/
    public void eora(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval ^= device.getA();
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
        device.setA(subval);
    }
    
    /** XOR accumulator B **/
    public void eorb(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval ^= device.getB();
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
        device.setB(subval);
    }
    
    /** Bitwise OR accumulator A **/
    public void oraa(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval |= device.getA();
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
        device.setA(subval);
    }
    
    /** Bitwise AND accumulator B **/
    public void orab(int mode){
        int subval;
        
        if(mode==IMM){
            subval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            subval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            subval = device.getX();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            subval = device.getY();
            subval+=device.nextInstruct();
            subval = device.getMem(subval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            subval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        subval |= device.getB();
        device.clearCCRbits((byte)(2)); //Clear V
        updateCCR8(subval);
        device.setB(subval);
    }
    
    /** Add to Accumulator A **/
    public void adda(int mode){
        int addval;
        
        if(mode==IMM){
            addval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            addval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            addval = device.getX();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            addval = device.getY();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            addval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        if((device.getA()&0xF)+(addval&0xF)>0xF)
            device.setCCRbits((byte)(0x20)); //Set Half Carry bit
        else
            device.clearCCRbits((byte)(0x20));
        
        addval = device.getA() + addval;
        if(addval > 255){
            addval-=256;
            device.setCCRbits((byte)(3)); //Set V and C
        }
        else
            device.clearCCRbits((byte)(3)); //Clear V and C
        updateCCR8(addval);
        device.setA(addval);
    }
    
    /** Add with carry to Accumulator A **/
    public void adca(int mode){
        int addval;
        
        if(mode==IMM){
            addval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            addval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            addval = device.getX();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            addval = device.getY();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            addval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        if((device.getA()&0xF)+(addval&0xF)+(device.getCCR()&1)>0xF)
            device.setCCRbits((byte)(0x20)); //Set Half Carry bit
        else
            device.clearCCRbits((byte)(0x20));
        
        addval = device.getA() + addval + (device.getCCR()&1);
        if(addval > 255){
            addval-=256;
            device.setCCRbits((byte)(3)); //Set V and C
        }
        else
            device.clearCCRbits((byte)(3)); //Clear V and C
        updateCCR8(addval);
        device.setA(addval);
    }
    
    /** Add to Accumulator B **/
    public void addb(int mode){
        int addval;
        
        if(mode==IMM){
            addval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            addval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            addval = device.getX();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            addval = device.getY();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            addval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        if((device.getB()&0xF)+(addval&0xF)>0xF)
            device.setCCRbits((byte)(0x20)); //Set Half Carry bit
        else
            device.clearCCRbits((byte)(0x20));
        
        addval = device.getB() + addval;
        if(addval > 255){
            addval-=256;
            device.setCCRbits((byte)(3)); //Set V and C
        }
        else
            device.clearCCRbits((byte)(3)); //Clear V and C
        updateCCR8(addval);
        device.setB(addval);
    }
    
    /** Add with carry to Accumulator B **/
    public void adcb(int mode){
        int addval;
        
        if(mode==IMM){
            addval = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            addval = device.getMem(get8bitImmediate());
            device.addCycles(3);
        }
        else if(mode==INDX){
            addval = device.getX();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(4);
        }
        else if(mode==INDY){
            addval = device.getY();
            addval+=device.nextInstruct();
            addval = device.getMem(addval);
            device.addCycles(5);
        }
        else{ //MODE==EXT
            addval = device.getMem(get16bitImmediate());
            device.addCycles(4);
        }
        
        if((device.getB()&0xF)+(addval&0xF)+(device.getCCR()&1)>0xF)
            device.setCCRbits((byte)(0x20)); //Set Half Carry bit
        else
            device.clearCCRbits((byte)(0x20));
        
        addval = device.getB() + addval + (device.getCCR()&1);
        if(addval > 255){
            addval-=256;
            device.setCCRbits((byte)(3)); //Set V and C
        }
        else
            device.clearCCRbits((byte)(3)); //Clear V and C
        updateCCR8(addval);
        device.setB(addval);
    }
    
    /** Subtract from Accumulator D **/
    public void subd(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get16bitImmediate();
            device.addCycles(4);
        }
        else if(mode==DIR){
            int temploc=get8bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(5);
        }
        else if(mode==INDX){
            int temploc = device.getX();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int temploc = device.getY();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(7);
        }
        else{ //MODE==EXT
            int temploc=get16bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(6);
        }
        
        int d = device.getD();
        result = d - subval;
        
        if(result < 0){
            result+=65536;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow16(d, subval, result);
        updateCCR16(result);
        device.setD(result);
    }
    
    /** Add to Accumulator D **/
    public void addd(int mode){
        int addval;
        
        if(mode==IMM){
            addval = get16bitImmediate();
            device.addCycles(4);
        }
        else if(mode==DIR){
            int temploc=get8bitImmediate();
            addval = device.getMem(temploc);
            addval<<=8;
            addval += device.getMem(temploc+1);
            device.addCycles(5);
        }
        else if(mode==INDX){
            int temploc = device.getX();
            temploc+=device.nextInstruct();
            addval = device.getMem(temploc);
            addval<<=8;
            addval+=device.getMem(temploc+1);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int temploc = device.getY();
            temploc+=device.nextInstruct();
            addval = device.getMem(temploc);
            addval<<=8;
            addval+=device.getMem(temploc+1);
            device.addCycles(7);
        }
        else{ //MODE==EXT
            int temploc=get16bitImmediate();
            addval = device.getMem(temploc);
            addval<<=8;
            addval += device.getMem(temploc+1);
            device.addCycles(6);
        }
        
        addval = device.getD() + addval;
        if(addval > 65535){
            addval-=65536;
            device.setCCRbits((byte)(3)); //Set V and C
        }
        else
            device.clearCCRbits((byte)(3)); //Clear V and C
        updateCCR16(addval);
        device.setD(addval);
    }
    
    /** Compare Accumulator D **/
    public void cmpd(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get16bitImmediate();
            device.addCycles(5);
        }
        else if(mode==DIR){
            int temploc=get8bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int temploc = device.getX();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(7);
        }
        else if(mode==INDY){
            int temploc = device.getY();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(7);
        }
        else{ //MODE==EXT
            int temploc=get16bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(7);
        }
        
        int d = device.getD();
        result = d - subval;
        
        if(result < 0){
            result+=65536;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow16(d, subval, result);
        updateCCR16(result);
    }
    
    /** Compare Accumulator X **/
    public void cmpx(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get16bitImmediate();
            device.addCycles(4);
        }
        else if(mode==DIR){
            int temploc=get8bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(5);
        }
        else if(mode==INDX){
            int temploc = device.getX();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int temploc = device.getY();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(7);
        }
        else{ //MODE==EXT
            int temploc=get16bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(6);
        }
        
        int x = device.getX();
        result = x - subval;
        if(result < 0){
            result+=65536;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow16(x, subval, result);
        updateCCR16(result);
    }
    
    /** Compare Accumulator Y **/
    public void cmpy(int mode){
        int subval,result;
        
        if(mode==IMM){
            subval = get16bitImmediate();
            device.addCycles(5);
        }
        else if(mode==DIR){
            int temploc=get8bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int temploc = device.getX();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(7);
        }
        else if(mode==INDY){
            int temploc = device.getY();
            temploc+=device.nextInstruct();
            subval = device.getMem(temploc);
            subval<<=8;
            subval+=device.getMem(temploc+1);
            device.addCycles(7);
        }
        else{ //MODE==EXT
            int temploc=get16bitImmediate();
            subval = device.getMem(temploc);
            subval<<=8;
            subval += device.getMem(temploc+1);
            device.addCycles(7);
        }
        
        int y = device.getY();
        result = y - subval;
        if(result < 0){
            result+=65536;
            device.setCCRbits((byte)(1)); //Set C
        }
        else
            device.clearCCRbits((byte)(1)); //Clear C
        
        updateOverflow16(y, subval, result);
        updateCCR16(result);
    }
    
    /** Load accumulator D **/
    public void ldd(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==IMM){
            value = get16bitImmediate();
            device.addCycles(3);
        }
        else if(mode==DIR){
            int tempad = get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(4);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(5);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(5);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(6);
        }
        //No else statement, because I want something bad to happen if you don't call this correctly.
        device.setD(value);
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /** Load accumulator A **/
    public void ldaa(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==IMM){
            value = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            int tempad = get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(3);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(4);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(4);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(5);
        }
        //No else statement, because I want something bad to happen if you don't call this correctly.
        device.setA(value);
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /** Clear an 8-bit value **/
    public void clr(int mode){
        if(mode==EXT){
            int tempad = get16bitImmediate();
            device.writeMem(tempad,0);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            device.writeMem(tempad,0);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            device.writeMem(tempad,0);
            device.addCycles(7);
        }
        else if(mode==IMMA){
            device.setA(0);
            device.addCycles(2);
        }
        else if(mode==IMMB){
            device.setB(0);
            device.addCycles(2);
        }
        
        //Update CCR
        device.setCCRbits((byte)0x04);
        device.clearCCRbits((byte)0xB);
    }
    
    /** Increment 8-bit value by one **/
    public void inc(int mode){
        int ramaddr = 0;
        int value = 0;
        byte ccrupdate=0;
        
        if(mode==EXT){
            ramaddr = get16bitImmediate();
            value = device.getMem(ramaddr);
            device.addCycles(6);
        }
        else if(mode==INDX){
            ramaddr = device.getX()+get8bitImmediate();
            value = device.getMem(ramaddr);
            device.addCycles(6);
        }
        else if(mode==INDY){
            ramaddr = device.getY()+get8bitImmediate();
            value = device.getMem(ramaddr);
            device.addCycles(7);
        }
        
        value++; //Add one to the value.
        if(value == 256){
            ccrupdate |= 6;
            value = 0;
        }
        if(value >= 128)
            ccrupdate |= 8;
        
        //Write back the value
        device.writeMem(ramaddr,value);
        
        //Update CCR
        device.clearCCRbits((byte)0xE);
        device.setCCRbits(ccrupdate);
    }
    
    /** Decrement 8-bit value by one **/
    public void dec(int mode){
        int ramaddr = 0;
        int value = 0;
        byte ccrupdate=0;
        
        if(mode==EXT){
            ramaddr = get16bitImmediate();
            value = device.getMem(ramaddr);
            device.addCycles(6);
        }
        else if(mode==INDX){
            ramaddr = device.getX()+get8bitImmediate();
            value = device.getMem(ramaddr);
            device.addCycles(6);
        }
        else if(mode==INDY){
            ramaddr = device.getY()+get8bitImmediate();
            value = device.getMem(ramaddr);
            device.addCycles(7);
        }
        
        value--; //Subtract one from the value.
        if(value == -1){
            ccrupdate |= 0x2;
            value = 255;
        }
        if(value >= 128)
            ccrupdate |= 8;
        if(value == 0)
            ccrupdate |= 4;
        
        //Write back the value
        device.writeMem(ramaddr,value);
        
        //Update CCR
        device.clearCCRbits((byte)0xE);
        device.setCCRbits(ccrupdate);
    }
    
    /** Increment Accumulator A by one **/
    public void inca(){
        int value = 0;
        byte ccrupdate=0;
        
        value = device.getA();
        device.addCycles(2);
                
        value++; //Add one to the value.
        if(value == 256){
            ccrupdate |= 6;
            value = 0;
        }
        if(value >= 128)
            ccrupdate |= 8;
        
        //Write back the value
        device.setA(value);
        
        //Update CCR
        device.clearCCRbits((byte)0xE);
        device.setCCRbits(ccrupdate);
    }
    
    /** Decrement Accumulator A by one **/
    public void deca(){
        int value = 0;
        byte ccrupdate=0;
        
        value = device.getA();
        device.addCycles(2);
                
        value--; //Subtract one from the value.
        if(value == -1){
            ccrupdate |= 0x2;
            value = 255;
        }
        if(value >= 128)
            ccrupdate |= 8;
        if(value == 0)
            ccrupdate |= 4;
        
        //Write back the value
        device.setA(value);
        
        //Update CCR
        device.clearCCRbits((byte)0xE);
        device.setCCRbits(ccrupdate);
    }
    
    /** Increment Accumulator B by one **/
    public void incb(){
        int value = 0;
        byte ccrupdate=0;
        
        value = device.getB();
        device.addCycles(2);
        
        value++; //Add one to the value.
        if(value == 256){
            ccrupdate |= 6;
            value = 0;
        }
        if(value >= 128)
            ccrupdate |= 8;
        
        //Write back the value
        device.setB(value);
        
        //Update CCR
        device.clearCCRbits((byte)0xE);
        device.setCCRbits(ccrupdate);
    }
    
    /** Decrement Accumulator B by one **/
    public void decb(){
        int value = 0;
        byte ccrupdate=0;
        
        value = device.getB();
        device.addCycles(2);
                
        value--; //Subtract one from the value.
        if(value == -1){
            ccrupdate |= 0x2;
            value = 255;
        }
        if(value >= 128)
            ccrupdate |= 8;
        if(value == 0)
            ccrupdate |= 4;
        
        //Write back the value
        device.setB(value);
        
        //Update CCR
        device.clearCCRbits((byte)0xE);
        device.setCCRbits(ccrupdate);
    }
    
    /** Increment the stack pointer **/
    public void ins(){
        int value = device.getSP();
        byte ccrupdate=0;
        
        device.addCycles(3);
                
        value++; //Add one to the value.
        if(value == 65536)
            value = 0;
        
        //Write back the value
        device.setSP(value);
    }
    
    /** Decrement the stack pointer **/
    public void des(){
        int value = device.getSP();
        byte ccrupdate=0;
        
        device.addCycles(3);
                
        value--; //Add one to the value.
        if(value == -1)
            value = 65535;
        
        //Write back the value
        device.setSP(value);
    }
    
    /** Increment X **/
    public void inx(){
        int value = device.getX();
        byte ccrupdate=0;
        
        device.addCycles(3);
                
        value++; //Add one to the value.
        if(value == 65536){
            value = 0;
            device.setCCRbits((byte)(4));
        }
        
        //Write back the value
        device.setX(value);
    }
    
    /** Increment Y **/
    public void iny(){
        int value = device.getY();
        byte ccrupdate=0;
        
        device.addCycles(4);
        
        value++; //Add one to the value.
        if(value == 65536){
            value = 0;
            device.setCCRbits((byte)(4));
        }
        
        //Write back the value
        device.setY(value);
    }
    
    /** Decrement X **/
    public void dex(){
        int value = device.getX();
        
        device.addCycles(3);
                
        value--; //Subtract one from the value.
        if(value == -1){
            value = 0xFFFF;
        }
        if(value == 0)
            device.setCCRbits((byte)(4));
        
        //Write back the value
        device.setX(value);
    }
    
    /** Decrement Y **/
    public void dey(){
        int value = device.getY();
        
        device.addCycles(4);
        
        value--; //Subtract one from the value.
        if(value == -1){
            value = 0xFFFF;
        }
        if(value == 0)
            device.setCCRbits((byte)(4));
        
        //Write back the value
        device.setY(value);
    }
    
    /** Store accumulator A **/
    public void staa(int mode){
        int value=device.getA();
        
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
        
        //Grab the operand
        if(mode==DIR){
            int tempad = get8bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(3);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(4);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(4);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(5);
        }
    }
    
    /** Store accumulator B **/
    public void stab(int mode){
        int value=device.getB();
        
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
        
        //Grab the operand
        if(mode==DIR){
            int tempad = get8bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(3);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(4);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(4);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            device.writeMem(tempad,value);
            device.addCycles(5);
        }
    }
    
    /** Store accumulator D - Sexually transmitted disease **/
    public void std(int mode){
        int value=device.getD();
        
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
        
        //Grab the operand
        if(mode==DIR){
            int tempad = get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(4);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(5);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(5);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(6);
        }
    }
    
    /** Store stack pointer **/
    public void sts(int mode){
        int value=device.getSP();
        
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
        
        //Grab the operand
        if(mode==DIR){
            int tempad = get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(4);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(5);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(5);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(6);
        }
    }
    
    /** Store accumulator X **/
    public void stx(int mode){
        int value=device.getX();
        
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
        
        //Grab the operand
        if(mode==DIR){
            int tempad = get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(4);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(5);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(5);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(6);
        }
    }
    
    /** Store accumulator Y **/
    public void sty(int mode){
        int value=device.getY();
        
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
        
        //Grab the operand
        if(mode==DIR){
            int tempad = get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(5);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(6);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(6);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            device.writeMem(tempad,(value>>8));
            device.writeMem(tempad+1,(value&0xFF));
            device.addCycles(6);
        }
    }
    
    /** Test for Zero or Negative in memory **/
    public void tst(int mode){
        int value=0;
        
        if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(7);
        }
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x03)); //Clear the overflow and carry bits
    }
    
    /** Test A for Zero or Negative **/
    public void tsta(){
        int value=device.getA();
        
        device.addCycles(2);
        
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x03)); //Clear the overflow and carry bits
    }
    
    /** Test B for Zero or Negative **/
    public void tstb(){
        int value=device.getB();
        
        device.addCycles(2);
        
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x03)); //Clear the overflow and carry bits
    }
    
    /** Transfer A to B **/
    public void tab(){
        int value=device.getA();
        device.setB(value);
        device.addCycles(2);
        
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /** Transfer B to A - Actual instruction name To Be Announced **/
    public void tba(){
        int value=device.getB();
        device.setA(value);
        device.addCycles(2);
        
        updateCCR8(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /** Transfer A to CCR **/
    public void tap(){
        int value=device.getA();
        if(value >= 128)
            value-=256;
        device.setCCR((byte)(value));
        device.addCycles(2);
    }
    
    /** Transfer CCR to A **/
    public void tpa(){
        int value=device.getCCR();
        if(value < 0)
            value+=256;
        device.setA(value);
        device.addCycles(2);
    }
    
    /** Exchange D and X **/
    public void xgdx(){
        int value=device.getX();
        device.setX(device.getD());
        device.setD(value);
        device.addCycles(3);
    }
    
    /** Exchange D and Y **/
    public void xgdy(){
        int value=device.getY();
        device.setY(device.getD());
        device.setD(value);
        device.addCycles(4);
    }
    
    /** Transfer Stack Pointer to X **/
    public void tsx(){
        int value=device.getSP();
        device.setX(value+1);
        device.addCycles(3);
    }
    
    /** Transfer X to Stack Pointer **/
    public void txs(){
        int value=device.getX();
        device.setSP(value-1);
        device.addCycles(3);
    }
    
    /** Transfer Stack Pointer to Y **/
    public void tsy(){
        int value=device.getSP();
        device.setY(value+1);
        device.addCycles(4);
    }
    
    /** Transfer Y to Stack Pointer **/
    public void tys(){
        int value=device.getY();
        device.setSP(value-1);
        device.addCycles(4);
    }
    
    public void ldab(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==IMM){
            value = get8bitImmediate();
            device.addCycles(2);
        }
        else if(mode==DIR){
            int tempad = get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(3);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(4);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(4);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            device.addCycles(5);
        }
        //No else statement, because I want something bad to happen if you don't call this correctly.
        device.setB(value);
        updateCCR8(value);
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /** Load accumulator X **/
    public void ldx(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==IMM){
            value = get16bitImmediate();
            device.addCycles(3);
        }
        else if(mode==DIR){
            int tempad = get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(4);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(5);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(5);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(6);
        }
        //No else statement, because I want something bad to happen if you don't call this correctly.
        device.setX(value);
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /** Load accumulator Y **/
    public void ldy(int mode){
        int value=0;
        
        //Grab the operand
        if(mode==IMM){
            value = get16bitImmediate();
            device.addCycles(4);
        }
        else if(mode==DIR){
            int tempad = get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(5);
        }
        else if(mode==EXT){
            int tempad = get16bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(6);
        }
        else if(mode==INDX){
            int tempad = device.getX()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(6);
        }
        else if(mode==INDY){
            int tempad = device.getY()+get8bitImmediate();
            value = device.getMem(tempad);
            value = value << 8;
            value += device.getMem(tempad+1);
            device.addCycles(6);
        }
        //No else statement, because I want something bad to happen if you don't call this correctly.
        device.setY(value);
        updateCCR16(value); //Set negative and zero bits
        device.clearCCRbits((byte)(0x02)); //Clear the overflow bit
    }
    
    /* Update the zero and negatives in CCR regarding an 8-bit value */
    private void updateCCR8(int value){
        if(value == 0){
            device.setCCRbits((byte)4);
            device.clearCCRbits((byte)8);
        }
        else if (value >= 128){
            device.setCCRbits((byte)8);
            device.clearCCRbits((byte)4);
        }
        else{ //Not zero, not negative
            device.clearCCRbits((byte)0xC);
        }
    }
    
    /* Update the zero and negatives in CCR regarding an 16-bit value */
    private void updateCCR16(int value){
        if(value == 0){
            device.setCCRbits((byte)4);
            device.clearCCRbits((byte)8);
        }
        else if (value >= 0x8000){
            device.setCCRbits((byte)8);
            device.clearCCRbits((byte)4);
        }
        else{ //Not zero, not negative
            device.clearCCRbits((byte)0xC);
        }
    }
    
    /* Update the overflow flag based on the result of subtraction */
    private void updateOverflow8(int dest, int sub, int result){
        //dest and sub are treated like signed bytes.
        //If dest (usually accumulator A) is negative, sub is positive, and result is positive, overflow
        //If dest is positive, sub is negative and result is negative, overflow
        if(((dest&0x80) != (sub&0x80)) && (sub&0x80) == (result&0x80))
            device.setCCRbits((byte)(2)); //Set V
        else
            device.clearCCRbits((byte)(2)); //Clear V
    }
    
    private void updateOverflow16(int dest, int sub, int result){
        //dest and sub are treated like signed 16-bit integers. (s16, short)
        //If dest (D, X or Y) is negative, sub is positive, and result is positive, overflow
        //If dest is positive, sub is negative and result is negative, overflow
        if(((dest&0x8000) != (sub&0x8000)) && (sub&0x8000) == (result&0x8000))
            device.setCCRbits((byte)(2)); //Set V
        else
            device.clearCCRbits((byte)(2)); //Clear V
    }
    
    private int get16bitImmediate(){
        int value = device.nextInstruct();
        value = value << 8;
        value += device.nextInstruct();
        return value;
    }
    
    private int get8bitImmediate(){
        return device.nextInstruct();
    }
    
}
