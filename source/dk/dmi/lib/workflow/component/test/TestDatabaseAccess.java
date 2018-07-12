package dk.dmi.lib.workflow.component.test;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import dk.dmi.lib.common.AnnotationUtils;
import dk.dmi.lib.persistence.common.EntityUtils;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.CommonDataUtils;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ArgumentListGetMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.Component;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.ExecuteMethod;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.InjectContextControllerMethod;

@Component(
	name = "Test database access", 
	category = "Test",
	description = "Access is tested and a single row select is performed on a random table, for every database in the persistence layer.",
    version = 1)
public class TestDatabaseAccess {
	
	static final String FORMAT_TEXT = "Text";
	static final String FORMAT_HTML = "HTML";
	
	WorkflowContextController workflowContextController;
	String format;
	
	@InjectContextControllerMethod
	public void injectContext(WorkflowContextController workflowContextController) {
		this.workflowContextController = workflowContextController;
	}
	
	@ArgumentListGetMethod(
			argumentIndex = "0")
	public String[] getListOfFormats(String ignore) {
		return new String[] {FORMAT_TEXT, FORMAT_HTML};
	}
	
	@ExecuteMethod(
			argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_LIST},
			argumentDescriptions = {"Type of result format, e.g. plain text or html"},
			returnDescription = "Test result text")
	public String execute(String resultFormat) {
		format = resultFormat;
		
		StringBuffer testResultBuffer = new StringBuffer();
		testResultBuffer.append("***** Testing database connections *****");
		testResultBuffer.append(newLine());
		testResultBuffer.append(newLine());
		
		boolean overallResult = true;
		String whereClause = "";
		int maxResultsInt = 1;
		Map<String, Object> contextMap = workflowContextController.getWorkflowContextMap();
		
		Map<String, Class<?>> persistenceUtilsClassMap = EntityUtils.getPersistenceUtilsClassMap();
		Iterator<Entry<String, Class<?>>> persistenceUtilsClassMapIterator = persistenceUtilsClassMap.entrySet().iterator();
		
		while (persistenceUtilsClassMapIterator.hasNext()) {
			Map.Entry<String, Class<?>> persistenceUtilsClassPairs = (Map.Entry<String, Class<?>>) persistenceUtilsClassMapIterator.next();
			overallResult = overallResult & testPersistenceClass(testResultBuffer, whereClause, maxResultsInt, contextMap, persistenceUtilsClassPairs);
		}
		
		String overallResultText = overallResult ? highlight("SUCCESS", overallResult) : highlight("FAILED", overallResult);
		
		testResultBuffer.append("Overall database test result: ");
		testResultBuffer.append(overallResultText);
		testResultBuffer.append(newLine());
		testResultBuffer.append(newLine());
		testResultBuffer.append("*****************************************");
		testResultBuffer.append(newLine());
		testResultBuffer.append(newLine());
		
		return testResultBuffer.toString();
	}

	boolean testPersistenceClass(StringBuffer testResultBuffer, String whereClause, int maxResultsInt, Map<String, Object> contextMap, Map.Entry<String, Class<?>> persistenceUtilsClassPairs) {
		boolean databaseSuccess = true;
		String persistenceUtilsName = persistenceUtilsClassPairs.getKey();
		testResultBuffer.append("Testing "+persistenceUtilsName+":");
		testResultBuffer.append(newLine());
		
		Class<?> persistenceUtilsClass = persistenceUtilsClassPairs.getValue();
		EntityUtils entityUtils = getPersistenceEntityUtils(persistenceUtilsClass);
		testResultBuffer.append(" - Connecting... ");
		
		if(entityUtils != null) {
			testResultBuffer.append(highlight("Success", true));
			testResultBuffer.append(newLine());
			
			boolean randomSelectsuccess = selectRandomPersistenceObject(whereClause, maxResultsInt, contextMap, entityUtils);
			testResultBuffer.append(" - Selecting data... ");
			
			if(randomSelectsuccess) {
				testResultBuffer.append(highlight("Success", true));
				testResultBuffer.append(newLine());
			} else {
				testResultBuffer.append(highlight("Failed", false));
				testResultBuffer.append(newLine());
				databaseSuccess = false;
			}
		} else {
			testResultBuffer.append(highlight("Failed", false));
			testResultBuffer.append(newLine());
			databaseSuccess = false;
		}
		
		testResultBuffer.append(newLine());
		return databaseSuccess;
	}

	EntityUtils getPersistenceEntityUtils(Class<?> persistenceUtilsClass) {
		try {
			Method getInstanceMethod = persistenceUtilsClass.getMethod("getInstance");
			EntityUtils entityUtils = (EntityUtils) getInstanceMethod.invoke(null);
			return entityUtils;
		} catch(Exception e) {
			return null;
		}
	}
	
	boolean selectRandomPersistenceObject(String whereClause, int maxResultsInt, Map<String, Object> contextMap, EntityUtils entityUtils) {
		try {
			EntityManager entityManager = entityUtils.getEntityManager();
			String schema = entityUtils.getDatabaseSchema();
			String entityPackagePath = entityUtils.getEntityPackagePath();
			String entityNameOfFirstTable = AnnotationUtils.getListOfAnnotatedClassNames(Entity.class, entityPackagePath)[1];
			String entityFullName = entityPackagePath+entityNameOfFirstTable;
			
			CommonDataUtils.selectSingleEntityByWhereClause(entityManager, schema, entityFullName, true, whereClause, maxResultsInt, contextMap);
			entityUtils.closeEntityManager();
			
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	String newLine() {
		if(FORMAT_TEXT.equals(format)) {
			return "\n";
		} else if(FORMAT_HTML.equals(format)) {
			return "<br/>";
		} else {
			return null;
		}
	}
	
	String highlight(String text, boolean success) {
		String highlightText = "";
		
		if(FORMAT_TEXT.equals(format)) {
			if(success) {
				highlightText += "+" + text + "+";
			} else {
				highlightText += "-" + text + "-";
			}
		} else if(FORMAT_HTML.equals(format)) {
			if(success) {
				highlightText += "<font color=\"green\">" + text + "</font>";
			} else {
				highlightText += "<font color=\"red\">" + text + "</font>";
			}
		}
		
		return highlightText;
	}

}
