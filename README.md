ZUtils
======
Version 0.2.3

Some android utils

Includes
--------
* ImageDownloader
* Simple Http
* Continuingly downloader with single thread
* Download manager, which to manage downloader above
* A canceler
* some android views
* other utils


Android Permission required
-------------------
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>


Log
---

#### v0.2.3
* Transplant a powerful image downloader from bitmapfun of google,
  Use it by ImageDownloaderEx which like the old ImageDownload 

#### v0.2.2
* Optimizes the download manager.(SQLite)
* Can limit the numbers of download task in the same time.
* Add some new download manager callbacks, (onAdd, onWait)

#### v0.2.1

* add GZIP supports for simple http
* add local cache for ImageDownloder, open memory cache auto clear.
* change cached image quality
* add phone state unity
* add a always marquee TextView
* add Textview foreground color tool, Foreground. It can change TextView text spancolor with regex