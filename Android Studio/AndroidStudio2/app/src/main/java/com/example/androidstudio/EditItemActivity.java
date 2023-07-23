package com.example.androidstudio;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androidstudio.Upload;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EditItemActivity extends AppCompatActivity {

    private String itemId;
    private EditText nameEditText;
    private EditText priceEditText;
    private EditText descEditText;
    private ImageView imageView;
    private CheckBox myCheckBox;
    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int PICK_LOCATION_REQUEST = 1;


    private void geoTagItem() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=Google Maps");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivityForResult(mapIntent, PICK_LOCATION_REQUEST);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = cursor.getString(column);
            cursor.close();

            String message = "Name: " + nameEditText.getText().toString() + "\nPrice: " + priceEditText.getText().toString() + "\nDescription: " + descEditText.getText().toString();
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(sendIntent);
        }
        if (requestCode == PICK_LOCATION_REQUEST && resultCode == RESULT_OK) {
            double lat = data.getDoubleExtra("latitude", 0);
            double lng = data.getDoubleExtra("longitude", 0);
            saveLocationToFirebase(lat, lng);
        }
    }


    private void saveLocationToFirebase(double lat, double lng) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference itemsRef = db.collection("products");

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", lat);
        location.put("longitude", lng);

        itemsRef.document(itemId).set(location, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Location saved to Firebase Firestore");
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        Button deleteButton = (Button) findViewById(R.id.delete_button);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        priceEditText = (EditText) findViewById(R.id.price_edit_text);
        descEditText = (EditText) findViewById(R.id.desc_edit_text);
        imageView = (ImageView) findViewById(R.id.image_view);
        myCheckBox = (CheckBox) findViewById(R.id.purchased_checkbox);
        itemId = getIntent().getStringExtra("itemId");
        retrieveItem(itemId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference productsRef = db.collection("products");
        DocumentReference itemRef = productsRef.document(itemId);
        itemRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        boolean checkboxValue = Boolean.TRUE.equals(document.getBoolean("checkbox"));
                        myCheckBox.setChecked(checkboxValue);
                    }
                }
            }
        });


        Button geoTagBtn = findViewById(R.id.geo_tag);
        geoTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoTagItem();
            }
        });

        Button sendSmsButton = (Button) findViewById(R.id.send_sms);
        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK);
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
            }
        });


        myCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference productsRef = db.collection("products");
                DocumentReference itemRef = productsRef.document(itemId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("checkbox", isChecked);
                itemRef.set(updates, SetOptions.merge());
            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItem(itemId);
                Intent intent = new Intent(getApplicationContext(), viewItem.class);
                startActivity(intent);
            }
        });

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
                Intent intent = new Intent(getApplicationContext(), viewItem.class);
                startActivity(intent);
            }
            private void saveChanges() {
                // Get the new values for the item's name, price, and description
                String newName = nameEditText.getText().toString();
                String newPrice = priceEditText.getText().toString();
                String newDesc = descEditText.getText().toString();

                // Initialize a FirebaseFirestore instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Get the "products" collection
                CollectionReference productsRef = db.collection("products");

                // Get the document with the matching itemId
                DocumentReference itemRef = productsRef.document(itemId);

                // Create a new map to store the new values
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", newName);
                updates.put("price", newPrice);
                updates.put("description", newDesc);

                // Update the item in Firestore
                itemRef.update(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Changes saved successfully
                                Toast.makeText(EditItemActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // An error occurred
                                Toast.makeText(EditItemActivity.this, "Error saving changes. Please try again.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error saving changes", e);
                            }
                        });
            }
        });
    }






    private void deleteItem(String itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference productsRef = db.collection("products");
        DocumentReference itemRef = productsRef.document(itemId);
        itemRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditItemActivity.this, "Item deleted successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditItemActivity.this, "Error deleting item. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    void retrieveItem(String itemId) {
        // Initialize a FirebaseFirestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the "products" collection
        CollectionReference productsRef = db.collection("products");

        // Get the document with the matching itemId
        DocumentReference itemRef = productsRef.document(itemId);

        // Retrieve the item from Firestore
        itemRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Display the item's information in the views
                        Upload upload = document.toObject(Upload.class);
                        String name = upload.getName();
                        String price = upload.getPrice();
                        String desc = upload.getDescription();
                        String url = upload.getUrl();
                        nameEditText.setText(name);
                        priceEditText.setText(price);
                        descEditText.setText(desc);
                        Picasso.with(EditItemActivity.this).load(url).into(imageView);
                    } else {
                        // An error occurred
                        Toast.makeText(EditItemActivity.this, "Error retrieving item. Please try again.", Toast.LENGTH_SHORT).show();
                        // or
                        Log.e(TAG, "Error retrieving item", task.getException());
                    }


                }
            }
        });
    }}


