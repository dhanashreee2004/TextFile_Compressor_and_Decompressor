# TextFile Compressor and Decompressor

A full-stack web application that provides **lossless text file compression and decompression** using classic algorithms — **Huffman Coding, Run-Length Encoding (RLE), and Lempel–Ziv–Welch (LZW)**.

This project combines a **Node.js + Express backend** with **Java-implemented algorithms** to deliver fast, accurate, and scalable compression for files ranging from a few kilobytes to several megabytes.

---

## Features

* **Multiple algorithms**: Huffman, RLE, LZW (choose at runtime).
* **High compression ratio**: Achieves **50–80% reduction** on text-based files.
* **Lossless recovery**: Ensures **100% accurate decompression**.
* **Scalable processing**: Handles files from a few KBs to several MBs in real time.
* **Automated reporting**: Generates metrics for file size before/after, compression ratio, and processing time.
* **Web-based UI**: Simple interface for uploading and downloading files.
* **Cross-language integration**: Node.js backend communicates with Java algorithms seamlessly.

---

## Tech Stack

* **Frontend**: HTML, CSS, JavaScript
* **Backend**: Node.js, Express.js
* **Algorithms**: Java (Huffman, RLE, LZW)
* **Integration**: Child process / API calls from Node.js to Java

---

## Project Structure

```
├── backend
│   ├── server.js          # Express server
│   ├── routes/            # API routes
│   └── utils/             # Node–Java integration scripts
├── algorithms
│   ├── Huffman.java
│   ├── RLE.java
│   └── LZW.java
├── frontend
│   ├── index.html
│   ├── styles.css
│   └── script.js
└── README.md
```

---

## Installation & Setup

### Prerequisites

* [Node.js](https://nodejs.org/) (v14+)
* [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) (v8+)

## Usage

1. Upload a text-based file through the web interface.
2. Select a compression algorithm (Huffman / RLE / LZW).
3. Download the compressed file + see metrics (size reduction, ratio, time).
4. Decompress to recover the original file (100% accurate).

---

## Example Results

| File Type       | Original Size | Compressed Size | Reduction |
| --------------- | ------------- | --------------- | --------- |
| `.txt` (1 MB)   | 1,024 KB      | 280 KB          | ~72%      |
| `.csv` (500 KB) | 500 KB        | 210 KB          | ~58%      |

---

## Future Enhancements

* Add support for **binary files** (images, PDFs).
* Deploy web app on **Heroku / Render / Vercel**.
* Implement **user accounts** and history of uploaded files.
* Add visualization of compression steps for learning purposes.

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss your ideas.

---

## Author

**Dhanashree**

Full-stack developer passionate about building efficient and scalable solutions.
