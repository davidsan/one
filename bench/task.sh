#! /bin/bash
# ONE simulator task

# Usage
usage="
Usage: $0 one-directory settings result-directory\n
one-directory: path to the ONE simulator's directory\n
settings: path to the scenario settings file\n
result-directory: path to the results directory
ONE simulator task\n"
[[ $# -lt 2 ]] && echo -e $usage >&2 && exit 1

one_directory=$1
settings=$2
result_dir=$3

cd $one_directory

for jar in $one_directory/lib/*.jar ; do
    echo $CLASSPATH | grep -q $jar
    [[ $? -ne 0 ]] && export CLASSPATH=$CLASSPATH:$jar
done

java core.DTNSim -b 1 $settings

# create result directory if it doesn't exist
mkdir -p $result_dir
settings_basename=`echo ${settings##**/}`
settings_basename_without_ext=`echo ${settings_basename%.*}`

# for convenience 
# move csv reports into subdirectory associated with scenario id
mv $one_directory/reports/csv/${settings_basename_without_ext}_* $result_dir/$settings_basename_without_ext

# move database reports into subdirectory associated with scenario id
mv $one_directory/reports/database/${settings_basename_without_ext}_* $result_dir/$settings_basename_without_ext


for csv in `ls $result_dir/$settings_basename_without_ext/*.csv` ; do
	db_file=`echo $csv | sed -e 's/csv$/db/'`
	if [ ! -f $db_file ]; then
		echo "Convert to SQLite $csv"
		./bench/csv2db.sh $csv
	fi

	# Delete CSV file
	rm -f $csv
done

# Compress all .db files
for db in `ls $result_dir/$settings_basename_without_ext/*.db` ; do
	gzip $db
done

echo "Done."


cd $OLDPWD
