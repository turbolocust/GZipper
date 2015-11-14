# GZipper

<b>Motivation:</b><br>

This JAVA program is mainly for practicing. It builds on a seminary work and as my main archiving program does not support creating GZIP/TAR archives, I thought this would be a great opportunity to enhance my skills.<br>
<br>
<b>How it works:</b><br>

Select files/folders to put them into an archive (tar.gz). The archive will be created in the same folder where the JAR-file is located. If there is already an archive with the default or same name, a second file (and so on...) will be created. So any old archive will not be overriden. Extraction works the same, you select an archive and it will be extracted into a subfolder in the directory of the executable app. Custom paths for zip/unzip locations are currently not supported.<br>
<br>
<b>This application requires Java 1.8</b><br>
<br>
<b>CHANGELOG</b><br>
<br>
<b>v0.6.5</b>
<ul>
<li>added logging capability</li>
<li>new options menu to enable/disable logging</li>
</ul>
<b>v0.6</b>
<ul>
<li>added icon image</li>
<li>updated "About" frame</li>
</ul>
<b>v0.5</b>
<ul>
<li>upgraded to commons-compress 1.10<br></li>
<li>added support for compressing sub-directories<br></li>
<li>built GUI for application with advanced functionality<br></li>
<li>added threading capability for GUI responsiveness<br></li>
<li>added Linux compatibility<br></li>
</ul>
