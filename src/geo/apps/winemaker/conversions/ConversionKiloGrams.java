package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionKiloGrams implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {

		switch (targetType)
		{
		case MASSG:
			return convertFromValue * 1000;
		case MASSMG:
			return convertFromValue * 1000000;
		case MASSLBS:
			return convertFromValue * 2.2046;
		case MASSOZ:
			return convertFromValue * 35.274;			
		default:
			return 0.0;
		}
	}	
}
