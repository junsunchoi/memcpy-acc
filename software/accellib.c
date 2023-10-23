#include <assert.h>
#include <malloc.h>
#include <stdio.h>
#include <stdint.h>
#include <inttypes.h>
#include "accellib.h"

#define PAGESIZE_BYTES 4096

unsigned char * MemcpyAccelSetup(size_t write_region_size) {
#ifndef NOACCEL_DEBUG
    ROCC_INSTRUCTION(MEMCPY_OPCODE, FUNCT_SFENCE);
#endif

    size_t regionsize = sizeof(char) * (write_region_size);

    unsigned char* fixed_alloc_region = (unsigned char*)memalign(PAGESIZE_BYTES, regionsize);
    for (uint64_t i = 0; i < regionsize; i += PAGESIZE_BYTES) {
        fixed_alloc_region[i] = 0;
    }

    uint64_t fixed_ptr_as_int = (uint64_t)fixed_alloc_region;

    assert((fixed_ptr_as_int & 0x7) == 0x0);

    printf("constructed %" PRIu64 " byte region, starting at 0x%016" PRIx64 ", paged-in, for accel\n",
            (uint64_t)regionsize, fixed_ptr_as_int);

    return fixed_alloc_region;
}

volatile int MemcpyBlockOnCompletion(volatile int * completion_flag) {
    uint64_t retval;
#ifndef NOACCEL_DEBUG
    ROCC_INSTRUCTION_D(MEMCPY_OPCODE, retval, FUNCT_CHECK_COMPLETION);
#endif
    asm volatile ("fence");

#ifndef NOACCEL_DEBUG
    while (! *(completion_flag)) {
        asm volatile ("fence");
    }
#endif
    return *completion_flag;
}

void MemcpyAccelNonblocking(const unsigned char* data, 
                            size_t data_length,
                            unsigned char* result, 
                            int* success_flag) {
#ifndef NOACCEL_DEBUG
    ROCC_INSTRUCTION_SS(MEMCPY_OPCODE,
                        (uint64_t)data,
                        (uint64_t)data_length,
                        FUNCT_SRC_INFO);

    ROCC_INSTRUCTION_SS(MEMCPY_OPCODE,
                        (uint64_t)result,
                        (uint64_t)success_flag,
                        FUNCT_DEST_INFO);
#endif
}

int MemcpyAccel(const unsigned char* data, 
                size_t data_length, 
                unsigned char* result) {
    int completion_flag = 0;

#ifdef NOACCEL_DEBUG
    printf("completion_flag addr : 0x%x\n", &completion_flag);
#endif

    MemcpyAccelNonblocking(data, 
                            data_length, 
                            result, 
                            &completion_flag);
    return MemcpyBlockOnCompletion(&completion_flag);
}

int MemcpyAccelMulti(const unsigned char** data,
                size_t* data_length,
                unsigned char** result,
                const int num_benchmark) {
    int completion_flag = 0;
#ifdef NOACCEL_DEBUG
    printf("completion_flag addr : 0x%x\n", &completion_flag);
#endif
    size_t result_area_consumed = 0;
    for(int i=0; i<num_benchmark; ++i){
        MemcpyAccelNonblocking(data[i], 
                                data_length[i], 
                                result+result_area_consumed,
                                &completion_flag);
        result_area_consumed += data_length[i];
    }
    return MemcpyBlockOnCompletion(&completion_flag);
}
