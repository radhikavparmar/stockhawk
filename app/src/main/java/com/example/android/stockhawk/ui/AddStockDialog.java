package com.example.android.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stockhawk.R;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddStockDialog extends DialogFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    EditText stock;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);
//        stock.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
//
//            }
//
//
//            public void afterTextChanged(Editable editable) {
//                Dialog d = getDialog();
//                if (d instanceof AlertDialog) {
//                    AlertDialog dialog = (AlertDialog) d;
//                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                    // Check if the EditText is empty
//                    if (editable.length() > 0) {
//                        //positiveButton.setEnabled(true);
//                        // Check for the availability of the stock!
//                        //suggestion for the correct availability.
//                        String s = stock.getText().toString();
//
//                        if(s.contains(" ")){
//                            s.replace(" ","");
//                            stock.setText(s);
//                        }else {
//
//                            String results = null;
//                            try {
//                                results = new CheckExistenceOfQuote().execute(stock.getText().toString()).get();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } catch (ExecutionException e) {
//                                e.printStackTrace();
//                            }
//
//                            if (results != null) {
//                                Log.e("TAG", "MMMMMMmatch");
//                                positiveButton.setEnabled(true);
//
//                            } else {
//                                stock.setError("Not a correct symbol!");
//                                Log.e("TAGG", "  NOOO  MMMMMMmatch");
//                                positiveButton.setEnabled(false);
//                            }
//                        }
//                    } else {
//                        // avoid dangling if else
//                    }
//                }
//            }
//        });
        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addStock();
                return true;
            }

        });
        builder.setView(custom);

        builder.setMessage(getString(R.string.dialog_title));


        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!networkUp()) {
                            Toast.makeText(getActivity(), "No internet connectivity.", Toast.LENGTH_SHORT).show();
                        } else {
                            String results = null;
                            String quoteEdittext = stock.getText().toString();

                            if (quoteEdittext.equals("") == false && quoteEdittext.contains(" ") == false) {

                                try {
                                    results = new CheckExistenceOfQuote().execute(quoteEdittext).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (results == null) {
                                Toast.makeText(getActivity(), "Invalid input.", Toast.LENGTH_SHORT).show();
                            } else {

                                addStock();
                            }
                        }
                    }
                });

        builder.setNegativeButton(getString(R.string.dialog_cancel), null);

        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }


    private void addStock() {
        Activity parent = getActivity();
        if (parent instanceof MainActivity) {
            ((MainActivity) parent).addStock(stock.getText().toString());
        }
        dismissAllowingStateLoss();
    }

    private boolean networkUp() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }


}
