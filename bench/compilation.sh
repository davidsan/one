#! /bin/bash
# OAR launcher for compiling the project

usage="Usage : $0 <chemin the one>"
[[ $# -lt 1 ]] && echo -e $usage >&2 && exit 1

echo "Launch compilation jobs..."
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ core/*.java"
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ movement/*.java"
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ report/*.java"
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ routing/*.java"
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ gui/*.java"
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ input/*.java"
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ applications/*.java"
oarsub -l "core=1,walltime=0:10:00" -E /dev/null -O /dev/null "cd $1 && javac -extdirs lib/ interfaces/*.java"
