package com.example.flickrfindr.View

import android.R.layout.select_dialog_item
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.example.flickrfindr.Model.PaginationScrollListener
import com.example.flickrfindr.Model.PhotoItemModel
import com.example.flickrfindr.ViewModel.MainActivityViewModel
import com.example.flickrfindr.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private var mRequestDelayTimer: Timer? = null
    private var mSearchTask: TimerTask? = null

    private var mainActivityAdapter: MainActivityAdapter? = null

    private val mainActivityViewModel by lazy { MainActivityViewModel() }

    var pageNumber: Int = 1
    var isLastPage: Boolean = false
    var isLoading: Boolean = false

    var linearLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        hideActionBar()
        setupObserver()
        setupRecyclerView()
        setupListener()
        setupSearch()
    }

    fun hideActionBar() {
        this.supportActionBar!!.hide()
    }

    fun setupRecyclerView() {
        mainActivityAdapter = MainActivityAdapter(this, true, null)

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding?.recyclerView?.layoutManager =
            linearLayoutManager

        binding?.recyclerView?.addOnScrollListener(object :
            PaginationScrollListener(linearLayoutManager!!) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true

                getMoreItems()
            }
        })

        binding?.recyclerView?.adapter = mainActivityAdapter
    }

    fun getMoreItems() {
        binding?.progressBar?.visibility = View.VISIBLE

        pageNumber++
        mainActivityViewModel.fetchSearchList(binding?.searchBar?.text.toString(), pageNumber)
    }

    fun setupListener() {
        binding!!.apply {
            ivCancel.setOnClickListener {
                resetSearch()
            }

            fabUp.setOnClickListener {
                val smoothScroller: SmoothScroller =
                    object : LinearSmoothScroller(this@MainActivity) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }
                smoothScroller.targetPosition = 0
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({ linearLayoutManager?.startSmoothScroll(smoothScroller) }, 100)
            }

            searchBar.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus -> if (hasFocus) searchBar.showDropDown() })

            searchBar.setOnTouchListener(OnTouchListener { v, event ->
                searchBar.showDropDown()
                false
            })
        }
    }

    fun resetSearch() {
        pageNumber = 0
        binding!!.searchBar.setText("")
        binding?.tvResults?.text = "0\nResults"
    }

    fun setupSearch() {
        binding!!.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                mRequestDelayTimer?.cancel()

                stopSearch()

                mRequestDelayTimer = Timer()
                mSearchTask = object : TimerTask() {
                    override fun run() {
                        startSearch(
                            binding!!.searchBar.text.toString().trim { it <= ' ' }
                        )
                    }
                }

                mRequestDelayTimer!!.schedule(mSearchTask, 1000)

                if (binding!!.searchBar.text.toString().trim { it <= ' ' } == "") {
                    binding!!.ivCancel.visibility = View.GONE
                } else {
                    binding!!.ivCancel.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    @Synchronized
    private fun startSearch(searchString: String) {

        if (searchString == "") {
            runOnUiThread {
                mainActivityAdapter?.clearList()
            }
        } else {
            mainActivityViewModel.fetchSearchList(searchString, 1)

            runOnUiThread {
                mainActivityAdapter?.addItemsToAdapter(true, ArrayList())

                binding?.shimmerViewContainer?.startShimmer()
            }
        }
    }

    private fun stopSearch() {
        binding?.shimmerViewContainer?.stopShimmer()
    }

    private fun setupObserver() {
        mainActivityViewModel.status.observe(this, Observer { status ->
            status?.let {
                if (!it) {
                    binding?.shimmerViewContainer?.stopShimmer()
                    Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        mainActivityViewModel.photoItemModelList.observe(this, Observer { value ->
            if (pageNumber == 1) {
                mainActivityAdapter =
                    MainActivityAdapter(this, false, value as ArrayList<PhotoItemModel>)
                binding?.recyclerView?.adapter = mainActivityAdapter
            } else {
                mainActivityAdapter?.addItemsToAdapter(false, value as ArrayList<PhotoItemModel>)
            }

            isLoading = false
            binding?.progressBar?.visibility = View.GONE
            binding?.shimmerViewContainer?.hideShimmer()
        })

        mainActivityViewModel.listSize.observe(this, Observer { value ->

            binding?.tvResults?.text = "$value\nResults"
        })

        mainActivityViewModel.previousSearchTerms.observe(this, Observer { value ->
            val adapter: ArrayAdapter<String> =
                ArrayAdapter(this, select_dialog_item, value)

            binding!!.searchBar.threshold = 0
            binding!!.searchBar.setAdapter(adapter)
        })
    }
}