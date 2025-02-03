import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.airbnb.lottie.LottieAnimationView
import com.example.fideicomisoapproverring.R

class ConnectingDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_connecting)

        // 🔹 Fondo transparente para evitar bordes blancos
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 🔹 Hacer el diálogo más compacto en altura
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.6).toInt(), // 60% del ancho de la pantalla
            (context.resources.displayMetrics.heightPixels * 0.4).toInt() // 🔹 30% del alto de la pantalla (antes era WRAP_CONTENT)
        )

        val animationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        animationView.setAnimation("Animation - 1738126146129.json")
        animationView.playAnimation()
    }
}
