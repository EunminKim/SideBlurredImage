package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.myapplication.databinding.ActivityMainBinding
import com.sanholo.sideblur.SideBlurredImage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.button.setOnClickListener {
            val requestUrl = String.format("https://picsum.photos/id/237/%s/%s", binding.width.text,
                binding.height.text)
            Glide.with(this).asBitmap().load(requestUrl).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d("TAG", "onResourceReady: ")
                    val converted = SideBlurredImage.convert(
                        binding.root.context, resource, binding.imageView.width,
                        binding.imageView.height, 20.0F
                    )
                    binding.imageView.setImageBitmap(converted)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}