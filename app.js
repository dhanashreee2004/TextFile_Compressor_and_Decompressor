const express = require("express");
const multer = require("multer");
const fs = require("fs");
const { spawn } = require("child_process");
const path = require("path");

const app = express();
const upload = multer({ dest: "uploads/" });

// Ensure downloads folder exists
const downloadsDir = path.join(__dirname, "downloads");
if (!fs.existsSync(downloadsDir)) fs.mkdirSync(downloadsDir);

app.use("/downloads", express.static(downloadsDir));
app.use(express.static(path.join(__dirname, "public"))); // serve index.html & assets

// Home route
app.get("/", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "index.html"));
});

// ---- COMPRESS ROUTE ----
app.post("/compress/:level", upload.single("file"), (req, res) => {
  if (!req.file) return res.status(400).json({ success: false, error: "No file uploaded" });

  const inputPath = req.file.path;
  const level = req.params.level;

  const uniqueName = `compressed_${Date.now()}.huff`;
  const outputPath = path.join(downloadsDir, uniqueName);

  const originalSize = fs.statSync(inputPath).size;

  const javaProcess = spawn("java", [
    "-jar",
    "HuffmanCompressor.jar",
    "compress",
    inputPath,
    outputPath,
    level
  ]);

  javaProcess.on("close", (code) => {
    fs.unlinkSync(inputPath); // cleanup uploaded file
    if (code === 0) {
      const processedSize = fs.statSync(outputPath).size;
      const ratio = ((1 - processedSize / originalSize) * 100).toFixed(2);

      res.json({
        success: true,
        filename: uniqueName,
        originalSize,
        processedSize,
        ratio
      });
    } else {
      res.status(500).json({ success: false, error: "Compression failed" });
    }
  });
});
// ----------DECOMPRESS ROUTE ------------
app.post("/decompress", upload.single("file"), (req, res) => {
  if (!req.file) return res.status(400).json({ success: false, error: "No file uploaded" });

  const inputPath = req.file.path;
  const uniqueName = `decompressed_${Date.now()}.txt`;
  const outputPath = path.join(downloadsDir, uniqueName);

  console.log("Decompress input:", inputPath);
  console.log("Decompress output:", outputPath);

  const javaProcess = spawn("java", [
    "-jar",
    "HuffmanCompressor.jar",
    "decompress",
    inputPath,
    outputPath
  ]);

  let javaError = "";

  javaProcess.stderr.on("data", (data) => {
    javaError += data.toString();
  });

  javaProcess.on("close", (code) => {
    console.log("Java exit code:", code);
    if (fs.existsSync(inputPath)) fs.unlinkSync(inputPath);

    if (code === 0) {
      try {
        const processedSize = fs.statSync(outputPath).size;
        console.log("Decompressed size:", processedSize);
        res.json({
          success: true,
          filename: uniqueName,
        });
      } catch (err) {
        console.error("Error reading output file:", err);
        res.status(500).json({ success: false, error: "Failed to read decompressed file." });
      }
    } else {
      console.error("Java error output:", javaError);
      res.status(500).json({ success: false, error: "Decompression failed." });
    }
  });
});

app.listen(3000, () => console.log("Server running at http://localhost:3000"));
