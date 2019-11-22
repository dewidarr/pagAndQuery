package com.example.monko.foreach;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;


public class PickerDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Datesettings datesettings=new Datesettings(getActivity());

        Calendar calendar= Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int mounth=calendar.get(Calendar.MONTH);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog;
        dialog=new DatePickerDialog(getActivity(),datesettings,year,mounth,day);
        return dialog;
    }
}