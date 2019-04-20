# GZipper

<a href="https://travis-ci.org/turbolocust/GZipper"><img href src="https://travis-ci.org/turbolocust/GZipper.svg?branch=master" alt="CI build passed"/></a>

<b>Features:</b><br />

* Create and extract the following archive types:
  - ZIP
  - JAR
  - Tarball (GZIP)
  - Tarball (BZIP2)
  - Tarball (LZMA)
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
<img src="https://homepages.fhv.at/mfu7609/images/gzipper_gui_FX_mainView.PNG" alt="main view with enabled default theme"/><br />
<br /><b>Hash view with enabled default theme:</b><br />
<img src="https://homepages.fhv.at/mfu7609/images/gzipper_gui_FX_hashView.PNG" alt="hash view with enabled default theme"/><br />
<br /><b>Main view with enabled dark theme:</b><br />
<img src="https://homepages.fhv.at/mfu7609/images/gzipper_gui_FX_mainView_dark.PNG" alt="main view with enabled dark theme"/><br />
<br /><b>Hash view with enabled dark theme:</b><br />
<img src="https://homepages.fhv.at/mfu7609/images/gzipper_gui_FX_hashView_dark.PNG" alt="hash view with enabled dark theme"/>
