package com.mvc.simple.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mvc.simple.R
import com.mvc.simple.model.DateModel
import kotlinx.android.synthetic.main.listrow_dates.view.*
import java.text.SimpleDateFormat

class DateAdapter(var arrayList: MutableList<DateModel>) :
    RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    var mContext: Context? = null
    var onItemClick: ((position: Int,date : String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateAdapter.ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.listrow_dates, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateAdapter.ViewHolder, position: Int) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy");
        val date = dateFormat.parse(arrayList[position].date)
        val day = DateFormat.format("dd", date)
        val month = DateFormat.format("MMM", date)
        val dayOfTheWeek = DateFormat.format("EEEE", date)
        holder.itemView.tvDate.text = day.toString() + "\n" + month.toString()

        if (arrayList[position].isSelected) {
            holder.itemView.tabIndicator.visibility = View.VISIBLE
        } else {
            holder.itemView.tabIndicator.visibility = View.INVISIBLE
        }
        val dateString = "$dayOfTheWeek $month $day"

        holder.itemView.setOnClickListener {
            onItemClick!!.invoke(position,dateString)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun updateList(list: ArrayList<DateModel>) {
        arrayList.clear()
        arrayList.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {}
}