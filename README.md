# Knowledge Ingestion Pipeline

> An automated serverless pipeline that fetches the latest CS research papers, uses Google Gemini 2.5 AI to summarize them into "Engineering Takeaways," and pushes the insights to Discord daily.

![Scala](https://img.shields.io/badge/scala-%23DC322F.svg?style=for-the-badge&logo=scala&logoColor=white)
![Google Gemini](https://img.shields.io/badge/Google%20Gemini-8E75B2?style=for-the-badge&logo=google&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white)

## Architecture

1.  **Trigger:** GitHub Actions wakes up daily at 2:00 PM IST (Cron Job).
2.  **Fetch:** The bot queries the Arxiv API for the latest papers in Database Systems (cs.DB).
3.  **Analyze:** It sends the abstract to Google Gemini 2.5 Flash, asking for a "Flex" (Why it matters) and "Engineering Takeaways."
4.  **Deliver:** The structured summary is pushed to a private Discord server via Webhook.

## How It Works
I built this project to automate my learning process. Instead of manually browsing Arxiv, this bot acts as a filter, delivering only high-signal engineering insights directly to my phone.

### The "Why" (Motivation)
Built entirely in Scala 3 to challenge myself with functional programming, Futures, and strict typing. It leverages:
* sbt for build management.
* Java HTTP Client for lightweight networking (no heavy frameworks).
* GitHub Actions for serverless, zero-cost deployment.

## Setup & Installation

### Prerequisites
* Java 21 (Temurin recommended)
* sbt 1.x
* A Google Cloud API Key (Gemini)
* A Discord Webhook URL

### Local Run
1.  Clone the repo:
    ```bash
    git clone [https://github.com/YOUR_USERNAME/knowledge-pipeline.git](https://github.com/YOUR_USERNAME/knowledge-pipeline.git)
    cd knowledge-pipeline
    ```
2.  Set environment variables (or add to Config.scala for local testing):
    ```bash
    export GEMINI_API_KEY="your_key_here"
    export DISCORD_WEBHOOK_URL="your_webhook_here"
    ```
3.  Run the pipeline:
    ```bash
    sbt run
    ```

## Automation (GitHub Actions)
The project includes a .github/workflows/daily_schedule.yml file that automates the run.
* **Schedule:** Runs automatically at 08:30 UTC (2:00 PM IST).
* **Secrets:** Requires GEMINI_API_KEY and DISCORD_WEBHOOK_URL in Repository Secrets.

## Tech Stack
* **Language:** Scala 3.3.4
* **AI Model:** Google Gemini 2.5 Flash
* **Data Source:** Arxiv API
* **Infrastructure:** GitHub Actions (Ubuntu Runner)

---
*Built to get comfortable with the uncomfortable.*
