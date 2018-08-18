package models.db.dao;

import akka.http.scaladsl.model.Uri;
import models.db.PatientAddress;
import models.db.dao.interfaces.PatientAddressDao;
import play.db.*;

import javax.persistence.*;
import java.util.concurrent.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.*;

import play.db.*;

import java.sql.*;

import play.libs.concurrent.*;
import scala.concurrent.stm.Txn;
import scala.reflect.internal.AnnotationInfos;

public class PatientAddressDaoImpl implements PatientAddressDao {

    private Connection db;
    //private CustomExecutionContext executionContext;


    @Inject
    public PatientAddressDaoImpl() {
        try {
            this.db = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pocketpillsdb",
                    "pocketpillsuser",
                    "pocketpills@123");
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
    }

    @Override
    public CompletionStage<List<PatientAddress>> getPatientAddress(long patientId) {
        List<PatientAddress> FetchedPatientAddresses = new ArrayList<PatientAddress>();
        try {
            PreparedStatement QueryString = db.prepareStatement("select * from patient_address where patient_id=?");
            QueryString.setLong(1, patientId);
            ResultSet FetchedPatientSet = QueryString.executeQuery();
            while (FetchedPatientSet.next())
                if(FetchedPatientSet.getBoolean("enabled") == true)
                    FetchedPatientAddresses.add(
                            new PatientAddress(
                                    FetchedPatientSet.getLong("patient_id"),
                                    FetchedPatientSet.getString("nickname"),
                                    FetchedPatientSet.getString("province"),
                                    FetchedPatientSet.getString("postal_code"),
                                    FetchedPatientSet.getString("street_address"),
                                    FetchedPatientSet.getString("city"),
                                    FetchedPatientSet.getString("country")
                            )
                    );


        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return CompletableFuture.completedFuture(FetchedPatientAddresses);
    }

    @Override
    public CompletionStage<Boolean> deletePatientAddress(long patientId, long addressId) {

        try{
            PreparedStatement Query = db.prepareStatement(
                    "UPDATE patient_address set enabled=? , when_modified=to_timestamp(?) where patient_id=?"
            );
            Query.setBoolean(1, false);
            Query.setLong(2, System.currentTimeMillis() / 1000L);
            Query.setLong(3, patientId);
            Query.executeUpdate();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.completedFuture(true);
    }


    @Override
    public CompletionStage<Long> addPatientAddress(long patientId, PatientAddress address) {
        // Implementing an Upsert Query to handle conflicts
        // Thank god for the assumption , normal insert
        Long InsertedRowId = -1L;
        try{
            PreparedStatement Query = db.prepareStatement(
                    "INSERT into patient_address values(default,?,?,?,?,?,?,?,to_timestamp(?),?,to_timestamp(?),?,?,?) "
            );
            Query.setLong(1, address.getPatientId());
            Query.setString(2, address.getNickname());
            Query.setString(3, address.getProvince());
            Query.setString(4, address.getPostalCode());
            Query.setString(5, address.getStreetAddress());
            Query.setString(6, address.getCity());
            Query.setString(7, address.getCountry());
            Query.setLong(8, (System.currentTimeMillis() / 1000L));
            Query.setBoolean(9, true);
            Query.setLong(10, (System.currentTimeMillis() / 1000L));
            Query.setLong(11, 1);
            Query.setLong(12, 1);
            Query.setLong(13, 1);

            Query.execute();

            PreparedStatement QueryId = db.prepareStatement(
                    "SELECT id from patient_address where patient_id=? AND nickname=? AND province=? AND postal_code=? AND street_address=? AND city=? AND country=?"
            );

            QueryId.setLong(1, address.getPatientId());
            QueryId.setString(2, address.getNickname());
            QueryId.setString(3, address.getProvince());
            QueryId.setString(4, address.getPostalCode());
            QueryId.setString(5, address.getStreetAddress());
            QueryId.setString(6, address.getCity());
            QueryId.setString(7, address.getCountry());

            ResultSet InsertedRecord = QueryId.executeQuery();
            while(InsertedRecord.next()) // Will always run only once
                InsertedRowId = InsertedRecord.getLong("id");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return CompletableFuture.completedFuture(-1L); // Unsuccessful
        }
        return CompletableFuture.completedFuture(InsertedRowId);
    }
}
