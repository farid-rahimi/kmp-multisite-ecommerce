//
//  QSoluApp.swift
//  QSolu
//
//  Created by Farid Rahimi on 25/02/2026.
//

import SwiftUI
import sharedUIKit

@main
struct QSoluApp: App {
    init() {
        let siteBrand = (Bundle.main.object(forInfoDictionaryKey: "SITE_BRAND") as? String) ?? "SITE_A"
        let baseUrl = (Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String) ?? "https://qeshminora.com/"
        let consumerKey = (Bundle.main.object(forInfoDictionaryKey: "API_CONSUMER_KEY") as? String) ?? ""
        let consumerSecret = (Bundle.main.object(forInfoDictionaryKey: "API_CONSUMER_SECRET") as? String) ?? ""

        IosKoinBridge().doInitKoin(
            siteBrand: siteBrand,
            baseUrl: baseUrl,
            consumerKey: consumerKey,
            consumerSecret: consumerSecret
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
