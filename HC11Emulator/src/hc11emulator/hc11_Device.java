/*
 * hc11_Device.java
 *
 * Created on May 4, 2006, 1:05 PM
 *
 * @author Paul Kratt
 */

package hc11emulator;

import java.util.ArrayList;

public abstract class hc11_Device {
    protected byte[] addrbus; //Address Bus
    protected int d,x,y,sp,pc; //Registers D(A:B), X, and Y. Stack Pointer, and Program Counter.
    protected byte ccr; //Condition code register
    protected int lastpc; //The location PC was at when we started executing this instruction.
    protected long cycles; //Total processor cycles executed.
    protected hc11_Instructions inset; //HC11 instruction set
    private static final int INH=0,IMM=1,DIR=2,EXT=3,INDX=4,INDY=5,IMMA=6,IMMB=7; //Instruction modes
    protected ArrayList<hc11_Observer> observers;
    protected ArrayList<Integer> addr_traps; //Memory address traps
    protected ArrayList<hc11_ramTrapper> trappers; //The class trapping this address
    
    /** Creates a new instance of hc11_Device */
    public hc11_Device() {
        addrbus = new byte[0x10000]; //Declare 64kb address bus
        inset = new hc11_Instructions(this); //Declare a copy of the instruction executor.
        ccr = (byte)0xD0; //SXHINZVC - Set SX and I on initially.
        observers = new ArrayList();
        addr_traps = new ArrayList();
        trappers = new ArrayList();
    }
    
    /** Remove an observer from the device
     *@param obs The observer to remove
     *@return True if successful. False if observer not found.
     **/
    public boolean removeObserver(hc11_Observer obs){
        return observers.remove(obs);
    }
    
    /** Add an observer to the device. Returns true if successful. **/
    public boolean addObserver(hc11_Observer obs){
        boolean hasname = hasObserver(obs);
        if(!hasname)
            observers.add(obs);
        return (!hasname); //Returns true if added the observer
    }
    
    /** Checks to see if we have an observer already **/
    public boolean hasObserver(hc11_Observer obs){
        boolean hasname=false;
        
        //First, make sure it doesn't already exist. We don't want two of the same thing fighting over memory.
        for(hc11_Observer check : observers){
            if(check.getName().equalsIgnoreCase(obs.getName()))
                hasname=true;
        }
        return hasname;
    }
    
    /** Notify observers that they can update their display **/
    public void notifyObservers(){
        for(hc11_Observer obs : observers)
            obs.visualUpdate(); //Update all observers
    }

