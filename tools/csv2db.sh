#!/bin/bash
# Convert CSV file to SQLite .db file

usage="Usage : $0 <csv file>"
[[ $# -lt 1 ]] && echo $usage >&2 && exit 1

CSV_FILE=$1
# Création bd
DB_FILE=`echo $1 | sed -e 's/csv$/db/'`

# Création table
TABLE_NAME=`echo $1 | sed -E "s/.*_(.*)ReportCSV.csv/\1/" | tr '[:upper:]' '[:lower:]'`
HEADERS=`head -n 1 $CSV_FILE`
echo "drop table if exists $TABLE_NAME;" | sqlite3 $DB_FILE
echo "create table $TABLE_NAME($HEADERS);" | sqlite3 $DB_FILE

# Import données
sqlite3 $DB_FILE <<EOF
.separator ','
.import $CSV_FILE $TABLE_NAME
EOF

# Suppression première ligne (entête)
echo "delete from $TABLE_NAME where rowid < (select rowid from $TABLE_NAME limit 1,1);" | sqlite3 $DB_FILE