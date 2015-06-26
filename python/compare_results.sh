#!/bin/bash

#
# Takes one or more _data files from test-logs and plots the
# numbers in a single chart.
#

# Requirements: bash, gnuplot

LINETYPES="set linetype 1 lc rgb \"#0072B2\""$'\n'"set linetype 2 lc rgb \"#CC79A7\""$'\n'"set linetype 3 lc rgb \"#009E73\""$'\n'"set linetype 4 lc rgb \"#D55E00\""$'\n'"set linetype 5 lc rgb \"#E69F00\""$'\n'"set linetype 6 lc rgb \"#56B4E9\""$'\n'"set linetype 7 lc rgb \"#0000ff\""$'\n'"set linetype 8 lc rgb \"#0000ff\""$'\n'"set linetype 9 lc rgb \"#ccccff\""$'\n'"set linetype 10 lc rgb \"#3333ff\""

if [ "." == ".$1" ]; then
    echo "Usage:  ./$0 datafiles"

    echo "Sample: ./$0 test-logs/ldukwadev-20150601a.graph_data test_logs/ldukwadev-20150601.graph_data"
    echo "Produces compare_results.png"
    echo ""
    echo "Sample: IMAGES=mycomparison.svg TITLE=\"Our measurements\" YMAX=5000 ./$0 test-logs/ldukwadev-20150601a.graph_data test_logs/ldukwadev-20150601.graph_data"
    echo "Produces mycomparison.svg, suitable for print"
    exit 2
fi

# IMAGES="foo.png bar.svg"
if [ "." == ".$IMAGES" ]; then
    IMAGES=compare_results.png
fi
# TITLE="UKWA measurements with 60.000 ARCs"
if [ "." == ".$TITLE" ]; then
    TITLE="Hits, response time, `date`"
fi
if [ "." == ".$WIDTH" ]; then
    WIDTH=1200
fi
if [ "." == ".$HEIGHT" ]; then
    HEIGHT=700
fi
if [ "." == ".$YMIN" ]; then
    YMIN=0
fi

# The graph area can be controlled by setting XMIN, XMAX, YMIN or YMAX


# Generate plot commands
TT=`mktemp`
TEMPS=""
PLOT="plot"
LT=1
# TODO: Base the delta on the number of data files to support comparison of 5+ measurements
ARGCOUNT=`echo "$@" | wc -w`
FRACTION_DELTA=`echo "scale=4;2.0/$ARGCOUNT" | bc` 
FRACTION="1"
for F in $@; do
    BASE=`basename $F`
    LEGEND=${BASE%.*}
    if [ ! "plot" == "$PLOT" ]; then
        PLOT="${PLOT},"
    fi

    # http://gnuplot.sourceforge.net/demo_4.3/candlesticks.html
    #http://stackoverflow.com/questions/15404628/how-can-i-generate-box-and-whisker-plots-with-variable-box-width-in-gnuplot
    # http://stackoverflow.com/questions/4805930/making-x-axis-tics-from-column-in-data-file-in-gnuplot


    # Data format
    # # Number of records in 10^1 = 3
    # ["10^1", "104", "182", "105", "181"], 

    # Is converted to
    # 10 10^1 104 182 105 181
    
    T="$BASE.tmp"
    TEMPS="$TEMPS $T"
    cat "$F" | grep -v "^#" | sed -e 's/[\[\"\,]//g' | tr ']' ' ' > $TT
    while read line; do
        echo -n "$line" | cut -d\  -f1 | bc | tr '\n' ' ' >> $T
        echo "$line" | cut -d\  -f 1,2,3,4,5 >> $T
    done < $TT

    # Data format:
    # hits 25% 95% low 75%
    # 10000000 581 2757 361 874


    # TODO: If we had the median, we could plot that as a line inside the candlesticks
    # Candlesticks: count 25% min 95% 75% label
    if [ "$LT" == "1" ]; then
        TICS=":xticlabels(2)"
    else
        TICS=""
    fi
    PLOT="$PLOT '$T' using (\$1*$FRACTION):3:5:4:6$TICS with candlesticks lc $LT lt 3 lw 2 title 'Quartiles $LEGEND' whiskerbars"
#, \\
#       '$T'      using (\$1*$FRACTION):5:5:5:5:xtic(2) with candlesticks lt -1 lw 2 title 'Median'"

    LT=$((LT+1))
    FRACTION=`echo "scale=4; $FRACTION + $FRACTION_DELTA" | bc`
done


# Plot it all
for IMAGE in $IMAGES; do
    EXT=${IMAGE##*.}

    TMPPLOT=`mktemp`
    cat > $TMPPLOT << EOF
set terminal $EXT size $WIDTH, $HEIGHT
$LINETYPES

set title "$TITLE"
set output "$IMAGE"

set boxwidth 0.1 absolute
set xlabel 'Number of hits in search result'
set ylabel 'Response time in milliseconds'

set xrange [ $XMIN : $XMAX ] noreverse nowriteback
set logscale x
set xtics ($XTICS)

set yrange [ $YMIN : $YMAX ] noreverse nowriteback
set grid ytics lt 0 lw 1 lc rgb "#bbbbbb"
$CUSTOM_GP

$PLOT
EOF

    gnuplot "$TMPPLOT"
done

rm $TEMPS
