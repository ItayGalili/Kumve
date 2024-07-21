package il.co.erg.mykumve.util

import android.opengl.Visibility
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

fun <T> StateFlow<Resource<T>>.bindLoadingView(
    lifecycleOwner: LifecycleOwner,
    lifecycleScope: LifecycleCoroutineScope,
    view: View
) {
    lifecycleScope.launchWhenStarted {
        this@bindLoadingView.collectLatest { resource ->
            if (resource.status == Status.LOADING) {
//                view.alpha = 1f
//                view.visibility = View.VISIBLE
            } else {
//                view.alpha = 0f
//                view.visibility = View.GONE
            }
        }
    }
}
