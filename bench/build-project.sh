#! /bin/bash
# OAR launcher for compiling the project

usage="Usage : $0 <path the one>"
[[ $# -lt 1 ]] && echo -e $usage >&2 && exit 1

echo "Launch compilation job..."
oarsub -l "core=1,walltime=0:30:00" -E /dev/null -O /dev/null "cd $1 && ant build-project"