package com.nex.bot.config

object Config {
  val ArxivFeed: String =
    sys.env.getOrElse(
      "ARXIV_FEED",
      "http://export.arxiv.org/api/query?search_query=cat:cs.DB&start=0&max_results=1&sortBy=submittedDate&sortOrder=descending"
    )

  
  val DiscordWebhookUrl =  sys.env.getOrElse("DISCORD_WEBHOOK_URL", "YOUR_LOCAL_TEST_KEY")
  val GeminiApiKey = sys.env.getOrElse("GEMINI_API_KEY", "YOUR_LOCAL_TEST_KEY")
  
}
