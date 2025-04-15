package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionHectoLiters implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {
		switch (targetType)
		{
		case VOLUMEGAL:
			return convertFromValue * 26.417;
		case VOLUMEL:
			return convertFromValue * 100.0;
		case VOLUMEML:
			return convertFromValue * 100000.0;
		default:
			return 0.0;	
		}
	}
}
