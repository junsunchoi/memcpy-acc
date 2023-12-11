#ifndef __ACCEL_H
#define __ACCEL_H

#include <stdint.h>
#include <stddef.h>
#define USING_OPCODE8
#ifdef USING_OPCODE8
#include "rocc_opcode8.h"
#else
#include "rocc.h"
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
                                      int64_t* success_flag);

int64_t MemcpyAccel(const unsigned char* data,
                           size_t data_length,
                           unsigned char* result);

volatile int64_t MemcpyBlockOnCompletion(volatile int64_t * completion_flag);

int64_t MemcpyAccelMulti(const unsigned char** data,
                unsigned int** data_length,
                unsigned char* result,
                const int num_benchmark);


#endif //__ACCEL_H
