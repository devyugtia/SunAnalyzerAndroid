package com.sunanalyzer.app.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sunanalyzer.app.R
import com.sunanalyzer.app.clickListener.DialogCallBacks
import com.sunanalyzer.app.databinding.BottomsheetWorkAreaSettingBinding
import com.sunanalyzer.app.model.SpinnerOptionModel
import com.sunanalyzer.app.model.WorkAreaConfigurationModel
import com.sunanalyzer.app.utility.MinMaxInputFilter
import com.sunanalyzer.app.utility.Text
import com.sunanalyzer.app.utility.toMyDouble

class BottomSheetWorkAreaSetting : BottomSheetDialogFragment() {
    private val selectedLayoutTypeList: ArrayList<SpinnerOptionModel> = ArrayList()
    private var selectedLayoutTypeId: Int = 1
    lateinit var mContext: Context
    private var dialogCallBacks: DialogCallBacks? = null
    lateinit var mBinder: BottomsheetWorkAreaSettingBinding
    private var settings = WorkAreaConfigurationModel()
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
            R.layout.bottomsheet_work_area_setting,
            container,
            false
        )
        setData()
        mBinder.edtPitchDistance.filters = arrayOf<InputFilter>(MinMaxInputFilter(0.1, 1000.0))
        mBinder.edtPanelTilt.filters = arrayOf<InputFilter>(MinMaxInputFilter(0.1, 360.0))
        mBinder.edtAzimuthAngel.filters = arrayOf<InputFilter>(MinMaxInputFilter(0.1, 360.0))
        mBinder.edtWpPerPanel.filters = arrayOf<InputFilter>(MinMaxInputFilter(0.1, 100000.0))
        mBinder.edtOffset.filters =
            arrayOf<InputFilter>(MinMaxInputFilter(0.1, settings.totalWorkArea / 2))
        mBinder.buttonSubmit.setOnClickListener(clickListener)
        mBinder.edtLayoutType.setOnClickListener(clickListener)
        return mBinder.root
    }

    private fun setData() {
        selectedLayoutTypeList.add(SpinnerOptionModel(1, "", "Layout 1 (1x1)", true))
        selectedLayoutTypeList.add(SpinnerOptionModel(2, "", "Layout 2 (1x2)"))
        selectedLayoutTypeList.add(SpinnerOptionModel(3, "", "Layout 3 (1x4)"))
        selectedLayoutTypeId = settings.layoutTypeId
        val layoutTypeModel = selectedLayoutTypeList.find { it.id == selectedLayoutTypeId }
        if (layoutTypeModel != null) {
            mBinder.edtLayoutType.setText(layoutTypeModel.name)
        }

        mBinder.edtPitchDistance.setText("" + settings.pitchDistance)
        mBinder.edtPanelTilt.setText("" + settings.panelTilt)
        mBinder.edtAzimuthAngel.setText("" + settings.azimuthAngle)
        mBinder.edtWpPerPanel.setText("" + settings.wpPerPanel)
        mBinder.edtOffset.setText("" + settings.offset)
        if (settings.panelType) {
            mBinder.radioHorizontal.isChecked = true
            mBinder.radioVertical.isChecked = false
        } else {
            mBinder.radioHorizontal.isChecked = false
            mBinder.radioVertical.isChecked = true
        }
    }

    private val clickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.buttonSubmit -> {
                settings.pitchDistance = getValueFromEdittext(mBinder.edtPitchDistance)
                settings.panelTilt = getValueFromEdittext(mBinder.edtPanelTilt)
                settings.azimuthAngle = getValueFromEdittext(mBinder.edtAzimuthAngel)
                settings.wpPerPanel = getValueFromEdittext(mBinder.edtWpPerPanel)
                settings.offset = getValueFromEdittext(mBinder.edtOffset)
                settings.panelType = mBinder.radioHorizontal.isChecked
                val bundle = Bundle()
                bundle.putParcelable("workAreaConfigurationModel", settings)
                dialogCallBacks!!.onDismiss(bundle)
                dismiss()
            }
            R.id.edtLayoutType -> {
                val dialog: CommonSpinnerDialog? =
                    this@BottomSheetWorkAreaSetting.mContext?.let { context ->
                        CommonSpinnerDialog().newInstance(
                            context,
                            selectedLayoutTypeList,
                            "Layout Type",
                            selectedLayoutTypeId,
                            object : DialogCallBacks {
                                override fun onDismiss(bundle: Bundle) {}
                                override fun dialogClick(position: Int, id: Int, value: String) {
                                    selectedLayoutTypeId = id
                                    settings.layoutTypeId = id
                                    settings.layoutType = value
                                    mBinder.edtLayoutType.setText(value)
                                }
                            })
                    }
                dialog!!.show(this@BottomSheetWorkAreaSetting.childFragmentManager, "dialog")
            }
        }
    }


    private fun getValueFromEdittext(editText: EditText): Double {
        return if (editText.Text().isNotEmpty() && !editText.Text().startsWith(".")) editText.Text()
            .toMyDouble() else 0.0
    }

    fun newInstance(
        mContext: Context,
        settings: WorkAreaConfigurationModel,
        dialogCallBack: DialogCallBacks
    ): BottomSheetWorkAreaSetting {
        val instance = BottomSheetWorkAreaSetting()
        instance.mContext = mContext
        instance.settings = settings
        instance.dialogCallBacks = dialogCallBack
        return instance
    }

}