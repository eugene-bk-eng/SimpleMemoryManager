 /*
 * 
 */
package com.ocean927.memory.test;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Class is used to for testing of buddy memory manager.
 * https://en.wikipedia.org/wiki/Buddy_memory_allocation
 * 
 * The implementations are presented through an interface. Two flavors are tested
 * 1. An on-heap memory manager which uses byte[] array to allocate and deallocate blocks.
 * 2. An off-heap memory manager. That memory is not collected by GC.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.ocean927.memory.client.MemoryClientInterface;
import com.ocean927.memory.impl.AbstractMemoryManagerAlgorithm;
import com.ocean927.memory.impl.AlgoImplEnum;
import com.ocean927.memory.impl.MemoryManagerException;
import com.ocean927.memory.impl.MemoryMgrFactory;
import com.ocean927.memory.impl.OffHeapMemoryMgrImpl;
import com.ocean927.memory.utils.Formatter;
import com.ocean927.memory.utils.Mean;
import com.ocean927.memory.utils.Profiler;
import com.ocean927.memory.utils.SysUtils;

/**
 * The Class TestMemoryMgr.
 */
public class TestMemoryMgr {
	
	private final Logger logger = Logger.getLogger(TestMemoryMgr.class);
	
	public TestMemoryMgr() throws MemoryManagerException {
		// for testing.
		unsafe = getUnsafe();
	}

	/**
	 * Test.
	 * @throws Exception the exception
	 */
	public void test() throws MemoryManagerException, IOException {

		// CHOOSE MEMORY MANAGER IMPLEMENTATION
		impl=MemoryMgrFactory.getImplementation(AlgoImplEnum.OFF_HEAP);
		//impl=MemoryMgrFactory.getImplementation(AlgoImplEnum.ON_HEAP);
		
		// EXECUTE TEST METHODS
		//test_simple_methods(); // sequence of allocate and deallocate
		//test_random_combination_of_alloca_malloc(); // random sequences of calls followed by full defragmentation.
		//test_allocation_performance();   // performance test of buddy memory manager
		//test_defragmentation_block();
		
		// TEST UNSAFE
		//testUnsafeWriter();
		testUnsafeReader();
		
		// CLEAN UP
		if( impl instanceof OffHeapMemoryMgrImpl ) { impl.freeMemory(); }
	}
	
	/**
	 *  method: simple sequence of allocate and deallocate methods.
	 *
	 * @throws Exception the exception
	 */
	public void test_simple_methods() throws MemoryManagerException {
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", BEG"); long l_beg=System.currentTimeMillis();
		
		// SETUP
		impl.setup((long)1024*1024*1024l,1024);
		
		impl.print();
		
