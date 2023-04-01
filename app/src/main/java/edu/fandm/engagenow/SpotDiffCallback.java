package edu.fandm.engagenow;


import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class SpotDiffCallback extends DiffUtil.Callback {

    private final List<Org> oldList;
    private final List<Org> newList;

    public SpotDiffCallback(List<Org> oldList, List<Org> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldPosition, int newPosition) {
        return oldList.get(oldPosition).id == newList.get(newPosition).id;
    }

    @Override
    public boolean areContentsTheSame(int oldPosition, int newPosition) {
        Org oldSpot = oldList.get(oldPosition);
        Org newSpot = newList.get(newPosition);
        return oldSpot.name.equals(newSpot.name)
                && oldSpot.descrip.equals(newSpot.descrip)
                && oldSpot.url.equals(newSpot.url);
    }

}