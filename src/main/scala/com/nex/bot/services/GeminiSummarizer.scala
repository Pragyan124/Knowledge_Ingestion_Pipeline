package com.nex.bot.services

import com.nex.bot.domain.Paper
import com.nex.bot.config.Config
import scala.concurrent.{Future, ExecutionContext, blocking}
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpRequest.BodyPublishers
import java.time.Duration

object GeminiSummarizer {
  
  // Use a longer timeout (30s) as AI sometimes takes time to "think"
  private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build()

  def summarize(paper: Paper)(implicit ec: ExecutionContext): Future[String] = Future {
    println(s"🧠 Asking Gemini to analyze: ${paper.title}...")

    // 1. The Prompt
    val promptText = s"""
      You are a senior Principal Engineer writing a daily tech newsletter. 
      Analyze this research paper: ${paper.title}
      Abstract: ${paper.abstractText}
      
      OUTPUT INSTRUCTIONS:
      1. Write a section titled "**The 'Flex' (Why it matters)**": Explain the core innovation.
      2. Write a section titled "**Engineering Takeaways**": Bullet points on real-world impact.
      3. Keep it concise (under 200 words).
      4. DO NOT use markdown code blocks (like ```json). Just plain text.
    """.trim

    val safePrompt = escapeJson(promptText)
    val jsonPayload = s"""{"contents": [{"parts": [{"text": "$safePrompt"}]}]}"""

    // 2. The URL (Using your working model)
    val url = s"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${Config.GeminiApiKey}"
    
    val request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .header("Content-Type", "application/json")
      .POST(BodyPublishers.ofString(jsonPayload))
      .build()

    blocking {
      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      
      if (response.statusCode() == 200) {
        extractTextFromJson(response.body())
      } else {
        println(s"DEBUG: AI Error Body: ${response.body()}")
        s"⚠️ AI Error (${response.statusCode()}): Check terminal."
      }
    }
  }

  // --- IMPROVED PARSER ---
  // This manually reads the string character-by-character to ensure we don't 
  // stop early just because the AI used a quote inside the text.
  private def extractTextFromJson(json: String): String = {
    val searchStr = "\"text\": \""
    val startIndex = json.indexOf(searchStr)
    
    if (startIndex == -1) return "Error: No text returned by AI."

    val contentStart = startIndex + searchStr.length
    val sb = new StringBuilder()
    var i = contentStart
    var inside = true
    
    // Loop until we find the CLOSING quote that isn't escaped
    while (i < json.length && inside) {
      val c = json.charAt(i)
      
      if (c == '\\') {
        // Handle escaped characters (like \" or \n)
        if (i + 1 < json.length) {
          val nextC = json.charAt(i + 1)
          nextC match {
            case '"' => sb.append('"')   // Convert \" back to "
            case 'n' => sb.append('\n')  // Convert \n back to actual newline
            case 't' => sb.append('\t')
            case _   => sb.append(nextC) // Keep other escapesl
          }
          i += 2 // Skip both \ and the character
        } else {
          inside = false
        }
      } else if (c == '"') {
        // This is the REAL end of the text field
        inside = false
      } else {
        sb.append(c)
        i += 1
      }
    }
    sb.toString()
  }

  private def escapeJson(text: String): String = {
    text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
  }
}