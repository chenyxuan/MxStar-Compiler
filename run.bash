echo "lover"
java mxstar.main.Main <input.txt
echo "nasm ok"
nasm -f elf64 output.txt
echo "output ok"
gcc output.o -no-pie
echo "gcc ok"
./a.out
echo "run out"
echo @?
