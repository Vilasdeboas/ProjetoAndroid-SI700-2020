package br.ft.unicamp.v206907.c195743.memeedit;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import br.ft.unicamp.v206907.c195743.projetoandroid.R;
import br.ft.unicamp.v206907.c195743.services.Payload;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditMemeFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private View lview;
    private TextView selected_meme_text;
    private EditText name;
    private EditText description;
    private EditText tag;
    private Button update;
    private Button select_meme;
    private ImageView selected_meme;
    private String mKey;
    private String mUri;
    private Uri selected_meme_uri;
    private DatabaseReference dbRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private String extension;
    private UploadTask uploadTask;
    private String BASE_URL = "projeto_final/meme_inc";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    public EditMemeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (lview == null) {
            lview = inflater.inflate(R.layout.fragment_edit_meme, container, false);
        }

        initialize();

        if (mKey != null && !mKey.equals("")) {
            getInfo();
        } else {
            Toast.makeText(getContext(), "Chave vazia", Toast.LENGTH_SHORT).show();
        }

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_meme_uri == null) {
                    updateInfo(name.getText().toString(), description.getText().toString(),
                            tag.getText().toString(), mUri, extension);
                } else {
                    updateInfo(name.getText().toString(), description.getText().toString(),
                            tag.getText().toString(), mUri, getFileExtension(selected_meme_uri));
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

    private void getInfo() {
        dbRef.child(mKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Payload payload = dataSnapshot.getValue(Payload.class);
                    name.setText(payload.getName());
                    description.setText(payload.getDescription());
                    tag.setText(payload.getTag());
                    mUri = payload.getUri();
                    extension = payload.getExtension();
                    Picasso.get().load(payload.getUri()).placeholder(R.mipmap.ic_launcher).into(selected_meme);
                    selected_meme.setBackgroundResource(R.drawable.generic_border);
                    selected_meme_text.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Err.: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(lview).navigate(R.id.nav_get_all);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Zuo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInfo(String name, String description, String tag, String uri, String extension) {
        String filename = name.trim().replace(" ", "-").toLowerCase()
                + "-" + System.currentTimeMillis() + "." + extension;
        Payload payload = new Payload(name, description, tag, uri, extension);
        try {
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload ainda em andamento", Toast.LENGTH_SHORT).show();
            } else {
                if (selected_meme_uri != null) {
                    insertNewImage(filename, payload);
                    removeOldImage(payload.getUri());
                } else {
                    dbRef.child(mKey).setValue(payload).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Atualizado!", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                                    navController.navigate(R.id.nav_get_all);
                                }
                            }, 1000);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Err.: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void insertNewImage(String filename, final Payload payload) {
        final StorageReference fileReference = mStorageReference.child("uploads"+"/"+mFirebaseUser.getUid()).child(filename);
        uploadTask = fileReference.putFile(selected_meme_uri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        payload.setUri(uri.toString());
                        dbRef.child(mKey).setValue(payload);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),
                        "Não funcionou. Erro: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeOldImage(String uri) {
        StorageReference imageRef = mFirebaseStorage.getReferenceFromUrl(uri);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "Alterado!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Não foi possível apagar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initialize() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mKey = getArguments().getString("key");
        dbRef = FirebaseDatabase.getInstance().getReference(BASE_URL+"/"+mFirebaseUser.getUid());
        mStorageReference = FirebaseStorage.getInstance().getReference(BASE_URL+"/"+mFirebaseUser.getUid());
        mFirebaseStorage = FirebaseStorage.getInstance();
        selected_meme_text = lview.findViewById(R.id.selected_meme_text);
        name = lview.findViewById(R.id.name);
        description = lview.findViewById(R.id.description);
        tag = lview.findViewById(R.id.tag);
        selected_meme = lview.findViewById(R.id.selected_meme);
        update = lview.findViewById(R.id.btn_update);
        select_meme = lview.findViewById(R.id.select_meme);
        selected_meme_uri = null;
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
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