		// BEGIN TEST
		List<Long> listAllocated=new ArrayList<>();
		int request=802; long address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address ); 
		request=1; address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address );
		request=1; address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address );
		request=1; address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address ); 
		request=6969; address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address );
		request=5148; address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address );
		request=6327; address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address );		
		
		impl.print();
		
		for (Long a: listAllocated) {		
			impl.deallocate(a);		
		}
		
		impl.print();
		
		logger.info("DEFRAGMENT");
		p.b(); impl.defragment(); p.e();logger.info("Defragmention in " + p.reportTimingMini() );

		impl.print();
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", END, elapsed " + Formatter.getDiff(System.currentTimeMillis(),l_beg));
	}
	
	/**
	 *  method: shows that defragmentation without moving used memory blocks
	 *  can't always solve allocation challenge.
	 *
	 * @throws Exception the exception
	 */
	public void test_defragmentation_block() throws MemoryManagerException {
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", BEG"); long l_beg=System.currentTimeMillis();		
		
		// SETUP
		impl.setup(64*1024l,1024);	
		
		// BEGIN TEST
		List<Long> listUsed=new ArrayList<>(); 
		{
			int request=12000; long address=impl.allocate(request); if(address>=0) {logger.info("successful allocation, requesting: " + request + " b, address: " + address); }else{logger.info("un-successful allocation, request: " + request); }
			if( address>=0 ) { listUsed.add(address); }
		} 
		{
			int request=1; long address=impl.allocate(request); if(address>=0) {logger.info("successful allocation, requesting: " + request + " b, address: " + address); }else{logger.info("un-successful allocation, request: " + request); }
			if( address>=0 ) { listUsed.add(address); }
		}		
		{
			int request=12000; long address=impl.allocate(request); if(address>=0) {logger.info("successful allocation, requesting: " + request + " b, address: " + address); }else{logger.info("un-successful allocation, request: " + request); }
			if( address>=0 ) { listUsed.add(address); }
		}
		impl.print();
		
		// free now
		listUsed.stream().forEach((Long address)->{
			try {impl.deallocate(address);} catch (MemoryManagerException e) {
				logger.error(e);
		}});
		
		logger.info("after freeing");
		impl.print();
		
		p.b(); impl.defragment(); p.e();logger.info("Defragmention in " + p.reportTimingMini() );
		impl.print();
		
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", END, elapsed " + Formatter.getDiff(System.currentTimeMillis(),l_beg));
	}
	
	/**
	 *  method: a random combination of calls followed by free the memory and defragmentation.
	 *
	 * @throws Exception the exception
	 */
	public void test_random_combination_of_alloca_malloc() throws MemoryManagerException {
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", BEG"); long l_beg=System.currentTimeMillis();
		impl.setup((long)128*1024*1024l,1024);
		List<Long> listAllocated=new ArrayList<Long>(); 
		impl.print();		

		logger.info("");
		int N=1000;
		for (int i = 1; i<=N; i++) {			
			if( rnd.nextBoolean() ) {				
				// attempt to allocate a block 
				int r=rnd.nextInt(62000)+1;
				//l("Seq: " + (i+1) + ", alloc, r:=" + r);
				long address=impl.allocate(r); 
				if( address>=0 ) { if(address>=0) { listAllocated.add(address); } logger.info((i) + ", Allocated: " + r + " bytes at address: " + address); }
				else{ logger.info((i) + ", Allocation of " + r + " failed"); }
			}else{
				// deallocate all
				// for (Integer a: listAllocated) {  deallocate(a);  }
				if( listAllocated.size()>0 ) { 
					long address=listAllocated.remove(listAllocated.size()-1);					
					int pagesFreed=impl.deallocate(address);
					logger.info((i) + ", De-allocating at address: " + address + ", pages freed: " + pagesFreed);
				}else{
					logger.info((i) + ", De-allocating empty"); 
				}
			}
			if( i%100==0) {
				//impl.printMem();
				//l("BEG DEFRAGMENTING");
				impl.defragment();
			}
		}		
		
		impl.print();
		
		logger.info("BEG DEALLOCATING");
		for (Long a: listAllocated) {  impl.deallocate(a);  }
		
		//impl.printMem();
		logger.info("BEG DEFRAGMENTING");
		p.b(); impl.defragment(); p.e();logger.info("Defragmention in " + p.reportTimingMini() );
				
		impl.print();
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", END, elapsed " + Formatter.getDiff(System.currentTimeMillis(),l_beg));
	}
	
	/**
	 *  method: tests performance. you can't use N too high 
	 * because the tests calculates statistics accumulating values
	 * and accumulates memory allocations in the list.
	 * N should be roughly 10M.
	 * @throws IOException 
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings(value = { "unused" })
	public void test_allocation_performance() throws MemoryManagerException, IOException {
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", BEG"); long l_beg=System.currentTimeMillis();
		//
		impl.setup("512 mb");
		int N=10*1000*1000; 
		//
		List<Long> listAllocated=new ArrayList<Long>();
		
		// warm up, letting JIT kick to pre-compile methods		
		for (int i = 1; i<=20000; i++) {											
			int r=rnd.nextInt((int)impl.getMemoryAllocated())+1;
			long address=impl.allocate(r); 			
			impl.deallocate(address);												
		}
		impl.defragment();
		
		// main cycle
		Mean meanAllocation=new Mean(); 
		Mean meanDeallocation=new Mean(); 		
		long sumAllocated=0; long cntAllocationCalls=0; long wastedCalls=0;
		for (int i = 1; i<=N; i++) {			
			if( rnd.nextBoolean() ) {				
				// attempt to allocate a block 				
				int r=rnd.nextInt((int)impl.getMemoryAllocated())+1;
				p.b(); long address=impl.allocate(r); p.e(); meanAllocation.add( p.getTimeNanoseconds() ); 
				if( address>=0 ) { listAllocated.add(address); sumAllocated+=r; cntAllocationCalls++; }
			}else{
				// deallocate all
				if( listAllocated.size()>0 ) { 
					long address=listAllocated.remove(listAllocated.size()-1);					
					p.b(); int pagesFreed=impl.deallocate(address); p.e(); meanDeallocation.add( p.getTimeNanoseconds() );	 				
				}else{ wastedCalls++; }
			}
			if( i%1000000==0 ) {logger.info("processed " + Formatter.fl(i) ); }
		}		
		
		logger.info("BEG DEALLOCATING");
		for (Long a: listAllocated) {  impl.deallocate(a);  }
		
		logger.info("BEG DEFRAGMENTING");
		p.b(); impl.defragment(); p.e();logger.info("Defragmention in " + p.reportTimingMini() );
				
		impl.print();
		
		logger.info("Allocation calls: " + Formatter.fl(cntAllocationCalls) + ", Wasted calls: " + Formatter.fl(wastedCalls) + ",  Allocated sum of " + sumAllocated + ", " + Formatter.fl(sumAllocated/(1024*1024)) + " MB., " + Formatter.fl(sumAllocated/(1024*1024*1024l)) + " GB., " + Formatter.fl(sumAllocated/(1024*1024*1024*1024l)) + " TB." );
		
		ResultWrapper w1=new ResultWrapper(); w1.n=N; w1.min=meanAllocation.getMin(); w1.mean=meanAllocation.getMean(); w1.median=meanAllocation.getAverage(); w1.max=meanAllocation.getMax();
		ResultWrapper w2=new ResultWrapper(); w2.n=N; w2.min=meanDeallocation.getMin(); w2.mean=meanDeallocation.getMean(); w2.median=meanDeallocation.getAverage(); w2.max=meanDeallocation.getMax();
		logger.info("---ALLOCATIONS---");
		printStatsTable(w1, "ALLOC");
		logger.info("---DEALLOCATIONS---");
		printStatsTable(w2, "DEALLOC");
		//
		logger.info("\nPERFORMANCE STATS in nanoseconds"); 		
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", END, elapsed " + Formatter.getDiff(System.currentTimeMillis(),l_beg));
	}
	
	/**
	 * The Class ResultWrapper.
	 */
	class ResultWrapper {
		
		/** The n. */
		long n;
		
		/** The min. */
		double min;
		
		/** The max. */
		double max;
		
		/** The mean. */
		double mean;
		
		/** The median. */
		double median;
		
		/** The stdev. */
		double stdev;
	}
	
	
	/**
	 * Prints the stats table.
	 *
	 * @param w the w
	 * @param label the label
	 * @param out the out
	 * @throws Exception the exception
	 */
	public void printStatsTable(ResultWrapper w, String label) throws IOException {
		logger.info( (Formatter.padInFront("LABEL", 10, " ") + "," + Formatter.padInFront("N", 10, " ") + "," + Formatter.padInFront("MEDIAN", 10, " ") + "," + Formatter.padInFront("MEAN", 10, " ") + "," + Formatter.padInFront("STDEV(nano)", 10, " ") + "," + Formatter.padInFront("MIN(nano)", 10, " ") + "," + Formatter.padInFront("MAX(nano)", 10, " ")));
						
		logger.info( (Formatter.padInFront(label, 10, " ") + "," + 
					  Formatter.padInFront(Long.toString(w.n), 10, " ") + "," +
					  Formatter.padInFront("" + Formatter.dbl(w.median), 10, " ") + "," +
					  Formatter.padInFront("" + Formatter.dbl(w.mean), 10, " ") + "," +
					  Formatter.padInFront("" + Formatter.dbl(w.stdev), 10, " ") + "," +
					  Formatter.padInFront("" + Formatter.dbl(w.min), 10, " ") + "," +
					  Formatter.padInFront("" + Formatter.dbl(w.max), 10, " ")) );		
	}
	
	/** shows off heap space can be used between JVMs 
	 * @throws MemoryManagerException */
	void testUnsafeWriter() throws MemoryManagerException {
		//
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", BEG"); long l_beg=System.currentTimeMillis();
		
		// SETUP
		impl.setup((long)1024*1024l,1024);
		
		impl.print();
		
		// BEGIN TEST
		List<Long> listAllocated=new ArrayList<>();
		int request=100; long address=impl.allocate(request); if(address>=0) { listAllocated.add(address); }logger.info("Requesting=" + request + ", Address=" + address );
		logger.info("Block at address:"+address);
		
		String str="Hello World";
		byte b[]=str.getBytes();
		impl.writeIntToByteArray(str.length(), address, 0);
		for (int i = 0; i < b.length; i++) {
			impl.writeByteToByteArray(b[i], address, i+4);	
		}		
		
		try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace(); }
		
		logger.info("DEFRAGMENT");
		p.b(); impl.defragment(); p.e();logger.info("Defragmention in " + p.reportTimingMini() );

		impl.print();
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", END, elapsed " + Formatter.getDiff(System.currentTimeMillis(),l_beg)); 
	}
	
	/** INCORRECT: shows off heap space can be used between JVMs 
	 * @throws MemoryManagerException */
	void testUnsafeReader() throws MemoryManagerException {
		//
		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", BEG"); long l_beg=System.currentTimeMillis();

		logger.info("Input memory address:");
		String args[]=readConsole();
		long memoryOffHeapAddress=Long.parseLong(args[0]);
		long start=Long.parseLong(args[1]);
		//
		System.out.println("ACCESSING memoryOffHeapAddress: " + memoryOffHeapAddress + ", start: " + start);
		int msgLength=readIntFromByteArray(memoryOffHeapAddress, start, 0);
		//
		byte b[]=new byte[msgLength];
		for (int i = 0; i < b.length; i++) {
			b[i]=readByteFromByteArray(memoryOffHeapAddress, start, i+4);	
		}		
		String message=new String(b);
		logger.info("MESSAGE: " + message);

		logger.info(SysUtils.getClassName() + "." + SysUtils.getMethodName() + ", END, elapsed " + Formatter.getDiff(System.currentTimeMillis(),l_beg)); 
	}
	
	@SuppressWarnings("restriction")
	public int readIntFromByteArray(long memoryOffHeapAddres, long start, long index) throws MemoryManagerException {		
		return unsafe.getInt(memoryOffHeapAddres+start+index+AbstractMemoryManagerAlgorithm.HEADER_LENGTH);
	}
	
	@SuppressWarnings("restriction")
	public byte readByteFromByteArray(long memoryOffHeapAddres, long start, long index) throws MemoryManagerException {		
		return unsafe.getByte(memoryOffHeapAddres+start+index+AbstractMemoryManagerAlgorithm.HEADER_LENGTH);
	}
	
	/**
	 * Gets the unsafe.
	 *
	 * @return the unsafe
	 * @throws MemoryManagerException the memory manager exception
	 */
	@SuppressWarnings(value = { "restriction" })
	public static sun.misc.Unsafe getUnsafe() throws MemoryManagerException {
	  try {	
		Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
		return unsafe;
	  }catch(Exception e) {
		  throw new MemoryManagerException(e);
	  }
	}
	
	String[] readConsole() {
		Scanner console = new Scanner(System.in);
        String str=console.nextLine();
        String args[]=str.split("\\s+");
        return args;
	}
	
	String readConsoleString() {
		Scanner console = new Scanner(System.in);
        String str=console.nextLine();                              
        return str;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception 
	 */
	public static void main(String args[]) throws Exception {
		 (new TestMemoryMgr()).test();	
	}

	/** Random generator */
	private Random rnd=new Random();
	
	private sun.misc.Unsafe unsafe;	
	
	/** Reference to implementation thru interface */
	private MemoryClientInterface impl;
	
	/** Profiler */
	private Profiler p=new Profiler();
}