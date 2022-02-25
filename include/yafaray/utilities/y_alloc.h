#pragma once

#ifndef Y_ALLOC_H
#define Y_ALLOC_H

#include <yafray_constants.h>
#include <vector>
#include <algorithm>
#include <cstdint>

#if defined(__FreeBSD__)
	#include <stdlib.h>
#elif !defined(WIN32) && !defined(__APPLE__)
	#include <alloca.h>
#elif defined (__MINGW32__) //Added by DarkTide to enable mingw32 compliation
	#include <malloc.h>
#endif

__BEGIN_YAFRAY

#if defined(_WIN32) && !defined(__MINGW32__) //Added by DarkTide to enable mingw32 compliation
#define alloca _alloca
#endif

inline void * y_memalign(size_t bound, size_t size)
{
	return new unsigned char [size];
}

inline void y_free(void *ptr) {
	delete [] (unsigned char *)ptr;
}


template <class T> class ObjectArena {
public:
	// ObjectArena Public Methods
	ObjectArena() {
		nAvailable = 0;
	}
	T *Alloc() {
		if (nAvailable == 0) {
			int nAlloc = std::max((unsigned long)16,
				(unsigned long)(65536/sizeof(T)));
			mem = (T *)y_memalign(64, nAlloc * sizeof(T));
			nAvailable = nAlloc;
			toDelete.push_back(mem);
		}
		--nAvailable;
		return mem++;
	}
	operator T *() {
		return Alloc();
	}
	~ObjectArena() { FreeAll(); }
	void FreeAll() {
		for (unsigned int i = 0; i < toDelete.size(); ++i)
			y_free(toDelete[i]);
		toDelete.erase(toDelete.begin(), toDelete.end());
		nAvailable = 0;
	}
private:
	// ObjectArena Private Data
	T *mem;
	int nAvailable;
	std::vector<T *> toDelete;
};

class MemoryArena {
public:
	// MemoryArena Public Methods
	MemoryArena(uint32_t bs = 32768) {
		blockSize = bs;
		curBlockPos = 0;
		currentBlock = (char *)y_memalign(64, blockSize);
	}
	~MemoryArena() {
		y_free(currentBlock);
		for (uint32_t i = 0; i < usedBlocks.size(); ++i)
			y_free(usedBlocks[i]);
		for (uint32_t i = 0; i < availableBlocks.size(); ++i)
			y_free(availableBlocks[i]);
	}
	void *Alloc(uint32_t sz) {
		// Round up _sz_ to minimum machine alignment
		sz = ((sz + 7) & (~7));
		if (curBlockPos + sz > blockSize) {
			// Get new block of memory for _MemoryArena_
			usedBlocks.push_back(currentBlock);
			if (availableBlocks.size() && sz <= blockSize) {
				currentBlock = availableBlocks.back();
				availableBlocks.pop_back();
			}
			else
				currentBlock = (char *)y_memalign(64, (std::max(sz, blockSize)) );
			curBlockPos = 0;
		}
		void *ret = currentBlock + curBlockPos;
		curBlockPos += sz;
		return ret;
	}
	void FreeAll() {
		curBlockPos = 0;
		while (usedBlocks.size()) {
			availableBlocks.push_back(usedBlocks.back());
			usedBlocks.pop_back();
		}
	}
private:
	// MemoryArena Private Data
	uint32_t curBlockPos, blockSize;
	char *currentBlock;
	std::vector<char *> usedBlocks, availableBlocks;
};

__END_YAFRAY
#endif // Y_ALLOC_H
