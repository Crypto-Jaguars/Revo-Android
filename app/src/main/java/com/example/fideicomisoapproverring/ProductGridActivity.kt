package com.example.fideicomisoapproverring

import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.os.Bundle
import com.example.fideicomisoapproverring.adapters.ProductGridAdapter
import com.example.fideicomisoapproverring.models.Product
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView.ItemAnimator.ItemHolderInfo

import com.example.fideicomisoapproverring.decorations.GridSpacingItemDecoration
import android.util.Log
import com.example.fideicomisoapproverring.data.ProductRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.example.fideicomisoapproverring.data.SampleProductDataSource
import kotlinx.coroutines.Job
import android.os.Parcelable


class ProductGridActivity : AppCompatActivity() {
    
    private lateinit var productGrid: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var loadingState: View
    private lateinit var emptyState: View
    private lateinit var errorState: View
    private lateinit var adapter: ProductGridAdapter
    private var currentPage = 1
    private var isLoading = false
    private val itemsPerPage = 10
    private val repository = ProductRepository.getInstance(SampleProductDataSource())
    private var loadMoreJob: Job? = null
    private var loadInitialJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_grid)
        
        currentPage = savedInstanceState?.getInt(KEY_CURRENT_PAGE, 1) ?: 1
        isLoading = savedInstanceState?.getBoolean(KEY_IS_LOADING, false) ?: false

        initializeViews()
        setupAdapter()
        setupRecyclerView()
        setupSwipeRefresh()
        
        if (savedInstanceState == null) {
            loadProducts()
        }
    }

    private fun initializeViews() {
        productGrid = findViewById(R.id.productGrid)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        loadingState = findViewById(R.id.loadingState)
        emptyState = findViewById(R.id.emptyState)
        errorState = findViewById(R.id.errorState)
    }

    private fun setupAdapter() {
        adapter = ProductGridAdapter(this) { product ->
            startProductDetails(product)
        }
        
        productGrid.apply {
            layoutManager = GridLayoutManager(this@ProductGridActivity, getGridSpanCount())
            adapter = this@ProductGridActivity.adapter
            setHasFixedSize(true)
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (totalItemCount <= lastVisibleItem + 2) {
                        loadMoreProducts()
                    }
                }
            })
        }

        swipeRefresh.setOnRefreshListener {
            loadProducts()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_PAGE, currentPage)
        outState.putBoolean(KEY_IS_LOADING, isLoading)
        outState.putParcelableArrayList(KEY_PRODUCTS, ArrayList(adapter.currentList.filterIsInstance<Parcelable>()))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedProducts = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelableArrayList(KEY_PRODUCTS, Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState.getParcelableArrayList(KEY_PRODUCTS)
        }
        savedProducts?.let {
            adapter.submitList(it)
        }
    }

    private fun loadProducts() {
        if (!swipeRefresh.isRefreshing) {
            showLoadingState()
        }
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE

        loadInitialJob?.cancel()
        loadInitialJob = lifecycleScope.launch(Dispatchers.Main) {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getProducts(1, itemsPerPage)
                }
                result.fold(
                    onSuccess = { products ->
                        adapter.submitList(products)
                        hideLoadingState()
                        swipeRefresh.isRefreshing = false
                        emptyState.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
                    },
                    onFailure = { e ->
                        errorState.visibility = View.VISIBLE
                        loadingState.visibility = View.GONE
                        swipeRefresh.isRefreshing = false
                        Log.e("ProductGridActivity", "Error loading products", e)
                    }
                )
            } catch (e: Exception) {
                errorState.visibility = View.VISIBLE
                loadingState.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                Log.e("ProductGridActivity", "Error loading products", e)
            }
        }
    }

    private fun loadMoreProducts() {
        if (isLoading) return
        isLoading = true
        
        loadMoreJob?.cancel()
        loadMoreJob = lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (currentPage >= MAX_PAGES) {
                    isLoading = false
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    repository.getProducts(currentPage + 1, itemsPerPage)
                }
                result.fold(
                    onSuccess = { newProducts ->
                        val filteredProducts = newProducts.filter { it.isAvailable }
                        if (filteredProducts.isNotEmpty()) {
                            val currentList = ArrayList(adapter.currentList)
                            currentList.addAll(filteredProducts)
                            adapter.submitList(currentList)
                            currentPage++
                        }
                        isLoading = false
                    },
                    onFailure = { e ->
                        isLoading = false
                        Log.e("ProductGridActivity", "Error loading more products", e)
                    }
                )
            } catch (e: Exception) {
                isLoading = false
                Log.e("ProductGridActivity", "Error loading more products", e)
            }
        }
    }

    private fun showLoadingMoreIndicator() {
        if (adapter.currentList.isNotEmpty()) {
            val currentList = ArrayList(adapter.currentList)
            currentList.add(Product.LoadingItem)
            adapter.submitList(currentList)
        }
    }

    private fun hideLoadingMoreIndicator() {
        val currentList = ArrayList(adapter.currentList)
        val filteredList = currentList.filter { !it.isLoading }
        if (filteredList.size != currentList.size) {
            adapter.submitList(filteredList)
        }
    }

    private fun showLoadMoreError() {
        Toast.makeText(
            this,
            getString(R.string.error_loading_more_products),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startProductDetails(product: Product) {
      
        Toast.makeText(this, "Selected: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showLoadingState() {
        loadingState.visibility = View.VISIBLE
        productGrid.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
    }

    private fun hideLoadingState() {
        loadingState.visibility = View.GONE
        productGrid.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        val spanCount = getGridSpanCount()
        productGrid.apply {
            setItemViewCacheSize(20)
            setHasFixedSize(true)
            
         
            (layoutManager as? GridLayoutManager)?.apply {
                isItemPrefetchEnabled = true
                initialPrefetchItemCount = 12
            }
            
         
            recycledViewPool.setMaxRecycledViews(0, 15)
            
          
            itemAnimator = object : DefaultItemAnimator() {
                override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
                    dispatchAddFinished(holder)
                    return false
                }
                
                override fun animateChange(
                    oldHolder: RecyclerView.ViewHolder,
                    newHolder: RecyclerView.ViewHolder,
                    preInfo: ItemHolderInfo,
                    postInfo: ItemHolderInfo
                ): Boolean {
                    dispatchChangeFinished(oldHolder, true)
                    return false
                }
            }
            
        
            addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount = spanCount,
                    spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
                    includeEdge = true
                )
            )
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.apply {
            setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3
            )
            setProgressViewOffset(true, 0, 160)
            setSize(SwipeRefreshLayout.LARGE)
        }
    }

    private fun getGridSpanCount(): Int {
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        
        return when {
            dpWidth >= 900 -> 4  
            dpWidth >= 600 -> 3  
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE -> 3
            else -> 2  
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadMoreJob?.cancel()
        loadInitialJob?.cancel()
    }

    companion object {
        private const val KEY_CURRENT_PAGE = "current_page"
        private const val KEY_IS_LOADING = "is_loading"
        private const val KEY_PRODUCTS = "products"
        private const val MAX_PAGES = 5
    }
} 