package com.fixmax.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DailySummaryDao_Impl implements DailySummaryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DailySummaryEntity> __insertionAdapterOfDailySummaryEntity;

  public DailySummaryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDailySummaryEntity = new EntityInsertionAdapter<DailySummaryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `daily_summaries` (`date`,`totalSteps`,`distanceMeters`,`activeMinutes`,`goalSteps`,`lastUpdatedMs`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DailySummaryEntity entity) {
        statement.bindString(1, entity.getDate());
        statement.bindLong(2, entity.getTotalSteps());
        statement.bindDouble(3, entity.getDistanceMeters());
        statement.bindLong(4, entity.getActiveMinutes());
        statement.bindLong(5, entity.getGoalSteps());
        statement.bindLong(6, entity.getLastUpdatedMs());
      }
    };
  }

  @Override
  public Object insertOrUpdate(final DailySummaryEntity summary,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDailySummaryEntity.insert(summary);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementDailySteps(final String date, final long delta,
      final float stepLengthMeters, final long timestampMs,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DailySummaryDao.DefaultImpls.incrementDailySteps(DailySummaryDao_Impl.this, date, delta, stepLengthMeters, timestampMs, __cont), $completion);
  }

  @Override
  public Flow<DailySummaryEntity> observeDailySummary(final String date) {
    final String _sql = "SELECT * FROM daily_summaries WHERE date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_summaries"}, new Callable<DailySummaryEntity>() {
      @Override
      @Nullable
      public DailySummaryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSteps");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfActiveMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "activeMinutes");
          final int _cursorIndexOfGoalSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "goalSteps");
          final int _cursorIndexOfLastUpdatedMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedMs");
          final DailySummaryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final long _tmpTotalSteps;
            _tmpTotalSteps = _cursor.getLong(_cursorIndexOfTotalSteps);
            final float _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getFloat(_cursorIndexOfDistanceMeters);
            final int _tmpActiveMinutes;
            _tmpActiveMinutes = _cursor.getInt(_cursorIndexOfActiveMinutes);
            final long _tmpGoalSteps;
            _tmpGoalSteps = _cursor.getLong(_cursorIndexOfGoalSteps);
            final long _tmpLastUpdatedMs;
            _tmpLastUpdatedMs = _cursor.getLong(_cursorIndexOfLastUpdatedMs);
            _result = new DailySummaryEntity(_tmpDate,_tmpTotalSteps,_tmpDistanceMeters,_tmpActiveMinutes,_tmpGoalSteps,_tmpLastUpdatedMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDailySummary(final String date,
      final Continuation<? super DailySummaryEntity> $completion) {
    final String _sql = "SELECT * FROM daily_summaries WHERE date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DailySummaryEntity>() {
      @Override
      @Nullable
      public DailySummaryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSteps");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfActiveMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "activeMinutes");
          final int _cursorIndexOfGoalSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "goalSteps");
          final int _cursorIndexOfLastUpdatedMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedMs");
          final DailySummaryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final long _tmpTotalSteps;
            _tmpTotalSteps = _cursor.getLong(_cursorIndexOfTotalSteps);
            final float _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getFloat(_cursorIndexOfDistanceMeters);
            final int _tmpActiveMinutes;
            _tmpActiveMinutes = _cursor.getInt(_cursorIndexOfActiveMinutes);
            final long _tmpGoalSteps;
            _tmpGoalSteps = _cursor.getLong(_cursorIndexOfGoalSteps);
            final long _tmpLastUpdatedMs;
            _tmpLastUpdatedMs = _cursor.getLong(_cursorIndexOfLastUpdatedMs);
            _result = new DailySummaryEntity(_tmpDate,_tmpTotalSteps,_tmpDistanceMeters,_tmpActiveMinutes,_tmpGoalSteps,_tmpLastUpdatedMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
