package nl.hva.vuwearable.ui.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import nl.hva.vuwearable.R
import nl.hva.vuwearable.databinding.FragmentElectrodeStatusBinding


class ElectrodeStatusFragment : Fragment() {

    private var _binding: FragmentElectrodeStatusBinding? = null
    private val binding get() = _binding!!

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
        createBitMap()
        binding.btnViewOtherBodyPart.text = getString(R.string.esf_btn_view_other_part_txt, "Back")

        binding.btnViewOtherBodyPart.setOnClickListener {
            switchBodyView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createBitMap() {
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
        imageView.setBackgroundResource(R.drawable.chest_status)
        canvas.drawCircle(71.5F, 69.5F, 2.1F, paintRed)
        canvas.drawCircle(51.2F, 61F, 2.1F, paintGreen)

        imageView.invalidate()
    }

    private fun switchBodyView() {
        if (currentBodyView == "Chest") {
            binding.ivBody.setBackgroundResource(R.drawable.back_status)
            currentBodyView = "Back"
            binding.btnViewOtherBodyPart.text = getString(R.string.esf_btn_view_other_part_txt, "Chest")
//            binding.ivBody.setImageBitmap(null)
        } else {
            binding.ivBody.setBackgroundResource(R.drawable.chest_status)
            currentBodyView = "Chest"
            binding.btnViewOtherBodyPart.text = getString(R.string.esf_btn_view_other_part_txt, "Back")
        }
    }
}