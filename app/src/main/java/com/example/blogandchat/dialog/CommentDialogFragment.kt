package com.example.blogandchat.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogandchat.R
import com.example.blogandchat.adapter.CommentAdapter
import com.example.blogandchat.databinding.DialogCommentBinding
import com.example.blogandchat.model.Comments
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot


class CommentDialogFragment : BottomSheetDialogFragment() {

	private lateinit var binding: DialogCommentBinding
	private lateinit var listenerRegistration: ListenerRegistration
	private var listComments: MutableList<Comments> = ArrayList()
	private lateinit var postId: String
	private lateinit var adapter: CommentAdapter
	private lateinit var uId: String

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)


	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DialogCommentBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		getDataComment()

		adapter = CommentAdapter(requireContext(), listComments, postId)
		binding.recyclerComments.layoutManager = LinearLayoutManager(requireContext())
		binding.recyclerComments.setHasFixedSize(true)
		binding.recyclerComments.adapter = adapter

		binding.btnAddOmment.setOnClickListener {
			if (binding.edtComment.text != null) {
				val commentsMap: MutableMap<String, Any> = HashMap()
				commentsMap["user"] = uId
				commentsMap["comment"] = binding.edtComment.text.toString()
				commentsMap["timestamp"] = FieldValue.serverTimestamp()
				if (postId != null) {
					val ref = FirebaseFirestore.getInstance()
						.collection("posts/$postId/comments").document()
						.set(commentsMap)
						.addOnSuccessListener {
							dismiss()
						}
						.addOnFailureListener {
							dismiss()
						}
				}
			} else {
				Toast.makeText(
					requireActivity(),
					"ban can nhap comment truoc",
					Toast.LENGTH_SHORT,
				).show()
			}
		}
	}

	/*override fun onStart() {
		val dialog: Dialog? = dialog
		val width = ViewGroup.LayoutParams.MATCH_PARENT
		val height = ViewGroup.LayoutParams.MATCH_PARENT
		dialog?.window?.setLayout(width, height)
		super.onStart()
	}*/

	private fun getDataComment() {
		val query = FirebaseFirestore.getInstance().collection("posts/$postId/comments")
			.orderBy("timestamp", Query.Direction.DESCENDING)
		listenerRegistration = query.addSnapshotListener(
			EventListener<QuerySnapshot?> { value, _ ->
				for (doc in value!!.documentChanges) {
					if (doc.type == DocumentChange.Type.ADDED) {
						val commentsId = doc.document.id
						val comments: Comments =
							doc.document.toObject(Comments::class.java).withId(commentsId)
						listComments.add(comments)
						adapter.notifyDataSetChanged()
					} else {
						adapter.notifyDataSetChanged()
					}
				}
				listenerRegistration.remove()
			},
		)
	}

	companion object {
		fun show(
			fm: FragmentManager?,
			tag: String? = null,
			postId: String? = null,
			uId: String? = null
		) {

			val tg = tag ?: CommentDialogFragment::class.java.name
			val manager = fm ?: return
			synchronized(tg) {
				if (manager.findFragmentByTag(tg) != null) return
				try {
					val confirmDialog = CommentDialogFragment()
					if (postId != null) {
						confirmDialog.postId = postId
					}
					if (uId != null) {
						confirmDialog.uId = uId
					}
					confirmDialog.show(manager, tg)
				} catch (e: IllegalStateException) {

				}
			}
		}
	}
}