package io.mehow.squashit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.view.AddedAttachmentsView
import io.mehow.squashit.view.AddedCommentView
import io.mehow.squashit.view.AddedCommentWithoutAttachmentsView
import io.mehow.squashit.view.AttachmentsView
import io.mehow.squashit.view.EpicView
import io.mehow.squashit.view.FailedToSubmitView
import io.mehow.squashit.view.InitFailureView
import io.mehow.squashit.view.IssueDescriptionView
import io.mehow.squashit.view.IssueView
import io.mehow.squashit.view.MentionsView
import io.mehow.squashit.view.NewIssueCreatedView
import io.mehow.squashit.view.NewIssueCreatedWithoutAttachmentsView
import io.mehow.squashit.view.NewIssueView
import io.mehow.squashit.view.ReporterView
import io.mehow.squashit.view.UpdateIssueView
import kotlin.reflect.KClass

internal class SquashItInflaterFactory(
  private val chain: LayoutInflater.Factory2,
  private val presenter: ReportPresenter
) : LayoutInflater.Factory2 {
  private val providers = mapOf<KClass<out View>, (Context, AttributeSet, ReportPresenter) -> View>(
      AttachmentsView::class to ::AttachmentsView,
      MentionsView::class to ::MentionsView,
      EpicView::class to ::EpicView,
      NewIssueView::class to ::NewIssueView,
      UpdateIssueView::class to ::UpdateIssueView,
      IssueView::class to ::IssueView,
      IssueDescriptionView::class to ::IssueDescriptionView,
      ReporterView::class to ::ReporterView,
      EpicView::class to ::EpicView,
      InitFailureView::class to ::InitFailureView,
      NewIssueCreatedView::class to ::NewIssueCreatedView,
      NewIssueCreatedWithoutAttachmentsView::class to ::NewIssueCreatedWithoutAttachmentsView,
      AddedCommentView::class to ::AddedCommentView,
      AddedCommentWithoutAttachmentsView::class to ::AddedCommentWithoutAttachmentsView,
      FailedToSubmitView::class to ::FailedToSubmitView,
      AddedAttachmentsView::class to ::AddedAttachmentsView
  ).mapKeys { (key, _) -> key.java.canonicalName!! }

  override fun onCreateView(
    parent: View?,
    name: String,
    context: Context,
    attrs: AttributeSet
  ): View? {
    return providers[name]?.invoke(context, attrs, presenter)
        ?: chain.onCreateView(parent, name, context, attrs)
  }

  override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
    return providers[name]?.invoke(context, attrs, presenter)
        ?: chain.onCreateView(name, context, attrs)
  }
}
