import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.lang.Object; 

public class Cache {
	
	public static int BITS_IN_WORD = 32;
	public int offsetLength;
	public int setLength;
	public int tagLength;
	
//	public int offsetMask;
//	public int setMask;
//	public int tagMask;
	
	int[][] cache;
	ArrayList<int[][]> LRUArrays;
	
	int totalCacheHits;
	int totalCacheMisses;
	
	
	public Cache(int l, int k, int n){
		// L = Line length ( number of bytes per cache line )
		// K = Number of directories ( also as a result the number of tags)
		// N = Number of sets (i.e: number of cache lines)

/*---------------------------------------Main Cache Array-----------------------------------------*/
		
		//For our very basic cache simulation we only need to know if it the program hits/misses
		//We don't actually care about the data itself.
		//Therefore we need 1 row per set, and 1 column per tag.
		
		int numRows = n;
		int numColumns = k;
		
		cache = new int[numRows][numColumns];
		//Can be accessed like: cache[setNumber][tagNumber]
		
/*---------------------------------------LRU Arrays---------------------------------------------*/
		
		//Then for each set (row) we need a numTags*numTags array to handle our LRU policy.
		
		//List of 2D integer arrays (all defaulted to 0's)
		LRUArrays = new ArrayList<int[][]>();
		
		for(int i = 0; i < n; i++){
			int[][] LRUArray = new int[k][k];
			LRUArrays.add(LRUArray);
		}
		
		
/*-------------------------------------Setting up bit masks---------------------------------------*/
		
		// Compute lengths of tag, set & offset:
		offsetLength 	= (int) (Math.log(l)/Math.log(2));
		setLength 		= (int) (Math.log(n)/Math.log(2));
		tagLength 		= BITS_IN_WORD - (offsetLength + setLength);
		
//		//Create mask of ones from these lengths (will be used to isolate those bits).
//		offsetMask 	= createMask(offsetLength);
//		setMask 	= createMask(setLength);
//		System.out.println(setMask);
//		tagMask 	= createMask(tagLength);
		
		
/*-------------------------------------Instantiating Counters-------------------------------------*/
		
		totalCacheHits 				= 0;
		totalCacheMisses			= 0;
	}
	
//	public int createMask(int len){
//		int mask = 0;
//		if(len >= 1){
//			for(int i = 0; i < len; i++){
//				mask *= 10;
//				mask += 1;			
//			}
//		}
//		return mask;	
//	}
	
//	public int getOffset(int address){
//		return(address & offsetMask);
//	}
	
	public int getSet(int address){
		//System.out.println("Address: " + Integer.toBinaryString(address));
//		int x = ((address >> offsetLength) & setMask);
//		int x = ((address >> offsetLength) & 0x7FF);

//		System.out.println("Set mask: " + Integer.toBinaryString(setMask));
		//System.out.println("Set: " + Integer.toBinaryString(x));
//		return x;
		address >>= offsetLength;
		int x = 0;
		for(int i = 0; i < setLength; i++){
			x = (x << 1) | ((address >> 1) & 1); // get bit from address 
			address >>= 1; // update address variable
		}
		return x;
	}
	
	public int getTag(int address){
		address >>= (offsetLength + setLength);
		int x = 0;
		for(int i = 0; i < tagLength; i++){
			x = (x << 1) | ((address >> 1) & 1); // get bit from address 
			address >>= 1; // update address variable
		}
		return x;
	}
	
	
	public void accessCache(int address, int burstCount){
		int set = getSet(address);
//		System.out.println("Set: "+set);
		int tag = getTag(address);
//		System.out.println("Tag: "+tag);
		boolean cacheHit = false;
		
		//Loops through all tags of set.
		for(int i = 0; i < cache[0].length; i++){
			if(cache[set][i] == tag){
				
				/*------------------Cache hit-------------------*/			
				cacheHit = true;
				
				//Update counters
				totalCacheHits += (burstCount + 1);
				
				//Update LRU
				updateLRUArrays(LRUArrays.get(set), i);
			}
		}
		if(cacheHit == false){
			/*------------------Cache miss-------------------*/
			//Update counters
			totalCacheMisses++;
			totalCacheHits += burstCount;
			
			int oldestTagIndex = findLRUTag(LRUArrays.get(set));
			updateCache(set, oldestTagIndex, tag);
		}		
	}

	public void updateLRUArrays(int[][] LRUArray, int tagIndex){
		// 1) Set row to 1's.
		// 2) Set column to 0's.
		
		for(int i = 0; i < LRUArray[0].length; i++){
			LRUArray[tagIndex][i] = 1;
		}
		for(int j = 0; j < LRUArray.length; j++){
			LRUArray[j][tagIndex] = 0;
		}
	}
	
	public int findLRUTag(int[][] LRUArray){
		int i = 0;
		boolean done = false;
		int returnTag = 0;
		
		//For each row.
		while(i < LRUArray.length && !done){
			int count = 0;
			
			//For each column
			for(int j = 0; j < LRUArray[0].length; j++){
				if(LRUArray[i][j] == 0){
					count++;
				}
			}
			if(count == LRUArray[0].length){
				returnTag = i;
				done = true;
			}
			i++;
		}
		return returnTag;
	}
	
	public void updateCache(int set, int oldTagIndex, int newTag){
		cache[set][oldTagIndex] = newTag;		
	}
	
/*
 	public static void main(String[] args){
		TraceFile inputFile = new TraceFile("src/gcc1.trace");
		Iterator<AddressRecord> addressRecords = inputFile.getRecordsData();
		
		for(int i = 0; addressRecords.hasNext(); i++){
			System.out.println("MemoryIO: " + addressRecords.next().memoryIO);
		}
		
	}
*/
	
}
