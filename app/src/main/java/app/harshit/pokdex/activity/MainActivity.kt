package app.harshit.pokdex.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import app.harshit.pokdex.HandleFileUpload
import app.harshit.pokdex.R
import app.harshit.pokdex.adapter.PokemonAdapter
import app.harshit.pokdex.model.Pokemon
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource
import com.google.firebase.storage.FirebaseStorage
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.pokemon_sheet.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


val pokeArray: Array<String> = arrayOf("Abra", "Aerodactyl", "Alakazam", "Arbok", "Arcanine", "Articuno", "Beedrill", "Bellsprout",
        "Blastoise", "Bulbasaur", "Butterfree", "Caterpie", "Chansey", "Charizard", "Charmander", "Charmeleon", "Clefable", "Clefairy", "Cloyster", "Cubone", "Dewgong",
        "Diglett", "Ditto", "Dodrio", "Doduo", "Dragonair", "Dragonite", "Dratini", "Drowzee", "Dugtrio", "Eevee", "Ekans", "Electabuzz",
        "Electrode", "Exeggcute", "Exeggutor", "Farfetchd", "Fearow", "Flareon", "Gastly", "Gengar", "Geodude", "Gloom",
        "Golbat", "Goldeen", "Golduck", "Golem", "Graveler", "Grimer", "Growlithe", "Gyarados", "Haunter", "Hitmonchan",
        "Hitmonlee", "Horsea", "Hypno", "Ivysaur", "Jigglypuff", "Jolteon", "Jynx", "Kabuto",
        "Kabutops", "Kadabra", "Kakuna", "Kangaskhan", "Kingler", "Koffing", "Krabby", "Lapras", "Lickitung", "Machamp",
        "Machoke", "Machop", "Magikarp", "Magmar", "Magnemite", "Magneton", "Mankey", "Marowak", "Meowth", "Metapod",
        "Mew", "Mewtwo", "Moltres", "Mrmime", "Muk", "Nidoking", "Nidoqueen", "Nidorina", "Nidorino", "Ninetales",
        "Oddish", "Omanyte", "Omastar", "Onix", "Paras", "Parasect", "Persian", "Pidgeot", "Pidgeotto", "Pidgey",
        "Pikachu", "Pinsir", "Poliwag", "Poliwhirl", "Poliwrath", "Ponyta", "Porygon", "Primeape", "Psyduck", "Raichu",
        "Rapidash", "Raticate", "Rattata", "Rhydon", "Rhyhorn", "Sandshrew", "Sandslash", "Scyther", "Seadra",
        "Seaking", "Seel", "Shellder", "Slowbro", "Slowpoke", "Snorlax", "Spearow", "Squirtle", "Starmie", "Staryu",
        "Tangela", "Tauros", "Tentacool", "Tentacruel", "Vaporeon", "Venomoth", "Venonat", "Venusaur", "Victreebel",
        "Vileplume", "Voltorb", "Vulpix", "Wartortle", "Weedle", "Weepinbell", "Weezing", "Wigglytuff", "Zapdos", "Zubat")

