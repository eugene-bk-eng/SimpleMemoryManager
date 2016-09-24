/*
 * Memory manager implementing a buddy-memory model 
 * based on blocks of 2^k [0..N] byte size.
 * 
 * This class is marked abstract. Subclasses provide 
 * concrete implementation. For example, a java version
 * may use a byte array or memory mapped file for storage.
 *  
 * Allocated blocks must be freed manually. There is no automatic 
 * garbage collection. If client forgets to deallocate a block 
 * after its use and loses a reference to it, the block is leaked.
 * 
 * User could always scan all blocks, determine used and free them. 
 * 
 * Class is not thread safe and will not perform correctly under concurrent access.
 * 
 * Like Unix kernel, blocks are composed of pages. Each block has 2^k pages.
 * Page size is dependent on your pattern of allocation and 
 * can help to reduce memory waste and increase performance.
 * For most cases, stick to default page size of 1024 bytes. 
 * 
 * Why use pages?
 * Using pages simplifies math required to allocate/deallocate 
 * blocks and reduces fragmentation for typical access patterns. 
 * 
 * https://en.wikipedia.org/wiki/Buddy_memory_allocation
 *  
 */
package com.ocean927.memory.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ocean927.memory.client.MemoryClientInterface;
import com.ocean927.memory.utils.ByteUtils;
import com.ocean927.memory.utils.Formatter;


/**
 * The Class AbstractMemoryManagerAlgorithm.
 */
public abstract class AbstractMemoryManagerAlgorithm implements MemoryClientInterface {
	
