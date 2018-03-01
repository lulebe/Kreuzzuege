package de.lulebe.kreuzzuege

import com.google.firebase.iid.FirebaseInstanceIdService
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId




class FBInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        refreshedToken?.let { token ->
            Log.d("FB_SERVICE", "Refreshed token: " + token)
            (application as Kreuzzuege).apiClient.sendFCMToken(token)
        }
    }

}