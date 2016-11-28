import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileReader;
import java.lang.Exception;
import java.io.BufferedReader;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

// one assembly instruction
class ProcInstruction {
    public static final int INST_EMPTY  = 0;     // empty/other
    public static final int INST_COMP   = 1;    // computation
    public static final int INST_BLOCK  = 2;    // blocking (input/output)
    public static final int INST_EXIT   = 3;    // exit program
    public static final int INST_BR     = 128;  // Branch (128-228) with jump percentage

    ProcInstruction( int curaddr, String asmLine ) {
        type = INST_EMPTY;
        addr = curaddr;
        sourceLine = asmLine;
        lineScan = null;
    }

    int type;               // type of instruction
    int addr;               // memory address
    String sourceLine;      // source code for this instruction
    Scanner lineScan;       // scanner saved for later use (branches)
}

public class Asm {
    // what instruction are we on?    
    static int instructionPointer;
    static ArrayList<ProcInstruction> program = new ArrayList<>();
    
    // list of symbols and locations
    static HashMap<String,Integer> symbolTable = new HashMap<>();  
    
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            // open the input file
            String inFileName = args[0];

            FileReader sfile = new FileReader( inFileName );
            BufferedReader in = new BufferedReader( sfile );
            
            // replace ".pasm" with "pexe"
            int alen = inFileName.length();
            
            // make sure it ended with '.pasm'
            if ( ! inFileName.substring(alen-5).equals(".pasm")) {
                // no, have error
                System.out.println("Filename doesn't end in .pasm, aborting");
                System.exit(1);
            }
            
            // generate output file name
            String outFileName = inFileName.substring(0, alen-5) + ".pexe";            
            PrintWriter out = new PrintWriter( outFileName );
            
            int psize = firstPass( in );
            in.close();            
            // output size of the program
            out.println(psize);
            
            secondPass( out );
            out.close();
        } catch ( Exception e ) {
            System.err.println( e.getMessage() );                    
        }
    }

    // do the first pass
    // read the code and determine the address of each instruction
    // return the total amount of memory used
    protected static int firstPass( BufferedReader in ) throws IOException, FileNotFoundException {
        int instAddr = 0;           // address of the next instruction
        
        // first pass - read lines and determine locations
        while ( true ) {
            String asmLine = in.readLine();
            
            // end of input?
            if ( asmLine == null )
                return instAddr;
			
            // instruction to be added to the program
            ProcInstruction inst = new ProcInstruction( instAddr, asmLine );
            
			// empty line?
			if ( asmLine.length() == 0 )
				continue;
			
            // prepare for scanning
            Scanner scanLine = new Scanner( asmLine );
            
            // process a label if we have one
            // labels must begin in first column
            if ( Character.isAlphabetic( asmLine.charAt(0) ) ) {
                String label = scanLine.next();
                
                // add this label
                symbolTable.put( label, instAddr );
            }
            
            // get "opcode"
            if ( ! scanLine.hasNext() ) {
                // empty line (except possibly for label)
                continue;
            }
            
            // get the next item (opcode)
            String op = scanLine.next();
            // nothing before a comment?
            if ( op.charAt(0) == ';' )
                continue;
            
            // so we do have something
            // what type of "instruction"?
            switch( op.charAt(0) ) {
                // computation statement
                case 'C':
                case 'c':
                    inst.type = ProcInstruction.INST_COMP;
                    instAddr++;     // one byte instruction
                    break;

                // blocking input/output statement
                case 'I':
                case 'i':
                    inst.type = ProcInstruction.INST_BLOCK;                       
                    instAddr++;     // one byte instruction
                    break;  

                // branch statement
                case 'B':
                case 'b':
                    inst.type = ProcInstruction.INST_BR;     
                    instAddr += 3;      // three byte instruction
                                        // opcode + 2 byte destination
                    // save scanner for getting percentage/destination label
                    inst.lineScan = scanLine;
                    break;
                    
                // exit this program
                case 'X':
                case 'x':
                    inst.type = ProcInstruction.INST_EXIT;    
                    instAddr++;     // one byte instruction                    
                    break;

                default:
                    System.err.println("Improper instuction token in " + asmLine );
                    continue;       // skip this 
            }
            // save this for the second pass
            program.add(inst);
        }
    }
    
    // do second pass and generate code
    public static void secondPass( PrintWriter out ) {        
        // generate the actual code        
        // primarily now look up branch destinations
        for( ProcInstruction inst : program ) {
            switch( inst.type ) {
                case ProcInstruction.INST_COMP:     // computation
                case ProcInstruction.INST_BLOCK:    // blocking (input/output)
                case ProcInstruction.INST_EXIT:     // exit program
                    // generate the instruction
                    out.println(inst.type);
                    break;
                case ProcInstruction.INST_BR:       // Branch (128-228) with jump percentage
                    // get the branch percentage
                    int brPercent = inst.lineScan.nextInt();
                    // check for valid range
                    if ( brPercent < 0 || brPercent > 100 ) {
                        // illegal percentage
                        System.out.println("Error: branch percentage "
                                + brPercent + " is outside of 0..100");
                        System.exit(1);
                    }
                    
                    // isolate the destination label
                    String destLabel = inst.lineScan.next();
                    
                    // does the label exist?
                    if ( ! symbolTable.containsKey(destLabel)) {
                        // can't find the destionation label
                        System.out.println("Error: Cannot find the branch destionation " + destLabel );
                        System.exit(1);                        
                    }
                    
                    // look up label, get corresponding location
                    int brDest = (Integer) symbolTable.get(destLabel);
                    
                    // generate the instruction and
                    out.println(ProcInstruction.INST_BR+brPercent);
                    //      destination in big-endian order
                    out.println(brDest>>8);
                    out.println(brDest & 0x0ff);
                    break;
                default:
                    System.err.println("Internal error: second pass with type=" + inst.type);
            }
        }
    }
}

