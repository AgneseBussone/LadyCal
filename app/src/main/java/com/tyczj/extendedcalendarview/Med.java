package com.tyczj.extendedcalendarview;

/**
 * Class used to carry the information needed by the medicine chart.
 */

public class Med {
    private long date;              // UTC for the first day
    private int quantity;           // total of meds taken
    private float[] meds;           // how many meds taken per day meds[0] = 5 -> first day, 5 meds


    public Med(long date, int period_length){
        this.date = date;
        meds = new float[period_length];
        this.quantity = 0;
    }

    public void setDay(int day, int quantity){
        if(day >=0 && day < meds.length){
            meds[day] = (float)quantity;
            this.quantity += quantity;
        }
    }

    public float getMedsinDay(int day){
        if(day >=0 && day < meds.length){
            return meds[day];
        }
        return 0;
    }

    public float[] getMeds() {
        return meds;
    }

    public int getTotalQuantity(){return quantity;}

    public long getDate(){return date;}

}
