package com.dewidar.foreach.foreach;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;


public class Datesettings implements DatePickerDialog.OnDateSetListener {

    Context context;
    public Datesettings(Context context){

        this.context=context;
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Toast.makeText(context,"selected date : "+dayOfMonth +"/"+month+"/"+year,Toast.LENGTH_SHORT).show();

        String dat=dayOfMonth +"/"+month+"/"+year;
        RegisterActivity.Birthdat=dat;


    }
}