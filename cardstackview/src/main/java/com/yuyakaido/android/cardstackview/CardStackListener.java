package com.yuyakaido.android.cardstackview;

import android.view.View;

import java.util.HashMap;

public interface CardStackListener {

    void onCardDragging(Direction direction, float ratio);
    void onCardSwiped(Direction direction);
    void onCardRewound();
    void onCardCanceled();
    void onCardAppeared(View view, int position);
    void onCardDisappeared(View view, int position);
    void onPositiveButtonClicked(HashMap<String, Object> choices);
    void onNegativeButtonClicked();

    CardStackListener DEFAULT = new CardStackListener() {
        @Override
        public void onCardDragging(Direction direction, float ratio) {}
        @Override
        public void onCardSwiped(Direction direction){}
        @Override
        public void onCardRewound() {}
        @Override
        public void onCardCanceled() {}
        @Override
        public void onCardAppeared(View view, int position) {}
        @Override
        public void onCardDisappeared(View view, int position) {}

        @Override
        public void onPositiveButtonClicked(HashMap<String, Object> choices) {

        }

        @Override
        public void onNegativeButtonClicked() {

        }

        @Override
        public void onCardClicked(View view, int position){}



    };

    void onCardClicked(View view, int position);
}
