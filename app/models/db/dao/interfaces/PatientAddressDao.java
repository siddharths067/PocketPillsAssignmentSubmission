package models.db.dao.interfaces;

import models.db.PatientAddress;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface PatientAddressDao {

    /**
     * Retrieves a list of addresses corresponding to the {@code patientId} that are enabled;
     *
     * @param patientId Patient Identifier
     * @return CompletableFuture of type <List<{@see PatientAddress}>>
     */
    CompletionStage<List<PatientAddress>> getPatientAddress(long patientId);


    /**
     * Deletes are all soft-deletes. Sets {@see models.db.BaseModel#enabled} as false
     *
     * @param patientId patient identifier
     * @param addressId {@see PatientAddress} identifier
     * @return CompletableFuture of type True if successfull, false otherwise
     */
    CompletionStage<Boolean> deletePatientAddress(long patientId, long addressId);
    // No address ID in Database , ignoring it

    /**
     * Inserts the address with enabled flag set as True.
     * Assumption: This entry does not exist in the database already
     *
     * @param patientId patient identifier
     * @param address {@see PatientAddress}
     * @return CompletableFuture of type Long - the identifier of the inserted object
     */
    CompletionStage<Long> addPatientAddress(long patientId, PatientAddress address);

}
