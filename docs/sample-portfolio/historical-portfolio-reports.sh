#!/bin/bash
# ##############################################################################
#  Portfolio history generator
#
#  This script can be used to generate an initial portfolio report CSV or Excel
#  file. Once you have this report with historical data back to the past
#  you can extend the file with the daily data by running the same command
#  from i.e. Linux cron scheduler.
#
#  Determine how long this bash script takes to run:
#     time bin/generate-historical-portfolio-reports.sh
#
#  Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
#  BSD (2-clause) licensed
#
#  author: arnold.somogyi@gmail.comm
#  since 2022 November
# ##############################################################################

set -u
set -e

start="2016-01-01"
end="2018-12-31"

daily_transaction_summary="'transactions/transactions_2022-10-08.md'"
portfolio_report="'reports/portfolio-report/portfolio-report.csv'"
daily_price_history="'price-histories/price-history_'yyyy-MM-dd'.md'"
data_providers="'market-data-providers.properties'"

jar="../../bin/portfolio-analyzer-0.2.1.jar"

while [ "$(date -d $start +%s)" -le "$(date -d $end +%s)" ]; do
    day=$(date --date $start  +%A)
    printf "\nTrade date: %s (%s)\n" "$start" "$day"
    java \
        -jar "$jar" portfolio \
        -i "$daily_transaction_summary" \
        -e \
        -a \
        -l "$data_providers" \
        -t "$start 23:59:59" \
        -L EN \
        -P "$daily_price_history" \
        -M APPEND \
        -U ONE_DAY \
        -O "'reports/portfolio-summaries/portfolio-summary_$start.md'" \
        -S "$portfolio_report"
    start=$(date -I -d "$start + 7 day")
    #read -r -p "Press enter to continue"
done
