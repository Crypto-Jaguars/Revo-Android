package com.example.fideicomisoapproverring.core.logging;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppLogger_Factory implements Factory<AppLogger> {
  @Override
  public AppLogger get() {
    return newInstance();
  }

  public static AppLogger_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppLogger newInstance() {
    return new AppLogger();
  }

  private static final class InstanceHolder {
    private static final AppLogger_Factory INSTANCE = new AppLogger_Factory();
  }
}
