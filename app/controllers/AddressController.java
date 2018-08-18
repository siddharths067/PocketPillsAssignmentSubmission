package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.db.PatientAddress;
import models.db.dao.PatientAddressDaoImpl;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import utils.AppUtil;

import javax.inject.Inject;

public class AddressController extends Controller {

    /*
     * HTTP Execution context saved incase queries go long
     * */

    private HttpExecutionContext httpExecutionContext;

    @Inject
    public AddressController(HttpExecutionContext httpExecutionContext) {
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Retrieves the List of addresses which are enabled corresponding to the patientId
     * (presented in the header as patient_id)
     * present in the header
     * <p>
     * We need to return only those addresses which have enabled flag set
     * as True {@see models.db.BaseModel#enabled}
     * <p>
     * <p>
     * The resultant JSON would look like:
     * {
     * "success": true,
     * "addresses": [
     * {
     * "nickname": "John Doe",
     * "province": "Manitoba",
     * "postal_code": "V0S0B1",
     * "street_address": "410, Hasbrouck, Gali number 37, Sector 4",
     * "city": "Toronto",
     * },
     * {
     * "nickname": "Batman",
     * ...
     * },
     * ...
     * ]
     * }
     * The addresses are given in the table
     *
     * @return List<Address> encapsulated in
     * @see models.db.PatientAddress
     * @see utils.AppUtil#getSuccessObject(JsonNode) if successful or
     * @see utils.AppUtil#getBadRequestObject(String) if unsuccessful
     */
    public CompletionStage<Result> getPatientAddress(long patientId) {
        CompletionStage<Result> res;
        try {
            res = ((new PatientAddressDaoImpl()).getPatientAddress(patientId)).thenApplyAsync(ans -> {
                return ok((new AppUtil()).getSuccessObject(Json.toJson(ans)));
            }, this.httpExecutionContext.current());
        } catch (Exception e) {
            res = CompletableFuture.completedFuture(
                    ok(
                            (new AppUtil().getBadRequestObject(e.getMessage().toString()))
                    )
            );
        }
        return res;
    }

    /**
     * @return json with the id of address encapsulated in
     * @see models.db.PatientAddress and the body are bound together.
     * The json body will look like as follows:
     * {
     * "nickname": "John Doe",
     * "province": "Manitoba",
     * "postal_code": "V0S0B1",
     * "street_address": "410, Hasbrouck, Gali number 37, Sector 4",
     * "city": "Toronto",
     * }
     * The patientId is present in the header (presented in the header as patient_id)
     * <p>
     * This object is saved in the table with enabled as True
     * @see utils.AppUtil#getSuccessObject(JsonNode) if successfull
     * @see utils.AppUtil#getBadRequestObject(String) if unsuccessfull
     */
    public CompletionStage<Result> addPatientAddress() {
        System.out.println("Entered Controller Succesfully ");
        CompletionStage<Result> res;
        try {
            Long patient_id = new Long(request().header("patient_id").get());
            System.out.println("patient id for insertion " + patient_id);
            System.out.println("Trying to get Data");
            res = (new PatientAddressDaoImpl().addPatientAddress(
                    patient_id,
                    new PatientAddress(
                            patient_id,
                            request().body().asJson().get("nickname").asText(),
                            request().body().asJson().get("province").asText(),
                            request().body().asJson().get("postal_code").asText(),
                            request().body().asJson().get("street_address").asText(),
                            request().body().asJson().get("city").asText(),
                            request().body().asJson().get("country").asText()
                    )
            )).thenApplyAsync(
                    InsertionStatus -> {
                        System.out.println("Data Recieved Maybe");
                        if (InsertionStatus != -1L)
                            return ok("Record Inserted with id = " + InsertionStatus);
                        else
                            return internalServerError("Record not Inserted, Contact Admin");
                    }
                    , this.httpExecutionContext.current());
        } catch (Exception e) {
            System.out.println("An Exception occcured here -> " + e.getMessage());
            return CompletableFuture.completedFuture(internalServerError(e.getMessage()));
        }
        return res;
    }


    /**
     * @param addressId which we need to delete
     * @return json denoting success or failure encapsulated in
     * @see models.db.PatientAddress entry is deleted corresponding to the {@code addressId}
     * For deleting a particular address, we just set the
     * enabled flag as false {@see models.db.BaseModel#enabled}
     * @see utils.AppUtil#getSuccessObject(JsonNode)  if successfully deleted
     * @see utils.AppUtil#getBadRequestObject(String) if unsuccessfull
     */
    public CompletionStage<Result> deletePatientAddress(long addressId) {
        CompletionStage<Result> res;
        try {
            res = (new PatientAddressDaoImpl().deletePatientAddress(addressId, 0))
                    .thenApplyAsync(StatusBoolean -> {
                        if (StatusBoolean == true)
                            return ok("Soft Deletion Successful");
                        else
                            return internalServerError("Soft Deletion Failed");
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ok("Deletion Failed " + e.getMessage()));
        }
        return res;
    }


}
