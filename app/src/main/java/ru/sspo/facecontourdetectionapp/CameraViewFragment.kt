package ru.sspo.facecontourdetectionapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.sspo.facecontourdetectionapp.databinding.FragmentCameraViewBinding

class CameraViewFragment : Fragment() {

    private var _binding: FragmentCameraViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraManager: CameraManager
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var surfaceView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionRequest()
    }

    @SuppressLint("MissingPermission")
    private fun permissionRequest() {
        // Инициализация ActivityResultLauncher для запроса разрешения на камеру
        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Требуется разрешение", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.let { surfaceView = it.surfaceView }

        // Получаем экземпляр CameraManager
        cameraManager = requireContext().getSystemService(CameraManager::class.java)

        // Проверяем разрешение на камеру
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        val permission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun openCamera() {
        val cameraId = cameraManager.cameraIdList[0]
        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(p0: CameraDevice) {

                }

                override fun onDisconnected(p0: CameraDevice) {
                    p0.close()
                }

                override fun onError(p0: CameraDevice, p1: Int) {
                    p0.close()
                    Log.e("CameraFragment", "Ошибка открытия камеры: $p1")
                }
            }, null)
        } catch (e: CameraAccessException) {
            Log.e("CameraFragment", "Ошибка доступа к камере: ${e.message}")
        }
    }


    // Перечисляем доступные камеры
    private fun listAvailableCameras() {
        try {
            // Получаем список идентификаторов камер
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                // Получаем характеристики камеры
                val characteristics: CameraCharacteristics =
                    cameraManager.getCameraCharacteristics(cameraId)
                val cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

                // Проверяем, является ли камера фронтальной или задней
                if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.d("CameraActivity", "Доступна фронтальная камера: $cameraId")
                } else if (cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.d("CameraActivity", "Доступна задняя камера: $cameraId")
                }
            }
        } catch (e: CameraAccessException) {
            Log.e("CameraActivity", "Ошибка доступа к камере: ${e.message}")
        }
    }

    // Перечисляем доступные камеры и их характеристик
    private fun listCameraCharacteristics() {
        try {
            // Получаем список идентификаторов камер
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                // Получаем характеристики камеры
                val characteristics: CameraCharacteristics =
                    cameraManager.getCameraCharacteristics(cameraId)

                // Пример получения информации о характеристиках
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                val sensorOrientation =
                    characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)

                // Логируем информацию о камере
                Log.d("CameraFragment", "Камера ID: $cameraId")
                Log.d("CameraFragment", "Направление объектива: $lensFacing")
                Log.d("CameraFragment", "Ориентация сенсора: $sensorOrientation")
            }
        } catch (e: CameraAccessException) {
            Log.e("CameraFragment", "Ошибка доступа к камере: ${e.message}")
        }
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}