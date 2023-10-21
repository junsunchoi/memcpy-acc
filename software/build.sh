BENCHDIR=memcpy-simple

mkdir -p $BENCHDIR
cp example.txt $BENCHDIR 
cd $BENCHDIR
cp example.txt data
xxd -i data >> data.h
rm example.txt data
cd ..
cp *.c $BENCHDIR
cp *.h $BENCHDIR
cd $BENCHDIR
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c accellib.c
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c test.c
riscv64-unknown-elf-gcc -static -specs=htif_nano.specs accellib.o test.o -o test.riscv


