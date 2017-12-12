package com.andeslaw.foodorder;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private GoogleAccountCredential accountCredential;
    private ProgressDialog progressDialog;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {
            "https://www.googleapis.com/auth/drive",
            "https://www.googleapis.com/auth/spreadsheets",
            "https://www.googleapis.com/auth/script.external_request"
    };

    private static final String scriptId_Foodorder = "M_YcidpdvAgVGdqPWUIcqJ0w9ezPKz0cG";

    private static String[] restaurantList = null;
    private static String selectedRestaurant = null;
    private static String[] menuList = null;
    private static String selectedFood = null;
    private static String[] foodList = null;
    private static String[] unitPriceList = null;
    private static String[] itemUnitList = null;
    private static String[] priceCurrencyList = null;

    //private LinearLayout group1 = null;
    //private LinearLayout group2 = null;
    private Button gotoSelectRestaurant = null;
    private Spinner selectRestauralt = null;
    private Button gotoMenu = null;
    private Spinner selectFood = null;
    private Button addToOrder = null;
    private TextView itemPrice = null;
    private EditText itemQuantity = null;
    private TextView itemUnit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //group1 = findViewById(R.id.group1);
        //group2 = findViewById(R.id.group2);
        gotoSelectRestaurant = findViewById(R.id.gotoSelectRestaurant);
        selectRestauralt = findViewById(R.id.selectRestaurant);
        gotoMenu = findViewById(R.id.gotoMenu);
        selectFood = findViewById(R.id.selectFood);
        addToOrder = findViewById(R.id.addToOrder);
        itemPrice = findViewById(R.id.itemPrice);
        itemQuantity = findViewById(R.id.itemQuantity);
        itemUnit = findViewById(R.id.itemUnit);

        selectRestauralt.setVisibility(View.GONE);
        gotoMenu.setVisibility(View.GONE);
        selectFood.setVisibility(View.GONE);
        addToOrder.setVisibility(View.GONE);
        itemPrice.setVisibility(View.GONE);
        itemQuantity.setVisibility(View.GONE);
        itemUnit.setVisibility(View.GONE);

        // Initialize credentials and service object..
        accountCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        progressDialog = new ProgressDialog(this);
    }

    public void gotoSelectRestaurant(View view) {
        getRestaurantListFromApi();
    }

    private void startSelectRestaurant() {
        //startActivity(new Intent(this, SelectRestaurant.class));
        //group1.setVisibility(View.GONE);
        //group2.setVisibility(View.VISIBLE);
        //gotoSelectRestaurant.setVisibility(View.GONE);
        selectRestauralt.setVisibility(View.VISIBLE);
        gotoMenu.setVisibility(View.VISIBLE);
        selectRestauralt.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, restaurantList));
    }

    public void gotoMenu(View view) {
        selectedRestaurant = selectRestauralt.getSelectedItem().toString();
        getMenuListFromApi();
    }

    private void startMenu() {
        int menuLength = menuList.length;
        foodList = new String[menuLength];
        itemUnitList = new String[menuLength];
        unitPriceList = new String[menuLength];
        priceCurrencyList = new String[menuLength];
        for (int i=0; i<menuLength; i++) {
            String[] itemInfo = menuList[i].split("\\|");
            foodList[i] = itemInfo[0];
            itemUnitList[i] = itemInfo[1];
            unitPriceList[i] = itemInfo[2];
            priceCurrencyList[i] = itemInfo[3];
        }
        selectFood.setVisibility(View.VISIBLE);
        addToOrder.setVisibility(View.VISIBLE);
        selectFood.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, foodList));
    }

    private void getMenuListFromApi() {
        new MakeRequestTask(accountCredential, "getMenuList").execute();
    }

    public void gotoSingleItemOrder(View view) {
        int itemId = selectFood.getSelectedItemPosition();
        selectedFood = selectFood.getSelectedItem().toString();
        itemPrice.setText(unitPriceList[itemId] + " " + priceCurrencyList[itemId] + " 每 " + itemUnitList[itemId]);
        itemQuantity.clearComposingText();
        itemUnit.setText(itemUnitList[itemId]);
        itemPrice.setVisibility(View.VISIBLE);
        itemQuantity.setVisibility(View.VISIBLE);
        itemUnit.setVisibility(View.VISIBLE);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getRestaurantListFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (accountCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            //mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(accountCredential, "getRestaurantList").execute();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                accountCredential.setSelectedAccountName(accountName);
                getRestaurantListFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(accountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS
            );
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * An asynchronous task that handles the Google Apps Script Execution API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.script.Script mService = null;
        private Exception mLastError = null;
        private String exeState = null;

        MakeRequestTask(GoogleAccountCredential credential, String state) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            exeState = state;
            mService = new com.google.api.services.script.Script.Builder(
                    transport, jsonFactory, setHttpTimeout(credential)
            ).setApplicationName("Foodorder").build();
        }

        /**
         * Background task to call Google Apps Script Execution API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Call the API to run an Apps Script function that returns a list
         * of folders within the user's root directory on Drive.
         *
         * @return list of String folder names and their IDs
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException, GoogleAuthException {
            // ID of the script to call. Acquire this from the Apps Script editor,
            // under Publish > Deploy as API executable.
            String scriptId = scriptId_Foodorder;
            String functionName = exeState;
            List<Object> functionParameters = new ArrayList<>();

            // Create an execution request object.
            ExecutionRequest request = new ExecutionRequest()
                    .setDevMode(Boolean.TRUE) //TODO: to be removed before releasing the software.
                    .setParameters(functionParameters)
                    .setFunction(functionName);

            switch (exeState) {
                case "getMenuList":
                    functionParameters.add(selectedRestaurant);
                    break;
                case "getRestaurantList":
                    break;
            }

            // Make the request.
            Operation op = mService.scripts().run(scriptId, request).execute();

            // Print results of request.
            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }

            if (op.getResponse() != null && op.getResponse().get("result") != null) {
                return (List<String>) op.getResponse().get("result");
            } else {
                return null;
            }
        }

        /**
         * Interpret an error response returned by the API and return a String
         * summary.
         *
         * @param op the Operation returning an error response
         * @return summary of error response, or null if Operation returned no
         *     error
         */
        private String getScriptError(Operation op) {
            if (op.getError() == null) {
                return null;
            }

            // Extract the first (and only) set of error details and cast as a Map.
            // The values of this map are the script's 'errorMessage' and
            // 'errorType', and an array of stack trace elements (which also need to
            // be cast as Maps).
            Map<String, Object> detail = op.getError().getDetails().get(0);
            List<Map<String, Object>> stacktrace = (List<Map<String, Object>>)detail.get("scriptStackTraceElements");

            java.lang.StringBuilder sb = new StringBuilder("\nScript error message: ");
            sb.append(detail.get("errorMessage"));

            if (stacktrace != null) {
                // There may not be a stacktrace if the script didn't start
                // executing.
                sb.append("\nScript error stacktrace:");
                for (Map<String, Object> elem : stacktrace) {
                    sb.append("\n  ");
                    sb.append(elem.get("function"));
                    sb.append(":");
                    sb.append(elem.get("lineNumber"));
                }
            }
            sb.append("\n");
            return sb.toString();
        }

        @Override
        protected void onPreExecute() {
            switch (exeState) {
                case "getMenuList":
                    progressDialog.setMessage("Retrieving menu list from the backend system ...");
                    break;
                case "getRestaurantList":
                    progressDialog.setMessage("Retrieving restaurant list from the backend system ...");
                    break;
            }
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(Boolean.FALSE);
        }

        @Override
        protected void onPostExecute(List<String> output) {
            progressDialog.hide();
            switch (exeState) {
                case "getMenuList":
                    menuList = output.toArray(new String[0]);
                    startMenu();
                    break;
                case "getRestaurantList":
                    restaurantList = output.toArray(new String[0]);
                    startSelectRestaurant();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            progressDialog.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n"
                    //        + mLastError.getMessage());
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Extend the given HttpRequestInitializer (usually a credentials object)
     * with additional initialize() instructions.
     *
     * @param requestInitializer the initializer to copy and adjust; typically
     *         a credential object.
     * @return an initializer with an extended read timeout.
     */
    public static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws java.io.IOException {
                requestInitializer.initialize(httpRequest);
                // This allows the API to call (and avoid timing out on)
                // functions that take up to 6 minutes to complete (the maximum
                // allowed script run time), plus a little overhead.
                httpRequest.setReadTimeout(380000);
            }
        };
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    //mOutputText.setText(
                    //        "This app requires Google Play Services. Please install " +
                    //                "Google Play Services on your device and relaunch this app.");
                } else {
                    getRestaurantListFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        accountCredential.setSelectedAccountName(accountName);
                        getRestaurantListFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getRestaurantListFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    //@Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    //@Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }
}
