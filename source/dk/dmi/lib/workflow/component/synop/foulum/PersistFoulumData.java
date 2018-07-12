package dk.dmi.lib.workflow.component.synop.foulum;

import dk.dmi.lib.persistence.common.UniqueIdentifierGenerator;
import dk.dmi.lib.persistence.database.processdb.publicc.controller.WorkflowContextController;
import dk.dmi.lib.workflow.common.BaseComponent;
import dk.dmi.lib.workflow.common.WorkflowAnnotations;
import dk.dmi.lib.workflow.common.WorkflowAnnotations.*;
import dk.dmi.lib.workflow.component.synop.asiaq.ObservationWithType;

import java.sql.*;
import java.util.List;

@Component(
        name = "Persist Foulum observation data",
        category = "Foulum",
        description = "",
        version = 1)
public class PersistFoulumData extends BaseComponent {
    private WorkflowContextController workflowContextController;

    @InjectContextControllerMethod
    public void injectContext(WorkflowContextController workflowContextController) {
        this.workflowContextController = workflowContextController;
    }

    @ExecuteMethod(
            argumentDisplayTypes = {WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT, WorkflowAnnotations.ARGUMENT_DISPLAY_TYPE_TEXT},
            argumentDescriptions = {"File name", "FoulumTest observation data."},
            returnDescription="true/false - true if data was persisted correctly. ")
    public boolean execute(String fileName, List<ObservationWithType> observations) {

        Connection connection = null;
        try {
            long utcUnixTime = observations.get(0).getUtcUnixTimeStamp();

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://obs6/obs1_2", "oman", "2Wild!");
            connection.setAutoCommit(false);

            final Statement statementFiles = connection.createStatement();

            LOGGER.debug("Saving file info for " + fileName + " to obs1_2.data_foulum_file");

            statementFiles.executeUpdate("INSERT INTO obs1_2.data_foulum_file (created, file_name, data, supplier, production_time, observation_time) VALUES(" +
                    "FROM_UNIXTIME(" + System.currentTimeMillis() / 1000 + ")," +
                    "'" + fileName + "'," +
                    "''," + // TO-DO: insert file content instead of null
                    "'FOULUM'," +
                    "FROM_UNIXTIME(" + (utcUnixTime + 60000) + ")," +
                    "FROM_UNIXTIME(" + utcUnixTime + ")"+
                    " )");

            LOGGER.debug("Saving observations from file " + fileName + " to obs1_2.data_foulum");

            String sql = "INSERT INTO obs1_2.data_foulum (created, foulum_station_id, " +
                    "observation_type, observation_interval, value, observation_time, unit, id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statementData = connection.prepareStatement(sql);

            for (ObservationWithType observationWithTypeData : observations) {
                long id = UniqueIdentifierGenerator.generateNewId(true);
                observationWithTypeData.setId(id);

                statementData.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statementData.setString(2, observationWithTypeData.getStationNo());
                statementData.setString(3, observationWithTypeData.getObservationType());
                statementData.setInt(4, observationWithTypeData.getObservationInterval());
                statementData.setString(5, observationWithTypeData.getValue());
                statementData.setTimestamp(6, new Timestamp(observationWithTypeData.getUtcUnixTimeStamp() * 1000));
                statementData.setString(7, observationWithTypeData.getUnit());
                statementData.setLong(8, id);
                statementData.addBatch();
            }

            statementData.executeBatch();

            connection.commit();
            statementData.close();

            statementFiles.close();
            connection.close();

            return true;
        } catch (Exception e) {
            LOGGER.error("Error in PersistFoulumData. ", e);
            workflowContextController.addObjectToContext("ERROR", "Error persisting observation data. -- " + e.getMessage(), false);
            if (connection != null) {
                try {
                    LOGGER.warn("Transaction is being rolled back");
                    connection.rollback();
                } catch(SQLException excep) {
                    LOGGER.error("Error in PersistFoulumData. Cannot roll back transaction.", excep);
                }
            }
        }

        return false;
    }

}