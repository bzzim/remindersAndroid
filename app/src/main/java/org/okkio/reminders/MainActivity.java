package org.okkio.reminders;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.okkio.reminders.model.Reminder;
import org.okkio.reminders.util.AppHelper;
import org.okkio.reminders.util.EmptyRecyclerView;
import org.okkio.reminders.util.ReminderItemTouchHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_REMINDER_CREATE = 100;
    private static final int REQUEST_CODE_REMINDER_EDIT = 200;

    private AppBarLayout mAppBarLayout;
    private AppHelper mAppHelper;
    private ArrayList<Reminder> mReminderArrayList;
    private ListAdapter mAdapter;
    public ItemTouchHelper mItemTouchHelper;

    private EmptyRecyclerView mRecyclerView;
    private View mContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        mContainerView = findViewById(R.id.container);
        mRecyclerView = findViewById(R.id.recycler_view);
        FloatingActionButton fab = findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toggleElevationAppbar();
        }

        mAppHelper = new AppHelper(this);
        mReminderArrayList = mAppHelper.loadReminderList();
        mAdapter = new ListAdapter(mReminderArrayList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), linearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setEmptyView(findViewById(R.id.empty_placeholder));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        ItemTouchHelper.Callback callback = new ReminderItemTouchHelper(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = FormActivity.newIntent(MainActivity.this, new Reminder());
                startActivityForResult(i, REQUEST_CODE_REMINDER_CREATE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mAppHelper.saveReminderList(mReminderArrayList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        Reminder item = (Reminder) data.getSerializableExtra(FormActivity.EXTRA_REMINDER_ITEM);
        if (requestCode == REQUEST_CODE_REMINDER_CREATE) {
            mReminderArrayList.add(0, item);
            mAdapter.notifyItemInserted(0);
            mRecyclerView.scrollToPosition(0);
        }
        if (requestCode == REQUEST_CODE_REMINDER_EDIT) {
            int position = -1;
            for (int i = 0; i < mReminderArrayList.size(); i++) {
                if (item.getIdentifier().equals(mReminderArrayList.get(i).getIdentifier())) {
                    position = i;
                    break;
                }
            }
            // hm, not found
            if (position == -1) {
                return;
            }
            if (item.isDeleted()) {
                mReminderArrayList.remove(position);
                item.setReminderDate(null);
                mAdapter.notifyItemRemoved(position);
            } else {
                mReminderArrayList.set(position, item);
                mAdapter.notifyItemChanged(position);
            }
        }
        mAppHelper.toggleReminderAlarm(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void toggleElevationAppbar() {
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mAppBarLayout.setElevation(0);
        // get initial position
        final int initialTopPosition = mRecyclerView.getTop();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ValueAnimator animator = null;
                float elevation = mAppBarLayout.getElevation();
                if (mRecyclerView.getChildAt(0).getTop() < initialTopPosition) {
                    if(elevation < 10) {
                        animator = ValueAnimator.ofFloat(elevation, 10);
                    }
                } else {
                    if (mAppBarLayout.getElevation() != 0) {
                        animator = ValueAnimator.ofFloat(elevation, 0);
                    }
                }
                if (animator != null) {
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mAppBarLayout.setElevation((Float) animation.getAnimatedValue());
                        }
                    });
                    animator.start();
                }
            }
        });
    }

    //region ListAdapter
    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements ReminderItemTouchHelper.ItemTouchHelperAdapter {
        private ArrayList<Reminder> items;

        ListAdapter(ArrayList<Reminder> items) {
            this.items = items;
        }

        @Override
        public void onItemMoved(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(items, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(items, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemRemoved(final int position) {
            final Reminder item = items.remove(position);
            mAppHelper.toggleReminderAlarm(item);
            notifyItemRemoved(position);

            String deletedText = getResources().getString(R.string.reminder_deleted);
            String undoText = getResources().getString(R.string.undo);
            Snackbar.make(mContainerView, deletedText, Snackbar.LENGTH_LONG)
                    .setAction(undoText, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            items.add(position, item);
                            mAppHelper.toggleReminderAlarm(item);
                            notifyItemInserted(position);
                        }
                    }).show();
        }

        @NonNull
        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ListAdapter.ViewHolder holder, final int position) {
            Reminder item = items.get(position);
            if (item.getReminderDate() != null) {
                holder.mTitleView.setMaxLines(1);
                holder.mDateView.setVisibility(View.VISIBLE);
            } else {
                holder.mDateView.setVisibility(View.GONE);
                holder.mTitleView.setMaxLines(2);
            }
            holder.mTitleView.setText(item.getTitle());

            if (item.getReminderDate() != null) {
                holder.mDateView.setText(mAppHelper.getRelativeTimeSpan(item.getReminderDate().getTime()));
                if(item.getReminderDate().getTime() < new Date().getTime()) {
                    holder.mDateView.setTextColor(Color.RED);
                }
            }
            holder.mDoneView.setChecked(item.isDone());
            if (item.isDone()) {
                holder.mTitleView.setPaintFlags(holder.mTitleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.mTitleView.setAlpha(0.5f);
                holder.mDateView.setAlpha(0.5f);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mTitleView;
            TextView mDateView;
            CheckBox mDoneView;

            ViewHolder(View v) {
                super(v);
                mTitleView = v.findViewById(R.id.title);
                mDateView = v.findViewById(R.id.date);
                mDoneView = v.findViewById(R.id.done);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reminder item = items.get(ViewHolder.this.getAdapterPosition());
                        Intent i = FormActivity.newIntent(MainActivity.this, item);
                        startActivityForResult(i, REQUEST_CODE_REMINDER_EDIT);
                    }
                });
                mDoneView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Reminder item = items.get(ViewHolder.this.getAdapterPosition());
                        item.setDone(isChecked);
                        mAppHelper.toggleReminderAlarm(item);
                        if (isChecked) {
                            mTitleView.setAlpha(0.5f);
                            mDateView.setAlpha(0.5f);
                            mTitleView.setPaintFlags(mTitleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else {
                            mTitleView.setAlpha(1f);
                            mDateView.setAlpha(1f);
                            mTitleView.setPaintFlags(mTitleView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                    }
                });
            }
        }
    }
    //endregion
}
