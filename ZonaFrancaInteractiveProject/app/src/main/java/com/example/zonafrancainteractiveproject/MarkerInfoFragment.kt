package com.example.zonafrancainteractiveproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView

class MarkerInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_detalles, container, false)

        val btnExit = view.findViewById<Button>(R.id.btnExit)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)

        val title = arguments?.getString("title") ?: "Title"
        val description = arguments?.getString("description") ?: "Description"
        val imageResource = arguments?.getInt("imageResource") ?: 0

        tvTitle.text = title
        tvDescription.text = description
        imageView.setImageResource(imageResource)

        btnExit.setOnClickListener {
            fragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(title: String, description: String, imageResource: Int) =
            MarkerInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("description", description)
                    putInt("imageResource", imageResource)
                }
            }
    }
}
