package com.fixmax.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SensorCheckpointDao_Impl implements SensorCheckpointDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SensorCheckpointEntity> __insertionAdapterOfSensorCheckpointEntity;

  public SensorCheckpointDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSensorCheckpointEntity = new EntityInsertionAdapter<SensorCheckpointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sensor_checkpoints` (`id`,`timestampMs`,`lastCumulativeHardwareReading`,`accumulatedStepsBeforeReboot`,`bootTimestampMs`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SensorCheckpointEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestampMs());
        statement.bindLong(3, entity.getLastCumulativeHardwareReading());
        statement.bindLong(4, entity.getAccumulatedStepsBeforeReboot());
        statement.bindLong(5, entity.getBootTimestampMs());
      }
    };
  }

  @Override
  public Object saveCheckpoint(final SensorCheckpointEntity checkpoint,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSensorCheckpointEntity.insert(checkpoint);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCheckpoint(final Continuation<? super SensorCheckpointEntity> $completion) {
    final String _sql = "SELECT * FROM sensor_checkpoints WHERE id = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SensorCheckpointEntity>() {
      @Override
      @Nullable
      public SensorCheckpointEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfLastCumulativeHardwareReading = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCumulativeHardwareReading");
          final int _cursorIndexOfAccumulatedStepsBeforeReboot = CursorUtil.getColumnIndexOrThrow(_cursor, "accumulatedStepsBeforeReboot");
          final int _cursorIndexOfBootTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "bootTimestampMs");
          final SensorCheckpointEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final long _tmpLastCumulativeHardwareReading;
            _tmpLastCumulativeHardwareReading = _cursor.getLong(_cursorIndexOfLastCumulativeHardwareReading);
            final long _tmpAccumulatedStepsBeforeReboot;
            _tmpAccumulatedStepsBeforeReboot = _cursor.getLong(_cursorIndexOfAccumulatedStepsBeforeReboot);
            final long _tmpBootTimestampMs;
            _tmpBootTimestampMs = _cursor.getLong(_cursorIndexOfBootTimestampMs);
            _result = new SensorCheckpointEntity(_tmpId,_tmpTimestampMs,_tmpLastCumulativeHardwareReading,_tmpAccumulatedStepsBeforeReboot,_tmpBootTimestampMs);
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
