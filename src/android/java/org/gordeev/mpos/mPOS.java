package org.gordeev.mpos;

import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.*;
import org.json.JSONObject;

import ru.m4bank.cardreaderlib.data.TransactionComponents;
import ru.m4bank.cardreaderlib.enums.TransactionType;
import ru.m4bank.cardreaderlib.manager.CardReader;
import ru.m4bank.cardreaderlib.manager.methods.output.CardReaderResponseExternalHandler;
import ru.m4bank.cardreaderlib.manager.methods.output.ReaderInfoStatusTransactionHandler;
import ru.m4bank.cardreaderlib.parser.readerinfo.ReaderInfo;
import ru.m4bank.cardreaderlib.readers.host.TransactionCompletionData;
import ru.m4bank.connectionreaders.activate.ManagerStateReader;
import ru.m4bank.connectionreaders.activate.enums.ReaderType;
import ru.m4bank.connectionreaders.activate.handler.ConnectionReader;

public class mPOS extends CordovaPlugin implements ConnectionReader, CardReaderResponseExternalHandler {

    private static final String TAG = "mPOS";
    private static final String countryCode = "643";
    private static final String currencyExponent = "2";

    // Actions
    private static final String ACTIVATE_MANAGER = "activateManagerAndGetAvailableDevices";
    private static final String CONNECT = "connect";
    private static final String DISCONNECT = "disconnect";
    private static final String IS_HAVE_CONNECTED_READER = "isHaveConnectedReader";
    private static final String PAYMENT = "payment";
    private static final String GET_READER_INFO = "getReaderInfo";
    private static final String CLOSE_DAY = "closeDayAndGetReport";
    private static final String GET_PAYMENT_STATUS = "getPaymentStatus";

