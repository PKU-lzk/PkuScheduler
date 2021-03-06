package com.engrave.pkuscheduler.Fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.engrave.pkuscheduler.Activities.MainActivity;
import com.engrave.pkuscheduler.Components.EmptySpecifiedRecyclerView;
import com.engrave.pkuscheduler.Components.ItemTouchHelperClass;
import com.engrave.pkuscheduler.Components.ToDoItemRecyclerViewAdapter;
import com.engrave.pkuscheduler.Models.CourseDeadlineJsonModel.CourseRawToDoItemsRootObject;
import com.engrave.pkuscheduler.Models.CourseLoginInfoModel;
import com.engrave.pkuscheduler.R;
import com.engrave.pkuscheduler.ViewModels.ToDoItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.engrave.pkuscheduler.ViewModels.ToDoItem.saveListInstance;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private View mView;
    private boolean isAltered = false;
    public List<ToDoItem> toDoItems =new ArrayList<>();
    private ToDoItemRecyclerViewAdapter adapter;
    private EmptySpecifiedRecyclerView mRecyclerView;
    private FetchScheduleInfoFromStorage fetchScheduleInfoFromStorage;
    private UpdateScheduleInfoFromWebApi updateScheduleInfoFromWebApi;
    private CourseLoginInfoModel courseLoginInfoModel;
    private final Long MILLISECONDS_OF_A_WEEK = Long.valueOf(604800000);
    public ItemTouchHelper itemTouchHelper;
    private MainActivity mMainActivity;

    public void addTodoItem(ToDoItem toDoItem){
        this.toDoItems.add(toDoItem);
        adapter.notifyDataSetChanged();
        sendBroadcastDataChanged();
    }

    public ScheduleListFragment() {
    }

    public static ScheduleListFragment newInstance() {
        ScheduleListFragment fragment = new ScheduleListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_schedule_list, container, false);


        mRecyclerView = (EmptySpecifiedRecyclerView) mView.findViewById(R.id.today_schedule_recycler_view);
        mRecyclerView.setEmptyView(mView.findViewById(R.id.schedule_list_empty_view_container));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ToDoItemRecyclerViewAdapter(toDoItems
        ,mView);
        ItemTouchHelper.Callback callback = new ItemTouchHelperClass(adapter,getContext());
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(adapter);
        courseLoginInfoModel =CourseLoginInfoModel.getInstanceFromSharedPreference(getContext());
        fetchScheduleInfoFromStorage = new FetchScheduleInfoFromStorage();
        fetchScheduleInfoFromStorage.execute();
        return mView;
    }

    public void broadcastRecyclerViewToShowEmpty(){
        mRecyclerView.showEmptyView();
    }
    @SuppressLint("StaticFieldLeak")
    private class FetchScheduleInfoFromStorage extends AsyncTask<Void, Void, String>{
        FetchScheduleInfoFromStorage(){}

        @Override
        protected String doInBackground(Void... voids) {
            isAltered=true;
            List<ToDoItem> _toDoItems = new ArrayList<ToDoItem>();

            try{
                _toDoItems = ToDoItem.getListInstanceFromStorage(getContext());
            } catch (IOException e) {
                return "获取存储的TODO项失败";
            }
            if(_toDoItems!=null)
            {
                toDoItems.addAll(_toDoItems);
                toDoItems.sort(Comparator.comparing(toDoItem -> {return toDoItem.getEndTime();}));
            }
            return "成功";
        }
        @Override
        protected void onPostExecute(final String returnStatus) {
            adapter.notifyDataSetChanged();

            //TODO handle exception
            updateScheduleInfoFromWebApi = new UpdateScheduleInfoFromWebApi();
            updateScheduleInfoFromWebApi.execute();
        }

    }
    @SuppressLint("StaticFieldLeak")
    private class UpdateScheduleInfoFromWebApi extends AsyncTask<Void, Void, Integer>{
        UpdateScheduleInfoFromWebApi(){
            mMainActivity.RevealSyncingIndicator(true);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            isAltered=true;
            List<ToDoItem> _toDoItems = new ArrayList<ToDoItem>();
            List<CourseRawToDoItemsRootObject> _courseRawToDoItemsRootObjects;
            try{
                _courseRawToDoItemsRootObjects
                        = CourseRawToDoItemsRootObject.getInstanceFromWebApi(
                        getContext(),
                        String.valueOf(System.currentTimeMillis()
                        -MILLISECONDS_OF_A_WEEK*1),
                        String.valueOf(System.currentTimeMillis()
                                +MILLISECONDS_OF_A_WEEK*4)
                );
                Log.e("",JSON.toJSONString(_courseRawToDoItemsRootObjects));
                for(CourseRawToDoItemsRootObject courseRawToDoItemsRootObject : _courseRawToDoItemsRootObjects){
                    ToDoItem newItem = new ToDoItem(courseRawToDoItemsRootObject, null);
                    _toDoItems.add(newItem);
                }
            }catch (Exception e){
                Log.e("ERR",e.getLocalizedMessage());
                return -1;//"获取CourseApi失败";
            }

            int distinctCount = 0;
            if(_toDoItems!=null)
            {
                boolean isDistinct;

                //查重
                for(ToDoItem td:_toDoItems){
                    isDistinct = true;
                    for(ToDoItem _td: toDoItems){
                        if(td.equals(_td))
                        {
                            isDistinct=false;
                            break;
                        }
                    }
                    if(isDistinct){
                        distinctCount++;
                        toDoItems.add(td);
                    }
                }
                toDoItems.sort(Comparator.comparing(toDoItem -> {return toDoItem.getEndTime();}));
                try {
                    saveListInstance(toDoItems,getContext());
                } catch (IOException e) {
                    return -2;//"更新存储来自CourseApi的信息失败";
                }
                return distinctCount;
            }
            return -3;//"未获取到有效信息";
        }
        @Override
        protected void onPostExecute(final Integer returnStatus) {

            if(returnStatus>0){
                mMainActivity.sendCourseSyncMsg("教学网同步完成","新增了 "+returnStatus+" 项 Deadline",
                        "新增了 "+returnStatus+" 项 Deadline。"
                );
            }
            mMainActivity.RevealSyncingIndicator(false);
            adapter.notifyDataSetChanged();
            sendBroadcastDataChanged();
            //TODO:Handle Excpetion
        }

        @Override
        protected void onCancelled() {
            mMainActivity.RevealSyncingIndicator(false);
            super.onCancelled();
        }
    }

    @Override
    public void onPause() {
        if(isAltered)
        try {
            saveListInstance(toDoItems,getContext());
        } catch (IOException e) {
            //TODO:Handle Excpetion
            e.printStackTrace();
        }
        super.onPause();
    }

    public void sendBroadcastDataChanged(){
        if(mMainActivity!=null){
            mMainActivity.broadcastDatasetChanged(toDoItems);
        }
    }
}
