#!/bin/bash

# This script prints the largest X coordinate and 
# the largest Y coordinate of the map in argument.
# (MovementModel.worldSize)

usage="Usage : $0 <file map>"
[[ $# -lt 1 ]] && echo $usage >&2 && exit 1


TMPFILEX=`mktemp 2>/dev/null || mktemp -t 'tmpfilex'` || exit 1
TMPFILEY=`mktemp 2>/dev/null || mktemp -t 'tmpfiley'` || exit 1

while read line 
do
  SED=`echo $line | sed -e "s/[^(]*(//" | sed -e "s/)$//" | tr , '\n'`
  echo "$SED" | cut -d' ' -f1 | sort -rn | head -n 1 >> $TMPFILEX
  echo "$SED" | cut -d' ' -f2 | sort -rn | head -n 1 >> $TMPFILEY
done < $1

X=`cat $TMPFILEX | sort -rn | head -n 1`
Y=`cat $TMPFILEY | sort -rn | head -n 1`

echo $X, $Y

rm -f $TMPFILEX
rm -f $TMPFILEY
