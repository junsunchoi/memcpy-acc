#include <stdio.h>
#include <stddef.h>
#include <stdint.h>
#include <inttypes.h>
#include <stdlib.h>

#include "accellib.h"
#include "encoding.h"
#include "data_helper.h"

int main() {
  unsigned int total_benchmark_len = 0; 
  for(size_t i=0; i<num_benchmarks; ++i){
    total_benchmark_len += *(benchmark_len_array[i]);
  }

  uint8_t* result_area = MemcpyAccelSetup(total_benchmark_len); //fence, write zero
 
  printf("Starting benchmark.\n");
  uint64_t t1 = rdcycle();
  MemcpyAccelMulti(benchmark_data_array,
              benchmark_len_array,
              result_area,
              num_benchmarks); //have to send ip, isize, op, cmpflag
  uint64_t t2 = rdcycle();

  printf("Start cycle: %" PRIu64 ", End cycle: %" PRIu64 ", Took: %" PRIu64 "\n", 
          t1, t2, t2 - t1);

  printf("Checking copied data correctness:\n");
  bool fail = false;
  size_t cumul_len = 0;
  for(size_t n=0; n < num_benchmarks; ++n){
    printf("Checking file #%d",n);
    for (size_t i = cumul_len; i < cumul_len+benchmark_len_array[n]; i++) {
      if (benchmark_data_array[n][i] != result_area[i]) {
        printf("idx %" PRIu64 ": expected: %c got: %c\n",
            i, benchmark_data_array[n][i], result_area[i]);
        fail = true;
      }
      cumul_len += benchmark_len_array[n];
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