    /** Execute the next instruction **/
    public void execute(){
        lastpc = pc;
        int instruct = nextInstruct(); //Grab the next instruction
        
        //Huge switch statement that calls the correct instructions
        switch(instruct){
            //Case 0 is TEST, which is unsupported. The default case will handle it.
            case 0x01: //nop
                inset.nop();
                break;
            case 0x02:
                inset.idiv();
                break;
            case 0x03:
                inset.fdiv();
                break;
            case 0x04:
                inset.lsrd();
                break;
            case 0x05:
                inset.lsld();
                break;
            case 0x06:
                inset.tap();
                break;
            case 0x07:
                inset.tpa();
                break;
            case 0x08:
                inset.inx();
                break;
            case 0x09:
                inset.dex();
                break;
            case 0x0A:
                inset.clv();
                break;
            case 0x0B:
                inset.sev();
                break;
            case 0x0C:
                inset.clc();
                break;
            case 0x0D:
                inset.sec();
                break;
            case 0x0E:
                inset.clc();
                break;
            case 0x0F:
                inset.sei();
                break;
            case 0x10:
                inset.sba(false);
                break;
            case 0x11:
                inset.sba(true);
                break;
            case 0x12:
                inset.brset(DIR);
                break;
            case 0x13:
                inset.brclr(DIR);
                break;
            case 0x14:
                inset.bset(DIR);
                break;
            case 0x15:
                inset.bclr(DIR);
                break;
            case 0x16:
                inset.tab();
                break;
            case 0x17:
                inset.tba();
                break;
            //case 0x18 is below
            case 0x19:
                inset.daa();
                break;
            //case 0x1A is also below
            case 0x1B:
                inset.aba();
                break;
            case 0x1C:
                inset.bset(INDX);
                break;
            case 0x1D:
                inset.bclr(INDX);
                break;
            case 0x1E:
                inset.brset(INDX);
                break;
            case 0x1F:
                inset.brclr(INDX);
                break;
            case 0x20:
                inset.bra();
                break;
            case 0x21:
                inset.brn();
                break;
            case 0x22:
                inset.bhi();
                break;
            case 0x23:
                inset.bls();
                break;
            case 0x24:
                inset.bcc();
                break;
            case 0x25:
                inset.bcs();
                break;
            case 0x26:
                inset.bne();
                break;
            case 0x27:
                inset.beq();
                break;
            case 0x28:
                inset.bvc();
                break;
            case 0x29:
                inset.bvs();
                break;
            case 0x2A:
                inset.bpl();
                break;
            case 0x2B:
                inset.bmi();
                break;
            case 0x2C:
                inset.bge();
                break;
            case 0x2D:
                inset.blt();
                break;
            case 0x2E:
                inset.bgt();
                break;
            case 0x2F:
                inset.ble();
                break;
            case 0x30:
                inset.tsx();
                break;
            case 0x31:
                inset.ins();
                break;
            case 0x32:
                inset.pula();
                break;
            case 0x33:
                inset.pulb();
                break;
            case 0x34:
                inset.des();
                break;
            case 0x35:
                inset.txs();
                break;
            case 0x36:
                inset.psha();
                break;
            case 0x37:
                inset.pshb();
                break;
            case 0x38:
                inset.pulx();
                break;
            case 0x39:
                inset.rts();
                break;
            case 0x3A:
                inset.abx();
                break;
            case 0x3B:
                inset.rti();
                break;
            case 0x3C:
                inset.pshx();
                break;
            case 0x3D:
                inset.mul();
                break;
            case 0x40:
                inset.neg(IMMA);
                break;
            case 0x43:
                inset.com(IMMA);
                break;
            case 0x44:
                inset.lsr(IMMA);
                break;
            case 0x46:
                inset.ror(IMMA);
                break;
            case 0x47:
                inset.asr(IMMA);
                break;
            case 0x48:
                inset.asl(IMMA);
                break;
            case 0x49:
                inset.rol(IMMA);
                break;
            case 0x4A:
                inset.deca();
                break;
            case 0x4C:
                inset.inca();
                break;
            case 0x4D:
                inset.tsta();
                break;
            case 0x4F:
                inset.clr(IMMA);
                break;
            case 0x50:
                inset.neg(IMMB);
                break;
            case 0x53:
                inset.com(IMMB);
                break;
            case 0x54:
                inset.lsr(IMMB);
                break;
            case 0x56:
                inset.ror(IMMB);
                break;
            case 0x57:
                inset.asr(IMMB);
                break;
            case 0x58:
                inset.asl(IMMB);
                break;
            case 0x59:
                inset.rol(IMMB);
                break;
            case 0x5A:
                inset.decb();
                break;
            case 0x5C:
                inset.incb();
                break;
            case 0x5D:
                inset.tstb();
                break;
            case 0x5F:
                inset.clr(IMMB);
                break;
            case 0x60:
                inset.neg(INDX);
                break;
            case 0x63:
                inset.com(INDX);
                break;
            case 0x64:
                inset.lsr(INDX);
                break;
            case 0x66:
                inset.ror(INDX);
                break;
            case 0x67:
                inset.asr(INDX);
                break;
            case 0x68:
                inset.asl(INDX);
                break;
            case 0x69:
                inset.rol(INDX);
                break;
            case 0x6A:
                inset.dec(INDX);
                break;
            case 0x6C:
                inset.inc(INDX);
                break;
            case 0x6D:
                inset.tst(INDX);
                break;
            case 0x6E:
                inset.jmp(INDX);
                break;
            case 0x6F:
                inset.clr(INDX);
                break;
            case 0x70:
                inset.neg(EXT);
                break;
            case 0x73:
                inset.com(EXT);
                break;
            case 0x74:
                inset.lsr(EXT);
                break;
            case 0x76:
                inset.ror(EXT);
                break;
            case 0x77:
                inset.asr(EXT);
                break;
            case 0x78:
                inset.asl(EXT);
                break;
            case 0x79:
                inset.rol(EXT);
                break;
            case 0x7A:
                inset.dec(EXT);
                break;
            case 0x7C:
                inset.inc(EXT);
                break;
            case 0x7D:
                inset.tst(EXT);
                break;
            case 0x7E:
                inset.jmp(EXT);
                break;
            case 0x7F:
                inset.clr(EXT);
                break;
            case 0x80:
                inset.suba(IMM);
                break;
            case 0x81:
                inset.cmpa(IMM);
                break;
            case 0x82:
                inset.sbca(IMM);
                break;
            case 0x83:
                inset.subd(IMM);
                break;
            case 0x84:
                inset.anda(IMM);
                break;
            case 0x85:
                inset.bita(IMM);
                break;
            case 0x86:
                inset.ldaa(IMM);
                break;
            case 0x88:
                inset.eora(IMM);
                break;
            case 0x89:
                inset.adca(IMM);
                break;
            case 0x8A:
                inset.oraa(IMM);
                break;
            case 0x8B:
                inset.adda(IMM);
                break;
            case 0x8C:
                inset.cmpx(IMM);
                break;
            case 0x8D:
                inset.bsr();
                break;
            case 0x8E:
                inset.lds(IMM);
                break;
            case 0x8F:
                inset.xgdx();
                break;
            case 0x90:
                inset.suba(DIR);
                break;
            case 0x91:
                inset.cmpa(DIR);
                break;
            case 0x92:
                inset.sbca(DIR);
                break;
            case 0x93:
                inset.subd(DIR);
                break;
            case 0x94:
                inset.anda(DIR);
                break;
            case 0x95:
                inset.bita(DIR);
                break;
            case 0x96:
                inset.ldaa(DIR);
                break;
            case 0x97:
                inset.staa(DIR);
                break;
            case 0x98:
                inset.eora(DIR);
                break;
            case 0x99:
                inset.adca(DIR);
                break;
            case 0x9A:
                inset.oraa(DIR);
                break;
            case 0x9B:
                inset.adda(DIR);
                break;
            case 0x9C:
                inset.cmpx(DIR);
                break;
            case 0x9D:
                inset.jsr(DIR);
                break;
            case 0x9E:
                inset.lds(DIR);
                break;
            case 0x9F:
                inset.sts(DIR);
                break;
            case 0xA0:
                inset.suba(INDX);
                break;
            case 0xA1:
                inset.cmpa(INDX);
                break;
            case 0xA2:
                inset.sbca(INDX);
                break;
            case 0xA3:
                inset.subd(INDX);
                break;
            case 0xA4:
                inset.anda(INDX);
                break;
            case 0xA5:
                inset.bita(INDX);
                break;
            case 0xA6:
                inset.ldaa(INDX);
                break;
            case 0xA7:
                inset.staa(INDX);
                break;
            case 0xA8:
                inset.eora(INDX);
                break;
            case 0xA9:
                inset.adca(INDX);
                break;
            case 0xAA:
                inset.oraa(INDX);
                break;
            case 0xAB:
                inset.adda(INDX);
                break;
            case 0xAC:
                inset.cmpx(INDX);
                break;
            case 0xAD:
                inset.jsr(INDX);
                break;
            case 0xAE:
                inset.lds(INDX);
                break;
            case 0xAF:
                inset.sts(INDX);
                break;
            case 0xB0:
                inset.suba(EXT);
                break;
            case 0xB1:
                inset.cmpa(EXT);
                break;
            case 0xB2:
                inset.sbca(EXT);
                break;
            case 0xB3:
                inset.subd(EXT);
                break;
            case 0xB4:
                inset.anda(EXT);
                break;
            case 0xB5:
                inset.bita(EXT);
                break;
            case 0xB6:
                inset.ldaa(EXT);
                break;
            case 0xB7:
                inset.staa(EXT);
                break;
            case 0xB8:
                inset.eora(EXT);
                break;
            case 0xB9:
                inset.adca(EXT);
                break;
            case 0xBA:
                inset.oraa(EXT);
                break;
            case 0xBB:
                inset.adda(EXT);
                break;
            case 0xBC:
                inset.cmpx(EXT);
                break;
            case 0xBD:
                inset.jsr(EXT);
                break;
            case 0xBE:
                inset.lds(EXT);
                break;
            case 0xBF:
                inset.sts(EXT);
                break;
            case 0xC0:
                inset.subb(IMM);
                break;
            case 0xC1:
                inset.cmpb(IMM);
                break;
            case 0xC2:
                inset.sbcb(IMM);
                break;
            case 0xC3:
                inset.addd(IMM);
                break;
            case 0xC4:
                inset.andb(IMM);
                break;
            case 0xC5:
                inset.bitb(IMM);
                break;
            case 0xC6:
                inset.ldab(IMM);
                break;
            case 0xC8:
                inset.eorb(IMM);
                break;
            case 0xC9:
                inset.adcb(IMM);
                break;
            case 0xCA:
                inset.orab(IMM);
                break;
            case 0xCB:
                inset.addb(IMM);
                break;
            case 0xCC:
                inset.ldd(IMM);
                break;
            case 0xCE:
                inset.ldx(IMM);
                break;
            //case 0xCF: STOP instruction, do something about this later.
            case 0xD0:
                inset.subb(DIR);
                break;
            case 0xD1:
                inset.cmpb(DIR);
                break;
            case 0xD2:
                inset.sbcb(DIR);
                break;
            case 0xD3:
                inset.addd(DIR);
                break;
            case 0xD4:
                inset.andb(DIR);
                break;
            case 0xD5:
                inset.bitb(DIR);
                break;
            case 0xD6:
                inset.ldab(DIR);
                break;
            case 0xD7:
                inset.stab(DIR);
                break;
            case 0xD8:
                inset.eorb(DIR);
                break;
            case 0xD9:
                inset.adcb(DIR);
                break;
            case 0xDA:
                inset.orab(DIR);
                break;
            case 0xDB:
                inset.addb(DIR);
                break;
            case 0xDC:
                inset.ldd(DIR);
                break;
            case 0xDD:
                inset.std(DIR);
                break;
            case 0xDE:
                inset.ldx(DIR);
                break;
            case 0xDF:
                inset.stx(DIR);
                break;
            case 0xE0:
                inset.subb(INDX);
                break;
            case 0xE1:
                inset.cmpb(INDX);
                break;
            case 0xE2:
                inset.sbcb(INDX);
                break;
            case 0xE3:
                inset.addd(INDX);
                break;
            case 0xE4:
                inset.andb(INDX);
                break;
            case 0xE5:
                inset.bitb(INDX);
                break;
            case 0xE6:
                inset.ldab(INDX);
                break;
            case 0xE7:
                inset.stab(INDX);
                break;
            case 0xE8:
                inset.eorb(INDX);
                break;
            case 0xE9:
                inset.adcb(INDX);
                break;
            case 0xEA:
                inset.orab(INDX);
                break;
            case 0xEB:
                inset.addb(INDX);
                break;
            case 0xEC:
                inset.ldd(INDX);
                break;
            case 0xED:
                inset.std(INDX);
                break;
            case 0xEE:
                inset.ldx(INDX);
                break;
            case 0xEF:
                inset.stx(INDX);
                break;
            case 0xF0:
                inset.subb(EXT);
                break;
            case 0xF1:
                inset.cmpb(EXT);
                break;
            case 0xF2:
                inset.sbcb(EXT);
                break;
            case 0xF3:
                inset.addd(EXT);
                break;
            case 0xF4:
                inset.andb(EXT);
                break;
            case 0xF5:
                inset.bitb(EXT);
                break;
            case 0xF6:
                inset.ldab(EXT);
                break;
            case 0xF7:
                inset.stab(EXT);
                break;
            case 0xF8:
                inset.eorb(EXT);
                break;
            case 0xF9:
                inset.adcb(EXT);
                break;
            case 0xFA:
                inset.orab(EXT);
                break;
            case 0xFB:
                inset.addb(EXT);
                break;
            case 0xFC:
                inset.ldd(EXT);
                break;
            case 0xFD:
                inset.std(EXT);
                break;
            case 0xFE:
                inset.ldx(EXT);
                break;
            case 0xFF:
                inset.stx(EXT);
                break;
            case 0x18: //Secondary instructions
                instruct = nextInstruct();
                switch(instruct){
                    case 0x08:
                        inset.iny();
                        break;
                    case 0x09:
                        inset.dey();
                        break;
                    case 0x1C:
                        inset.bset(INDY);
                        break;
                    case 0x1D:
                        inset.bclr(INDY);
                        break;
                    case 0x1E:
                        inset.brset(INDY);
                        break;
                    case 0x1F:
                        inset.brclr(INDY);
                        break;
                    case 0x30:
                        inset.tsy();
                        break;
                    case 0x35:
                        inset.tys();
                        break;
                    case 0x38:
                        inset.puly();
                        break;
                    case 0x3A:
                        inset.aby();
                        break;
                    case 0x3C:
                        inset.pshy();
                        break;
                    case 0x60:
                        inset.neg(INDY);
                        break;
                    case 0x63:
                        inset.com(INDY);
                        break;
                    case 0x64:
                        inset.com(INDY);
                        break;
                    case 0x66:
                        inset.ror(INDY);
                        break;
                    case 0x67:
                        inset.asr(INDY);
                        break;
                    case 0x68:
                        inset.asr(INDY);
                        break;
                    case 0x69:
                        inset.rol(INDY);
                        break;
                    case 0x6A:
                        inset.dec(INDY);
                        break;
                    case 0x6C:
                        inset.inc(INDY);
                        break;
                    case 0x6D:
                        inset.tst(INDY);
                        break;
                    case 0x6E:
                        inset.jmp(INDY);
                        break;
                    case 0x6F:
                        inset.clr(INDY);
                        break;
                    case 0x8C:
                        inset.cmpy(IMM);
                        break;
                    case 0x8F:
                        inset.xgdy();
                        break;
                    case 0x9C:
                        inset.cmpy(DIR);
                        break;
                    case 0xA0:
                        inset.suba(INDY);
                        break;
                    case 0xA1:
                        inset.cmpa(INDY);
                        break;
                    case 0xA2:
                        inset.sbca(INDY);
                        break;
                    case 0xA3:
                        inset.subd(INDY);
                        break;
                    case 0xA4:
                        inset.anda(INDY);
                        break;
                    case 0xA5:
                        inset.bita(INDY);
                        break;
                    case 0xA6:
                        inset.lds(INDY);
                        break;
                    case 0xA7:
                        inset.staa(INDY);
                        break;
                    case 0xA8:
                        inset.eora(INDY);
                        break;
                    case 0xA9:
                        inset.adca(INDY);
                        break;
                    case 0xAA:
                        inset.oraa(INDY);
                        break;
                    case 0xAB:
                        inset.adda(INDY);
                        break;
                    case 0xAC:
                        inset.cmpy(INDY);
                        break;
                    case 0xAD:
                        inset.jsr(INDY);
                        break;
                    case 0xAE:
                        inset.lds(INDY);
                        break;
                    case 0xAF:
                        inset.sts(INDY);
                        break;
                    case 0xBC:
                        inset.cmpy(EXT);
                        break;
                    case 0xCE:
                        inset.ldy(IMM);
                        break;
                    case 0xDE:
                        inset.ldy(DIR);
                        break;
                    case 0xDF:
                        inset.sty(DIR);
                        break;
                    case 0xE0:
                        inset.subb(INDY);
                        break;
                    case 0xE1:
                        inset.cmpb(INDY);
                        break;
                    case 0xE2:
                        inset.sbcb(INDY);
                        break;
                    case 0xE3:
                        inset.addd(INDY);
                        break;
                    case 0xE4:
                        inset.andb(INDY);
                        break;
                    case 0xE5:
                        inset.bitb(INDY);
                        break;
                    case 0xE6:
                        inset.ldab(INDY);
                        break;
                    case 0xE7:
                        inset.stab(INDY);
                        break;
                    case 0xE8:
                        inset.eorb(INDY);
                        break;
                    case 0xE9:
                        inset.adcb(INDY);
                        break;
                    case 0xEA:
                        inset.orab(INDY);
                        break;
                    case 0xEB:
                        inset.addb(INDY);
                        break;
                    case 0xEC:
                        inset.ldd(INDY);
                        break;
                    case 0xED:
                        inset.std(INDY);
                        break;
                    case 0xEE:
                        inset.ldy(INDY);
                        break;
                    case 0xEF:
                        inset.sty(INDY);
                        break;
                    case 0xFE:
                        inset.ldy(EXT);
                        break;
                    case 0xFF:
                        inset.sty(EXT);
                        break;
                    default:
                        inset.nop();
                        System.out.println("Unsupported Opcode: " + Integer.toString(instruct,16) + " at " + Integer.toHexString(lastpc));
                }
                break;
            case 0x1A: //Secondary instructions page 3
                instruct = nextInstruct();
                switch(instruct){
                    case 0x83:
                        inset.cmpd(IMM);
                        break;
                    case 0x93:
                        inset.cmpd(DIR);
                        break;
                    case 0xA3:
                        inset.cmpd(INDX);
                        break;
                    case 0xAC:
                        inset.cmpy(INDX);
                        break;
                    case 0xB3:
                        inset.cmpd(EXT);
                        break;
                    case 0xEE:
                        inset.ldy(INDX);
                        break;
                    case 0xEF:
                        inset.sty(INDX);
                        break;
                    default:
                        inset.nop();
                        System.out.println("Unsupported Opcode: " + Integer.toString(instruct,16) + " at " + Integer.toHexString(lastpc));
                }
                break;
            case 0xCD: //Secondary instructions page 4
                instruct = nextInstruct();
                switch(instruct){
                    case 0xA3:
                        inset.cmpd(INDY);
                        break;
                    case 0xAC:
                        inset.cmpx(INDY);
                        break;
                    case 0xEE:
                        inset.ldx(INDY);
                        break;
                    case 0xEF:
                        inset.stx(INDY);
                        break;
                    default:
                        inset.nop();
                        System.out.println("Unsupported Opcode: " + Integer.toString(instruct,16) + " at " + Integer.toHexString(lastpc));
                }
                break;
            default:
                inset.nop();
                System.out.println("Unsupported Opcode: " + Integer.toString(instruct,16) + " at " + Integer.toHexString(lastpc));
        }
    }
    
