package com.example.blogandchat.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.adapter.MessageAdapter
import com.example.blogandchat.databinding.ActivitySpecificChatBinding
import com.example.blogandchat.model.Message
import com.example.blogandchat.utils.AppKey
import com.example.blogandchat.utils.convertUriToBitmap
import com.example.blogandchat.utils.createFileFromUri
import com.example.blogandchat.utils.getVideoFileSize
import com.example.blogandchat.utils.isVideoFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class SpecificChat : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var messageList: MutableList<Message> = ArrayList()
    private lateinit var messagesAdapter: MessageAdapter
    private lateinit var binding: ActivitySpecificChatBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_PICKER = 2

    private val viewModel: ChatViewModel by viewModels()
    var mSenderUid: String = ""
    var mReceiverUid: String = ""
    var mReceiverName: String = ""
    var senderRoom = ""
    var receiverRoom = ""
    lateinit var databaseReference: DatabaseReference
    val postListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            coroutineScope.launch(IO) {
                for (snapshot1 in snapshot.children) {
                    val message: Message? = snapshot1.getValue(Message::class.java)
                    if (message != null && (messageList.find { it.timeStamp == message.timeStamp } == null)) {
                        if (message.type == 0) {
                            AppKey.decrypt(message.message)?.let {
                                message.message = it
                            }
                            messageList.add(message)
                        } else {
                            val data = AppKey.decryptByteArray(message.message)
                            if (data.isNotEmpty()) {
                                message.message = Base64.encodeToString(data, Base64.DEFAULT)
                                messageList.add(message)
                            }
                        }
                    }
                }
                withContext(Main) {
                    messagesAdapter.notifyDataSetChanged()
                    binding.recycleChat.scrollToPosition(messageList.size - 1)
                }
            }

            if (snapshot.exists() && snapshot.children.last().exists()) {
                viewModel.updateLastMessage(
                    mReceiverUid,
                    snapshot.children.last().getValue(Message::class.java) ?: Message()
                )
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(">>>>>>>>>>>>>", databaseError.message)

        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri?.let { isVideoFile(this, it) } == true) {
            CoroutineScope(IO).launch {
                val size = getVideoFileSize(uri, this@SpecificChat)?.div(1024 * 1024) ?: 0
                withContext(Main) {
                    if (size <= 10L) {
                        uri.let { viewModel.uploadVideo(it, senderRoom, receiverRoom) }
                    } else {
                        Toast.makeText(
                            this@SpecificChat,
                            "Video có kích thước tối đa 10MB",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else {
            val bitmap = convertUriToBitmap(this, uri)
            bitmap?.let {
                viewModel.uploadImage(
                    it, senderRoom = senderRoom,
                    mReceiverUid = mReceiverUid,
                    receiverRoom = receiverRoom
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_specific_chat
        )

        requestPermission()


        mSenderUid = firebaseAuth.uid.toString()
        mReceiverUid = intent.getStringExtra("receiveruid").toString()
        mReceiverName = intent.getStringExtra("name").toString()
        val publicKey = intent.getStringExtra("publicKey")
        AppKey.calculateKey(publicKey.toString())


        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.recycleChat.layoutManager = linearLayoutManager

        senderRoom = mSenderUid + mReceiverUid
        receiverRoom = mReceiverUid + mSenderUid

        databaseReference =
            firebaseDatabase.reference.child("chats").child(senderRoom).child("messages")


        messagesAdapter = MessageAdapter(this@SpecificChat, messageList)

        binding.recycleChat.adapter = messagesAdapter


        binding.tvUsernameChat.text = mReceiverName

        val uri = intent.getStringExtra("imageuri")
        if (uri!!.isEmpty()) {
            Toast.makeText(applicationContext, "null is received", Toast.LENGTH_SHORT).show()
        } else {
            Glide.with(this).load(uri).into(binding.imgAvt)
        }

        binding.edtChat.onFocusChangeListener = OnFocusChangeListener { p0, p1 ->
            binding.imgNew.visibility = if (p1) View.GONE else View.VISIBLE
            binding.imgPick.visibility = if (p1) View.GONE else View.VISIBLE
        }

        binding.imgNew.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.imgPick.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }

        binding.imgBack.setOnClickListener { finish() }


        binding.imageBtnChat.setOnClickListener(View.OnClickListener {
            val enterdMessage = binding.edtChat.text.toString()
            if (enterdMessage.isEmpty()) {
                Toast.makeText(applicationContext, "Enter meassage first", Toast.LENGTH_SHORT)
                    .show()
            } else {
                viewModel.sendMessageNormal(
                    senderRoom = senderRoom,
                    enterdMessage = enterdMessage,
                    mReceiverUid = mReceiverUid,
                    receiverRoom = receiverRoom
                )
                binding.edtChat.text = null
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()
        databaseReference.addValueEventListener(postListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStop() {
        databaseReference.removeEventListener(postListener)
        super.onStop()
    }

    fun hideSoftKeyboard(activity: Activity?) {
        if (activity == null) {
            return
        }
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            binding.edtChat.clearFocus()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null && !ev.isButtonPressed(binding.imageBtnChat.id)) {
            hideSoftKeyboard(this)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val extras = data?.extras
            val bitmap = extras?.get("data") as Bitmap
            viewModel.uploadImage(
                bitmap, senderRoom = senderRoom,
                mReceiverUid = mReceiverUid,
                receiverRoom = receiverRoom
            )
        }
    }

    fun requestPermission() {
        val permissions: Array<String> = try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val requestedPermissions = info.requestedPermissions
            if (requestedPermissions != null && requestedPermissions.isNotEmpty()) {
                requestedPermissions
            } else {
                arrayOf()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            arrayOf()
        }

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_DENIED
//        ) {
//
//            ActivityCompat.requestPermissions(
//                this,
//                listOf(Manifest.permission.CAMERA).toTypedArray(),
//                99
//            );
//        }
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            return
        }
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            // Thông báo cho người dùng rằng không có ứng dụng camera nào được cài đặt
            Toast.makeText(this, "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICKER)
    }


//    private val activityResultLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        )
//        { permissions ->
//            // Handle Permission granted/rejected
//            var permissionGranted = true
//            permissions.entries.forEach {
//                if (it.key in arrayListOf(
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ) && !it.value
//                )
//                    permissionGranted = false
//            }
//            if (!permissionGranted) {
//                Toast.makeText(
//                    baseContext,
//                    "Permission request denied",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                //startCamera()
//            }
//        }

//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            // Used to bind the lifecycle of cameras to the lifecycle owner
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Preview
//            val preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
//                }
//
//            // Select back camera as a default
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll()
//
//                // Bind use cases to camera
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview)
//
//            } catch(exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val response = RetrofitInstance.api.postNotification(notification)
//            if (response.isSuccessful) {
//                //Toast.makeText(spe)
//            }
//        } catch (e: Exception) {
//
//        }
//    }
}