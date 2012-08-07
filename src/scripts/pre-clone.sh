#!/bin/bash
OUTDIR="/tmp/repo_loader"
if [ ! -d $OUTDIR ]; then
    mkdir $OUTDIR
fi
for x in $(cat $1); do
    USER="$(echo $x | cut -d / -f 1)"
    REPO="$(echo $x | cut -d / -f 2)"
    if [ ! -d $OUTDIR/$USER ]; then
        mkdir $OUTDIR/$USER
    fi
    echo "***************************************************************************"
    if [ -d $OUTDIR/$USER/$REPO ]; then
        cd $OUTDIR/$USER/$REPO
	echo $OUTDIR/$USER/$REPO
        echo "Updating from origin git://github.com/$USER/$REPO.git"
        git pull origin master
    else
        cd $OUTDIR/$USER
        echo "Cloning git://github.com/$USER/$REPO.git"
        git clone git://github.com/$USER/$REPO.git
    fi
done
