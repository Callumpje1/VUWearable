package nl.hva.vuwearable.ui.professordashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ProfessorDashboardViewModel(application: Application) : AndroidViewModel(application) {

    val isRecording = MutableLiveData(false)
    val testerId = MutableLiveData("Not Set")

    fun setIsRecording (value: Boolean) {
        isRecording.value = value
    }

    fun setTesterId (value: String) {
        testerId.value = value
    }
}