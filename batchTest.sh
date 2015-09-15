#!/bin/bash

# bash for loop
for a in `seq 1 22` ; do
	infile="test$a.micro"
	echo $infile
#	find -name $infile -exec cat {} \;
	input=$(find -name $infile -exec java -cp lib/antlr.jar:classes/ Micro {} \;)
	echo $input
	output=$(find -name "test$a.out" -exec cat {} \;)
	echo $output
#	if [ $input = $output ]; then
#		echo "Pass"
#	else
#		echo "False"
#	fi
	echo
done

#find . -name 'test1.out' -exec cat {} \;
