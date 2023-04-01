package edu.fandm.engagenow;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Org> organizations;

    public CardStackAdapter(Context context, List<Org> organizations) {
        this.inflater = LayoutInflater.from(context);
        this.organizations = organizations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_org, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Org spot = organizations.get(position);
        holder.name.setText(spot.name);
        holder.descrip.setText(spot.descrip);
        holder.image.setImageResource(R.drawable.place_holder_fore_ground);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), spot.name, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return organizations.size();
    }

    public List<Org> getOrgs() {
        return organizations;
    }

    public void setOrgs(List<Org> organizations) {
        this.organizations = organizations;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView descrip;
        ImageView image;
        ViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.item_name);
            this.descrip = view.findViewById(R.id.item_descrip);
            this.image = view.findViewById(R.id.item_image);
        }
    }

}
