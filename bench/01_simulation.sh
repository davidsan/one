#! /bin/bash
# Lance une simulation avec le fichier de setting donné

usage="Usage : $0 <chemin the one> <fichier settings> <results dir>"
[[ $# -lt 2 ]] && echo $usage >&2 && exit 1

path_the_one=$1
settings_name=$2
result_dir=$3
cd $path_the_one

for jar in $path_the_one/lib/*.jar ; do
    echo $CLASSPATH | grep -q $jar
    [[ $? -ne 0 ]] && export CLASSPATH=$CLASSPATH:$jar
done

java core.DTNSim -b 1 $settings_name

# create result directory
mkdir -p $path_the_one/$result_dir

settings_basename=`echo ${settings_name##**/}`

settings_basename_without_ext=`echo ${settings_basename%.*}`

# move csv reports into subdirectory associated with scenario id
mv $path_the_one/reports/csv/${settings_basename_without_ext}_* $path_the_one/$result_dir/$settings_basename_without_ext

# move database reports into subdirectory associated with scenario id
mv $path_the_one/reports/database/${settings_basename_without_ext}_* $path_the_one/$result_dir/$settings_basename_without_ext


cd $OLDPWD
