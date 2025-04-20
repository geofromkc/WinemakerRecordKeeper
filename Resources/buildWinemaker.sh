#! /bin/bash

jpackage --type msi \
	--name WinemakerRecordKeeper \
	--description "Record Keeper for Wine and Mead Makers" \
	--vendor "Spring Hill Tools" \
	--app-version 3.2.0 \
	--input input \
	--dest output \
	--icon input/Folder-wine-icon.ico \
	--main-jar WineMaker-V3.2.0.jar \
	--main-class geo.apps.winemaker.WineMakerMain \
	--module-path c:/MiscSoftware/javafx-jmods-21.0.2 \
	--add-modules javafx.controls,javafx.fxml,java.sql,java.management,java.naming \
	--arguments -D \
	--win-dir-chooser \
	--win-shortcut \
	--win-menu \
	--verbose
