###############################################################################
# Diagram generator for the 'portfolio-report.csv' file.
#
# Usage: gnuplot -e "input_csv_file='data.csv'" \
#                -e "output_png_file='report.png'" \
#                -e "range_in_day=365" \
#                -e "resolution_x=2880" \
#                -e "resolution_y=1200" \
#                 summary-chart.plot
#
# Install gnuplot:
#    * Ubuntu: 'sudo apt-get install gnuplot'
#    * CentOS: 'sudo yum install gnuplot'
#
# Show installed terminals:
#    gnuplot> show variables all
#    gnuplot> set terminal
#
# Since  October 2022
# Author Arnold Somogyi <arnold.somogyi@gmail.com>
#
# Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
# BSD (2-clause) licensed
###############################################################################

# -----------------------------------------------------------------------------
# command line arguments
# -----------------------------------------------------------------------------
#input_csv_file = "portfolio-report/portfolio-report.csv"
#output_png_file = "portfolio-performance.png"
#range_in_day = 0
#resolution_x = 2880
#resolution_y = 1200

if (!exists("input_csv_file") || !exists("output_png_file")) {
    print "The mandatory parameters are not set."
    print "Usage: gnuplot -e \"input_csv_file='data.csv'\" -e \"output_png_file='report.png'\" summary-chart.plot"
    print ""
    exit
}

# -----------------------------------------------------------------------------
# environment configuration
# -----------------------------------------------------------------------------
set datafile separator ","
set title "Portfolio performance report in euro: COINBASE" font "Helvetica Bold, 15"
set grid
set border 0
set ylabel "EUR"

set autoscale x
set autoscale y
set xtics format "%b %d"

# x axis configuration
set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"
set format x "%Y-%m-%d"

# legend
set key outside left bottom horizontal samplen 1

# -----------------------------------------------------------------------------
# recommended output formats: https://en.wikipedia.org/wiki/Ultrawide_formats
#
#    - 16:10 Widescreen:     1280x800, 1440x900, 1680x1050, 1920x1200,
#                            2560x1600, 2880x1800,3072x1920, 3840x2400
#
#    - European Widescreen:  800x480, 1280x768
#
#    - 16:9 Widescreen:      1920x1080, 2560x1440, 3840x2160, 7680x4320
#
#    - 16:10 Tallboy:        640x400, 960x600, 1280x800, 1440x900, 1680x1050,
#                            1920x1200, 2560x1600, 3840x2400
#
#    - 24:10 Ultrawide:      2880x1200, 3840x1600, 4320x1800, 5760x2400,
#                            7680x3200
# -----------------------------------------------------------------------------
set output output_png_file
if (exists("resolution_x") && exists("resolution_y")) {
    set terminal pngcairo size resolution_x, resolution_y
} else {
    set terminal pngcairo size 2560, 1440
}

# -----------------------------------------------------------------------------
# plot historical data
# -----------------------------------------------------------------------------
if (exists("range_in_day")) {
    now = time(0)
    set xrange [now - (range_in_day * 24 * 60 * 60):now]
}

# -----------------------------------------------------------------------------
# line styles
# -----------------------------------------------------------------------------
set style line 1 linetype 1 linecolor rgb "#2979FF" linewidth 2 pointtype 7 pointsize 1  # blue
set style line 2 linetype 2 linecolor rgb "#FF1744" linewidth 2 pointtype 7 pointsize 1  # red
set style line 3 linetype 2 linecolor rgb "#76FF03" linewidth 2 pointtype 7 pointsize 1  # light-green
set style line 4 linetype 2 linecolor rgb "#D500F9" linewidth 2 pointtype 7 pointsize 1  # purple
set style line 5 linetype 2 linecolor rgb "#1DE9B6" linewidth 2 pointtype 7 pointsize 1  # teal

# -----------------------------------------------------------------------------
# chart
# -----------------------------------------------------------------------------
plot input_csv_file using 1:($6-$8) with linespoints linestyle 1 title "invested", \
     input_csv_file using 1:12      with linespoints linestyle 2 title "market value"
