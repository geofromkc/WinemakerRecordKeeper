package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionPounds implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {

		switch (targetType)
		{
		case MASSKG:
			return convertFromValue * 0.4536;
		case MASSMG:
			return convertFromValue * 453592.0;
		case MASSG:
			return convertFromValue * 453.59;
		case MASSOZ:
			return convertFromValue * 16;			
		default:
			return 0.0;	
		}
	}
}
