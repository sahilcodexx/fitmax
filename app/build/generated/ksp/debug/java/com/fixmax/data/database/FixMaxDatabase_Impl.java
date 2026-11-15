package com.fixmax.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FixMaxDatabase_Impl extends FixMaxDatabase {
  private volatile SensorCheckpointDao _sensorCheckpointDao;

  private volatile DailySummaryDao _dailySummaryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `device_profiles` (`id` INTEGER NOT NULL, `deviceModel` TEXT NOT NULL, `androidVersion` INTEGER NOT NULL, `accelName` TEXT NOT NULL, `gyroName` TEXT NOT NULL, `stepCounterName` TEXT NOT NULL, `hasHardwareStepCounter` INTEGER NOT NULL, `hasHardwareStepDetector` INTEGER NOT NULL, `recommendedTrackingMode` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sensor_checkpoints` (`id` INTEGER NOT NULL, `timestampMs` INTEGER NOT NULL, `lastCumulativeHardwareReading` INTEGER NOT NULL, `accumulatedStepsBeforeReboot` INTEGER NOT NULL, `bootTimestampMs` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_summaries` (`date` TEXT NOT NULL, `totalSteps` INTEGER NOT NULL, `distanceMeters` REAL NOT NULL, `activeMinutes` INTEGER NOT NULL, `goalSteps` INTEGER NOT NULL, `lastUpdatedMs` INTEGER NOT NULL, PRIMARY KEY(`date`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f3032745f4a946f4343c1e752d123436')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `device_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `sensor_checkpoints`");
        db.execSQL("DROP TABLE IF EXISTS `daily_summaries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsDeviceProfiles = new HashMap<String, TableInfo.Column>(9);
        _columnsDeviceProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("deviceModel", new TableInfo.Column("deviceModel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("androidVersion", new TableInfo.Column("androidVersion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("accelName", new TableInfo.Column("accelName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("gyroName", new TableInfo.Column("gyroName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("stepCounterName", new TableInfo.Column("stepCounterName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("hasHardwareStepCounter", new TableInfo.Column("hasHardwareStepCounter", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("hasHardwareStepDetector", new TableInfo.Column("hasHardwareStepDetector", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceProfiles.put("recommendedTrackingMode", new TableInfo.Column("recommendedTrackingMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeviceProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeviceProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeviceProfiles = new TableInfo("device_profiles", _columnsDeviceProfiles, _foreignKeysDeviceProfiles, _indicesDeviceProfiles);
        final TableInfo _existingDeviceProfiles = TableInfo.read(db, "device_profiles");
        if (!_infoDeviceProfiles.equals(_existingDeviceProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "device_profiles(com.fixmax.data.database.DeviceProfileEntity).\n"
                  + " Expected:\n" + _infoDeviceProfiles + "\n"
                  + " Found:\n" + _existingDeviceProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsSensorCheckpoints = new HashMap<String, TableInfo.Column>(5);
        _columnsSensorCheckpoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorCheckpoints.put("timestampMs", new TableInfo.Column("timestampMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorCheckpoints.put("lastCumulativeHardwareReading", new TableInfo.Column("lastCumulativeHardwareReading", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorCheckpoints.put("accumulatedStepsBeforeReboot", new TableInfo.Column("accumulatedStepsBeforeReboot", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSensorCheckpoints.put("bootTimestampMs", new TableInfo.Column("bootTimestampMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSensorCheckpoints = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSensorCheckpoints = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSensorCheckpoints = new TableInfo("sensor_checkpoints", _columnsSensorCheckpoints, _foreignKeysSensorCheckpoints, _indicesSensorCheckpoints);
        final TableInfo _existingSensorCheckpoints = TableInfo.read(db, "sensor_checkpoints");
        if (!_infoSensorCheckpoints.equals(_existingSensorCheckpoints)) {
          return new RoomOpenHelper.ValidationResult(false, "sensor_checkpoints(com.fixmax.data.database.SensorCheckpointEntity).\n"
                  + " Expected:\n" + _infoSensorCheckpoints + "\n"
                  + " Found:\n" + _existingSensorCheckpoints);
        }
        final HashMap<String, TableInfo.Column> _columnsDailySummaries = new HashMap<String, TableInfo.Column>(6);
        _columnsDailySummaries.put("date", new TableInfo.Column("date", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailySummaries.put("totalSteps", new TableInfo.Column("totalSteps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailySummaries.put("distanceMeters", new TableInfo.Column("distanceMeters", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailySummaries.put("activeMinutes", new TableInfo.Column("activeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailySummaries.put("goalSteps", new TableInfo.Column("goalSteps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailySummaries.put("lastUpdatedMs", new TableInfo.Column("lastUpdatedMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDailySummaries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDailySummaries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDailySummaries = new TableInfo("daily_summaries", _columnsDailySummaries, _foreignKeysDailySummaries, _indicesDailySummaries);
        final TableInfo _existingDailySummaries = TableInfo.read(db, "daily_summaries");
        if (!_infoDailySummaries.equals(_existingDailySummaries)) {
          return new RoomOpenHelper.ValidationResult(false, "daily_summaries(com.fixmax.data.database.DailySummaryEntity).\n"
                  + " Expected:\n" + _infoDailySummaries + "\n"
                  + " Found:\n" + _existingDailySummaries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "f3032745f4a946f4343c1e752d123436", "bb7299b6e65440b217bf75d759a274ea");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "device_profiles","sensor_checkpoints","daily_summaries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `device_profiles`");
      _db.execSQL("DELETE FROM `sensor_checkpoints`");
      _db.execSQL("DELETE FROM `daily_summaries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SensorCheckpointDao.class, SensorCheckpointDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DailySummaryDao.class, DailySummaryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SensorCheckpointDao checkpointDao() {
    if (_sensorCheckpointDao != null) {
      return _sensorCheckpointDao;
    } else {
      synchronized(this) {
        if(_sensorCheckpointDao == null) {
          _sensorCheckpointDao = new SensorCheckpointDao_Impl(this);
        }
        return _sensorCheckpointDao;
      }
    }
  }

  @Override
  public DailySummaryDao dailySummaryDao() {
    if (_dailySummaryDao != null) {
      return _dailySummaryDao;
    } else {
      synchronized(this) {
        if(_dailySummaryDao == null) {
          _dailySummaryDao = new DailySummaryDao_Impl(this);
        }
        return _dailySummaryDao;
      }
    }
  }
}
