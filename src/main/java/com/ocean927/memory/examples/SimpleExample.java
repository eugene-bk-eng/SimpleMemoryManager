/*
 * 
 */
package com.ocean927.memory.examples;

import org.apache.log4j.Logger;

import com.ocean927.memory.client.MemoryClientInterface;
import com.ocean927.memory.impl.AlgoImplEnum;
import com.ocean927.memory.impl.MemoryManagerException;
import com.ocean927.memory.impl.MemoryMgrFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleExample.
 */
public class SimpleExample {
		
	/** The logger. */
	private final Logger logger = Logger.getLogger(TestMemoryMgr.class);
	
	/**
	 * Instantiates a new simple example.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public SimpleExample() throws MemoryManagerException {
	}	
	
	/**
	 * Test.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void test() throws MemoryManagerException {
		//
		logger.info("Starting test");
		// use either of two algorithms
		MemoryClientInterface impl_offheap=MemoryMgrFactory.getImplementation(AlgoImplEnum.OFF_HEAP);
		//MemoryClientInterface impl_onheap=MemoryMgrFactory.getImplementation(AlgoImplEnum.ON_HEAP);

		// set up 64MB with 1K page
		impl_offheap.setup(64*1024*1024,1024);
		// another notation: impl_offheap.setup("64 kb", "1 kb");

		// allocate 10K bytes
		long address=impl_offheap.allocate(10000);

		printSeparator();
		// write integer values into address space [0...10000-1]
		int myIntWriting1=7;
		int myIntWriting2=-500;		
		impl_offheap.writeIntToByteArray( myIntWriting1, address, 0 );
		impl_offheap.writeIntToByteArray( myIntWriting2, address, 4 ); // for ints step by 4.
		logger.info("Wrote two values to memory, myIntWriting1:=" + myIntWriting1 + ", myIntWriting2:=" + myIntWriting2);

		// read back
		int myIntRead1=impl_offheap.readIntFromByteArray( address, 0 );
		int myIntRead2=impl_offheap.readIntFromByteArray( address, 4 );
		logger.info("Read two values from memory, myIntRead1:=" + myIntRead1 + ", myIntRead2:=" + myIntRead2);

		printSeparator();
		// when done release memory back to the pool
		impl_offheap.deallocate(address);

		printSeparator();
		// show memory usage
		impl_offheap.print();
	}
	
	/**
	 * Prints the separator.
	 */
	protected void printSeparator() {
		logger.info("--------------------------------------");
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String args[]) throws Exception {
		 (new SimpleExample()).test();	
	}
}
