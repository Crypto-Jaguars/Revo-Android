import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.airbnb.lottie.LottieAnimationView
import com.example.fideicomisoapproverring.R

class ConnectingDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_connecting)

        val animationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        animationView.setAnimation("Animation - 1738126146129.json")
        animationView.playAnimation()
    }
}
