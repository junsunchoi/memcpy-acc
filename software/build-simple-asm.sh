#!/bin/bash

set -ex

BENCHDIR=memcpy-simple-asm

if [ -d "$BENCHDIR" ]; then
  rm -rf $BENCHDIR
fi
mkdir -p $BENCHDIR

cp example1.txt $BENCHDIR
cd $BENCHDIR
cp example1.txt data1
xxd -i data1 >> data1.h
rm example1.txt data1
cd ..
cp *.S $BENCHDIR
cp *.c $BENCHDIR
cp *.h $BENCHDIR
cd $BENCHDIR
riscv64-unknown-elf-gcc -c memcpy.S
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c accellib.c
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c test-simple-asm.c
riscv64-unknown-elf-gcc -static -specs=htif_nano.specs accellib.o memcpy.o test-simple-asm.o -o test-simple-asm.riscv
