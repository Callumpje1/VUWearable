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
import nl.hva.vuwearable.R
import nl.hva.vuwearable.databinding.FragmentElectrodeStatusBinding

class ElectrodeStatusFragment : Fragment() {

    private var _binding: FragmentElectrodeStatusBinding? = null
    private val binding get() = _binding!!

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
            canvas.drawCircle(
                circle.key.value[0],
                circle.key.value[1],
                circle.key.value[2],
                if (circle.value) paintGreen else paintRed
            )
        }

        imageView.invalidate()
    }

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

    private fun checkAreasStatus(circleMap: HashMap<String, Array<Float>>): HashMap<Map.Entry<String, Array<Float>>, Boolean> {
        val statusMap: HashMap<Map.Entry<String, Array<Float>>, Boolean> = HashMap()
        circleMap.entries.forEach { circle ->
            statusMap[circle] = true
        }

        return statusMap
    }
}