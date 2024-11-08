#include <stdio.h>
#include <stddef.h>
#include <stdint.h>
#include <inttypes.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include "rerocc.h"
#include "accellib.h"
#include "encoding.h"
#include "data1.h"

extern unsigned long memcpy_internal(void *, const void *, size_t);

// overall lengths
#define TEST_LEN_SZ 6
size_t test_lens[] = {16, 32, 64, 128, 256, 512};
// #define TEST_LEN_SZ 1
// size_t test_lens[] = {512};

// offsets in bytes (64b/8B words, 256b/32B bus w)
#define TEST_OFF_SZ 6
size_t test_offs[] = {0, 1, 4, 8, 9, 32};

// doesn't test if regions are overlapped
int main() {
  printf("Start\n");

  // pad by 256 since we are going to check offset of that much
  uint8_t* dst_workspace = MemcpyAccelSetup(data1_len + 256); //fence, write zero

  for (size_t i = 0; i < TEST_LEN_SZ; ++i) {
    size_t len = test_lens[i];

    for (size_t j = 0; j < TEST_OFF_SZ; ++j) {
      for (size_t k = 0; k < TEST_OFF_SZ; ++k) {
        size_t src_off = test_offs[j];
        size_t dst_off = test_offs[k];
        printf("L:%ld SO:%ld DO:%ld\n", len, src_off, dst_off);

        // memset the dst to all 1's to check if there is leakage
        memset(dst_workspace, 0xFF, len + dst_off + 256);

#define OPC 0
#define CONFIG_ACC_ID 2
#define CONFIG_CFG_ID 15
        if (!rr_acquire_single(CONFIG_CFG_ID, CONFIG_ACC_ID)) {
          printf("Failed acquire\n");
          return 1;
        }

        rr_set_opc(OPC, CONFIG_CFG_ID);
        rr_fence(CONFIG_CFG_ID);
        printf("Done with ReRoCC setup\n");

        MemcpyClearTLB();

        size_t bytes_uncopied = 0;
        //bytes_uncopied = memcpy_internal(dst_workspace + dst_off, data1 + src_off, len);
        MemcpyAccel(data1 + src_off, len, dst_workspace + dst_off);

        rr_release(CONFIG_CFG_ID);

        // // check return code
        // if (bytes_uncopied != 0) {
        //   printf("L:%ld SO:%ld DO:%ld - bytes_uncopied!=0, %ld\n", len, src_off, dst_off, bytes_uncopied);
        // }

        // check copied area
        for (size_t l = 0; l < len; l++) {
          char s = (data1 + src_off)[l];
          char d = (dst_workspace + dst_off)[l];
          if (s != d) {
            printf("L:%ld SO:%ld DO:%ld - I:%" PRIu64 " %c!=%c, Skipping Checks\n", len, src_off, dst_off, l, s, d);
            break;
          }
        }

        // check surrounding area is still 1's
        for (size_t l = 0; l < dst_off; ++l) {
          if (dst_workspace[l] != 0xFF) {
            printf("L:%ld SO:%ld DO:%ld - I:%" PRIu64 " leakage, Skipping Checks\n", len, src_off, dst_off, l);
            break;
          }
        }
        for (size_t l = 0; l < 256; ++l) {
          if ((dst_workspace + len + dst_off)[l] != 0xFF) {
            printf("L:%ld SO:%ld DO:%ld - I:%" PRIu64 " leakage afterwards, Skipping Checks\n", len, src_off, dst_off, l);
            break;
          }
        }
      }
    }
  }

  return 0;
}
