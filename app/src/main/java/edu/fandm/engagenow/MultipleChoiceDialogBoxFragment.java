package edu.fandm.engagenow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.fragment.app.DialogFragment;

import com.yuyakaido.android.cardstackview.CardStackListener;

import java.util.HashMap;

public class MultipleChoiceDialogBoxFragment extends DialogFragment {
    public interface onMultiChoiceSelector{

    }
    CardStackListener mListener;
    AlertDialog.Builder builder;
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            mListener = (CardStackListener) context;

        }
        catch (Exception e){
            throw new ClassCastException();
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Please choose search filters!\n\n");
        View holder = View.inflate(getContext(), R.layout.spinners, null);
        populateSpinner(holder);
        builder.setView(holder);
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String time_commitment = ((Spinner) holder.findViewById(R.id.spinner1)).getSelectedItem().toString();
                String age_group = ((Spinner) holder.findViewById(R.id.spinner2)).getSelectedItem().toString();
                String availability = ((Spinner) holder.findViewById(R.id.spinner3)).getSelectedItem().toString();
                boolean fbi_certification = ((CheckBox) holder.findViewById(R.id.fbi_search)).isChecked();
                boolean child_certification = ((CheckBox) holder.findViewById(R.id.child_search)).isChecked();
                boolean criminal_history = ((CheckBox) holder.findViewById(R.id.criminal_search)).isChecked();
                boolean english = ((CheckBox) holder.findViewById(R.id.english_search)).isChecked();
                boolean spanish = ((CheckBox) holder.findViewById(R.id.spanish_search)).isChecked();
                boolean german = ((CheckBox) holder.findViewById(R.id.german_search)).isChecked();
                boolean chinese = ((CheckBox) holder.findViewById(R.id.chinese_search)).isChecked();
                HashMap<String, Object> choices = new HashMap<>();
                choices.put("time_commitment", time_commitment);
                choices.put("age_group", age_group);
                choices.put("availability", availability);
                choices.put("fbi_clearance", fbi_certification);
                choices.put("criminal_history", criminal_history);
                choices.put("child_clearance", child_certification);
                choices.put("english", english);
                choices.put("spanish", spanish);
                choices.put("german", german);
                choices.put("chinese", chinese);
                Log.d("CPS", choices.toString());
                mListener.onPositiveButtonClicked(choices);
            }
        });
        return builder.create();
    }
    private void populateSpinner(View v){
        //time commitment drop down
        Spinner timeDropDown = v.findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.time_commitment_select, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        timeDropDown.setAdapter(adapter);

        //age group drop down
        Spinner ageGroupDropDown = v.findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.age_group_select, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(R.layout.custom_spinner_item);
        ageGroupDropDown.setAdapter(adapter1);

        //availability drop down
        Spinner availabilityGroupDropDown = v.findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(), R.array.availability_select, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(R.layout.custom_spinner_item);
        availabilityGroupDropDown.setAdapter(adapter2);
    }

}
