package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionMilliLiters implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {
		switch (targetType)
		{
		case VOLUMEGAL:
			return convertFromValue * 0.00026;
		case VOLUMEHL:
			return convertFromValue * 1.00E-05;
		case VOLUMEL:
			return convertFromValue * 0.001;
		default:
			return 0.0;	
		}
	}
}
