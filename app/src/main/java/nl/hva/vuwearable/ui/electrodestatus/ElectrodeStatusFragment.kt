/**
 * @author Hugo Zuidema
 */
package nl.hva.vuwearable.ui.electrodestatus

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import nl.hva.vuwearable.R
import nl.hva.vuwearable.databinding.FragmentElectrodeStatusBinding
import nl.hva.vuwearable.models.Electrode
import nl.hva.vuwearable.ui.chart.scichart.ChartViewModel

class ElectrodeStatusFragment : Fragment() {

    private var _binding: FragmentElectrodeStatusBinding? = null
    private val binding get() = _binding!!

    private val chartViewModel: ChartViewModel by activityViewModels()

    private val electrodes: Array<Electrode> = arrayOf(
        Electrode("BLACK", arrayOf(71.5F, 69.5F, 2.1F), arrayOf("ECG", "ICG"), false, "Chest"),
        Electrode("YELLOW", arrayOf(51.2F, 61F, 2.1F), arrayOf("TEST", "TEST"), false, "Chest"),
        Electrode("DEVICE", arrayOf(52F, 23F, 4F), arrayOf("ECG", "ICG"), false, "Chest"),
        Electrode("BLUE", arrayOf(47F, 66.5F, 2.1F), arrayOf("ECG", "ICG"), false, "Back"),
        Electrode("DARK_BLUE", arrayOf(51F, 11.5F, 2.1F), arrayOf("TEST", "TEST"), false, "Back"),
    )

    private var currentBodyView: String = "Chest"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElectrodeStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeMeasurements()

        binding.btnViewOtherBodyPart.text = getString(R.string.esf_btn_view_other_part_txt, "Back")

        binding.btnViewOtherBodyPart.setOnClickListener {
            switchBodyView()
        }

        view.postDelayed({
            // draw the status circle once the values have been checked
            createBitMap(electrodes.filter { e -> e.location == "Chest" }, R.drawable.chest_status)
        }, 100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Function which observes the measurements from the ChartViewModel
     */
    private fun observeMeasurements() {
        chartViewModel.sectionAMeasurements.observe(viewLifecycleOwner) {
            it.values.forEach { aSection ->
                aSection.status.entries.forEach { status ->

                    electrodes
                        .filter {
                            // check if the status is related to any electrodes
                                electrode -> electrode.relatedChannels.contains(status.key)
                        }.forEach { electrode ->
                            // if the string contains a 1, the electrode is not working correctly
                            electrode.isFailing = status.value.contains("1")
                        }
                }
            }
        }
    }

    /**
     * Function which creates a bitmap and draws the given circles onto the given image.
     * @param locatedElectrodes a List with the electrodes to draw onto the image
     * @param drawableBackground the ID of the drawable image to use as background to draw on
     */
    private fun createBitMap(locatedElectrodes: List<Electrode>,
                             drawableBackground: Int) {

        var bitmap: Bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap = bitmap.copy(bitmap.config, true)

        val canvas = Canvas(bitmap)

        val paintRed = Paint()
        paintRed.color = Color.RED
        paintRed.style = Paint.Style.FILL_AND_STROKE
        paintRed.isAntiAlias = true

        val paintGreen = Paint()
        paintGreen.color = Color.GREEN
        paintGreen.style = Paint.Style.FILL
        paintGreen.isAntiAlias = true

        val imageView: ImageView = binding.ivBody
        imageView.setImageBitmap(bitmap)
        imageView.setBackgroundResource(drawableBackground)

        locatedElectrodes.forEach { electrode ->
            canvas.drawCircle(
                electrode.circleCoordinates[0],
                electrode.circleCoordinates[1],
                electrode.circleCoordinates[2],
                if (electrode.isFailing) paintRed else paintGreen
            )
        }

        imageView.invalidate()
        updateStatusText()
    }

    /**
     * Function which switches views between the chest and back
     */
    private fun switchBodyView() {
        if (currentBodyView == getString(R.string.body_chest)) {
            currentBodyView = getString(R.string.body_back)
            binding.btnViewOtherBodyPart.text = getString(
                R.string.esf_btn_view_other_part_txt, getString(R.string.body_chest))

            createBitMap(electrodes.filter { e -> e.location == "Back" }, R.drawable.back_status)
        } else {
            currentBodyView = getString(R.string.body_chest)
            binding.btnViewOtherBodyPart.text = getString(
                R.string.esf_btn_view_other_part_txt, getString(R.string.body_back))

            createBitMap(electrodes.filter { e -> e.location == "Chest" }, R.drawable.chest_status)
        }
    }

    /**
     * Function which updates the status feedback text the user sees based on wheter any
     * electrodes are failing or not
     */
    private fun updateStatusText() {
        val temp =  electrodes.filter { electrode -> !electrode.isFailing }

        if (temp.isNotEmpty()) {
            binding.tvStatus.text = getString(R.string.es_electrodes_status_title,
                getString(R.string.es_electrode_error))

            binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_error_24, 0, 0, 0)
        } else {
            binding.tvStatus.text = getString(R.string.es_electrodes_status_title,
                getString(R.string.es_electrode_working))

            binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_check_circle_24, 0, 0, 0)
        }
    }
}