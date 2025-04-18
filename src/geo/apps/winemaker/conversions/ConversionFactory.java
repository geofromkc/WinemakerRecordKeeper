package geo.apps.winemaker.conversions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

/**
 * Returns class instance of requested conversion 
 */
public class ConversionFactory {

	static Map<String, ConversionTemplate> conversionMap = new HashMap<>();
	
	static {
		conversionMap.put(MassAndVolume.MASSKG.getValue(), new ConversionKiloGrams());
		conversionMap.put(MassAndVolume.MASSG.getValue(), new ConversionGrams());
		conversionMap.put(MassAndVolume.MASSMG.getValue(), new ConversionMilliGrams());
		conversionMap.put(MassAndVolume.MASSLBS.getValue(), new ConversionPounds());
		conversionMap.put(MassAndVolume.MASSOZ.getValue(), new ConversionOunces());
		conversionMap.put(MassAndVolume.VOLUMEHL.getValue(), new ConversionHectoLiters());
		conversionMap.put(MassAndVolume.VOLUMEGAL.getValue(), new ConversionGallons());
		conversionMap.put(MassAndVolume.VOLUMEL.getValue(), new ConversionLiters());
		conversionMap.put(MassAndVolume.VOLUMEML.getValue(), new ConversionMilliLiters());
	}

	public static Optional<ConversionTemplate> getConverter(String conversionType)
	{
		return Optional.ofNullable(conversionMap.get(conversionType));
	}
}
