# WinemakerRecordKeeper
Support home winemaking activities, from crushing to bottling

## Description
Home winemaking is relatively easy to do, but difficult to master.  There are many places between grape and bottle where the winemaker can steer the process towards something magical, or run it right off the road over a cliff.  An important part of the process is knowing how any particular path was taken, which means having documentation that describes every action or intervention by the winemaker, and the state of the wine at every stage of the journey to the bottle.

This application attempts to be that documentation keeper, to help the winemaker understand how the final product came into being.   These are the primary features of the application:
* Create batches, logging the source, the quantity and the cost
* Add fermentation activities, like Crush, Press, Rack, Ameliorate
* Add test results
* Delete any of the above, either individually or everything at one time

Supporting features:
* Resource codes: customizable collections of the names of things, like grapes, fermentation additives, yeasts, fermentation containers
* Inventory: the physical instances of things, like additives, yeasts and containers
  * The batch summary will show the additives and yeasts that have been used in the batch, and which specific containers are currently used by a batch

Other functions:
* Batch data, the resource codes and the inventory can all be exported to .CSV files for importing into spreadsheets
* The resource code and inventory data can also be imported in bulk to facilitate easier updates
* The inventory data can be exported in a brief text report format, showing the containers' batch assignments and current stock on hand for consumable things like yeast and additives
* All of the data can be manually backed up and restored, or it can be moved to a different network location

## Getting Started

### Dependencies
This application was built with the following libraries
* OpenJDK 21.0.2
* Apache Derby 10.16.1.1
* Gluon JavaFX JMods 21.0.2
* Gluon JavaFX SDK 21.0.2

The Derby libraries required:
* derbyclient.jar
* derbyshared.jar
* derbytools.jar

### Installing
The application was exported from Eclipse as a jar file, extracting the required libraries into the jar.
A Windows install file was created with this jpackage file, included in this repository:

'''
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
'''

The output file will install the application as a standard Windows program.

### Executing the program
The installation will add an icon to the Windows desktop:
![Folder-wine-icon](https://github.com/user-attachments/assets/80acdf5b-3cd5-470f-aec0-8d4b61f56fe8)

## Help
A Users Guide is being developed.  Otherwise, the application is provided as-is.   

## Author
George Owen (geofromkc@gmail.com)
