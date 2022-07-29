package com.restart.chapter04_calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    //텍스트뷰 지연초기화
    private val expressionTextView: TextView by lazy {
        findViewById(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById(R.id.resultTextView)
    }

    //연산자 예외처리를 위한 변수 초기화
    private var isOperator = false

    private var hasOperator = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //xml에 onClick속성으로 연결한 태그의 id값으로 가져오기.
    fun buttonClicked(v: View) {
        when (v.id) {
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")

            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonMulti -> operatorButtonClicked("x")
            R.id.buttonModulo -> operatorButtonClicked("%")
            R.id.buttonDivider-> operatorButtonClicked("/")

        }

    }
    //숫자버튼이 눌리면 할일 함수로 지정
    private fun numberButtonClicked(number: String) {

        if (isOperator){
            expressionTextView.append(" ")
        }
        isOperator= false

        //expressionTextView 스페이스바를 통해 분기처리()..잘이해안됨.
       val expressionText = expressionTextView.text.split(" ")

        if(expressionText.isNotEmpty() && expressionText.last().length > 15){
            Toast.makeText(this, "15자리 까지만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }else if (expressionText.last().isEmpty() && number == "0" ){
            Toast.makeText(this, "0은 제일 앞에 올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        expressionTextView.append(number) //예외처리가 끝나면 number를 붙힘.
        resultTextView.text = calculateExpression() //계산된 함수의 값 resultTextView의 텍스트에 대입.


    }
    //연산자 버튼이 눌리면 할일 함수로 지정
    private fun operatorButtonClicked(operator: String) {

        //계산은 숫자가 먼저 나와야 하므로 연산자 버튼을 먼저 누를경우 . 바로리턴.
        if (expressionTextView.text.isEmpty()){

            Toast.makeText(this, "계산할 숫자부터 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        when{
            //오퍼레이터를 눌렀는데 다른 기능의 오퍼레이터를 다시누를 경우 ( ex) +를 누르고 -를 누름)
            isOperator -> {
                val text = expressionTextView.text.toString()
                expressionTextView.text = text.dropLast(1) + operator

            }
            //오퍼레이터를 한번만 사용할 수 있게
            hasOperator -> {
                Toast.makeText(this, "연산자는 한번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                //예외처리 완료후 텍스트뷰에 오퍼레이터 붙여주기.
                expressionTextView.append(" $operator")
            }
        }
        //텍스트뷰에 오퍼레이터 색깔 세팅
        val ssb = SpannableStringBuilder(expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            expressionTextView.text.length -1,
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        expressionTextView.text = ssb

        isOperator = true
        hasOperator = true

    }

    fun resultButtonClicked(v: View) {

    }

    private fun calculateExpression(): String{
        //expressionTextView 스플릿 스페이스로 분기처리해서 변수에 저장.분기처리하면 인덱스 번호로 접근가능.
        val expressionTexts = expressionTextView.text.split(" ")

        //연산자가 없거나 expressionTexts의 크기가 3이아니면 return""/ 0번째,2번째 인덱스에 숫자가 없으면 return""
        if (hasOperator.not() || expressionTexts.size !=3){
            return ""
        } else if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()){ //확장함수 isNumber
            return ""
        }
        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when(op){
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "x" -> (exp1 * exp2).toString()
            "/" -> (exp1 / exp2).toString()
            "%" -> (exp1 % exp2).toString()
            else -> ""

        }
    }

    fun clearButtonClicked(v: View) {

    }

    fun historyButtonClicked(v: View) {

    }

}

//스트링을 확장하는 확장함수 만들기 => 객체.함수이름으로 오면 그 객체를 확장하게 됨.마치 그 객체가 가지고 있는 함수처럼 만들어줌.
fun String.isNumber(): Boolean{
    return try{
        this.toBigInteger() //this: 원래있던 String, 무한대의 숫자까지 저장하기 위해 BigInteger로 치환.
        true //성공시 트루 반환.
    }
    //숫자로 제대로 치환이 되지 않을 경우 넘버포멧익셉션이 반환됨.
    catch (e: NumberFormatException) {
        return false
    }
}