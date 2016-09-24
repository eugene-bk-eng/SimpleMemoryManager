package com.ocean927.memory.impl;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ocean927.memory.client.MemoryClientInterface;

public class FunctionalTest {
	private final Logger logger = Logger.getLogger(AbstractMemoryManagerAlgorithm.class);
	private MemoryClientInterface impl_on_heap=null;
	private MemoryClientInterface impl_off_heap=null;
	
	/**
	 * Test reading and writing particular implementation.   
	 */
	@Test
	public void testFunctional_OnHeap() {
		try {
			
			//impl_on_heap.setup(8*1024*1024, 1024);
			impl_on_heap.setup("16 kb", "1 kb");
			
			testFunctional(impl_on_heap);
			
		} catch (MemoryManagerException e) { e.printStackTrace(); }								
	}
	
	/**
	 * Test reading and writing particular implementation.   
	 */
	@Test
	public void testFunctional_OffHeap() {
		try {
			
			impl_off_heap.setup(64*1024*1024, 1024);
			//impl_off_heap.setup("64 kb", "1 kb");
			
			testFunctional(impl_off_heap);
			
		} catch (MemoryManagerException e) { e.printStackTrace(); }								
	}
		
	public void testFunctional(MemoryClientInterface impl) {
		try {			
						
			// data blocks			
			Random rnd=new Random();
			long trials=3;
			for (int trial = 1; trial <= trials; trial++) {
				logger.info("TRIAL #" + trial );
				Map<Long,Byte[]> mapWritten=new HashMap<>();
				int failureAttempts=0;
				while(failureAttempts<10) {			
					int requestingBlock=rnd.nextInt((int)impl.getMemoryAllocated())+1;
					//int requestingBlock=rnd.nextInt(1000)+1;
					long addressOfBlock=impl.allocate(requestingBlock);
					// write
					if( addressOfBlock>=0) { 
						// create random block
						Byte b[]=new Byte[requestingBlock];
						for (int i = 0; i < b.length; i++) { 
							b[i]=(byte)rnd.nextInt(); 
							//if( i<3 ) { logger.info("writing[" + i + "]=" + b[i]); }
						}
						mapWritten.put(addressOfBlock, b);
						// write it in
						for (int i = 0; i < b.length; i++) { 
							impl.writeByteToByteArray(b[i], addressOfBlock+i);
							//failureAttempts=11;break;
						}						
					}else{
						failureAttempts++;
					}
				}
				
				//impl.print();
				
				// read back and compare
				logger.info("Created " + mapWritten.keySet().size() + " unique allocation requests." );
				for (Long addressOfBlock:mapWritten.keySet()) {					
					Byte b[]=(Byte[]) mapWritten.get(addressOfBlock);
					logger.info("Reading back data, addressOfBlock:=" + addressOfBlock + ", array.len:=" +  b.length );
					for (int i = 0; i < b.length; i++) {
						byte expected=b[i];
						byte actual=impl.readByteFromByteArray(addressOfBlock+i*1);
						assertEquals("Failed comparison at index#:=" + i, expected, actual);
					}
				}
				// deallocate
				for (Long addressOfBlock:mapWritten.keySet()) {
					logger.info("Deallocating addressOfBlock:=" + addressOfBlock );
					impl.deallocate(addressOfBlock);
				}
				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void before() {
		// CHOOSE MEMORY MANAGER IMPLEMENTATION 
		try {
			impl_on_heap=MemoryMgrFactory.getImplementation(AlgoImplEnum.ON_HEAP);
			impl_off_heap=MemoryMgrFactory.getImplementation(AlgoImplEnum.OFF_HEAP);
		} catch (MemoryManagerException e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void after() {
		// CHOOSE MEMORY MANAGER IMPLEMENTATION 
		try {
			if(impl_on_heap!=null) {
				impl_on_heap.freeMemory();
			}
			if(impl_off_heap!=null) {
				impl_off_heap.freeMemory();
			}
		} catch (MemoryManagerException e) {
			e.printStackTrace();
		}		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	@AfterClass
	public static void tearDownAfterClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}	
}
