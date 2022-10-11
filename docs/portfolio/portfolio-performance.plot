###############################################################################
# Diagram generator for the 'portfolio-report.csv' file.
#
# Usage: gnuplot portfolio-performance.plot
#
# Install gnuplot:
#    * Ubuntu: 'sudo apt-get install gnuplot'
#    * CentOS: 'sudo yum install gnuplot'
#
# Since  October 2022
# Author Arnold Somogyi <arnold.somogyi@gmail.com>
#
# Copyright (c) 2020-2021 Remal Software and Arnold Somogyi All rights reserved
# BSD (2-clause) licensed
###############################################################################

data_file = "92-portfolio-report/portfolio-report.csv"
chart_file = "portfolio-performance.png"

# -----------------------------------------------------------------------------
# environment configuration
# -----------------------------------------------------------------------------
set datafile separator ","
set title "Portfolio Performance Report" font "Helvetica Bold, 15"
set grid
set border 0
set ylabel "EUR"

set autoscale x
set autoscale y
set xtics format "%b %d"

# x line configuration
set xdata time
set timefmt "%Y-%m-%d %H:%M:%S"

# output format
set terminal pngcairo size 1280,720
set output chart_file

# legend
set key outside left bottom horizontal samplen 1

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
plot data_file using 1:20 with linespoints linestyle 1 title "profit loss", \
     data_file using 1:17 with linespoints linestyle 2 title "market value in EUR", \
     data_file using 1:14 with linespoints linestyle 3 title "invested amount in EUR", \
     data_file using 1:8  with linespoints linestyle 4 title "deposit in EUR"
