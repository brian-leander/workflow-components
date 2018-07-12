package dk.dmi.lib.workflow.component.polygon;

import java.io.IOException;

import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.persistence.database.climadb.polygon.controller.GridFileCalculationController;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFile;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.GridFileCalculation;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;

import dk.dmi.lib.common.DateUtils;
import dk.dmi.lib.common.FileUtils;
import dk.dmi.lib.persistence.database.climadb.polygon.entity.Parameter;
import dk.dmi.lib.persistence.database.climadb.publicc.controller.BasisDailyDkController;
import dk.dmi.lib.polygon.mapproduction.drawing.ColorDefinitions;
import dk.dmi.lib.polygon.mapproduction.imagegenerator.GenerateGridImage;
import dk.dmi.lib.polygon.mapproduction.imagegenerator.GenerateKlimaInfoImage;
import dk.dmi.lib.polygon.mapproduction.imagegenerator.LegendSelector;
import dk.dmi.lib.polygon.mapproduction.location.GridPoint;
import dk.dmi.lib.polygon.mapproduction.location.GridPointSimple;
import dk.dmi.lib.polygon.mapproduction.location.Station;
import dk.dmi.lib.polygon.mapproduction.utilities.Country;

@Component(
		name = "Generate Grid File Map", 
		category = "Polygon",
		description = "Generate grid file map image based on a grid file",
        version = 1)
public class GenerateGridFileImage extends BaseComponent {
	
	@ExecuteMethod(
			argumentDescriptions = {"Root local location of the grid file folder", "grid file entity", "path and filename from root location to store map image", "Pixel width of the image"})
	public void execute(String gridFilesRootLocation, GridFile gridFile, String mapFileName, int widthResolution, int qualityLevel) throws IOException, ParseException {
		Country currentCountry = new Country(Country.SELECT_DENMARK);
		
		Parameter parameter = gridFile.getParameter();
		ArrayList<GridPoint> griddata = generateGridPointsFromFile(gridFilesRootLocation, gridFile);
		String legendFileName = getLegendFileName(parameter, griddata, currentCountry);
		
		GenerateGridImage ggi = new GenerateGridImage(currentCountry, legendFileName);
		
		Locale denmark = new Locale("da", "DK");
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(gridFile.getTimeObs());
		
		String formattedDayDate = String.format(denmark, "%1$te. %1$tB %1$tY", gregorianCalendar);
		String toDisplay = parameter.getMapType()+" "+formattedDayDate;
		
		Date today = new Date();
		long diffDays = TimeUnit.DAYS.convert(today.getTime()-gridFile.getTimeObs().getTime(), TimeUnit.MILLISECONDS);
		
		if(diffDays == 0) {
			GridFileCalculationController gridFileCalculationController = GridFileCalculationController.getInstance();
			GridFileCalculation gridFileCalculation = gridFileCalculationController.getCompletedLatestGridFileCalculationByGridFile(gridFile);
			
			TimeZone newTimeZone = TimeZone.getTimeZone("Europe/Copenhagen");
			Date dkCalculatedTime = DateUtils.convertDateToTimeZones(gridFileCalculation.getTimeCalculated(), newTimeZone);
			String calculateHour = DateUtils.formatDate(dkCalculatedTime, "HH");
			toDisplay += " kl.00-"+calculateHour;
		}
		
		if(!parameter.getUnit().getName().trim().equals("")) {
			toDisplay += " ("+parameter.getUnit().getName()+")"; // ex. Middel. 29. november 2017 (°C)
		}
		
		BasisDailyDkController basisDailyDkController = BasisDailyDkController.getInstance();
		int amountFull = basisDailyDkController.getBasisDailyDkStats(gridFile.getTimeObs(), gridFile.getParameter().getElementNumber(), BasisDailyDkController.STATS_TYPE_FULL);
		int amountSome = basisDailyDkController.getBasisDailyDkStats(gridFile.getTimeObs(), gridFile.getParameter().getElementNumber(), BasisDailyDkController.STATS_TYPE_NONE);
		int amountNone = basisDailyDkController.getBasisDailyDkStats(gridFile.getTimeObs(), gridFile.getParameter().getElementNumber(), BasisDailyDkController.STATS_TYPE_SOME);
		String versionString = GenerateKlimaInfoImage.generateVersionString(denmark, new GregorianCalendar(), qualityLevel, amountFull, amountSome, amountNone); //ex Ver.: 20171130 0952 051 004 008 C AUTO
		
		String title = parameter.getMapName(); //ex Temperatur
		boolean automaticLegend = true;
		
		int offset = 0;
		int elementNumber = parameter.getElementNumber().intValue();
		
		if (elementNumber == 1121 || elementNumber == 3021 || elementNumber == 6331) {
			offset = -4;
		}
		
		// unused
		ArrayList<Station> stations = null;
		String name = null;
		
		String lowerLeftTitle = qualityLevel == 0 ? "Foreløbige Data" : "";
		
		ggi.generateImage(toDisplay, gregorianCalendar, versionString, mapFileName, title, griddata, automaticLegend, offset, stations, name, lowerLeftTitle, widthResolution);
	}
	
