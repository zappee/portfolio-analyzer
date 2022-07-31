#!/bin/bash
# ##############################################################################
#   Portfolio history generator
#
#  This script can be used to generate an initial portfolio report CSV or Excel
#  file. Once you have this report with historical data back to the past
#  you can extend the file with the daily data by running the same command
#  from i.e. Linux cron scheduler.
#
#  Determine how long this bash script takes to run:
#     time bin/historical-portfolio-report.sh
#
#  Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
#  BSD (2-clause) licensed
#
#  author: arnold.somogyi@gmail.comm
#  since 2021 Jun
# ##############################################################################

start="2018-01-18"
end="2022-08-01"
transactionReport="'tmp/transactions-report_2022-07-12.md'"
dataProvidersRegistry="'tmp/provider.properties'"
priceHistory="'tmp/price-history.md'"
jar="target/portfolio-analyzer-0.1.13.jar"

while [ "$(date -d $start +%s)" -le "$(date -d $end +%s)" ]; do
    echo "Trade date: $start"
    java \
        -jar "$jar" portfolio \
        -i "$transactionReport" \
        -e \
        -a \
        -l "$dataProvidersRegistry" \
        -t "$start 23:59:59" \
        -L EN \
        -P "$priceHistory" \
        -M APPEND \
        -U ONE_DAY \
        -O "'tmp/portfolio/portfolio-report_$start.md'"
    start=$(date -I -d "$start + 1 day")
    # read -r -p "Press enter to continue"
done
