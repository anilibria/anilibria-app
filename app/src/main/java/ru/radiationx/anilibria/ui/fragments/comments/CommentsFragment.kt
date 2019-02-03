package ru.radiationx.anilibria.ui.fragments.comments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.android.synthetic.main.fragment_comments.view.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.comments.CommentsPresenter
import ru.radiationx.anilibria.presentation.comments.CommentsView
import ru.radiationx.anilibria.presentation.release.details.ReleasePresenter
import ru.radiationx.anilibria.ui.adapters.PlaceholderListItem
import ru.radiationx.anilibria.ui.adapters.global.CommentsAdapter
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.UniversalItemDecoration

class CommentsFragment : BaseFragment(), CommentsView {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ID_CODE: String = "release_id_code"
        const val ARG_ITEM: String = "release_item"
    }

    private val commentsAdapter: CommentsAdapter by lazy {
        CommentsAdapter(adapterListener, PlaceholderListItem(
                R.drawable.ic_comment,
                R.string.placeholder_title_comments,
                R.string.placeholder_desc_comments
        ))
    }

    @InjectPresenter
    lateinit var presenter: CommentsPresenter

    @ProvidePresenter
    fun providePresenter(): CommentsPresenter = CommentsPresenter(
            App.injections.releaseRepository,
            App.injections.commentsRepository,
            App.injections.releaseInteractor,
            App.injections.historyRepository,
            App.injections.authRepository,
            (parentFragment as RouterProvider).getRouter(),
            App.injections.linkHandler,
            App.injections.errorHandler
    )

    override fun getBaseLayout(): Int = R.layout.fragment_comments

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("S_DEF_LOG", "ONCRETE $this")
        Log.e("S_DEF_LOG", "ONCRETE REL $arguments, $savedInstanceState")
        val args = savedInstanceState ?: arguments
        args?.also { bundle ->
            bundle.getInt(ARG_ID, -1).let { presenter.releaseId = it }
            bundle.getString(ARG_ID_CODE, null)?.let { presenter.releaseIdCode = it }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentsRefreshLayout.setOnRefreshListener {
            presenter.reloadComments()
        }
        commentsRecyclerView.apply {
            adapter = commentsAdapter
            layoutManager = LinearLayoutManager(this.context)
            //addItemDecoration(ru.radiationx.anilibria.ui.widgets.DividerItemDecoration(this.context))
            addItemDecoration(UniversalItemDecoration().fullWidth(true).spacingDp(1f).includeEdge(false))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0 && recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                        hideSoftwareKeyboard()
                        commentField?.clearFocus()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    /*if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                        hideSoftwareKeyboard()
                        localCommentsRootLayout?.commentField?.clearFocus()
                    }*/
                }
            })
        }
        commentSend.setOnClickListener {
            presenter.onClickSendComment(commentField.text?.toString()?.trim().orEmpty())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_ID, presenter.releaseId)
        outState.putString(ARG_ID_CODE, presenter.releaseIdCode)
        outState.putSerializable(ARG_ITEM, presenter.currentData)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {
        commentsRefreshLayout.isRefreshing = refreshing
    }

    override fun insertMoreComments(comments: List<Comment>) {
        commentsAdapter.addComments(comments)
    }

    override fun setEndlessComments(enable: Boolean) {
        commentsAdapter.endless = enable
    }

    override fun showComments(comments: List<Comment>) {
        commentsAdapter.setComments(comments)
    }

    override fun onCommentSent() {
        hideSoftwareKeyboard()
        commentField.text.clear()
    }

    @SuppressLint("SetTextI18n")
    override fun addCommentText(text: String) {
        commentField.text?.toString()?.also {
            commentField.setText(it + text)
        }
        commentField?.postDelayed({
            commentField?.also {
                it.requestFocus()
                showSoftwareKeyboard(it)
                it.setSelection(it.text.length)
            }
        }, 500)
        //appbarLayout.setExpanded(false, true)
    }

    private val adapterListener = object : CommentsAdapter.ItemListener {
        override fun onLoadMore() {
            presenter.loadMoreComments()
        }

        override fun onClick(item: Comment) {
            context?.let {
                AlertDialog.Builder(it)
                        .setItems(arrayOf("Ответить")) { dialog, which ->
                            when (which) {
                                0 -> presenter.onCommentClick(item)
                            }
                        }
                        .show()
            }
        }
    }
}