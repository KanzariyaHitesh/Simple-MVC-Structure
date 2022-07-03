package com.mvc.simple

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mvc.simple.adapter.DateAdapter
import com.mvc.simple.adapter.EventDataAdapter
import com.mvc.simple.model.AgendaModel
import com.mvc.simple.model.DateModel
import com.mvc.simple.model.EventListApiResponse
import com.mvc.simple.repository.RepoModel
import com.mvc.simple.util.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    val repo: RepoModel by inject()
    val agendaList = ArrayList<EventListApiResponse.Data.Agenda>()
    var eventDataAdapter = EventDataAdapter(mutableListOf())
    var dateSet = HashSet<DateModel>()
    var dateList: ArrayList<DateModel>? = null
    var dateAdapter = DateAdapter(mutableListOf())
    var dateWiseAgenda = ArrayList<AgendaModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setRecyclerView()
        callEventApi()
        edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (p0.isNotEmpty()) {
                        Log.e("TAG", "afterTextChanged: " + dateWiseAgenda.size)
                        val list = filterAgenda(dateWiseAgenda, p0.toString())
                        eventDataAdapter.updateList(list)
                    } else {
                        eventDataAdapter.updateList(dateWiseAgenda)
                    }
                }
            }

        })
    }

    fun filterAgenda(
        agendaList: ArrayList<AgendaModel>,
        searchText: String
    ): ArrayList<AgendaModel> {
        val tempList = ArrayList<AgendaModel>()
        for (obj in agendaList) {
            Log.e("TAG", "searchText: $searchText")
            Log.e("TAG", "objText: "+obj.heading)
            if (obj.heading.lowercase().contains(searchText.lowercase())) {
                tempList.add(obj)
            }
        }
        Log.e("TAG", "filterAgenda: " + tempList.size)
        return tempList
    }

    private fun setRecyclerView() {
        rvData.setHasFixedSize(true)
        rvData.adapter = eventDataAdapter

        rvDates.setHasFixedSize(true)
        rvDates.adapter = dateAdapter
        dateAdapter.onItemClick = { position, date ->
            tvMainDate.text = date
            for (i in dateList!!.indices) {
                dateList!![i].isSelected = i == position
            }
            dateAdapter.updateList(dateList!!)
            Log.e("TAG", "date==>$date")
            dateWiseAgenda.addAll(getAgendaList(dateList!![position].date))
            eventDataAdapter.updateList(dateWiseAgenda)
        }
    }

    private fun getAgendaList(date: String): ArrayList<AgendaModel> {
        val dateWiseAgenda = ArrayList<AgendaModel>()
        for (i in agendaList.indices) {
            if (date == agendaList[i].Start_date) {
                dateWiseAgenda.add(
                    AgendaModel(
                        agendaList[i].Heading,
                        agendaList[i].Start_time,
                        agendaList[i].End_time
                    )
                )
            }
        }
        return dateWiseAgenda
    }

    @SuppressLint("CheckResult")
    private fun callEventApi() {
        if (isInternetAvailable(this)) {
            progressBar.visibility = View.VISIBLE
            val params = HashMap<String, Any>()
            params[Constants.Param.event_id] = 3342
            params[Constants.Param.lang_id] = 3112
            params[Constants.Param.time_zone] = "Asia/Kolkata"
            repo.api.callGetEventListApi(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    progressBar.visibility = View.GONE
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            if (response.body()!!.success) {
                                agendaList.clear()
                                agendaList.addAll(response.body()!!.data.agenda_list)
                                for (i in agendaList.indices) {
                                    dateSet.add(DateModel(agendaList[i].Start_date))
                                }
                                dateList = ArrayList(dateSet)
                                if (dateList!!.isNotEmpty()) {
                                    dateList!![0].isSelected = true
                                    dateAdapter.updateList(dateList!!)
                                    dateWiseAgenda.addAll(getAgendaList(dateList!![0].date))
                                    eventDataAdapter.updateList(dateWiseAgenda)

                                    val dateFormat = SimpleDateFormat("dd-MM-yyyy");
                                    val date = dateFormat.parse(dateList!![0].date)
                                    val day = DateFormat.format("dd", date)
                                    val month = DateFormat.format("MMM", date)
                                    val dayOfTheWeek = DateFormat.format("EEEE", date)

                                    tvMainDate.text = "$dayOfTheWeek $month $day"
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error calling api", Toast.LENGTH_SHORT).show()
                    }
                }, {
                    Log.e("TAG", "callEventApiException" + it.localizedMessage)
                })
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "No internet available", Toast.LENGTH_SHORT).show()
        }
    }

    fun isInternetAvailable(ctx: Context): Boolean {
        val cm = ctx.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnected && networkInfo.isConnectedOrConnecting
    }
}