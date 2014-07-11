#! /bin/bash
# Convert db to csv

usage="Usage : $0 <scenario_name> <results dir>"
[[ $# -lt 2 ]] && echo $usage >&2 && exit 1

scenario_name=$1
result_dir=$2


for csv in `ls $result_dir/$scenario_name_*/*.csv` ; do
	./csv2db.sh $csv
done

#cd $OLDPWD
