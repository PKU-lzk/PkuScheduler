package com.example.pkuscheduler.Activities;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.example.pkuscheduler.R;
import com.example.pkuscheduler.ViewModels.ToDoItem;

import java.util.Date;
import java.util.Locale;

import ws.vinta.pangu.Pangu;

public class EventDescriptionActivity extends AppCompatActivity {

    private Intent mIntent;
    private ToDoItem toDoItem;
    private TextView mTitle;
    private TextView mDesc;
    private TextView mDeadline;
    private TextView mReminder;
    private TextView mCourseSource;
    private Pangu pangu= new Pangu();

    private DateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.CHINA);
    public String FormatDate( Date dt){
        return  dateFormat.format(dt)+"  " +timeFormat.format(dt);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_description);
        setUpActionBar();
        mIntent = getIntent();
        toDoItem= JSON.parseObject(mIntent.getStringExtra("SCHEDULE"),ToDoItem.class);
        mTitle=findViewById(R.id.event_title);
        mDesc=findViewById(R.id.event_todo_description);
        mReminder = findViewById(R.id.event_reminder);
        mCourseSource = findViewById(R.id.event_link_to_course);
        mDeadline = findViewById(R.id.event_date_time);
        mTitle.setText(
                pangu.spacingText(toDoItem.getScheduleTitle()).replace("("," (").replace(")",") ")
        );
        if(toDoItem.getHasReminder()){
            mReminder.setText(
                FormatDate(toDoItem.getScheduleReminderTimeList()));
        }
        else{
            mReminder.setHint("未设置提醒");

        }

        mDesc.setText(toDoItem.getScheduleDescription());
        mCourseSource.setText(
                pangu.spacingText(toDoItem.getScheduleCourseSource()));
        mDeadline.setText(
                FormatDate(toDoItem.getEndTime())
        );
    }

    public void setUpActionBar(){
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("查看 Deadline");
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
