package dk.dmi.lib.workflow.component.synop;

import java.util.ArrayList;
import java.util.List;

public final class PS416Data {
	public String supplierName;
	public String productiontime;
	public String observationtime;
	public PS416ObservationDataVO currentObservationVO;
	public List<PS416ObservationDataVO> observationVOs = new ArrayList<>();
	
	public String getSupplierName() {
		return supplierName;
	}

	public String getProductiontime() {
		return productiontime;
	}

	public String getObservationtime() {
		return observationtime;
	}

	public List<PS416ObservationDataVO> getObservationVOs() {
		return observationVOs;
	}	

	public void createObservationVO() {
		PS416ObservationDataVO observationVO = new PS416ObservationDataVO();
		observationVOs.add(observationVO);
		currentObservationVO = observationVO;
	}
	
	public class PS416ObservationDataVO {
		public String sensorId;
		public String value;
		public String valueObservationTime;
		public String valueObservationUnit;
		
		public String getSensorId() {
			return sensorId;
		}

		public String getValue() {
			return value;
		}

		public String getValueObservationTime() {
			return valueObservationTime;
		}

		public String getValueObservationUnit() {
			return valueObservationUnit;
		}
	}
}