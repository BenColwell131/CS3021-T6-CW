import java.util.ArrayList;
import java.io.*;
import java.nio.ByteBuffer;  


/*
 * This class is purely to deal with the input binary trace file.
 * It aims to break it into more usable data as per the specifications of the assignment.
 * See: https://www.scss.tcd.ie/~jones/CS3021/t6%20CW.pdf for details.
 * 
 * Upon the recommendation of a colleague I am using ArrayLists and a buffered input stream.
 * 
 * @Author: Ben Colwell
 * @Date: 19/12/2017
 * 
 */

class AddressRecord{
	
	// L -> R of Word0
	//Originally used integers for most of these but think Boolean values are actually
	//cleaner for the 1 bit variables.
	boolean memoryIO;
	boolean dataControl;
	boolean writeRead;
	int burstCount;		//2 bit
	boolean[] busEnableSignals = new boolean[4];	//One bit for each signal	
	int address;
	
	//Word1
	int intervalCount;
}

public class TraceFile {
	
	public static final int BYTES_IN_WORD = 4;

	public ArrayList<AddressRecord> addressRecords = new ArrayList<AddressRecord>();
	
	public TraceFile(String traceFilename){
		
		File file = new File(traceFilename);
		try {
			//Using a buffered input stream - this requires a standard input stream to be passed to it.
			FileInputStream basicInputStream = new FileInputStream(file);
			
			BufferedInputStream inputStream = new BufferedInputStream(basicInputStream);
						
			byte[] currentWordArray = new byte[BYTES_IN_WORD];
			
			//To check if we have reached the end of the file.
			int returnCode = inputStream.read(currentWordArray); 
			
			//To make this word useful we really need it in int form.
			//This translates the byte array to an int:
			int currentWord = ByteBuffer.wrap(currentWordArray).getInt();
			
			while(returnCode != -1){ // While not EOF
				
				/*---------------Word0---------------*/
				AddressRecord currentRecord = new AddressRecord();
				
				//Masking out relevant bits
				currentRecord.memoryIO 		= ((currentWord >> 31) & 1) == 1;	//True if 1, False if 0.
				currentRecord.dataControl 	= ((currentWord >> 30) & 1) == 1;	//True if 1, False if 0.
				currentRecord.writeRead 	= ((currentWord >> 29) & 1) == 1;	//True if 1, False if 0.
				
				//TODO check this is returning an int correctly (i.e: 3 not 11 etc).
				currentRecord.burstCount	= ((currentWord >> 27) & 11) + 1;
				
				currentRecord.busEnableSignals[3] = ((currentWord >> 26) & 1) == 1;
				currentRecord.busEnableSignals[2] = ((currentWord >> 25) & 1) == 1;
				currentRecord.busEnableSignals[1] = ((currentWord >> 24) & 1) == 1;
				currentRecord.busEnableSignals[0] = ((currentWord >> 23) & 1) == 1;
				
				// Address bits A0 and A1 are not stored in the trace as they are always 0.
				//Hence the '<< 2'.
				currentRecord.address 		= (currentWord & 0x007FFFFF) << 2;
				
				/*--------------Word1-------------------*/
				returnCode = inputStream.read(currentWordArray);
				
				if (returnCode != -1){
					currentWord = ByteBuffer.wrap(currentWordArray).getInt();
					
					currentRecord.intervalCount = (currentWord & 11111111);
					
					addressRecords.add(currentRecord);
					
					/*-------Setup next word0-------*/
					returnCode = inputStream.read(currentWordArray);
					
					if(returnCode != -1){
						currentWord = ByteBuffer.wrap(currentWordArray).getInt();
					}
				}
				/* End of while loop */
			}
			
			
/*---------------------------------Close Input Streams---------------------------------------------*/
			basicInputStream.close();
			inputStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
//Simple function so that we can pull out records data.	
public ArrayList<AddressRecord> getRecordsData(){
	return addressRecords;
}
	

}
