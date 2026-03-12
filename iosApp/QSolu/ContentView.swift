import SwiftUI
import sharedUIKit

struct ContentView: View {
    @State private var paymentReturnPayload: PaymentReturnPayload?
    @State private var paymentReturnEventId: Int64 = 0

    var body: some View {
        SharedHomeComposableView(paymentReturnPayload: paymentReturnPayload)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .ignoresSafeArea(.all)
            .background(Color(uiColor: .systemBackground))
            .onOpenURL { url in
                guard let parsed = parsePaymentReturn(from: url) else { return }
                paymentReturnEventId += 1
                paymentReturnPayload = PaymentReturnPayload(
                    status: parsed.status,
                    orderId: parsed.orderId,
                    eventId: paymentReturnEventId
                )
            }
    }
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

struct SharedHomeComposableView: UIViewControllerRepresentable {
    fileprivate let paymentReturnPayload: PaymentReturnPayload?

    final class Coordinator {
        let host = IosProductListComposeHost()
        var handledEventId: Int64 = 0
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = context.coordinator.host.viewController()
        controller.view.backgroundColor = .systemBackground
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        guard let payload = paymentReturnPayload else { return }
        guard payload.eventId > context.coordinator.handledEventId else { return }

        context.coordinator.handledEventId = payload.eventId
        context.coordinator.host.onPaymentReturn(
            status: payload.status,
            orderId: Int32(payload.orderId ?? -1),
            eventId: payload.eventId
        )
    }
}

fileprivate struct PaymentReturnPayload {
    let status: String
    let orderId: Int?
    let eventId: Int64
}

#Preview {
    ContentView()
}
