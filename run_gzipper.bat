@echo off
start /min PowerShell.exe -WindowStyle Hidden "java --module-path ".\lib" --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,org.apache.commons.compress,org.tukaani.xz,kotlin.stdlib -jar .\GZipper.jar"
