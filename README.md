> Project Title: Tinylink-URL Shortner System

> Project Overview: The Tinylink-URL-Shortner-System is an application where users enter a Original long URL and then the system generates a short link, it stores the mapping and redirects users to the original url whenever the short link is open. The system also support custom aliases, link expiration, click tracking, redis caching, and rate limiting.

> Project Structure:

 tinylink/
 frontend
     index.html
     css/style.css
     js/script.js

    
  tinylink/
  backend/
     src/
         main/
             java/
                com/
                github/
                ranveer/
                tinylink/
                    config/
                      CleanupScheduler
                      RedisConfig
                      WebConfig
                    controllers/
                      UrlShortnerController
                    dto/
                      ShortenUrlRequest
                      ShortenUrlResponse
                      UrlAnalyticsResponse
                      UrlStatsResponse
                    models/
                      ClickEvent
                      RateLimitData
                      UrlData
                    services/
                      RateLimitService
                      UrlShortnerService
                    validator/
                      UrlValidator
                      ValidUrl
                    TinyLinkApplication
                    resources/
                      application.yaml




> Project Architecture and Data Flow:

[View Architecture and Data Flow](https://github.com/ranveersinghrathoredev/tinylink-url-shortner-system/blob/master/Backend/src/main/java/com/github/ranveer/tinylink/image/img.png)


> Real World Examples: Bitly and Twitter







 





















