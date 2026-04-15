import SwiftUI
import sharedUIKit
import UIKit
import Foundation

private enum RootTab: Int, CaseIterable {
    case home = 0
    case category = 1
    case cart = 2
    case account = 3

    func localizedTitle(languageCode: String) -> String {
        switch (self, languageCode) {
        case (.home, "ar"): return "الرئيسية"
        case (.category, "ar"): return "الفئات"
        case (.cart, "ar"): return "السلة"
        case (.account, "ar"): return "حسابي"
        case (.home, "fa"): return "خانه"
        case (.category, "fa"): return "دسته‌بندی"
        case (.cart, "fa"): return "سبد خرید"
        case (.account, "fa"): return "حساب کاربری"
        case (.home, _): return "Home"
        case (.category, _): return "Category"
        case (.cart, _): return "Cart"
        case (.account, _): return "Account"
        }
    }

    var systemImage: String {
        switch self {
        case .home: return "house"
        case .category: return "square.grid.2x2"
        case .cart: return "cart"
        case .account: return "person"
        }
    }

    var selectedSystemImage: String {
        switch self {
        case .home: return "house.fill"
        case .category: return "square.grid.2x2.fill"
        case .cart: return "cart.fill"
        case .account: return "person.fill"
        }
    }
}

struct ContentView: View {
    @State private var selectedTab: RootTab = .home
    @State private var cartItemCount: Int = 0
    @State private var tabBarVisibilityByTab: [RootTab: Bool] = Dictionary(
        uniqueKeysWithValues: RootTab.allCases.map { ($0, true) }
    )
    @State private var paymentReturnPayload: PaymentReturnPayload?
    @State private var paymentReturnEventId: Int64 = 0
    private let brandTint = BrandPalette.current

