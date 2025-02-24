import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavigationView
import androidx.navigation.ui.NavigationUI
import androidx.core.view.GravityCompat
import android.widget.Toast

fun handleNavigationClick(menuItem: NavigationDrawerMenuItem, context: Context) {
    Log.d("NavigationDrawer", "Menu item clicked: ${menuItem.route}")
    when (menuItem.route) {
        Routes.ImageUpload -> {
            Toast.makeText(context, "Image Upload clicked", Toast.LENGTH_SHORT).show()
            // val intent = Intent(context, ImageUploadActivity::class.java)
            // context.startActivity(intent)
        }
        // Handle other routes...
    }
}

// Assuming you have a DrawerLayout and NavigationView set up
val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
val navigationView: NavigationView = findViewById(R.id.nav_view)

navigationView.setNavigationItemSelectedListener { menuItem ->
    handleNavigationClick(menuItem, this) // Pass the context
    drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after selection
    true
}