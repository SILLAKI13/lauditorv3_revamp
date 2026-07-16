package com.digicoffer.lauditor.FirmProfile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.digicoffer.lauditor.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

class ProfilePhotoEditorFragment : Fragment() {

    private var cropView: CropImageView? = null
    private var seekBar: SeekBar? = null
    private var callback: Callback? = null
    private var editorListener: ProfileEditorListener? = null

    interface Callback {
        fun onCropSaved(croppedFilePath: String)
        fun onChoosePhotoRequested()
    }

    fun interface ProfileEditorListener {
        fun onEditorClosed()
    }

    fun setEditorListener(listener: ProfileEditorListener?) {
        this.editorListener = listener
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editorListener?.onEditorClosed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_profile_photo_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropView = view.findViewById(R.id.cropImageView)
        seekBar = view.findViewById(R.id.zoomSeekBar)
        val tvZoomIn = view.findViewById<TextView>(R.id.tvZoomIn)
        val tvZoomOut = view.findViewById<TextView>(R.id.tvZoomOut)
        val rotateLeft = view.findViewById<ImageView>(R.id.rotate_left)
        val rotateRight = view.findViewById<ImageView>(R.id.rotate_right)

        val tvRotLeft = view.findViewById<TextView>(R.id.tvRotateLeft)
        val tvRotRight = view.findViewById<TextView>(R.id.tvRotateRight)
        val tvCancel = view.findViewById<TextView>(R.id.tvCancel)
        val tvSave = view.findViewById<TextView>(R.id.tvSave)
        val tvChoosePhoto = view.findViewById<TextView>(R.id.tvChoosePhoto)
        val ivBack = view.findViewById<View>(R.id.ivBack)

        val uriString = arguments?.getString(ARG_URI)
        if (uriString == null) {
            Toast.makeText(requireContext(), "No image provided", Toast.LENGTH_SHORT).show()
            popFragment()
            return
        }

        try {
            val uri = Uri.parse(uriString)
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bm = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bm == null) {
                Toast.makeText(requireContext(), "Cannot decode image", Toast.LENGTH_SHORT).show()
                popFragment()
                return
            }

            cropView?.setImageBitmap(bm)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Cannot open image: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            popFragment()
            return
        }

        seekBar?.max = 300
        seekBar?.progress = 0
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) cropView?.setZoomFromSeek(progress, sb.max)
            }

            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        cropView?.setOnZoomChangedListener(object : CropImageView.OnZoomChangedListener {
            override fun onZoomChanged(zoom: Float) {
                val viewCrop = cropView ?: return
                val bar = seekBar ?: return
                val min = viewCrop.getMinScale()
                val max = viewCrop.getMaxScale()
                val progress = ((zoom - min) / (max - min) * bar.max).toInt()
                bar.progress = max(0, min(bar.max, progress))
            }
        })

        tvZoomIn?.setOnClickListener {
            val bar = seekBar ?: return@setOnClickListener
            val step = bar.max / 10
            val next = min(bar.max, bar.progress + step)
            bar.progress = next
            cropView?.setZoomFromSeek(next, bar.max)
        }

        tvZoomOut?.setOnClickListener {
            val bar = seekBar ?: return@setOnClickListener
            val step = bar.max / 10
            val next = max(0, bar.progress - step)
            bar.progress = next
            cropView?.setZoomFromSeek(next, bar.max)
        }

        tvRotLeft?.setOnClickListener {
            cropView?.rotate(-90f)
            seekBar?.progress = 0
        }
        rotateLeft?.setOnClickListener {
            cropView?.rotate(-90f)
            seekBar?.progress = 0
        }

        tvRotRight?.setOnClickListener {
            cropView?.rotate(90f)
            seekBar?.progress = 0
        }
        rotateRight?.setOnClickListener {
            cropView?.rotate(90f)
            seekBar?.progress = 0
        }

        ivBack?.setOnClickListener {
            popFragment()
            callback?.onChoosePhotoRequested()
        }

        tvChoosePhoto?.setOnClickListener {
            popFragment()
            callback?.onChoosePhotoRequested()
        }

        tvCancel?.setOnClickListener { popFragment() }
        tvSave?.setOnClickListener { saveCrop() }
    }

    private fun saveCrop() {
        val cropped = cropView?.getCroppedBitmap(512)
        if (cropped == null) {
            Toast.makeText(requireContext(), "Error creating crop", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val outFile = File(
                requireContext().cacheDir,
                "profile_crop_${System.currentTimeMillis()}.jpg"
            )

            val baos = ByteArrayOutputStream()
            cropped.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            cropped.recycle()
            val fos = FileOutputStream(outFile)
            fos.write(baos.toByteArray())
            fos.flush()
            fos.close()
            baos.close()

            val path = outFile.absolutePath
            callback?.onCropSaved(path)
            popFragment()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Save failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun popFragment() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val ARG_URI = "imageUri"

        @JvmStatic
        fun newInstance(imageUri: Uri): ProfilePhotoEditorFragment {
            val f = ProfilePhotoEditorFragment()
            val args = Bundle()
            args.putString(ARG_URI, imageUri.toString())
            f.arguments = args
            return f
        }
    }
}
