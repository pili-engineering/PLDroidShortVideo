#!/bin/bash
FILE=`ls -d */ | cut -f1 -d'/'`
for i in $FILE
do
rm $i.zip
zip -r $i.zip $i
rm -rf $i
done