    private CallbackContext callbackContext;
    private String transactionId;
    private CardReader cardReader;
    private ManagerStateReader managerStateReader;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, "Initializing mPOS plugin");
        managerStateReader = new ManagerStateReader(cordova.getContext(), this);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        try {

            if (action.equals(ACTIVATE_MANAGER)) {
                this.callbackContext = callbackContext;
                String type = args.optString(0);
                ReaderType readerType = type.equals("D200") ? ReaderType.D200 : ReaderType.ICMP_UPOS;
                activateReader(readerType);
            }

            if (action.equals(CONNECT)) {
                this.callbackContext = callbackContext;
                String deviceID = args.getString(0);
                connect(deviceID);
            }

            if (action.equals(DISCONNECT)) {
                this.callbackContext = callbackContext;
                disconnect();
            }

            if (action.equals(IS_HAVE_CONNECTED_READER)) {
                this.callbackContext = callbackContext;
                isHaveConnectedReader();
            }

            if (action.equals(PAYMENT)) {
                this.callbackContext = callbackContext;
                this.transactionId = UUID.randomUUID().toString();
                String amount = args.getString(0);
                payment(this.transactionId, amount);
            }

            if (action.equals(GET_PAYMENT_STATUS)) {
                this.callbackContext = callbackContext;
                String transactionId = args.getString(0);
                payment(transactionId,null);
            }

            if (action.equals(GET_READER_INFO)) {
                getReaderInfo(callbackContext);
            }

            if (action.equals(CLOSE_DAY)) {
                this.callbackContext = callbackContext;
                closeDay();
            }

            return true;

        } catch (Exception error) {
            Log.d(TAG, error.toString());
            callbackContext.error(TAG + "fail on execute '" + action + "' action. For more information see logcat");
            return false;
        }

    }

    @Override
    public void unsupportedMethod() {
        return;
    }

    public void activateReader(ReaderType readerType) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                managerStateReader.activateReader(readerType);
            }
        });
    }

    @Override
    public void onDeviceList(List<String> devices) {
        Boolean isEmpty = devices.isEmpty();
        PluginResult pluginResult = new PluginResult(isEmpty ? PluginResult.Status.NO_RESULT : PluginResult.Status.OK, new JSONArray(devices));
        callbackContext.sendPluginResult(pluginResult);
        callbackContext = null;
    }

    public void connect(final String deviceName) {
        Log.d(TAG, "Connect - " + deviceName);
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                managerStateReader.selectReaderFromList(deviceName);
            }
        });
    }

    @Override
    public void onConnected(CardReader reader) {
        Log.d(TAG, "onConnected");
        cardReader = reader;
        callbackContext.success();
        callbackContext = null;
    }

    @Override
    public void onConnectedError(CardReader cardReader) {
        Log.d(TAG, "onConnectedError");
        callbackContext.error(TAG + "fail on connect to reader");
        callbackContext = null;
    }

    public void disconnect() {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                if (cardReader != null) {
                    cardReader.disconnect();
                    return;
                }

                callbackContext.success();
                callbackContext = null;
            }
        });
    }

    @Override
    public void onDisconnected(CardReader cardReader) {
        Log.d(TAG, "onDisconnected");
        // TODO: emit event window.mPOSHasDisconnected()
        if (callbackContext != null) {
            callbackContext.success();
            callbackContext = null;
        }
        cardReader = null;
    }

    public void isHaveConnectedReader() {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Boolean connected = cardReader != null && cardReader.isConnected();
                if (callbackContext != null) {
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, connected);
                    callbackContext.sendPluginResult(pluginResult);
                    callbackContext = null;
                }
            }
        });
    }

    public void closeDay() {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                cardReader.reconciliation((CardReaderResponseExternalHandler) mPOS.this);
            }
        });
    }

    public void payment(final String transactionId, final String amount) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                TransactionComponents transactionComponents = new TransactionComponents();
                transactionComponents.setCurrency(countryCode);
                transactionComponents.setCountryCode(countryCode);
                transactionComponents.setCurrencyExponent(currencyExponent);
                transactionComponents.setTransactionUnicalId(transactionId);
                transactionComponents.setAmount(amount);
                transactionComponents.setTransactionType(amount != null ? TransactionType.PAYMENT : TransactionType.TRANSACTION_STATUS_RESPONSE);
                cardReader.readCard(transactionComponents, (CardReaderResponseExternalHandler) mPOS.this);
            }
        });
    }

    @Override
    public void onTransactionWithHostCompleted(final TransactionCompletionData data) {
        Boolean isFail = data.getResultCode() == 30110001;
        Boolean isRecocilation = !isFail && data.getAuthorizationCode() == null;

        HashMap<String, String> result = new HashMap<>();
        result.put("resultCode", data.getResultCode() + "");
        result.put("terminalNumber", data.getTerminalNumber());

        if (isFail) {
            result.put("errorMessage", data.getErrorMessage());
        } else {
            if (!isRecocilation) {
                result.put("transactionId", this.transactionId);
                result.put("authorizationCode", data.getAuthorizationCode());
                result.put("cardExpiryDate", data.getCardExpiryDate());
                result.put("cardNumberHash", data.getCardNumberHash());
                result.put("maskedCardNumber", data.getMaskedCardNumber());
                result.put("paymentSystemName", data.getPaymentSystemName());
                result.put("rrn", data.getRrn());
                result.put("transactionNumber", data.getTransactionNumber());
                result.put("operationDateTime", data.getOperationDateTime() + "");
                result.put("isPinEntered", data.isPinEntered() + "");
                result.put("isSign", data.isSign() + "");
                result.put("requestId", data.getRequestId() + "");
                if (data.getCardEntryType() != null) {
                    result.put("cardEntryTypeName", data.getCardEntryType().name());
                    result.put("cardEntryTypeCode", data.getCardEntryType().getCode());
                }
            }

            result.put("receipt", data.getReceipt());
        }

        PluginResult pluginResult = new PluginResult(isFail ? PluginResult.Status.ERROR : PluginResult.Status.OK, new JSONObject(result));
        callbackContext.sendPluginResult(pluginResult);
        callbackContext = null;
    }

    public void getReaderInfo(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {

                ReaderInfoStatusTransactionHandler handler = new ReaderInfoStatusTransactionHandler() {
                    @Override
                    public void success(final ReaderInfo result) {
                        HashMap<String, String> resultInfo = new HashMap<>();
                        resultInfo.put( "ksn", result.getKsn());
                        resultInfo.put( "readerVersion", result.getReaderVersion() );
                        resultInfo.put( "serialNumber", result.getSerialNumber() );
                        callbackContext.success(new JSONObject(resultInfo));
                    }
                    @Override
                    public void unsupportedMethod() {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                        callbackContext.sendPluginResult(pluginResult);
                    }
                };

                cardReader.getCardReaderInformation(handler);
            }
        });
    }
}