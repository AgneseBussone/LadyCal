package com.beacat.calendar.ladycal;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.Period;
import com.tyczj.extendedcalendarview.PeriodDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity for adding past periods to the database.
 * It shows also all the periods already into the db.
 */

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private List<Period> entries;
    private MyArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if(i != null){
            setTheme(i.getIntExtra("themeId", R.style.AppTheme));
        }
        setContentView(R.layout.history);

        listView = (ListView)findViewById(R.id.periods_list);

        // Change the title in the action bar
        getSupportActionBar().setTitle(R.string.historyTitle);

        // Add back navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Use an Async task to load all the periods showing a spinner in the meantime */
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            RelativeLayout linlaHeaderProgress = (RelativeLayout) findViewById(R.id.progress_layout);

            @Override
            protected void onPreExecute() {
                // show the spinner
                linlaHeaderProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                entries = PeriodDatabase.getInstance(getApplicationContext()).getAllPeriods("DESC");
                if(BuildConfig.DEBUG) {
//                    SystemClock.sleep(5000); // TEST ONLY
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if(entries != null) {
                    adapter = new MyArrayAdapter(HistoryActivity.this, entries);
                    listView.setAdapter(adapter);
                }
                // hide the spinner
                linlaHeaderProgress.setVisibility(View.GONE);
            }
        };

        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case  R.id.deleteAll:
                AlertDialog alertDialog = new AlertDialog.Builder(HistoryActivity.this).create();
                alertDialog.setTitle(R.string.dialog_delete_all_period);

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteAllPeriodsTask(HistoryActivity.this).execute();
                                entries.clear();
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;

            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback to add a period
     * @param view
     */
    public void addPeriod(View view) {
        AlertDialog.Builder builder= new AlertDialog.Builder(HistoryActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.add_period, null);
        builder.setView(dialogView);

        final DatePicker start = (DatePicker) dialogView.findViewById(R.id.startDate);
        final DatePicker end = (DatePicker) dialogView.findViewById(R.id.endDate);

        // set max date: it's not possible to set period in the future
        start.setMaxDate(Calendar.getInstance().getTimeInMillis());
        end.setMaxDate(Calendar.getInstance().getTimeInMillis());

        /* Workaround to leave the dialog open */
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("CANCEL", null);
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Day startDay = new Day(null, start.getYear(), start.getMonth(), start.getDayOfMonth());
                Day endDay = new Day(null, end.getYear(), end.getMonth(), end.getDayOfMonth());

                if(startDay.getDayUTC() <= endDay.getDayUTC()) {
                    new AddPeriodTask(HistoryActivity.this, null).execute(startDay, endDay);

                    // It is an insertion at the end of the list, I don't care about sorting
                    // because the next time the user call the activity the list will be sorted
                    entries.add(new Period(startDay.getDayUTC(), endDay.getDayUTC()));
                    ((MyArrayAdapter) listView.getAdapter()).notifyDataSetChanged();

                    dialog.dismiss();
                }
                else{
                    Toast.makeText(HistoryActivity.this, "Error: start day after end day", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * Inner class used by the list
     */
    private class MyArrayAdapter extends BaseAdapter{

        private List<Period> data;
        private Context context;

        MyArrayAdapter(Context context, List<Period> data){
            this.data = data;
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                // inflate the layout
                convertView = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
            }

            final Period item = (Period)getItem(position);
            TextView period = (TextView)convertView.findViewById(R.id.period);
            TextView length = (TextView)convertView.findViewById(R.id.days);
            Button deleteBtn = (Button)convertView.findViewById(R.id.deleteBtn);
            Button editBtn = (Button)convertView.findViewById(R.id.editBtn);
            Button medBtn = (Button)convertView.findViewById(R.id.editMedBtn);

            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(item.getStartDay());
            period.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.getTime()));
            period.setTextColor(UtilityClass.getThemeColor(HistoryActivity.this, R.attr.colorPrimaryDark));
            long days = ExtendedCalendarView.getDifferenceInDays(item.getEndDay(), item.getStartDay()) + 1;
            length.setText("Length: " + String.valueOf(days));
            length.setTextColor(UtilityClass.getThemeColor(HistoryActivity.this, R.attr.colorAccent));

            /* Delete button */
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(HistoryActivity.this).create();
                    alertDialog.setTitle(R.string.dialog_delete_period);

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    new DeletePeriodTask(HistoryActivity.this).execute(item);
                                    data.remove(position);
                                    notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            });

            /* Edit button */
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                    final View dialogView = getLayoutInflater().inflate(R.layout.add_period, null);
                    builder.setView(dialogView);

                    final DatePicker start = (DatePicker) dialogView.findViewById(R.id.startDate);
                    final DatePicker end = (DatePicker) dialogView.findViewById(R.id.endDate);
                    // set max date: it's not possible to set period in the future
                    start.setMaxDate(Calendar.getInstance().getTimeInMillis());
                    end.setMaxDate(Calendar.getInstance().getTimeInMillis());

                    cal.setTimeInMillis(item.getStartDay());
                    start.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
                    cal.setTimeInMillis(item.getEndDay());
                    end.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

                    /* Workaround to leave the dialog open */
                    builder.setPositiveButton("OK", null);
                    builder.setNegativeButton("CANCEL", null);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Day startDay = new Day(null, start.getYear(), start.getMonth(), start.getDayOfMonth());
                            Day endDay = new Day(null, end.getYear(), end.getMonth(), end.getDayOfMonth());

                            if(startDay.getDayUTC() <= endDay.getDayUTC()) {

                                // the cycle length will be updated if necessary
                                Period new_period = new Period(startDay.getDayUTC(),
                                        endDay.getDayUTC(),
                                        ExtendedCalendarView.getDifferenceInDays(endDay.getDayUTC(), startDay.getDayUTC()) +1,
                                        item.getCycleLength());

                                new EditPeriodTask(HistoryActivity.this).execute(item, new_period);

                                data.remove(position);
                                data.add(position, new_period);

                                notifyDataSetChanged();

                                dialog.dismiss();
                            }
                            else{
                                Toast.makeText(HistoryActivity.this, "Error: start day after end day", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            /* Medicine button */
            medBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(HistoryActivity.this).create();
                    alertDialog.setTitle(R.string.dialog_edit_medicine);

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra(MainActivity.CHANGE_MEDS_STRING_ID, item.getStartDay());
                                    setResult(Activity.RESULT_OK, resultIntent);

                                    dialog.dismiss();

                                    finish();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO, STAY HERE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                }
            });

            return convertView;
        }
    }

}
