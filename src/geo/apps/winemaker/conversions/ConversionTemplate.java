package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.WineMakerLogging;
import geo.apps.winemaker.utilities.Constants.RegistryKeys;
import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public interface ConversionTemplate {
	
	WineMakerLogging winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);
	
	Double apply(MassAndVolume targetType, Double convertFromValue);

}
