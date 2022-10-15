#!/bin/bash
# ##############################################################################
#  Generate daily portfolio performance report
#
#  This script generates daily portfolio report and updates the portfolio
#  performance chart.
#
#  Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved
#  BSD (2-clause) licensed
#
#  author: arnold.somogyi@gmail.comm
#  since 2021 October
# ##############################################################################
coinbase_daily_transaction="'10-coinbase-transactions/coinbase-transactions_2022-08-27.md'"
coinbase_correction_shib="'11-coinbase-corrections/coinbase-corrections-shib.md'"
ib_transactions="'20-interactive-brokers-transactions/interactive-brokers-transactions.md'"
daily_transaction_summary="'90-transactions-summaries/transactions-report_'yyyy-MM-dd'.md'"
daily_portfolio_summary="'91-portfolio-summaries/portfolio-summary_'yyyy-MM-dd'.md'"
portfolio_report="'92-portfolio-report/portfolio-report.csv'"
daily_price_history="'93-price-histories/price-history_'yyyy-MM-dd'.md'"
data_providers="'market-data-providers.properties'"

coinbase_api_access_key="9...c"
coinbase_api_passphrase="9...7"
coinbase_api_secret="z...="

jar="../../bin/portfolio-analyzer-0.1.13.jar"

# ----------------------------------------------------------
# downloading transactions from coinbase
# ----------------------------------------------------------
function download_coinbase_transactions {
    printf "downloading transactions from coinbase...\n"
    java \
        -jar "$jar" coinbase \
        -k $coinbase_api_access_key \
        -p $coinbase_api_passphrase \
        -e $coinbase_api_secret \
        -b EUR \
        -O "$coinbase_daily_transaction" \
        -M OVERWRITE \
        -E \
        -R default:coinbase \
        -L EN \
        -D "yyyy-MM-dd HH:mm:ss" \
        -Z GMT
    printf "\n"
}

# ----------------------------------------------------------
# combine transaction data files
# ----------------------------------------------------------
function combine_transactions {
    printf "combine transaction data files...\n"
    java \
        -jar $jar combine \
        -i "$coinbase_daily_transaction, $coinbase_correction_shib, $ib_transactions" \
        -a \
        -o \
        -O "$daily_transaction_summary" \
        -M OVERWRITE \
        -L EN \
        -Z GMT
    printf "\n"
}

# ----------------------------------------------------------
# generating the daily reports
# ----------------------------------------------------------
function generate_reports {
    printf "generating the daily reports...\n"

    local date=$(date +%Y-%m-%d)
    java \
       -jar $jar portfolio \
       -i "$daily_transaction_summary" \
       -e \
       -a \
       -l "$data_providers" \
       -t "$date 23:00:00" \
       -B EUR \
       -P "$daily_price_history" \
       -L EN \
       -M APPEND \
       -U ONE_HOUR \
       -O "$daily_portfolio_summary" \
       -S "$portfolio_report"
    printf "\n"
}

# ----------------------------------------------------------
# generating a new portfolio-performance diagram
# ----------------------------------------------------------
function generate_chart {
    printf "generating the daily reports...\n"
    printf "\n"
}

# ----------------------------------------------------------
# main program starts here
# ----------------------------------------------------------
#download_coinbase_transactions
combine_transactions
generate_reports
generate_chart
