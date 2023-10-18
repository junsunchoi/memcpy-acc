#ifndef __ACCEL_H
#define __ACCEL_H

#include <stdint.h>
#include <stddef.h>
#include "rocc.h"
#include <stdbool.h>

#define MEMCPY_OPCODE 0

#define FUNCT_SFENCE 0
#define FUNCT_SRC_INFO 1
#define FUNCT_DEST_INFO 2
#define FUNCT_CHECK_COMPLETION 3

unsigned char * MemcpyAccelSetup(size_t write_region_size);

void MemcpyAccelNonblocking(const unsigned char* data,
                                      size_t data_length,
                                      unsigned char* result,
                                      int* success_flag);

int MemcpyAccel(const unsigned char* data,
                           size_t data_length,
                           unsigned char* result);

volatile int MemcpyBlockOnCompletion(volatile int * completion_flag);

#endif //__ACCEL_H
