package com.nex.bot

import fetch.ArxivFetcher
import services.{GeminiSummarizer, DiscordPusher}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends App {

  println("Starting Knowledge Ingestion Pipeline...")

  // Define the entire job as a single Future chain
  val pipelineJob: Future[Unit] = ArxivFetcher.getLatestPaper().flatMap {
    case Some(paper) =>
      println(s"Found paper: ${paper.title}")
      println("Sending to Gemini for analysis...")
      
      GeminiSummarizer.summarize(paper).flatMap { summary =>
        println("Summary received. Pushing to Discord...")
        DiscordPusher.push(paper, summary)
      }

    case None =>
      println("No relevant paper found in the feed today.")
      Future.successful(())
  }

  // Block the main thread until the Future completes
  // We give it a generous timeout (2 minutes) to account for network/AI latency
  try {
    Await.result(pipelineJob, 2.minutes)
    println("Pipeline finished successfully.")
  } catch {
    case e: Exception =>
      println(s"Pipeline failed: ${e.getMessage}")
      e.printStackTrace()
      sys.exit(1) // Exit with error code so GitHub Actions knows it failed
  }
}