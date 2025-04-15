package geo.apps.winemaker.conversions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionFactory {

	static Map<String, ConversionTemplate> conversionMap = new HashMap<>();
	
	static {
		conversionMap.put(MassAndVolume.MASSKG.getValue(), new ConversionKiloGrams());
		conversionMap.put(MassAndVolume.MASSG.getValue(), new ConversionGrams());
		conversionMap.put(MassAndVolume.MASSMG.getValue(), new ConversionMilliGrams());
		conversionMap.put(MassAndVolume.MASSLBS.getValue(), new ConversionPounds());
		conversionMap.put(MassAndVolume.MASSOZ.getValue(), new ConversionOunces());
	}

	public static Optional<ConversionTemplate> getConverter(String conversionType)
	{
		return Optional.ofNullable(conversionMap.get(conversionType));
	}
}
