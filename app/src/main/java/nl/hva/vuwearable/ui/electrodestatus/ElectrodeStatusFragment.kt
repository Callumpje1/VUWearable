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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import nl.hva.vuwearable.R
import nl.hva.vuwearable.databinding.FragmentElectrodeStatusBinding
import nl.hva.vuwearable.databinding.FragmentHomeBinding
import nl.hva.vuwearable.databinding.FragmentProfesorDashboardBinding
import nl.hva.vuwearable.ui.udp.UDPViewModel


class ElectrodeStatusFragment : Fragment() {

    private var _binding: FragmentElectrodeStatusBinding? = null
    private val binding get() = _binding!!

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createBitMap() {
        var bitmap: Bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap = bitmap.copy(bitmap.config, true)

        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true

        val imageView: ImageView = binding.ivBody
        imageView.setImageBitmap(bitmap)
        imageView.setBackgroundResource(R.drawable.chest_status)
        canvas.drawCircle(50F, 50F, 3F, paint)

        imageView.invalidate()
    }
}