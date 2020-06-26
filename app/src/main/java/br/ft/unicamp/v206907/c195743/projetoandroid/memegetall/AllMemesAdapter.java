package br.ft.unicamp.v206907.c195743.projetoandroid.memegetall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import br.ft.unicamp.v206907.c195743.projetoandroid.R;
import br.ft.unicamp.v206907.c195743.services.Payload;

public class AllMemesAdapter extends RecyclerView.Adapter implements Filterable {

    private Context mContext;
    private List<Payload> mPayloads;
    private List<Payload> mPayloadsFull;
    private OnItemClickListener mListener;

    public AllMemesAdapter(Context context, List<Payload> payloads) {
        this.mContext = context;
        this.mPayloads = payloads;
        mPayloadsFull = new ArrayList<>(payloads);
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

    @Override
    public Filter getFilter() {
        return allMemesFilter;
    }

    private Filter allMemesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Payload> filteredPayload = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredPayload.addAll(mPayloadsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Payload item : mPayloadsFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getTag().toLowerCase().contains(filterPattern)) {
                        filteredPayload.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredPayload;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mPayloads.clear();
            mPayloads.add((Payload) results.values);
            notifyDataSetChanged();
        }
    };

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
            MenuItem share = menu.add(Menu.NONE, 1, 1, "Share");
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
            this.name.setText("Nome: " + payload.getName());
            this.description.setText("Descrição: " + payload.getDescription());
            this.tag.setText("Tag: " + payload.getTag());
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