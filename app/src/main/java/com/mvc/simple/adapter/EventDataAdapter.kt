package com.mvc.simple.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mvc.simple.R
import com.mvc.simple.model.AgendaModel
import kotlinx.android.synthetic.main.listrow_api_data.view.*

class EventDataAdapter(var arrayList: MutableList<AgendaModel>) : RecyclerView.Adapter<EventDataAdapter.ViewHolder>() {

    var mContext : Context?= null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventDataAdapter.ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.listrow_api_data,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventDataAdapter.ViewHolder, position: Int) {
        holder.itemView.tvEventTime.text = arrayList[position].startTime + " - " + arrayList[position].endTime
        holder.itemView.tvEventName.text = arrayList[position].heading
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun updateList(list: ArrayList<AgendaModel>) {
        arrayList.clear()
        arrayList.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){}
}