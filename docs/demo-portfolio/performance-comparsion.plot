###############################################################################
# Diagram generator for the 'portfolio-report.csv' file.
#
# Usage: gnuplot -e "series_1='a.csv'" \
#                -e "series_2='b.csv'" \
#                -e "series_3='a.csv'" \
#                -e "series_4='d.csv'" \
#                -e "series_5='e.csv'" \
#                -e "series_6='f.csv'" \
#                -e "output_file='report.png'" \
#                -e "range_in_days=365" \
#                -e "range_label='1 year'" \
#                -e "resolution_x=2880" \
#                -e "resolution_y=1200" \
#                -e "base_currency='EUR'" \
#                performance-comparsion.plot
#
#
# Recommended resolutions: https://en.wikipedia.org/wiki/Ultrawide_formats
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
#
# Install gnuplot:
#    * Ubuntu: 'sudo apt-get install gnuplot'
#    * CentOS: 'sudo yum install gnuplot'
#
# Show installed terminals:
#    gnuplot> show variables all
#    gnuplot> set terminal
#
# Since  December 2022
# Author Arnold Somogyi <arnold.somogyi@gmail.com>
#
# Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
# BSD (2-clause) licensed
###############################################################################

# -----------------------------------------------------------------------------
# command line arguments
# -----------------------------------------------------------------------------
threshold_days = 30 

# -----------------------------------------------------------------------------
# user input validation
# -----------------------------------------------------------------------------
if (!exists("series_1") \
    || !exists("output_file") \
    || !exists("range_in_days") \
    || !exists("range_label") \
    || !exists("resolution_x") \
    || !exists("resolution_y") \
    || !exists("base_currency")) {

    print "The mandatory parameters are not set."
    print sprintf("Usage: gnuplot %s %s %s %s %s %s %s performance-comparsion.plot", \
        "-e \"series_1='*.csv'\"", \
        "-e \"output_file='*.png'\"", \
        "-e \"range_in_days=365\"", \
        "-e \"range_label='1 week'\"", \
        "-e \"resolution_x=2880\"", \
        "-e \"resolution_y=1200\"", \
        "-e \"base_currency=1200\"")
    print ""
    exit
}

# -----------------------------------------------------------------------------
# environment configuration
# -----------------------------------------------------------------------------
set title sprintf("Portfolio performance comparsion, range: %s", range_label) font "Helvetica Bold, 15"
set datafile separator ","
set grid
set border 0
set ylabel base_currency

# x axis configuration
set autoscale x
set xtics format "%b %d"
set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"
set format x "%Y-%m-%d"

# y axis configuration
set autoscale y
set format y "%'.0f"

# legend
set key outside left bottom horizontal samplen 1

# output image size
set output output_file
set terminal pngcairo size resolution_x, resolution_y

# -----------------------------------------------------------------------------
# styles
# -----------------------------------------------------------------------------
now = time(0)
line_width = 2
point_type = 0
point_size = 1

set xrange [now - (range_in_days * 24 * 60 * 60):now]

if (range_in_days <= threshold_days) {
    point_t = 7
    set logscale y 1.2
}

set linetype 1 linecolor rgb "#f90d1b" linewidth line_width pointtype point_type pointsize point_size  # red
set linetype 2 linecolor rgb "#1a237e" linewidth line_width pointtype point_type pointsize point_size  # blue
set linetype 3 linecolor rgb "#fde005" linewidth line_width pointtype point_type pointsize point_size  # yellow
set linetype 4 linecolor rgb "#ec00fc" linewidth line_width pointtype point_type pointsize point_size  # pink
set linetype 5 linecolor rgb "#9d00fe" linewidth line_width pointtype point_type pointsize point_size  # violet
set linetype 6 linecolor rgb "#00cf35" linewidth line_width pointtype point_type pointsize point_size  # green

# -----------------------------------------------------------------------------
# chart
# -----------------------------------------------------------------------------
x = (range_in_days <= threshold_days) ? 1 : NaN
column_name = sprintf("Total market value in %s", base_currency)
get_label(x) = sprintf("%'.0f", column(x))

plot series_1 using 1:(column(column_name))                          with linespoints linestyle 1 title "coinbase", \
     ''       using x:(column(column_name)):(get_label(column_name)) every 2::0 with labels textcolor linestyle 1 center offset 0, 1 notitle, \
     series_2 using 1:(column(column_name))                          with linespoints linestyle 2 title "erste", \
     ''       using x:(column(column_name)):(get_label(column_name)) every 2::0 with labels textcolor linestyle 2 center offset 0, 1 notitle, \
     series_3 using 1:(column(column_name))                          with linespoints linestyle 5 title "interactive-brokers", \
     ''       using x:(column(column_name)):(get_label(column_name)) every 2::0 with labels textcolor linestyle 5 center offset 0, 1 notitle, \
     series_4 using 1:(column(column_name))                          with linespoints linestyle 4 title "randomcapital", \
     ''       using x:(column(column_name)):(get_label(column_name)) every 2::0 with labels textcolor linestyle 4 center offset 0, 1 notitle

