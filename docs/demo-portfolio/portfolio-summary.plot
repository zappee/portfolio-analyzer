###############################################################################
# Diagram generator for the 'portfolio-report.csv' file.
#
# Usage: gnuplot -e "input_file='data.csv'" \
#                -e "output_file='report.png'" \
#                -e "title='coinbase'" \
#                -e "range_in_days=365" \
#                -e "resolution_x=2880" \
#                -e "resolution_y=1200" \
#                -e "base_currency='EUR'" \
#                portfolio-summary.plot
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
# Since  October 2022
# Author Arnold Somogyi <arnold.somogyi@gmail.com>
#
# Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
# BSD (2-clause) licensed
###############################################################################

# -----------------------------------------------------------------------------
# user input validation
# -----------------------------------------------------------------------------
if (!exists("input_file") \
    || !exists("output_file") \
    || !exists("title") \
    || !exists("range_in_days") \
    || !exists("resolution_x") \
    || !exists("resolution_y") \
    || !exists("base_currency")) {

    print "The mandatory parameters are not set."
    print sprintf("Usage: gnuplot %s %s %s %s %s %s %s portfolio-summary.plot", \
        "-e \"input_file='*.csv'\"", \
        "-e \"output_file='*.png'\"", \
        "-e \"title='coinbase'\"", \
        "-e \"range_in_days=365\"", \
        "-e \"resolution_x=2880\"", \
        "-e \"resolution_y=1200\"", \
        "-e \"base_currency=1200\"")
    print ""
    exit
}

# -----------------------------------------------------------------------------
# environment configuration
# -----------------------------------------------------------------------------
set title sprintf("Portfolio summary report in %s: %s", base_currency, title) font "Helvetica Bold, 15"
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
threshold_days = 30 
now = time(0)
line_width = 2
point_type = 0
point_size = 1

set xrange [now - (range_in_days * 24 * 60 * 60):now]

if (range_in_days <= threshold_days) {
    point_type = 7
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
x          = (range_in_days <= threshold_days) ? 1 : NaN
deposit    = sprintf("Total deposits in %s",     base_currency)
withdrawal = sprintf("Total withdrawals in %s",  base_currency)
investment = sprintf("Total investment in %s",   base_currency)
value      = sprintf("Total market value in %s", base_currency)
pl         = sprintf("Total P/L in %s", base_currency)

get_label(x) = sprintf("%'.0f", column(x))

plot input_file using 1:(column(deposit) - column(withdrawal)) with linespoints linestyle 5 title "deposits", \
     input_file using 1:(column(investment))                   with linespoints linestyle 6 title "invested amount", \
     input_file using 1:(column(value))                        with linespoints linestyle 1 title "market value", \
     ''         using x:(column(value)):(get_label(value))     every 2::0 with labels textcolor linestyle 1 center offset 0, 1 notitle, \
     input_file using 1:(column(pl))                           with linespoints linestyle 2 title "P/L", \
     ''         using x:(column(pl)):(get_label(pl))           every 2::0 with labels textcolor linestyle 2 center offset 0, 1 notitle

