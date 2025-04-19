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



