package com.example.fideicomisoapproverring.core.logging;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SecureAuditLogger_Factory implements Factory<SecureAuditLogger> {
  private final Provider<Context> contextProvider;

  private final Provider<AppLogger> appLoggerProvider;

  public SecureAuditLogger_Factory(Provider<Context> contextProvider,
      Provider<AppLogger> appLoggerProvider) {
    this.contextProvider = contextProvider;
    this.appLoggerProvider = appLoggerProvider;
  }

  @Override
  public SecureAuditLogger get() {
    return newInstance(contextProvider.get(), appLoggerProvider.get());
  }

  public static SecureAuditLogger_Factory create(Provider<Context> contextProvider,
      Provider<AppLogger> appLoggerProvider) {
    return new SecureAuditLogger_Factory(contextProvider, appLoggerProvider);
  }

  public static SecureAuditLogger newInstance(Context context, AppLogger appLogger) {
    return new SecureAuditLogger(context, appLogger);
  }
}
