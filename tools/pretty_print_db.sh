#!/bin/bash
if [ $# -lt 1 ]; then
	echo "usage: $0 filename"
	exit 1
fi

sqlite3 $1 <<!
.mode column
.headers on
select * from report;
!