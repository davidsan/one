#!/bin/bash
if [ $# -lt 2 ]; then
	echo "usage: $0 filename table"
	exit 1
fi

sqlite3 $1 <<!
.mode column
.headers on
select * from $2;
!