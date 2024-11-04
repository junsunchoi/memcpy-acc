#!/bin/bash

set -ex

BENCHDIR=memcpy-simple-asm-sweep

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
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c test-simple-asm-sweep.c
riscv64-unknown-elf-gcc -static -specs=htif_nano.specs accellib.o memcpy.o test-simple-asm-sweep.o -o test-simple-asm-sweep.riscv
riscv64-unknown-elf-objdump -S test-simple-asm-sweep.riscv > dump
