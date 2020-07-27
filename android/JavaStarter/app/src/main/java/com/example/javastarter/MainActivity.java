package com.example.javastarter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.midtrans.sdk.gopaycheckout.core.GoPayCheckoutCallback;
import com.midtrans.sdk.gopaycheckout.core.GoPayCheckoutClient;
import com.midtrans.sdk.gopaycheckout.core.GoPayCheckoutError;
import com.midtrans.sdk.gopaycheckout.core.account.AccountResponse;
import com.midtrans.sdk.gopaycheckout.core.account.GoPayPartnerDetails;
import com.midtrans.sdk.gopaycheckout.core.account.PaymentOption;
import com.midtrans.sdk.gopaycheckout.core.transaction.GoPayDetails;
import com.midtrans.sdk.gopaycheckout.core.transaction.ItemDetail;
import com.midtrans.sdk.gopaycheckout.core.transaction.TransactionDetails;
import com.midtrans.sdk.gopaycheckout.core.transaction.TransactionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText phone;
    private EditText merchantServer;
    private TextView accountId;
    private TextView accountStatus;
    private LinearLayout accountDetails;
    private Button accountLinking;
    private LinearLayout accountDetailsButtons;
    private Button enquireAccount;
    private LinearLayout walletDetails;
    private TextView paymentOptions;
    private Button disableAccount;
    private EditText amount;
    private LinearLayout createTransactionContainer;
    private Button createTransaction;
    private EditText callbackUrl;

    private GoPayCheckoutClient goPayCheckoutClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = findViewById(R.id.phone_number_field);
        merchantServer = findViewById(R.id.merchant_server_url_field);
        accountLinking = findViewById(R.id.account_linking_button);
        accountDetailsButtons = findViewById(R.id.account_details_buttons);
        enquireAccount = findViewById(R.id.enquire_account_button);
        accountId = findViewById(R.id.account_id);
        accountStatus = findViewById(R.id.account_status);
        accountDetails = findViewById(R.id.account_details);
        walletDetails = findViewById(R.id.wallet_details);
        paymentOptions = findViewById(R.id.payment_options);
        disableAccount = findViewById(R.id.disable_account);
        amount = findViewById(R.id.amount_field);
        createTransactionContainer = findViewById(R.id.create_transaction_container);
        createTransaction = findViewById(R.id.create_transaction);
        callbackUrl = findViewById(R.id.callback_url_field);

        accountLinking.setOnClickListener(v -> {
            initGoPayClient();

            String phoneNumber = phone.getText().toString();
            goPayCheckoutClient.linkAccount(
                    MainActivity.this,
                    new GoPayPartnerDetails(
                            phoneNumber,
                            "62"
                    ),
                    new GoPayCheckoutCallback<AccountResponse>() {
                        @Override
                        public void onResponse(AccountResponse response) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Account linking succeeded",
                                    Toast.LENGTH_SHORT
                            ).show();

                            int statusCode = Integer.parseInt(response.getStatusCode());
                            if (statusCode >= 200 && statusCode < 300)
                                handleSucceededAccountLinking(response);
                            else accountDetails.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(GoPayCheckoutError error, AccountResponse errorResponse) {
                            Log.e("GoPayCheckoutDemo", "GOPAY CHECKOUT error", error);
                            accountDetails.setVisibility(View.GONE);
                            Toast.makeText(
                                    MainActivity.this,
                                    "Account linking failed. Error: " + error.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        });
    }

    private void initGoPayClient() {
        goPayCheckoutClient = new GoPayCheckoutClient(this, "MID-EXAMPLE-MID", callbackUrl.getText().toString(), merchantServer.getText().toString(), true);
    }

    private void handleSucceededAccountLinking(AccountResponse response) {
        accountDetails.setVisibility(View.VISIBLE);
        accountId.setText(response.getAccountId());
        String status = response.getAccountStatus();
        accountStatus.setText(status);

        int statusCode = Integer.parseInt(response.getStatusCode());

        if (statusCode == 200 && response.getMetadata() != null) {
            walletDetails.setVisibility(View.VISIBLE);
            PaymentOption option = response.getMetadata().getPaymentOptions().get(0);
            String formatted = String.format(
                    "Name:%1$s\nActive:%2$s\nToken:%3$s\nBalance:%4$s",
                    option.getName(),
                    option.getActive(),
                    option.getToken(),
                    String.format("%1$s %2$s", option.getBalance().getCurrency(), option.getBalance().getValue())
            );
            paymentOptions.setText(formatted);
            paymentOptions.setTag(option);

            disableAccount.setVisibility(View.VISIBLE);
            disableAccount.setOnClickListener(v -> {
                initGoPayClient();
                goPayCheckoutClient.disableAccount(accountId.getText().toString(), new GoPayCheckoutCallback<AccountResponse>() {
                    @Override
                    public void onResponse(AccountResponse response) {
                        Toast.makeText(
                                MainActivity.this,
                                "Account disabling succeeded",
                                Toast.LENGTH_SHORT
                        ).show();

                        handleSucceededAccountDisabling(response);
                    }

                    @Override
                    public void onFailure(GoPayCheckoutError error, AccountResponse errorResponse) {
                        Log.e("GoPayCheckoutDemo", "GOPAY CHECKOUT error", error);
                        walletDetails.setVisibility(View.GONE);
                        Toast.makeText(
                                MainActivity.this,
                                "Disable account failed. Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            });

            createTransactionContainer.setVisibility(View.VISIBLE);
            createTransaction.setOnClickListener(v -> {
                initGoPayClient();

                List<ItemDetail> items = new ArrayList<>();
                items.add(
                        new ItemDetail(
                                UUID.randomUUID().toString(),
                                "Beli barang bagus",
                                Long.parseLong(amount.getText().toString()),
                                1,
                                null
                        )
                );

                goPayCheckoutClient
                        .createTransaction(
                                MainActivity.this,
                                new GoPayDetails(
                                        accountId.getText().toString(),
                                        ((PaymentOption) paymentOptions.getTag()).getToken(),
                                        null
                                ),
                                new TransactionDetails(
                                        UUID.randomUUID().toString(),
                                        Long.parseLong(amount.getText().toString()),
                                        "IDR"
                                ),
                                "62" + phone.getText().toString(),
                                items,
                                new GoPayCheckoutCallback<TransactionResponse>() {
                                    @Override
                                    public void onResponse(TransactionResponse response) {
                                        Toast.makeText(
                                                MainActivity.this,
                                                "Transaction succeeded. ID = " + response.getTransactionId(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    @Override
                                    public void onFailure(GoPayCheckoutError error, TransactionResponse errorResponse) {
                                        Log.e("GoPayCheckoutDemo", "GOPAY CHECKOUT error", error);
                                        Toast.makeText(
                                                MainActivity.this,
                                                "Transaction failed. Error: " + error.getMessage(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
            });
        } else {
            disableAccount.setVisibility(View.GONE);
            disableAccount.setOnClickListener(null);
            createTransactionContainer.setVisibility(View.GONE);
            createTransaction.setOnClickListener(null);
        }

        if (statusCode < 202) {
            accountDetailsButtons.setVisibility(View.VISIBLE);
            enquireAccount.setOnClickListener(v -> {
                initGoPayClient();
                goPayCheckoutClient.enquireAccount(accountId.getText().toString(), new GoPayCheckoutCallback<AccountResponse>() {
                    @Override
                    public void onResponse(AccountResponse response) {
                        Toast.makeText(
                                MainActivity.this,
                                "Account enquiring succeeded",
                                Toast.LENGTH_SHORT
                        ).show();

                        int statusCode = Integer.parseInt(response.getStatusCode());
                        if (statusCode >= 200 && statusCode < 300)
                            handleSucceededAccountLinking(response);
                    }

                    @Override
                    public void onFailure(GoPayCheckoutError error, AccountResponse errorResponse) {
                        Log.e("GoPayCheckoutDemo", "GOPAY CHECKOUT error", error);
                        walletDetails.setVisibility(View.GONE);
                        Toast.makeText(
                                MainActivity.this,
                                "Enquire account failed. Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            });
        } else {
            accountDetailsButtons.setVisibility(View.GONE);
        }
    }

    private void handleSucceededAccountDisabling(AccountResponse response) {
        int statusCode = Integer.parseInt(response.getStatusCode());
        if (statusCode >= 200 && statusCode < 300) {
            walletDetails.setVisibility(View.GONE);
            disableAccount.setVisibility(View.GONE);
            String status = response.getAccountStatus();
            accountStatus.setText(status);
        }
    }
}