    /** Disassemble the instruction at the specificed address
     *@param addr The address to disassemble
     *@return The disassembled instruction, and description
     */
    public Instruct_Container dissassemble(int addr){
        Instruct_Container retval = new Instruct_Container();
        
        int instruct = getMemSilent(addr);
        
        switch(instruct){
            case 0x00:
                retval.setInstruction("TEST");
                retval.setDescription("Used for testing mode only!");
                retval.setSize(1);
                break;
            case 0x01:
                retval.setInstruction("nop");
                retval.setDescription("Perform no operation.");
                retval.setSize(1);
                break;
            case 0x02:
                retval.setInstruction("idiv");
                retval.setDescription("Perform integer division. D/X, result into X, remainder into D.");
                retval.setSize(1);
                break;
            case 0x03:
                retval.setInstruction("fdiv");
                retval.setDescription("Perform fractional division. D/X, result into X, remainder into D.");
                retval.setSize(1);
                break;
            case 0x04:
                retval.setInstruction("lsrd");
                retval.setDescription("Logically shift the contents of D to the right once.");
                retval.setSize(1);
                break;
            case 0x05:
                retval.setInstruction("asld");
                retval.setDescription("Arithmetically shift the contents of D to the left once.");
                retval.setSize(1);
                break;
            case 0x06:
                retval.setInstruction("tap");
                retval.setDescription("Transfer the contents of accumulator A into the CCR.");
                retval.setSize(1);
                break;
            case 0x07:
                retval.setInstruction("tpa");
                retval.setDescription("Transfer the contents of the CCR back into A.");
                retval.setSize(1);
                break;
            case 0x08:
                retval.setInstruction("inx");
                retval.setDescription("Increment the value in accumulator X by one.");
                retval.setSize(1);
                break;
            case 0x09:
                retval.setInstruction("dex");
                retval.setDescription("Decrement the value in accumulator X by one.");
                retval.setSize(1);
                break;
            case 0x0A:
                retval.setInstruction("clv");
                retval.setDescription("Clear the overflow flag in the CCR.");
                retval.setSize(1);
                break;
            case 0x0B:
                retval.setInstruction("sev");
                retval.setDescription("Set the overflow flag in the CCR.");
                retval.setSize(1);
                break;
            case 0x0C:
                retval.setInstruction("clc");
                retval.setDescription("Clear the carry flag in the CCR.");
                retval.setSize(1);
                break;
            case 0x0D:
                retval.setInstruction("sec");
                retval.setDescription("Set the carry flag in the CCR.");
                retval.setSize(1);
                break;
            case 0x0E:
                retval.setInstruction("cli");
                retval.setDescription("Clear the interrupt mask in the CCR. This enables interruopts.");
                retval.setSize(1);
                break;
            case 0x0F:
                retval.setInstruction("sei");
                retval.setDescription("Set the interrupt mask in the CCR. This disables interrupts.");
                retval.setSize(1);
                break;
            case 0x10:
                retval.setInstruction("sba");
                retval.setDescription("Subtract B from A. Result goes back into A.");
                retval.setSize(1);
                break;
            case 0x11:
                retval.setInstruction("cba");
                retval.setDescription("Compare A to B, and update the CCR accordingly.");
                retval.setSize(1);
                break;
            case 0x12:
                retval.setInstruction("brset");
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+3)+4) + " if the bits " + 
                        Integer.toHexString(getMemSilent(addr+2)) + " are set at the memory location 0x00" +
                        Integer.toHexString(getMemSilent(addr+1)) + ".");
                retval.setSize(4);
                break;
            case 0x13:
                retval.setInstruction("brclr");
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+3)+4) + " if the bits " + 
                        Integer.toHexString(getMemSilent(addr+2)) + " are cleared at the memory location 0x00" +
                        Integer.toHexString(getMemSilent(addr+1)) + ".");
                retval.setSize(4);
                break;
            case 0x14:
                retval.setInstruction("bset");
                retval.setDescription("Set the bits at the memory address 0x00" + Integer.toHexString(getMemSilent(addr+1)) +
                        " based on the ones that are set in the value 0x" + Integer.toHexString(getMemSilent(addr+2)) + ".");
                retval.setSize(3);
                break;
            case 0x15:
                retval.setInstruction("bclr");
                retval.setDescription("Clear the bits at the memory address 0x00" + Integer.toHexString(getMemSilent(addr+1)) +
                        " based on the ones that are set in the value 0x" + Integer.toHexString(getMemSilent(addr+2)) + ".");
                retval.setSize(3);
                break;
            case 0x16:
                retval.setInstruction("tab");
                retval.setDescription("Transfer the value in accumulator A to accumulator B.");
                retval.setSize(1);
                break;
            case 0x17:
                retval.setInstruction("tba");
                retval.setDescription("Transfer the value in accumulator B to accumulator A.");
                retval.setSize(1);
                break;
            case 0x19:
                retval.setInstruction("daa");
                retval.setDescription("Decimal adjust accumulator A. This assumes that A contains a binary coded decimal number.");
                retval.setSize(1);
                break;
            case 0x1B:
                retval.setInstruction("aba");
                retval.setDescription("Add B to A, and put the result in A.");
                retval.setSize(1);
                break;
            case 0x1C:
                retval.setInstruction("bset");
                retval.setDescription("Set the bits at the memory address whose location is 0x" + Integer.toHexString(getMemSilent(addr+1)) +
                        " bytes from the location stored in X, based on the ones that are set in the value 0x" + Integer.toHexString(getMemSilent(addr+2)) + ".");
                retval.setSize(3);
                break;
            case 0x1D:
                retval.setInstruction("bclr");
                retval.setDescription("Clear the bits at the memory address whose location is 0x" + Integer.toHexString(getMemSilent(addr+1)) +
                        " bytes from the location stored in X, based on the ones that are set in the value 0x" + Integer.toHexString(getMemSilent(addr+2)) + ".");
                retval.setSize(3);
                break;
            case 0x1E:
                retval.setInstruction("brset");
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+3)+4) + " if the bits " + 
                        Integer.toHexString(getMemSilent(addr+2)) + " are set at the memory location 0x" +
                        Integer.toHexString(getMemSilent(addr+1)) + " bytes from X.");
                retval.setSize(4);
                break;
            case 0x1F:
                retval.setInstruction("brclr");
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+3)+4) + " if the bits " + 
                        Integer.toHexString(getMemSilent(addr+2)) + " are cleared at the memory location 0x" +
                        Integer.toHexString(getMemSilent(addr+1)) + " bytes from X.");
                retval.setSize(4);
                break;
            case 0x20:
                retval.setInstruction("bra 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Always branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x21:
                retval.setInstruction("brn 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Never branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x22:
                retval.setInstruction("bhi 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if higher to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x23:
                retval.setInstruction("bls 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if lower or same to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x24:
                retval.setInstruction("bcc 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + " if the carry bit in the CCR is cleared.");
                retval.setSize(2);
                break;
            case 0x25:
                retval.setInstruction("bcs 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + " if the carry bit in the CCR is set.");
                retval.setSize(2);
                break;
            case 0x26:
                retval.setInstruction("bne 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if not equal to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x27:
                retval.setInstruction("beq 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if equal to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x28:
                retval.setInstruction("bvc 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + " if the overflow bit in the CCR is cleared.");
                retval.setSize(2);
                break;
            case 0x29:
                retval.setInstruction("bvs 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + " if the overflow bit in the CCR is set.");
                retval.setSize(2);
                break;
            case 0x2A:
                retval.setInstruction("bpl 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if plus (positive) to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x2B:
                retval.setInstruction("bmi 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if minus (negative) to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x2C:
                retval.setInstruction("bge 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + " if greater than or equal.");
                retval.setSize(2);
                break;
            case 0x2D:
                retval.setInstruction("blt 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if less than to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x2E:
                retval.setInstruction("bgt 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch if greater than to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + ".");
                retval.setSize(2);
                break;
            case 0x2F:
                retval.setInstruction("ble 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getSignedMemSilent(addr+1)+2) + " if less than or equal to.");
                retval.setSize(2);
                break;
            case 0x30:
                retval.setInstruction("tsx");
                retval.setDescription("Transfer the stack pointer into accumulator X.");
                retval.setSize(1);
                break;
            case 0x31:
                retval.setInstruction("ins");
                retval.setDescription("Increment the stack pointer by one.");
                retval.setSize(1);
                break;
            case 0x32:
                retval.setInstruction("pula");
                retval.setDescription("Pull the top value off of the stack, and put it into accumulator A.");
                retval.setSize(1);
                break;
            case 0x33:
                retval.setInstruction("pulb");
                retval.setDescription("Pull the top value off of the stack, and put it into accumulator B.");
                retval.setSize(1);
                break;
            case 0x34:
                retval.setInstruction("des");
                retval.setDescription("Decrement the stack pointer by one.");
                retval.setSize(1);
                break;
            case 0x35:
                retval.setInstruction("txs");
                retval.setDescription("Transfer the value in accumulator X to the stack pointer.");
                retval.setSize(1);
                break;
            case 0x36:
                retval.setInstruction("psha");
                retval.setDescription("Push the value in accumulator A onto the stack.");
                retval.setSize(1);
                break;
            case 0x37:
                retval.setInstruction("pshb");
                retval.setDescription("Push the value in accumulator B onto the stack.");
                retval.setSize(1);
                break;
            case 0x38:
                retval.setInstruction("pulx");
                retval.setDescription("Pull a 16-bit value off of the stack, and put it into accumulator X.");
                retval.setSize(1);
                break;
            case 0x39:
                retval.setInstruction("rts");
                retval.setDescription("Return from a subroutine. Pulls a 16-bit value off of the stack and puts it into the program counter.");
                retval.setSize(1);
                break;
            case 0x3A:
                retval.setInstruction("abx");
                retval.setDescription("Add the value in accumulator B to X.");
                retval.setSize(1);
                break;
            case 0x3B:
                retval.setInstruction("rti");
                retval.setDescription("Return from an interrupt.");
                retval.setSize(1);
                break;
            case 0x3C:
                retval.setInstruction("pshx");
                retval.setDescription("Push the value in accumulator X onto the stack.");
                retval.setSize(1);
                break;
            case 0x3D:
                retval.setInstruction("mul");
                retval.setDescription("Multiply A and B, put the result into D.");
                retval.setSize(1);
                break;
            case 0x3E:
                retval.setInstruction("wai");
                retval.setDescription("Wait for an interrupt to occur.");
                retval.setSize(1);
                break;
            case 0x3F:
                retval.setInstruction("swi");
                retval.setDescription("Perform a software interrupt.");
                retval.setSize(1);
                break;
            case 0x40:
                retval.setInstruction("nega");
                retval.setDescription("2's complement the value in accumulator A. (0x00-value)");
                retval.setSize(1);
                break;
            case 0x43:
                retval.setInstruction("coma");
                retval.setDescription("1's complement the value in accumulator A. (0xFF-value)");
                retval.setSize(1);
                break;
            case 0x44:
                retval.setInstruction("lsra");
                retval.setDescription("Logical shift right the value in accumulator A.");
                retval.setSize(1);
                break;
            case 0x46:
                retval.setInstruction("rora");
                retval.setDescription("Rotate right the value in accumulator A.");
                retval.setSize(1);
                break;
            case 0x47:
                retval.setInstruction("asra");
                retval.setDescription("Arithmetic shift right the value in accumulator A.");
                retval.setSize(1);
                break;
            case 0x48:
                retval.setInstruction("asla");
                retval.setDescription("Arithmetic shift left the value in accumulator A.");
                retval.setSize(1);
                break;
            case 0x49:
                retval.setInstruction("rola");
                retval.setDescription("Rotate left the value in accumulator A.");
                retval.setSize(1);
                break;
            case 0x4A:
                retval.setInstruction("deca");
                retval.setDescription("Decrement the value in accumulator A by one.");
                retval.setSize(1);
                break;
            case 0x4C:
                retval.setInstruction("inca");
                retval.setDescription("Increment the value in accumulator A by one.");
                retval.setSize(1);
                break;
            case 0x4D:
                retval.setInstruction("tsta");
                retval.setDescription("Test the value in accumulator A, and set the CCR bits accordingly.");
                retval.setSize(1);
                break;
            case 0x4F:
                retval.setInstruction("clra");
                retval.setDescription("Clear the value in accumulator A.");
                retval.setSize(1);
                break;
            case 0x50:
                retval.setInstruction("negb");
                retval.setDescription("2's complement the value in accumulator B. (0x00-value)");
                retval.setSize(1);
                break;
            case 0x53:
                retval.setInstruction("comb");
                retval.setDescription("1's complement the value in accumulator B. (0xFF-value)");
                retval.setSize(1);
                break;
            case 0x54:
                retval.setInstruction("lsrb");
                retval.setDescription("Logical shift right the value in accumulator B.");
                retval.setSize(1);
                break;
            case 0x56:
                retval.setInstruction("rorb");
                retval.setDescription("Rotate right the value in accumulator B.");
                retval.setSize(1);
                break;
            case 0x57:
                retval.setInstruction("asrb");
                retval.setDescription("Arithmetic shift right the value in accumulator B.");
                retval.setSize(1);
                break;
            case 0x58:
                retval.setInstruction("aslb");
                retval.setDescription("Arithmetic shift left the value in accumulator B.");
                retval.setSize(1);
                break;
            case 0x59:
                retval.setInstruction("rolb");
                retval.setDescription("Rotate left the value in accumulator B.");
                retval.setSize(1);
                break;
            case 0x5A:
                retval.setInstruction("decb");
                retval.setDescription("Decrement the value in accumulator B by one.");
                retval.setSize(1);
                break;
            case 0x5C:
                retval.setInstruction("incb");
                retval.setDescription("Increment the value in accumulator B by one.");
                retval.setSize(1);
                break;
            case 0x5D:
                retval.setInstruction("tstb");
                retval.setDescription("Test the value in accumulator B, and set the CCR bits accordingly.");
                retval.setSize(1);
                break;
            case 0x5F:
                retval.setInstruction("clrb");
                retval.setDescription("Clear the value in accumulator B.");
                retval.setSize(1);
                break;
            case 0x60:
                retval.setInstruction("neg " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("2's complement the value at " + Integer.toString(getMemSilent(addr+1)) + ",X. (0x00-value)");
                retval.setSize(2);
                break;
            case 0x63:
                retval.setInstruction("com " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("1's complement the value at " + Integer.toString(getMemSilent(addr+1)) + ",X. (0xFF-value)");
                retval.setSize(2);
                break;
            case 0x64:
                retval.setInstruction("lsr " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Logical shift right the value at " + Integer.toString(getMemSilent(addr+1)) + ",X.");
                retval.setSize(2);
                break;
            case 0x66:
                retval.setInstruction("ror " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Rotate right the value at " + Integer.toString(getMemSilent(addr+1)) + ",X.");
                retval.setSize(2);
                break;
            case 0x67:
                retval.setInstruction("asr " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Arithmetically shift right the value at " + Integer.toString(getMemSilent(addr+1)) + ",X.");
                retval.setSize(2);
                break;
            case 0x68:
                retval.setInstruction("asl " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Arithmetically shift left the value at " + Integer.toString(getMemSilent(addr+1)) + ",X.");
                retval.setSize(2);
                break;
            case 0x69:
                retval.setInstruction("rol " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Rotate left the value at " + Integer.toString(getMemSilent(addr+1)) + ",X.");
                retval.setSize(2);
                break;
            case 0x6A:
                retval.setInstruction("dec " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Decrement the value at " + Integer.toString(getMemSilent(addr+1)) + ",X by one.");
                retval.setSize(2);
                break;
            case 0x6C:
                retval.setInstruction("inc " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Increment the value at " + Integer.toString(getMemSilent(addr+1)) + ",X by one.");
                retval.setSize(2);
                break;
            case 0x6D:
                retval.setInstruction("tst " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Test the value at " + Integer.toString(getMemSilent(addr+1)) + ",X and set the CCR bits accordingly.");
                retval.setSize(2);
                break;
            case 0x6E:
                retval.setInstruction("jmp " + Integer.toString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Set the program counter to the value at " + Integer.toString(getMemSilent(addr+1)) + ",X.");
                retval.setSize(2);
                break;
            case 0x6F:
                retval.setInstruction("clr " + Integer.toHexString(getMemSilent(addr+1)) + ",X");
                retval.setDescription("Clear the value at " + Integer.toHexString(getMemSilent(addr+1)) + ",X.");
                retval.setSize(2);
                break;
            case 0x70:
                retval.setInstruction("neg 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("2's complement the value at 0x" + get16bitHex(addr+1) + ". (0x00-value)");
                retval.setSize(3);
                break;
            case 0x73:
                retval.setInstruction("com 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("1's complement the value at 0x" + get16bitHex(addr+1) + ". (0xFF-value)");
                retval.setSize(3);
                break;
            case 0x74:
                retval.setInstruction("lsr 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Logical shift right the value at 0x" + get16bitHex(addr+1) + ".");
                retval.setSize(3);
                break;
            case 0x76:
                retval.setInstruction("ror 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Rotate right the value at 0x" + get16bitHex(addr+1) + ".");
                retval.setSize(3);
                break;
            case 0x77:
                retval.setInstruction("asr 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Arithmetically shift right the value at 0x" + get16bitHex(addr+1) + ".");
                retval.setSize(3);
                break;
            case 0x78:
                retval.setInstruction("asl 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Arithmetically shift left the value at 0x" + get16bitHex(addr+1) + ".");
                retval.setSize(3);
                break;
            case 0x79:
                retval.setInstruction("rol 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Rotate left the value at 0x" + get16bitHex(addr+1) + ".");
                retval.setSize(3);
                break;
            case 0x7A:
                retval.setInstruction("dec 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Decrement the value at 0x" + get16bitHex(addr+1) + " by one.");
                retval.setSize(3);
                break;
            case 0x7C:
                retval.setInstruction("inc 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Increment the value at 0x" + get16bitHex(addr+1) + " by one.");
                retval.setSize(3);
                break;
            case 0x7D:
                retval.setInstruction("tst 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Test the value at 0x" + get16bitHex(addr+1) + " and set the CCR bits accordingly.");
                retval.setSize(3);
                break;
            case 0x7E:
                retval.setInstruction("jmp 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Set the program counter to the value 0x" + get16bitHex(addr+1) + ".");
                retval.setSize(3);
                break;
            case 0x7F:
                retval.setInstruction("clr 0x" + Integer.toHexString(getMemSilent(addr+1)) + Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Clear the value at 0x" + get16bitHex(addr+1) + ".");
                retval.setSize(3);
                break;
            case 0x80:
                retval.setInstruction("suba #0x" +Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Subtract 0x"+Integer.toHexString(getMemSilent(addr+1))+" from accumulator A.");
                retval.setSize(2);
                break;
            case 0x81:
                retval.setInstruction("cmpa #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Compare the value in accumulator A with 0x"+Integer.toHexString(getMemSilent(addr+1))+ " and update the CCR.");
                retval.setSize(2);
                break;
            case 0x82:
                retval.setInstruction("sbca #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Subtract 0x"+Integer.toHexString(getMemSilent(addr+1))+" and the value of the carry bit from accumulator A.");
                retval.setSize(2);
                break;
            case 0x83:
                retval.setInstruction("subd #0x"+get16bitHex(addr+1));
                retval.setDescription("Subtract 0x"+get16bitHex(addr+1)+" from D.");
                retval.setSize(2);
                break;
            case 0x84:
                retval.setInstruction("anda #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Bitwise AND the value in Accumulator A with 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x85:
                retval.setInstruction("bita #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Update the CCR based on the Bitwise AND between accumulator A and 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x86:
                retval.setInstruction("ldaa #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Load the value 0x"+get8bitHex(addr+1)+" into accumulator A.");
                retval.setSize(2);
                break;
            case 0x88:
                retval.setInstruction("eora #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("XOR the value in accumulator A with 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x89:
                retval.setInstruction("adca #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Add 0x"+get8bitHex(addr+1)+" plus the value of the carry bit to accumulator A.");
                retval.setSize(2);
                break;
            case 0x8A:
                retval.setInstruction("oraa #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Bitwise OR the value in Accumulator A with 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x8B:
                retval.setInstruction("adda #0x"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Add 0x"+get8bitHex(addr+1)+" to accumulator A.");
                retval.setSize(2);
                break;
            case 0x8C:
                retval.setInstruction("cpx #0x"+get16bitHex(addr+1));
                retval.setDescription("Compare X with the value 0x"+get16bitHex(addr+1)+" and update the CCR.");
                retval.setSize(3);
                break;
            case 0x8D:
                retval.setInstruction("bsr 0x"+Integer.toHexString(addr+getSignedMemSilent(addr+1)+2));
                retval.setDescription("Branch to the subroutine at 0x"+Integer.toHexString(addr+getSignedMemSilent(addr+1)+2)+".");
                retval.setSize(2);
                break;
            case 0x8E:
                retval.setInstruction("lds #0x"+get16bitHex(addr+1));
                retval.setDescription("Load the stack pointer with 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0x8F:
                retval.setInstruction("xgdx");
                retval.setDescription("Swap the values in D and accumulator X.");
                retval.setSize(1);
                break;
            case 0x90:
                retval.setInstruction("suba 0x00"+get8bitHex(addr+1));
                retval.setDescription("Subtract from accumulator A the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x91:
                retval.setInstruction("cmpa 0x00"+get8bitHex(addr+1));
                retval.setDescription("Compare the value in accumulator A with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x92:
                retval.setInstruction("sbca 0x00"+get8bitHex(addr+1));
                retval.setDescription("Subtract the value at 0x00"+get8bitHex(addr+1)+" and the carry bit from accumulator A.");
                retval.setSize(2);
                break;
            case 0x93:
                retval.setInstruction("subd 0x00"+get8bitHex(addr+1));
                retval.setDescription("Subtract from D the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x94:
                retval.setInstruction("anda 0x00"+get8bitHex(addr+1));
                retval.setDescription("Bitwise AND the value at 0x00"+get8bitHex(addr+1)+" with accumulator A.");
                retval.setSize(2);
                break;
            case 0x95:
                retval.setInstruction("bita 0x00"+get8bitHex(addr+1));
                retval.setDescription("Update the CCR according to the AND between accumulator A and the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x96:
                retval.setInstruction("ldaa 0x00"+get8bitHex(addr+1));
                retval.setDescription("Load accumulator A with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x97:
                retval.setInstruction("staa 0x00"+get8bitHex(addr+1));
                retval.setDescription("Store the value in accumulator A to 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;                
            case 0x98:
                retval.setInstruction("eora 0x00"+get8bitHex(addr+1));
                retval.setDescription("XOR the value at 0x00"+get8bitHex(addr+1)+" with accumulator A.");
                retval.setSize(2);
                break;
            case 0x99:
                retval.setInstruction("adca 0x00"+get8bitHex(addr+1));
                retval.setDescription("Add the value at 0x00"+get8bitHex(addr+1)+" and the carry bit to accumulator A.");
                retval.setSize(2);
                break;
            case 0x9A:
                retval.setInstruction("oraa 0x00"+get8bitHex(addr+1));
                retval.setDescription("Bitwise OR the value in accumulator A with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x9B:
                retval.setInstruction("adda 0x00"+get8bitHex(addr+1));
                retval.setDescription("Add the value at 0x00"+get8bitHex(addr+1)+" to accumulator A.");
                retval.setSize(2);
                break;
            case 0x9C:
                retval.setInstruction("cpx 0x00"+get8bitHex(addr+1));
                retval.setDescription("Compare the value in X with the value at 0x00"+get8bitHex(addr+1)+", updating the CCR.");
                retval.setSize(2);
                break;
            case 0x9D:
                retval.setInstruction("jsr 0x00"+get8bitHex(addr+1));
                retval.setDescription("Jump to the subroutine at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x9E:
                retval.setInstruction("lds 0x00"+Integer.toHexString(getMemSilent(addr+1)));
                retval.setDescription("Load the stack pointer with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0x9F:
                retval.setInstruction("sts 0x00"+get8bitHex(addr+1));
                retval.setDescription("Store the value in the stack pointer to 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xA0:
                retval.setInstruction("suba "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Subtract from a the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xA1:
                retval.setInstruction("cmpa "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Compare acumulator A to the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xA2:
                retval.setInstruction("sbca "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Subtract from accumulator A the value at "+Integer.toString(getMemSilent(addr+1))+",X and the carry bit.");
                retval.setSize(2);
                break;
            case 0xA3:
                retval.setInstruction("subd "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Subtract from D the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xA4:
                retval.setInstruction("anda "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Bitwise AND accumulator A with the value at "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setSize(2);
                break;
            case 0xA5:
                retval.setInstruction("bita "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Update the CCR according to the AND of accumulator A and "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xA6:
                retval.setInstruction("ldaa "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Load into accumulator A the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xA7:
                retval.setInstruction("staa "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Store the value in accumulator A to "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;                
            case 0xA8:
                retval.setInstruction("eora "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("XOR the value in accumulator A with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xA9:
                retval.setInstruction("adca "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Add with carry to A the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xAA:
                retval.setInstruction("oraa "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Bitwise OR the value in A with "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xAB:
                retval.setInstruction("adda "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Add to accumulator A the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xAC:
                retval.setInstruction("cpx "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Compare to X the value at "+Integer.toString(getMemSilent(addr+1))+",X updating the CCR accordingly.");
                retval.setSize(2);
                break;
            case 0xAD:
                retval.setInstruction("jsr "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Jump to the subroutine located at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xAE:
                retval.setInstruction("lds "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Load the stack pointer with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xAF:
                retval.setInstruction("sts "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Store the stack pointer value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xB0:
                retval.setInstruction("suba 0x"+get16bitHex(addr+1));
                retval.setDescription("Subtract from accumulator A the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB1:
                retval.setInstruction("cmpa 0x"+get16bitHex(addr+1));
                retval.setDescription("Compare A to the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB2:
                retval.setInstruction("sbca 0x"+get16bitHex(addr+1));
                retval.setDescription("Subtract with carry from A the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB3:
                retval.setInstruction("subd 0x"+get16bitHex(addr+1));
                retval.setDescription("Subtract from D the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB4:
                retval.setInstruction("anda 0x"+get16bitHex(addr+1));
                retval.setDescription("Bitwise AND accumulator A with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB5:
                retval.setInstruction("bita 0x"+get16bitHex(addr+1));
                retval.setDescription("Update the CCR according to the AND between accumulator A and the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB6:
                retval.setInstruction("ldaa 0x"+get16bitHex(addr+1));
                retval.setDescription("Load accumulator A with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB7:
                retval.setInstruction("staa 0x"+get16bitHex(addr+1));
                retval.setDescription("Store the value of accumulator A at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;                
            case 0xB8:
                retval.setInstruction("eora 0x"+get16bitHex(addr+1));
                retval.setDescription("XOR accumulator A with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xB9:
                retval.setInstruction("adca 0x"+get16bitHex(addr+1));
                retval.setDescription("Add with carry to accumulator A the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xBA:
                retval.setInstruction("oraa 0x"+get16bitHex(addr+1));
                retval.setDescription("Bitwise OR the value in Accumulator A with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xBB:
                retval.setInstruction("adda 0x"+get16bitHex(addr+1));
                retval.setDescription("Add to accumulator A the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xBC:
                retval.setInstruction("cpx 0x"+get16bitHex(addr+1));
                retval.setDescription("Compare with X the value at 0x"+get16bitHex(addr+1)+", updating the CCR.");
                retval.setSize(3);
                break;
            case 0xBD:
                retval.setInstruction("jsr 0x"+get16bitHex(addr+1));
                retval.setDescription("Jump to the subroutine at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xBE:
                retval.setInstruction("lds 0x"+get16bitHex(addr+1));
                retval.setDescription("Load the stack pointer with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xBF:
                retval.setInstruction("sts 0x"+get16bitHex(addr+1));
                retval.setDescription("Store the stack pointer at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xC0:
                retval.setInstruction("subb #0x"+get8bitHex(addr+1));
                retval.setDescription("Subtract 0x"+get8bitHex(addr+1)+" from accumulator B.");
                retval.setSize(2);
                break;
            case 0xC1:
                retval.setInstruction("cmpb #0x"+get8bitHex(addr+1));
                retval.setDescription("Compare accumulator B to 0x"+get8bitHex(addr+1)+" updating the CCR accordingly.");
                retval.setSize(2);
                break;
            case 0xC2:
                retval.setInstruction("sbcb #0x"+get8bitHex(addr+1));
                retval.setDescription("Subtract 0x"+get8bitHex(addr+1)+" and the carry bit from accumulator B.");
                retval.setSize(2);
                break;
            case 0xC3:
                retval.setInstruction("addd #0x"+get16bitHex(addr+1));
                retval.setDescription("Add 0x"+get16bitHex(addr+1)+" to D.");
                retval.setSize(3);
                break;
            case 0xC4:
                retval.setInstruction("andb #0x"+get8bitHex(addr+1));
                retval.setDescription("Bitwise AND 0x"+get8bitHex(addr+1)+" with accumulator B.");
                retval.setSize(2);
                break;
            case 0xC5:
                retval.setInstruction("bitb #0x"+get8bitHex(addr+1));
                retval.setDescription("Update the CCR based on an AND between accumulator B and 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xC6:
                retval.setInstruction("ldab #0x"+get8bitHex(addr+1));
                retval.setDescription("Load accumulator B with 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xC8:
                retval.setInstruction("eorb #0x"+get8bitHex(addr+1));
                retval.setDescription("XOR the value in accumulator B with 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xC9:
                retval.setInstruction("adcb #0x"+get8bitHex(addr+1));
                retval.setDescription("Add with carry to accumulator B the value 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xCA:
                retval.setInstruction("orab #0x"+get8bitHex(addr+1));
                retval.setDescription("Bitwise OR the value in accumulator B with 0x"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xCB:
                retval.setInstruction("addb #0x"+get8bitHex(addr+1));
                retval.setDescription("Add 0x"+get8bitHex(addr+1)+" to accumulator B.");
                retval.setSize(2);
                break;
            case 0xCC:
                retval.setInstruction("ldd #0x"+get16bitHex(addr+1));
                retval.setDescription("Load D with the value 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xCE:
                retval.setInstruction("ldx #0x"+Integer.toHexString(getMemSilent(addr+1))+Integer.toHexString(getMemSilent(addr+2)));
                retval.setDescription("Load X with the value 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xCF:
                retval.setInstruction("stop");
                retval.setDescription("Halt execution of the current program.");
                retval.setSize(1);
                break;
            case 0xD0:
                retval.setInstruction("subb 0x00"+get8bitHex(addr+1));
                retval.setDescription("Subtract from B the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xD1:
                retval.setInstruction("cmpb 0x00"+get8bitHex(addr+1));
                retval.setDescription("Compare B to the value at 0x00"+get8bitHex(addr+1)+", updating the CCR.");
                retval.setSize(2);
                break;
            case 0xD2:
                retval.setInstruction("sbcb 0x00"+get8bitHex(addr+1));
                retval.setDescription("Subtract with carry the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xD3:
                retval.setInstruction("addd 0x00"+get8bitHex(addr+1));
                retval.setDescription("Add to D the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xD4:
                retval.setInstruction("andb 0x00"+get8bitHex(addr+1));
                retval.setDescription("Bitwise AND the value in accumulator B with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xD5:
                retval.setInstruction("bitb 0x00"+get8bitHex(addr+1));
                retval.setDescription("Update the CCR based on the AND between B and the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xD6:
                retval.setInstruction("ldab 0x00"+get8bitHex(addr+1));
                retval.setDescription("Load accumulator B with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xD7:
                retval.setInstruction("stab 0x00"+get8bitHex(addr+1));
                retval.setDescription("Store the value in B at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;                
            case 0xD8:
                retval.setInstruction("eorb 0x00"+get8bitHex(addr+1));
                retval.setDescription("XOR the value in B with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xD9:
                retval.setInstruction("adcb 0x00"+get8bitHex(addr+1));
                retval.setDescription("Add with carry to B the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xDA:
                retval.setInstruction("orab 0x00"+get8bitHex(addr+1));
                retval.setDescription("Bitwise OR the value in accumulator B with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xDB:
                retval.setInstruction("addb 0x00"+get8bitHex(addr+1));
                retval.setDescription("Add to accumulator B the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xDC:
                retval.setInstruction("ldd 0x00"+get8bitHex(addr+1));
                retval.setDescription("Load D with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xDD:
                retval.setInstruction("std 0x00"+get8bitHex(addr+1));
                retval.setDescription("Store the value in D to 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xDE:
                retval.setInstruction("ldx 0x00"+get8bitHex(addr+1));
                retval.setDescription("Load X with the value at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xDF:
                retval.setInstruction("stx 0x00"+get8bitHex(addr+1));
                retval.setDescription("Store the value of X at 0x00"+get8bitHex(addr+1)+".");
                retval.setSize(2);
                break;
            case 0xE0:
                retval.setInstruction("subb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Subtract from accumulator B the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE1:
                retval.setInstruction("cmpb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Compare B to the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE2:
                retval.setInstruction("sbcb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Subtract with carry from B the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE3:
                retval.setInstruction("addd "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Add to D the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE4:
                retval.setInstruction("andb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Bitwise AND accumulator B with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE5:
                retval.setInstruction("bitb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Update the CCR based on an AND between B and the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE6:
                retval.setInstruction("ldab "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Load accumulator B with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE7:
                retval.setInstruction("stab "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Store the value in B at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;                
            case 0xE8:
                retval.setInstruction("eorb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("XOR accumulator B with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xE9:
                retval.setInstruction("adcb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Add with carry to B the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xEA:
                retval.setInstruction("orab "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Bitwise OR accumulator B with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xEB:
                retval.setInstruction("addb "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Add to accumulator B the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xEC:
                retval.setInstruction("ldd "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Load D with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xED:
                retval.setInstruction("std "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Store the value in D to "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xEE:
                retval.setInstruction("ldx "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Load X with the value at "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xEF:
                retval.setInstruction("stx "+Integer.toString(getMemSilent(addr+1))+",X");
                retval.setDescription("Store the value in X to "+Integer.toString(getMemSilent(addr+1))+",X.");
                retval.setSize(2);
                break;
            case 0xF0:
                retval.setInstruction("subb 0x"+get16bitHex(addr+1));
                retval.setDescription("Subtract from B the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xF1:
                retval.setInstruction("cmpb 0x"+get16bitHex(addr+1));
                retval.setDescription("Compare B to the value at 0x"+get16bitHex(addr+1)+", updating the CCR.");
                retval.setSize(3);
                break;
            case 0xF2:
                retval.setInstruction("sbcb 0x"+get16bitHex(addr+1));
                retval.setDescription("Subtract with carry from B the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xF3:
                retval.setInstruction("addd 0x"+get16bitHex(addr+1));
                retval.setDescription("Add to D the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xF4:
                retval.setInstruction("andb 0x"+get16bitHex(addr+1));
                retval.setDescription("Bitwise AND the value in B with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xF5:
                retval.setInstruction("bitb 0x"+get16bitHex(addr+1));
                retval.setDescription("Update the CCR based on the AND between B and the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xF6:
                retval.setInstruction("ldab 0x"+get16bitHex(addr+1));
                retval.setDescription("Load accumulator B with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xF7:
                retval.setInstruction("stab 0x"+get16bitHex(addr+1));
                retval.setDescription("Store the value in B to 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;                
            case 0xF8:
                retval.setInstruction("eorb 0x"+get16bitHex(addr+1));
                retval.setDescription("XOR accumulator B with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xF9:
                retval.setInstruction("adcb 0x"+get16bitHex(addr+1));
                retval.setDescription("Add with carry to B the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xFA:
                retval.setInstruction("orab 0x"+get16bitHex(addr+1));
                retval.setDescription("Bitwise OR accumulator B with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xFB:
                retval.setInstruction("addb 0x"+get16bitHex(addr+1));
                retval.setDescription("Add to accumulator B the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xFC:
                retval.setInstruction("ldd 0x"+get16bitHex(addr+1));
                retval.setDescription("Load D with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xFD:
                retval.setInstruction("std 0x"+get16bitHex(addr+1));
                retval.setDescription("Store the value of D at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xFE:
                retval.setInstruction("ldx 0x"+get16bitHex(addr+1));
                retval.setDescription("Load X with the value at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0xFF:
                retval.setInstruction("stx 0x"+get16bitHex(addr+1));
                retval.setDescription("Store the value of X at 0x"+get16bitHex(addr+1)+".");
                retval.setSize(3);
                break;
            case 0x18: //Page 2, Accumulator Y based instructions
                instruct = getMemSilent(addr+1);
                switch(instruct){
                    case 0x08:
                        retval.setInstruction("iny");
                        retval.setDescription("Increment the value in accumulator Y by one.");
                        retval.setSize(2);
                        break;
                    case 0x09:
                        retval.setInstruction("dey");
                        retval.setDescription("Decrement the value in accumulator Y by one.");
                        retval.setSize(2);
                        break;
                    case 0x1C:
                        retval.setInstruction("bset");
                        retval.setDescription("Set the bits at the memory address whose location is 0x" + Integer.toHexString(getMemSilent(addr+2)) +
                                " bytes from the location stored in Y, based on the ones that are set in the value 0x" + Integer.toHexString(getMemSilent(addr+3)) + ".");
                        retval.setSize(4);
                        break;
                    case 0x1D:
                        retval.setInstruction("bclr");
                        retval.setDescription("Clear the bits at the memory address whose location is 0x" + Integer.toHexString(getMemSilent(addr+2)) +
                                " bytes from the location stored in Y, based on the ones that are set in the value 0x" + Integer.toHexString(getMemSilent(addr+3)) + ".");
                        retval.setSize(4);
                        break;
                    case 0x1E:
                        retval.setInstruction("brset");
                        retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getMemSilent(addr+4)+4) + " if the bits " + 
                                Integer.toHexString(getMemSilent(addr+3)) + " are set at the memory location 0x" +
                                Integer.toHexString(getMemSilent(addr+2)) + " bytes from Y.");
                        retval.setSize(5);
                        break;
                    case 0x1F:
                        retval.setInstruction("brclr");
                        retval.setDescription("Branch to 0x" + Integer.toHexString(addr+getMemSilent(addr+4)+4) + " if the bits " + 
                                Integer.toHexString(getMemSilent(addr+3)) + " are cleared at the memory location 0x" +
                                Integer.toHexString(getMemSilent(addr+2)) + " bytes from Y.");
                        retval.setSize(5);
                        break;
                    case 0x30:
                        retval.setInstruction("tsy");
                        retval.setDescription("Transfer the stack pointer into accumulator Y.");
                        retval.setSize(2);
                        break;
                    case 0x35:
                        retval.setInstruction("tys");
                        retval.setDescription("Transfer the value in accumulator Y to the stack pointer.");
                        retval.setSize(2);
                        break;
                    case 0x38:
                        retval.setInstruction("puly");
                        retval.setDescription("Pull a 16-bit value off of the stack, and put it into accumulator Y.");
                        retval.setSize(2);
                        break;
                    case 0x3A:
                        retval.setInstruction("aby");
                        retval.setDescription("Add the value in accumulator B to Y.");
                        retval.setSize(2);
                        break;
                    case 0x3C:
                        retval.setInstruction("pshy");
                        retval.setDescription("Push the value in accumulator Y onto the stack.");
                        retval.setSize(2);
                        break;
                    case 0x60:
                        retval.setInstruction("neg " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("2's complement the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y. (0x00-value)");
                        retval.setSize(3);
                        break;
                    case 0x63:
                        retval.setInstruction("com " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("1's complement the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y. (0xFF-value)");
                        retval.setSize(3);
                        break;
                    case 0x64:
                        retval.setInstruction("lsr " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Logical shift right the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y.");
                        retval.setSize(3);
                        break;
                    case 0x66:
                        retval.setInstruction("ror " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Rotate right the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y.");
                        retval.setSize(3);
                        break;
                    case 0x67:
                        retval.setInstruction("asr " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Arithmetically shift right the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y.");
                        retval.setSize(3);
                        break;
                    case 0x68:
                        retval.setInstruction("asl " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Arithmetically shift left the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y.");
                        retval.setSize(3);
                        break;
                    case 0x69:
                        retval.setInstruction("rol " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Rotate left the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y.");
                        retval.setSize(3);
                        break;
                    case 0x6A:
                        retval.setInstruction("dec " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Decrement the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y by one.");
                        retval.setSize(3);
                        break;
                    case 0x6C:
                        retval.setInstruction("inc " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Increment the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y by one.");
                        retval.setSize(3);
                        break;
                    case 0x6D:
                        retval.setInstruction("tst " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Test the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y and set the CCR bits accordingly.");
                        retval.setSize(3);
                        break;
                    case 0x6E:
                        retval.setInstruction("jmp " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Set the program counter to the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y.");
                        retval.setSize(3);
                        break;
                    case 0x6F:
                        retval.setInstruction("clr " + Integer.toString(getMemSilent(addr+2)) + ",Y");
                        retval.setDescription("Clear the value at " + Integer.toString(getMemSilent(addr+2)) + ",Y.");
                        retval.setSize(3);
                        break;
                    case 0x8C:
                        retval.setInstruction("cpy #0x"+get16bitHex(addr+2));
                        retval.setDescription("Compare Y to the value 0x"+get16bitHex(addr+2)+" and update the CCR accordingly.");
                        retval.setSize(4);
                        break;
                    case 0x8F:
                        retval.setInstruction("xgdy");
                        retval.setDescription("Exchange the values of D and Y.");
                        retval.setSize(2);
                        break;
                    case 0x9C:
                        retval.setInstruction("cpy 0x00"+get8bitHex(addr+2));
                        retval.setDescription("Compare Y to the value at 0x00"+get8bitHex(addr+2)+".");
                        retval.setSize(3);
                        break;
                    case 0xA0:
                        retval.setInstruction("suba "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Subtract from A the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA1:
                        retval.setInstruction("cmpa "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Compare A to the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA2:
                        retval.setInstruction("sbca "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Subtract with carry from A the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA3:
                        retval.setInstruction("subd "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Subtract from D the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA4:
                        retval.setInstruction("anda "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Bitwise AND accumulator A with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA5:
                        retval.setInstruction("bita "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Update the CCR based on an AND between A and the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA6:
                        retval.setInstruction("ldaa "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Load accumulator A with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA7:
                        retval.setInstruction("staa "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Store the value of accumulator A at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;                
                    case 0xA8:
                        retval.setInstruction("eora "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("XOR the value of accumulator A with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xA9:
                        retval.setInstruction("adca "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Add with carry to A the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xAA:
                        retval.setInstruction("oraa "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Bitwise OR accumulator A with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xAB:
                        retval.setInstruction("adda "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Add to accumulator A the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xAC:
                        retval.setInstruction("cpx "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Compare X to the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xAD:
                        retval.setInstruction("jsr "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Jump to the subroutine at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xAE:
                        retval.setInstruction("lds "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Load the stack pointer with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xAF:
                        retval.setInstruction("sts "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Store the stack pointer at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xBC:
                        retval.setInstruction("cpy 0x"+get16bitHex(addr+2));
                        retval.setDescription("Compare the value in Y to the value at 0x"+get16bitHex(addr+2)+".");
                        retval.setSize(4);
                        break;
                    case 0xCE:
                        retval.setInstruction("ldy #0x"+get16bitHex(addr+2));
                        retval.setDescription("Load Y with the value 0x"+get16bitHex(addr+2)+".");
                        retval.setSize(4);
                        break;
                    case 0xDE:
                        retval.setInstruction("ldy 0x00"+Integer.toHexString(getMemSilent(addr+2)));
                        retval.setDescription("Load Y with the value at 0x00"+get8bitHex(addr+2)+".");
                        retval.setSize(3);
                        break;
                    case 0xDF:
                        retval.setInstruction("sty 0x00"+get8bitHex(addr+2));
                        retval.setDescription("Store the value in Y at 0x00"+get8bitHex(addr+2)+".");
                        retval.setSize(3);
                        break;
                    case 0xE0:
                        retval.setInstruction("subb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Subtract from B the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE1:
                        retval.setInstruction("cmpb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Compare B to the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE2:
                        retval.setInstruction("sbcb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Subtract with carry from B the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE3:
                        retval.setInstruction("addd "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Add to D the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE4:
                        retval.setInstruction("andb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Bitwise AND accumulator B with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE5:
                        retval.setInstruction("bitb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Update the CCR based on the AND between B and the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE6:
                        retval.setInstruction("ldab "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Load accumulator B with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE7:
                        retval.setInstruction("stab "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Store the value in B at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE8:
                        retval.setInstruction("eorb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("XOR accumulator B with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xE9:
                        retval.setInstruction("adcb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Add with carry to B the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xEA:
                        retval.setInstruction("orab "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Bitwise OR accumulator B with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xEB:
                        retval.setInstruction("addb "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Add to accumulator B the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xEC:
                        retval.setInstruction("ldd "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Load accumulator D with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xED:
                        retval.setInstruction("std "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Store the value in D at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xEE:
                        retval.setInstruction("ldy "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Load Y with the value at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xEF:
                        retval.setInstruction("sty "+Integer.toHexString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Store the value of Y at "+Integer.toHexString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xFE:
                        retval.setInstruction("ldy 0x"+get16bitHex(addr+2));
                        retval.setDescription("Load Y with the value at 0x"+get16bitHex(addr+2)+".");
                        retval.setSize(4);
                        break;
                    case 0xFF:
                        retval.setInstruction("sty 0x"+get16bitHex(addr+2));
                        retval.setDescription("Store the value of Y at "+get16bitHex(addr+2)+".");
                        retval.setSize(4);
                        break;
                    default:
                        retval.setInstruction("ERR");
                        retval.setDescription("Invalid Opcode!");
                        retval.setSize(2);
                        break;
                }
                break;
            case 0x1A: //Page 3
                instruct = getMemSilent(addr+1);
                switch(instruct){
                    case 0x83:
                        retval.setInstruction("cpd #0x"+get16bitHex(addr+2));
                        retval.setDescription("Compare D with the value 0x"+get16bitHex(addr+2)+".");
                        retval.setSize(4);
                        break;
                    case 0x93:
                        retval.setInstruction("cpd 0x00"+get8bitHex(addr+2));
                        retval.setDescription("Compare D with the value at 0x00"+get8bitHex(addr+2)+".");
                        retval.setSize(3);
                        break;
                    case 0xA3:
                        retval.setInstruction("cpd "+Integer.toString(getMemSilent(addr+2))+",X");
                        retval.setDescription("Compare D to the value at "+Integer.toString(getMemSilent(addr+2))+",X.");
                        retval.setSize(3);
                        break;
                    case 0xAC:
                        retval.setInstruction("cpy "+Integer.toString(getMemSilent(addr+2))+",X");
                        retval.setDescription("Compare Y to the value at "+Integer.toString(getMemSilent(addr+2))+",X");
                        retval.setSize(3);
                        break;
                    case 0xB3:
                        retval.setInstruction("cpd 0x"+get16bitHex(addr+2));
                        retval.setDescription("Compare D to the value at 0x"+get16bitHex(addr+2)+".");
                        retval.setSize(4);
                        break;
                    case 0xEE:
                        retval.setInstruction("ldy "+Integer.toString(getMemSilent(addr+2))+",X");
                        retval.setDescription("Load Y with the value at "+Integer.toString(getMemSilent(addr+2))+",X.");
                        retval.setSize(3);
                        break;
                    case 0xEF:
                        retval.setInstruction("sty "+Integer.toString(getMemSilent(addr+2))+",X");
                        retval.setDescription("Store the value in Y at "+Integer.toString(getMemSilent(addr+2))+",X.");
                        retval.setSize(3);
                        break;
                    default:
                        retval.setInstruction("ERR");
                        retval.setDescription("Invalid Opcode!");
                        retval.setSize(2);
                        break;
                }
                break;
            case 0xCD: //Page 4
                instruct = getMemSilent(addr+1);
                switch(instruct){
                    case 0xA3:
                        retval.setInstruction("cpd "+Integer.toString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Compare D to the value at "+Integer.toString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xAC:
                        retval.setInstruction("cpx "+Integer.toString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Compare X to the value at "+Integer.toString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xEE:
                        retval.setInstruction("ldx "+Integer.toString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Load X with the value at "+Integer.toString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    case 0xEF:
                        retval.setInstruction("stx "+Integer.toString(getMemSilent(addr+2))+",Y");
                        retval.setDescription("Store the value of X at "+Integer.toString(getMemSilent(addr+2))+",Y.");
                        retval.setSize(3);
                        break;
                    default:
                        retval.setInstruction("ERR");
                        retval.setDescription("Invalid Opcode!");
                        retval.setSize(2);
                        break;
                }
                break;
            default:
                retval.setInstruction("ERR");
                retval.setDescription("Invalid Opcode!");
                retval.setSize(1);
                break;
        }
        return retval;
    }
    
    /** Return a byte of memory an integer
     @param addr The address to read
     @return The value of this address in memory
     **/
    public int getMem(int addr){
        checkRead(addr); //Trap memory read attempts
        return (addrbus[addr] < 0) ? (addrbus[addr] + 256) : (addrbus[addr]);
    }
    
    /** Return a byte of memory an integer - Calls to this version aren't trapped.
     @param addr The address to read
     @return The value of this address in memory
     **/
    public int getMemSilent(int addr){
        return (addrbus[addr] < 0) ? (addrbus[addr] + 256) : (addrbus[addr]);
    }

    /** Return a byte of memory a signed integer
     @param addr The address to read
     @return The value of this address in memory
     **/
    public int getSignedMemSilent(int addr){
        checkRead(addr); //Trap memory read attempts
        return addrbus[addr];
    }
    
    /** Get the next instruction byte to execute, and increment the program counter
     *@return The current value PC points to
     */
    public int nextInstruct(){
        return getMemSilent(pc++);
    }
    
    /** Write an array of bytes to memory - This function is always silent.
     *@param addr The address to write the bytes to
     *@param values The values to write
     */
    public void writeMem(int addr, byte[] values){
        for (int i=0; i<values.length; i++)
            addrbus[addr+i] = (byte) (values[i]);
    }
    
    /** Write a single byte to memory
     *@param addr The address to write the byte to
     *@param value The value to write
     */
    public void writeMem(int addr, int value){
        if(value >= 128)
            value -=256;
        if(addr>0xFFFF)
            System.out.println("Invalid memory access at: " + Integer.toString(lastpc,16));
        addrbus[addr] = (byte) (value);
        checkWrite(addr);
    }
    
    /** Write a single byte to memory - This is silent, so it won't trip a trap
     *@param addr The address to write the byte to
     *@param value The value to write
     */
    public void writeMemSilent(int addr, int value){
        if(value >= 128)
            value -=256;
        addrbus[addr] = (byte) (value);
    }
    
    /** Add a certian amount of cycles to the tick counter 
     *@param cycles Number of clock cycles to add
     **/
    public void addCycles(long cycles){
        this.cycles += cycles;
    }

    public byte getCCR() {
        return ccr;
    }
    
    public int getIntCCR() {
        if(ccr < 0)
            return ccr+256;
        return ccr;
    }

    public void setCCR(byte ccr) {
        if((this.ccr & 0x40)==0)
            this.ccr = (byte)(ccr & 0xBF);
        else
            this.ccr = ccr;        
    }
    
    public void setCCRbits(byte bits){
        ccr |= (byte)(bits & 0xBF); //0xBF Prevents the XIRQ bit from going back on.
    }
    
    /** Clear the bits that are 1 in the value passed in
     */
    public void clearCCRbits(byte bits){
        ccr &= (~bits);
    }

    public int getD() {
        return d;
    }
    
    public int getA() {
        return (d>>8);
    }
    
    public int getB() {
        return (d&0xFF);
    }

    public void setD(int d) {
        this.d = d;
    }
    
    public void setA(int a) {
        d = ((d & 0xFF) + (a<<8));
    }
    
    public void setB(int b) {
        d = ((d & 0xFF00) + b);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSP() {
        return sp;
    }

    public void setSP(int sp) {
        this.sp = sp;
    }

    public int getPC() {
        return pc;
    }
    
    public void setPC(int pc) {
        this.pc = pc;
    }
    
    /** Get the current amount of cycles the processor has been through.
     *@return Number of cycles
     */
    public long getCycles(){
        return cycles;
    }
    
    /** Return a nicely formatted version of the 8-bit value at the memory address passed in **/
    private String get8bitHex(int memloc){
        String result = Integer.toHexString(getMemSilent(memloc));
        if(result.length()==1)
            result = "0" + result;
        return result;
    }
    
    /** Return a nicely formatted version of the 16-bit value at the memory address passed in **/
    private String get16bitHex(int memloc){
        String result = Integer.toHexString(getMemSilent(memloc+1));
        if(result.length()==1)
            result = "0" + result;
        result = Integer.toHexString(getMemSilent(memloc)) + result;
        if(result.length()==3)
            result = "0" + result;
        return result;
    }
    
    /** Add a trap to a specific memory address **/
    public void addTrap(int address, hc11_ramTrapper trapper){
        addr_traps.add(new Integer(address));
        trappers.add(trapper);
    }
    
    /** Remove all traps on a specified address 
     *@return The number of traps removed
     **/
    public int removeTraps(int address){
        int trapsremoved = 0;
        for(int i=0; i<addr_traps.size(); i++){
            if(addr_traps.get(i).intValue()==address){
                addr_traps.remove(i);
                trappers.remove(i);
                trapsremoved++;
            }
        }
        return trapsremoved;
    }
    
    /** Remove occurances of a specific trap at a specific memory location
     *@param address The address the trap is at
     *@param trap The trap to remove
     *@return The number of traps removed
     **/
    public boolean removeTrap(int address, hc11_ramTrapper trap){
        boolean trapremoved = false;
        
        for(int i=0; i<trappers.size();i++){
            if(addr_traps.get(i).intValue()==address && trappers.get(i)==trap){
                trappers.remove(i);
                addr_traps.remove(i);
                trapremoved = true;
                break; //Only remove one.
            }
        }
        
        return trapremoved;
    }
    
    /** Remove all occurances of a specific trap
     *@return The number of traps removed
     **/
    public int removeTrap(hc11_ramTrapper trap){
        int trapsremoved = 0;
        
        while(trappers.indexOf(trap) > 0){
            int idx = trappers.indexOf(trap);
            trappers.remove(idx);
            addr_traps.remove(idx);
            trapsremoved++;
        }
        
        return trapsremoved;
    }
    
    /** Check if this address is trapped, and notify of a write state **/
    public void checkWrite(int address){
        for(int i=0; i<addr_traps.size(); i++){
            if(addr_traps.get(i).intValue() == address){
                trappers.get(i).writeTrap(address,lastpc);
            }
        }
    }
    
    /** Check if this address is trapped, and notify of a read state **/
    public void checkRead(int address){
        for(int i=0; i<addr_traps.size(); i++){
            if(addr_traps.get(i).intValue() == address){
                trappers.get(i).readTrap(address,lastpc);
            }
        }
    }
    
}
