package com.example.flickrfindr.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flickrfindr.Model.FlickrSearchAPIModel
import com.example.flickrfindr.Model.PhotoItemModel
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivityViewModel : ViewModel() {

    private var _status = MutableLiveData<Boolean>()
    private var _listSize = MutableLiveData<Int>()
    private var _photoItemModelList = MutableLiveData<ArrayList<PhotoItemModel>>()
    private var _previousSearchTerms = MutableLiveData<ArrayList<String>>()

    val status: MutableLiveData<Boolean>
        get() = _status

    val listSize: MutableLiveData<Int>
        get() = _listSize

    val photoItemModelList: MutableLiveData<ArrayList<PhotoItemModel>>
        get() = _photoItemModelList

    val previousSearchTerms: MutableLiveData<ArrayList<String>>
        get() = _previousSearchTerms

    private var flickrSearchAPIModel: FlickrSearchAPIModel? = null
    private var previousSearchTermsList = arrayListOf<String>()

    fun fetchSearchList(searchString: String, pageNumber: Int) {
        addPreviousSearchTerm(searchString)

        getAPIObservable(searchString, pageNumber)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(Schedulers.newThread())
            ?.subscribe(
                { value ->
                    flickrSearchAPIModel = value
                },
                { error ->
                    _status.postValue(false)
                },
                {
                    _listSize.postValue(flickrSearchAPIModel?.photos?.total)

                    val tempList: ArrayList<PhotoItemModel> = arrayListOf()

                    var size = 0

                    try {
                        size = flickrSearchAPIModel?.photos?.photo!!.size
                    } catch (ex: java.lang.Exception) {

                    }

                    for (i in 0 until size) {

                        val photoObject = flickrSearchAPIModel?.photos?.photo?.get(i)

                        val photoItemModel = PhotoItemModel(
                            photoObject?.title,
                            "https://live.staticflickr.com/${photoObject?.server}/${photoObject?.id}_${photoObject?.secret}_w.jpg"
                        )

                        tempList += photoItemModel
                    }

                    _photoItemModelList.postValue(tempList)
                }
            )
    }

    fun addPreviousSearchTerm(searchTerm: String) {

        if (previousSearchTermsList.contains(searchTerm)) {
            previousSearchTermsList.remove(searchTerm)
        }

        if (previousSearchTermsList.size == 5) {
            previousSearchTermsList.removeAt(0)
        }

        previousSearchTermsList.add(searchTerm)
        _previousSearchTerms.postValue(previousSearchTermsList)
    }

    private fun getAPIObservable(
        searchString: String,
        pageNumber: Int
    ): Observable<FlickrSearchAPIModel>? {
        return Observable.create(ObservableOnSubscribe<FlickrSearchAPIModel> { emitter ->
            val url =
                "http://api.flickr.com/services/rest/?method=flickr.photos.search&format=json&tags=$searchString&api_key=1508443e49213ff84d566777dc211f2a&per_page=25&page=$pageNumber&nojsoncallback=?"

            val client = OkHttpClient()

            val request =
                Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val gson = Gson()
                    val flickrSearchAPIModel: FlickrSearchAPIModel =
                        gson.fromJson(response.body()!!.string(), FlickrSearchAPIModel::class.java)

                    if (flickrSearchAPIModel.stat == "ok") {
                        emitter.onNext(flickrSearchAPIModel)
                        emitter.onComplete()
                    } else {
                        emitter.onError(Throwable(""))
                    }
                }
            } catch (ex: Exception) {
                emitter.onError(Throwable(""))
            }
        })
    }
}