	final Logger logger = Logger.getLogger(AbstractMemoryManagerAlgorithm.class);
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryManagement#setup(java.lang.String)
	 */
	@Override
	public void setup(String memorySizeBytes) throws MemoryManagerException {
		setup(memorySizeBytes, "1 kb"); 
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryManagement#setup(java.lang.String, java.lang.String)
	 */
	@Override
	public void setup(String memorySizeBytes, String pageSizeParam) throws MemoryManagerException {
		setup( Formatter.parseMemory(memorySizeBytes), (int)Formatter.parseMemory(pageSizeParam));
	}
	
	/**
	 *  finds the starting address of the block big enough to fit in requested length.
	 *
	 * @param requestedLength the requested length
	 * @return the long
	 * @throws MemoryManagerException the memory manager exception
	 */
	@Override
	public long allocate(long requestedLength) throws MemoryManagerException {
		long index=-1;
		  if( requestedLength>0 ) {	
			  int minRequiredPages=(int)Math.ceil((double)(requestedLength+1)/pageSize); // +1 to account for block header.
			  // what block size satisfies request? 2^0=1 pg/block, 2^1=2 pgs/block, .... 2^k pgs/block.
			  for (int blockPowerSize = 0; blockPowerSize < pagesPerBlock.length; blockPowerSize++) {
				// which block fits?
				if( minRequiredPages<=pagesPerBlock[blockPowerSize] ) { 
					// are there free blocks in the list?
					if( hasListFreeBlocks(blockPowerSize) ) {
					  index=getLastFreeBlockAddress(blockPowerSize);
					  // take last list
					  split(index, pagesPerBlock[blockPowerSize], minRequiredPages);					  
					  //
					  int p=getPagesInThisBlock(index);
					  setBlockHeaderUsed(index, p);					  
					  terminateBlockFromFreeList(index, p);
					  return index;
					}
				}
			  }
		  }else{ throw new MemoryManagerException("Requested length is wrong " + requestedLength ); }
		return index;
	}
	
	/**
	 *  split the block up to or bigger than minRequiredPages.
	 *
	 * @param freeBlockAddress the free block address
	 * @param pagesInBlock the pages in block
	 * @param minRequiredPages the min required pages
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected void split(long freeBlockAddress, int pagesInBlock, int minRequiredPages) throws MemoryManagerException {
		// can you split the block in half and still satisfy the request?
		int halfPagesInBlock=pagesInBlock/2;
		if( halfPagesInBlock>=minRequiredPages) {
			// split in half. right is free, left is tried to be split again. 
			long newBlockAddress=freeBlockAddress+halfPagesInBlock*pageSize;
			terminateBlockFromFreeList(freeBlockAddress, pagesInBlock); // terminate big block
			placeOnFreeList(newBlockAddress, halfPagesInBlock); // free right block
			placeOnFreeList(freeBlockAddress, halfPagesInBlock); // free left block					
			split(freeBlockAddress, halfPagesInBlock, minRequiredPages); // try to split left block
		}
	}
	
	/**
	 *  
	 * marks the block of given size at that address as free 
	 * and adds the block address to the appropriate size list.
	 * 
	 * index - where block starts [0..N-1]. depends on pageSize
	 * pagesInBlock - block size in pages [1..N/pageSize]
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @param pagesInBlock the pages in block
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected void placeOnFreeList(long addressOfFreeBlock, long pagesInBlock) throws MemoryManagerException {
		// get free list
		long list[]=(long[])free_list[ByteUtils.log2_v2(pagesInBlock)];
		// mark block as free
		setBlockHeaderFree(addressOfFreeBlock, pagesInBlock);
		// add empty block at the end		
		int new_last=(int)list[0]+1;
		list[new_last]=addressOfFreeBlock; // add new element
		list[0]++; // increment counter
	}
	
	/**
	 *  returns first element of the list, a counter of free blocks.
	 *
	 * @param blockSize the block size
	 * @return true, if successful
	 */
	boolean hasListFreeBlocks(int blockSize) {
		long list[]=(long[])free_list[blockSize];
		if( list[0]>0 ) { return true; }
		else{ return false; }
	}
	
	/**
	 * Gets the last free block address.
	 *
	 * @param blockSize the block size
	 * @return the last free block address
	 * @throws MemoryManagerException the memory manager exception
	 */
	long getLastFreeBlockAddress(int blockSize) throws MemoryManagerException {
		if(flagCorrectness) { if( !hasListFreeBlocks(blockSize) ) { throw new MemoryManagerException("No free blocks for " + blockSize); } }
		long list[]=(long[])free_list[blockSize];
		int index_last=(int)list[0];
		return list[ index_last ];
	}
	
	
	/**
	 *  block is terminated due to splitting or combining.
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @param pagesInBlock the pages in block
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected void terminateBlockFromFreeList(long addressOfFreeBlock, int pagesInBlock) throws MemoryManagerException {
		if(flagCorrectness) { if( !hasListFreeBlocks(ByteUtils.log2_v2(pagesInBlock)) ) { throw new MemoryManagerException("Can't terminate block, there doesn't one at " + addressOfFreeBlock + ", pages " + pagesInBlock); } }
		// get free list
		long list[]=(long[])free_list[ByteUtils.log2_v2(pagesInBlock)];
		// erase the block
		for (int i = (int)list[0], k=0; i>=1; i--, k++) {
			if( list[i]==addressOfFreeBlock ) { 
				list[i]=-1;
				// copy remaining elements down by one
				if( k>0 ) {
					System.arraycopy(list, i+1, list, i, k);								
				}				
				list[(int)list[0]]=-1;
				list[0]--; // decrement list counter	
				return;
			}
		}		
	}
	
	/**
	 *  deallocates the block at the given address
	 *  returns block size freed after merge.
	 *
	 * @param address the address
	 * @return the int
	 * @throws MemoryManagerException the memory manager exception
	 */
	public int deallocate(long address) throws MemoryManagerException {
		// coalesce blocks
		mergeBlocksRecursively(address);
		int pages= getPagesInThisBlock(address);
		// place on free list
		if( !isOnFreeList(address, pages) ) {
			setBlockHeaderFree(address, getPagesInThisBlock(address));
			placeOnFreeList(address, pages);
		}		
		return pages;
	}
	
	/**
	 *  deallocates the block at the given address
	 *  returns block size freed after merge.
	 *
	 * @param address the address
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected  void mergeBlocksRecursively(long address) throws MemoryManagerException {
		int pages=getPagesInThisBlock(address);		
		long buddyBlockAddress = address + pages*pageSize;		
		// merge
		if( buddyBlockAddress<N && isFree(buddyBlockAddress) ) {			
			int buddyPages=getPagesInThisBlock(buddyBlockAddress);
			if( pages==buddyPages ) {
				// merge them, they are both free and of equal size in pages.
				mergeTwoBlocks(address,buddyBlockAddress, pages);
			}
		}
	}
	
	/**
	 *  merge two blocks, both must be free.
	 *
	 * @param addressLeft the address left
	 * @param addressRight the address right
	 * @param pages the pages
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected void mergeTwoBlocks(long addressLeft, long addressRight, int pages) throws MemoryManagerException {
		setBlockHeaderFree(addressLeft, pages);
		setBlockHeaderFree(addressRight, pages);
		terminateBlockFromFreeList(addressLeft, pages);
		terminateBlockFromFreeList(addressRight, pages);
		// temporarily mark used and x2 size. at the end it will freed again
		setBlockHeaderUsed(addressLeft, pages*2L);
		mergeBlocksRecursively(addressLeft);
	}
	
	/**
	 *  is on free list.
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @param pagesInBlock the pages in block
	 * @return true, if is on free list
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected boolean isOnFreeList(long addressOfFreeBlock, int pagesInBlock) throws MemoryManagerException {
		// scan
		long list[]=(long[])free_list[ByteUtils.log2_v2(pagesInBlock)];
		for (int i = 1; i <= (int)list[0]; i++) {
			if( list[i]==addressOfFreeBlock ) {
				return true;
			}
		}		
		return false;
	}
	
	/**
	 *  scan all blocks and attempts to combine, puts on free list.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public void defragment() throws MemoryManagerException {
		List<Integer> listFreeBlocks=new ArrayList<Integer>();
		int addressOfBlock=0;
		while( addressOfBlock<N ) {
			int pages=getPagesInThisBlock(addressOfBlock);
			if( isFree(addressOfBlock) ) {
				// add free page
				listFreeBlocks.add(addressOfBlock);
			}else{
				// free pages before this one.
				if( listFreeBlocks.size()>0 ) {
					defragmentListOfFreeBlocks(listFreeBlocks);
					listFreeBlocks.clear();
				}
			}
			addressOfBlock += pages*pageSize; // move on 
		}	
		if( listFreeBlocks.size()>0 ) {
			defragmentListOfFreeBlocks(listFreeBlocks);
			listFreeBlocks.clear();
		}
	}	
	
	/**
	 * Defragment list of free blocks.
	 *
	 * @param listFreeBlocks the list free blocks
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected void defragmentListOfFreeBlocks(List<Integer> listFreeBlocks) throws MemoryManagerException {
		int sumPages=getPagesFromTo(listFreeBlocks,0,listFreeBlocks.size()-1);
		if( ByteUtils.ispowerof2(sumPages) ) {
			// free [i..0] blocks
  			for (int k = 0; k<=listFreeBlocks.size()-1; k++) {
  				int address=listFreeBlocks.get(k);
  				int pages=getPagesInThisBlock(address);
  				terminateBlockFromFreeList(address, pages);
			}
  			int address=listFreeBlocks.get(0);
  			setBlockHeaderUsed(address, sumPages);
  			placeOnFreeList(address, sumPages);
		}			
	}
	
	/**
	 * Gets the pages from to.
	 *
	 * @param listFreeBlocks the list free blocks
	 * @param from the from
	 * @param to the to
	 * @return the pages from to
	 */
	protected int getPagesFromTo(List<Integer> listFreeBlocks, int from, int to) {
		int sumPages=0; 
			for (int i = from; i<=to && i < listFreeBlocks.size(); i++) { 
				int address=listFreeBlocks.get(i); 
				sumPages+=getPagesInThisBlock(address); 
			}
		return sumPages;
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryStats#getPageSize()
	 */
	public int getPageSize() {
		return pageSize;
	}
		
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryStats#getMemoryAllocated()
	 */
	public long getMemoryAllocated() {
		return N;
	}
	
	/**
	 *  if you plan to allocate N huge and pageSize=1, you could get an overflow.
	 *
	 * @return the max pages
	 */
	protected long getMaxPages() {
		return N/pageSize;
	}
	
	/* (non-Javadoc)
	 * @see com.ocean927.memory.client.MemoryStats#print()
	 */
	public void print() throws MemoryManagerException {				
		// REPORT ON MEMORY
		logger.info("MEMORY LAYOUT: " + Formatter.fl(N) + " bytes in " + getMaxPages() + " pages of " + pageSize + " bytes/page.");
		
		// SCAN EACH INDIVIDUAL BLOCK
		long addressOfBlock=0; int cnt=0;
		int sumPages=0; int sumUsedPages=0; int sumFreePages=0;  
		while( addressOfBlock<N ) { cnt++;
			boolean isFree=isFree(addressOfBlock);
			int pages=getPagesInThisBlock(addressOfBlock); sumPages+=pages; 
			if( isFree ) {  sumFreePages+=pages; }
			else {  sumUsedPages+=pages; }							
			addressOfBlock += pages*pageSize; 
		}

		// EVALUATE FREE LISTS
		int sumFreePagesOnList=0;
		for (int blockPowerSize = 0; blockPowerSize < pagesPerBlock.length; blockPowerSize++) {
			long list[]=(long[])free_list[blockPowerSize];
			int pages=pagesPerBlock[blockPowerSize];
			sumFreePagesOnList +=list[0]*pages;
		}
		logger.info("Free Pages: " + sumFreePages + ", Used Pages: " + sumUsedPages);
		logger.info("Free Bytes: " + sumFreePages*pageSize + ", " + Formatter.rDbl((double)100*sumFreePages*pageSize/N, 2) + " %" + ", Used Bytes: " + sumUsedPages*pageSize + ", " + Formatter.rDbl((double)100*sumUsedPages*pageSize/N, 2) + " %" );

		// PRIT EACH BLOCK
		addressOfBlock=0; cnt=0;
		logger.info(Formatter.padInFront("BLOCK", 15, " ") + "," + Formatter.padInFront("ADDRESS", 15, " ") + "," + Formatter.padInFront("PAGES", 15, " ") + "," + Formatter.padInFront("BLOCK_LEN", 15, " ") + "," + Formatter.padInFront("STATUS", 15, " "));
		while( addressOfBlock<N ) { cnt++;
			boolean isFree=isFree(addressOfBlock);
			int pages=getPagesInThisBlock(addressOfBlock);
			String status=null;  if( isFree ) {  status="free"; }else {  status="used"; }				
			logger.info(Formatter.padInFront(""+ cnt, 15, " ") + "," + 
					  Formatter.padInFront(""+addressOfBlock, 15, " ") + "," +
					  Formatter.padInFront(""+pages, 15, " ") + "," +
					  Formatter.padInFront(""+pages*pageSize, 15, " ") + "," +
					  Formatter.padInFront(status, 15, " "));
			// move to next;
			addressOfBlock += pages*pageSize; 
		}

		// SANITY CHECKS
		if( sumFreePagesOnList!=sumFreePages) { throw new MemoryManagerException("Count of freepages doesn't match up, lists sum=" + sumFreePagesOnList + ", scanned=" + sumFreePages); }
		if( sumPages!=getMaxPages() ) { throw new MemoryManagerException("Total number of pages doesn't match up sumPages: " + sumPages + ", expected: " + getMaxPages()); }	 
	}		

	/* -------------------------------------------------------------- */
	/* -------------------------------------------------------------- */
	/* -------------------------------------------------------------- */
	/**
	 *  allocates an array or in the off-heap space.
	 *
	 * @param memorySizeBytes the memory size bytes
	 * @param pageSizeParam the page size param
	 * @throws MemoryManagerException the memory manager exception
	 */
	public abstract void setup(long memorySizeBytes, int pageSizeParam) throws MemoryManagerException;
	
	/**
	 *  reads the header and calls on the power function.
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @return the pages in this block
	 */
	public abstract int getPagesInThisBlock(long addressOfFreeBlock);
	
	/**
	 *  sets header, pages in block determine block size (in pages).
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @param pagesInBlock the pages in block
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected abstract void setBlockHeaderFree(long addressOfFreeBlock, long pagesInBlock) throws MemoryManagerException;
	
	/**
	 * Sets the block header used.
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @param pagesInBlock the pages in block
	 * @throws MemoryManagerException the memory manager exception
	 */
	protected abstract void setBlockHeaderUsed(long addressOfFreeBlock, long pagesInBlock) throws MemoryManagerException;
	
	/**
	 *  read the block header, specific bit.
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @return true, if is used
	 */
	protected abstract boolean isUsed(long addressOfFreeBlock);
	
	/**
	 *  read the block header, specific bit.
	 *
	 * @param addressOfFreeBlock the address of free block
	 * @return true, if is free
	 */
	protected abstract boolean isFree(long addressOfFreeBlock);
	
	/**
	 *  frees memory blocks.
	 *
	 * @throws MemoryManagerException the memory manager exception
	 */
	public abstract void freeMemory() throws MemoryManagerException;

	/* -------------------------------------------------------------- */
	/* -------------------------------------------------------------- */
	/** The n. */
	/* -------------------------------------------------------------- */
	protected long N;
	
	/** The pages. */
	protected long pages; 
	
	/** The page size. */
	protected int  pageSize;
	
	/** The free list. */
	protected Object free_list[]; // contains index of free lists
	
	/** The pages per block. */
	protected int pagesPerBlock[];
	
	/** The Constant powerOf2. */
	protected final static int powerOf2[]=new int[31];
	
	/** The flag correctness. */
	private boolean flagCorrectness=true;
	static { for (int i = 0; i < powerOf2.length; i++) { powerOf2[i]=(int) Math.pow(2, i); } }	
}