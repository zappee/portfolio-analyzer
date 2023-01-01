#!/bin/bash
# ##############################################################################
#  Portfolio Analyzer Executor
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


# ------------------------------------------------------------------------------
#  Show help.
# ------------------------------------------------------------------------------
function show_help() {
    if [ "$1" -eq 0 ]
    then
        printf "\n"
        printf "Remal portfolio-analyzer executor.\n\n"
        printf "Environment Variable: PORTFOLIO_HOME=%s/portfolio-analyzer\n" "$HOME"
        printf "Usage: ./portfolio-analyzer.sh <task-id>\n"
        printf "Supported task IDs:\n"
        printf "   a:  coinbase transactions downloader\n"
        printf "   b:  combine transaction files\n"
        printf "   c:  generate a portfolio-summary report\n"
        printf "   d:  generate historical portfolio-summary reports\n"
        printf "   e:  generate portfolio-summary charts\n"
        printf "   f:  generate performance-comparison chart\n"
        printf "   g:  delete the old files\n"
        printf "   h:  do a backup\n"
        printf "   i:  show the execution time of portfolio-summary report\n"
        printf "   x:  show portfolio-analyzer configuration\n"
        printf "\n"
        printf "Example: ./pa.sh bd\n"
        printf "         ./pa.sh ef\n\n"
        printf "Contact: arnold.somogyi@gmail.com\n\n"
        printf "Copyright (c) 2020-2022 Remal Software and Arnold Somogyi All rights reserved\n"
        printf "BSD (2-clause) licensed\n\n"
        exit 0
    fi
}

# ------------------------------------------------------------------------------
#  Get the last execution date of the reports.
#  It reads the last execution time from the portfolio-report files.
#
#  Arguments:
#     1:      the transaction CSV file
#     return: the last execution date-time, date format: "YYYY-MM-DD HH:MM:SS"
# ------------------------------------------------------------------------------
function get_last_execution() {
    local report_file last_execution
    report_file="$1"
    last_execution="$(tail -n 1 "$report_file" | awk -F "," '{print $1}')"
    printf "%s" "$(date +"%Y-%m-%d %H:%M:%S" -d "$last_execution")"
}


# ------------------------------------------------------------------------------
#  Increase the date.
#
#  Arguments:
#     1:      date-time to increase, format: "YYYY-MM-DD HH:MM:SS"
#     2:      the adjustment value, e.g. "1 day", "6 hours", etc.
#     return: the adjusted date-time in format of "YYYY-MM-DD HH:MM:SS"
# ------------------------------------------------------------------------------
function increase_timestamp() {
    local original_timestamp adjustment
    original_timestamp="$1"
    adjustment="$2"
    date -d "$original_timestamp" +%s -u | xargs -i echo "{} + $adjustment" | bc | xargs -i date -d @{} "+%Y-%m-%d %H:%M:%S" -u
}


# ------------------------------------------------------------------------------
#  Get the next end date of the report.
# ------------------------------------------------------------------------------
function get_end_date() {
    local current_time
    current_time=$(date +%H:%M)

    if [[ "$current_time" > "23:00" ]] || [[ "$current_time" < "07:00" ]]; then
     printf "%s" "$(date +"%Y-%m-%d %H:%M:%S")"
   else
     printf "%s" "$(date +"%Y-%m-%d %H:%M:%S" -d "1 day ago")"
   fi
}


# ------------------------------------------------------------------------------
#  Download transactions from coinbase.
#
#  Arguments:
#     1: output file
# ------------------------------------------------------------------------------
function download_coinbase_transactions {
    local output_file
    output_file="$1"

    printf "\n--> downloading the transaction history from Coinbase...\n"
    java -jar "$workspace/bin/$portfolio_analyzer_jar" coinbase \
       --api-access-key "$coinbaseAccessKey" \
       --api-passphrase "$coinbasePassphrase" \
       --api-secret "$coinbaseSecret" \
       --base-currency "$base_currency" \
       --output-file "$output_file" \
       --file-mode OVERWRITE \
       --hide-report-title \
       --replace default:coinbase \
       --language "$language" \
       --out-date-pattern "yyyy-MM-dd HH:mm:ss" \
       --out-timezone "$time_zone"
}


