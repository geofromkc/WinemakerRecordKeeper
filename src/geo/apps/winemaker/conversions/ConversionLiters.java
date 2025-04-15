package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionLiters implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {
		switch (targetType)
		{
		case VOLUMEGAL:
			return convertFromValue * 0.2642;
		case VOLUMEHL:
			return convertFromValue * 0.01;
		case VOLUMEML:
			return convertFromValue * 1000.0;
		default:
			return 0.0;	
		}
	}
}
