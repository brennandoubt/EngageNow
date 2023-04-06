package edu.fandm.engagenow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DiffUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolunteerSwiping extends VolunteerBaseClass implements CardStackListener {
    private DrawerLayout drawerLayout;

    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private CardStackView cardStackView;
    List<Org> orgs = new ArrayList<>();
    boolean swipedRight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_swiping);
        setupCardStackView();
        setupButton();
    }

//    commented this out so app does not crash on back swipe because the app should just close since the user can't go back to the login screen
//    @Override
//    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawers();
//        } else {
//            super.onBackPressed();
//        }
//    }
    //Adds to database after swiping
    public void addToDatabase(int position){
        if(position < orgs.size()){
            // store in the potentialMatches folder at the organization id
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().getRoot().child("potentialMatches").child(orgs.get(position).userID);
            Map<String, Object> m = new HashMap<>();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            m.put(auth.getCurrentUser().getUid(), auth.getCurrentUser().getUid());
            dbr.updateChildren(m);
        }

    }


    @Override
    public void onCardDragging(Direction direction, float ratio) {
        Log.d("CardStackView", "onCardDragging: d = " + direction.name() + ", r = " + ratio);
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if(direction.equals(Direction.Right)){
            addToDatabase(manager.getTopPosition() - 1);
        }

        Log.d("CardStackView", "onCardSwiped: p = " + manager.getTopPosition() + ", d = " + direction);
        if (manager.getTopPosition() == adapter.getItemCount() - 5) {
            paginate();
        }

    }

    @Override
    public void onCardRewound() {
        Log.d("CardStackView", "onCardRewound: " + manager.getTopPosition());
    }

    @Override
    public void onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled:" + manager.getTopPosition());
    }

    @Override
    public void onCardAppeared(View view, int position) {
        TextView textView = view.findViewById(R.id.item_name);
        Log.d("CardStackView", "onCardAppeared: (" + position + ") " + textView.getText());
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        TextView textView = view.findViewById(R.id.item_name);
        Log.d("CardStackView", "onCardDisappeared: (" + position + ") " + textView.getText());
    }


    private void setupCardStackView() {
        initialize();
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
    }

    private void initialize() {
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
        adapter = new CardStackAdapter(this, createOrgs());
        cardStackView = findViewById(R.id.card_stack_view);
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
    }

    private void paginate() {
        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = new ArrayList<Org>() {{
            addAll(adapter.getOrgs());
            addAll(createOrgs());
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void reload() {
        List<Org> oldList = adapter.getOrgs();
        List<Org> newList = createOrgs();
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setOrgs(newList);
        result.dispatchUpdatesTo(adapter);
    }

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

    private List<Org> createOrgs() {
        orgs.add(new Org("Claire House", "Add description", "", "test1"));
        orgs.add(new Org("YMCA", "Add description", "", "test2"));
        orgs.add(new Org("Red Cross", "Add description", "", "test3"));
        orgs.add(new Org("Ware institute", "Add description", "", "test4"));
        orgs.add(new Org("Canine Search and Rescue", "New York", "", "test5"));
        return orgs;
    }


}