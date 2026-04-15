import Foundation

#if canImport(FirebaseCore)
import FirebaseCore
#endif

#if canImport(FirebaseAnalytics)
import FirebaseAnalytics
#endif

#if canImport(FirebaseCrashlytics)
import FirebaseCrashlytics
#endif

enum FirebaseBridge {
    static func configureIfAvailable() {
#if canImport(FirebaseCore)
        guard FirebaseApp.app() == nil else { return }
        guard
            let googleServicePath = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
            let options = FirebaseOptions(contentsOfFile: googleServicePath)
        else {
            print("Firebase configuration skipped: GoogleService-Info.plist is missing or invalid.")
            return
        }

        FirebaseApp.configure(options: options)

#if canImport(FirebaseAnalytics)
        if !isDebugBuild {
            Analytics.setAnalyticsCollectionEnabled(true)
        }
#endif

#if canImport(FirebaseCrashlytics)
        if !isDebugBuild {
            Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(true)
        }
        Crashlytics.crashlytics().setCustomValue(
            Bundle.main.bundleIdentifier ?? "unknown",
            forKey: "bundle_id"
        )
        Crashlytics.crashlytics().setCustomValue(
            (Bundle.main.object(forInfoDictionaryKey: "SITE_BRAND") as? String) ?? "unknown",
            forKey: "site_brand"
        )
        Crashlytics.crashlytics().setCustomValue(
            isDebugBuild ? "debug" : "release",
            forKey: "build_config"
        )
#endif
#endif
    }

    static func logTestAnalyticsEvent() {
#if canImport(FirebaseAnalytics)
        Analytics.logEvent(
            "ios_firebase_test_event",
            parameters: [
                "site_brand": (Bundle.main.object(forInfoDictionaryKey: "SITE_BRAND") as? String) ?? "unknown",
                "build_config": isDebugBuild ? "debug" : "release"
            ]
        )
#endif
    }

    static func triggerTestCrash() -> Never {
        let buildConfig = isDebugBuild ? "debug" : "release"

#if canImport(FirebaseCrashlytics)
        Crashlytics.crashlytics().log("Crashlytics test crash requested. build_config=\(buildConfig)")
        Crashlytics.crashlytics().setCustomValue("deeplink", forKey: "test_crash_source")
        Crashlytics.crashlytics().setCustomValue(buildConfig, forKey: "build_config")
#endif
        fatalError("Crashlytics test crash triggered. build_config=\(buildConfig)")
    }

    private static var isDebugBuild: Bool {
#if DEBUG
        return true
#else
        return false
#endif
    }
}
