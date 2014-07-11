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
sqlite3 $db_file <<EOF
.separator ','
.import $csv_file $table_name
EOF

# Remove first row (headers from CSV file)
echo "delete from $table_name where rowid < (select rowid from $table_name limit 1,1);" | sqlite3 $db_file
