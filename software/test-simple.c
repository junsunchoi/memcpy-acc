#include <stdio.h>
#include <stddef.h>
#include <stdint.h>
#include <inttypes.h>
#include <stdlib.h>

#include "accellib.h"
#include "encoding.h"
#include "data_helper.h"

int main() {
  uint8_t* result_area = MemcpyAccelSetup(data_len); //fence, write zero

  printf("src start addr: 0x%016" PRIx64 "\n", (uint64_t)data);
  printf("Starting benchmark.\n");
  uint64_t t1 = rdcycle();
  MemcpyAccel(data1,
              data1_len,
              result_area,
              num_benchmarks); //have to send ip, isize, op, cmpflag
  uint64_t t2 = rdcycle();

  printf("Start cycle: %" PRIu64 ", End cycle: %" PRIu64 ", Took: %" PRIu64 "\n", 
          t1, t2, t2 - t1);

  printf("Checking copied data correctness:\n");
  bool fail = false;
  for (size_t i = 0; i < data_len; i++) {
    if (data1[i] != result_area[i]) {
      printf("idx %" PRIu64 ": expected: %c got: %c\n",
          i, data1[i], result_area[i]);
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
