> Project Title: Tinylink-URL Shortner System

> Project Overview: The Tinylink-URL-Shortner-System is an application where users enter a Original long URL and then the system generates a short link, it stores the mapping and redirects users to the original url whenever the short link is open. The system also support custom aliases, link expiration, click tracking, redis caching, and rate limiting.

> Project Structure:

 [View Architecture Diagram 2](https://github.com/ranveersinghrathoredev/tinylink-url-shortner-system/blob/master/Backend/src/main/java/com/github/ranveer/tinylink/image/img_1.png)


> Project Architecture and Data Flow:

[View Architecture and Data Flow](https://github.com/ranveersinghrathoredev/tinylink-url-shortner-system/blob/master/Backend/src/main/java/com/github/ranveer/tinylink/image/img.png)


> Real World Examples: Bitly and Twitter

> Project Trade-offs

1. In-Memory Storage vs. Database
   Used `ConcurrentHashMap` to store shortened URLs in server memory instead of using a database.
   Memory is faster and avoids the extra setup of a database.
   Trade-off: URL lookups are extremely fast (under 1 ms), but all stored URLs are lost when the server restarts.
              A database like MySQL would be needed for permanent storage.

2. Dual-Layer Rate Limiting
   Used both local memory and Redis to track user request limits.
   If Redis becomes unavailable, the application can still enforce rate limits using local memory.
   Trade-off: The application remains available, but small delays in syncing both layers may temporarily allow extra requests.

3. Redis vs. Local Map for Caching
   Used Redis instead of relying only on local Java maps.
   Local maps work on a single server, while Redis shares cached data across multiple servers. It also automatically removes expired links
   using TTL and   reduces memory usage on application servers.
   Trade-off: Makes the application easier to scale but adds another service that needs to be configured and maintained.





 





















