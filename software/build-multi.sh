BENCHDIR=memcpy-multi
HELPER_H=memcpy-multi/data_helper.h

if [ -d "$BENCHDIR" ]; then
  rm -rf $BENCHDIR
fi
mkdir -p $BENCHDIR

counter=1

for file in example*.txt; do
  if [ -f "$file" ]; then
    NUM=${file:7:(${#file}-7-4)}
    dest="data$NUM"
    cp $file $BENCHDIR/$dest
    cd $BENCHDIR
    xxd -i "$dest" > "$dest.h"
    rm "$dest"
    cd ..
    ((counter++))
  fi
done

for((i=1; i<$counter; i++)) do
  echo "#include \"data$i.h\"" >> $HELPER_H
done
num_bench="$((counter-1))"
echo "unsigned int num_benchmarks = $num_bench;" >> $HELPER_H
echo "char** benchmark_names[] = {" >> $HELPER_H
for((i=1; i<$counter; i++)) do
  echo "data$i," >> $HELPER_H
done
echo "};" >> $HELPER_H
echo "unsigned char* benchmark_data_array[]={" >> $HELPER_H
for((i=1; i<$counter; i++)) do
  echo "(char*)(&data$i)," >> $HELPER_H
done
echo "};" >> $HELPER_H
echo "unsigned int* benchmark_len_array[]={" >> $HELPER_H
for((i=1; i<$counter; i++)) do
  echo "&data${i}_len," >> $HELPER_H
done
echo "};" >> $HELPER_H



cp *.c $BENCHDIR
cp *.h $BENCHDIR
cd $BENCHDIR
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c accellib.c
riscv64-unknown-elf-gcc -fno-common -fno-builtin-printf -specs=htif_nano.specs -c test-multi.c
riscv64-unknown-elf-gcc -static -specs=htif_nano.specs accellib.o test-multi.o -o test-multi.riscv


