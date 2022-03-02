package com.sunanalyzer.app.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sunanalyzer.app.R
import com.sunanalyzer.app.clickListener.DialogCallBacks
import com.sunanalyzer.app.databinding.BottomsheetDrawingTypeBinding
import com.sunanalyzer.app.utility.AppConstant

class BottomSheetDrawingType : BottomSheetDialogFragment() {
    lateinit var mContext: Context
    private var dialogCallBacks: DialogCallBacks? = null
    lateinit var mBinder: BottomsheetDrawingTypeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinder = DataBindingUtil.inflate(
            inflater,
            R.layout.bottomsheet_drawing_type,
            container,
            false
        )

        mBinder.buttonSubmit.setOnClickListener(clickListener)
        return mBinder.root
    }

    private val clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.buttonSubmit -> {
                val drawingType = when (mBinder.radioGroup.checkedRadioButtonId) {
                    R.id.radioBtnShadow -> AppConstant.DrawingType.SHADOW
                    R.id.radioBtnFreeArea -> AppConstant.DrawingType.FREE_AREA
                    else -> AppConstant.DrawingType.WORK_AREA
                }

                dialogCallBacks!!.dialogClick(0, 0, drawingType)
                dismiss()
            }
        }
    }

    fun newInstance(mContext: Context, dialogCallBack: DialogCallBacks): BottomSheetDrawingType {
        val instance = BottomSheetDrawingType()
        instance.mContext = mContext
        instance.dialogCallBacks = dialogCallBack
        return instance
    }

}