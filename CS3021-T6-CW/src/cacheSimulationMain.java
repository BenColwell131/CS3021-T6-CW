import java.text.DecimalFormat;
import java.util.Iterator;

public class cacheSimulationMain {
	
	public static void main(String[] args){
		
		final long startTime = System.currentTimeMillis();
		
		
		TraceFile inputFile = new TraceFile("src/gcc1.trace");
		Iterator<AddressRecord> addressRecords = inputFile.getRecordsData();
		
		Cache dataCache 		= new Cache(16, 8, 256);
		Cache instructionCache 	= new Cache(16, 1, 1024);
		
		while(addressRecords.hasNext()){
			AddressRecord currentRecord = addressRecords.next();
			
			//Need to sort the addresses to the appropriate cache.
			if(!currentRecord.memoryIO){
				if(!currentRecord.dataControl && !currentRecord.writeRead){
					//Must be an instruction read
					instructionCache.accessCache(currentRecord.address, currentRecord.burstCount);
				}
				else if(currentRecord.dataControl){
					//Must be a data read or write.
					dataCache.accessCache(currentRecord.address, currentRecord.burstCount);
				}
			}
		}
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime) + " ms.");
		
		DecimalFormat percentageFormat = new DecimalFormat("#.00");
		
		//Instruction cache results:
		
		System.out.println("Instruction Cache:");
		System.out.println("	Cache Hits: " + instructionCache.totalCacheHits);
		System.out.println("	Cache Misses: " + instructionCache.totalCacheMisses);
		
		double instructionAccesses = instructionCache.totalCacheHits + instructionCache.totalCacheMisses;
		double instructionHitRate = (instructionCache.totalCacheHits / instructionAccesses )*100;
		
		System.out.println("	Hit Rate: " + percentageFormat.format(instructionHitRate) + "%");
		
		// Data cache results:
		
		System.out.println("Data Cache:");
		System.out.println("	Cache Hits: " + dataCache.totalCacheHits);
		System.out.println("	Cache Misses: " + dataCache.totalCacheMisses);
		
		double dataAccesses = dataCache.totalCacheHits + dataCache.totalCacheMisses;
		double dataHitRate = (dataCache.totalCacheHits / dataAccesses )*100;
		System.out.println("	Hit Rate: " + percentageFormat.format(dataHitRate) + "%");
		
		
	}

}
