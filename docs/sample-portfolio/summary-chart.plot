###############################################################################
# Diagram generator for the 'portfolio-report.csv' file.
#
# Usage: gnuplot -e "input_csv_file='data.csv'" \
#                -e "output_png_file='report.png'" \
#                -e "diagram_title='coinbase'" \
#                -e "diagram_range_in_days=365" \
#                -e "diagram_resolution_x=2880" \
#                -e "diagram_resolution_y=1200" \
#                -e "col_total_deposits=6" \
#                -e "col_total_withdrawals=8" \
#                -e "col_total_market_value=12" \
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
#diagram_title = "coinbase"
#diagram_range_in_days = 0
#diagram__resolution_x = 2880
#diagram__resolution_y = 1200
#col_total_deposits = 6
#col_total_withdrawals = 8
#col_total_market_value = 12

# -----------------------------------------------------------------------------
# user input validation
# -----------------------------------------------------------------------------
if (!exists("input_csv_file") \
    || !exists("output_png_file") \
    || !exists("col_total_deposits") \
    || !exists("col_total_withdrawals") \
    || !exists("col_total_market_value") ) {

    print "The mandatory parameters are not set."
    print sprintf("Usage: gnuplot %s %s %s %s %s summary-chart.plot", \
        "-e \"input_csv_file='...csv'\"", \
        "-e \"output_png_file='...png'\"", \
        "-e \"col_total_deposits=6\"", \
        "-e \"col_total_withdrawals=8\"", \
        "-e \"col_total_market_value=12\"")
    print ""
    exit
}

if (!exists("diagram_title")) {
    diagram_title = "NA"
}

# -----------------------------------------------------------------------------
# environment configuration
# -----------------------------------------------------------------------------
set datafile separator ","
set title sprintf("Portfolio performance report in euro: %s", diagram_title) font "Helvetica Bold, 15"
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
if (exists("diagram_resolution_x") && exists("diagram_resolution_y")) {
    set terminal pngcairo size diagram_resolution_x, diagram_resolution_y
} else {
    set terminal pngcairo size 2560, 1440
}

# -----------------------------------------------------------------------------
# plot historical data
# -----------------------------------------------------------------------------
if (exists("diagram_range_in_days")) {
    now = time(0)
    set xrange [now - (diagram_range_in_days * 24 * 60 * 60):now]
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
plot input_csv_file using 1:(column(col_total_deposits)-column(col_total_withdrawals)) with linespoints linestyle 1 title "invested", \
     input_csv_file using 1:col_total_market_value                                     with linespoints linestyle 2 title "market value"
