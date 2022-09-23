<img style="float: right;" src="docs/images/logo-remal.jpg" alt="remal logo" width="5%" height="5%" />

# Remal Portfolio Analyzer

> keywords: java, portfolio, stock market, gdax, interactive broker, etoro, prise, report, currency, exchange rate

## 1) Overview
Investors diversificate investments and build portfolio while investing money on stock market.
Diversification is the practice of spreading your investments around so that your exposure to any one type of asset is limited.
This practice is designed to help reduce the volatility of your portfolio over time.

While you are building your portfolio usually you end up in a situation where you need to manage multiple brokerage accounts parallelly.
For example, you use `Brokerage A` company to trade with cryptocurrencies, then you open another account with `Brokerage B` where you trade on the US market and probably you will open other accounts for the European market.
If you have multiple accounts, then to keep up to date your own portfolio register and build an overall `portfolio summary` report is hard and requires hard manual administration.

<p align="center"><img src="docs/images/remal-portfolio-diversification.png" alt="portfolio administration" /></p>

The `Remal Portfolio Analyzer` helps you to track the performance of your separated portfolios in one place with as less effort as possible.
The tool downloads your daily trading transactions from the brokerage companies, merges them into a big ledger, and generates a portfolio summary report based on the live market price as often as you wish.
The report can be any kind of diagram or a [Markdown (text)][markdown] or an Excel file.

