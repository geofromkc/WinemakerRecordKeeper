# WinemakerRecordKeeper
Support home winemaking activities, from crushing to bottling

## Description
Application to document the winemaking process, helping the winemaker understand how the final product came into being.   These are the primary features:
* Create batches, logging the source, the quantity and the cost
* Add fermentation activities, like Crush, Press, Rack, Ameliorate, etc
* Add test results

Supporting features:
* Resource codes: customizable collections of the names of things, like grapes, fermentation additives, yeasts, fermentation containers, etc
* Inventory: the physical instances of things, like additives, yeasts and containers

Other functions:
* Batch data, the resource codes and the inventory can all be exported to .CSV files for importing into spreadsheets
* The resource code and inventory data can also be imported in bulk to facilitate easier updates
* The batch and inventory data can be exported in a text-based report format
* All of the data can be manually backed up and restored, or moved to a different network location

## Getting Started

### Dependencies
This application was built with the following libraries
* OpenJDK 21.0.2
* Apache Derby 10.16.1.1
* Gluon JavaFX JMods 21.0.2
* Gluon JavaFX SDK 21.0.2
* jfoenix 9.0.10

The Derby libraries required:
* derbyclient.jar
* derbyshared.jar
* derbytools.jar

### Installing
The application was exported from Eclipse as a jar file.
The Windows install file was created with this jpackage file, included in this repository:

'''
#! /bin/bash
jpackage --type msi \
        --name WinemakerRecordKeeper \
        --description "Record Keeper for Wine and Mead Makers" \
        --vendor "Spring Hill Tools" \
        --app-version 1.2.1 \
        --input input \
        --dest output \
        --icon input/Folder-wine-icon.ico \
        --main-jar WineMaker-V1.2.1.jar \
        --main-class geo.apps.winemaker.WineMakerMain \
        --module-path c:/MiscSoftware/javafx-jmods-24.0.1 \
        --module-path c:/MiscSoftware/jfoenix-9.0.10.jar \
        --add-modules javafx.controls,javafx.fxml,java.sql,java.management,java.naming,com.jfoenix \
        --arguments -D \
        --win-dir-chooser \
        --win-shortcut \
        --win-menu \
        --verbose

'''

The output file will install the application as a standard Windows program.  During the initial install, the user will be prompted to download the install guide, if desired.


### Executing the program
The installation will add an icon to the Windows desktop:

![Desktop icon, showing the results of your labor](https://github.com/user-attachments/assets/80acdf5b-3cd5-470f-aec0-8d4b61f56fe8)


## Help
A Users Guide can be downloaded from the application.   

## Author
George Owen (geofromkc@gmail.com)
