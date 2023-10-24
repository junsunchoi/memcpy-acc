BENCHDIR=memcpy-simple

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
cp *.c $BENCHDIR
cp *.h $BENCHDIR
cd $BENCHDIR
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c accellib.c
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c test-simple.c
riscv64-unknown-elf-gcc -static -specs=htif_nano.specs accellib.o test-simple.o -o test-simple.riscv