	ArrayList<GridPoint> generateGridPointsFromFile(String gridFilesRootLocation, GridFile gridFile) throws IOException {
		ArrayList<GridPoint> griddata = new ArrayList<GridPoint>();
		String gridFileLocation = FilenameUtils.separatorsToSystem(gridFilesRootLocation+gridFile.getFileUri());
		List<List<String>> gridFileList = FileUtils.textFileToListListReader(gridFileLocation, " ", FileUtils.COMPRESSION_GZIP_KEY, 0);
		
		for (List<String> gridLine : gridFileList) {
			int id = Integer.parseInt(gridLine.get(0));
			int x = Integer.parseInt(gridLine.get(1));
			int y = Integer.parseInt(gridLine.get(2));
			
			double terrPercent = 1.0;
			double value = Double.parseDouble(gridLine.get(3));
			
			GridPoint gridPoint = new GridPointSimple(id, x, y, terrPercent, value);
			griddata.add(gridPoint);
		}
		
		return griddata;
	}
	
	String getLegendFileName(Parameter parameter, ArrayList<GridPoint> griddata, Country currentCountry) {
		String legendFileName = null;
		int elementNumber = parameter.getElementNumber().intValue();
		
		if (elementNumber == 101 || elementNumber == 112 || elementNumber == 122) {
            double minData = Double.POSITIVE_INFINITY;
            double maxData = Double.NEGATIVE_INFINITY;
            double meanData = 0;

            //Find statitstics in order to use correct legend
            for (int j = 0; j < griddata.size(); j++) {
                if (griddata.get(j).getValue() < minData) {
                    minData = griddata.get(j).getValue();
                }
                if (griddata.get(j).getValue() > maxData) {
                    maxData = griddata.get(j).getValue();
                }
                meanData += griddata.get(j).getValue();
            }
            meanData /= griddata.size();


            LegendSelector ls = new LegendSelector();

            String[] temperatureLegends = new String[]{
            	"temperatur-32--11.lvl",
                "temperatur-8-+13.lvl",
                "temperatur+8-+29.lvl",
                "temperatur-16-+5.lvl",
                "temperatur-24--3.lvl",
                "temperatur+0-+21.lvl",
                "temperatur+16-+37.lvl"
            };

            for (String s : temperatureLegends) {
                String legendXFileName = "/config/ColorDefinitions/" + s;

                ColorDefinitions cDef = new ColorDefinitions(legendXFileName, currentCountry.getColorDefinitions());
                ls.addLegend(legendXFileName, cDef, minData, maxData, meanData);
            }
            
            legendFileName = ls.getBestLegend();
        } else {
        	legendFileName = "/config/ColorDefinitions/"+parameter.getElementNumber()+".lvl";
        }
		
		return legendFileName;
	}
	
}
