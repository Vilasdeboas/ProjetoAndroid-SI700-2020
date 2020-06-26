package br.ft.unicamp.v206907.c195743.projetoandroid.memegetall;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ft.unicamp.v206907.c195743.projetoandroid.R;
import br.ft.unicamp.v206907.c195743.services.Payload;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetAllMemeFragment extends Fragment implements AllMemesAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private View lview;
    private AllMemesAdapter mAdapter;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private StorageReference mStorageReference;
    private List<Payload> mPayloads;
    private ProgressBar mProgressCircle;
    private String BASE_URL = "projeto_final/meme_inc";

    public GetAllMemeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (lview == null) {
            lview = inflater.inflate(R.layout.fragment_get_all_meme, container, false);
        }

        mStorageReference = FirebaseStorage.getInstance().getReference(BASE_URL);

        mRecyclerView = lview.findViewById(R.id.get_all_meme_recycler_view);
        mProgressCircle = lview.findViewById(R.id.progress_circle);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mPayloads = new ArrayList<>();

        mAdapter = new AllMemesAdapter(getContext(), mPayloads);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(GetAllMemeFragment.this);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference(BASE_URL);
        mStorage = FirebaseStorage.getInstance();

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mPayloads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Payload payload = postSnapshot.getValue(Payload.class);
                    payload.setKey(postSnapshot.getKey());
                    mPayloads.add(payload);
                }

                mAdapter.notifyDataSetChanged();

                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Permissão negada", Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });

        return lview;
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getContext(), "Normal click at position " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareClick(int position) {
        String uri = mPayloads.get(position).getUri();
        final String extension = mPayloads.get(position).getExtension();
        try {
            StorageReference storageRef = mStorage.getReferenceFromUrl(uri);
            final File localFile = File.createTempFile("temp_meme", "." + extension);
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getTask().addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            String packagename = getContext().getApplicationContext().getPackageName();
                            Uri photoUri = FileProvider.getUriForFile(getContext(), packagename, localFile);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, photoUri);
                            intent.setType("image/" + extension);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "Meme Inc. Share"));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Log.i("file-err", e.getMessage());
                            Toast.makeText(getContext(), "Falhou!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Falha", Toast.LENGTH_SHORT).show();
                    Log.i("file-err", e.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEditClick(int position) {
        Bundle bundle = new Bundle();
        String key = mPayloads.get(position).getKey();
        if (key != null && !key.equals("")) {
            bundle.putString("key", key);
            Navigation.findNavController(lview).navigate(R.id.editMemeFragment, bundle);
        } else {
            Toast.makeText(getContext(), "A chave está vazia", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(int position) {
        Payload selectedItem = mPayloads.get(position);
        final String selectedItemKey = selectedItem.getKey();
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getUri());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedItemKey).removeValue();
                Toast.makeText(getContext(), "Item deletado", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Não foi possível apagar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mDatabaseRef.removeEventListener(mDBListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
