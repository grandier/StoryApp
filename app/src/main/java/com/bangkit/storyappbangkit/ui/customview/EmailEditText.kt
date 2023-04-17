package com.bangkit.storyappbangkit.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.opengl.ETC1.isValid
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.bangkit.storyappbangkit.R

class EmailEditText : AppCompatEditText, View.OnTouchListener {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private val emailRegex: Regex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")

    private fun isValid (s: CharSequence) : Boolean{
        return emailRegex.matches(s)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hint = "email@email.com"
    }

    private fun init() {
        setOnTouchListener(this)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && !emailRegex.matches(s.toString())) {
                    error = resources.getString(R.string.invalid_email)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if(s?.let { isValid(it) } == false && s.isNotEmpty()){
                    error = resources.getString(R.string.invalid_email)
                }
            }
        })
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        return false
    }

}