    var body: some View {
        let languageCode = currentAppLanguageCode()
        let appLayoutDirection = layoutDirectionForLanguage(languageCode)
        TabView(selection: $selectedTab) {
            NativeTabHostView(
                tab: .home,
                selectedTab: selectedTab,
                paymentReturnPayload: nil,
                isTabBarVisible: tabBarVisibilityByTab[.home] ?? true,
                onCartCountChanged: { count in
                    cartItemCount = Int(count)
                },
                onBottomBarVisibilityChanged: { visibility in
                    tabBarVisibilityByTab[.home] = visibility > 0
                }
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .ignoresSafeArea(.keyboard, edges: .bottom)
            .tabItem {
                Label(RootTab.home.localizedTitle(languageCode: languageCode), systemImage: selectedTab == .home ? RootTab.home.selectedSystemImage : RootTab.home.systemImage)
            }
            .tag(RootTab.home)

            NativeTabHostView(
                tab: .category,
                selectedTab: selectedTab,
                paymentReturnPayload: nil,
                isTabBarVisible: tabBarVisibilityByTab[.category] ?? true,
                onCartCountChanged: { count in
                    cartItemCount = Int(count)
                },
                onBottomBarVisibilityChanged: { visibility in
                    tabBarVisibilityByTab[.category] = visibility > 0
                }
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .ignoresSafeArea(.keyboard, edges: .bottom)
            .tabItem {
                Label(RootTab.category.localizedTitle(languageCode: languageCode), systemImage: selectedTab == .category ? RootTab.category.selectedSystemImage : RootTab.category.systemImage)
            }
            .tag(RootTab.category)

            NativeTabHostView(
                tab: .cart,
                selectedTab: selectedTab,
                paymentReturnPayload: paymentReturnPayload,
                isTabBarVisible: tabBarVisibilityByTab[.cart] ?? true,
                onCartCountChanged: { count in
                    cartItemCount = Int(count)
                },
                onBottomBarVisibilityChanged: { visibility in
                    tabBarVisibilityByTab[.cart] = visibility > 0
                }
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .ignoresSafeArea(.keyboard, edges: .bottom)
            .tabItem {
                Label(RootTab.cart.localizedTitle(languageCode: languageCode), systemImage: selectedTab == .cart ? RootTab.cart.selectedSystemImage : RootTab.cart.systemImage)
            }
            .badge(cartBadgeText)
            .tag(RootTab.cart)

            NativeTabHostView(
                tab: .account,
                selectedTab: selectedTab,
                paymentReturnPayload: nil,
                isTabBarVisible: tabBarVisibilityByTab[.account] ?? true,
                onCartCountChanged: { count in
                    cartItemCount = Int(count)
                },
                onBottomBarVisibilityChanged: { visibility in
                    tabBarVisibilityByTab[.account] = visibility > 0
                }
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .ignoresSafeArea(.keyboard, edges: .bottom)
            .tabItem {
                Label(RootTab.account.localizedTitle(languageCode: languageCode), systemImage: selectedTab == .account ? RootTab.account.selectedSystemImage : RootTab.account.systemImage)
            }
            .tag(RootTab.account)
        }
        .tint(brandTint)
        .toolbar(activeTabBarVisible ? .visible : .hidden, for: .tabBar)
        .toolbarBackground(.hidden, for: .tabBar)
        .background(TabBarConfigurator(isVisible: activeTabBarVisible))
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .environment(\.layoutDirection, appLayoutDirection)
        .onAppear {
            configureTransparentTabBar()
        }
        .onOpenURL { url in
            guard let parsed = parsePaymentReturn(from: url) else { return }
            paymentReturnEventId += 1
            paymentReturnPayload = PaymentReturnPayload(
                status: parsed.status,
                orderId: parsed.orderId,
                eventId: paymentReturnEventId
            )
            selectedTab = .cart
        }
    }

    private var cartBadgeText: String? {
        guard cartItemCount > 0 else { return nil }
        return cartItemCount > 99 ? "99+" : "\(cartItemCount)"
    }

    private var activeTabBarVisible: Bool {
        tabBarVisibilityByTab[selectedTab] ?? true
    }
}

private func currentAppLanguageCode() -> String {
    if let code = UserDefaults.standard.string(forKey: "app_language"), !code.isEmpty {
        return code.lowercased()
    }
    if let preferred = Locale.preferredLanguages.first {
        let normalized = preferred.lowercased()
        if normalized.hasPrefix("ar") { return "ar" }
        if normalized.hasPrefix("fa") { return "fa" }
    }
    return "en"
}

private func layoutDirectionForLanguage(_ languageCode: String) -> LayoutDirection {
    switch languageCode {
    case "ar", "fa":
        return .rightToLeft
    default:
        return .leftToRight
    }
}

private enum BrandPalette {
    static var current: Color {
        let siteBrand = (Bundle.main.object(forInfoDictionaryKey: "SITE_BRAND") as? String) ?? "SITE_A"
        if siteBrand == "SITE_B" {
            return Color(red: 0.0 / 255.0, green: 94.0 / 255.0, blue: 46.0 / 255.0)
        }
        return Color(red: 241.0 / 255.0, green: 92.0 / 255.0, blue: 52.0 / 255.0)
    }
}

private func configureTransparentTabBar() {
    let appearance = UITabBarAppearance()
    appearance.configureWithTransparentBackground()
    appearance.backgroundColor = .clear
    appearance.shadowColor = .clear

    let tabBarAppearance = UITabBar.appearance()
    tabBarAppearance.standardAppearance = appearance
    if #available(iOS 15.0, *) {
        tabBarAppearance.scrollEdgeAppearance = appearance
    }
    tabBarAppearance.isTranslucent = true
    tabBarAppearance.backgroundImage = UIImage()
    tabBarAppearance.shadowImage = UIImage()
    tabBarAppearance.backgroundColor = .clear
    tabBarAppearance.barTintColor = .clear
    tabBarAppearance.itemPositioning = .fill
    tabBarAppearance.itemSpacing = 0
}

private struct TabBarConfigurator: UIViewControllerRepresentable {
    let isVisible: Bool

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = UIViewController()
        controller.view.backgroundColor = .clear
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        DispatchQueue.main.async {
            guard let tabBarController = uiViewController.findTabBarController() else { return }
            let tabBar = tabBarController.tabBar
            tabBar.isTranslucent = true
            tabBar.backgroundImage = UIImage()
            tabBar.shadowImage = UIImage()
            tabBar.backgroundColor = .clear
            tabBar.barTintColor = .clear
            tabBar.itemPositioning = .fill
            tabBar.itemSpacing = 0
            tabBar.layoutMargins = UIEdgeInsets(top: 0, left: 6, bottom: 0, right: 6)
            tabBar.isHidden = !isVisible
            tabBarController.additionalSafeAreaInsets.bottom = isVisible ? -6 : 0
        }
    }
}

private struct NativeTabHostView: UIViewControllerRepresentable {
    let tab: RootTab
    let selectedTab: RootTab
    let paymentReturnPayload: PaymentReturnPayload?
    let isTabBarVisible: Bool
    let onCartCountChanged: (Int32) -> Void
    let onBottomBarVisibilityChanged: (Int32) -> Void

    final class Coordinator {
        private let host: IosProductListComposeHost
        var handledEventId: Int64 = 0

        init(
            tab: RootTab,
            onCartCountChanged: @escaping (Int32) -> Void,
            onBottomBarVisibilityChanged: @escaping (Int32) -> Void
        ) {
            host = IosProductListComposeHost(
                initialTabIndex: Int32(tab.rawValue),
                showBottomBar: false,
                lockTabToInitial: true,
                onCartCountChanged: { rawCount in
                    onCartCountChanged(normalizeToInt32(rawCount))
                },
                onBottomBarVisibilityChanged: { rawVisibility in
                    onBottomBarVisibilityChanged(normalizeToInt32(rawVisibility))
                }
            )
        }

        func viewController() -> UIViewController {
            let controller = host.viewController()
            controller.view.backgroundColor = .clear
            controller.view.isOpaque = false
            clearSubviewBackgrounds(controller.view)
            return controller
        }

        func deliverPaymentReturnIfNeeded(_ payload: PaymentReturnPayload?) {
            guard let payload else { return }
            guard payload.eventId > handledEventId else { return }
            handledEventId = payload.eventId
            host.onPaymentReturn(
                status: payload.status,
                orderId: Int32(payload.orderId ?? -1),
                eventId: payload.eventId
            )
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(
            tab: tab,
            onCartCountChanged: onCartCountChanged,
            onBottomBarVisibilityChanged: onBottomBarVisibilityChanged
        )
    }

    func makeUIViewController(context: Context) -> UIViewController {
        context.coordinator.viewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        if selectedTab == tab, let tabBarController = uiViewController.findTabBarController() {
            tabBarController.tabBar.isHidden = !isTabBarVisible
            tabBarController.additionalSafeAreaInsets.bottom = isTabBarVisible ? -6 : 0
        }

        guard tab == .cart && selectedTab == .cart else { return }
        context.coordinator.deliverPaymentReturnIfNeeded(paymentReturnPayload)
    }
}

private func normalizeToInt32<T>(_ rawValue: T) -> Int32 {
    if let value = rawValue as? Int32 { return value }
    if let value = rawValue as? KotlinInt { return value.int32Value }
    if let value = rawValue as? NSNumber { return value.int32Value }
    if let value = rawValue as? Int { return Int32(value) }
    return 0
}

private func clearSubviewBackgrounds(_ view: UIView) {
    view.backgroundColor = .clear
    view.isOpaque = false
    for subview in view.subviews {
        clearSubviewBackgrounds(subview)
    }
}

private extension UIViewController {
    func findTabBarController() -> UITabBarController? {
        if let tab = self.tabBarController {
            return tab
        }
        var parentController = parent
        while parentController != nil {
            if let tab = parentController as? UITabBarController {
                return tab
            }
            parentController = parentController?.parent
        }
        return nil
    }
}

private struct PaymentReturnPayload {
    let status: String
    let orderId: Int?
    let eventId: Int64
}

private func parsePaymentReturn(from url: URL) -> (status: String, orderId: Int?)? {
    let host = (url.host ?? "").lowercased()
    let path = url.path.lowercased()
    let pathComponents = url.pathComponents.map { $0.lowercased() }
    let hasPaymentReturnMarker =
        host == "payment-return" ||
        pathComponents.contains("payment-return") ||
        path.contains("payment-return")

    guard hasPaymentReturnMarker else { return nil }

    let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
    let queryItems = components?.queryItems ?? []
    let queryStatus = queryItems
        .first(where: { $0.name == "status" })?
        .value?
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .lowercased()

    let status: String?
    if let queryStatus, !queryStatus.isEmpty {
        status = queryStatus
    } else if path.contains("order-received") || path.contains("success") {
        status = "success"
    } else if path.contains("cancel") {
        status = "canceled"
    } else if path.contains("fail") {
        status = "failed"
    } else {
        status = nil
    }

    guard let status else { return nil }

    let orderId = queryItems
        .first(where: { $0.name == "order_id" })?
        .value
        .flatMap(Int.init)

    return (status, orderId)
}

#Preview {
    ContentView()
}
