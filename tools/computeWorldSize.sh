#!/bin/bash

# This script prints the largest X coordinate and 
# the largest Y coordinate of the map in argument.
# (MovementModel.worldSize)

usage="Usage : $0 <wkt file map>"
[[ $# -lt 1 ]] && echo $usage >&2 && exit 1


MINX=`mktemp 2>/dev/null || mktemp -t 'minx'` || exit 1
MINY=`mktemp 2>/dev/null || mktemp -t 'miny'` || exit 1
MAXX=`mktemp 2>/dev/null || mktemp -t 'maxx'` || exit 1
MAXY=`mktemp 2>/dev/null || mktemp -t 'maxy'` || exit 1

while read line 
do
  SED=`echo $line | sed -e "s/[^(]*(//" | sed -e "s/)$//" | tr , '\n'`
  SED2=`echo "$SED" | sed -e 's/^ //'`
  X=`echo "$SED2" | cut -d' ' -f1 | sort -n `
  echo "$X" | head -n 1 >> $MINX
  echo "$X" | tail -n 1 >> $MAXX
  Y=`echo "$SED2" | cut -d' ' -f2 | sort -n `
  echo "$Y" | head -n 1 >> $MINY
  echo "$Y" | tail -n 1 >> $MAXY

done < $1


LOWERX=`cat $MINX | sort -n | head -n 1`
LOWERY=`cat $MINY | sort -n | head -n 1`

UPPERX=`cat $MAXX | sort -rn | head -n 1`
UPPERY=`cat $MAXY | sort -rn | head -n 1`

PADDING=50

echo `echo "($UPPERX-$LOWERX+$PADDING)/1" | bc`, `echo "($UPPERY-$LOWERY+$PADDING)/1" | bc`

rm -f $MINX
rm -f $MINY
rm -f $MAXX
rm -f $MAXY