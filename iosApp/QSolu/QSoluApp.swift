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
        let paymentReturnScheme = (Bundle.main.object(forInfoDictionaryKey: "PAYMENT_RETURN_SCHEME") as? String) ?? "solutioniuma"

        IosKoinBridge().doInitKoin(
            siteBrand: siteBrand,
            baseUrl: baseUrl,
            consumerKey: consumerKey,
            consumerSecret: consumerSecret,
            paymentReturnScheme: paymentReturnScheme
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
