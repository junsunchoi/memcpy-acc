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
//#define OVERLAPPED

int main() {
  assert(SRC_OFFSET < data1_len);

#ifndef OVERLAPPED
  uint8_t* result_area = MemcpyAccelSetup(data1_len + DST_OFFSET); //fence, write zero
#else
  uint8_t* result_area = data1;
#endif

  uint8_t* data = data1 + SRC_OFFSET;
  size_t data_len = data1_len - SRC_OFFSET;
  result_area += DST_OFFSET;

  printf("src: 0x%016" PRIx64 ", %ld\n", (uint64_t)data, data_len);
  printf("dest: 0x%016" PRIx64 "\n", (uint64_t)result_area);

  uint64_t t1 = rdcycle();
  MemcpyAccel(data,
              data_len,
              result_area); //have to send ip, isize, op, cmpflag
  uint64_t t2 = rdcycle();

  printf("Start cycle: %" PRIu64 ", End cycle: %" PRIu64 ", Took: %" PRIu64 "\n",
          t1, t2, t2 - t1);

  printf("Checking copied data correctness:\n");
  bool fail = false;
  for (size_t i = 0; i < data_len; i++) {
    if (data[i] != result_area[i]) {
      printf("idx %" PRIu64 ": expected: %c got: %c\n",
          i, data[i], result_area[i]);
      fail = true;
    }
  }

  if (fail) {
      printf("TEST FAILED!\n");
      exit(1);
  } else {
      printf("TEST PASSED!\n");
  }
  return 0;
}
