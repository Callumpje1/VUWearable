package nl.hva.vuwearable.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import nl.hva.vuwearable.R
import nl.hva.vuwearable.databinding.FragmentDashboardBinding
import nl.hva.vuwearable.ui.udp.UDPViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    private val udpViewModel: UDPViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.ivFaq.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_faqFragment)
        }

        binding.ivBreathingWidget.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_dashboard_to_breathingFragment)
        }

        connectionEstablished()

        return root
    }

    /**
     * Observe the udpviewmodel for changes in wifi connection and display appropriate icons
     */
    private fun connectionEstablished() {
        udpViewModel.isConnected.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.ivWifi.setImageResource(R.drawable.ic_baseline_wifi_24)
                    binding.wifiConnection.text = getString(R.string.connection_success)

                    udpViewModel.isReceivingData.observe(viewLifecycleOwner) { isReceivingData ->
                        if (!isReceivingData) {
                            binding.wifiConnection.text = getString(R.string.no_data_connection)
                            binding.ivWifi.setImageResource(R.drawable.ic_baseline_wifi_24_no_data)
                        }
                    }
                }
                false -> {
                    binding.ivWifi.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                    binding.wifiConnection.text = getString(R.string.connection_failed)
                }
            }
        }
    }

    /**
     * Show dialog when an issue occurs
     */
    private fun showIssueDialog() {

        val dialogLayout = layoutInflater.inflate(R.layout.issue_dialog, null)

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.set_issue_title))

        builder.setCancelable(true)

        builder.setView(dialogLayout)

        builder.show()
    }
}