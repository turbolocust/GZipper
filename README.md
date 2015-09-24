# gzipper

<b>Motivation:</b><br>

This JAVA program is mainly for practicing. It builds on a seminary work and as my main archiving program does not support GZIP archives I thought this would be a wonderful opportunity to enhance my skills.<br>
<br>
<b>How it works:</b><br>

Select files/folders to put them into an archive (tar.gz). The archive will be created in the same folder where the JAR-file is located. If there is already an archive with the default or same name, a second file (and so on...) be created. So the old archive will not be overriden. Extraction works the same, you select an archive and it will be extracted into an subfolder to the directory of the executable app. In the future I am planning to implement the option to select custom paths for zip/unzip destinations.<br>
<br>
<b>CHANGELOG</b><br>
<br>
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
