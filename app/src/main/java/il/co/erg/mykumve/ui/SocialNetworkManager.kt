package il.co.erg.mykumve.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import il.co.erg.mykumve.R
import il.co.erg.mykumve.databinding.TravelNetworkBinding

class SocialNetworkManager : Fragment() {

    private var _binding : TravelNetworkBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TravelNetworkBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.msBtn.setOnClickListener{
            findNavController().navigate(R.id.action_networkManager_to_mainScreenManager)
        }

        binding.reportsBtn.setOnClickListener{
            findNavController().navigate(R.id.action_networkManager_to_UsersReports)
        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}