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
import nl.hva.vuwearable.ui.chart.scichart.ChartViewModel

class ElectrodeStatusFragment : Fragment() {

    private var _binding: FragmentElectrodeStatusBinding? = null
    private val binding get() = _binding!!

    private val chartViewModel: ChartViewModel by activityViewModels()

    private var currentBodyView: String = "Chest"

    // key = wire color, value = array[cx, cy, circleRadius)
    private val chestCircleCoordinates: HashMap<String, Array<Float>> = hashMapOf(
        "BLACK" to arrayOf(71.5F, 69.5F, 2.1F),
        "YELLOW" to arrayOf(51.2F, 61F, 2.1F),
        "DEVICE" to arrayOf(52F, 23F, 4F)
    )

    private val backCircleCoordinates: HashMap<String, Array<Float>> = hashMapOf(
        "BLUE" to arrayOf(47F, 66.5F, 2.1F),
        "DARK_BLUE" to arrayOf(51F, 11.5F, 2.1F)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElectrodeStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createBitMap(checkAreasStatus(chestCircleCoordinates), R.drawable.chest_status)

        binding.btnViewOtherBodyPart.text = getString(R.string.esf_btn_view_other_part_txt, "Back")

        binding.btnViewOtherBodyPart.setOnClickListener {
            switchBodyView()
        }

        chartViewModel.sectionAMeasurements.observe(viewLifecycleOwner) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Function which creates a bitmap and draws the given circles onto the given image.
     * @param circles the HashMap with the circles and if their sensors are working or not, true results
     * in a green circle, false results in a red circle
     * @param drawableBackground the ID of the drawable image to use as background to draw on
     */
    private fun createBitMap(circles: HashMap<Map.Entry<String, Array<Float>>, Boolean>,
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

        circles.entries.forEach { circle ->
            // draw a circle on the canvas for each given circle
            canvas.drawCircle(
                circle.key.value[0],
                circle.key.value[1],
                circle.key.value[2],
                if (circle.value) paintGreen else paintRed
            )
        }

        imageView.invalidate()
    }

    /**
     * Function which checks the status of the given circles and their corresponding sensors.
     * @param circleMap the HashMap with circles to check the status of.
     * @return a HashMap with the items of the circleMap as keys with their boolean
     * whether they are working or not
     */
    private fun checkAreasStatus(circleMap: HashMap<String, Array<Float>>): HashMap<Map.Entry<String, Array<Float>>, Boolean> {
        val statusMap: HashMap<Map.Entry<String, Array<Float>>, Boolean> = HashMap()
        circleMap.entries.forEach { circle ->
            //TODO: Check the actual values using the decoding features.
            statusMap[circle] = true
        }

        return statusMap
    }

    /**
     * Function which switches views between the chest and back
     */
    private fun switchBodyView() {
        if (currentBodyView == getString(R.string.body_chest)) {
            currentBodyView = getString(R.string.body_back)
            binding.btnViewOtherBodyPart.text = getString(
                R.string.esf_btn_view_other_part_txt, getString(R.string.body_chest))

            createBitMap(checkAreasStatus(backCircleCoordinates), R.drawable.back_status)
        } else {
            currentBodyView = getString(R.string.body_chest)
            binding.btnViewOtherBodyPart.text = getString(
                R.string.esf_btn_view_other_part_txt, getString(R.string.body_back))

            createBitMap(checkAreasStatus(chestCircleCoordinates), R.drawable.chest_status)
        }
    }
}