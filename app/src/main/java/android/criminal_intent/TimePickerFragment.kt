package android.criminal_intent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.util.*
import java.util.Calendar.*

private const val ARG_TIME = "time"
class TimePickerFragment: DialogFragment() {

    interface Callbacks{
        fun onTimeSelected(date: Date)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = arguments?.getSerializable(ARG_TIME) as Date
        val calendar = getInstance()
        calendar.time = date
        val calendarYear = calendar.get(YEAR)
        val calendarMonth = calendar.get(MONTH)
        val calendarDay = calendar.get(DAY_OF_MONTH)

        val timeListener = TimePickerDialog.OnTimeSetListener{
            _: TimePicker, hourOfDay: Int, minute: Int ->

            val resultTime: Date =
                GregorianCalendar(calendarYear, calendarMonth, calendarDay, hourOfDay, minute).time


            targetFragment?.let { fragment ->
                (fragment as Callbacks).onTimeSelected(resultTime)
            }
        }

        val initialHourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val is24HourView = true

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHourOfDay,
            initialMinute,
            is24HourView
        )

    }


    companion object{
        fun newInstance(time:Date): TimePickerFragment {
            val args = Bundle().apply { putSerializable(ARG_TIME, time) }
            return TimePickerFragment().apply { arguments = args
            }
        }
    }
}