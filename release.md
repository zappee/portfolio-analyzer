# Release info
## Remal Portfolio Analyzer

All notable changes to this project will be documented in this file.

### [0.1.13] - 15 October 2022
#### Added
- Initial version based on the requirements

### [0.2.1] - xx November 2022
#### Modified
- Improvement in documentation
- Improve the title of the `portfolio` markdown report
- Remove useless separator from the footer of the `portfolio` markdown report
- Fix decimal number positioning problem in the `portfolio` Markdown report
- Fix average price calculation issue in case of a currency product
- Improvement in the application log
- Change the `--has-title` command line parameter to `--has-report-title`
- Change the `--has-header` command line parameter to `--has-table-header`
- Application exits if the price for products do not exist while downloading them from a market data provider
- Fixing a transaction parser issue: fee currency parsing error in case of no fee
- Fix fee calculation problem
- Update some command-line help
- Fix an issue related to te Yahoo API
- Fix timezone related issue
- Fix `portfolio` and `symbol` filter problems
- Fix an exchange rate issue when base currency and cash currency are equal
#### Added
- Support currency for fees
- Transfer assets between brokerage companies, new transaction types: `TRANSFER_IN` and `TRANSFER_OUT`
- The `*` symbol can be used to select the all portfolio in the `portfolio` command. The `--portfolio "*"` option has the same result as the parameter is not used. 
- Sorted `price-history` list by `symbol` and then `trade-date`
