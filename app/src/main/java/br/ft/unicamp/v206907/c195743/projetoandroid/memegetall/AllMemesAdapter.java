package br.ft.unicamp.v206907.c195743.projetoandroid.memegetall;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.ft.unicamp.v206907.c195743.projetoandroid.R;
import br.ft.unicamp.v206907.c195743.projetoandroid.services.Payload;

public class AllMemesAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private List<Payload> mPayloads;
    private OnItemClickListener mListener;

    public AllMemesAdapter(Context context, List<Payload> payloads) {
        this.mContext = context;
        this.mPayloads = payloads;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.get_all_meme_recycler_view, parent, false);
        return new MemesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Payload payloadCurrent = mPayloads.get(position);
        ((MemesViewHolder) holder).bind(payloadCurrent);
    }

    @Override
    public int getItemCount() {
        return mPayloads.size();
    }

    public class MemesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        private TextView name;
        private TextView description;
        private TextView tag;
        private ImageView meme;

        public MemesViewHolder(@NonNull View itemView) {
            super(itemView);

            initialize();

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem share = menu.add(Menu.NONE, 1, 1, "Share").setIcon(R.drawable.ic_search);
            MenuItem edit = menu.add(Menu.NONE, 2, 2, "Edit");
            MenuItem delete = menu.add(Menu.NONE, 3, 3, "Delete");

            share.setOnMenuItemClickListener(this);
            edit.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (item.getItemId()) {
                        case 1:
                            mListener.onShareClick(position);
                            return true;
                        case 2:
                            mListener.onEditClick(position);
                            return true;
                        case 3:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }

        void initialize() {
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            tag = itemView.findViewById(R.id.tag);
            meme = itemView.findViewById(R.id.meme);
        }

        void bind(Payload payload) {
            this.name.setText(payload.getName());
            this.description.setText(payload.getDescription());
            String hashtag = itemView.getResources().getString(R.string.hashtag, payload.getTag());
            this.tag.setText(hashtag);
            Picasso.get().load(payload.getUri()).placeholder(R.mipmap.ic_launcher).into(this.meme);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onShareClick(int position);

        void onDeleteClick(int position);

        void onEditClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}