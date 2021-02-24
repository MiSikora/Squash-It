package io.mehow.squashit.report

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.view.AddedAttachmentsView
import io.mehow.squashit.report.view.AttachmentsView
import io.mehow.squashit.report.view.EpicView
import io.mehow.squashit.report.view.FailedToAttachView
import io.mehow.squashit.report.view.FailedToSubmitView
import io.mehow.squashit.report.view.InitFailureView
import io.mehow.squashit.report.view.IssueDescriptionView
import io.mehow.squashit.report.view.IssueView
import io.mehow.squashit.report.view.MentionsView
import io.mehow.squashit.report.view.ReportCreatedView
import io.mehow.squashit.report.view.ReporterView
import io.mehow.squashit.report.view.UpdateIssueView
import kotlin.reflect.KClass

internal class ReportInflaterFactory(
  private val presenter: ReportPresenter,
) : LayoutInflater.Factory2 {
  private val providers = mapOf<KClass<out View>, (Context, AttributeSet, ReportPresenter) -> View>(
      AttachmentsView::class to ::AttachmentsView,
      MentionsView::class to ::MentionsView,
      EpicView::class to ::EpicView,
      UpdateIssueView::class to ::UpdateIssueView,
      IssueView::class to ::IssueView,
      IssueDescriptionView::class to ::IssueDescriptionView,
      ReporterView::class to ::ReporterView,
      EpicView::class to ::EpicView,
      InitFailureView::class to ::InitFailureView,
      ReportCreatedView::class to ::ReportCreatedView,
      FailedToAttachView::class to ::FailedToAttachView,
      FailedToSubmitView::class to ::FailedToSubmitView,
      AddedAttachmentsView::class to ::AddedAttachmentsView
  ).mapKeys { (key, _) -> key.java.canonicalName!! }

  override fun onCreateView(
    parent: View?,
    name: String,
    context: Context,
    attrs: AttributeSet,
  ): View? {
    return providers[name]?.invoke(context, attrs, presenter)
  }

  override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
    return providers[name]?.invoke(context, attrs, presenter)
  }
}
