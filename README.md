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

<p align="center">
  <img src="docs/images/remal-portfolio-diversification.png" alt="portfolio administration" />
</p>

The `Remal Portfolio Analyzer` helps you to track the performance of your separated portfolios in one place with as less effort as possible.
The tool downloads your daily trading transactions from the brokerage companies, merges them into a big ledger, and generates a portfolio summary report based on the live market price as often as you wish.
The report can be any kind of diagram or a [Markdown][markdown] or an Excel file.

## 2) How the tool works
The `Remal Portfolio Analyzer` supports th following activities:
* Downloading `trading history` directly from a brokerage company.
* Trading history file transformation, i.e. converting timestamps between time zones, etc.
* Combine multiple `trading history` files into one.
* Showing the `trading history` files.
* Downloading the real time market price based on the provided `ticker/symbol` from market data-provider. 
* Generating the portfolio report.
* Showing the portfolio report on charts (column, line, pie, bar, etc.).
* Calculating a new portfolio repor, especially the average price, based on a simulated buy/selll transaction that you are planning to execute. 

The following flow shows how a general workflow looks like:

<p align="center">
  <img src="docs/images/remal-general-workflow.png" alt="general workflow" />
</p>


<a href="https://trackgit.com"><img src="https://us-central1-trackgit-analytics.cloudfunctions.net/token/ping/kzedlbkk4k0r4vk2iack" alt="trackgit-views" /></a>

[markdown]: https://www.markdownguide.org/basic-syntax "Markdown"
