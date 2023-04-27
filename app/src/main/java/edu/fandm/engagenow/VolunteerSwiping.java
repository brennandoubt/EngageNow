package edu.fandm.engagenow;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VolunteerSwiping extends VolunteerBaseClass implements CardStackListener{

    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private CardStackView cardStackView;
    List<Org> orgs = new ArrayList<>();
    boolean go_back = false;
    int unsortedIdx = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_swiping);
        setTitle("Volunteer Events");
        setupButton();
        initialize();

    }

    //Adds to database after swiping
    public void addToDatabase(int position){
        if(position < orgs.size()){
            // store in the potentialMatches folder at the organization id
            FirebaseAuth auth = FirebaseAuth.getInstance();
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(orgs.get(position).userID).child(auth.getCurrentUser().getUid());
            Map<String, Object> m = new HashMap<>();
            //True meaning they are a potential match
            m.put(orgs.get(position).event, true);
            dbr.updateChildren(m);
        }

    }

    public void deleteFromDatabase(int position){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference  dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(orgs.get(position).userID).
                child(auth.getCurrentUser().getUid()).child(orgs.get(position).event);
        dbr.removeValue();

    }

    @Override
    public void  onPositiveButtonClicked(HashMap<String, Object> choices){
        ArrayList<Org> unsortedList = new ArrayList<>();
        ArrayList<Org> sorted = new ArrayList<>();
        ArrayList<String> langs = new ArrayList<String>(){{add("english"); add("spanish"); add("german"); add("chinese");}};
        ArrayList<String> clearances = new ArrayList<String>(){{add("child_clearance"); add("fbi_clearance"); add("criminal_history");}};
        for(Org o : orgs){
            boolean match = false;
            HashMap<String, Object> eventInfo = o.m;
            for(Map.Entry<String, Object> entry: choices.entrySet()) {
                if (choices.get(entry.getKey()).equals("Select Time Commitment") || choices.get(entry.getKey()).equals("Select Age")
                        || choices.get(entry.getKey()).equals("Select Availability")) {
                    continue;
                }
                if(langs.contains(entry.getKey()) && choices.get(entry.getKey()).equals(true)
                        && choices.get(entry.getKey()).equals(eventInfo.get(entry.getKey())) && !sorted.contains(o)){
                    sorted.add(o);
                    match = true;
                }
                if(clearances.contains(entry.getKey()) && choices.get(entry.getKey()).equals(true)
                        && choices.get(entry.getKey()).equals(eventInfo.get(entry.getKey())) && !sorted.contains(o)){
                    sorted.add(o);
                    match = true;
                }
                if (!langs.contains(entry.getKey()) && !clearances.contains(entry.getKey()) &&
                        choices.get(entry.getKey()).equals(eventInfo.get(entry.getKey())) && !sorted.contains(o)) {
                    sorted.add(o);
                    match = true;
                }
            }
            if(!match && !unsortedList.contains(o)){
                unsortedList.add(o);
            }

        }
        unsortedIdx = sorted.size();
        //Log.d("CPS", String.valueOf(unsortedIdx));
        sorted.addAll(unsortedList);
        orgs = sorted;
        CardStackCreator();
    }

    @Override
    public void onNegativeButtonClicked(){}

    @Override
    public void onCardDragging(Direction direction, float ratio) {
//        Log.d("CardStackView", "onCardDragging: d = " + direction.name() + ", r = " + ratio);
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if(direction.equals(Direction.Right)){
            addToDatabase(manager.getTopPosition() - 1);
        }
        //        Log.d("CardStackView", "onCardSwiped: p = " + manager.getTopPosition() + ", d = " + direction);
        if (manager.getTopPosition() == adapter.getItemCount() - 5) {
            paginate();
        }

    }
    public void onCardClicked(View view, int position){

    }


    @Override
    public void onCardRewound() {
        deleteFromDatabase(manager.getTopPosition());
//        Log.d("CardStackView", "onCardRewound: " + manager.getTopPosition());
    }

    @Override
    public void onCardCanceled() {
//        Log.d("CardStackView", "onCardCanceled:" + manager.getTopPosition());
    }

    @Override
    public void onCardAppeared(View view, int position) {
        if(unsortedIdx != -1 && unsortedIdx == position && !go_back){
            Toast.makeText(this.getApplicationContext(),"It seems like there are no more events matching your preferences!", Toast.LENGTH_SHORT).show();
            unsortedIdx= -1;
        }
        TextView textView = view.findViewById(R.id.item_name);
//        Log.d("CardStackView", "onCardAppeared: (" + position + ") " + textView.getText());
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        TextView textView = view.findViewById(R.id.item_name);
//        Log.d("CardStackView", "onCardDisappeared: (" + position + ") " + textView.getText());
        if(position == orgs.size() - 1 && !go_back) {
            Toast.makeText(this.getApplicationContext(), "It seems like there are no more events matching your preferences!", Toast.LENGTH_SHORT).show();
        }
        else{
            go_back = false;
        }
      //  Log.d("CardStackView", "onCardDisappeared: (" + position + ") " + textView.getText());

    }


    private void setupButton() {
        View skip = findViewById(R.id.skip_button);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Left)
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .build();
                manager.setSwipeAnimationSetting(setting);
                cardStackView.swipe();
            }
        });

        View rewind = findViewById(R.id.rewind_button);
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RewindAnimationSetting setting = new RewindAnimationSetting.Builder()
                        .setDirection(Direction.Bottom)
                        .setDuration(200)
                        .setInterpolator(new DecelerateInterpolator())
                        .build();
                manager.setRewindAnimationSetting(setting);
                cardStackView.rewind();
                go_back = true;
            }
        });

        View like = findViewById(R.id.like_button);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .build();
                manager.setSwipeAnimationSetting(setting);
                cardStackView.swipe();
                addToDatabase(manager.getTopPosition());
            }
        });
        View search = findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultipleChoiceDialogBoxFragment d = new MultipleChoiceDialogBoxFragment();
                d.show(getSupportFragmentManager(), "CPS");
            }
        });
    }
