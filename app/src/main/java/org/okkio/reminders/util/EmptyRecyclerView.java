package org.okkio.reminders.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class EmptyRecyclerView extends RecyclerView {
    private View mEmptyView;

    private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            showEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            showEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            showEmptyView();
        }
    };

    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public void showEmptyView() {
        Adapter<?> adapter = getAdapter();
        if (adapter != null && mEmptyView != null) {
            if (adapter.getItemCount() == 0) {
                mEmptyView.setVisibility(VISIBLE);
                EmptyRecyclerView.this.setVisibility(GONE);
            } else {
                mEmptyView.setVisibility(GONE);
                EmptyRecyclerView.this.setVisibility(VISIBLE);
            }
        }
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
            observer.onChanged();
        }
    }

    public void setEmptyView(View v) {
        mEmptyView = v;
    }
}
