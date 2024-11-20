package com.franklinwireless.android.jexkids.fragments

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.franklinwireless.android.jexkids.databinding.FragmentOnboarding1Binding

class OnboardingFragment : Fragment() {
    private var title: String? = null
    private var description: String? = null
    private var imageResource = 0
    private var linkurl: String? = null
    private var linkaddress: String? = null
    private lateinit var tvTitle: AppCompatTextView
    private lateinit var tvDescription: AppCompatTextView
    private lateinit var image: LottieAnimationView
    private lateinit var link: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = requireArguments().getString(ARG_PARAM1)
            description = requireArguments().getString(ARG_PARAM2)
            imageResource = requireArguments().getInt(ARG_PARAM3)
            linkurl = requireArguments().getString(ARG_PARAM4)
            linkaddress = requireArguments().getString(ARG_PARAM5)
        }
    }

    private var _binding: FragmentOnboarding1Binding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnboarding1Binding.inflate(inflater, container, false)
        val view = binding.root

        tvTitle = binding.textOnboardingTitle
        tvDescription = binding.textOnboardingDescription
        image = binding.imageOnboarding
        link = binding.textOnboardingLink
        tvTitle.text = title
        tvDescription.text = description
        image.setAnimation(imageResource)
        link.text = linkurl
        link.paintFlags = link.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        link.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkaddress))
            startActivity(browserIntent)
        }
        //link.movementMethod = LinkMovementMethod.getInstance();
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val ARG_PARAM3 = "param3"
        private const val ARG_PARAM4 = "param4"
        private const val ARG_PARAM5 = "param5"
        fun newInstance(
            title: String?,
            description: String?,
            imageResource: Int,
            link: String?,
        linkaddress: String?
        ): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, title)
            args.putString(ARG_PARAM2, description)
            args.putInt(ARG_PARAM3, imageResource)
            args.putString(ARG_PARAM4, link)
            args.putString(ARG_PARAM5, linkaddress)
            fragment.arguments = args
            return fragment
        }
    }
}
