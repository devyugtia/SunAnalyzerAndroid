package com.sunanalyzer.app.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sunanalyzer.app.R
import com.sunanalyzer.app.adapter.CommonSpinnerAdapter
import com.sunanalyzer.app.clickListener.DialogCallBacks
import com.sunanalyzer.app.clickListener.RecyclerRowClick
import com.sunanalyzer.app.databinding.DialogCommonSpinnerBinding
import com.sunanalyzer.app.model.SpinnerOptionModel
import com.sunanalyzer.app.utility.Text


class CommonSpinnerDialog : BottomSheetDialogFragment(), RecyclerRowClick {

    var mContext: Context? = null
    var arrayList: ArrayList<SpinnerOptionModel> = ArrayList()
    var dialogCallBacks: DialogCallBacks? = null
    var isClearSelectionVisible: Boolean = true
    var selectedId: Int = -1
    var headerTitle: String = ""
    lateinit var mBinder: DialogCommonSpinnerBinding
    lateinit var adaptar: CommonSpinnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinder = DataBindingUtil.inflate(inflater, R.layout.dialog_common_spinner, container, true)
        adaptar = CommonSpinnerAdapter(arrayList, this)
        mBinder.rvOptionList.adapter = adaptar
        mBinder.isShowSearchBar = arrayList.size > 20
        mBinder.imageViewCancel.setOnClickListener {
            dismiss()
        }
        mBinder.cardViewClearSelection.visibility =
            if (isClearSelectionVisible && selectedId > -1) View.VISIBLE else View.GONE
        mBinder.cardViewClearSelection.setOnClickListener(clickListener)
        mBinder.txtDialogTitle.text = headerTitle

        mBinder.layoutSearchBar.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                adaptar.getFilter()!!.filter(mBinder.layoutSearchBar.edtSearch.Text().toString())
                true
            }
            false
        }

        mBinder.layoutSearchBar.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mBinder.isTyping = mBinder.layoutSearchBar.edtSearch.Text().isNotEmpty()
                if (adaptar != null) {
                    mBinder.isTyping = mBinder.layoutSearchBar.edtSearch.Text().isNotEmpty()
                    adaptar.getFilter()!!.filter(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        mBinder.layoutSearchBar.imageViewCancel.setOnClickListener(clickListener)
        setSelectedItem()
        return mBinder.root
    }

    private val clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.cardViewClearSelection -> {
                dialogCallBacks!!.dialogClick(
                    -1,
                    -1,
                    ""
                )
                dismiss()
            }
            R.id.imageViewCancel -> {
                if (adaptar != null) {
                    mBinder.layoutSearchBar.edtSearch.setText("")
                    adaptar.getFilter()!!.filter("")
                }
            }
        }
    }

    private fun setSelectedItem() {
        for (position in arrayList.indices) {
            arrayList[position].isSelected =
                selectedId == arrayList[position].id && selectedId != -1
        }
    }

    fun newInstance(
        mContext: Context, transportViewModelLists: ArrayList<SpinnerOptionModel>, title: String,
        selectedId: Int, dialogCallBack: DialogCallBacks, isClearSelectionVisible: Boolean = false
    ): CommonSpinnerDialog {
        val commonSpinnerDialog = CommonSpinnerDialog()
        commonSpinnerDialog.mContext = mContext
        commonSpinnerDialog.headerTitle = title
        commonSpinnerDialog.arrayList = transportViewModelLists
        commonSpinnerDialog.selectedId = selectedId
        commonSpinnerDialog.dialogCallBacks = dialogCallBack
        commonSpinnerDialog.isClearSelectionVisible = isClearSelectionVisible
        return commonSpinnerDialog
    }

    override fun rowClick(pos: Int, flag: Int) {

        val model = adaptar.getSelectedItem()
        if (model != null) {
            dialogCallBacks!!.dialogClick(
                pos,
                model.id,
                if (model.value.isEmpty()) model.name else model.value
            )
            dismiss()
        }

    }

    override fun loadMore(pos: Int) {

    }
}