## 2) How the tool works
The `Remal Portfolio Analyzer` supports the following activities:
* [Downloading the `trading-history` directly from a brokerage company.](#31-downloading-the-trading-history-data-from-a-brokerage-company)
* [Trading history file transformation, i.e. converting timestamps between time zones, etc.](#32-trading-history-file-transformation)
* [Combine multiple `trading-history` files into one.](#33-combine-multiple-trading-history-files-into-one)
* [Showing the `trading history` files.](#34-showing-the-trading-history-file)
* [Downloading the real time market price based on the provided `ticker/symbol` from market data-provider.](#35-downloading-the-real-time-market-price-of-a-company)
* [Generating the portfolio report.](#36-generating-the-portfolio-report)
* Showing the portfolio report on charts (column, line, pie, bar, etc.).
* Calculating a new portfolio report, especially the average price, based on a simulated buy/sell transaction that you are planning to execute. 

The following flow shows how a general workflow looks like:

<p align="center"><img src="docs/images/remal-general-workflow.png" alt="general workflow" /></p>

## 3) How to execute the tool
The `Remal Portfolio Analyzer` is a command-line tool (`CLI`) that accepts text input from the user to execute the functions.
Today, the majority of applications have a graphical user interfaces (`GUI`), and most users never use command-line interfaces.
However, CLI is still used to configure computers, install software, and access features that are not available in the graphical interface.
The CLI applications provide us with many benefits that are unavailable, difficult to achieve, or incomplete with any GUI applications.
These benefits are numerous, but four in particular may come immediately to mind:
* Scalability
* Scriptability
* Simple design
* Repeatable executions

Considering the benefits of the CLI, this tool is just the right tool for the job.

__The main operations that the `Remal Portfolio Analyzer` supports and the way of recall them is the following:__

* Command that shows the application help: `java -jar portfolio-analyzer.jar`
* Result:
    ```
    Usage: java -jar portfolio-analyzer.jar [-hV] [coinbase | show | combine | price | portfolio]
    Remal Portfolio Analyzer is a command-line tool that helps you to track your portfolio in one place and generate regular investment reports.
    
    -h, --help      Show this help message and exit.
    -V, --version   Print version information and exit.
    
    Commands:
    coinbase   Download your personal transactions from Coinbase.
    show       Show transactions.
    combine    Combine transactions coming from different sources.
    price      Get the price of a stock.
    portfolio  Generates portfolio summary report.
    
    Exit codes:
    0    Successful execution.
    1    An unexpected error appeared while executing this application.
    
    Please report issues at arnold.somogyi@gmail.com.
    Documentation, source code: https://github.com/zappee/portfolio-analyzer
    ```

### 3.1) Downloading the trading-history data from a brokerage company
The tool can download the daily trading transactions from the following brokerage companies:
* Coinbase: Crypto Currency marketplace
* Command that activates the `coinbase` trading history download: `java -jar portfolio-analyzer.jar coinbase`
  * Result:
    ```
    Usage: java -jar portfolio-analyzer.jar coinbase [-s] (-k=<key> -p=<passphrase> -e=<secret> [-b=<baseCurrency>] [-v=<inventoryValuation>] [-f=<from>]
    [-t=<to>]) [[-O=<outputFile>] [-M=<writeMode>] [-R=<replaces>[,<replaces>...]]... [-E] [-A] [-L=<language>]
    [-C=<columnsToHide>]... [-I=<decimalFormat>] [-D=<dateTimePattern>] [-Z=<zone>] [-F=<from>] [-T=<to>]]
  
    Download your personal transactions from Coinbase.
  
      -s, --silent             Perform actions without displaying any details.
  
    Input (Coinbase PRO API)
      -k, --api-access-key     Coinbase PRO API key.
      -p, --api-passphrase     Coinbase PRO API passphrase.
      -e, --api-secret         Coinbase PRO API secret.
      -b, --base-currency      The currency of your Coinbase account you are allowed to trade, e.g. "EUR", etc. Default: "EUR"
      -v, --valuation          Default inventory valuation type. Candidates: FIFO, LIFO. Default: "FIFO"
      -f, --in-from            Filter on trade date, after a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
      -t, --in-to              Filter on trade date, before a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
  
    Output:
      -O, --output-file        Write report to file (i.e. "'tmp/'yyyy-MM-dd'_report.md'"). Accepted extensions: .txt, .md and .csv
      -M, --file-mode          How to write the report to the file. Default: STOP_IF_EXIST Candidates: OVERWRITE, APPEND, STOP_IF_EXIST
      -R, --replace            Replace the portfolio name. Format: "from:to, from:to", e.g. "default:coinbase".
      -E, --hide-title         Hide the report title.
      -A, --hide-header        Hide the table header in the report.
      -L, --language           Two-letter ISO-639-1 language code that controls the report language. Default: EN
      -C, --columns-to-hide    Comma separated list of column names that won't be displayed in the report. Candidates: PORTFOLIO, SYMBOL, TYPE, VALUATION,
      TRADE_DATE, QUANTITY, PRICE, FEE, CURRENCY, ORDER_ID, TRADE_ID, TRANSFER_ID
      -I, --decimal-format     Format numbers and decimals in the report. Default: "###,###,###,###,###,###.########"
      -D, --out-date-pattern   Pattern for formatting date and time in the report. Default: "yyyy-MM-dd HH:mm:ss"
      -Z, --out-timezone       The timezone of the dates, e.g. "GMT+2", "Europe/Budapest" Default: the system default time-zone
      -F, --out-from           Filter on trade date, after a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
      -T, --out-to             Filter on trade date, before a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
    ```

The following fields are mandatory and must be provided:

| parameter name         | description                 |
|------------------------|-----------------------------|
| `-k, --api-access-key` | Coinbase PRO API key        |
| `-p, --api-passphrase` | Coinbase PRO API passphrase |
| `-e, --api-secret`     | Coinbase PRO API secret     |

The article ['How to create an API key'][coinbase-api-key] describes the steps to obtain your personal API keys.

The command bellow downloads your personal transaction history from `Coinbase` and show the transactions on the screen:

   ```
   java -jar portfolio-analyzer.jar coinbase \
      -k 94c...a1c \
      -p 9fx...xy7 \
      -e zuT...A==
   ```
_You must replace the API key values with your personal ones before the execution._

You will get a similar output:
   ```
   # Transaction report
   _Generated: 2022-09-18 14:02:05_
  
   |portfolio|symbol |type   |inventory valuation|trade date         |quantity          |price    |fee           |currency|order id     |trade id|transfer id  |
   |---------|-------|-------|-------------------|-------------------|------------------|---------|--------------|--------|-------------|--------|-------------|
   |default  |EUR    |DEPOSIT|                   |2022-01-18 08:51:51|    5 500         |     1   |              |EUR     |             |        |60dea8b3-b796|
   |default  |BTC-EUR|BUY    |                   |2022-02-02 09:15:44|        0.00640035|34 065.92|0.436067622144|EUR     |83bb62a9-c8a5|19094504|             |
   |default  |BTC-EUR|BUY    |                   |2022-02-02 09:15:49|        0.03359965|34 065.92|2.289205977856|EUR     |b0cd4543-0842|19067657|             |
   |default  |BTC-EUR|SELL   |FIFO               |2022-04-12 13:57:33|        0.04      |37 178.52|2.9742816     |EUR     |06e93b40-f824|11160462|             |
   |default  |ETH-EUR|BUY    |                   |2022-06-07 08:17:35|        1         | 1 645.07|3.29014       |EUR     |ca200a35-e23d|11653001|             |
   |default  |BTC-EUR|BUY    |                   |2022-07-05 06:47:25|        0.025     |19 370.35|0.9685175     |EUR     |61787b51-d425|11355386|             |
   |default  |ETH-EUR|BUY    |                   |2022-08-27 10:30:09|        1.5       | 1 515.51|4.54653       |EUR     |f8e65f80-30fc|14563886|             |
   ```

The following commands download and save your transaction history to a Markdown and a CSV file:

* Markdown (text) format:
   ```
  java \
     -jar portfolio-analyzer.jar coinbase \
     -k 94...1c \
     -p 9f...y7 \
     -e zu...== \
     -b EUR \
     -O "'coinbase-transactions_'yyyy-MM-dd'.md'" \
     -M OVERWRITE \
     -E \
     -R default:coinbase \
     -L EN \
     -D "yyyy-MM-dd HH:mm:ss" \
     -Z GMT
   ```
* Excel format:
   ```
  java \
     -jar portfolio-analyzer.jar coinbase \
     -k 94...1c \
     -p 9f...y7 \
     -e zu...== \
     -b EUR \
     -O "'coinbase-transactions_'yyyy-MM-dd'.csv'" \
     -M OVERWRITE \
     -E \
     -R default:coinbase \
     -L EN \
     -D "yyyy-MM-dd HH:mm:ss" \
     -Z GMT
   ```

The `*.csv` file can be opened as an Excel file.
While opening the file use comma (`,`) for the CSV separator character.

For the best user experience you can open the `*.md` file with a Markdown editor like [dillinger][dillinger]. The resul you get is a wel formatted report:

|portfolio|symbol |type   |inventory valuation|trade date         |quantity          |price    |fee           |currency|order id     |trade id|transfer id  |
|---------|-------|-------|-------------------|-------------------|------------------|---------|--------------|--------|-------------|--------|-------------|
|default  |EUR    |DEPOSIT|                   |2022-01-18 08:51:51|    5 500         |     1   |              |EUR     |             |        |60dea8b3-b796|
|default  |BTC-EUR|BUY    |                   |2022-02-02 09:15:44|        0.00640035|34 065.92|0.436067622144|EUR     |83bb62a9-c8a5|19094504|             |
|default  |BTC-EUR|BUY    |                   |2022-02-02 09:15:49|        0.03359965|34 065.92|2.289205977856|EUR     |b0cd4543-0842|19067657|             |
|default  |BTC-EUR|SELL   |FIFO               |2022-04-12 13:57:33|        0.04      |37 178.52|2.9742816     |EUR     |06e93b40-f824|11160462|             |
|default  |ETH-EUR|BUY    |                   |2022-06-07 08:17:35|        1         | 1 645.07|3.29014       |EUR     |ca200a35-e23d|11653001|             |
|default  |BTC-EUR|BUY    |                   |2022-07-05 06:47:25|        0.025     |19 370.35|0.9685175     |EUR     |61787b51-d425|11355386|             |
|default  |ETH-EUR|BUY    |                   |2022-08-27 10:30:09|        1.5       | 1 515.51|4.54653       |EUR     |f8e65f80-30fc|14563886|             |

### 3.2) Trading history file transformation
There is a possibility to convert the data in the transaction file.
You can
* rename the `portfolio name`
* convert the `trade date` between different timezones
* change date-time format of the `trade date`
* change the way how decimal numbers show in `quantity`, `price` and `fee` columns
* hide any columns
* filter the data by `trade-date`
* filter the data by `symbol`
* set the report language
* convert `*.md` formatted file to a `*.csv` content

Command that activates the `transform` command: `java -jar portfolio-analyzer.jar show`
Result:
  ```
  Usage: java -jar portfolio-analyzer.jar show [-s] ([-e] [-a] [-p=<portfolio>] [-c=<symbols>]... [-d=<dateTimePattern>] [-z=<zone>] [-f=<from>] [-t=<to>]
  [-m=<missingColumns>]... -i=<file>) [[-O=<outputFile>] [-M=<writeMode>] [-R=<replaces>[,<replaces>...]]... [-E]
  [-A] [-L=<language>] [-C=<columnsToHide>]... [-I=<decimalFormat>] [-D=<dateTimePattern>] [-Z=<zone>] [-F=<from>]
  [-T=<to>]]

  Show transactions.

    -s, --silent             Perform actions without displaying any details.

  Input:
    -i, --input-file         File with transactions. Accepted extensions: .txt, .md and .csv
    -e, --has-title          The report file contains title.
    -a, --has-header         The table has a header in the report.
    -p, --portfolio          Portfolio name filter.
    -c, --symbol             Product filter, that is a comma separated list with symbols, e.g. "BTC-EUR, AMZN".
    -d, --in-date-pattern    Pattern for parsing date and time. Default: "yyyy-MM-dd HH:mm:ss"
    -z, --in-timezone        The timezone of the dates, e.g. "GMT+2", "Europe/Budapest" Default: the system default time-zone
    -f, --in-from            Filter on trade date, after a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
    -t, --in-to              Filter on trade date, before a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
    -m, --missing-columns    Comma separated list to set the missing columns in the report. Use with the '-columns-to-hide' option.

  Output:
    -O, --output-file        Write report to file (i.e. "'tmp/'yyyy-MM-dd'_report.md'"). Accepted extensions: .txt, .md and .csv
    -M, --file-mode          How to write the report to the file. Default: STOP_IF_EXIST Candidates: OVERWRITE, APPEND, STOP_IF_EXIST
    -R, --replace            Replace the portfolio name. Format: "from:to, from:to", e.g. "default:coinbase".
    -E, --hide-title         Hide the report title.
    -A, --hide-header        Hide the table header in the report.
    -L, --language           Two-letter ISO-639-1 language code that controls the report language. Default: EN
    -C, --columns-to-hide    Comma separated list of column names that won't be displayed in the report. Candidates: PORTFOLIO, SYMBOL, TYPE, VALUATION,
                             TRADE_DATE, QUANTITY, PRICE, FEE, CURRENCY, ORDER_ID, TRADE_ID, TRANSFER_ID
    -I, --decimal-format     Format numbers and decimals in the report. Default: "###,###,###,###,###,###.########"
    -D, --out-date-pattern   Pattern for formatting date and time in the report. Default: "yyyy-MM-dd HH:mm:ss"
    -Z, --out-timezone       The timezone of the dates, e.g. "GMT+2", "Europe/Budapest" Default: the system default time-zone
    -F, --out-from           Filter on trade date, after a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
    -T, --out-to             Filter on trade date, before a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
  ```

### 3.3) Combine multiple trading-history files into one
As that was mentioned earlier, the `Remal Portfolio Analyzer` can download your transaction history from the brokerage companies.
The transaction history file contains information about the trades that you have executed earlier (i.e. buying price, selling price, deposit, withdrawals, fee, etc.).
That history file can be used to generate your portfolio report.
But if you have multiple brokerage accounts then you have multiple transaction history files, and if you would like to see your overall portfolio result then you need to have a transaction history file that contains all transaction data that you have executed.
The `combine` command helps you to merge your separated transaction history files into one.
Then you can generate the overall portfolio report based on this data.

This is how it works:

1. Use the `coinbase` function to download the transaction history from Coinbase
2. Maintain your transaction files manually that are contains your trade data coming from different brokerage companies
3. Optional: manipulate the transaction data if it is necessary
4. Optional: generate portfolio summary report per brokerage account
5. Combine the individual portfolio reports into one
6. Generate the overall portfolio summary report
7. Generate your charts based on the portfolio summary report (*.csv file) 

<p align="center"><img src="docs/images/remal-generate-overall-portfolio-report.png" alt="generate overall portfolio report" /></p>

Command that activates the `combine` command: `java -jar portfolio-analyzer.jar combine`

Result:
  ```
  Usage: java -jar portfolio-analyzer.jar combine [-q] ([-e] [-a] [-p=<portfolio>] [-c=<symbols>]... [-d=<dateTimePattern>] [-z=<zone>] [-f=<from>]
                                                  [-t=<to>] [-m=<missingColumns>]... [-i=<files>...]... [-o]) [[-O=<outputFile>] [-M=<writeMode>]
                                                  [-R=<replaces>[,<replaces>...]]... [-E] [-A] [-L=<language>] [-C=<columnsToHide>]...
                                                  [-I=<decimalFormat>] [-D=<dateTimePattern>] [-Z=<zone>] [-F=<from>] [-T=<to>]]
  
  Combine transactions coming from different sources.
  
    -q, --quiet              In this mode log wont be shown.
  
  Input:
    -i, --input-files        Comma separated list of files with transactions to be combined. Accepted extensions: .txt, .md and .csv
    -o, --overwrite          Overwrite the same transactions while combining them.
    -e, --has-title          The report file contains title.
    -a, --has-header         The table has a header in the report.
    -p, --portfolio          Portfolio name filter.
    -c, --symbol             Product filter, that is a comma separated list with symbols, e.g. "BTC-EUR, AMZN".
    -d, --in-date-pattern    Pattern for parsing date and time. Default: "yyyy-MM-dd HH:mm:ss"
    -z, --in-timezone        The timezone of the dates, e.g. "GMT+2", "Europe/Budapest" Default: the system default time-zone
    -f, --in-from            Filter on trade date, after a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
    -t, --in-to              Filter on trade date, before a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
    -m, --missing-columns    Comma separated list to set the missing columns in the report. Use with the '-columns-to-hide' option.
  
  Output:
    -O, --output-file        Write report to file (i.e. "'tmp/'yyyy-MM-dd'_report.md'"). Accepted extensions: .txt, .md and .csv
    -M, --file-mode          How to write the report to the file. Default: STOP_IF_EXIST Candidates: OVERWRITE, APPEND, STOP_IF_EXIST
    -R, --replace            Replace the portfolio name. Format: "from:to, from:to", e.g. "default:coinbase".
    -E, --hide-title         Hide the report title.
    -A, --hide-header        Hide the table header in the report.
    -L, --language           Two-letter ISO-639-1 language code that controls the report language. Default: EN
    -C, --columns-to-hide    Comma separated list of column names that won't be displayed in the report. Candidates: PORTFOLIO, SYMBOL, TYPE,
                               VALUATION, TRADE_DATE, QUANTITY, PRICE, FEE, CURRENCY, ORDER_ID, TRADE_ID, TRANSFER_ID
    -I, --decimal-format     Format numbers and decimals in the report. Default: "###,###,###,###,###,###.########"
    -D, --out-date-pattern   Pattern for formatting date and time in the report. Default: "yyyy-MM-dd HH:mm:ss"
    -Z, --out-timezone       The timezone of the dates, e.g. "GMT+2", "Europe/Budapest" Default: the system default time-zone
    -F, --out-from           Filter on trade date, after a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
    -T, --out-to             Filter on trade date, before a specified date. Pattern: "yyyy-MM-dd HH:mm:ss"
  ```

Example command that combines three different transaction files into one:
  ```
  java \
    -jar portfolio-analyzer.jar combine \
    -i "'daily-coinbase-transactions_'yyyy-MM-dd'.md', 'coinbase-corrections.md', 'ib-transactions.md'" \
    -a \
    -o \
    -O "'transactions-report_'yyyy-MM-dd'.md'" \
    -M OVERWRITE \
    -L EN \
    -Z GMT
   ```

### 3.4) Showing the trading history file
The `show` command can be used to show the content of the transaction history file on the screen.
The same command can be also used to transform history file content as well.

The following simple command prints the transaction history data stored in a `csv` file on the screen:
  ```
  java \
    -jar portfolio-analyzer.jar show \
    -i "'daily-coinbase-transactions_'yyyy-MM-dd'.csv'" \
    -a \
    -C "ORDER_ID, TRADE_ID, TRANSFER_ID"
   ```

Please check [paragraph 3.2](#32-trading-history-file-transformation) for more information. 

### 3.5) Downloading the real time market price of a company
The `Remal Portfolio Analyzer` is able to download real market prices of and known company.
In order to you can use this feature, you need to know the company's symbol/ticker name.
If you do not know that three character length symbol then search for it on the internet.

Popular symbols:
* `BTC-EUR`: Bitcoin price in euro
* `ETH-EUR`: Ethereum price in euro
* `AMZN`: Anazon
* `TSLA`: Tesla
* `USDEUR=X`: USD/EUR exchange rate

Command that you can use to download market price of a company: `java -jar portfolio-analyzer.jar price`

Result:
  ```
  Usage: java -jar portfolio-analyzer.jar price [-q] [-P=<priceHistoryFile>] (-i=<symbol> [-c=<tradeDate>] [-t=<dateTimePattern>] (-d=<dataProvider> |
                                                -p=<dataProviderFile>)) [[-U=<multiplicity>] [-M=<writeMode>] [-L=<language>] [-I=<decimalFormat>]
                                                [-D=<dateTimePattern>] [-Z=<zone>]]
  
  Get the price of a stock.
  
    -q, --quiet                In this mode log wont be shown.
    -P, --price-history        Storing the price in a file, e.g. "'price_'yyyy'.md'". Accepted extensions: .txt, .md and .csv
  
  Input:
    -i, --symbol               The product id that represents the company's stock.
    -c, --date                 The price of a stock on a certain date in the past.
    -t, --date-pattern         Pattern for parsing the provided date. Default: "yyyy-MM-dd HH:mm:ss".
    -d, --data-provider        Retrieve the market price using the provider. Candidates: YAHOO, COINBASE_PRO, NOT_DEFINED.
    -p, --data-provider-file   Path to a *.properties file to get the data provider name  used to retrieve the market price.
  
  Output:
    -U, --multiplicity         Controls the price export to file. Candidates: ONE_MINUTE, FIVE_MINUTES, FIFTEEN_MINUTES, THIRTY_MINUTES, ONE_HOUR, FOUR_HOURS,
                                 ONE_DAY, MANY. Default: ONE_HOUR.
    -M, --file-mode            How to write the history file to disk. Default: STOP_IF_EXIST Candidates: OVERWRITE, APPEND, STOP_IF_EXIST
    -L, --language             Two-letter ISO-639-1 language code that controls the report language. Default: EN.
    -I, --decimal-format       Format numbers and decimals in the report. Default: "###,###,###,###,###,###.########"
    -D, --out-date-pattern     Pattern for formatting date and time in the report. Default: "yyyy-MM-dd HH:mm:ss"
    -Z, --timezone             The timezone of the dates, e.g. "GMT+2", "Europe/Budapest" Default: the system default time-zone
  ```

The following command will download the Amazon stock price from Yahoo:
```
java \
  -jar portfolio-analyzer.jar price \
  -i AMZN \
  -d YAHOO \
  -c "2022-09-14 18:00:00"
 ```

Result:
  ```
|symbol|price     |trade date         |request date       |data provider|
|------|----------|-------------------|-------------------|-------------|
|AMZN  |128.550003|2022-09-14 00:00:00|2022-09-14 18:00:00|YAHOO        |
  ```

If you are interested in the real-time price then do not use the `date` filter.
If you wish, you can save the result to a Markdown or a CSV file as well.

For the better user experience you can define a `data-provider-dictionary` file and use it via the `--data-provider-file` parameter.
This dictionary file is also used by the tool when you generate the portfolio summary report.

The dictionary is a file with `key-value` pairs where the `key` is the `symbol` of the company and the `value` represents the `data-privider`:
* `AMZN=YAHOO`

In a complicated case, when the company's symbol that you use in your transaction history does not match with the symbol that is used by the data provider you must use the following value:
* `USD-EUR=YAHOO;USDEUR=X`

Sometimes the symbol that is used locally does not match with the company's international symbol name.
For example the symbol of `OTP Bank` that is used in the [Budapest Stock Exchange](bux) is `OTP`.
But Yahoo knows this company as `OTP.BD`.
In this case you must place a special definition in the dictionary file for this company, otherwise the portfolio summary generator may fail. 
* `OTP=YAHOO;OTP.BD`

The following example shows how a dictionary file may look like:
```
# cryptos
AVAX-EUR=COINBASE_PRO
BTC-EUR=COINBASE_PRO
DOGE-EUR=COINBASE_PRO
ETH-EUR=COINBASE_PRO
SHIB-EUR=COINBASE_PRO
SOL-EUR=COINBASE_PRO

# stocks
AMZN=YAHOO
MASTERPLAST=YAHOO;MAST.BD
MOL=YAHOO;MOL.BD
OPUS=YAHOO;OPUS.BD
OTP=YAHOO;OTP.BD
TSLA=YAHOO
XRP-EUR=YAHOO

# currencies
EUR-HUF=YAHOO;EURHUF=X
HUF-EUR=YAHOO;HUFEUR=X
USD-EUR=YAHOO;USDEUR=X
```
You can download a ready for use dictionary file here: [docs/market-data-providers.properties](docs/market-data-providers.properties)

The following example shows you how to define the path to a dictionary file:
```
java \
  -jar portfolio-analyzer.jar price \
  -i AMZN \
  -p "'docs/market-data-providers.properties'" \
  -c "2022-09-14 18:00:00"
 ```

If you wish to save the price that the tool downloaded to a `price-history` file, then you can control the number of prices within a period with the `multiplicity` parameter.
That way you can keep your price-history fila as small as possible.
Otherwise, the history file can be huge quickly, especially if you call this command from a loop.

Another cool feature of the `price` command is that if you use a price-history file and the requested price exists in the history, then the tool will not connect to the internet to download the price.
Instead of it, the tool will take the price from the history file.

The benefit of using the price-history file is the followings:
* That way you can use the tool on a computer where you have no internet connection.
* During the portfolio summary report generation, it is possible that the tool tries to download the same price multiple time. After the first download, the price will be stored in the history file. Then the next time the tool take the price from the history file. This can degrees the portfolio report generation time dramatically.

If you get a `[ERROR] Price not found` error message, then you need to use a different date and time because the data-provider that you use has no price for this time.

### 3.6) Generating the portfolio report

dfsddsf

## 4) Generating the portfolio summary diagram

## 5) Installation and system requirements

[markdown]: https://www.markdownguide.org/basic-syntax "Markdown"
[coinbase-api-key]: https://help.coinbase.com/en/exchange/managing-my-account/how-to-create-an-api-key
[dillinger]: https://dillinger.io
[bux]: https://www.bse.hu

<a href="https://trackgit.com"><img src="https://us-central1-trackgit-analytics.cloudfunctions.net/token/ping/kzedlbkk4k0r4vk2iack" alt="trackgit-views" /></a>
