#! /bin/bash
# Convert CSV to SQLite for all reports

usage="Usage : $0 <scenario_name> <results dir>"
[[ $# -lt 2 ]] && echo $usage >&2 && exit 1

scenario_name=$1
result_dir=$2

for csv in `ls $result_dir/$scenario_name_*/*.csv` ; do
	db_file=`echo $csv | sed -e 's/csv$/db/'`
	if [ ! -f $db_file ]; then
		echo "Convert to SQLite $csv"
		oarsub -l "core=1,walltime=72:0:0" --name "csv2db ${csv}" --stdout /dev/null --stderr /dev/null "./csv2db.sh $csv" &
    	# For sequential run, comment previous line and
    	# uncomment next
		# ./csv2db.sh $csv
	fi
done
echo "Jobs submitted."