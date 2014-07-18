#!/bin/bash
# Convert CSV file to SQLite .db file

usage="Usage : $0 <csv file>"
[[ $# -lt 1 ]] && echo $usage >&2 && exit 1

csv_file=$1
# Create database
db_file=`echo $csv_file | sed -e 's/csv$/db/'`

# Create table
table_name=`echo $csv_file | sed -E "s/.*_(.*)ReportCSV.csv/\1/" | tr '[:upper:]' '[:lower:]'`
headers=`head -n 1 $csv_file`
echo "drop table if exists $table_name;" | sqlite3 $db_file
echo "create table $table_name($headers);" | sqlite3 $db_file

# Import data
tail -n+2 < $csv_file | sqlite3 -separator ',' $db_file ".import /dev/stdin $table_name"
