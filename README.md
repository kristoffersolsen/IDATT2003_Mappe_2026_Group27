# Millions — Stock Trading Game
 
A turn-based stock trading game built with JavaFX. You start with a cash
balance of your choosing, load a stock exchange from a CSV file, and trade
over a series of weeks. Each week stock prices shift randomly by up to ±10%.
The game ends when you choose to cash out, and your final net worth is compared
against your starting capital to determine your investor status.
 
---
 
## Requirements
 
- Java 25
- Maven 3.9+
 
---
 
## Getting started
 
**1. Clone the repository**
 
```bash
git clone <repo-url>
```
 
**2. Run the application**
 
```bash
mvn javafx:run
```
 
The start screen will appear. From there:
 
1. Enter your player name.
2. Enter your starting capital (a positive number).
3. Click **Browse** and select a stock data CSV file.
4. Click **Start** to begin.
A sample file is provided at `src/main/resources/data/stocks/stocks.csv`.
 
---
 
## Stock data file format
 
The game loads stock data from a CSV file. The file has an optional metadata
section followed by a header row and one stock per line.
 
```
"metadata","description","Week 1 — Opening prices"
"metadata","week","1"
"Symbol","Name","Price"
"AAPL","Apple Inc.","189.50"
"MSFT","Microsoft Corporation","415.20"
```
 
| Field | Description |
|---|---|
| `metadata,description,...` | Optional. Human-readable label shown on the start screen. |
| `metadata,week,...` | Optional. The week number to resume from. Omit for a new game starting at week 1. |
| `Symbol` | Short ticker symbol, e.g. `AAPL`. |
| `Name` | Full company name. |
| `Price` | Starting price as a decimal, e.g. `189.50`. |
 
The game writes updated prices back to the same file when you advance weeks,
so the file doubles as a save state.
 
---
 
## How to play
 
- **Advance week** — simulates one week of trading. All stock prices shift
  randomly by up to ±10%.
- **Buy** — select a stock from the market list, set a quantity, and confirm
  the purchase. A commission is deducted automatically.
- **Sell** — open your portfolio, select a position, and choose how many
  shares to sell. Tax is applied to any realized gain.
- **End game** — ends your session and shows a summary of your starting
  capital, final net worth, and investor status.
### Investor status tiers
 
| Status | Requirement |
|---|---|
| Novice | Starting tier — no requirements. |
| Investor | At least 10 weeks played and 20% net worth growth. |
| Speculator | At least 20 weeks played and 100% net worth growth (doubled capital). |
 
---
 
## Running the tests
 
```bash
mvn test
```
 
To run the full build including Checkstyle enforcement and JaCoCo coverage:
 
```bash
mvn verify
```
 
Reports are written to:
 
| Report | Location |
|---|---|
| Test results | `target/surefire-reports/` |
| Coverage (HTML) | `target/site/jacoco/index.html` |
| Checkstyle (HTML) | `target/site/checkstyle.html` |
 
Coverage and Checkstyle reports require `mvn verify` or `mvn site`, not just
`mvn test`.
 
---
 
## CI
 
The repository runs a GitHub Actions workflow on every push to `main` and
every pull request targeting `main`. The workflow enforces:
 
- Checkstyle (Google Java Style)
- All unit tests passing
- JaCoCo coverage threshold
Artifacts (test results, coverage report, Checkstyle report) are uploaded for
each run and available from the Actions tab for 90 days.
 
Pull requests to `main` require the workflow to pass before merging.
 
