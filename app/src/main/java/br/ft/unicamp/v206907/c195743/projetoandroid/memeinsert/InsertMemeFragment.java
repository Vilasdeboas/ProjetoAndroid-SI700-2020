package br.ft.unicamp.v206907.c195743.projetoandroid.memeinsert;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import br.ft.unicamp.v206907.c195743.projetoandroid.R;
import br.ft.unicamp.v206907.c195743.services.Payload;
import br.ft.unicamp.v206907.c195743.services.SignInActivity;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class InsertMemeFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private String BASE_URL = "projeto_final/meme_inc";
    private View lview;
    private TextView selected_meme_text;
    private EditText name;
    private EditText description;
    private EditText tag;
    private Button botao;
    private Button select_meme;
    private ImageView selected_meme;
    private Uri selected_meme_uri;
    private DatabaseReference mFirebaseDatabaseReference;
    private StorageReference mStorageReference;
    private StorageTask mUploadTask;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    public InsertMemeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (lview == null) {
            lview = inflater.inflate(R.layout.fragment_insert_meme, container, false);
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            startActivity(new Intent(getContext(), SignInActivity.class));
        }

        initialize();

        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_text = name.getText().toString().trim();
                String tag_text = tag.getText().toString().trim();
                String description_text = description.getText().toString().trim();
                if (name_text.equals("") || tag_text.equals("")) {
                    Toast.makeText(getContext(), "Nome e tag não podem ser vazios", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (mUploadTask != null && mUploadTask.isInProgress()) {
                        Toast.makeText(v.getContext(), "Upload está em progresso!", Toast.LENGTH_SHORT).show();
                    } else {
                        insertMeme(name_text, description_text, tag_text, System.currentTimeMillis());
                    }
                }
            }
        });

        select_meme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMeme();
            }
        });

        return lview;
    }

    private void initialize() {
        name = lview.findViewById(R.id.name);
        description = lview.findViewById(R.id.description);
        tag = lview.findViewById(R.id.tag);
        selected_meme = lview.findViewById(R.id.selected_meme);
        botao = lview.findViewById(R.id.btn_save);
        select_meme = lview.findViewById(R.id.select_meme);
        selected_meme_text = lview.findViewById(R.id.selected_meme_text);
        mStorageReference = FirebaseStorage.getInstance().getReference(BASE_URL + "/" + mFirebaseUser.getUid());
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(BASE_URL + "/" + mFirebaseUser.getUid());
    }

    private void clearLayoutItems() {
        name.setText("");
        description.setText("");
        tag.setText("");
        selected_meme.setImageResource(0);
        selected_meme.setBackgroundResource(0);
        selected_meme_text.setVisibility(View.INVISIBLE);
    }

    private void insertMeme(final String name, final String description, final String tag, final long mili) {

        if (selected_meme_uri != null) {

            final String extension = getFileExtension(selected_meme_uri);
            String filename = name.trim().replace(" ", "-").toLowerCase();
            filename = filename + "-" + mili + "." + extension;
            final StorageReference fileReference = mStorageReference.child("uploads/" + "/" + mFirebaseUser.getUid() + filename);
            mUploadTask = fileReference.putFile(selected_meme_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Payload payload = new Payload(name, description, tag, uri.toString(), extension);
                                    mFirebaseDatabaseReference.child(BASE_URL + "/" + mFirebaseUser.getUid()).push().setValue(payload);
                                    Toast.makeText(getContext(), "Enviado!", Toast.LENGTH_SHORT).show();
                                    clearLayoutItems();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Selecione uma foto antes!", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectMeme() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selected_meme_uri = data.getData();

            Picasso.get().load(selected_meme_uri).into(selected_meme);
            selected_meme.setBackgroundResource(R.drawable.generic_border);
            selected_meme_text.setVisibility(View.VISIBLE);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }
}
