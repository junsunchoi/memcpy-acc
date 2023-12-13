#ifndef __ACCEL_H
#define __ACCEL_H

#include <stdint.h>
#include <stddef.h>
#define OPCODE8
#ifndef OPCODE8
#include "rocc.h"
#else
#include "rocc_opcode8.h"
#endif

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

int MemcpyAccelMulti(const unsigned char** data,
                unsigned int** data_length,
                unsigned char* result,
                const int num_benchmark);


#endif //__ACCEL_H
