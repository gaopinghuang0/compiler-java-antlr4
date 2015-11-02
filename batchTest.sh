#!/bin/bash

# bash for loop
#for a in `seq 1 22` ; do
#	infile="test$a.micro"
#	echo $infile
#	find -name $infile -exec java -cp lib/antlr.jar:classes/ Micro {} > test_result \;
#	find -name "test$a.out" -exec diff -b -B test_result {} \;
#	echo
#done
#rm test_result

# bash for loop
for entry in testcases/input/*.micro
do
	set -- "$entry"
	IFS="."; read -ra ADDR <<< "$entry"
	echo "${ADDR[0]}.micro"
	java -cp lib/antlr.jar';'classes/ Micro "${ADDR[0]}.micro" > tempout
	IFS="/"; read -ra ARRAY <<< "${ADDR[0]}"
	find -name "${ARRAY[2]}.out" -exec diff -b -B tempout {} \;
#	echo "${ADDR[0]}.input"
done
rm tempout