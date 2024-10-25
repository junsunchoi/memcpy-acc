#include <stdio.h>
#include <stddef.h>
#include <stdint.h>
#include <inttypes.h>
#include <stdlib.h>
#include <assert.h>

#include "accellib.h"
#include "encoding.h"
#include "data1.h"

// no support for overlapping regions
// support for mis-aligned regions (bot src/dest)
// both in bytes (8b)
#define SRC_OFFSET (0)
#define DST_OFFSET (0)

extern void *memcpy_internal(void *, const void *, size_t);
int main() {
  printf("start srcoff:%d dstoff:%d\n", SRC_OFFSET, DST_OFFSET);

  uint8_t* predst = MemcpyAccelSetup(data1_len + DST_OFFSET); //fence, write zero

  size_t total_lens = 0;
  size_t lens[100];
  lens[0] = 1;
  for (size_t i = 1; i < 100; i++) {
    lens[i] = lens[i-1]+2;
  }

  size_t cur_idx = 0;
  while (lens[cur_idx] < data1_len + DST_OFFSET) {
    uint8_t* src = data1 + SRC_OFFSET;
    size_t src_len = lens[cur_idx];
    uint8_t* dst = predst + DST_OFFSET;

    // to bring in uncached
    uint64_t t1 = rdcycle();
    memcpy_internal(dst, src, src_len);
    uint64_t t2 = rdcycle();

    //printf("0x%016" PRIx64 ",0x%016" PRIx64 ",%ld: Start cycle: %" PRIu64 ", End cycle: %" PRIu64 ", Took: %" PRIu64 "\n",
    //        (uint64_t)src,
    //        (uint64_t)dst,
    //        src_len, t1, t2, t2 - t1);

    t1 = rdcycle();
    memcpy_internal(dst, src, src_len);
    t2 = rdcycle();

    printf("0x%016" PRIx64 ",0x%016" PRIx64 ",%08ld: %" PRIu64 "\n",
            (uint64_t)src,
            (uint64_t)dst,
            src_len, t2 - t1);

    cur_idx++;
  }

  return 0;
}
