debug:
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

Coinbase
--------
java \
    -jar target/portfolio-analyzer-0.1.13.jar coinbase \
    -k 94...1c \
    -p 9f...y7 \
    -e zu...== \
    -b EUR \
    -O "'tmp/coinbase-transactions_'yyyy-MM-dd'.md'" \
    -M OVERWRITE \
    -E \
    -R default:coinbase \
    -L EN \
    -D "yyyy-MM-dd HH:mm:ss" \
    -Z GMT

Show
----
java \
    -jar target/portfolio-analyzer-0.1.13.jar show \
    -i "'tmp/coinbase-transactions_'yyyy-MM-dd'.md'" \
    -a \
    -C "ORDER_ID, TRADE_ID, TRANSFER_ID"

Combine
-------
java \
    -jar target/portfolio-analyzer-0.1.13.jar combine \
    -i "'tmp/coinbase-transactions_'yyyy-MM-dd'.md', 'tmp/coinbase-correction-shib.md', 'tmp/ib-transactions.md'" \
    -a \
    -o \
    -O "'tmp/transactions-report_'yyyy-MM-dd'.md'" \
    -M OVERWRITE \
    -L EN \
    -Z GMT

Price
-----
java \
    -jar target/portfolio-analyzer-0.1.13.jar price \
    -i AMZN \
    -f "'docs/provider.properties'" \
    -P "'tmp/price-history.md'" \
    -M OVERWRITE \
    -U MANY

Portfolio
-------
java \
    -jar target/portfolio-analyzer-0.1.13.jar portfolio \
    -i "'tmp/transactions-report_2022-07-12.md'" \
    -e \
    -a \
    -l "docs/market-data-providers.properties" \
    -B EUR \
    -P "'tmp/market-price-history.md'" \
    -L EN \
    -C "PORTFOLIO, SYMBOL, PROFIT_LOSS, QUANTITY, AVG_PRICE, INVESTED_AMOUNT, MARKET_UNIT_PRICE, MARKET_VALUE, COST_TOTAL, DEPOSIT_TOTAL, WITHDRAWAL_TOTAL" \
    -M OVERWRITE \
    -U ONE_HOUR \
    -O "'tmp/summary.csv'"

cash value:
The cash value, also referred to as the cash balance value, is the total amount of actual
money — the most liquid of funds — in the account. This figure is the amount that is
available for immediate withdrawal or the total amount available to purchase securities
in a cash account.

account value:
The account value, also known as total equity, is the total value of all the holdings of
the trading account. Not just the securities, but the cash as well. This figure is
calculated by adding the total amount of cash in the account and the current market value
of all the securities and then subtracting the market value of any stocks that are shorted.
