package com.example.navigationwalkers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class Frag2 : Fragment() {
    private var view: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.frag2, container, false)

        // 버튼 참조
        val button1 = view?.findViewById<Button>(R.id.button1)
        val button2 = view?.findViewById<Button>(R.id.button2)

        // 버튼 클릭 이벤트 처리
        button1?.setOnClickListener {
            // 버튼 3을 클릭했을 때의 동작을 여기에 추가
            // 버튼의 배경 색을 custom_button_pressed.xml로 변경
            button1?.setBackgroundResource(R.drawable.custom_button_pressed)
            button2?.setBackgroundResource(R.drawable.custom_button) // 버튼 5의 배경 원래 색상으로 변경
        }

        button2?.setOnClickListener {
            // 버튼 5를 클릭했을 때의 동작을 여기에 추가
            // 버튼의 배경 색을 custom_button_pressed.xml로 변경
            button2?.setBackgroundResource(R.drawable.custom_button_pressed)
            button1?.setBackgroundResource(R.drawable.custom_button) // 버튼 3의 배경 원래 색상으로 변경
        }


        return view
    }
}