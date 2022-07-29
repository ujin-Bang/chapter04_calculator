package com.restart.chapter04_calculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.restart.chapter04_calculator.model.History

@Dao // HistoryDao 룸에 연결된 Dao : 룸에 연결된 Entity(History)의 조회, 업그레이드, 삭제등 함수로 지정.
interface HistoryDao {

    @Query("SELECT * FROM history") //value에 SQLQuery작성 : 히스토리라는 테이블에서 모든 히스토리 Entity 가져오기.
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history") //history 테이블 전체 삭제
    fun deleteAll()

//    @Delete  => 하나의 테이블만 삭제하고 싶은 경우
//    fun delete(history: History)

//    조건에 맞는 결과만 추출하기
//    result가 where문의 조건으로 걸리면서 모든 result가 인자로 들어온 result를 가져오게 된다. 하나만 가져오고 싶으면 LIMTIT1,History
//    @Query("SELECT * FROM history WHERE result LIKE :result")
//    fun findByResult(result: String): List<History>
}