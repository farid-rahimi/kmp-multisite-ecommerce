import SwiftUI
import sharedUIKit
import UIKit

private enum RootTab: Int, CaseIterable {
    case home = 0
    case category = 1
    case cart = 2
    case account = 3

    var title: String {
        switch self {
        case .home: return "Home"
        case .category: return "Category"
        case .cart: return "Cart"
        case .account: return "Account"
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
    @State private var paymentReturnPayload: PaymentReturnPayload?
    @State private var paymentReturnEventId: Int64 = 0
    private let brandTint = BrandPalette.current

    var body: some View {
        TabView(selection: $selectedTab) {
            NativeTabHostView(
                tab: .home,
                selectedTab: selectedTab,
                paymentReturnPayload: nil
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .tabItem {
                Label(RootTab.home.title, systemImage: selectedTab == .home ? RootTab.home.selectedSystemImage : RootTab.home.systemImage)
            }
            .tag(RootTab.home)

            NativeTabHostView(
                tab: .category,
                selectedTab: selectedTab,
                paymentReturnPayload: nil
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .tabItem {
                Label(RootTab.category.title, systemImage: selectedTab == .category ? RootTab.category.selectedSystemImage : RootTab.category.systemImage)
            }
            .tag(RootTab.category)

            NativeTabHostView(
                tab: .cart,
                selectedTab: selectedTab,
                paymentReturnPayload: paymentReturnPayload
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .tabItem {
                Label(RootTab.cart.title, systemImage: selectedTab == .cart ? RootTab.cart.selectedSystemImage : RootTab.cart.systemImage)
            }
            .tag(RootTab.cart)

            NativeTabHostView(
                tab: .account,
                selectedTab: selectedTab,
                paymentReturnPayload: nil
            )
            .ignoresSafeArea(.container, edges: .bottom)
            .tabItem {
                Label(RootTab.account.title, systemImage: selectedTab == .account ? RootTab.account.selectedSystemImage : RootTab.account.systemImage)
            }
            .tag(RootTab.account)
        }
        .tint(brandTint)
        .toolbarBackground(.hidden, for: .tabBar)
        .background(TabBarConfigurator())
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
            tabBarController.additionalSafeAreaInsets.bottom = -6
        }
    }
}

private struct NativeTabHostView: UIViewControllerRepresentable {
    let tab: RootTab
    let selectedTab: RootTab
    let paymentReturnPayload: PaymentReturnPayload?

    final class Coordinator {
        private let host: IosProductListComposeHost
        var handledEventId: Int64 = 0

        init(tab: RootTab) {
            host = IosProductListComposeHost(
                initialTabIndex: Int32(tab.rawValue),
                showBottomBar: false,
                lockTabToInitial: true
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
        Coordinator(tab: tab)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        context.coordinator.viewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        guard tab == .cart && selectedTab == .cart else { return }
        context.coordinator.deliverPaymentReturnIfNeeded(paymentReturnPayload)
    }
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
