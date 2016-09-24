package com.ocean927.memory.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.ocean927.memory.client.MemoryClientInterface;
import com.ocean927.memory.utils.ByteUtils;
import com.ocean927.memory.utils.Formatter;

public class AlgorithmTest {
	
	private final Logger logger = Logger.getLogger(AbstractMemoryManagerAlgorithm.class);
	private MemoryClientInterface impl=null;
		
	/**
	 * This test asserts that every allocation and deallocation up 
	 * to the maximum requested size sets the block header. 
	 * 
	 * This test is implementation specific looking 
	 * at private methods and fields. It does not test 
	 * behaviour seen by the outside observer. It tests 
	 * an internal state of the algorithm.
	 * 
	 */
	@Test
	public void testAllocation() {
		try {
			//
			impl.setup("64 kb", "1 kb");
						
			// 
			long cntSuccessfulAllocations=0; long maxAllocationRequest=0;
			long upto=impl.getMemoryAllocated();
			for (long requestingBlock = 1; requestingBlock<impl.getMemoryAllocated() && requestingBlock<upto; requestingBlock++) {
				// request
				long addressOfBlock=impl.allocate(requestingBlock);
				// check
				if( addressOfBlock>=0) { 
					assertBlockState(addressOfBlock, BlockState.USED );
					impl.deallocate(addressOfBlock);
					assertBlockState(addressOfBlock, BlockState.FREE );
					maxAllocationRequest=Math.max(requestingBlock, maxAllocationRequest);
					cntSuccessfulAllocations++;
				}				
			}
			
			impl.print();
			logger.info("Requests: " + Formatter.fl(cntSuccessfulAllocations) + ", Max Request: " + Formatter.fl(maxAllocationRequest) + " bytes.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	@SuppressWarnings("restriction")
	public void assertBlockState(long addressOfBlock, BlockState state) {
	  try {	
		byte actual=0, expected=0;
		int pagesInBlock=impl.getPagesInThisBlock(addressOfBlock);
		int logof2=ByteUtils.log2_v2(pagesInBlock);
		if( state==BlockState.USED) {
			expected=(byte) ((logof2<<1) | (1)); 
		}else if( state==BlockState.FREE) {
			expected=(byte) ((logof2<<1) | (0)); 
		} 
		if( impl instanceof OffHeapMemoryMgrImpl ) {
			Field fieldunsafe=((OffHeapMemoryMgrImpl)impl).getClass().getDeclaredField("unsafe");
			Field fieldmemoryOffHeapAddres=((OffHeapMemoryMgrImpl)impl).getClass().getDeclaredField("memoryOffHeapAddres");
			fieldunsafe.setAccessible(true);
			fieldmemoryOffHeapAddres.setAccessible(true);
			sun.misc.Unsafe unsafe=(sun.misc.Unsafe) fieldunsafe.get(impl);
			long memoryOffHeapAddres=(long) fieldmemoryOffHeapAddres.get(impl);
			actual=unsafe.getByte(memoryOffHeapAddres + addressOfBlock);
		}
		else if( impl instanceof OnHeapMemoryMgrImpl ) {											
			Field field=((OnHeapMemoryMgrImpl)impl).getClass().getDeclaredField("memory");
			field.setAccessible(true);
			byte memory[]=(byte[]) field.get(impl);
			actual=memory[(int)addressOfBlock];
		}		
		assertEquals(expected, actual);
		
	  } catch (Exception e) {
			e.printStackTrace();
	  }
	}
	
	enum BlockState{
		USED((byte)1),
		FREE((byte)0);
		byte state;
		private BlockState(byte state) {
			this.state=state;
		}
	}
	
	@Before
	public void before() {
		// CHOOSE MEMORY MANAGER IMPLEMENTATION 
		try {
			impl=MemoryMgrFactory.getImplementation(AlgoImplEnum.ON_HEAP);
		} catch (MemoryManagerException e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void after() {
		// CHOOSE MEMORY MANAGER IMPLEMENTATION 
		try {
			if(impl!=null) {
				impl.freeMemory();
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
