#!/bin/bash

# bash for loop
for a in `seq 1 22` ; do
	infile="test$a.micro"
	echo $infile
	find -name $infile -exec java -cp lib/antlr.jar:classes/ Micro {} > test_result \;
	find -name "test$a.out" -exec diff -b -B test_result {} \;
	echo
done
rm test_result
