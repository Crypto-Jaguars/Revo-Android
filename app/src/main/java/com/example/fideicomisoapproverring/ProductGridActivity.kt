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
    private val repository = ProductRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_grid)

    
        productGrid = findViewById(R.id.productGrid)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        loadingState = findViewById(R.id.loadingState)
        emptyState = findViewById(R.id.emptyState)
        errorState = findViewById(R.id.errorState)

    
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

        setupRecyclerView()
        setupSwipeRefresh()
        loadProducts()
    }

    private fun loadProducts() {
        if (!swipeRefresh.isRefreshing) {
            showLoadingState()
        }
        emptyState.visibility = View.GONE
        errorState.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.Main) {
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
        showLoadingMoreIndicator()
        
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (currentPage >= MAX_PAGES) {
                    hideLoadingMoreIndicator()
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    repository.getProducts(currentPage + 1, itemsPerPage)
                }
                result.fold(
                    onSuccess = { newProducts ->
                        if (newProducts.isEmpty()) {
                            hideLoadingMoreIndicator()
                            return@launch
                        }
                        
                        val currentList = ArrayList(adapter.currentList)
                        currentList.addAll(newProducts)
                        adapter.submitList(currentList)
                        
                        currentPage++
                        isLoading = false
                        hideLoadingMoreIndicator()
                    },
                    onFailure = { e ->
                        isLoading = false
                        hideLoadingMoreIndicator()
                        showLoadMoreError()
                        Log.e("ProductGridActivity", "Error loading more products", e)
                    }
                )
            } catch (e: Exception) {
                isLoading = false
                hideLoadingMoreIndicator()
                showLoadMoreError()
                Log.e("ProductGridActivity", "Error loading more products", e)
            }
        }
    }

    private fun showLoadingMoreIndicator() {
     
        val currentList = ArrayList(adapter.currentList)
        currentList.add(Product.LoadingItem)
        adapter.submitList(currentList)
    }

    private fun hideLoadingMoreIndicator() {
   
        val currentList = ArrayList(adapter.currentList)
        if (currentList.lastOrNull()?.isLoading == true) {
            currentList.removeAt(currentList.lastIndex)
            adapter.submitList(currentList)
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

    private fun createSampleProducts(): List<Product> {
        return listOf(
            Product(
                id = "1",
                name = "Organic Tomatoes",
                description = "Fresh organic tomatoes from local farm",
                price = 4.99,
                imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer123",
                certifications = listOf("organic", "fair_trade")
            ),
            Product(
                id = "2",
                name = "Fresh Sweet Corn",
                description = "Sweet corn harvested today",
                price = 3.49,
                imageUrl = "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer456",
                certifications = listOf("organic")
            ),
            Product(
                id = "3",
                name = "Rainbow Carrots",
                description = "Organic rainbow carrots, freshly harvested",
                price = 3.99,
                imageUrl = "https://images.unsplash.com/photo-1582515073490-39981397c445?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer789",
                certifications = listOf("organic")
            ),
            Product(
                id = "4",
                name = "Fresh Strawberries",
                description = "Sweet and juicy strawberries",
                price = 5.99,
                imageUrl = "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=500&q=80",
                isAvailable = false,
                farmerId = "farmer123",
                certifications = listOf("organic", "fair_trade")
            ),
            Product(
                id = "5",
                name = "Bell Peppers Mix",
                description = "Colorful mix of fresh bell peppers",
                price = 4.49,
                imageUrl = "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer456",
                certifications = listOf("fair_trade")
            ),
            Product(
                id = "6",
                name = "Fresh Basil",
                description = "Aromatic fresh basil leaves",
                price = 2.99,
                imageUrl = "https://images.unsplash.com/photo-1618164435735-413d3b066c9a?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer789",
                certifications = listOf("organic")
            ),
            Product(
                id = "7",
                name = "Red Potatoes",
                description = "Fresh red potatoes, perfect for roasting",
                price = 3.49,
                imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer123",
                certifications = listOf("fair_trade")
            ),
            Product(
                id = "8",
                name = "Fresh Spinach",
                description = "Organic baby spinach leaves",
                price = 3.99,
                imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer456",
                certifications = listOf("organic", "fair_trade")
            ),
            Product(
                id = "9",
                name = "Honey Crisp Apples",
                description = "Sweet and crispy apples",
                price = 4.99,
                imageUrl = "https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=500&q=80",
                isAvailable = false,
                farmerId = "farmer789",
                certifications = listOf("organic")
            ),
            Product(
                id = "10",
                name = "Cherry Tomatoes",
                description = "Sweet cherry tomatoes",
                price = 3.99,
                imageUrl = "https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=500&q=80",
                isAvailable = true,
                farmerId = "farmer123",
                certifications = listOf("organic", "fair_trade")
            )
        )
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

    companion object {
        private const val MAX_PAGES = 5 // Limit to 5 pages of products
    }
} 