#!/bin/bash

# ---------------------------------------------------------------
# delete old Markdown files
#
# arguments:
#   1: directory from where the files will be deleted
#   2: file mask, e.g. "*.md"
#   3: age of the files in days that will be deleted
#   4: quiet mode (true/false)
# ---------------------------------------------------------------
function delete_old_files {
    local directory mask days quiet_file_delete_mode file_list
    directory=$1
    mask=$2
    days=$3
    quiet_file_delete_mode="${4:-false}"
    file_list=$(find "$directory" -maxdepth 1 -type f -name "$mask" -mtime +"$days")

    printf "\n--> deleting the %s files from %s that older then %s days...\n" "$mask" "$directory" "$days"
    if [[ -z "$file_list" ]]; then
        printf "    no files to delete\n"
    else
        printf "    - %s\n" "${file_list[@]}"
        if ! $quiet_file_delete_mode ; then read -r -p "Press enter to continue"; fi
        echo "$file_list" | xargs rm
    fi
}

# ---------------------------------------------------------------
# download transactions from coinbase
#
# arguments:
#   1: jarfile the portfolio-analyzer jar file
#   2: output file
#   3: coinbase api access key
#   4: coinbase api passphrase
#   5: coinbase api secret
#   6: time zone
# ---------------------------------------------------------------
function download_coinbase_transactions {
    local jarfile output_file api_access_key api_passphrase api_secret time_zone
    jarfile=$1
    output_file=$2
    api_access_key=$3
    api_passphrase=$4
    api_secret=$5
    time_zone=$6

    printf "\n--> downloading the transaction history from Coinbase...\n"
    java -jar "$jarfile" coinbase \
       --api-access-key "$api_access_key" \
       --api-passphrase "$api_passphrase" \
       --api-secret "$api_secret" \
       --base-currency EUR \
       --output-file "$output_file" \
       --file-mode OVERWRITE \
       --hide-report-title \
       --replace default:coinbase \
       --language EN \
       --out-date-pattern "yyyy-MM-dd HH:mm:ss" \
       --out-timezone "$time_zone"
}

# ---------------------------------------------------------------
# combine multiple transaction files into one
#
# arguments:
#   1: jarfile the portfolio-analyzer jar file
#   2: time zone
#   3: output directory where the file will be saved
#   4: files to combine
# ---------------------------------------------------------------
function combine_transactions {
    local jarfile output_dir time_zone files
    jarfile=$1
    time_zone=$2
    output_dir=$3

    # handling the array argument
    files="$4[@]"
    files=("${!files}")
    files=$(IFS=,;printf  "%s" "${files[*]}")

    printf "\n--> combining the transaction files into one...\n"
    java -jar "$jarfile" combine \
       --input-files "$files" \
       --has-table-header \
       --overwrite \
       --output-file "'$output_dir/transactions_'yyyy-MM-dd'.md'" \
       --file-mode OVERWRITE \
       --language EN \
       --out-timezone "$time_zone"
}


# ---------------------------------------------------------------
# generating the daily portfolio summary markdown report
#
# arguments:
#   1: jarfile the portfolio-analyzer jar file
#   2: the file that contains the transactions
#   3: market data provider dictionary file
#   4: the name of the portfolio
#   5: price history file
#   6: the portfolio summary file
#   7: the portfolio report file
#   8: base currency
#   9: time zone
# ---------------------------------------------------------------
function generate_portfolio_summary {
    local jarfile transaction_file data_provider_file portfolio price_history_file
    local portfolio_summary_file portfolio_report_file base_currency time_zone
    jarfile=$1
    transaction_file=$2
    data_provider_file=$3
    portfolio=$4
    price_history_file=$5
    portfolio_summary_file=$6
    portfolio_report_file=$7
    base_currency=$8
    time_zone=$9

    printf "\n--> generating the daily '%s' portfolio summary markdown report...\n" "$portfolio"

    # create the report directories
    mkdir -p "$(dirname "$portfolio_summary_file")"
    mkdir -p "$(dirname "$portfolio_report_file")"
    mkdir -p "$(dirname "$price_history_file")"

    # run the portfolio-analyzer tool
    java -jar "$jarfile" portfolio \
       --input-file "$transaction_file" \
       --in-timezone "$time_zone" \
       --data-provider-file "$data_provider_file" \
       --has-report-title \
       --has-table-header \
       --portfolio "$portfolio" \
       --price-history "'$price_history_file'" \
       --base-currency "$base_currency" \
       --file-mode APPEND \
       --multiplicity ONE_DAY \
       --language EN \
       --out-timezone "$time_zone" \
       --portfolio-summary "'$portfolio_summary_file'" \
       --portfolio-report "'$portfolio_report_file'"
}


