package com.nex.bot.fetch

import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import java.net.{URL, HttpURLConnection}
import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.CollectionConverters._
import com.nex.bot.domain.Paper
import com.nex.bot.config.Config

object ArxivFetcher {
  def getLatestPaper()(implicit ec: ExecutionContext): Future[Option[Paper]] = Future {
    
    // 1. Force HTTPS to avoid redirects dropping headers
    val originalUrl = Config.ArxivFeed
    val secureUrl = if (originalUrl.startsWith("http:")) originalUrl.replace("http:", "https:") else originalUrl
    
    println(s"DEBUG: Fetching from $secureUrl") // Debug log

    val url = new URL(secureUrl)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    
    // 2. Set strict headers to look like a browser
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    
    // 3. Connect and check status
    connection.connect()
    val status = connection.getResponseCode
    println(s"DEBUG: Response Code = $status") // Debug log

    if (status != 200) {
      println(s"ERROR: Failed to fetch. Server message: ${connection.getResponseMessage}")
      None
    } else {
      // 4. Parse only if successful
      val stream = connection.getInputStream
      val feed = new SyndFeedInput().build(new XmlReader(stream))

      feed.getEntries.asScala.headOption.map { entry =>
        Paper(
          title = entry.getTitle,
          abstractText = Option(entry.getDescription).map(_.getValue).getOrElse(""),
          link = entry.getLink,
          authors = entry.getAuthors.asScala.map(_.getName).mkString(", ")
        )
      }
    }
  }
}