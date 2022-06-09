#!/bin/bash
# ##############################################################################
#   Portfolio history generator
#
#  This script can be used to generate an initial portfolio report CSV or Excel
#  file. Once you have this report with historical data back to the past
#  you can extend the file with the daily data by running the same command
#  from i.e. Linux cron scheduler.
#
#  Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
#  BSD (2-clause) licensed
#
#  author: arnold.somogyi@gmail.comm
#  since 2011 Jun
# ##############################################################################

start="2019-01-01"
end="2019-01-05"
transactionReport="'tmp/transactions-report_2022-05-30.md'"
dataProvidersRegistry="tmp/provider.properties"
priceHistory="'tmp/price-history.md'"
portfolioAnalyzer="target/portfolio-analyzer-0.1.13.jar"

while [ "$start" != "$end" ]; do
    echo "Trade date: $start"
    java \
        -jar "$portfolioAnalyzer" summary \
        -i "$transactionReport" \
        -l "$dataProvidersRegistry" \
        -t "$start 00:00:00" \
        -e \
        -a \
        -L EN \
        -C "COST_TOTAL, DEPOSIT_TOTAL, WITHDRAWAL_TOTAL, INVESTED_AMOUNT" \
        -P "$priceHistory" \
        -M OVERWRITE \
        -U ONE_DAY \
        -Z GMT \
        -q

    start=$(date -I -d "$start + 1 day")
done
