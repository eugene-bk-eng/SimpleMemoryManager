## Synopsis

This is an implementation of a memory manager based on buddy memory algorithm. 
https://en.wikipedia.org/wiki/Buddy_memory_allocation 

Package provides two concrete implementations; 
an on-heap algorithm backed by byte array and off JVM heap. 

Deallocation is explicit. Memory defragmentation is invoked manually. There is 
no daemon thread to combine freed memory blocks. 

User who forgets to deallocate will cause a memory leak.   

## Code Example

long address=impl.allocate(10000); // allocate 10K bytes
// do something
impl.deallocate(address);

## Motivation

Project is born to support 
- efficient resizing of arrays of primitives
- exceed heap limitation of 2^31-1 array elements.
- improve performance by moving allocation 
  off heap and avoiding jvm array checks.

## Package Limitations and Future Enhancements

This project is a prototype. 

- Concurrent access to memory manager is not supported. Results 
will be unpredictable. 
Solution: synchronize access to memory mutator methods.

- Block barriers. If user allocated 1000 bytes and decided 
to write 1010 bytes, next memory block(s) will be corrupted. 
Solution: Proxy memory I/O access and track block boundaries.

- De-fragmentation is not automatic like GC in Java. 
Solution: Create a daemon thread, run concurrently with 
the allocator creating contiguous free space.

- Each allocation request stores a header. Due to this overhead, 
available space is always less 100% of setup memory.
Solution: none. 

## Installation

See Test class

## API Reference

See /docs

## Tests

See Test class

## Contributors

Single contributor. 

## License

The MIT License (MIT)

Copyright (c) 2015-2016 ocean927

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.