class MainActivity : BaseCameraActivity(), HandleFileUpload {
    companion object {
        /** Dimensions of inputs.  */
        const val DIM_IMG_SIZE_X = 224
        const val DIM_IMG_SIZE_Y = 224
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 3
        const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128.0f
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val rootRef = FirebaseStorage.getInstance().reference.child("pokemon")
    private lateinit var currentBitmap: Bitmap
    private val pokemonList = mutableListOf<Pokemon>()
    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    private lateinit var imgData: ByteBuffer
    private lateinit var fireBaseInterpreter: FirebaseModelInterpreter
    private lateinit var inputOutputOptions: FirebaseModelInputOutputOptions

    private lateinit var itemAdapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupBottomSheet(R.layout.pokemon_sheet)
        imgData = ByteBuffer.allocateDirect(
                4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder())

        btnRetry.setOnClickListener {
            if (cameraView.visibility == View.VISIBLE) showPreview() else hidePreview()
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            itemAdapter.setList(mutableListOf())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_UPLOAD, getString(R.string.feedback_notification), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        rvLabel.layoutManager = LinearLayoutManager(this)
        itemAdapter = PokemonAdapter(pokemonList, this)
        rvLabel.adapter = itemAdapter
//        Load a cloud model using the FirebaseCloudModelSource Builder class
        val cloudSource = FirebaseCloudModelSource.Builder("pokedex")
                .enableModelUpdates(true)
                .build()

        //Registering the cloud model loaded above with the ModelManager Singleton
        FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource)

        //Load a local model using the FirebaseLocalModelSource Builder class
        val fireBaseLocalModelSource = FirebaseLocalModelSource.Builder("pokedex")
                .setAssetFilePath("pokedex_84.tflite")
                .build()

        //Registering the model loaded above with the ModelManager Singleton
        FirebaseModelManager.getInstance().registerLocalModelSource(fireBaseLocalModelSource)

        val firebaseModelOptions = FirebaseModelOptions.Builder()
                .setLocalModelName("pokedex")
                .setCloudModelName("pokedex")
                .build()

        fireBaseInterpreter = FirebaseModelInterpreter.getInstance(firebaseModelOptions)!!

        inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 149))
                .build()
    }

    override fun onClick(v: View?) {
        fabProgressCircle.show()
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                CameraUtils.decodeBitmap(jpeg) {
                    currentBitmap = it
                    val scaledBitmap = Bitmap.createScaledBitmap(it, 224, 224, false)
                    getPokemonFromBitmap(scaledBitmap)
                    showPreview()
                    imagePreview.setImageBitmap(it)
                }
            }
        })
        cameraView.capturePicture()
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer {
        //Clear the ByteBuffer for a new image
        imgData.rewind()
        bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val currPixel = intValues[pixel++]
                imgData.putFloat(((currPixel shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((currPixel and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return imgData
    }

    private fun getPokemonFromBitmap(bitmap: Bitmap?) {
        val inputs = FirebaseModelInputs.Builder()
                .add(convertBitmapToByteBuffer(bitmap)) // add() as many input arrays as your model requires
                .build()

        fireBaseInterpreter.run(inputs, inputOutputOptions)
                ?.addOnSuccessListener {
                    val pokeList = mutableListOf<Pokemon>()
                    /**
                     * Run a foreach loop through the output float array containing the probabilities
                     * corresponding to each label
                     * @see pokeArray to know what labels are supported
                     */
                    it.getOutput<Array<FloatArray>>(0)[0].forEachIndexed { index, fl ->
                        //Only consider a pokemon when the accuracy is more than 20%
                        if (fl > .20)
                            pokeList.add(Pokemon(pokeArray[index], fl))
                    }
                    itemAdapter.setList(pokeList)
                    fabProgressCircle.hide()
                    sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                ?.addOnFailureListener {
                    it.printStackTrace()
                    fabProgressCircle.hide()
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }

    override fun uploadImageToStorage(name: String) {
        val baos = ByteArrayOutputStream()
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
        val data = baos.toByteArray()
        rootRef.child(name)
                .child("${name.toLowerCase()}${System.currentTimeMillis()}.jpg")
                .putBytes(data)
                .addOnSuccessListener {
                    notificationManager.cancel(420)
                    Toast.makeText(this, getString(R.string.thanks_for_feedback), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    notificationManager.cancel(420)
                    Toast.makeText(this, getString(R.string.feedback_failed), Toast.LENGTH_SHORT).show()
                }

        showProgressNotification()
    }

    private fun showProgressNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_UPLOAD)
                .setContentTitle(getString(R.string.sending_feedback))
                .setContentText(getString(R.string.feedback_in_progress))
                .setSmallIcon(R.drawable.ic_cloud_upload)
                .setProgress(100, 0, true)
                .build()

        notificationManager.notify(420, notification)
    }
}
