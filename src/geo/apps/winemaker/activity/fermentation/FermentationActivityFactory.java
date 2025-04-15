package geo.apps.winemaker.activity.fermentation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import geo.apps.winemaker.utilities.Constants.ActivityName;

/**
 * Provide a structure for displaying data of the various ferment activities.
 * 
 * @author geo
 *
 */
public class FermentationActivityFactory {

	static Map<String, FermentationActivity> activityMap = new HashMap<>();
	
	/*
	 * For new activity displays, create a new implementation class and adjust the activityMap accordingly
	 */
	static 
	{
		/*
		 * Implemented activities
		 */
		activityMap.put(ActivityName.BOTTLE.getValue(), new FermentActivityBottle());
		activityMap.put(ActivityName.CRUSH.getValue(), new FermentActivityCrush());
		activityMap.put(ActivityName.CHECKPOINT.getValue(), new FermentActivityCheckpoint());
		activityMap.put(ActivityName.FERMENT.getValue(), new FermentActivityFerment());
		activityMap.put(ActivityName.RACK.getValue(), new FermentActivityRack());
		activityMap.put(ActivityName.PRESS.getValue(), new FermentActivityPress());
		activityMap.put(ActivityName.YEASTPITCH.getValue(), new FermentActivityYeastPitch());
		activityMap.put(ActivityName.AMELIORATION.getValue(), new FermentActivityAmeliorate());
		
		/*
		 * Not-Implemented activities
		 */
		activityMap.put("drain", new FermentActivityDefault());
		activityMap.put("settle", new FermentActivityDefault());
		activityMap.put("onskins", new FermentActivityDefault());
		activityMap.put("leefilter", new FermentActivityDefault());
		activityMap.put("heatxfer", new FermentActivityDefault());
		activityMap.put("punch", new FermentActivityDefault());
		activityMap.put("modify", new FermentActivityDefault());
		activityMap.put("coldstor", new FermentActivityDefault());
	}
	
	public static Optional<FermentationActivity> getActivity(String activity)
	{
		return Optional.ofNullable(activityMap.get(activity));
	}
}