# ---------------------------------------------------------------
# generating the daily portfolio summary markdown report
#
# arguments:
#   1: the data CSV file that contains the portfolio reports
#   2: path to the output PNG image
#   3: diagram title
#   4: plot historical data
#   5: PNG image x resolution
#   6: PNG image y resolution
#   7: column number of the 'Total deposits' in the CSV file
#   8: column number of the 'Total withdrawals' in the CSV file
#   9: column number of the 'Total market value' in the CSV file
# ---------------------------------------------------------------
function generate_chart {
    local input_csv_file output_png_file title
    local data_range_in_days png_x_resolution png_y_resolution
    local col_total_deposits col_total_withdrawals col_total_market_value
    input_csv_file=$1
    output_png_file=$2
    title=$3
    data_range_in_days=$4
    png_x_resolution=$5
    png_y_resolution=$6
    col_total_deposits=$7
    col_total_withdrawals=$8
    col_total_market_value=$9


    printf "\n--> generating the PNG chart, output: %s...\n" "$output_png_file"

    # create the report directories
    mkdir -p "$workspace/charts"

    gnuplot \
        -e "input_csv_file=$input_csv_file" \
        -e "output_png_file=$output_png_file" \
        -e "diagram_title=$title" \
        -e "diagram_range_in_days=$data_range_in_days" \
        -e "diagram_resolution_x=$png_x_resolution" \
        -e "diagram_resolution_y=$png_y_resolution" \
        -e "col_total_deposits=$col_total_deposits" \
        -e "col_total_withdrawals=$col_total_withdrawals" \
        -e "col_total_market_value=$col_total_market_value" \
        $workspace/charts/summary-chart.plot
}


# ---------------------------------------------------------------
# main program
#
# arguments:
#   1: id of the tasks that will be executed, default: abcde
#       a: download the coinbase transactions
#       b: delete old coinbase transaction files
#       c: combine transaction files into one
#       d: delete the old transaction files
#       e: generate the portfolio summary markdown reports
#       f: generate the charts
# ---------------------------------------------------------------
set -u
set -e

base_currency="EUR"
time_zone="GMT"
file_max_age=5
quiet_file_delete_mode=true
jarfile="$HOME/workspace/sample-portfolio/bin/portfolio-analyzer-0.2.1.jar"
workspace="$HOME/workspace/sample-portfolio"

diagram_interval=730
diagram_resolution_x=1440
diagram_resolution_y=900

tasks_to_execute="${1:-abcdef}"

# ---- task a: coinbase downloader ------------------------------
if [[ "$tasks_to_execute" == *a* ]]; then
    download_coinbase_transactions \
        "$jarfile" \
        "'$workspace/transactions/coinbase/coinbase-transactions_'yyyy-MM-dd'.md'" \
        "ffg...g98" \
        "k18...8qd" \
        "WOs...Q==" \
        "$time_zone"
fi

# ---- task b: delete old coinbase transaction files ------------
if [[ "$tasks_to_execute" == *b* ]]; then
    delete_old_files \
        "$workspace/transactions/coinbase" \
        "coinbase-transactions_????-??-??.md" \
        "$file_max_age" \
        $quiet_file_delete_mode
fi

# ---- task c: combine transaction files ------------------------
if [[ "$tasks_to_execute" == *c* ]]; then
    files_to_combine=(
        "'$workspace/transactions/coinbase/coinbase-transactions_'yyyy-MM-dd'.md'"
        "'$workspace/transactions/coinbase/coinbase-correction-shib.md'"
        "'$workspace/transactions/interactive-brokers/interactive-brokers-transactions.md'"
    )
    combine_transactions \
        "$jarfile" \
        "$time_zone" \
        "$workspace/transactions" \
        files_to_combine
fi

# ---- task d: delete old transaction files ---------------------
if [[ "$tasks_to_execute" == *d* ]]; then
    delete_old_files \
        "$workspace/transactions" \
        "transactions_????-??-??.md" \
        "$file_max_age" \
        $quiet_file_delete_mode
fi

# ---- task e: generate the daily portfolio summary reports -----
if [[ "$tasks_to_execute" == *e* ]]; then
    portfolios=("coinbase" "ib" "*")
    for portfolio in "${portfolios[@]}"
    do
        suffix=$([ "$portfolio" == "*" ] && echo "" || echo "-$portfolio")
        generate_portfolio_summary \
            "$jarfile" \
            "'$workspace/transactions/transactions_'yyyy-MM-dd'.md'" \
            "'$workspace/market-data-providers.properties'" \
            "$portfolio" \
            "$workspace/price-histories/price-history_'yyyy-MM-dd'.md" \
            "$workspace/reports/portfolio-summary/portfolio-summary${suffix}_'yyyy-MM-dd'.md" \
            "$workspace/reports/portfolio-report/portfolio-report$suffix.csv" \
            "$base_currency" \
            "$time_zone"
    done
fi

# ---- task f: generate the charts ------------------------------
if [[ "$tasks_to_execute" == *f* ]]; then
    portfolios=("coinbase" "ib" "*")
    col_total_deposits=(6 6 12)
    col_total_withdrawals=(8 8 16)
    col_total_market_values=(12 12 23)

    for index in "${!portfolios[@]}"; do
        portfolio="${portfolios[$index]}"
        col_total_deposit="${col_total_deposits[$index]}"
        col_total_withdrawal="${col_total_withdrawals[$index]}"
        col_total_market_value="${col_total_market_values[$index]}"
        suffix=$([ "$portfolio" == "*" ] && echo "" || echo "-$portfolio")
        title=$([ "$portfolio" == "*" ] && echo "all" || echo "$portfolio")

        generate_chart \
            "'$workspace/reports/portfolio-report/portfolio-report${suffix}.csv'" \
            "'$workspace/charts/portfolio-report${suffix}.png'" \
            "'${title^^}'" \
            "$diagram_interval" \
            "$diagram_resolution_x" \
            "$diagram_resolution_y" \
            "$col_total_deposit" \
            "$col_total_withdrawal" \
            "$col_total_market_value"
    done
fi
