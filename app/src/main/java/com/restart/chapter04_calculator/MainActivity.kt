package com.restart.chapter04_calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import com.restart.chapter04_calculator.model.History
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    //텍스트뷰 지연초기화
    private val expressionTextView: TextView by lazy {
        findViewById(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById(R.id.resultTextView)
    }

    private val historyLayout: View by lazy {
        findViewById(R.id.historyLayout)
    }

    private val historyLinearLayout: LinearLayout by lazy {
        findViewById(R.id.historyLinearLayout)
    }

    lateinit var db: AppDatabase

    //연산자 예외처리를 위한 변수 초기화
    private var isOperator = false

    private var hasOperator = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //lateinit var db 초기화 하기
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()
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
                expressionTextView.text = text.dropLast(1) + operator //텍스트마지막자리에서 -1을 빼고 오퍼레이터를 더한다.

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

        val expressionTexts = expressionTextView.text.split(" ")

        if (expressionTextView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }

        if (expressionTexts.size !=3 && hasOperator){
            Toast.makeText(this, "아직 완성되지 않은 수식입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()){ //확장함수 isNumber
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val expressionText = expressionTextView.text.toString()
        val resultText = calculateExpression()

        //todo 디비에 넣어주는 부분
        // db와 관련된 과정은 메인쓰레드가 아니라 새로운 쓰레드에서 해야함.어떤 쓰레드가 먼저 실행될지는 알 수 없음.
        Thread(Runnable {
            db.historyDao().insertHistory(History(null,expressionText, resultText))
        }).start()

        resultTextView.text = ""
        expressionTextView.text = resultText

        isOperator = false
        hasOperator = false
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

    //클리어 버튼이 눌렸을 경우.
    fun clearButtonClicked(v: View) {

        expressionTextView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }

    fun historyButtonClicked(v: View) {

        //히스토리 레이아웃 보이게 (백그라운드를 화이트로 지정해 주어서 뒤에 버튼패드들이 가려지는 효과)
        historyLayout.isVisible = true

        //todo 디비에서 모든 기록가져오기
        //todo 뷰에 모든 기록 할당하기

        historyLinearLayout.removeAllViews() //리니어 레이아웃 하위의 모든 뷰 삭제됨.

        //db연결 쓰레드만들기 => 메인쓰레드로 연결 runOnUiThread
        Thread(Runnable {
            //db안에 historyDao안에 getAll함수호출 => 최신목록부터 나오도록reversed => 순서대로 꺼내기forEach
            db.historyDao().getAll().reversed().forEach {

                runOnUiThread { //Main쓰레드와 연결 runOnUiThread
                    //만들어둔 화면 가져오기
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    //db에 저장해둔 expression값,result값 텍스트뷰의 텍스트로 반영
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    //historyLinearLayout에 addView로 만들어둔 historyView 사용.리니어 레이아웃이므로 선형으로 표현됨.
                    historyLinearLayout.addView(historyView)

                }

            }
        }).start()
    }

    fun closeHistoryButtonClicked(v: View){
        historyLayout.isVisible = false
    }

    fun historyClearButtonClicked(v: View){
        //todo 디비에서 모든 기록 삭제
        //db접근 쓰레드로
        Thread(Runnable{
            db.historyDao().deleteAll()
        }).start()

        //todo 뷰에서 모든 기록 삭제
        historyLinearLayout.removeAllViews()

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