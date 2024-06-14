package com.example.mykumve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mykumve.databinding.TravelManagerViewBinding

class TravelManager : Fragment() {

    private var _binding : TravelManagerViewBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TravelManagerViewBinding.inflate(inflater, container, false)
        /*binding.finishBtn.setOnClickListener {
            //val bundle = bundleOf("title" to binding.itemTitel.text.toString(), "description" to binding.itemDescription.text.toString())
            //findNavController().navigate(R.id.action_addItemFragment_to_allItemsFragment, bundle)
            val item = Item(binding.itemTitel.text.toString(), binding.itemDescription.text.toString(), null)
            ItemManager.add(item)
            findNavController().navigate(R.id.action_addItemFragment_to_allItemsFragment)
        }*/
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}