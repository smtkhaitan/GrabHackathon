package com.example.test;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.test.DatabaseHelper.TRANSACTION_TABLE_NAME;
import static com.example.test.DatabaseHelper.WALLET_TABLE_NAME;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;
    private int mInterval = 10; // 10 seconds by default, can be changed later
    private Handler mHandler;
    UpdateMsqlTable updateMsqlTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseHelper = new DatabaseHelper(this);
        updateMsqlTable = new UpdateMsqlTable();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        startRepeatingTask();


        Button databaseButton = findViewById(R.id.database);
        Button viewButton = findViewById(R.id.view);
        Button walletButton = findViewById(R.id.wallet);

        databaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInserted = insertToDatabase();
//                if (isInserted == true) {
//                    Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "Data not Inserted", Toast.LENGTH_LONG).show();
//                }
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = databaseHelper.getAllData(TRANSACTION_TABLE_NAME);
                if (cursor.getCount() == 0) {
                    showMessage("Error", "No data found");
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    while (cursor.moveToNext()) {
                        stringBuffer.append(cursor.getString(0) + "," +
                                cursor.getString(1) + "," +
                                cursor.getString(2) + "," +
                                cursor.getString(3) + "," +
                                cursor.getString(4) + "," +
                                cursor.getString(5) + "\n");
                    }
                    showMessage("Data", stringBuffer.toString());
                }
            }
        });

        walletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = databaseHelper.getAllData(WALLET_TABLE_NAME);
                if (cursor.getCount() == 0) {
                    showMessage("Error", "No data found");
                    return;
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    while (cursor.moveToNext()) {
                        stringBuffer.append(cursor.getString(1));
                    }
                    showMessage("Wallet Amount : ", stringBuffer.toString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    private boolean insertToDatabase() {
        boolean isInserted =  databaseHelper.insertData("1100.0", "sumit_test_123",
                "100.00", "0", "Received");

//        Toast.makeText(MainActivity.this, "Inserted to db...", Toast.LENGTH_LONG).show();
        if(isInserted) {
            int res = databaseHelper.updateWalletTable(100, "Received");
            if(res == -1) {
                Toast.makeText(MainActivity.this, "Wallet doesn't have money...", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Wallet is updated...", Toast.LENGTH_LONG).show();
            }
        }
        return isInserted;
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;


        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void updateDbToExternalDatabaseWhenInternetIsAvailable() {
        if (haveNetworkConnection()) {
            Cursor cursor = databaseHelper.getAllData(TRANSACTION_TABLE_NAME);
            if (cursor.getCount() != 0) {
                ArrayList<String> ids = new ArrayList<>();
                StringBuffer stringBuffer = new StringBuffer();
                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    String updatedToExternalDatabase = cursor.getString(4);
                    if (updatedToExternalDatabase.equals("0")) {
                        boolean isUpdated = updateMsqlTable.insertIntoRemoteDatabase(id,
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(5));

                        if (isUpdated) {
                            databaseHelper.update(id,
                                    cursor.getString(1),
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    "1",
                                    cursor.getString(5));
                            stringBuffer.append(id);
                            ids.add(id);
                        }
                    }
                }
                if (stringBuffer.length() > 0) {
                    Toast.makeText(MainActivity.this, "Updated transId = " + ids + " to external DB "
                            , Toast.LENGTH_LONG).show();
                }
            }

            Cursor cursor1 = databaseHelper.getAllData(WALLET_TABLE_NAME);
            if (cursor1.getCount() != 0) {
                while (cursor1.moveToNext()) {
                    String id = cursor1.getString(0);
                    updateMsqlTable.insertIntoRemoteDatabaseWallet(id, cursor1.getString(1));
                }
                Toast.makeText(MainActivity.this, "Updated wallet to external db"
                            , Toast.LENGTH_LONG).show();
            }

        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateDbToExternalDatabaseWhenInternetIsAvailable(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
