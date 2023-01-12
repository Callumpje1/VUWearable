package nl.hva.vuwearable

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.scichart.charting.visuals.SciChartSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.vuwearable.databinding.ActivityMainBinding
import nl.hva.vuwearable.udp.UDPConnection
import nl.hva.vuwearable.ui.chart.scichart.ChartViewModel
import nl.hva.vuwearable.ui.login.LoginViewModel
import nl.hva.vuwearable.ui.udp.UDPViewModel
import nl.hva.vuwearable.workmanager.BackgroundWorker
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Bunyamin Duduk
 * @author Lorenzo Bindemann
 * @author Callum Svadkovski
 * @author Hugo Zuidema
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    // Login viewmodel is public to use it also in other classes to check the login status
    val loginViewModel: LoginViewModel by viewModels()
    private val chartViewModel: ChartViewModel by viewModels()
    private val udpViewModel: UDPViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(BackgroundWorker::class.java, 1, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                .build()

        setupAppBar()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "Background notifications",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )

        navView.setupWithNavController(navController)

        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setIcon(R.drawable.topappbarlogo);

        // Android does not allow to use a UDP socket on the main thread,
        // so we need to use it on a different thread
        Thread(
            UDPConnection(
                this.applicationContext,
                3,
                3,
                setConnectedCallback = { isConnected, isReceivingData ->
                    // Update the view model on the main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        udpViewModel.setIsReceivingData(isReceivingData)
                        udpViewModel.setIsConnected(isConnected)
                    }
                },
                setASectionMeasurement = {
                    CoroutineScope(Dispatchers.Main).launch {
                        chartViewModel.setASectionMeasurement(TreeMap(it))
                    }
                })
        ).start()

        try {
            SciChartSurface.setRuntimeLicenseKey(BuildConfig.SCI_CHART_KEY)
        } catch (e: Exception) {
            Log.e("SciChart", e.toString())
        }

        initializeBottomNavbar()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_button -> showDialog()
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupAppBar() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // This list is made to not show any back button
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_chart,
                R.id.professorDashboardFragment,
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * This function is made to show a login or logout dialog
     * @author Lorenzo Bindemann
     */
    private fun showDialog() {
        if (loginViewModel.isLoggedIn.value == false) {
            // create login pop up
            val dialogLayout = layoutInflater.inflate(R.layout.login_dialog, null)
            val builder = android.app.AlertDialog.Builder(this).setView(dialogLayout).show()

            // set login function on button click
            dialogLayout.findViewById<Button>(R.id.login_button).setOnClickListener {
                val inputCode =
                    dialogLayout.findViewById<EditText>(
                        R.id.input_password
                    ).text.toString()
                loginViewModel.checkInput(inputCode, this@MainActivity)
                // check if login is successfully
                if (loginViewModel.isLoggedIn.value == true) {
                    builder.hide()
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.professorDashboardFragment)
                } else {
                    // login is unsuccessfully
                    builder.findViewById<EditText>(R.id.input_password).setTextColor(Color.RED)
                    builder.findViewById<TextView>(R.id.wrong_password).visibility = View.VISIBLE
                }
            }
        } else {
            // create logout pop up
            val dialogLayout = layoutInflater.inflate(R.layout.logout_dialog, null)
            val builder = android.app.AlertDialog.Builder(this).setView(dialogLayout).show()

            // set logout function on button click
            dialogLayout.findViewById<Button>(R.id.logout_button).setOnClickListener {
                builder.hide()
                loginViewModel.setIsLoggedIn(false)
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_dashboard)
                Toast.makeText(this, getString(R.string.logout_successful), Toast.LENGTH_LONG)
                    .show()
            }
            // set cancel function on button click
            dialogLayout.findViewById<Button>(R.id.cancel_button).setOnClickListener {
                builder.hide()
            }
        }
        setupAppBar()
    }

    /**
     * Initialize a click listener on the bottom navigation items in order to navigate correctly
     */
    private fun initializeBottomNavbar() {
        // Find the bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_view)

        // Set a click listener on the selected item
        bottomNav.setOnItemSelectedListener {
            // Based on the item in the bottom navigation, navigate to it
            when (it.itemId) {
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }
                R.id.navigation_chart -> {
                    navController.navigate(R.id.navigation_chart)
                    true
                }
                else -> {
                    true
                }
            }
        }
    }
}