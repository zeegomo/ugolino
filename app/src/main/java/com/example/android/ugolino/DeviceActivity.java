package com.example.android.ugolino;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * Created by ${Giacomo} on ${04/01/2017}
 */

public class DeviceActivity extends AppCompatActivity {

    MqttHandler mqttHandler = MainActivity.mqttHandler;
    ArrayList<Device> devices = new ArrayList<>();
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        final boolean type = getIntent().getBooleanExtra("type", false);
        getDevices(type);
        position = getIntent().getIntExtra("position", 0);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextAppearance(this, R.style.MyTitleTextAppearance);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(DeviceActivity.this);
            }
        });

        setSupportActionBar(myToolbar);


        final TextView deviceName = (TextView) findViewById(R.id.device_name_activity);
        deviceName.setText(devices.get(position).getmName());

        final TextView topicTextView = (TextView) findViewById(R.id.topic);
        if (type)
            topicTextView.setText(devices.get(position).getmWrite_topic());
        else
            topicTextView.setText(devices.get(position).getmRead_topic());


        final TextView maskTextView = (TextView) findViewById(R.id.mask);
        maskTextView.setText(devices.get(position).getmMask());

        final TextView brokerTextView = (TextView) findViewById(R.id.broker);
        brokerTextView.setText(devices.get(position).getmBroker());


        //Delete Device
        ImageView deleteButton = (ImageView) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                alert.setMessage("Are you sure?");
                alert.setTitle("Delete Device");

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        devices.remove(position);
                        Save(type);
                        Intent mainIntent = new Intent(DeviceActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });


        //Change Device Name
        ImageView modifyButton = (ImageView) findViewById(R.id.modify_button);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                final EditText edittext = new EditText(DeviceActivity.this);
                alert.setMessage("Enter Name");
                alert.setTitle("New Device");
                alert.setView(edittext);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = edittext.getText().toString();
                        devices.get(position).setmName(name);
                        deviceName.setText(devices.get(position).getmName());
                        Save(type);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });

        //Change Device Name
        final ImageView tlsButton = (ImageView) findViewById(R.id.tls_button);

        if (devices.get(position).isSecure())
            tlsButton.setImageResource(R.drawable.ic_vpn_key_red_24dp);
        else
            tlsButton.setImageResource(R.drawable.ic_vpn_key_black_24dp);

        tlsButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alert.setMessage("Provide optional username and password");
                    alert.setTitle("TLS config");

                    LinearLayout layout = new LinearLayout(DeviceActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText passEditText = new EditText(DeviceActivity.this);
                    final EditText userEditText = new EditText(DeviceActivity.this);

                    passEditText.setHint("password");
                    //NOT WORKING
                    passEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    passEditText.setGravity(Gravity.CENTER);

                    userEditText.setHint("username");
                    userEditText.setGravity(Gravity.CENTER);

                    layout.addView(passEditText);
                    layout.addView(userEditText);
                    alert.setView(layout);

                    alert.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            tlsButton.setImageResource(R.drawable.ic_vpn_key_red_24dp);


                            //Password safe handling done by AndroidKeyStore
                            String password = null;
                            byte[] iv = null;
                            try {
                                final byte[] encryptedText = MainActivity.encryptor
                                        .encryptText(devices.get(position).getAlias(), passEditText.getText().toString());
                                password = (Base64.encodeToString(encryptedText, Base64.DEFAULT));
                                iv = MainActivity.encryptor.getIv();
                            } catch (UnrecoverableEntryException | NoSuchAlgorithmException | NoSuchProviderException |
                                    KeyStoreException | IOException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | SignatureException |
                                    IllegalBlockSizeException | BadPaddingException e) {
                                e.printStackTrace();
                            }

                            String user = userEditText.getText().toString();
                            devices.get(position).setUser(user);
                            devices.get(position).setIv(iv);
                            if (password == null) {
                                Toast toast = Toast.makeText(DeviceActivity.this, "Password encryption error - password not set", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                devices.get(position).setPassword(password);
                            }
                            devices.get(position).setSecure(true);
                            Save(type);

                        }
                    });

                    alert.setNegativeButton("Disable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // what ever you want to do with No option.
                            tlsButton.setImageResource(R.drawable.ic_vpn_key_black_24dp);
                            devices.get(position).setSecure(false);
                            Save(type);
                        }
                    });
                }else{

                    //Handling call with API < 23 where it is not possible to store passwords
                    alert.setMessage("Password login is only available in Android 6.0 or above, Plain TLS is still supported");
                    alert.setTitle("TLS Connection");

                    alert.setPositiveButton("Enable",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // what ever you want to do with No option.
                            tlsButton.setImageResource(R.drawable.ic_vpn_key_red_24dp);
                            devices.get(position).setSecure(true);
                            Save(type);
                        }
                    });

                    alert.setNegativeButton("Disable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // what ever you want to do with No option.
                            tlsButton.setImageResource(R.drawable.ic_vpn_key_black_24dp);
                            devices.get(position).setSecure(false);
                            Save(type);
                        }
                    });
                }
                alert.show();
            }
        });

        //Change Device Topic
        topicTextView.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                final EditText edittext = new EditText(DeviceActivity.this);
                edittext.setGravity(Gravity.CENTER);
                alert.setMessage("Enter new Topic");
                alert.setTitle("Change Topic");
                alert.setView(edittext);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String topic = edittext.getText().toString();
                        if (type) {
                            devices.get(position).setmWrite_topic(topic);
                            topicTextView.setText(devices.get(position).getmWrite_topic());

                        } else {
                            devices.get(position).setmRead_topic(topic);
                            topicTextView.setText(devices.get(position).getmRead_topic());

                        }
                        Save(type);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });

        //Change Device Mask
        maskTextView.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                final EditText edittext = new EditText(DeviceActivity.this);
                edittext.setGravity(Gravity.CENTER);
                alert.setMessage("Enter new Mask");
                alert.setTitle("Change Mask");
                alert.setView(edittext);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String mask = edittext.getText().toString();
                        devices.get(position).setmMask(mask);
                        maskTextView.setText(devices.get(position).getmMask());
                        //mqttHandler.updateConnections();
                        Save(type);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });


        //Change Device Broker

        brokerTextView.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                final EditText edittext = new EditText(DeviceActivity.this);
                edittext.setGravity(Gravity.CENTER);
                alert.setMessage("Enter new Broker");
                alert.setTitle("Change Broker");
                alert.setView(edittext);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String broker = edittext.getText().toString();
                        devices.get(position).setmBroker(broker);
                        brokerTextView.setText(devices.get(position).getmBroker());
                        //mqttHandler.updateConnections();
                        Save(type);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });


    }

    public void getDevices(boolean type) {
        if (type)
            devices = MainActivity.interact_devices;
        else
            devices = MainActivity.read_devices;
    }

    void Save(boolean type) {
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();

        Log.e("Saving", "DeviceActivity");
        //Saving devices
        String json = gson.toJson(devices);
        if (type) {
            prefsEditor.putString("interact_devices", json);
            InteractFragment.dataNotify(devices);
        } else {
            ReadFragment.dataNotify(devices);
            prefsEditor.putString("read_devices", json);
        }
        prefsEditor.apply();
    }
}
