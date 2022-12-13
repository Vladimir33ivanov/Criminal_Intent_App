package android.criminal_intent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.lifecycle.Observer
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import android.text.format.DateFormat
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.PackageManagerCompat.LOG_TAG
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = ""
private const val LOG_TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHOTO = 3
private const val DATE_FORMAT = "EEE, MMM, dd"

@Suppress("RedundantSamConstructor")
class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

//    private lateinit var pickContactContract: ActivityResultContract<Uri, Uri?>
//    private lateinit var pickContactCallback: ActivityResultCallback<Uri?>
//    private lateinit var pickContactLauncher: ActivityResultLauncher<Uri>
//    private var launcher:ActivityResultLauncher<>
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this)[CrimeDetailViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)

//        pickContactContract = object : ActivityResultContract<Uri, Uri?>() {
//            override fun createIntent(context: Context, input: Uri): Intent {
//                Log.d(TAG, "createIntent() called")
//                return Intent(Intent.ACTION_PICK, input)
//            }
//            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
//                Log.d(TAG, "parseResult() called")
//                if(resultCode != Activity.RESULT_OK || intent == null)
//                    return null
//                return intent.data
//            }
//        }
//        pickContactCallback = ActivityResultCallback<Uri?> { contactUri: Uri? ->
//            Log.d(TAG, "onActivityResult() called with result: ")
//            // handle the actual result later to query the database for the contact
//        }
//        pickContactLauncher = registerForActivityResult(pickContactContract, pickContactCallback)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "android.criminal_intent.fileprovider",
                        photoFile)
                    updateUI()
                }
            })
    }
    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher{  // создаем анонимный класс который реализует интерфейс

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Оставленно пустым специально
            }

            override fun onTextChanged( // Возвращает строку для создания зоголовка
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // Оставленно пустым специально
            }
        }
        titleField.addTextChangedListener(titleWatcher)


        solvedCheckBox.apply {
            setOnCheckedChangeListener{ _, isChecked ->
            crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener{
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }
        timeButton.setOnClickListener{
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener{
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            setOnClickListener {
                pickContact.launch(null)
            }
        }


        photoButton.apply {
//            val packageManager: PackageManager = requireActivity().packageManager
//
//            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            val resolvedActivity: ResolveInfo? =
//                packageManager.resolveActivity(captureImage,
//                    PackageManager.MATCH_DEFAULT_ONLY)
//            if (resolvedActivity == null){
//                isEnabled = false
//            }

            setOnClickListener {
                takePicture.launch(photoUri)
//                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//
//                val cameraActivities: List<ResolveInfo> =
//                    packageManager.queryIntentActivities(captureImage,
//                        PackageManager.MATCH_DEFAULT_ONLY)
//
//                for (cameraActivity in cameraActivities) {
//                    requireActivity().grantUriPermission(
//                        cameraActivity.activityInfo.packageName,
//                        photoUri,
//                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//                }

                // заменить на актуальное
            }
        }

    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            // The image was saved into the given Uri -> do something with it
            Log.d (TAG, "We took a picture...")
            updatePhotoView()    // You'll need this later for listing 16.16
        }
    }

    private val pickContact = registerForActivityResult(ActivityResultContracts.PickContact()) { contactUri ->
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        val cursor = contactUri?.let {
            requireActivity().contentResolver.query (it, queryFields, null, null, null)
        }
        cursor?.use {
            // Verify cursor contains at least one result
            if (it.count > 0) {
                // Pull out first column of the first row of data, that's our suspect name
                it.moveToFirst()
                val suspect = it.getString(0)
                crime.suspect = suspect
                crimeDetailViewModel.saveCrime(crime)
                suspectButton.text = suspect
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onDateSelected(date: Date){
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun updateUI(){
        titleField.setText(crime.title)
        val crimeDate = SimpleDateFormat("EEEE, MMM d, yyyy")
            .format(this.crime.date)
        dateButton.text = crimeDate
        val crimeTime = SimpleDateFormat("hh:mm a")
            .format(this.crime.date)
        timeButton.text = crimeTime
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if ( crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }

        updatePhotoView()
    }

    private fun updatePhotoView(){
        if (photoFile.exists()){
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri : Uri? = data.data // указазать, для каких полей ваш запрос должен возвращать значения
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0){
                        return
                    }
                        // имя подозреваемого первый столбец первой строки
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }


            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                updatePhotoView()
            }
        }

    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        }else{
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        }else{
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report,
                crime.title, dateString, solvedString, suspect)
    }
    companion object {

        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply { putSerializable(ARG_CRIME_ID, crimeId) }
            return CrimeFragment().apply { arguments = args
            }
        }
    }
}