package dk.dmi.lib.workflow.component.polygon;

import java.util.ArrayList;
import java.util.List;

import dk.dmi.lib.polygon.gridproduction.locations.GridPoint;
import dk.dmi.lib.polygon.gridproduction.locations.GridPointSimple;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;

@Component(
		name = "Create drought index grid content", 
		category = "Polygon",
		description = "Create drought index 1x1 grid, based on 1x1 grid from precipitation, potential evaporation and yesterdays drought index",
        version = 1)
public class CreateDroughtIndex extends BaseComponent {
	static double VRC = 100.0;
	
	@ExecuteMethod(
			argumentDescriptions = {"Precipitation grid points", "Potential evaporation grid points", "Drought index yesterday grid points"},
			returnDescription = "drought index grid points")
    public List<GridPoint> execute(List<List<String>> precipitationGridPoints, List<List<String>> potentialEvaporationGridPoints, List<List<String>> droughtIndexYesterdayGridPoints) throws Exception {
        boolean firstDate = droughtIndexYesterdayGridPoints == null;
        ArrayList<GridPoint> droughtIndexGridPoints = new ArrayList<GridPoint>();

        for (int i = 0; i < precipitationGridPoints.size(); i++) {
    		int id = Integer.parseInt(precipitationGridPoints.get(i).get(0).trim());
    		int eastings = Integer.parseInt(precipitationGridPoints.get(i).get(1).trim());
    		int northings = Integer.parseInt(precipitationGridPoints.get(i).get(2).trim());
    		
            double precipitation = Double.parseDouble(precipitationGridPoints.get(i).get(3).trim());
            double potentialEvaporation = Double.parseDouble(potentialEvaporationGridPoints.get(i).get(3).trim());
            double droughtIndexYesterday = 100;
            
            if (!firstDate) {
            	droughtIndexYesterday = Double.parseDouble(droughtIndexYesterdayGridPoints.get(i).get(3).trim());
            }

            double currentVolume = getV(droughtIndexYesterday, precipitation, potentialEvaporation);
            droughtIndexGridPoints.add(new GridPointSimple(id, eastings, northings, 0, currentVolume));
        }

        return droughtIndexGridPoints;
    }
	
	double getV(double Vr, double N, double Ep) {
        double Ea = getEa(Vr, Ep);
        double R = getR(N, Ea, Vr);
        double result = Math.round((Vr + (N - Ea - R)) * 10.0) / 10.0;

        if (result > VRC) {
            result = VRC;
        }
        
        if (result < 0) {
            result = 0;
        }

        return result;
    }

	double getEa(double Vr, double Ep) {
        double result;
        
        if (Vr > 0.7 * VRC) {
            result = Ep;
        } else {
            result = Ep * (0.188 + 2 * Vr / VRC - 1.2 * Math.pow(Vr / VRC, 2));
        }
        
        return result;
    }

	double getR(double N, double Ea, double Vr) {
        double result;
        
        if (Vr < 0.7 * VRC) {
            result = 0;
        } else if (0.7 * VRC <= Vr && Vr <= VRC) {
            result = 0.1 * (N - Ea);
        } else {
            result = VRC - Vr;
        }
        
        return result;
    }
	
}