# ------------------------------------------------------------------------------
#  Combine multiple transaction files into one.
#
#  Arguments:
#     1: output directory where the file will be saved
#     2: files to combine
# ------------------------------------------------------------------------------
function combine_transactions {
    local output_dir files files_as_strings
    output_dir="$1"

    # handling the array argument
    shift
    files=("$@")
    files_as_strings=$(IFS=,; printf "%s" "${files[*]}")

    printf "\n--> combining the transaction files into one..."
    printf "\n    files to combine:\n"
    printf "       - %s\n" "${files[@]}"

    java -jar "$workspace/bin/$portfolio_analyzer_jar" combine \
       --input-files "$files_as_strings" \
       --has-table-header \
       --overwrite \
       --output-file "'$output_dir/transactions_'yyyy-MM-dd'.md'" \
       --file-mode OVERWRITE \
       --language "$language" \
       --out-timezone "$time_zone"
}


# ------------------------------------------------------------------------------
#  Generating the daily portfolio-summary markdown report.
#
#  Arguments:
#     1: name of the portfolio
#     2: the file that contains the transactions
#     3: market data provider dictionary file
#     4: price history file
#     5: portfolio-summary file
#     6: portfolio report file
# ------------------------------------------------------------------------------
function generate_portfolio_summary {
    local portfolio data_source
    local data_provider price_history portfolio_summary portfolio_report
    portfolio="$1"
    data_source="$2"
    data_provider="$3"
    price_history="$4"
    portfolio_summary="$5"
    portfolio_report="$6"

    printf "\n--> generating the portfolio-summary markdown report..."
    printf "\n    portfolio:            %s" "$portfolio"
    printf "\n    data source:          %s" "$data_source"
    printf "\n    market data provider: %s" "$data_provider"
    printf "\n    price history:        %s" "$price_history"
    printf "\n    portfolio-summary:    %s" "$portfolio_summary"
    printf "\n    portfolio report:     %s" "$portfolio_report"
    printf "\n"

    # create the report directories
    mkdir -p "$(dirname "$price_history")"
    mkdir -p "$(dirname "$portfolio_summary")"
    mkdir -p "$(dirname "$portfolio_report")"

    java -jar "$workspace/bin/$portfolio_analyzer_jar" portfolio \
       --input-file "$data_source" \
       --in-timezone "$time_zone" \
       --data-provider-file "$data_provider" \
       --has-report-title \
       --has-table-header \
       --portfolio "$portfolio" \
       --price-history "$price_history" \
       --base-currency "$base_currency" \
       --file-mode APPEND \
       --multiplicity ONE_DAY \
       --language "$language" \
       --out-timezone "$time_zone" \
       --portfolio-summary "$portfolio_summary" \
       --portfolio-report "$portfolio_report"
}


