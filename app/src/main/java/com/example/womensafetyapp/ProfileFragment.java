package com.example.womensafetyapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.util.Base64;
import java.util.Objects;

public class ProfileFragment extends Fragment{

    View view;
    EditText etName, etEmail, etPhone, etAddress, etDob, etBloodGroup;
    MaterialButton btEditProfile;
    ImageView ivProfilePhoto,ivSelectImage;
    Uri imgPath;
    SharedPreferences profileData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        etDob = view.findViewById(R.id.etDob);
        etBloodGroup = view.findViewById(R.id.etBloodGroup);
        btEditProfile = view.findViewById(R.id.btEditProfile);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePicture);
        ivSelectImage = view.findViewById(R.id.ivSelectImage);

        profileData = getActivity().getSharedPreferences("profile", Context.MODE_PRIVATE);
        String name = profileData.getString("name","Name");
        String email = profileData.getString("email","Email");
        String phone = profileData.getString("phone","Phone");
        String address = profileData.getString("address","Address");
        String dob = profileData.getString("dob","Date of Birth");
        String bloodgroup = profileData.getString("bloodgroup","Blood Group");
        String myImg = profileData.getString("imgpath","");
        imgPath = Uri.parse(myImg);

        // Checking if SharedPreference has data or not
        if (!name.equals("Name")){
            etName.setText(name);
            etEmail.setText(email);
            etPhone.setText(phone);
            etAddress.setText(address);
            etDob.setText(dob);
            etBloodGroup.setText(bloodgroup);
            ivProfilePhoto.setImageURI(imgPath);
        }

        btEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btEditProfile.getText().toString().equals("Edit Profile")) {
                    etName.setEnabled(true);
                    etEmail.setEnabled(true);
                    etPhone.setEnabled(true);
                    etAddress.setEnabled(true);
                    etDob.setEnabled(true);
                    etBloodGroup.setEnabled(true);
                    btEditProfile.setText("Save Changes");
                    btEditProfile.setIconResource(R.drawable.ic_save_changes);
                    ivSelectImage.setAlpha(0.99f);
                }

                else{
                    etName.setEnabled(false);
                    etEmail.setEnabled(false);
                    etPhone.setEnabled(false);
                    etAddress.setEnabled(false);
                    etDob.setEnabled(false);
                    etBloodGroup.setEnabled(false);
                    btEditProfile.setText("Edit Profile");
                    btEditProfile.setIconResource(R.drawable.ic_edit_profile);
                    ivSelectImage.setAlpha(0.0f);


                    // Storing Data in Shared Preference so data is not lost when app is closed
                    profileData = getActivity().getSharedPreferences("profile", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit =profileData.edit();

                    edit.putString("name",etName.getText().toString());
                    edit.putString("email",etEmail.getText().toString());
                    edit.putString("phone",etPhone.getText().toString());
                    edit.putString("address",etAddress.getText().toString());
                    edit.putString("dob",etDob.getText().toString());
                    edit.putString("bloodgroup",etBloodGroup.getText().toString());
                    edit.putString("imgpath",imgPath.toString());

                    edit.commit();

                    Toast.makeText(getActivity().getApplicationContext(), "Changes Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(getPhoto);
            }
        });

        return view;
    }
//    @Override
//    protected void onActivityResult(int reqCode, int resCode, Intent data){
//        if (reqCode==303 && resCode==RESULT_OK){
//            imgPath = data.getData();
//            ivProfilePhoto.setImageURI(imgPath);
//        }
//    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imgPath = result.getData().getData();
                    Bitmap bitmap = getBitmapFromUri(imgPath);
                    imgPath = getUriFromBitmap(getContext(),bitmap);
                    ivProfilePhoto.setImageURI(imgPath);
                }
            });


    public Bitmap getBitmapFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = getActivity().getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Uri getUriFromBitmap(Context context, Bitmap bitmap) {
        try {
            // Create a temporary file in the cache directory
            File file = new File(context.getCacheDir(), "temp_image.png");
            FileOutputStream outStream = new FileOutputStream(file);

            // Compress the bitmap and write to the output stream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

            // Return the Uri of the file
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
