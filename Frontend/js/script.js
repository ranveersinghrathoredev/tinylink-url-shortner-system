
const shortenForm = document.getElementById("shortenForm");
const statsForm = document.getElementById("statsForm");

shortenForm.addEventListener("submit", function (event) {
    event.preventDefault();

    document.getElementById("shortenError").textContent = "";
    document.getElementById("rateLimitError").textContent = "";
    document.getElementById("shortenResult").replaceChildren();

    const originalUrl = document.getElementById("originalUrl").value;
    const customAlias = document.getElementById("customAlias").value;
    const expiresAt = document.getElementById("expiresAt").value;

    if (!originalUrl.trim()) {
        document.getElementById("shortenError").textContent =
            "Please enter a URL.";
        return;
    }

   try {
       const url = new URL(originalUrl);

       if (url.protocol !== "http:" && url.protocol !== "https:") {
           throw new Error("Invalid protocol");
       }
   } catch {
       document.getElementById("shortenError").textContent =
           "Please enter a valid HTTP or HTTPS URL.";
       return;
   }

    if (customAlias && !/^[a-zA-Z0-9_-]{3,30}$/.test(customAlias)) {
        document.getElementById("shortenError").textContent =
            "Alias can only contain letters, numbers, hyphens, and underscores.";
        return;
    }


    const requestData = {
        originalUrl: originalUrl,
        customAlias: customAlias,
        expiresAt: expiresAt,
    };

    const jsonData = JSON.stringify(requestData);

    fetch("http://localhost:8080/api/shorten", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: jsonData,
    }).then(function (response) {
        return response.json().catch(function () {
            return {};
        }).then(function (data) {

            if (response.status === 400) {

                if (typeof data.error === "string" && data.error.includes("Custom alias already exists")) {
                    document.getElementById("shortenError").textContent =
                        "Custom alias already exists. Please enter another alias.";

                    return;
                }
            }

            if (response.status === 429) {
                document.getElementById("rateLimitError").textContent =
                    "Too many requests. Please try again later.";

                return;
            }

            if (!response.ok) {
                document.getElementById("shortenError").textContent =
                    "Something went wrong. Please try again.";
                return;
            }


            const result = document.getElementById("shortenResult");
            result.replaceChildren();

            const fields = [
                ["Short URL:", data.shortUrl],
                ["Short Code:", data.shortCode],
                ["Created:", data.createdAt]
            ];

            fields.forEach(function ([label, value]) {
                const item = document.createElement("div");
                item.className = "result-item";

                const strong = document.createElement("strong");
                strong.textContent = label;

                const span = document.createElement("span");
                span.textContent = value ?? "";

                item.append(strong, span);
                result.appendChild(item);
            });

            document.getElementById("shortCode").value = data.shortCode ?? "";
        });
    }).catch(function (error) {
        
        document.getElementById("shortenError").textContent =
            "Unable to shorten URL. Please try again.";
    });
});

statsForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const shortCode = document.getElementById("shortCode").value;
    document.getElementById("statsResult").textContent = "";

      if (!shortCode.trim()) {
            document.getElementById("statsResult").textContent =
                "Please enter a short code.";
            return;
        }


    fetch(`http://localhost:8080/api/stats/${encodeURIComponent(shortCode)}`)
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Failed to fetch link statistics.");
            }

            return response.json();
        })
        .then(function (data) {


            const result = document.getElementById("statsResult");
            result.replaceChildren();

            const fields = [
                ["Original URL", data.originalUrl],
                ["Clicks", data.clickCount],
                ["Active", data.active],
                ["Created By", data.createdBy]
            ];

            fields.forEach(function ([label, value]) {
                const item = document.createElement("div");
                item.className = "stat-item";

                const span = document.createElement("span");
                span.textContent = label;

                const strong = document.createElement("strong");
                strong.textContent = value ?? "";

                item.append(span, strong);
                result.appendChild(item);
            });
        })
        .catch(function (error) {
            console.error("Stats error:", error);

            const result = document.getElementById("statsResult");
            result.textContent = "Unable to load statistics. Please try again.";
        });
});
