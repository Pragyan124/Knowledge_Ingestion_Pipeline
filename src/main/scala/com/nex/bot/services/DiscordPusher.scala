package com.nex.bot.services

import com.nex.bot.domain.Paper
import com.nex.bot.config.Config
import scala.concurrent.{Future, ExecutionContext, blocking}
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpRequest.BodyPublishers
import java.time.Duration

object DiscordPusher {
  private val client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build()

  def push(paper: Paper, summary: String)(implicit ec: ExecutionContext): Future[Unit] = Future {
    
    // 1. Clean up Arxiv data (Arxiv titles often have hidden newlines that break things)
    val cleanTitle = paper.title.replace("\n", " ").replaceAll("\\s+", " ").trim
    val cleanAuthors = paper.authors.replace("\n", " ").trim

    // 2. Build the message using | for precise margins
    // The .stripMargin command ensures the lines stay exactly where they are
    val rawMessage =
      s"""**Today's Paper:** $cleanTitle
         |**Authors:** $cleanAuthors
         |**Venue:** [Arxiv Link](${paper.link})
         |
         |$summary
         |""".stripMargin

    // 3. Escape for JSON
    val safeJsonContent = escapeJson(rawMessage)
    val jsonPayload = s"""{"content": "$safeJsonContent"}"""

    // 4. Send
    val request = HttpRequest.newBuilder()
      .uri(URI.create(Config.DiscordWebhookUrl))
      .header("Content-Type", "application/json")
      .POST(BodyPublishers.ofString(jsonPayload))
      .build()

    println(s"DEBUG: Sending to Discord...")
    blocking {
      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        println("✅ Successfully pushed to Discord!")
      } else {
        println(s"❌ Discord Error (${response.statusCode()}): ${response.body()}")
      }
    }
  }

  private def escapeJson(text: String): String = {
    text
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "")
      .replace("\t", " ")
  }
}