//Fetch data from database

    private void initialize() {

        DatabaseReference organizationsRef = FirebaseDatabase.getInstance().getReference("organization_accounts");

        organizationsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator i = snapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot OrgId = (DataSnapshot) i.next();
//                    Log.d("CPS", OrgId.toString());
//
                   FirebaseAuth auth = FirebaseAuth.getInstance();
                   String userID = auth.getCurrentUser().getUid();
                   //dbr = root -> potentialMatches -> Org_id -> volunteer_id
                   DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(OrgId.getKey()).child(userID);
                   dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                            HashMap<String, Object> map = (HashMap<String, Object>) OrgId.getValue();
                            if (map.containsKey("events")) {
                                HashMap<String, HashMap<String, Object>> events = (HashMap<String, HashMap<String, Object>>) map.get("events");
                                for (Map.Entry<String, HashMap<String, Object>> entry : events.entrySet()) {
                                    //root -> potentialMatches -> Org_id -> volunteer_id -> event_name doesn't exist == has not swiped before
                                    if(!snapshot.hasChild(entry.getKey())) {
//                                        Log.d("CPS", "images/" + OrgId.getKey());

                                        orgs.add(new Org(map.get("name") + " - " + entry.getKey(), events.get(entry.getKey()).get("description").toString(), "images/" + OrgId.getKey(), OrgId.getKey(),
                                                events.get(entry.getKey()), entry.getKey()));
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
       CardStackCreator();
    }
    public void CardStackCreator(){
        manager = new CardStackLayoutManager(this, this);
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(true);
        adapter = new CardStackAdapter(this, orgs);
        cardStackView = findViewById(R.id.card_stack_view);
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
    }

    private void paginate() {
        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = new ArrayList<Org>() {{
            addAll(adapter.getOrgs());
            //addAll(createOrgs());
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }
    /**
    private void reload() {
        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = createOrgs();
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }
     */

    private void addFirst(final int size) {
        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = new ArrayList<Org>() {{
            addAll(adapter.getOrgs());
            for (int i = 0; i < size; i++) {
                add(manager.getTopPosition(), createOrg());
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void addLast(final int size) {
        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = new ArrayList<Org>() {{
            addAll(adapter.getOrgs());
            for (int i = 0; i < size; i++) {
                add(createOrg());
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeFirst(final int size) {
        if (adapter.getOrgs().isEmpty()) {
            return;
        }

        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = new ArrayList<Org>() {{
            addAll(adapter.getOrgs());
            for (int i = 0; i < size; i++) {
                remove(manager.getTopPosition());
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeLast(final int size) {
        if (adapter.getOrgs().isEmpty()) {
            return;
        }

        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = new ArrayList<Org>() {{
            addAll(adapter.getOrgs());
            for (int i = 0; i < size; i++) {
                remove(size() - 1);
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private Org createOrg() {
        return null;
    }



}