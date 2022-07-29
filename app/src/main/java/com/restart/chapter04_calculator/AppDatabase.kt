package com.restart.chapter04_calculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.restart.chapter04_calculator.dao.HistoryDao
import com.restart.chapter04_calculator.model.History

//어노테이션데이터베이스라고 알려주기. 데이터베이스에 히스토리를 테이블로사용하겠다. 버전작성.앱 업데이트시 db가 변경되었을때 마이그레이션을 해주어서 데이터가 날라가지 않게.
@Database(entities = [History::class], version = 1)
abstract class AppDatabase: RoomDatabase() { //추상클래스로 룸데이터베이스 상속
    abstract fun historyDao(): HistoryDao
}