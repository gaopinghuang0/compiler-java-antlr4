#!/usr/bin/bash

# bash for loop
for a in `seq 1 22` ; do
	echo "test$a.micro"
	java -cp lib/antlr.jar:gen:src Micro "test$a.micro"
done

find . -name 'test1.out' -exec cat {} \;
