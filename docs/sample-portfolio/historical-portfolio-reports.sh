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
step="7 day"
jarfile="/home/$USER/workspace/sample-portfolio/bin/portfolio-analyzer-0.2.1.jar"
workspace="/home/$USER/workspace/sample-portfolio"

while [ "$(date -d $start +%s)" -le "$(date -d $end +%s)" ]; do
    day=$(date --date $start  +%A)
    printf "\nTrade date: %s (%s)\n" "$start" "$day"

    portfolios=("coinbase" "interactive-brokers" "*")
    for pf in "${portfolios[@]}"
    do
        suffix=$([ "$pf" == "*" ] && echo "" || echo "-$pf")
        pf=$([ "$pf" == "*" ] && echo "" || echo "$pf")
        java \
           -jar "$jarfile" portfolio \
           --input-file "'$workspace/transactions/transactions_2022-10-28.md'" \
           --has-report-title \
           --has-table-header \
           --data-provider-file "'$workspace/market-data-providers.properties'" \
           --in-to "$start 21:00:00" \
           --language EN \
           --price-history "'$workspace/price-histories/price-history_$start.md'" \
           --file-mode APPEND \
           --multiplicity ONE_DAY \
           --portfolio-summary "'$workspace/reports/portfolio-summary/$pf/portfolio-summary${suffix}_$start.md'" \
           --portfolio-report "'$workspace/reports/portfolio-report/portfolio-report${suffix}.csv'"
    done
    start=$(date -I -d "$start + $step")
    read -r -p "Press enter to continue"
done
