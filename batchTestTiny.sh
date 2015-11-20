#!/bin/bash

for entry in testcases/input/*.micro
do
	# split filename by ',' into ${ADDR[@]}, including 2 parts ${ADDR[0]} and ${ADDR[1]}
	IFS="."; read -ra ADDR <<< "$entry"
	echo "${ADDR[0]}.micro"
	java -cp lib/antlr.jar';'classes/ Micro "${ADDR[0]}.micro" > tempout
	# split path by '/' into ${ARRAY[@]}, including 3 parts
	./tiny tempout
	IFS="/"; read -ra ARRAY <<< "${ADDR[0]}"
	find -name "${ARRAY[2]}.out" -exec ./tiny {} \;
done
rm tempout