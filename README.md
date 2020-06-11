<p align="center">
  <img alt="gzipper logo" src="./src/main/resources/images/icon_256.png" width="100px" />
  <h1 align="center">GZipper</h1>
</p>

![Travis CI](https://travis-ci.org/turbolocust/GZipper.svg?branch=master)
![Java CI](https://github.com/turbolocust/GZipper/workflows/Java%20CI/badge.svg?branch=master)

<b>Features:</b><br />

* Create and extract the following archive types:
  - ZIP
  - JAR
  - Tarball (GZIP)
  - Tarball (BZIP2)
  - Tarball (LZMA)
  - Tarball (XZ)
* Compress and decompress GZIP
* Compression levels can be adjusted
  - if supported by compressor
* Support for regular expressions
  - allows filtering of files/entries
* Supports a dark theme (CSS style)
* Message Digest algorithms:
  - MD5
  - SHA-1
  - SHA-256
  - SHA-384
  - SHA-512
* Languages which are supported:
  - English
  - German
  
<b>The application has been tested on these platforms:</b>
 * Windows
 * Unix-based systems
   - make sure to install the JavaFX components
   - `sudo apt-get install openjfx`
   
<br /><br />
For compression and decompression this application uses parts of the commons-compress library by Apache Foundation as well as 'XZ for Java' by Tukaani. Their source code and documentation can be found here: 
  - <a href>http://commons.apache.org/</a>
  - <a href>https://tukaani.org/</a>
  
<br />
<b>This application is built for Java 11 but is compatible with Java 8 as well. To build this application with Java 8, module-specific files have to be removed and the Maven configuration (pom.xml) needs to be updated.</b>

# Screenshots

<b>Main view with enabled default theme:</b><br />
![Main view with enabled default theme.](./images/gzipper_gui_FX_mainView.PNG)

<br /><b>Hash view with enabled default theme:</b><br />
![Hash view with enabled default theme.](./images/gzipper_gui_FX_hashView.PNG)

<br /><b>Main view with enabled dark theme:</b><br />
![Main view with enabled dark theme.](./images/gzipper_gui_FX_mainView_dark.PNG)

<br /><b>Hash view with enabled dark theme:</b><br />
![Hash view with enabled dark theme.](./images/gzipper_gui_FX_hashView_dark.PNG)