# ------------------------------------------------------------------------------
#  Generating historical portfolio-summary markdown reports.
#
#  Arguments:
#     1: name of the portfolio
#     2: the file that contains the transactions
#     3: market data provider dictionary file
#     4: price history file
#     5: portfolio-summary file
#     6: portfolio report file
#     7: start date
# ------------------------------------------------------------------------------
function generate_historical_portfolio_summary {
    local portfolio data_source data_provider
    local price_history portfolio_summary portfolio_report start day
    portfolio="$1"
    data_source="$2"
    data_provider="$3"
    price_history="$4"
    portfolio_summary="$5"
    portfolio_report="$6"
    start="$7"
    day=$(date --date "$start" +%A)

    printf "\n--> generating portfolio-summary history markdown reports..."
    printf "\n    start date: %s (%s)" "$start" "$day"
    printf "\n    portfolio:            %s" "$portfolio"
    printf "\n    data source:          %s" "$data_source"
    printf "\n    market data provider: %s" "$data_provider"
    printf "\n    price history:        %s" "$price_history"
    printf "\n    portfolio-summary:    %s" "$portfolio_summary"
    printf "\n    portfolio report:     %s" "$portfolio_report"
    printf "\n"

    # removing the trailing and ending single quotes and
    # create the directories
    mkdir -p "$(dirname "${price_history//"'"/}")"
    mkdir -p "$(dirname "${portfolio_summary//"'"/}")"
    mkdir -p "$(dirname "${portfolio_report//"'"/}")"

    java -jar "$workspace/bin/$portfolio_analyzer_jar" portfolio \
       --input-file "$data_source" \
       --in-timezone "$time_zone" \
       --data-provider-file "$data_provider" \
       --in-to "$start" \
       --has-report-title \
       --has-table-header \
       --portfolio  "$portfolio" \
       --price-history "$price_history" \
       --base-currency "$base_currency" \
       --file-mode APPEND \
       --multiplicity ONE_DAY \
       --language "$language" \
       --out-timezone "$time_zone" \
       --portfolio-summary "$portfolio_summary" \
       --portfolio-report "$portfolio_report"
}


# ---------------------------------------------------------------
#  Delete old files
#
#  Arguments:
#     1: directory from where the files will be deleted
#     2: file mask, e.g. "*.md"
# ---------------------------------------------------------------
function delete_old_files {
    local directory mask
    directory="$1"
    mask="$2"

    printf "\n--> deleting the %s files from %s that older then %s days...\n" "$mask" "$directory" "$file_max_age"

    local files_to_delete
    files_to_delete=$(find "$directory" -maxdepth 1 -type f -name "$mask" -mtime +"$file_max_age")

    if [[ -z "$files_to_delete" ]]; then
        printf "    no files to delete\n"
    else
        local array=($files_to_delete)
        printf "    - %s\n" "${array[@]}"

        if ! $quiet_mode ; then read -r -p "\nPress enter to continue"; fi
        echo "$files_to_delete" | xargs rm
    fi
}


# ------------------------------------------------------------------------------
#  Generating the portfolio-summary PNG chart.
#
#  Arguments:
#      1: the CSV file that contains the portfolio report
#      2: output file
#      3: diagram range in days
#      4: image X resolution
#      5: image Y resolution
#      6: diagram title
#      7: base currency
# ------------------------------------------------------------------------------
function portfolio_summary_chart {
    local input_file output_file range_in_days resolution_x resolution_y
    local base_currency title
    input_file="$1"
    output_file="$2"
    range_in_days="$3"
    resolution_x="$4"
    resolution_y="$5"
    title="$6"
    base_currency="$7"

    printf "\n--> generating the portfolio-summary PNG chart..."
    printf "\n    data source:        %s" "$input_file"
    printf "\n    output file:        %s" "$output_file"
    printf "\n    diagram range:      %s days" "$range_in_days"
    printf "\n    image X resolution: %s" "$resolution_x"
    printf "\n    image Y resolution: %s" "$resolution_y"
    printf "\n    diagram title:      %s" "$title"
    printf "\n    base currency:      %s" "$base_currency"
    printf "\n"

    mkdir -p "$workspace/charts"
    gnuplot \
        -e "input_file='$input_file'" \
        -e "output_file='$output_file'" \
        -e "range_in_days=$range_in_days" \
        -e "resolution_x=$resolution_x" \
        -e "resolution_y=$resolution_y" \
        -e "title='$title'" \
        -e "base_currency='$base_currency'" \
        "$workspace/portfolio-summary.plot"
}


# ------------------------------------------------------------------------------
#  Generating the portfolio-comparison PNG chart.
#
#  Arguments:
#       1: data series 1 (*.csv file)
#       2: data series 2 (*.csv file)
#       3: output file
#       4: diagram range in days
#       5: range label
#       6: image X resolution
#       7: image Y resolution
#       8: base currency
# ------------------------------------------------------------------------------
function performance_comparison_chart {
    local series_1 series_2
    local output_file range_in_days range_label resolution_x resolution_y base_currency
    series_1="$1"
    series_2="$2"
    output_file="$3"
    range_in_days="$4"
    range_label="$5"
    resolution_x="$6"
    resolution_y="$7"
    base_currency="$8"

    printf "\n--> generating the portfolio-summary PNG chart..."
    printf "\n    series 1:           %s" "$series_1"
    printf "\n    series 2:           %s" "$series_2"
    printf "\n    output file:        %s" "$output_file"
    printf "\n    range in days:      %s" "$range_in_days"
    printf "\n    range label:        %s" "$range_label"
    printf "\n    image X resolution: %s" "$resolution_x"
    printf "\n    image Y resolution: %s" "$resolution_y"
    printf "\n    base currency:      %s" "$base_currency"
    printf "\n"

    mkdir -p "$workspace/charts"

    gnuplot \
        -e "series_1='$series_1'" \
        -e "series_2='$series_2'" \
        -e "output_file='$output_file'" \
        -e "range_in_days=$range_in_days" \
        -e "range_label='$range_label'" \
        -e "resolution_x=$resolution_x" \
        -e "resolution_y=$resolution_y" \
        -e "base_currency='$base_currency'" \
        "$workspace/performance-comparison.plot"
}


# ------------------------------------------------------------------------------
#  Compress and back up the whole workspace.
# ------------------------------------------------------------------------------
function do_backup {
    local path_to_workspace workspace_name backup_file
    path_to_workspace="$(dirname "$workspace")"
    workspace_name="$(basename "$workspace")"
    backup_file="$workspace/backup/${workspace_name}_$(date '+%Y-%m-%d_%H.%M.%S').tar.gz"

    printf "\n--> backing up the workspace..."
    printf "\n    path to workspace: %s" "$path_to_workspace"
    printf "\n    workspace name:    %s" "$workspace_name"
    printf "\n    backup file:       %s" "$backup_file"
    printf "\n"

    mkdir -p "$workspace/backup"
    tar --exclude='backup' -C "$path_to_workspace" -czvf "$backup_file" "$workspace_name"
    printf "\nbackup file: %s\n\n" "$backup_file"
}


# ------------------------------------------------------------------------------
#  Main program.
# ------------------------------------------------------------------------------
#
#  Program arguments
#  -----------------
#     The id of the task(s) that will be executed.
#     Default: execute nothing
#
#  Task ID definitions
#  -------------------
#     a:  coinbase transactions downloader
#     b:  combine transaction files
#     c:  generate a portfolio-summary report
#     d:  generate historical portfolio-summary reports
#     e:  generate portfolio-summary charts
#     f:  generate performance-comparison chart
#     g:  delete the old files
#     h:  do a backup
#     i:  show the last execution time
#     x:  show portfolio-analyzer configuration
#
#  Variables used in this bash script
#  ----------------------------------
#     workspace                Path to the directory that contains the files.
#
#     jar_file                 The Portfolio Analyzer JAR file name.
#
#     language                 Report language.
#
#     coinbaseAccessKey        Your personal coinbase api access key.
#
#     coinbasePassphrase       Your personal coinbase api passphrase.
#
#     coinbaseSecret           Your personal coinbase api secret.
#
#     portfolios               The name of the portfolios that the report
#                              includes.
#
#     base_currency            The currency of the reports.
#
#     time_zone                The timezone of the reports.
#
#     file_max_age             The files that are older then given days will be
#                              deleted.
#
#     quiet_mode               If it set to "true", then the old files will be
#                              deleted automatically, without question.
#                              Otherwise you need to approve the deletion.
#
#     step_in_sec              The frequency of the data in the report files and
#                              the charts.
#
#     diagram_resolution_x     The width of the chart image.
#
#     diagram_resolution_y     The height of the chart image.
# ------------------------------------------------------------------------------
set -u
set -e

workspace="${PORTFOLIO_HOME:-HOME/Java/portfolio-analyzer/docs/demo-portfolio}"
portfolio_analyzer_jar="portfolio-analyzer-0.2.1.jar"
coinbaseAccessKey="ff7...998"
coinbasePassphrase="k1v...3qd"
coinbaseSecret="WOs...Q=="
language="EN"
portfolios=("coinbase" "interactive-brokers" "*")
base_currency="EUR"
time_zone=""
file_max_age=5
quiet_mode=true
step_in_sec="$((60 * 60 * 24))"
diagram_resolution_x=1440
diagram_resolution_y=900
diagram_ranges=(7 30 90 180 365 730 1826 3652)
diagram_range_labels=("1 week" "1 month" "3 months" "6 months" "1 year" "2 years" "5 years" "max")
files_to_combine=(
    "'$workspace/transactions/coinbase/coinbase-transactions.md'"
    "'$workspace/transactions/coinbase/coinbase-corrections-shib.md'"
    "'$workspace/transactions/interactive-brokers/interactive-brokers-transactions.md'")
tasks="${1:-}"

# ---- check existence of input arguments --------------------------------------
show_help "$#"

# ---- task a: coinbase transactions downloader --------------------------------
if [[ "$tasks" == *a* ]]; then
    download_coinbase_transactions \
        "'$workspace/transactions/coinbase/coinbase-transactions_'yyyy-MM-dd'.md'" \
        "$time_zone"
fi

# ---- task b: combine transaction files ---------------------------------------
if [[ "$tasks" == *b* ]]; then
    combine_transactions \
        "$workspace/transactions" \
        "${files_to_combine[@]}"
fi

# ---- task c: generate a portfolio-summary report -----------------------------
if [[ "$tasks" == *c* ]]; then
    for portfolio in "${portfolios[@]}"; do
        suffix=$([ "$portfolio" == "*" ] && echo "" || echo "-$portfolio")
        generate_portfolio_summary \
            "$portfolio" \
            "'$workspace/transactions/transactions_'yyyy-MM-dd'.md'" \
            "'$workspace/market-data-providers.properties'" \
            "'$workspace/price-histories/price-history_'yyyy-MM-dd'.md'" \
            "'$workspace/reports/portfolio-summary/portfolio-summary${suffix}_'yyyy-MM-dd'.md'" \
            "'$workspace/reports/portfolio-report/portfolio-report$suffix.csv'"
    done
fi

# ---- task d: generate historical portfolio-summary reports -------------------
if [[ "$tasks" == *d* ]]; then
    if ! "$quiet_mode" ; then read -r -p "Press enter to continue"; fi
    end="$(get_end_date)"

    for portfolio in "${portfolios[@]}"; do
        suffix=$([ "$portfolio" == "*" ] && echo "" || echo "-$portfolio")
        subdir=$([ "$portfolio" == "*" ] && echo "" || echo "$portfolio")
        last_execution="$(get_last_execution "$workspace/reports/portfolio-report/portfolio-report$suffix.csv")"
        start="$(increase_timestamp "$last_execution" "$step_in_sec")"

        if [ "$(date -d "$start" +%s)" -gt "$(date -d "$end" +%s)" ]; then printf "\nThere is nothing to do at the moment.\n\n"; fi
        while [ "$(date -d "$start" +%s)" -le "$(date -d "$end" +%s)" ]; do
            generate_historical_portfolio_summary \
                "$portfolio" \
                "'$workspace/transactions/transactions_'yyyy-MM-dd'.md'" \
                "'$workspace/market-data-providers.properties'" \
                "'$workspace/price-histories/price-history_'yyyy-MM-dd'.md'" \
                "'$workspace/reports/portfolio-summary/$subdir/portfolio-summary${suffix}_'yyyy-MM-dd'.md'" \
                "'$workspace/reports/portfolio-report/portfolio-report$suffix.csv'" \
                "$start"

            start="$(increase_timestamp "$start" "$step_in_sec")"
            if ! "$quiet_mode" ; then read -r -p "Press enter to continue"; fi
            sleep 10 # yahoo api does not support unlimited access
        done
    done
fi

# ---- task e: generate portfolio-summary charts -------------------------------
if [[ "$tasks" == *e* ]]; then
    for portfolio_index in "${!portfolios[@]}"; do 
        portfolio="${portfolios[$portfolio_index]}"
        suffix=$([ "$portfolio" == "*" ] && echo "" || echo "-$portfolio")
        title=$([ "$portfolio" == "*" ] && echo "all" || echo "$portfolio")

        for range_index in "${!diagram_ranges[@]}"; do
            range_label=${diagram_range_labels[$range_index]}
            range_label=${range_label// /-}
            portfolio_summary_chart \
                "$workspace/reports/portfolio-report/portfolio-report${suffix}.csv" \
                "$workspace/charts/portfolio-report${suffix}-$range_label.png" \
                "${diagram_ranges[$range_index]}" \
                "$diagram_resolution_x" \
                "$diagram_resolution_y" \
                "${title^^} (${diagram_range_labels[$range_index]})" \
                "$base_currency"
        done
    done
fi

# ---- task f: generate performance-comparison chart ---------------------------
if [[ "$tasks" == *f* ]]; then
    csv_files=(
        "$workspace/reports/portfolio-report/portfolio-report-${portfolios[0]}.csv"
        "$workspace/reports/portfolio-report/portfolio-report-${portfolios[1]}.csv" )

    for range_index in "${!diagram_ranges[@]}"; do
        range_label=${diagram_range_labels[$range_index]}
        range_label=${range_label// /-}
        performance_comparison_chart \
            "${csv_files[0]}" \
            "${csv_files[1]}" \
            "$workspace/charts/performance-comparison-${range_label}.png" \
            "${diagram_ranges[$range_index]}" \
            "${diagram_range_labels[$range_index]}" \
            "$diagram_resolution_x" \
            "$diagram_resolution_y" \
            "$base_currency"
    done
fi

# ---- task g: delete the old files --------------------------------------------
if [[ "$tasks" == *g* ]]; then
    delete_old_files "$workspace/transactions/coinbase" "coinbase-transactions_????-??-??.md"
    delete_old_files "$workspace/transactions" "transactions_????-??-??.md"
fi

# ---- task h: do a backup -----------------------------------------------------
if [[ "$tasks" == *h* ]]; then
    do_backup
fi

# ---- task i: show the last execution time ------------------------------------
if [[ "$tasks" == *i* ]]; then
    printf "\n--> getting the last and the next execution time, step: %s seconds..." "$step_in_sec"
    for portfolio in "${portfolios[@]}"; do
        suffix=$([ "$portfolio" == "*" ] && echo "" || echo "-$portfolio")
        last_execution="$(get_last_execution "$workspace/reports/portfolio-report/portfolio-report$suffix.csv")"
        printf "\n    %-22s: %s" "$portfolio" "$last_execution"
    done
    printf "\n\n"
fi

# ---- task x: show portfolio-analyzer configuration ---------------------------
if [[ "$tasks" == *x* ]]; then
    printf "\n--> showing portfolio-analyzer configuration...\n"
    workspace="${PORTFOLIO_HOME:-HOME/Workspace/portfolio-analyzer}"
    printf "PORTFOLIO_HOME: %s\n" "$PORTFOLIO_HOME"
    printf "Workspace:      %s\n" "$workspace"
    printf "JAR:            %s\n" "$workspace/bin/$portfolio_analyzer_jar"
fi
