package edu.fandm.engagenow;



import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Org> organizations;
    private final String TAG = "CardStackAdapter";

    public CardStackAdapter(Context context, List<Org> organizations) {
        this.inflater = LayoutInflater.from(context);
        this.organizations = organizations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_org, parent, false));
    }
    private String getEventInfo(HashMap<String, Object> eventInfo) {
        StringBuilder sb = new StringBuilder();

        sb.append("Description: " + eventInfo.get("description") + "\n");
        sb.append("Location/Start Time: " + eventInfo.get("location_start_time") + "\n");
        sb.append("Start Date: " + eventInfo.get("start_date") + "\n");
        sb.append("Time Commitment: " + eventInfo.get("time_commitment") + "\n");
        sb.append("Age Group: " + eventInfo.get("age_group") + "\n");
        sb.append("Availability: " + eventInfo.get("availability") + "\n");

        if (String.valueOf(eventInfo.get("fbi_clearance")).equals("true")) {
            sb.append("Requires FBI Clearance\n");
        }
        if (String.valueOf(eventInfo.get("child_clearance")).equals("true")) {
            sb.append("Requires Child Clearance\n");
        }
        if (String.valueOf(eventInfo.get("criminal_history")).equals("true")) {
            sb.append("Criminal History Checked\n");
        }
        if (String.valueOf(eventInfo.get("labor_skill")).equals("true")) {
            sb.append("Requires Labor\n");
        }
        if (String.valueOf(eventInfo.get("care_taking_skill")).equals("true")) {
            sb.append("Requires Care-taking\n");
        }
        if (String.valueOf(eventInfo.get("food_service_skill")).equals("true")) {
            sb.append("Can do Food Service\n");
        }

        if (String.valueOf(eventInfo.get("english")).equals("true")) {
            sb.append("Must speak English\n");
        }
        if (String.valueOf(eventInfo.get("spanish")).equals("true")) {
            sb.append("Must speak Spanish\n");
        }
        if (String.valueOf(eventInfo.get("chinese")).equals("true")) {
            sb.append("Must speak Chinese\n");
        }
        if (String.valueOf(eventInfo.get("german")).equals("true")) {
            sb.append("Must speak German\n");
        }

        if (String.valueOf(eventInfo.get("vehicle")).equals("true")) {
            sb.append("Need a Vehicle\n");
        }

        if (!String.valueOf(eventInfo.get("other_info")).equals("")) {
            sb.append("Other Info: " + eventInfo.get("other_info") + "\n");
        }

        return sb.toString();
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Org spot = organizations.get(position);
        holder.name.setText(spot.name);
        holder.descrip.setText(spot.descrip);
        try{
            File local = File.createTempFile("temp", ".jpg");
            StorageReference storageRef = FirebaseStorage.getInstance().getReference(spot.sr);
            storageRef.getFile(local).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                    Log.d("CPS", "SUCCESS");
                    Bitmap bitmap = BitmapFactory.decodeFile(local.getAbsolutePath());
                    holder.image.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    holder.image.setImageResource(R.drawable.place_holder_fore_ground);

                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                dialog.setCancelable(true);
                dialog.setTitle("Event: " + spot.name.split(" - ")[1]);
                String eventInfo = getEventInfo(spot.m);
                TextView info = new TextView(v.getContext());
                info.setText(eventInfo);
                info.setTextSize(20);
                info.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                info.setPadding(60, 5, 5, 5);
                dialog.setView(info);

                String website = (String) spot.m.get("website");
                if (website!= null) {
//                    Log.d(TAG, website);
                }
                dialog.setPositiveButton(website, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(website!=null) {
                            launchWebsite(website, v);
                        }
                    }
                });
                dialog.show();


            }
        });

    }

    private void launchWebsite(String website, View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
        Intent chooserIntent = Intent.createChooser(intent, "Open with");
        if (chooserIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
            v.getContext().startActivity(chooserIntent);
        } else {
            Toast.makeText(v.getContext(), "No browser app available", Toast.LENGTH_SHORT).show();
        }
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
